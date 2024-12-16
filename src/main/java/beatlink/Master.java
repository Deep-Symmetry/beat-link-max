package beatlink;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;
import org.apiguardian.api.API;
import org.deepsymmetry.beatlink.DeviceUpdate;
import org.deepsymmetry.beatlink.MasterAdapter;
import org.deepsymmetry.beatlink.MasterListener;
import org.deepsymmetry.beatlink.VirtualCdj;

/**
 * An MXJ object that reports the current master player number whenever that changes, or reports zero if there is
 * no longer a tempo master. Nonzero messages from this object can be prepended with "player" and used to configure
 * player-specific objects like {@link Position} to always be following the current tempo master.
 */
@API(status = API.Status.EXPERIMENTAL)
public class Master extends MaxObject {

    /**
     * Allows us to learn about tempo master changes, so we can pass them on.
     */
    private final MasterListener listener = new MasterAdapter() {
        @Override
        public void masterChanged(DeviceUpdate update) {
            if (update == null) {
                outlet(0, 0);
            } else {
                outlet(0, update.getDeviceNumber());
            }
        }
    };

    /**
     * Configures our single outlet and registers our listener.
     */
    public Master() {
        declareInlets(new int[] {DataTypes.ALL});
        setInletAssist(new String[] {"bang to report current master player"});
        declareOutlets(new int[] {DataTypes.INT});
        setOutletAssist(new String[] {"master player number, or 0 if none"});
        createInfoOutlet(false);
        VirtualCdj.getInstance().addMasterListener(listener);
    }

    @Override
    protected void notifyDeleted() {
        VirtualCdj.getInstance().removeMasterListener(listener);
        super.notifyDeleted();
    }

    @Override
    protected void bang() {
        DeviceUpdate masterUpdate = null;
        if (VirtualCdj.getInstance().isRunning()) {
            masterUpdate = VirtualCdj.getInstance().getTempoMaster();
        }
        listener.masterChanged(masterUpdate);
    }

    @Override
    protected void loadbang() {
        DeviceUpdate masterUpdate = null;
        if (VirtualCdj.getInstance().isRunning()) {
            masterUpdate = VirtualCdj.getInstance().getTempoMaster();
        }
        listener.masterChanged(masterUpdate);
        super.loadbang();
    }
}
