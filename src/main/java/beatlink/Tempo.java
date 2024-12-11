package beatlink;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;
import org.apiguardian.api.API;
import org.deepsymmetry.beatlink.*;

/**
 * An MXJ object that allows you to track tempo and beats for a particular player or the current master player.
 */
@API(status = API.Status.EXPERIMENTAL)
public class Tempo extends MaxObject {

    /**
     * The attribute that keeps track of the player whose tempo we are interested in. Zero means the current
     * tempo master.
     */
    private int player = 0;

    /**
     * Keeps track of the most recent tempo value we have seen for a player so we can report changes, or repeat
     * it when we receive a bang.
     */
    private double lastTempo = 0.0;

    /**
     * Helper method to send the last-known tempo to the tempo outlet.
     */
    private void reportTempo() {
        outlet(0, lastTempo);
    }

    /**
     * Keeps track if we have received our loadBang message, so we know that tempo changes can be safely reported.
     */
    private boolean loaded = false;

    /**
     * Helper method to record a new tempo, and if is different from the last value we reported, send a new report.
     *
     * @param tempo the updated tempo information we have received.
     */
    private void changeTempo(double tempo) {
        final double prevTempo = lastTempo;
        lastTempo = tempo;
        if (lastTempo != prevTempo) {
            reportTempo();
        }
    }

    /**
     * Helper function called at load time or when changing player numbers to update the tempo to the current master
     * tempo if we are configured to watch the master player, and are currently online.
     */
    private void reportMasterTempoIfNeeded() {
        if (player == 0 && VirtualCdj.getInstance().isRunning()) {
            changeTempo(VirtualCdj.getInstance().getMasterTempo());
        }
    }

    /**
     * Sets the player attribute. If this represents a change, report a zero tempo since we don't yet know it.
     * Even though this appears not to be used, {@link MaxObject} will invoke it reflectively.
     *
     * @param n the device number of the player to watch, 1 through 6, or 0 to watch the master player.
     */
    @SuppressWarnings("unused")
    private void setPlayer(int n) {
        if (n < 0 || n > 6) {
            MaxObject.error("Tempo object's player attribute must be in range 0-6");
            return;
        }
        if (player != n) {
            changeTempo(0.0);
        }
        player = n;
        if (loaded) {
            reportMasterTempoIfNeeded();
        }
    }

    /**
     * Report that a beat has occurred on the player we are configured to watch; if it is a downbeat, report that
     * too.
     *
     * @param beat the beat event reported by Beat Link, so we can check the device number and beat within bar.
     */
    private void reportBeat(Beat beat) {
        outlet(1, beat.getBeatWithinBar());
        if (beat.getBeatWithinBar() == 1) {
            outlet(2, 1);
        }
    }

    /**
     * Used to report tempo changes and beats when we are configured to watch the master player.
     */
    private final MasterListener masterListener = new MasterListener() {
        @Override
        public void masterChanged(DeviceUpdate update) {
            // Nothing to do.
        }

        @Override
        public void tempoChanged(double tempo) {
            if (player == 0) {
                changeTempo(tempo);
            }
        }

        @Override
        public void newBeat(Beat beat) {
            if (player == 0) {
                reportBeat(beat);
            }
        }
    };

    /**
     * Used to report beats when we are configured to watch a specific player.
     */
    private final BeatListener beatListener = beat -> {
        if (beat.getDeviceNumber() == player) {
            reportBeat(beat);
        }
    };

    /**
     * Used to report tempo changes when we are configured to watch a specific player.
     */
    private final DeviceUpdateListener updateListener = update -> {
        if (update.getDeviceNumber() == player && update.getBpm() != 0xffff) {
            // It's the player we are configured to watch, and tempo is known.
            changeTempo(update.getEffectiveTempo());
        }
    };

    /**
     * Sets up and describes the inlets, outlets, and attributes, and registers our listeners.
     */
    public Tempo() {
        declareInlets(new int[]{DataTypes.ALL});
        setInletAssist(new String[] {"bang to query current tempo"});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        setOutletAssist(new String[]{"reports tempo changes", "reports beats", "reports down beats"});
        declareAttribute("player", null, "setPlayer");

        VirtualCdj.getInstance().addMasterListener(masterListener);
        VirtualCdj.getInstance().addUpdateListener(updateListener);
        BeatFinder.getInstance().addBeatListener(beatListener);
    }

    @Override
    protected void loadbang() {
        super.loadbang();
        loaded = true;
        reportMasterTempoIfNeeded();
    }

    @Override
    protected void bang() {
        reportTempo();
    }

    @Override
    protected void notifyDeleted() {
        VirtualCdj.getInstance().removeMasterListener(masterListener);
        VirtualCdj.getInstance().removeUpdateListener(updateListener);
        BeatFinder.getInstance().removeBeatListener(beatListener);
        super.notifyDeleted();
    }
}
