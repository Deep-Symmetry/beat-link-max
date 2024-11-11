package beatlink;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;
import org.apiguardian.api.API;

/**
 * Created by James Elliott on 11/10/24.
 */
@API(status = API.Status.EXPERIMENTAL)
public class Connect extends MaxObject {
    /**
     * Constructor sets up and describes the inlets and outlets.
     */
    public Connect() {
        declareInlets(new int[]{DataTypes.ALL});
        setInletAssist(new String[] {"send true/non-zero value to go online"});
        declareOutlets(new int[]{DataTypes.ALL, DataTypes.ALL});
        setOutletAssist(new String[]{"reports devices found", "reports devices lost"});
    }
}
