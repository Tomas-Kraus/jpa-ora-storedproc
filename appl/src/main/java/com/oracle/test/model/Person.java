package com.oracle.test.model;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/*
    CREATE TABLE PERSON (
        ID NUMBER NOT NULL PRIMARY KEY,
        FIRST_NAME VARCHAR(64),
        LAST_NAME VARCHAR(64),
        BIRTH_DATE DATE,
        ADDRESS_ID NUMBER REFERENCES ADDRESS(ID)
    );
 */

@NamedNativeQueries({
        @NamedNativeQuery(
                name="PersonNameByZIPIndexArg",
                query="SELECT P.FIRST_NAME AS FIRST_NAME, P.LAST_NAME AS LAST_NAME FROM PERSON P, ADDRESS A WHERE P.ADDRESS_ID = A.ID AND A.ZIP = ?",
                resultSetMapping="PersonNameByZIPToPerson"),
        @NamedNativeQuery(
                name="PersonNameByZIPNameArg",
                query="SELECT P.FIRST_NAME AS FIRST_NAME, P.LAST_NAME AS LAST_NAME FROM PERSON P, ADDRESS A WHERE P.ADDRESS_ID = A.ID AND A.ZIP = :zip",
                resultSetMapping="PersonNameByZIPToPerson")
})
@SqlResultSetMapping(
        name = "PersonNameByZIPToPerson",
        classes = {
                @ConstructorResult(
                        targetClass = Person.class,
                        columns = {
                                @ColumnResult(name = "FIRST_NAME", type = String.class),
                                @ColumnResult(name = "LAST_NAME", type = String.class)
                        })
        })
@Entity
@Table(name="PERSON")
public class Person {

    @Id
    @Column(name="ID")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PERSON_SEQ")
    private long id;
    @Column(name="FIRST_NAME")
    private String firstName;
    @Column(name="LAST_NAME")
    private String lastName;
    @Column(name="BIRTH_DATE")
    private LocalDate birthDate;
    @ManyToOne
    @JoinColumn(name="ADDRESS_ID")
    private Address address;

    public Person() {
        this.id = -1;
        this.firstName = null;
        this.lastName = null;
        this.birthDate = null;
        this.address = null;
    }

    /**
     * Creates an instance of Person for {@code PersonNameByZIPToPerson} mapping.
     *
     * @param firstName
     * @param lastName
     */
    public Person(String firstName, String lastName) {
        this.id = -1;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = null;
        this.address = null;
    }

    public Person(long id, String firstName, String lastName, LocalDate birthDate, Address address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public JsonObject toJson() {
        JsonObjectBuilder job = Json.createObjectBuilder()
                .add("id", id)
                .add("firstName", firstName)
                .add("lastName", lastName);
        if (birthDate != null) {
            job.add("birthDate", birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        } else {
            job.addNull("birthDate");
        }
        if (address != null) {
            job.add("address", address.toJson());
        } else {
            job.addNull("address");
        }
        return job.build();
    }

}
