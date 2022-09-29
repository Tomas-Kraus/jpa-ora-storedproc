package com.oracle.test;

import com.oracle.test.service.ExitService;
import com.oracle.test.service.TestService;
import io.helidon.common.LogConfig;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.media.jsonb.JsonbSupport;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

import java.io.File;
import java.io.ObjectInputFilter;
import java.util.logging.Logger;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class TestMain {

    private static final Logger LOGGER = Logger.getLogger(TestMain.class.getName());
    private static final String CONFIG_PROPERTY_NAME="com.oracle.test.config";
    private static final String DEFAULT_CONFIG_FILE="test.yaml";

    private static WebServer startServer(final String configFile) {

        final Config config = Config.create(ConfigSources.classpath(configFile));

        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("bugdb");

        final TestService testService = new TestService(emf);
        final ExitService exitService = new ExitService();

        final Routing routing = Routing.builder()
                .register("/Test", testService)
                .register("/Exit", exitService)
                .build();

        final WebServer.Builder serverBuilder = WebServer.builder(routing)
                .port(config.get("server.port").as(Integer.class).get())
                // Get webserver config from the "server" section of configuration file
                .config(config.get("server"));

        final WebServer server = serverBuilder
                .addMediaSupport(JsonpSupport.create())
                .addMediaSupport(JsonbSupport.create())
                .build();

        exitService.setServer(server);

        // Start the server and print some info.
        server.start().thenAccept(ws -> {
            System.out.println(
                    "WEB server is up! http://localhost:" + ws.port() + "/");
        });

        // Server threads are not daemon. NO need to block. Just react.
        server.whenShutdown().thenRun(
                () -> System.out.println("WEB server is DOWN. Good bye!"));

        return server;
    }

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        String configFile;
        if (args != null && args.length > 0) {
            configFile = args[0];
        } else {
            configFile = System.getProperty(CONFIG_PROPERTY_NAME, DEFAULT_CONFIG_FILE);
        }
        LOGGER.info(() -> String.format("Configuration file: %s", configFile));

        LogConfig.configureRuntime();

        startServer(configFile);

    }

}
