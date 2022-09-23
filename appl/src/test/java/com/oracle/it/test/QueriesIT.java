package com.oracle.it.test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import com.oracle.it.tools.TestClient;
import com.oracle.it.tools.TestServiceClient;
import com.oracle.test.model.Address;
import com.oracle.test.model.Person;
import io.helidon.media.common.MessageBodyReadableContent;
import io.helidon.webclient.WebClientRequestBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QueriesIT {

    private static final Logger LOGGER = Logger.getLogger(QueriesIT.class.getName());

    private static final TestServiceClient testClient = TestClient.builder()
            .service("Test")
            .build();

    private final Address[] ADDRESSES = {
            new Address(-1, "Long street", "Unknown City", "12345"),
            new Address(-1, "Short street", "Crazy City", "23456")
    };

    private final Person[] PERSONS = {
            new Person(-1, "John", "Smith", LocalDate.of(1987, 1, 12), ADDRESSES[1])
    };

    @BeforeAll
    public static void setup() {
        System.out.println("Running setup");
        testClient.callServiceAndGetString("setup");
    }

    //@AfterAll
    public static void cleanup() {
        System.out.println("Running cleanup");
        testClient.callServiceAndGetString("cleanup");
    }

    @Test
    @Order(1)
    public void createAddress() {
        System.out.println("Running createAddress");
        WebClientRequestBuilder rb = testClient.webClient().post()
                .path(testClient.buildPath("address"));
        final MessageBodyReadableContent content = rb.submit(ADDRESSES[0].toJson())
                .await(1, TimeUnit.MINUTES)
                .content();
        JsonObject address = content.as(JsonObject.class)
                .await(1, TimeUnit.MINUTES);
        MatcherAssert.assertThat(address.getInt("id"), Matchers.greaterThan(0));
    }

    @Test
    @Order(2)
    public void createPerson() {
        System.out.println("Running createPerson");
        WebClientRequestBuilder rb = testClient.webClient().post()
                .path(testClient.buildPath("person"));
        final MessageBodyReadableContent content = rb.submit(PERSONS[0].toJson())
                .await(1, TimeUnit.MINUTES)
                .content();
        JsonObject person = content.as(JsonObject.class)
                .await(1, TimeUnit.MINUTES);
        MatcherAssert.assertThat(person.getInt("id"), Matchers.greaterThan(0));
    }

    @Test
    @Order(3)
    public void listAddressesForName() {
        System.out.println("Running listAddressesForName");
        Map<String, String> params =new HashMap<>(2);
        params.put("firstName", "John");
        params.put("lastName", "Smith");
        JsonArray value = testClient.callServiceAndGetRawData("listAddressesForName", params).asJsonArray();
        MatcherAssert.assertThat(1, Matchers.equalTo(value.size()));
    }

    @Test
    @Order(4)
    public void listPersonByZipNameArg() {
        System.out.println("Running listPersonByZipNameArg");
        Map<String, String> params =new HashMap<>(1);
        params.put("zip", "23456");
        JsonArray value = testClient.callServiceAndGetRawData("listPersonByZipNameArg", params).asJsonArray();
        MatcherAssert.assertThat(1, Matchers.equalTo(value.size()));
    }

    @Test
    @Order(5)
    public void listAddressByZipStoredProc() {
        System.out.println("Running listAddressByZipStoredProc");
        Map<String, String> params =new HashMap<>(1);
        params.put("zip", "23456");
        JsonObject value = testClient.callServiceAndGetRawData("listAddressByZipStoredProc", params).asJsonObject();
        MatcherAssert.assertThat("23456", Matchers.equalTo(value.getString("zip")));
    }

    @Test
    @Order(6)
    public void listPersonByZipIndexArg() {
        System.out.println("Running listPersonByZipIndexArg");
        Map<String, String> params =new HashMap<>(1);
        params.put("zip", "23456");
        JsonArray value = testClient.callServiceAndGetRawData("listPersonByZipIndexArg", params).asJsonArray();
        MatcherAssert.assertThat(1, Matchers.equalTo(value.size()));
    }

}
