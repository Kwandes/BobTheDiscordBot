/*
    Configuration class for the application runtime variables
    Uses the application.properties file
 */

package dev.hotdeals.BobTheDiscordBot.Config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

public class Config
{
    final static Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static Properties properties;
    private static final String configFileName = "application.properties";

    public static void loadProperties() throws IOException
    {
        properties = new Properties();
        FileInputStream fi = new FileInputStream("src/main/resources/" + configFileName);
        properties.load(fi);
        logger.debug("The config.properties file has been loaded");
        logger.debug("Retrieving and assigning environment variables");
        setEnvVariables();
        logger.debug("Environment variables have been loaded and set");
    }

    // the properties file cannot process the env variables so the config holds names of the env vars instead
    private static void setEnvVariables() throws NullPointerException
    {
        logger.debug("Processing the bot token env variable");
        properties.setProperty("botToken", System.getenv(Config.getProperties().getProperty("botToken")));
    }

    public static Properties getProperties()
    {
        return properties;
    }
}