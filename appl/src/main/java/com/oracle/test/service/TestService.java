package com.oracle.test.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonValue;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;

import com.oracle.test.TestException;
import com.oracle.test.dao.PersonDao;
import com.oracle.test.model.Address;
import com.oracle.test.model.Person;
import io.helidon.webserver.Handler;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public class TestService implements Service {

    private static final String SRC_PREFIX = "target/classes/";
    private static final String GET_ADDRESS_BY_NAME_FILE = "GET_ADDRESS_BY_NAME.sql";
    private static final String GET_ADDRESS_BY_ZIP_FILE = "GET_ADDRESS_BY_ZIP.sql";

    private final EntityManagerFactory emf;
    private final PersonDao pd;

    public TestService(EntityManagerFactory emf) {
        this.emf = emf;
        this.pd = new PersonDao(emf);
    }

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/setup", this::setup)
                .get("/cleanup", this::cleanup)
                .post("/address", Handler.create(Address.class, this::createAddress))
                .post("/person", Handler.create(Person.class, this::createPerson))
                .get("/listAddressesForName", this::listAddressesForName)
                .get("/listPersonByZipNameArg", this::listPersonByZipNameArg)
                .get("/listPersonByZipIndexArg", this::listPersonByZipIndexArg)
                .get("/listAddressByZipStoredProc", this::listAddressByZipStoredProc);
    }

    private void executeFile(String filePath) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction et = em.getTransaction();
        try (InputStream is = new FileInputStream(filePath)) {
            String statement = new String(is.readAllBytes(), StandardCharsets.US_ASCII);
            et.begin();
            int result = em.createNativeQuery(statement).executeUpdate();
            em.flush();
            et.commit();
            System.out.println(String.format("[Test:APPL] setup %s: %d", filePath, result));
        } catch (IOException ex) {
            et.rollback();
            throw new RuntimeException(ex);
        } finally {
            em.close();
        }
    }

    private void setup(ServerRequest request, ServerResponse response) {
        System.out.println("[Test:APPL] setup REQUEST");
        CompletableFuture.supplyAsync(
                () -> {
                    executeFile(SRC_PREFIX + GET_ADDRESS_BY_NAME_FILE);
                    executeFile(SRC_PREFIX + GET_ADDRESS_BY_ZIP_FILE);
                    return null;
                })
                .thenApply(x -> response.send())
                .exceptionally(t -> {
                    response.send(t);
                    return null;
                });
    }

    private void cleanup(ServerRequest request, ServerResponse response) {
        System.out.println("[Test:APPL] cleanup REQUEST");
        CompletableFuture.supplyAsync(
                        () -> {
                            String statement = "DROP PROCEDURE GET_ADDRESS_BY_NAME";
                            EntityManager em = emf.createEntityManager();
                            try {
                                EntityTransaction et = em.getTransaction();
                                et.begin();
                                int result = em.createNativeQuery(statement).executeUpdate();
                                em.flush();
                                et.commit();
                                System.out.println("[Test:APPL] cleanup result: " + result);
                                return null;
                            } finally {
                                em.close();
                            }
                        })
                .thenApply(x -> response.send())
                .exceptionally(t -> {
                    response.send(t);
                    return null;
                });
    }

    private void createAddress(ServerRequest request, ServerResponse response, Address address) {
        System.out.println("[Test:APPL] createAddress REQUEST: " + address.toJson().toString());
        CompletableFuture.supplyAsync(
                () -> {
                    EntityManager em = emf.createEntityManager();
                    EntityTransaction et = em.getTransaction();
                    et.begin();
                    try {
                        em.persist(address);
                        em.flush();
                        et.commit();
                        System.out.println("[Test:APPL] createAddress PERSIST: " + address.toJson().toString());
                    } catch (PersistenceException ex) {
                        et.rollback();
                        throw ex;
                    } finally {
                        em.close();
                    }
                    return null;
                })
                .thenApply(x -> response.send(address))
                .exceptionally(t -> {
                    response.send(t);
                    return null;
                });
    }

    private void createPerson(ServerRequest request, ServerResponse response, Person person) {
        System.out.println("[Test:APPL] createPerson REQUEST: " + person.toJson().toString());
        CompletableFuture.supplyAsync(
                        () -> {
                            pd.createPerson(person);
                            return null;
                        })
                .thenApply(x -> response.send(person))
                .exceptionally(t -> {
                    response.send(t);
                    return null;
                });
    }

    private void listAddressesForName(ServerRequest request, ServerResponse response) {
        String firstName = param(request, "firstName");
        String lastName = param(request, "lastName");
        System.out.println(String.format("[Test:APPL] createPerson REQUEST: %s %s", firstName, lastName));
        CompletableFuture.supplyAsync(
                        () -> pd.listAddressesForName(firstName,lastName))
                .thenApply(x -> {
                    JsonArrayBuilder jab = Json.createArrayBuilder();
                    for (Address address : x) {
                        jab.add(address.toJson());
                    }
                    response.send(jab.build());
                    return null;
                })
                .exceptionally(t -> {
                    response.send(t);
                    return null;
                });
    }

    private void listAddressByZipStoredProc(ServerRequest request, ServerResponse response) {
        String zip = param(request, "zip");
        System.out.println(String.format("[Test:APPL] listAddressByZipStoredProc REQUEST: %s", zip));
        CompletableFuture.supplyAsync(
                        () -> pd.listAddressesForZip(zip))
                .thenApply(x -> {
                    if (x != null) {
                        response.send(x.toJson());
                    } else {
                        response.send(JsonValue.NULL);
                    }
                    return null;
                })
                .exceptionally(t -> {
                    response.send(t);
                    return null;
                });
    }


    private void listPersonByZipNameArg(ServerRequest request, ServerResponse response) {
        String zip = param(request, "zip");
        System.out.println(String.format("[Test:APPL] listPersonByZipNameArg REQUEST: %s", zip));
        CompletableFuture.supplyAsync(
                        () -> pd.listNamesForZipNameArg(zip))
                .thenApply(x -> {
                    JsonArrayBuilder jab = Json.createArrayBuilder();
                    for (Person person : x) {
                        jab.add(person.toJson());
                    }
                    response.send(jab.build());
                    return null;
                })
                .exceptionally(t -> {
                    response.send(t);
                    return null;
                });
    }

    private void listPersonByZipIndexArg(ServerRequest request, ServerResponse response) {
        String zip = param(request, "zip");
        System.out.println(String.format("[Test:APPL] listPersonByZipIndexArg REQUEST: %s", zip));
        CompletableFuture.supplyAsync(
                        () -> pd.listNamesForZipIndexArg(zip))
                .thenApply(x -> {
                    JsonArrayBuilder jab = Json.createArrayBuilder();
                    for (Person person : x) {
                        jab.add(person.toJson());
                    }
                    response.send(jab.build());
                    return null;
                })
                .exceptionally(t -> {
                    response.send(t);
                    return null;
                });
    }

    public static final String param( final ServerRequest request, final String name) {
        Optional<String> maybeParam = request.queryParams().first(name);
        if (maybeParam.isPresent()) {
            return maybeParam.get();
        } else {
            throw new TestException(String.format("Query parameter %s is missing.", name));
        }
    }

}
