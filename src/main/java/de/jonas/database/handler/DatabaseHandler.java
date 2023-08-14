package de.jonas.database.handler;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Mithilfe dieses {@link DatabaseHandler} wird eine Verbindung zu einer MariaDB Datenbank hergestellt. Durch diesen
 * Handler ist die Verbindung sehr stabil und kann nur sehr schwer durch einen Fehler getrennt werden.
 */
@NotNull
public final class DatabaseHandler {

    //<editor-fold desc="CONSTANTS">

    //<editor-fold desc="constraints">
    /** Die maximale Zeichen-Länge des Pool-Namens. */
    @Range(from = 32, to = 32)
    private static final int MAX_POOL_NAME_LENGTH = 32;
    //</editor-fold>

    //<editor-fold desc="bounds">
    /** Die minimale Größe des Connection-Pools. */
    @Range(from = 5, to = 5)
    private static final int POOL_MIN = 5;
    /** Die maximale Größe des Connection-Pools. */
    @Range(from = 5, to = 5)
    private static final int POOL_MAX = 5;
    //</editor-fold>

    //<editor-fold desc="maintenance">
    /** Die Zeit, die eine Verbindung in dem Pool maximal bestehen darf, ohne genutzt zu werden. */
    @Range(from = 0, to = Integer.MAX_VALUE)
    private static final int MAX_LIFETIME = 15 * 60 * 1000;
    /** Die Zeit, die der Handler versucht eine Verbindung zu der Datenbank herzustellen, wenn er keine bekommt. */
    @Range(from = 0, to = Integer.MAX_VALUE)
    private static final int CONNECT_TIMEOUT = 15 * 1000;
    //</editor-fold>

    //<editor-fold desc="optimization">
    /** Die Anzahl an {@link PreparedStatement}, die der Handler pro Verbindung cached. */
    @Range(from = 0, to = Integer.MAX_VALUE)
    private static final int PREPARED_STATEMENT_CACHE_SIZE = 300;
    /** Die maximale Länge eines {@link PreparedStatement}, die der Handler cached. */
    @Range(from = 0, to = Integer.MAX_VALUE)
    private static final int PREPARED_STATEMENT_SQL_LIMIT = 2048;
    /** Die Anzahl an {@link CallableStatement}, die der Handler pro Verbindung cached. */
    @Range(from = 0, to = Integer.MAX_VALUE)
    private static final int CALLABLE_STATEMENT_CACHE_SIZE = 300;
    /** Die Anzahl an Anfragen, die "gestapelt" gesendet werden dürfen, die dann nach und nach verarbeitet werden. */
    @Range(from = 0, to = Integer.MAX_VALUE)
    private static final int USE_BATCH_MULTI_SEND_NUMBER = 500;
    //</editor-fold>

    //</editor-fold>


    //<editor-fold desc="LOCAL FIELDS">
    /** Die Daten-Quelle der Datenbank, die die Verbindungen aus dem Pool verwaltet. */
    @Getter
    @NotNull
    private final HikariDataSource dataSource;
    //</editor-fold>


    //<editor-fold desc="CONSTRUCTORS">

    /**
     * Erzeugt mithilfe eines Pool-Namens und von {@link Properties} einen neuen und vollständig unabhängigen
     * {@link DatabaseHandler}. Mit einem {@link DatabaseHandler} wird eine Verbindung zu einer Datenbank hergestellt.
     * Diese Verbindung ist äußerst sicher und kann nur sehr schwer einfach so getrennt werden.
     *
     * @param poolName   Der Pool-Name.
     * @param properties Die {@link Properties}.
     */
    public DatabaseHandler(
        @NotNull final String poolName,
        @NotNull final Properties properties
    ) {
        // check the pool name for validity
        if (poolName.isEmpty()) {
            throw new IllegalArgumentException("The supplied pool name was empty!");
        }
        if (poolName.length() > MAX_POOL_NAME_LENGTH) {
            throw new IllegalArgumentException("The supplied pool name exceeded the maximum length of " + MAX_POOL_NAME_LENGTH + " chars!");
        }

        // initialize properties based configuration as defaults
        final HikariConfig config = new HikariConfig(properties);

        // set visual metadata
        config.setPoolName("mariadb-" + poolName);

        // set optimized mariadb datasource implementation
        config.setDriverClassName("org.mariadb.jdbc.Driver");

        // set pool size limits
        config.setMinimumIdle(POOL_MIN);
        config.setMaximumPoolSize(POOL_MAX);

        // set pool time limits
        config.setMaxLifetime(MAX_LIFETIME);

        // set pool-initialization timeout
        config.setInitializationFailTimeout(CONNECT_TIMEOUT);

        // configuration: define options that cannot be overridden
        config.addDataSourceProperty("passwordCharacterEncoding", "UTF-8");
        config.addDataSourceProperty("useCompression", false);
        config.addDataSourceProperty("tinyInt1isBit", true);
        config.addDataSourceProperty("yearIsDateType", true);

        // performance: enable prepared statement caching
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", PREPARED_STATEMENT_CACHE_SIZE);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", PREPARED_STATEMENT_SQL_LIMIT);
        config.addDataSourceProperty("useServerPrepStmts", true);

        // performance: enable callable statement caching
        config.addDataSourceProperty("cacheCallableStmts", true);
        config.addDataSourceProperty("callableStmtCacheSize", CALLABLE_STATEMENT_CACHE_SIZE);

        // performance: add batch performance optimizations
        config.addDataSourceProperty("useBatchMultiSend", true);
        config.addDataSourceProperty("useBatchMultiSendNumber", USE_BATCH_MULTI_SEND_NUMBER);

        // security: disable local access
        config.addDataSourceProperty("allowLocalInfile", false);

        // security: disable multi queries (injection mitigation)
        config.addDataSourceProperty("allowMultiQueries", false);


        // assign newly created datasource
        this.dataSource = new HikariDataSource(config);
    }
    //</editor-fold>


    //<editor-fold desc="connection">

    /**
     * Gibt eine Verbindung zu der Datenbank auf der Grundlage der {@link HikariDataSource Quelle}, welche beim
     * Erstellen dieser Instanz neu erzeugt wurde, zurück. Die Verbindung wird aus dem Connection-Pool gewählt.
     *
     * @return Eine Verbindung zu der Datenbank auf der Grundlage der {@link HikariDataSource Quelle}, welche beim
     *     Erstellen dieser Instanz neu erzeugt wurde.
     *
     * @throws SQLException Die Fehlermeldung, die auftreten kann, wenn die Verbindung fehlerhaft sein sollte.
     */
    @NotNull
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    //</editor-fold>
}
