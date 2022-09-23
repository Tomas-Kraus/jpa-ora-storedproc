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
                err_out \:= SQLCODE || ' ' || SQLERRM;
        END;
