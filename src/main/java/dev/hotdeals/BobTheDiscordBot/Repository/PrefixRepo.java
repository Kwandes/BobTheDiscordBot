/*
    Repository layer - database calls using JDBC
    This class handles the calls regarding the command prefix
 */

package dev.hotdeals.BobTheDiscordBot.Repository;

import dev.hotdeals.BobTheDiscordBot.Config.JdbcConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.HashMap;

public class PrefixRepo
{
    final static Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static HashMap<String, String> fetchPrefixes()
    {
        try
        {
            PreparedStatement statement = JdbcConfig.getConnection().prepareStatement("SELECT guild_id, prefix FROM prefix");
            ResultSet rs = statement.executeQuery();

            HashMap<String, String> result = new HashMap<String, String>();
            while (rs.next())       // extract the prefix from the resultSet
            {
                result.put(rs.getString("guild_id"), rs.getString("prefix"));
            }
            return result;

        } catch (SQLException e)
        {
            logger.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            logger.error("Database connection is null, probably due to invalid configuration");
        }
        // if the response contained no entries, return an empty hashmap
        return new HashMap<String, String>();
    }

    public static void setPrefixForGuild(String guildId, String newPrefix)
    {
        try
        {
            PreparedStatement statement = JdbcConfig.getConnection()
                    .prepareStatement("INSERT INTO prefix (guild_id, prefix) VALUES (?, ?)" +
                            " ON DUPLICATE KEY UPDATE prefix = ?");
            statement.setString(1, guildId);
            statement.setString(2, newPrefix);
            statement.setString(3, newPrefix);
            statement.executeUpdate();
        } catch (SQLException e)
        {
            logger.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            logger.error("Database connection is null, probably due to invalid configuration");
        }
    }
}