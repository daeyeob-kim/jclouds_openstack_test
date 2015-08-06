import java.io.IOException;
import co.fastcat.openstack.OpenstackAPI;
/**
 * Created by user on 2015-08-05.
 */
public class OpenstackAPITest {
    public static void main(String[] args) throws IOException {

        OpenstackAPI testControl = new OpenstackAPI("http://10.0.1.65:5000/v2.0/", "openstack-nova", "admin:admin", "3f4a0c469aa9451d");

        try {
            /*
             * 인스턴스 리스트 출력
             */
            //testControl.listInstance();

            /*
             * 특정 인스턴스의 Floating IP 출력
             */
            //testControl.loadFloatingIpsForInstance("c27149f4-5a6b-4577-8c10-a95f5060c0d4getInstancesInfo");

            /*
             * 특정 인스턴스의 Floating IP 출력
             */
            //testControl.getInstanceInfo("ef9484e8-99e2-4878-b1e9-0785fadc5415");


            /*
             * 인스턴스 Shut Off
             */
            //testControl.stopInstance("ef9484e8-99e2-4878-b1e9-0785fadc5415");

            /*
             * 인스턴스 종료
             */
            //testControl.terminateInstance("9da274bf-5a42-4f0c-ac2c-d5ee09e76c0a");


            /*
             * 인스턴스 실행
             */
            //testControl.createInstance("RegionOne", "test_api_create_instance4", "19257f34-d638-4343-a0c7-c7e2f407ebff", "2", "30711dfc-49b3-4d28-b4b8-942847551db2", "1234", "ubuntu_test");

            /*
             *
             */
            //System.out.println(testControl.getInstancesInfo().get(1).getId());

            /*
             * 인스턴스에 Floating IP 배정정
             */
           testControl.addFloatingIp();

             /*
             * Openstack API 종료
             */
            testControl.close();

        } catch (Exception e) {

            e.printStackTrace();
        } finally {

            testControl.close();
        }
    }
}
