package com.oracle.test.model;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/*
    CREATE TABLE ADDRESS (
        ID NUMBER NOT NULL PRIMARY KEY,
        STREET VARCHAR(64),
        CITY VARCHAR(64),
        ZIP VARCHAR(16)
    );
*/
@Entity
@Table(name="ADDRESS")
public class Address {

    @Id
    @Column(name="ID")
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="ADDRESS_SEQ")
    private long id;
    @Column(name="STREET")
    private String street;
    @Column(name="CITY")
    private String city;
    @Column(name="ZIP")
    private String zip;

    public Address() {
        this.id = -1;
        this.street = null;
        this.city = null;
        this.zip = null;

    }

    public Address(long id, String street, String city, String zip) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.zip = zip;
    }

    public long getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("id", id)
                .add("street", street)
                .add("city", city)
                .add("zip", zip)
                .build();
    }

}
