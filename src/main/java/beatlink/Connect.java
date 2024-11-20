package beatlink;

import com.cycling74.max.Atom;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;
import org.apiguardian.api.API;
import org.deepsymmetry.beatlink.*;
import org.deepsymmetry.beatlink.data.CrateDigger;
import org.deepsymmetry.beatlink.data.MetadataFinder;
import org.deepsymmetry.beatlink.data.SignatureFinder;
import org.deepsymmetry.beatlink.data.TimeFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by James Elliott on 11/10/24.
 */
@API(status = API.Status.EXPERIMENTAL)
public class Connect extends MaxObject {

    private static final Logger logger = LoggerFactory.getLogger(Connect.class);

    /**
     * Used to track and report the current DJ Link connection state, and to ensure starting and stopping
     * can only be attempted from appropriate states, even if multiple instances of this object are created.
     */
    private static final AtomicReference<String> state = new AtomicReference<>("stopped");

    /**
     * Constructor sets up and describes the inlets and outlets.
     */
    public Connect() {
        Util.initializeLogging();
        declareInlets(new int[]{DataTypes.ALL});
        setInletAssist(new String[] {"send \"start\" to go online, \"stop\" to go offline"});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL});
        setOutletAssist(new String[]{"reports status changes", "reports devices found/lost"});

        DeviceFinder.getInstance().addLifecycleListener(new LifecycleListener() {
            @Override
            public void started(LifecycleParticipant sender) {
                // Nothing to do here.
            }

            @Override
            public void stopped(LifecycleParticipant sender) {
                state.set("stopped");
                outlet(0, "stopped");
            }
        });

        TimeFinder.getInstance().addLifecycleListener(new LifecycleListener() {
            @Override
            public void started(LifecycleParticipant sender) {
                state.set("started");
                outlet(0, "started");
            }

            @Override
            public void stopped(LifecycleParticipant sender) {
                // Nothing to do here.
            }
        });

        DeviceFinder.getInstance().addDeviceAnnouncementListener(new DeviceAnnouncementListener() {
            @Override
            public void deviceFound(DeviceAnnouncement announcement) {
                outlet(1, "found",
                        new Atom[]{Atom.newAtom(announcement.getDeviceName()),
                                Atom.newAtom(announcement.getDeviceNumber())});
            }

            @Override
            public void deviceLost(DeviceAnnouncement announcement) {
                outlet(1, "lost",
                        new Atom[]{Atom.newAtom(announcement.getDeviceName()),
                                Atom.newAtom(announcement.getDeviceNumber())});
            }
        });
    }

    /**
     * Joins a Pro DJ Link network. Separated into its own method so it can be run on a background thread.
     */
    private void tryGoingOnline() {
        if (state.compareAndSet("stopped", "starting")) {
            outlet(0, "starting");
            try {
                DeviceFinder.getInstance().start();
            } catch (Exception e) {
                state.set("stopped");
                outlet(0, "stopped");
                logger.error("Unable to start DeviceFinder", e);
                MaxObject.error("Unable to go online: " + e);
                return;
            }
            try {
                if (VirtualCdj.getInstance().start()){
                    logger.info("Virtual CDJ running as Player {}", VirtualCdj.getInstance().getDeviceNumber());
                    MetadataFinder.getInstance().start();
                    MetadataFinder.getInstance().setPassive(true);  // Start out conservatively.
                    CrateDigger.getInstance().start();
                    SignatureFinder.getInstance().start();
                    TimeFinder.getInstance().start();
                } else {
                    logger.error("Unable to start VirtualCdj");
                    MaxObject.error("Unable to go online.");
                    DeviceFinder.getInstance().stop();
                }
            } catch (Exception e) {
                logger.error("Unable to start VirtualCdj", e);
                MaxObject.error("Unable to go online: " + e);
                DeviceFinder.getInstance().stop();
            }
        } else {
            MaxObject.error("Can only start if current state is stopped");
        }

    }

    /**
     * Takes us off the Pro DJ Link network. Separated into its own method so it can be run on a background thread.
     */
    private void goOffline() {
        if (state.compareAndSet("started", "stopping")) {
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

    /**
     * When we receive a stop message, we are supposed to go offline.
     */
    @API(status = API.Status.EXPERIMENTAL)
    public void stop() {
        new Thread(this::goOffline).start();
    }

}
