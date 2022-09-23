package com.oracle.it.init;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class InitDb {

    /* Startup timeout in seconds for database. */
    private static final int TIMEOUT = 900;
    /* Thread sleep time in miliseconds while waiting for database or appserver to come up. */
    private static final int SLEEP_MILIS = 1000;

    @SuppressWarnings("SleepWhileInLoop")
    public static void waitForDatabase(String dbUser, String dbPassword, String dbUrl) {
        if (dbUser == null) {
            throw new IllegalStateException("Database user name was not set!");
        }
        if (dbPassword == null) {
            throw new IllegalStateException("Database user password was not set!");
        }
        if (dbUrl == null) {
            throw new IllegalStateException("Database URL was not set!");
        }
        long endTm = 1000 * TIMEOUT + System.currentTimeMillis();
        Properties properties = new Properties();
        properties.put("user", dbUser);
        properties.put("password", dbPassword);
        properties.put("internal_logon", "SYSDBA");
        // Connection is available at later stages of Oracle DB initialization. But still before database is ready.
        System.out.println(String.format("Connection check: user=%s password=%s url=%s", dbUser, "**********", dbUrl));
        while (true) {
            try {
                Connection conn = DriverManager.getConnection(dbUrl, properties);
                closeConnection(conn);
                System.out.println(" - Database connection check passed");
                break;
            } catch (Exception ex) {
                //System.out.println(String.format("Connection check: %s", ex.getMessage()));
                if (System.currentTimeMillis() > endTm) {
                    System.out.println(String.format("Database is not ready within %d seconds", TIMEOUT));
                    throw new IllegalStateException(String.format("Database is not ready within %d seconds", TIMEOUT));
                }
                try {
                    Thread.sleep(SLEEP_MILIS);
                } catch (InterruptedException ie) {
                    System.out.println(String.format("Connection check: Thread was interrupted: %s", ie.getMessage()));
                }
            }
        }
        // Existence of users tablespace confirms that Oracle database is fully initialized.
        Connection conn = null;
        Statement stmt = null;
        System.out.println("Tablespace users check:");
        try {
            conn = DriverManager.getConnection(dbUrl, properties);
            stmt = conn.createStatement();
            while (true) {
                ResultSet rs = null;
                try {
                    rs = stmt.executeQuery(String.format("SELECT TABLESPACE_NAME FROM DBA_TABLESPACES WHERE TABLESPACE_NAME='USERS'", dbUser));
                    if (rs.next()) {
                        String tablespace = rs.getString(1);
                        if ("users".equalsIgnoreCase(tablespace)) {
                            System.out.println(" - Tablespace users was found.");
                            break;
                        }
                    }
                } catch (SQLException ex) {
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
                try {
                    Thread.sleep(SLEEP_MILIS);
                } catch (InterruptedException ie) {
                    System.out.println(String.format("Connection check: Thread was interrupted: %s", ie.getMessage()));
                }
            }
        } catch (Exception ex) {
            System.out.println(String.format(" - Database tablespace check failed: %s", ex.getMessage()));
            if (System.currentTimeMillis() > endTm) {
                System.out.println(String.format("Database is not ready within %d seconds", TIMEOUT));
                throw new IllegalStateException(String.format("Database is not ready within %d seconds", TIMEOUT));
            }
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    System.out.println(String.format("Could not close statement: %s", ex.getMessage()));
                }
            }
            if (conn != null) {
                closeConnection(conn);
            }
        }
    }

    private static void initDatabase(String dbSysUser, String dbSysPw, String dbUrl, String dbUser, String dbPassword) {
        Properties properties = new Properties();
        properties.put("user", dbSysUser);
        properties.put("password", dbSysPw);
        properties.put("internal_logon", "SYSDBA");
        Connection conn = null;
        System.out.println(String.format("Database initialization for user %s", dbUser));
        try {
            conn = DriverManager.getConnection(dbUrl, properties);
            executeStatement(conn, String.format("DROP USER %s CASCADE", dbUser));
            executeStatement(conn, String.format("CREATE USER %s IDENTIFIED BY %s DEFAULT TABLESPACE USERS TEMPORARY TABLESPACE TEMP QUOTA UNLIMITED ON USERS", dbUser, dbPassword));
            executeStatement(conn, String.format("GRANT CONNECT, RESOURCE, QUERY REWRITE TO %s", dbUser));
            executeStatement(conn, String.format("GRANT CREATE VIEW, CREATE ANY CONTEXT TO %s", dbUser));
            executeStatement(conn, String.format("GRANT EXECUTE ON dbms_rls TO %s", dbUser));
        } catch (Exception ex) {
            System.out.println(String.format("Database initialization failed: %s", ex.getMessage()));
        } finally {
            if (conn != null) {
                closeConnection(conn);
            }
        }
    }

    private static void executeStatement(Connection conn, String statement) {
        Statement stmt = null;
        try {
            System.out.println(String.format(" - Executing: %s", statement));
            stmt = conn.createStatement();
            stmt.execute(statement);
        }  catch (Exception ex) {
            System.out.println(String.format("   Database statement failed: %s", ex.getMessage()));
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    System.out.println(String.format("Could not close statement: %s", ex.getMessage()));
                }
            }
        }

    }

    // Close database connection.
    private static void closeConnection(final Connection connection) {
        try {
            connection.close();
        } catch (SQLException ex) {
            System.out.println(String.format("Could not close database connection: %s", ex.getMessage()));
        }
    }

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        final String dbSysUser = args.length > 0 ? args[0] : null;
        final String dbSysPw = args.length > 1 ? args[1] : null;
        final String dbUrl = args.length > 2 ? args[2] : null;
        final String dbUser =  args.length > 3 ? args[3] : null;
        final String dbPassword =  args.length > 4 ? args[4] : null;
        waitForDatabase(dbSysUser, dbSysPw, dbUrl);
        initDatabase(dbSysUser, dbSysPw, dbUrl, dbUser, dbPassword);
    }

}