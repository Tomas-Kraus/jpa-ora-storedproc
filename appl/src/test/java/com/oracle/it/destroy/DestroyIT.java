package com.oracle.it.destroy;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.oracle.it.test.QueriesIT;
import com.oracle.it.tools.TestClient;
import com.oracle.it.tools.TestServiceClient;

import org.junit.jupiter.api.Test;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;

public class DestroyIT {

    private static final Logger LOGGER = Logger.getLogger(QueriesIT.class.getName());

    private final TestClient testClient = TestClient.builder().build();

    /**
     * Verify {@code params(Object... parameters)} parameters setting method.
     */
    @Test
    public void testGetArrayParams() {
        WebClientResponse response = testClient.webClient()
                .get()
                .path("/Exit")
                .submit()
                .await(1, TimeUnit.MINUTES);
        LOGGER.info(() -> String.format(
                "Status: %s",
                response.status()));
        LOGGER.info(() -> String.format(
                "Response: %s",
                response
                        .content()
                        .as(String.class)
                        .await(1, TimeUnit.MINUTES)));
    }

}
