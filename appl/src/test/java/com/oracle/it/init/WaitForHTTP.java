package com.oracle.it.init;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;

/**
 * Wait for HTTP server to come up
 */
public class WaitForHTTP {

    private static final String CONFIG_PROPERTY_NAME="com.oracle.test.config";
    private static final String DEFAULT_CONFIG_FILE="test.yaml";

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
        final Config config = Config.create(ConfigSources.classpath(configFile));

        int port = config.get("server.port").as(Integer.class).get();
        String host = config.get("server.host").as(String.class).get();
        SocketAddress sa = new InetSocketAddress(host, port);
        System.out.println(String.format("[HTTP Check] Checking connection to %s:%d", host, port));
        boolean exit = false;
        while (!exit) {
            try {
                Socket socket = new Socket();
                socket.connect(sa, 1000);
                exit = true;
                System.out.println(String.format("[HTTP Check] Server active at %s:%d", host, port));
            } catch (IOException e) {
                System.out.println("[HTTP Check] Connection failed: " + e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

}
