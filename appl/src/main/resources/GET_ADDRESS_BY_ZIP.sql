       CREATE OR REPLACE PROCEDURE GET_ADDRESS_BY_ZIP (
            zip_in IN ADDRESS.ZIP%TYPE,
            street_out OUT ADDRESS.STREET%TYPE,
            city_out OUT ADDRESS.CITY%TYPE,
            zip_out OUT ADDRESS.ZIP%TYPE
        ) IS
        BEGIN
            SELECT A.STREET, A.CITY, A.ZIP
            INTO street_out, city_out, zip_out
            FROM ADDRESS A
            WHERE A.ZIP = zip_in;
        END;
