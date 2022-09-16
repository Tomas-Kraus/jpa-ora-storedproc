package com.oracle.test.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ADDRESS")
public class Address {

    @Id
    @Column(name="ID")
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

}
