/*
    Configuration class for the database connection using JDBC
    Uses the jdbcConnection.properties file
 */

package dev.hotdeals.BobTheDiscordBot.Config;

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
    final static Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static Properties properties;
    private static final String configFileName = "jdbcConnection.properties";

    private static Connection connection;


    public static void loadProperties() throws IOException, SQLException
    {
        properties = new Properties();
        FileInputStream fi = new FileInputStream("src/main/resources/" + configFileName);
        properties.load(fi);
        logger.trace("The config.properties file has been loaded");
        setConnection();
        logger.trace("The JDBC connection has been loaded");
    }

    public static void setConnection() throws SQLException
    {

        connection = DriverManager.getConnection(properties.getProperty("jdbcUrl"),
                properties.getProperty("jdbcUsername"), properties.getProperty("jdbcPassword"));
    }

    public static Connection getConnection()
    {
        return connection;
    }
}