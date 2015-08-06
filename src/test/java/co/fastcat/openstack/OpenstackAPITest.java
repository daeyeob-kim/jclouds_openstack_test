package co.fastcat.openstack;

import org.jclouds.openstack.nova.v2_0.domain.Server;

import java.util.List;
import java.util.Random;

/**
 * 작성자: 전제현, 김대엽
 * 작성일: 2015. 08. 05 ~
 * OpenstackAPITest
 * OpenstackAPI 클래스의 테스트를 위한 Testcase
 */
public class OpenstackAPITest {
    public static void main(String[] args) throws Exception {

        // admin 프로젝트, admin 계정으로 65번 서버의 오픈스택 서버에 접근
        OpenstackAPI testControl = new OpenstackAPI("http://10.0.1.65:5000/v2.0/", "openstack-nova", "admin", "admin", "3f4a0c469aa9451d");
        List<Server> instanceList;
        int number_of_instanceList;
        Random randomNumber = new Random();
        String createdInstanceId; /* Testcase 1 용 만들어진 인스턴스 ID */

        // TODO 1: Testcase 1
        /*
         * 인스턴스 실행
         */
        createdInstanceId = testControl.launchInstance(
                "RegionOne",
                String.format("test_api_create_instance" + randomNumber.nextInt(100)),
                "19257f34-d638-4343-a0c7-c7e2f407ebff",
                "2",
                "30711dfc-49b3-4d28-b4b8-942847551db2",
                "1234",
                "ubuntu_test"
        );

        /*
        * 방금 실행한 인스턴스의 정보 보여주고, Floating IP 적용한 뒤 Floating IP 별도로 보여주기.
        * */
        testControl.getInstanceInfo(createdInstanceId).addFloatingIp(createdInstanceId).loadFloatingIpsForInstance(createdInstanceId);

        /*
        * 인스턴스 종료
        * */
        testControl.terminateInstance(createdInstanceId);

        // TODO 2: Testcase 2 and more Testcase

        /*
         * 인스턴스 리스트 출력한 뒤 해당 리스트를 getInstanceList에 넣는다.
         */
        /*
        instanceList = testControl.listInstance().getInstancesList();
        number_of_instanceList = instanceList.size();
        */

        /*
        * 인스턴스들의 정보 출력, 그리고 Floating IP 별도 출력
        */
       /* for (int instanceList_count = 0; instanceList_count < number_of_instanceList; instanceList_count++) {
            testControl.getInstanceInfo(instanceList.get(instanceList_count)).loadFloatingIpsForInstance(instanceList.get(instanceList_count));
        }*/

        /*
        * 인스턴스 Shut Off
        */
//      testControl.stopInstance(instanceList.get(0).getId());

         /*
         * 인스턴스 종료
         */
//       testControl.terminateInstance(instanceList.get(0).getId());

        /*
        * 모든 인스턴스 일괄적으로 Floating IP 넣어주기
        * */
        /*
        for (int instance_list_count = (number_of_instanceList-1); instance_list_count < 0; instance_list_count--) {
            testControl.addFloatingIp(instanceList.get(instance_list_count).getId());
        }
        */

        /*
        * Openstack API 종료
        * */
        testControl.close();
    }
}
