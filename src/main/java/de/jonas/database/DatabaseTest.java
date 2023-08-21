package de.jonas.database;

import de.jonas.database.handler.DatabaseHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;

/**
 * Die Haupt- und Main-Klasse dieses {@link DatabaseTest Test-Projekts}, mit dessen Hilfe eine stabile Verbindung zu
 * einer Datenbank hergestellt werden soll.
 */
public final class DatabaseTest {

    //<editor-fold desc="CONSTANTS">
    /** Der Pool-Name, mit dem der {@link DatabaseHandler} initialisiert wird. */
    private static final String POOL_NAME = "TestDB";
    /** Die Adresse, unter der die Datenbank angesprochen wird. */
    private static final String JDBC_URL = "jdbc:mariadb://localhost:3306/testDB";
    /** Der Nutzer, mit dem auf die Datenbank zugegriffen wird. */
    private static final String USER = "testUser";
    /** Das Passwort, welches zu dem {@code USER} gehört. */
    private static final String PASSWORD = "password";
    //</editor-fold>


    //<editor-fold desc="STATIC FIELDS">
    /** Der {@link DatabaseHandler}, mit der die Verbindung zu der Datenbank hergestellt wird. */
    private static DatabaseHandler databaseHandler;
    //</editor-fold>


    //<editor-fold desc="main">

    /**
     * Die Main-Methode dieser Anwendung, welche als erstes aufgerufen wird und die Schnittstelle zu der JRE darstellt.
     *
     * @param args Die Argumente, die beim Ausführen dieser Anwendung übergeben wird.
     */
    public static void main(final String[] args) {
        // save properties to instance database-handler
        final Properties databaseProperties = new Properties();
        databaseProperties.put("jdbcUrl", JDBC_URL);
        databaseProperties.put("dataSource.user", USER);
        databaseProperties.put("dataSource.password", PASSWORD);

        // create database-handler
        databaseHandler = new DatabaseHandler(
            POOL_NAME,
            databaseProperties
        );

        // create table
        createTestTable();

        // insert values
        try {
            insertValues("Name1", 1);
            Thread.sleep(500);
            insertValues("Name2", 2);
            Thread.sleep(500);
            insertValues("Name3", 3);
            Thread.sleep(500);
            insertValues("Name4", 4);
            Thread.sleep(500);
            insertValues("Name5", 5);
            Thread.sleep(500);
            insertValues("Name6", 6);
            Thread.sleep(500);
            insertValues("Name7", 7);
            Thread.sleep(500);
            insertValues("Name8", 8);
            Thread.sleep(500);
            insertValues("Name9", 9);
            Thread.sleep(500);
            insertValues("Name10", 10);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        // get the newest name
        System.out.println(getNewestName());
    }
    //</editor-fold>


    /**
     * Gibt den {@link DatabaseHandler} zurück, mit dem eine stabile Verbindung zur Datenbank hergestellt wurde.
     * @return Der {@link DatabaseHandler}, mit dem eine stabile Verbindung zur Datenbank hergestellt wurde.
     */
    public static DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }


    //<editor-fold desc="utility">

    /**
     * Erzeugt eine Test-Tabelle
     */
    private static void createTestTable() {
        try (final Connection conn = getDatabaseHandler().getConnection()) {
            final PreparedStatement stmt = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS testTable("
                    + "userName VARCHAR(255) PRIMARY KEY, "
                    + "age INT, "
                    + "date DATETIME(1)"
                    + ")"
            );

            stmt.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fügt einen neuen Datensatz hinzu, welcher aus einem Namen, einem Alter und einem Zeitpunkt besteht, wobei der
     * Zeitpunkt nicht mit übergeben wird, da immer der aktuelle Zeitpunkt gewählt wird. Sollte der Name bereits
     * verwendet werden, wird einfach nur der Zeitstempel aktualisiert.
     *
     * @param name Der Name, welcher für diesen Eintrag genutzt werden soll.
     * @param age  Das Alter, welches für diesen Eintrag genutzt werden soll.
     */
    private static void insertValues(
        final String name,
        final int age
    ) {
        try (final Connection conn = getDatabaseHandler().getConnection()) {
            final PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO testTable (userName, age, date) VALUES (?, ?, @date:=?) "
                    + "ON DUPLICATE KEY UPDATE date=@date"
            );

            stmt.setString(1, name);
            stmt.setInt(2, age);
            stmt.setTimestamp(3, Timestamp.from(Instant.now()));

            stmt.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gibt den Namen zurück, der zuletzt eingefügt wurde in der Datenbank.
     *
     * @return Der Name, der zuletzt eingefügt wurde in der Datenbank.
     */
    private static String getNewestName() {
        try (final Connection conn = getDatabaseHandler().getConnection()) {
            final PreparedStatement stmt = conn.prepareStatement(
                "SELECT userName as name FROM testTable WHERE date IS NOT NULL ORDER BY date DESC"
            );

            final ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        }

        return "";
    }
    //</editor-fold>

}
