/**
 * 작성자: 전제현, 김대엽
 * 작성일: 2015. 08. 05 ~
 * OpenstackAPI
 * 오픈스택 인스턴스 액션을 Nova를 통해서 컨트롤할 수 있는 API
 */

package co.fastcat.openstack;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.inject.Module;
import com.google.common.base.Optional;
import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.loaders.LoadFloatingIpsForInstance;
import org.jclouds.openstack.nova.v2_0.domain.FloatingIP;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
import org.jclouds.openstack.nova.v2_0.domain.regionscoped.RegionAndId;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

public class OpenstackAPI implements Closeable {

    private static Logger logger = LoggerFactory.getLogger(OpenstackAPI.class);
    private final NovaApi novaApi;
    private final Set<String> regions;

    /*
     * OpenstackAPI 생성자
     * */
    public OpenstackAPI(String getUri, String getProvider, String getProject, String getUser, String credential) {
        String identity = getProject + ":" + getUser;
        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());

        novaApi = ContextBuilder.newBuilder(getProvider)
                .endpoint(getUri)
                .credentials(identity, credential)
                .modules(modules)
                .buildApi(NovaApi.class);
        regions = novaApi.getConfiguredRegions();
    }

    /*
    * 인스턴스 리스트 및 정보 출력
    * */
    public OpenstackAPI listInstance() {

        logger.debug("------------------------------- Start listInstance / 인스턴스 리스트 및 정보 출력 시작 -------------------------------");

        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);

            for (Server server : serverApi.listInDetail().concat()) {
                logger.debug(server.toString());
            }
        }

        logger.debug("------------------------------- End listInstance / 인스턴스 리스트 및 정보 출력 끝 -------------------------------");

        return this;
    }

    /*
    * 인스턴스 전체 리스트를 List에 담아서 리턴
    * */
    public List<Server> getInstancesList(){

        logger.debug("------------------------------- Start getInstancesList / 인스턴스 전체 리스트를 List에 담아서 리턴 시작 -------------------------------");

        List<Server> servers = new ArrayList<Server>();
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);

            for (Server server : serverApi.listInDetail().concat()) {
                servers.add(server);
            }
        }

        logger.debug("------------------------------- End getInstancesList / 인스턴스 전체 리스트를 List에 담아서 리턴 끝 -------------------------------");

        return servers;
    }

    /*
    * 인스턴스 서버 객체를 받아 해당 인스턴스의 정보를 출력
    * */
    public OpenstackAPI getInstanceInfo(Server instance) {

        logger.debug("------------------------------- Start getInstanceInfo / 인스턴스 정보를 출력 시작 / [ 인스턴스명: {} ] -------------------------------", instance.getName());
        String instanceId = instance.getId();
        logger.debug("------------------------------- Start getInstanceInfo / 인스턴스 정보를 출력 끝 / [ 인스턴스명: {} ] -------------------------------", instance.getName());

        return this;
    }

    /*
    * 인스턴스 ID를 받아 해당 인스턴스의 정보를 출력
    * */
    public OpenstackAPI getInstanceInfo(String instanceId) {

        logger.debug("------------------------------- Start getInstanceInfo / 인스턴스 정보를 출력 시작 / [ 인스턴스 ID: {} ] -------------------------------", instanceId);

        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);
            if(serverApi.get(instanceId) != null) {
                logger.debug("getInstanceInfo 출력: {}", serverApi.get(instanceId).toString());
                System.out.println(serverApi.get(instanceId).toString());
            }
        }

        logger.debug("------------------------------- End getInstanceInfo / 인스턴스 정보를 출력 끝 / [ 인스턴스 ID: {} ] -------------------------------", instanceId);

        return this;
    }

    /*
    * 인스턴스 ID를 받아 해당 인스턴스를 끔 (Shut Down)
    * */
    public OpenstackAPI stopInstance(String instanceId) {

        logger.debug("------------------------------- Start stopInstance / 인스턴스를 끔 (Shut Down) 시작 / [ 인스턴스 ID: {} ] -------------------------------", instanceId);

        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);
            try {
                serverApi.stop(instanceId);
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }

        logger.debug("------------------------------- End stopInstance / 인스턴스를 끔 (Shut Down) 끝 / [ 인스턴스 ID: {} ] -------------------------------", instanceId);

        return this;
    }

    /*
    * 인스턴스 ID를 입력, 해당 인스턴스의 객체를 리턴 (Server 객체 return)
    * */
    public Server getInstanceObject(String instanceId) {

        logger.debug("------------------------------- Start getInstanceObject / 인스턴스의 객체를 리턴 시작 / [ 인스턴스 ID: {} ] -------------------------------", instanceId);

        for (String region : regions) {

            ServerApi serverApi = novaApi.getServerApi(region);

            if (serverApi.get(instanceId) != null) {

                logger.debug("------------------------------- End stopInstance / 인스턴스의 객체를 리턴 끝 / [ 인스턴스 ID: {} ] -------------------------------", instanceId);
                return serverApi.get(instanceId);
            }
        }

        logger.debug("------------------------------- End stopInstance / 인스턴스의 객체를 리턴 끝 / return null / [ 인스턴스 ID: {} ] -------------------------------", instanceId);
        return null;
    }

   /*
   * 인스턴스 ID를 받아 해당 인스턴스를 종료 (terminate)
   * */
    public OpenstackAPI terminateInstance(String instanceId) {

        logger.debug("------------------------------- Start terminateInstance / 인스턴스를 종료 시작 (terminate) / [ 인스턴스 ID: {} ] -------------------------------", instanceId);

        for (String region : regions) {

            ServerApi serverApi = novaApi.getServerApi(region);
            try {
                serverApi.delete(instanceId);
            } catch (Exception e) {
                logger.error(e.toString());
                logger.debug("------------------------------- End terminateInstance / 인스턴스를 종료 끝 (terminate) / Error / [ 인스턴스 ID: {} ] -------------------------------", instanceId);
            }
        }

        logger.debug("------------------------------- End terminateInstance / 인스턴스를 종료 (terminate) 끝 / [ 인스턴스 ID: {} ] -------------------------------", instanceId);

        return this;
    }

    /*
    * 인스턴스 생성 (생성 후 생성된 인스턴스 ID를 리턴한다.)
    * (REGION_NAME, 인스턴스 네임, 이미지 아이디, Flavor 아이디, 네트워크 아이디, 세팅할 관리자 계정 비밀번호, 키페어)
    * */
    public String launchInstance(
            String getRegion,       // * getRegion: REGION_NAME, keystonerc_계정 파일에서 cat으로 확인 가능
            String getInstanceName, // * getInstanceName : 인스턴스 네임, 원하는 인스턴스명으로 입력할 것, 기존 인스턴스명과 중복 가능
            String getImageId,      // * getImageId : 이미지 아이디, horizon 대쉬보드에서 확인 가능
            String getFlavorId,     // * getFlavorId : Flavor 아이디, horizon 대쉬보드에서 확인 가능
            String getNetworkId,    // * getNetworkId : 네트워크 아이디, horizon 대쉬보드에서 확인 가능
            String getSettingPw,    // * getSettingPw : 인스턴스 서버 내의 계정 관리자 비밀번호 설정 값
            String getKeyPair       // * getKeyPair : 키페어명 입력, horizon 대쉬보드에서 확인 가능
    ) {

        logger.debug("------------------------------- Start createInstance / 인스턴스 생성 시작 / [ 만들어질 인스턴스명: {} ] -------------------------------", getInstanceName);

        CreateServerOptions option = new CreateServerOptions();

        option.networks(getNetworkId);
        option.adminPass(getSettingPw);
        option.keyPairName(getKeyPair);

        ServerApi serverApi = novaApi.getServerApi(getRegion);

        logger.debug("------------------------------- End createInstance / 인스턴스 생성 바로 이전 / [ 만들어질 인스턴스명: {} ] -------------------------------", getInstanceName);

        return serverApi.create(getInstanceName, getImageId, getFlavorId, option).getId();
    }

    /*
    * 인스턴스 서버 객체를 받아 할당된 Floating IP를 출력
    * */
    public OpenstackAPI loadFloatingIpsForInstance(Server instance) {

        logger.debug("------------------------------- Start loadFloatingIpsForInstance / 할당된 Floating IP 출력 시작 / [ 인스턴스명: {} ] -------------------------------", instance.getName());

        String instanceId = instance.getId();

        for (String region : regions) {

            LoadFloatingIpsForInstance loadFloatingIpsForInstance = new LoadFloatingIpsForInstance(novaApi);
            RegionAndId regionAndId = null;
            regionAndId = regionAndId.fromSlashEncoded(region+"/"+instanceId);

            try {
                Iterable tmp = loadFloatingIpsForInstance.load(regionAndId);
                Iterator iterator = tmp.iterator();
                while (iterator.hasNext()) {
                    Object element = iterator.next();
                    if(fromColonStringToMap("FloatingIP",element.toString()).get("ip").toString() != null)
                        logger.debug("Floating IP: {}", fromColonStringToMap("FloatingIP", element.toString()).get("ip").toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logger.debug("------------------------------- End loadFloatingIpsForInstance / 할당된 Floating IP 출력 끝 / [ 인스턴스명: {} ] -------------------------------", instance.getName());

        return this;
    }

    /*
    * 인스턴스 ID를 입력하면, 할당된 Floating IP를 출력
    * */
    public OpenstackAPI loadFloatingIpsForInstance(String instanceId) {

        logger.debug("------------------------------- Start loadFloatingIpsForInstance / 할당된 Floating IP 출력 시작 / [ 인스턴스 ID: {} ] -------------------------------", instanceId);

        for (String region : regions) {

            LoadFloatingIpsForInstance loadFloatingIpsForInstance = new LoadFloatingIpsForInstance(novaApi);
            RegionAndId regionAndId = null;
            regionAndId = regionAndId.fromSlashEncoded(region+"/"+instanceId);

            try {
                Iterable tmp = loadFloatingIpsForInstance.load(regionAndId);
                Iterator iterator = tmp.iterator();
                while (iterator.hasNext()) {
                    Object element = iterator.next();
                    if(fromColonStringToMap("FloatingIP",element.toString()).get("ip").toString() != null)
                        logger.debug("Floating IP: {}", fromColonStringToMap("FloatingIP", element.toString()).get("ip").toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logger.debug("------------------------------- End loadFloatingIpsForInstance / 할당된 Floating IP 출력 끝 / [ 인스턴스 ID: {} ] -------------------------------", instanceId);

        return this;
    }


    /*
    * CustomString 파서..
    * FloatingIP{id=62c10231-126d-40db-aca3-989ab76e6a40, ip=10.0.1.183, fixedIp=10.0.0.25, instanceId=ef9484e8-99e2-4878-b1e9-0785fadc5415, pool=public2}
    * 형식의 String을 map에 담음
    * */
    public Map fromColonStringToMap(String title, String inputString) {

        Map<String, String> tmpMap = new HashMap<String, String>();
        String[] pairs = inputString.trim().substring(title.length()+1,inputString.length()-1).split(",");
        for (int keyPair_count=0; keyPair_count<pairs.length; keyPair_count++) {
            String pair = pairs[keyPair_count];
            String[] keyValue = pair.split("=");
            tmpMap.put(keyValue[0].trim(), keyValue[1].trim());
//            logger.debug("keyValue[1]: {}", keyValue[1].trim());
        }
        return tmpMap;
    }

    /*
    * 고정 IP가 지정되지 않은 인스턴스에 Floating IP를 설정한다.
    * */
    public OpenstackAPI addFloatingIp(String instanceId) throws Exception {

        logger.debug("------------------------------- Start addFloatingIp / 인스턴스에 Floating IP를 설정 시작 / [ 인스턴스 ID: {} ] -------------------------------", instanceId);

        for (String region : regions) {
            Optional<? extends FloatingIPApi> apiOption = novaApi.getFloatingIPApi(region);
            if (!apiOption.isPresent())
                continue;
            FloatingIPApi api = apiOption.get();
            ServerApi serverApi = this.novaApi.getServerApi(region);
            Server server = serverApi.get(instanceId);
            FloatingIP floatingIP = null;
            for (int instance_count = 0, instance_list_size = api.list().size(); instance_count < instance_list_size; instance_count++) {
                if (isFixedIPNull(api.list().get(instance_count).toString()) == true) {
                    floatingIP = api.list().get(instance_count);
                    break;
                }
                logger.debug("instance_list_size: {}, instance_count: {}", instance_list_size, instance_count);
            }
            try {
                api.addToServer(floatingIP.getIp(), server.getId());
            } catch (Exception e) {
                logger.error(e.toString());
            } finally {
            }
        }

        logger.debug("------------------------------- End addFloatingIp / 인스턴스에 Floating IP를 설정 끝 / [ 인스턴스 ID: {} ] -------------------------------", instanceId);

        return this;
    }

    /*
    * FixedIP 값이 null인 경우를 찾는다.
    * */
    public boolean isFixedIPNull(String ipdataToString){ //FloatingIP{id=d3c731a4-726d-492e-ace8-36393dd603a7, ip=10.0.1.185, fixedIp=null, instanceId=null, pool=public2}

        logger.debug("------------------------------- Start isFixedIPNull / FixedIP 값이 null인 경우를 찾는다 / [ ipdataToString: {} ] -------------------------------", ipdataToString);

        Map tmpMap = fromEqualsStringToMap("FloatingIP",ipdataToString);
        if (tmpMap.get("fixedIp").equals("null")) {
            logger.debug("------------------------------- End isFixedIPNull / FixedIP 값이 null인 경우를 찾는다 / return true / [ ipdataToString: {} ] -------------------------------", ipdataToString);
            return true;
        }

        logger.debug("------------------------------- End isFixedIPNull / FixedIP 값이 null인 경우를 찾는다 / return false / [ ipdataToString: {} ] -------------------------------", ipdataToString);

        return false;
    }

    //CustomString 파서..
    //FloatingIP{id=62c10231-126d-40db-aca3-989ab76e6a40, ip=10.0.1.183, fixedIp=10.0.0.25, instanceId=ef9484e8-99e2-4878-b1e9-0785fadc5415, pool=public2}
    //형식의 String을 map에 담음
    private Map fromEqualsStringToMap(String title,String inputString){

        logger.debug("------------------------------- Start fromEqualsStringToMap -------------------------------");

        Map<String, String> tmpMap = new HashMap<String, String>();
        String[] pairs = inputString.trim().substring(title.length()+1,inputString.length()-1).split(",");
        for (int keyPair_count=0; keyPair_count<pairs.length; keyPair_count++) {
            String pair = pairs[keyPair_count];
            String[] keyValue = pair.split("=");
            tmpMap.put(keyValue[0].trim(), keyValue[1].trim());
        }

        logger.debug("------------------------------- End fromEqualsStringToMap -------------------------------");

        return tmpMap;
    }

    public void close() throws IOException {
        Closeables.close(novaApi, true);
    }
}