package com.oracle.test.dao;

import com.oracle.test.TestException;
import com.oracle.test.model.Address;
import com.oracle.test.model.Person;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PersonDao {

    private final EntityManager em;

    public PersonDao(EntityManager em) {
        this.em = em;
    }

    public Person createPerson(String firstName, String lastName, LocalDate birthDate, Address address) {
        Person person = new Person(-1, firstName, lastName, birthDate, address);
        em.persist(person);
        return person;
    }

    public Person getPersonbyId(long id) {
        return em.find(Person.class, id);
    }

    public List<Person> listNamesForZip(String zip) {
        return em.createNamedQuery("PersonNameByZIP", Person.class)
                .setParameter(1, zip)
                .getResultList();
    }


    /* Stored procedure to retrieve Adress data for given Person first and last names.
       CREATE OR REPLACE PROCEDURE GET_ADDRESS_BY_NAME (
           FIRST_NAME_IN IN VARCHAR,
           LAST_NAME_IN IN VARCHAR,
           RESULT OUT SYS_REFCURSOR
       ) AS
       BEGIN
           OPEN RESULT FOR
           SELECT A.STREET, A.CITY, A.ZIP
           FROM PERSON P, ADDRESS A
           WHERE P.ADDRESS_ID = ADDRESS.ID
             AND P.FIRST_NAME = FIRST_NAME_IN
             AND P.LAST_NAME = LAST_NAME_IN;

       END;
       /
     */
    public List<Address> listAddressesForName(String firstName, String lastName) {
        StoredProcedureQuery query = this.em.createStoredProcedureQuery("GET_ADDRESS_BY_NAME")
                .registerStoredProcedureParameter("FIRST_NAME_IN", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("LAST_NAME_IN", String.class, ParameterMode.IN)
                .registerStoredProcedureParameter("RESULT", ResultSet.class, ParameterMode.OUT);
        ResultSet rs = null;
        try {
            query
                    .setParameter("FIRST_NAME_IN", firstName)
                    .setParameter("LAST_NAME_IN", lastName);
            query.execute();
            rs = (ResultSet) query.getOutputParameterValue("RESULT");
            List<Address> listAddress = new LinkedList<>();
            while (rs.next()) {
                Address address = new Address(
                        -1,
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3));
                listAddress.add(address);
            }
            return new ArrayList<>(listAddress);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new TestException("Could not retrieve Addresses for " + firstName + " " + lastName, e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
