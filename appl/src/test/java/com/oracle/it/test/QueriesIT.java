package com.oracle.it.test;

import java.util.logging.Logger;

import com.oracle.it.tools.TestClient;
import com.oracle.it.tools.TestServiceClient;

public class QueriesIT {

    private static final Logger LOGGER = Logger.getLogger(QueriesIT.class.getName());

    private final TestServiceClient testClient = TestClient.builder()
            .service("QueryStatement")
            .build();

}
