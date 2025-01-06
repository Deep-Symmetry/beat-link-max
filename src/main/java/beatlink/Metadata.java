package beatlink;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;
import org.apiguardian.api.API;
import org.deepsymmetry.beatlink.data.MetadataFinder;
import org.deepsymmetry.beatlink.data.TrackMetadata;
import org.deepsymmetry.beatlink.data.TrackMetadataUpdate;

/**
 * An MXJ object that reports metadata about tracks on a particular player as it becomes available.
 */
@API(status = API.Status.EXPERIMENTAL)
public class Metadata extends MaxObject {

    /**
     * The attribute that keeps track of the player whose tempo we are interested in.
     */
    private volatile int player = 1;

    /**
     * Informs the patch that we have no metadata information available for the player.
     */
    private void reportNoMetadata() {
        outlet(0, new Atom[] {});
    }

    /**
     * Informs the patch of the metadata available for the monitored player.
     */
    private void reportMetadata(TrackMetadata metadata) {
        if (metadata == null) {
            reportNoMetadata();
        } else {
            outlet(0, new Atom[]{
                    Atom.newAtom(metadata.getTitle()),
                    Atom.newAtom(Util.labelIfNotNull(metadata.getArtist())),
                    Atom.newAtom(Util.labelIfNotNull(metadata.getAlbum())),
                    Atom.newAtom(Util.stringIfNotNull(metadata.getComment())),
                    Atom.newAtom(Util.labelIfNotNull(metadata.getGenre())),
                    Atom.newAtom(metadata.getTempo() / 100.0),
                    Atom.newAtom(metadata.getDuration())
            });
        }
    }

    /**
     * Informs the patch of the metadata available if the player number matches the monitored player.
     */
    private void reportMetadata(TrackMetadataUpdate update) {
        if (update.player == player) {
            reportMetadata(update.metadata);
        }
    }

    /**
     * Sets the player attribute. If this represents a change, report a lack of metadata since we don't yet know it.
     * Even though this appears not to be used, {@link MaxObject} will invoke it reflectively.
     *
     * @param n the device number of the player to watch, 1 through 6.
     */
    @SuppressWarnings("unused")
    private void setPlayer(int n) {
        if (n < 1 || n > 6) {
            MaxObject.error("Position object's player attribute must be in range 1-6");
            return;
        }
        if (player != n) {
            player = n;
            bang();  // Report metadata if we have any available.
        }
    }

    /**
     * Sets up and describes the inlets, outlets, and attributes, and registers our listeners.
     */
    @API(status = API.Status.EXPERIMENTAL)
    public Metadata() {
        declareInlets(new int[]{DataTypes.ALL});
        setInletAssist(new String[] {"bang to query current metadata"});
        declareOutlets(new int[]{DataTypes.LIST});
        setOutletAssist(new String[]{"empty if no metadata, otherwise title, artist"});
        declareAttribute("player", null, "setPlayer");
        MetadataFinder.getInstance().addTrackMetadataListener(this::reportMetadata);
    }

    @Override
    protected void bang() {
        if (MetadataFinder.getInstance().isRunning()) {
            reportMetadata(MetadataFinder.getInstance().getLatestMetadataFor(player));
        }
    }

    @Override
    protected void notifyDeleted() {
        MetadataFinder.getInstance().removeTrackMetadataListener(this::reportMetadata);
        super.notifyDeleted();
    }
}
