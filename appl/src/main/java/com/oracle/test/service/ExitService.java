package com.oracle.test.service;

import io.helidon.common.http.MediaType;
import io.helidon.webserver.*;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Web resource to terminate web server.
 */
public class ExitService implements Service {

    private static class ExitThread implements Runnable {

        private static final Logger LOGGER = Logger.getLogger(ExitThread.class.getName());

        /**
         * Starts application exit thread.
         *
         * @param server web server instance to shut down
         */
        public static final void start(final WebServer server) {
            new Thread(new ExitThread(server)).start();
        }

        private final WebServer server;

        private ExitThread(final WebServer server) {
            this.server = server;
        }

        /**
         * Wait few seconds and terminate web server.
         */
        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                LOGGER.log(Level.WARNING, ie, () -> String.format("Thread was interrupted: %s", ie.getMessage()));
            } finally {
                server.shutdown();
            }
        }

    }

    private static final Logger LOGGER = Logger.getLogger(ExitService.class.getName());

    private WebServer server;

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/", this::exit);
    }

    public void setServer(final WebServer server) {
        this.server = server;
    }

    /**
     * Terminates web server.
     * @param request not used
     * @param response where to send server termination message.
     * @return {@code null} value
     */
    public String exit(final ServerRequest request, final ServerResponse response) {
        response.headers().contentType(MediaType.TEXT_PLAIN);
        response.send("Testing web application shutting down.");
        ExitThread.start(server);
        return null;
    }

}