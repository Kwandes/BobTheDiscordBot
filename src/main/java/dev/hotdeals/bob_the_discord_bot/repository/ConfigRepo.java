package dev.hotdeals.bob_the_discord_bot.repository;

import dev.hotdeals.bob_the_discord_bot.config.JdbcConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class ConfigRepo
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static Properties fetchConfig()
    {
        Properties properties = new Properties();

        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM config");
            ResultSet rs = statement.executeQuery();

            while (rs.next())
            {
                properties.put("activity", rs.getString("activity"));
                properties.put("defaultCommandPrefix", rs.getString("defaultCommandPrefix"));
                properties.put("embedColor", rs.getString("embedColor"));
                properties.put("embedErrorColor", rs.getString("embedErrorColor"));
                properties.put("monitoringChannel", rs.getString("monitoringChannel"));
                properties.put("botToken", rs.getString("botTokenVarName"));
                properties.put("reminderFrequency", rs.getString("reminderFrequency"));
            }
        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
        return properties;
    }
}
