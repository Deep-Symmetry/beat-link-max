package beatlink;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;
import org.apiguardian.api.API;
import org.deepsymmetry.beatlink.DeviceFinder;
import org.deepsymmetry.beatlink.VirtualCdj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by James Elliott on 11/10/24.
 */
@API(status = API.Status.EXPERIMENTAL)
public class Connect extends MaxObject {

    private static final Logger logger = LoggerFactory.getLogger(Connect.class);

    /**
     * Constructor sets up and describes the inlets and outlets.
     */
    public Connect() {
        Util.initializeLogging();
        declareInlets(new int[]{DataTypes.ALL});
        setInletAssist(new String[] {"send \"start\" to go online, \"stop\" to go offline"});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL});
        setOutletAssist(new String[]{"reports devices found", "reports devices lost"});
        logger.info("Instantiated.");
    }

    private synchronized void tryGoingOnline() {
        try {
            DeviceFinder.getInstance().start();
        } catch (Exception e) {
            logger.error("Unable to start DeviceFinder", e);
            MaxObject.error("Unable to go online: " + e);
        }
        try {
            VirtualCdj.getInstance().start();
        } catch (Exception e) {
            logger.error("Unable to start VirtualCdj", e);
            MaxObject.error("Unable to go online: " + e);
            DeviceFinder.getInstance().stop();
        }
    }

    /**
     * When we receive a start message, we are supposed to go online.
     */
    @API(status = API.Status.EXPERIMENTAL)
    public void start() {
        new Thread(this::tryGoingOnline).start();
    }
}
