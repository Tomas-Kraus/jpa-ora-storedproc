package com.oracle.test.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;

import com.oracle.test.TestException;
import com.oracle.test.model.Address;
import com.oracle.test.model.Person;

public class PersonDao {

    private final EntityManagerFactory emf;

    public PersonDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Person createPerson(Person person) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction et = em.getTransaction();
        et.begin();
        try {
            em.persist(person.getAddress());
            em.persist(person);
            em.flush();
            et.commit();
            return person;
        } catch (Throwable t) {
            et.rollback();
            throw t;
        } finally {
            em.close();
        }
    }

    public Person getPersonbyId(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Person.class, id);
        }  finally {
            em.close();
        }
    }

    public List<Person> listNamesForZipIndexArg(String zip) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("PersonNameByZIPIndexArg", Person.class)
                    .setParameter(1, zip)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Person> listNamesForZipNameArg(String zip) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("PersonNameByZIPNameArg", Person.class)
                    .setParameter("zip", zip)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /*
        CREATE OR REPLACE PROCEDURE GET_ADDRESS_BY_NAME (
            fname_in IN PERSON.FIRST_NAME%TYPE,
            lname_in IN PERSON.LAST_NAME%TYPE,
            rec_out OUT SYS_REFCURSOR,
            err_out OUT VARCHAR
        ) IS
        BEGIN
            OPEN rec_out FOR
            SELECT A.STREET, A.CITY, A.ZIP
            FROM PERSON P, ADDRESS A
            WHERE P.ADDRESS_ID = A.ID
              AND P.FIRST_NAME = fname_in
              AND P.LAST_NAME = lname_in;
        EXCEPTION
            WHEN OTHERS THEN
                err_out := SQLCODE || ' ' || SQLERRM;
        END;
        /
     */
    public List<Address> listAddressesForName(String firstName, String lastName) {
        EntityManager em = emf.createEntityManager();
        try {
            StoredProcedureQuery query = em.createStoredProcedureQuery("GET_ADDRESS_BY_NAME");
//        query.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
//        query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
//        query.registerStoredProcedureParameter(3, void.class, ParameterMode.REF_CURSOR);
            query.registerStoredProcedureParameter("fname_in", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("lname_in", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("rec_out", ResultSet.class, ParameterMode.REF_CURSOR);
            query.registerStoredProcedureParameter("err_out", String.class, ParameterMode.OUT);

            ResultSet rs = null;
            try {
                query
                        .setParameter("fname_in", firstName)
                        .setParameter("lname_in", lastName);
                query.execute();
                rs = (ResultSet) query.getOutputParameterValue("rec_out");
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
        } finally {
            em.close();
        }
    }

    public Address listAddressesForZip(String zip) {
        EntityManager em = emf.createEntityManager();
        try {
            StoredProcedureQuery query = em.createStoredProcedureQuery("GET_ADDRESS_BY_ZIP");
            query.registerStoredProcedureParameter("zip_in", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("street_out", String.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("city_out", String.class, ParameterMode.OUT);
            query.registerStoredProcedureParameter("zip_out", String.class, ParameterMode.OUT);

            query.setParameter("zip_in", zip);
            query.execute();
            String streetOut = (String) query.getOutputParameterValue("street_out");
            String cityOut = (String) query.getOutputParameterValue("city_out");
            String zipOut = (String) query.getOutputParameterValue("zip_out");
            return new Address(-1, streetOut, cityOut, zipOut);
        } finally {
            em.close();
        }
    }

}
