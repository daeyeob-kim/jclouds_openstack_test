import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Server;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

public class JCloudsNova implements Closeable {
    private final NovaApi novaApi;
    private final Set<String> regions;

    public static void main(String[] args) throws IOException {
        JCloudsNova jcloudsNova = new JCloudsNova();

        try {
            jcloudsNova.listServers();
            jcloudsNova.stopServers();
            jcloudsNova.createInstance();
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

    private void listServers() {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);

            System.out.println("Servers in " + region);

            for (Server server : serverApi.listInDetail().concat()) {
                System.out.println("  " + server);
            }
        }
    }

    private void stopServers() {
        for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);

            serverApi.stop("c27149f4-5a6b-4577-8c10-a95f5060c0d4");
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

    public void close() throws IOException {
        Closeables.close(novaApi, true);
    }
}