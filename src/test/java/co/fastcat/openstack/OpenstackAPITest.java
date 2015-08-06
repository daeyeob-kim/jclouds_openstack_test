package co.fastcat.openstack;

import org.jclouds.openstack.nova.v2_0.domain.Server;

import java.util.List;
import java.util.Random;

/**
 * �ۼ���: ������, ��뿱
 * �ۼ���: 2015. 08. 05 ~
 * OpenstackAPITest
 * OpenstackAPI Ŭ������ �׽�Ʈ�� ���� Testcase
 */
public class OpenstackAPITest {
    public static void main(String[] args) throws Exception {

        // admin ������Ʈ, admin �������� 65�� ������ ���½��� ������ ����
        OpenstackAPI testControl = new OpenstackAPI("http://10.0.1.65:5000/v2.0/", "openstack-nova", "admin", "admin", "3f4a0c469aa9451d");
        List<Server> instanceList;
        int number_of_instanceList;
        Random randomNumber = new Random();
        String createdInstanceId; /* Testcase 1 �� ������� �ν��Ͻ� ID */

        // TODO 1: Testcase 1
        /*
         * �ν��Ͻ� ����
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
        * ��� ������ �ν��Ͻ��� ���� �����ְ�, Floating IP ������ �� Floating IP ������ �����ֱ�.
        * */
        testControl.getInstanceInfo(createdInstanceId).addFloatingIp(createdInstanceId).loadFloatingIpsForInstance(createdInstanceId);

        /*
        * �ν��Ͻ� ����
        * */
        testControl.terminateInstance(createdInstanceId);

        // TODO 2: Testcase 2 and more Testcase

        /*
         * �ν��Ͻ� ����Ʈ ����� �� �ش� ����Ʈ�� getInstanceList�� �ִ´�.
         */
        /*
        instanceList = testControl.listInstance().getInstancesList();
        number_of_instanceList = instanceList.size();
        */

        /*
        * �ν��Ͻ����� ���� ���, �׸��� Floating IP ���� ���
        */
       /* for (int instanceList_count = 0; instanceList_count < number_of_instanceList; instanceList_count++) {
            testControl.getInstanceInfo(instanceList.get(instanceList_count)).loadFloatingIpsForInstance(instanceList.get(instanceList_count));
        }*/

        /*
        * �ν��Ͻ� Shut Off
        */
//      testControl.stopInstance(instanceList.get(0).getId());

         /*
         * �ν��Ͻ� ����
         */
//       testControl.terminateInstance(instanceList.get(0).getId());

        /*
        * ��� �ν��Ͻ� �ϰ������� Floating IP �־��ֱ�
        * */
        /*
        for (int instance_list_count = (number_of_instanceList-1); instance_list_count < 0; instance_list_count--) {
            testControl.addFloatingIp(instanceList.get(instance_list_count).getId());
        }
        */

        /*
        * Openstack API ����
        * */
        testControl.close();
    }
}
