package beatlink;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;
import org.apiguardian.api.API;
import org.deepsymmetry.beatlink.data.*;

/**
 * An MXJ object that allows you to track playback position for a particular player.
 */
@API(status = API.Status.EXPERIMENTAL)
public class Position extends MaxObject {

    /**
     * The attribute that keeps track of the player whose tempo we are interested in.
     */
    private int player = 1;

    /**
     * Informs the patch that we have no position information for the player.
     */
    private void reportNoPosition() {
        outlet(0, -1);   // Playback position in ms
        outlet(1, 0);    // Track length in ms
        outlet(2, 0.0);  // Playback pitch
        outlet(3, false);    // Playing flag
        // Beat number, reverse flag, definitive flag, precise flag:
        outlet(4, new Atom[] {Atom.newAtom(0), Atom.newAtom(false), Atom.newAtom(false), Atom.newAtom(false)});
    }

    /**
     * Informs the patch of the current interpolated playback position of the monitored player.
     *
     * @param time the (possibly interpolated) playback position of the player
     * @param definitive indicates whether we are certain of the time, only ever {@code true} when this report comes directly from a received update
     *                  that is itself definitive
     * @param lastUpdate the most recent position update received from the player
     */
    private void reportPosition(long time, boolean definitive, TrackPositionUpdate lastUpdate) {

        if (lastUpdate == null || !TimeFinder.getInstance().isRunning() || time < 0) {
            reportNoPosition();
            return;
        }

        outlet(0, time);  // Playback position in ms
        WaveformDetail waveform = null;
        if (WaveformFinder.getInstance().isRunning()) {
            waveform = WaveformFinder.getInstance().getLatestDetailFor(player);
        }
        // Track length in ms
        if (waveform == null) {
            outlet(1, 0);
        } else {
            outlet(1, waveform.getTotalTime());
        }
        outlet(2, lastUpdate.pitch);
        outlet(3, lastUpdate.playing);
        outlet(4, new Atom[] {Atom.newAtom(lastUpdate.beatNumber),
                Atom.newAtom(lastUpdate.reverse),
                Atom.newAtom(definitive),
                Atom.newAtom(lastUpdate.precise)});
    }

    /**
     * Informs the patch of the current track position just reported by the monitored player.
     *
     * @param update the current track position information just received from Beat Link, or {@code null} if we have lost position information for the player.
     */
    private void reportPosition(TrackPositionUpdate update) {
        if (update != null) {
            reportPosition(update.milliseconds, update.definitive, update);
        } else {
            reportPosition(-1, false, null);
        }
    }

    /**
     * Sets the player attribute. If this represents a change, report a zero tempo since we don't yet know it.
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
            TimeFinder.getInstance().addTrackPositionListener(player, this::reportPosition);  // Replaces old registration with new player number.
        }
    }

    /**
     * Used to send updates when movement is reported by Beat Link.
     */
    private final TrackPositionListener trackPositionListener = this::reportPosition;

    /**
     * Used to send updates when we gain information about the track length from its waveform.
     */
    private final WaveformListener waveformListener = new WaveformListener() {
        @Override
        public void previewChanged(WaveformPreviewUpdate update) {
            // Nothing to do here, we only care about detailed waveforms.
        }

        @Override
        public void detailChanged(WaveformDetailUpdate update) {
            if (update.player == player) {
                reportPosition(null);
            }
        }
    };

    /**
     * Sets up and describes the inlets, outlets, and attributes, and registers our listeners.
     */
    public Position() {
        declareInlets(new int[]{DataTypes.ALL});
        setInletAssist(new String[] {"bang to query current position"});
        declareOutlets(new int[]{DataTypes.INT, DataTypes.INT, DataTypes.FLOAT, DataTypes.INT, DataTypes.LIST});
        setOutletAssist(new String[]{"track position in milliseconds",
                "total track length in milliseconds",
                "playback pitch (1.0 = normal speed)",
                "playing flag (track playing if nonzero)",
                "list of beat number, reverse flag, definitive flag, precise flag"
        });
        declareAttribute("player", null, "setPlayer");

        TimeFinder.getInstance().addTrackPositionListener(player, trackPositionListener);
        WaveformFinder.getInstance().addWaveformListener(waveformListener);
    }

    @Override
    protected void bang() {
        if (TimeFinder.getInstance().isRunning()) {
            reportPosition(TimeFinder.getInstance().getTimeFor(player), false, TimeFinder.getInstance().getLatestPositionFor(player));
        } else {
            reportPosition(null);
        }
    }

    @Override
    protected void notifyDeleted() {
        TimeFinder.getInstance().removeTrackPositionListener(trackPositionListener);
        WaveformFinder.getInstance().removeWaveformListener(waveformListener);
        super.notifyDeleted();
    }
}
