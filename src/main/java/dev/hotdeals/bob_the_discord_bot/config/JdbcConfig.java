/*
    Configuration class for the database connection using JDBC
    Uses the jdbcConnection.properties file
 */

package dev.hotdeals.bob_the_discord_bot.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcConfig
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static JdbcConfig instance = null;

    private JdbcConfig()
    {
        LOGGER.debug("A config instance has been created");
    }

    public static JdbcConfig getInstance()
    {
        if (instance == null)
            instance = new JdbcConfig();
        return instance;
    }

    private Properties properties;
    private final String configFileName = "jdbcConnection.properties";

    public void loadProperties() throws IOException
    {
        properties = new Properties();
        FileInputStream fi = new FileInputStream("src/main/resources/" + configFileName);
        properties.load(fi);
        LOGGER.trace("The config.properties file has been loaded");
    }

    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(properties.getProperty("jdbcUrl"),
                this.properties.getProperty("jdbcUsername"), properties.getProperty("jdbcPassword"));
    }
}