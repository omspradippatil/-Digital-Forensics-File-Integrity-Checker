package com.dffic.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection class
 * Manages JDBC connections to MySQL database.
 * Reads configuration from properties file securely.
 */
public class DatabaseConnection {

    private static final String CONFIG_FILE = "config/database.properties";
    private static final String DEFAULT_CONFIG = "config/database.properties.example";

    private String url;
    private String username;
    private String password;

    /**
     * Constructor that loads database configuration.
     * Uses database.properties if it exists, otherwise uses example file.
     *
     * @throws IOException If configuration cannot be loaded
     */
    public DatabaseConnection() throws IOException {
        loadConfiguration();
    }

    /**
     * Constructor with explicit credentials.
     * Useful for testing or when configuration file is not available.
     *
     * @param host     Database hostname
     * @param port     Database port
     * @param database Database name
     * @param username Database username
     * @param password Database password
     */
    public DatabaseConnection(String host, String port, String database,
                              String username, String password) {
        this.url = String.format("jdbc:mysql://%s:%s/%s", host, port, database);
        this.username = username;
        this.password = password;
    }

    /**
     * Get a database connection using JDBC.
     * Uses try-with-resources pattern for automatic resource management.
     *
     * @return Connection to MySQL database
     * @throws SQLException If connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-j.jar to classpath.", e);
        }

        // Establish connection with configured credentials
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Test database connectivity.
     *
     * @return true if connection can be established
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Test database connectivity with timeout.
     *
     * @param timeoutSeconds Maximum time to wait for connection
     * @return true if connection successful within timeout
     */
    public boolean testConnectionWithTimeout(int timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (testConnection()) {
                return true;
            }
            try {
                Thread.sleep(1000); // Wait 1 second between attempts
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Load configuration from properties file.
     *
     * @throws IOException If configuration file cannot be read
     */
    private void loadConfiguration() throws IOException {
        Properties properties = new Properties();
        String configPath = CONFIG_FILE;

        try {
            // Try to load from main config file
            properties.load(new FileInputStream(configPath));
        } catch (IOException e) {
            // Try example file as fallback
            try {
                configPath = DEFAULT_CONFIG;
                properties.load(new FileInputStream(configPath));
                System.out.println("Warning: Using example configuration. Copy database.properties.example to database.properties and edit with your credentials.");
            } catch (IOException ex) {
                throw new IOException("Cannot find database configuration file. Expected at: " + CONFIG_FILE +
                        " or " + DEFAULT_CONFIG, ex);
            }
        }

        // Read configuration values
        String host = properties.getProperty("db.host", "localhost");
        String port = properties.getProperty("db.port", "3306");
        String database = properties.getProperty("db.name", "digital_forensics");
        this.username = properties.getProperty("db.user", "root");
        this.password = properties.getProperty("db.password", "");

        // Build JDBC URL
        this.url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC",
                host, port, database);
    }

    /**
     * Get the JDBC URL used for connections.
     *
     * @return JDBC URL string
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the database username.
     *
     * @return Username string
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set database credentials programmatically.
     * Useful for runtime configuration changes.
     *
     * @param host     Database host
     * @param port     Database port
     * @param database Database name
     * @param username Database username
     * @param password Database password
     */
    public void setCredentials(String host, String port, String database,
                               String username, String password) {
        this.url = String.format("jdbc:mysql://%s:%s/%s", host, port, database);
        this.username = username;
        this.password = password;
    }

    /**
     * Close a connection properly.
     *
     * @param connection The connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    /**
     * Rollback a transaction safely.
     *
     * @param connection The connection with active transaction
     */
    public static void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                System.err.println("Error rolling back transaction: " + e.getMessage());
            }
        }
    }
}
