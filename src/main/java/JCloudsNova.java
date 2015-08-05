import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.loaders.LoadFloatingIpsForInstance;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.regionscoped.RegionAndId;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

public class JCloudsNova implements Closeable {
    private final NovaApi novaApi;
    private final Set<String> regions;

    public static void main(String[] args) throws IOException {
        JCloudsNova jcloudsNova = new JCloudsNova();

        try {
 //           jcloudsNova.listServers();
 //           jcloudsNova.loadFloatingIpsForInstance("c27149f4-5a6b-4577-8c10-a95f5060c0d4getInstancesInfo");
//            jcloudsNova.getInstanceInfo("ef9484e8-99e2-4878-b1e9-0785fadc5415");
 //           jcloudsNova.terminateInstance("9da274bf-5a42-4f0c-ac2c-d5ee09e76c0a");
//            jcloudsNova.stopServers();
//            jcloudsNova.createInstance();
//            System.out.println(jcloudsNova.getInstancesInfo().get(1).getId());

            jcloudsNova.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jcloudsNova.close();
        }
    }

    public JCloudsNova() {
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());

        String provider = "openstack-nova";
        String identity = "admin:admin"; // tenantName:userName
        String credential = "3f4a0c469aa9451d";

        novaApi = ContextBuilder.newBuilder(provider)
                .endpoint("http://10.0.1.65:5000/v2.0/")
                .credentials(identity, credential)
                .modules(modules)
                .buildApi(NovaApi.class);
        regions = novaApi.getConfiguredRegions();
    }

    //현재 서버 전체의 리스트를 출력
    private void listServers() {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);

            for (Server server : serverApi.listInDetail().concat()) {
                System.out.printf(server.toString());
            }
        }
    }
    //현재 서버 전체의 리스트를 list에 담아서 리턴
    private List<Server> getInstancesInfo(){
        List<Server> servers = new ArrayList<Server>();
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);

            for (Server server : serverApi.listInDetail().concat()) {
                servers.add(server);
            }
        }
        return servers;
    }
    //인스턴스 id를 입력, 해당 인스턴스의 정보를 출력
    private void getInstanceInfo(String instanceId) {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);
            if(serverApi.get(instanceId) != null)
                System.out.println(serverApi.get(instanceId).toString());
        }
    }

    //인스턴스 id를 입력, 해당 인스턴스의 객체를 리턴(Server)
    public Server getInstanceObject(String instanceId) {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);
            if(serverApi.get(instanceId) != null)
                return serverApi.get(instanceId);
        }
        return null;
    }

    //인스턴스 id를 입력, 해당 인스턴스를 정지(shut down)
    private void stopInstance(String instanceId) {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);
            try {
                serverApi.stop(instanceId);
            }catch(Exception e){
                System.out.println(e.toString()); //로거로 error 쏴주어야 함..
            }
        }
    }

    //인스턴스 id를 입력, 해당 인스턴스를 정지(terminate)
    private void terminateInstance(String instatnceId) {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);
            try {
                serverApi.delete(instatnceId);
            }catch(Exception e){
                System.out.println(e.toString()); //로거로 error 쏴주어야 함..
            }
        }
    }

    private void createInstance() {
        CreateServerOptions option = new CreateServerOptions();
        option.networks("30711dfc-49b3-4d28-b4b8-942847551db2");
        option.adminPass("1234");
        option.keyPairName("ubuntu_test");

        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);
            serverApi.create("test_api_create_instance", "19257f34-d638-4343-a0c7-c7e2f407ebff", "2", option);
        }
    }

   //인스턴스 id를 입력하면, 할당된 floating ip를 출력
    private String loadFloatingIpsForInstance(String instatnceId) {
        for (String region : regions) {

            LoadFloatingIpsForInstance loadFloatingIpsForInstance = new LoadFloatingIpsForInstance(novaApi);
            RegionAndId regionAndId = null;
            regionAndId = regionAndId.fromSlashEncoded(region+"/"+instatnceId);

            try {
                Iterable tmp = loadFloatingIpsForInstance.load(regionAndId);
                Iterator iterator = tmp.iterator();
                while (iterator.hasNext()) {
                    Object element = iterator.next();
                    if(fromColonStringToMap("FloatingIP",element.toString()).get("ip").toString() != null)
                        return fromColonStringToMap("FloatingIP",element.toString()).get("ip").toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //CustomString 파서..
    //FloatingIP{id=62c10231-126d-40db-aca3-989ab76e6a40, ip=10.0.1.183, fixedIp=10.0.0.25, instanceId=ef9484e8-99e2-4878-b1e9-0785fadc5415, pool=public2}
    //형식의 String을 map에 담음
    private Map fromColonStringToMap(String title,String inputString){
        Map<String, String> tmpMap = new HashMap<String, String>();
        String[] pairs = inputString.trim().substring(title.length()+1,inputString.length()-1).split(",");
        for (int i=0;i<pairs.length;i++) {
            String pair = pairs[i];
            String[] keyValue = pair.split("=");
            tmpMap.put(keyValue[0].trim(), keyValue[1].trim());
            System.out.println(keyValue[1].trim());
        }
        return tmpMap;
    }

    public void close() throws IOException {
        Closeables.close(novaApi, true);
    }
}