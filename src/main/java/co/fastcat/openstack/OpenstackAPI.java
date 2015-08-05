package co.fastcat.openstack;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
 * Created by user on 2015-08-05.
 */
public class OpenstackAPI implements Closeable {

    private static Logger logger = LoggerFactory.getLogger(OpenstackAPI.class);
    private final NovaApi novaApi;
    private final Set<String> regions;

    // OpenstackAPI ������
    public OpenstackAPI(String uri, String provider, String identity, String credential) {

        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());

        novaApi = ContextBuilder.newBuilder(provider)
                .endpoint(uri)
                .credentials(identity, credential)
                .modules(modules)
                .buildApi(NovaApi.class);
        regions = novaApi.getConfiguredRegions();
    }

    public String instanceNameToId(String instanceName, List<Server> servers){
        String instanceId = null;
        return instanceId;
    }

    //���� ���� ��ü�� ����Ʈ�� ���
    public void listInstance() {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);

            for (Server server : serverApi.listInDetail().concat()) {
                System.out.printf(server.toString());
            }
        }
    }

    //�ν��Ͻ� id�� �Է�, �ش� �ν��Ͻ��� ������ ���
    public void getInstanceInfo(String instanceId) {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);
            if(serverApi.get(instanceId) != null)
                System.out.println(serverApi.get(instanceId).toString());
        }
    }

    //�ν��Ͻ� id�� �Է�, �ش� �ν��Ͻ��� ����(shut down)
    public void stopInstance(String instanceId) {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);
            try {
                serverApi.stop(instanceId);
            }catch(Exception e){
                System.out.println(e.toString()); //�ΰŷ� error ���־�� ��..
            }
        }
    }


    //�ν��Ͻ� id�� �Է�, �ش� �ν��Ͻ��� ��ü�� ����(Server)
    public Server getInstanceObject(String instanceId) {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);
            if(serverApi.get(instanceId) != null)
                return serverApi.get(instanceId);
        }
        return null;
    }

    //���� ���� ��ü�� ����Ʈ�� list�� ��Ƽ� ����
    public List<Server> getInstancesInfo(){
        List<Server> servers = new ArrayList<Server>();
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);

            for (Server server : serverApi.listInDetail().concat()) {
                servers.add(server);
            }
        }
        return servers;
    }

    //�ν��Ͻ� id�� �Է�, �ش� �ν��Ͻ��� ����(terminate)
    public void terminateInstance(String instatnceId) {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);
            try {
                serverApi.delete(instatnceId);
            }catch(Exception e){
                System.out.println(e.toString()); //�ΰŷ� error ���־�� ��..
            }
        }
    }

    // �ν��Ͻ� ���� (REGION_NAME, �ν��Ͻ� ����, �̹��� ���̵�, Flavor ���̵�, ��Ʈ��ũ ���̵�, ������ ������ ���� ��й�ȣ, Ű���)
    public void createInstance(String getRegion, String getInstanceName, String getImageId, String getFlavorId, String getNetworkId, String getSettingPw, String getKeyPair) {
        CreateServerOptions option = new CreateServerOptions();
        option.networks(getNetworkId);
        option.adminPass(getSettingPw);
        option.keyPairName(getKeyPair);

        ServerApi serverApi = novaApi.getServerApi(getRegion);
        serverApi.create(getInstanceName, getImageId, getFlavorId, option);
    }

    //�ν��Ͻ� id�� �Է��ϸ�, �Ҵ�� floating ip�� ���
    public String loadFloatingIpsForInstance(String instatnceId) {
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

    // CustomString �ļ�..
    // FloatingIP{id=62c10231-126d-40db-aca3-989ab76e6a40, ip=10.0.1.183, fixedIp=10.0.0.25, instanceId=ef9484e8-99e2-4878-b1e9-0785fadc5415, pool=public2}
    // ������ String�� map�� ����
    public Map fromColonStringToMap(String title, String inputString) {
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