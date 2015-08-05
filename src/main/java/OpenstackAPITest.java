import java.io.IOException;
import co.fastcat.openstack.OpenstackAPI;
/**
 * Created by user on 2015-08-05.
 */
public class OpenstackAPITest {
    public static void main(String[] args) throws IOException {

        OpenstackAPI testControl = new OpenstackAPI("http://10.0.1.65:5000/v2.0/", "openstack-nova", "admin:admin", "3f4a0c469aa9451d");

        try {
            testControl.listInstance();
            testControl.loadFloatingIpsForInstance("c27149f4-5a6b-4577-8c10-a95f5060c0d4getInstancesInfo");
            testControl.getInstanceInfo("ef9484e8-99e2-4878-b1e9-0785fadc5415");
            testControl.terminateInstance("9da274bf-5a42-4f0c-ac2c-d5ee09e76c0a");
            testControl.stopInstance("ef9484e8-99e2-4878-b1e9-0785fadc5415");
            testControl.createInstance("RegionOne", "test_api_create_instance3", "19257f34-d638-4343-a0c7-c7e2f407ebff", "2", "30711dfc-49b3-4d28-b4b8-942847551db2", "1234", "ubuntu_test");
            System.out.println(testControl.getInstancesInfo().get(1).getId());

            testControl.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            testControl.close();
        }
    }
}
