/*
    Configuration class for the database connection using JDBC
    Uses the jdbcConnection.properties file
 */

package dev.hotdeals.bob_the_discord_bot.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        this.properties = new Properties();
        this.properties.load(this.getClass().getResourceAsStream("/" + configFileName));
        LOGGER.debug("The JdbcConfig.properties file has been loaded");

        LOGGER.debug("Processing the JDBC URL env variable");
        try
        {
            String botToken = System.getenv(this.properties.getProperty("jdbcUrl"));
            this.properties.setProperty("jdbcUrl", botToken);
        } catch (NullPointerException e)
        {
            LOGGER.error("Failed to load jdbc environment variables!", e);
        }
    }

    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(properties.getProperty("jdbcUrl"));
    }
}