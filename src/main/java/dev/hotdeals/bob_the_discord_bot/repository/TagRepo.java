package dev.hotdeals.bob_the_discord_bot.repository;

import dev.hotdeals.bob_the_discord_bot.config.JdbcConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class TagRepo
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static HashMap<String, String> fetchTagsForGuild(String guildId)
    {
        try
        {
            PreparedStatement statement = JdbcConfig.getConnection().prepareStatement(
                    "SELECT tag_name, tag_content FROM tag WHERE guild_id = ?");
            statement.setString(1, guildId);
            ResultSet rs = statement.executeQuery();

            HashMap<String, String> result = new HashMap<String, String>();
            while (rs.next())
            {
                result.put(rs.getString("tag_name"), rs.getString("tag_content"));
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
        return new HashMap<String, String>();
    }

    public static boolean createTag(String guildId, String tagName, String tagContent)
    {
        try
        {
            PreparedStatement statement = JdbcConfig.getConnection().prepareStatement(
                    "INSERT INTO tag (guild_id, tag_name, tag_content) VALUES (?, ?, ?)");
            statement.setString(1, guildId);
            statement.setString(2, tagName);
            statement.setString(3, tagContent);
            return statement.executeUpdate() > 0;
        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
        return false;
    }

    public static boolean updateTag(String guildId, String tagName, String tagContent)
    {
        try
        {
            PreparedStatement statement = JdbcConfig.getConnection().prepareStatement(
                    "UPDATE tag SET tag_name = ?, tag_content = ? WHERE guild_id = ? AND tag_name = ?");
            statement.setString(1, tagName);
            statement.setString(2, tagContent);
            statement.setString(3, guildId);
            statement.setString(4, tagName);
            return statement.executeUpdate() > 0;

        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
        return false;
    }

    public static boolean deleteTag(String guildId, String tagName)
    {
        try
        {
            PreparedStatement statement = JdbcConfig.getConnection().prepareStatement(
                    "DELETE FROM tag WHERE guild_id = ? AND tag_name = ?");
            statement.setString(1, guildId);
            statement.setString(2, tagName);
            return statement.executeUpdate() > 0;
        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
        return false;
    }
}
