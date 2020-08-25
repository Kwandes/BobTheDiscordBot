/*
    Repository layer - database calls using JDBC
    This class handles the calls regarding the command prefix
 */

package dev.hotdeals.bob_the_discord_bot.repository;

import dev.hotdeals.bob_the_discord_bot.config.JdbcConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.HashMap;

public class PrefixRepo
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static HashMap<String, String> fetchPrefixes()
    {
        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("SELECT guild_id, prefix FROM prefix");
            ResultSet rs = statement.executeQuery();

            HashMap<String, String> result = new HashMap<>();
            while (rs.next())       // extract the prefix from the resultSet
            {
                result.put(rs.getString("guild_id"), rs.getString("prefix"));
            }
            return result;

        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
        // if the response contained no entries, return an empty hashmap
        return new HashMap<>();
    }

    public static void setPrefixForGuild(String guildId, String newPrefix)
    {
        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO prefix (guild_id, prefix) VALUES (?, ?)" +
                    " ON DUPLICATE KEY UPDATE prefix = ?");
            statement.setString(1, guildId);
            statement.setString(2, newPrefix);
            statement.setString(3, newPrefix);
            statement.executeUpdate();
        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
    }
}