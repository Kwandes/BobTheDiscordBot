package dev.hotdeals.bob_the_discord_bot.repository;

import dev.hotdeals.bob_the_discord_bot.config.JdbcConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RankRepo
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static boolean isAdmin(String userId, String guildId)
    {
        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT * FROM userRank WHERE userId = ? AND guildId = ? AND userRank = 'Administrator')");
            statement.setString(1, userId);
            statement.setString(2, guildId);
            ResultSet rs = statement.executeQuery();
            rs.next();
            return rs.getString(1).equals("1");
        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
        return false;
    }

    public static boolean isDeveloper(String userId)
    {
        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT * FROM userRank WHERE userId = ? AND userRank = 'Developer')");
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            rs.next();
            return rs.getString(1).equals("1");
        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
        return false;
    }

    public static void addAdmin(String userId, String guildId)
    {
        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO userRank (userId, guildId, userRank) VALUES (?, ?, ?)");
            statement.setString(1, userId);
            statement.setString(2, guildId);
            statement.setString(3, "Administrator");
            statement.executeUpdate();
        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
    }

    public static void addDeveloper(String userId)
    {
        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO userRank (userId, guildId, userRank) VALUES (?, ?, ?)");
            statement.setString(1, userId);
            statement.setString(2, "");
            statement.setString(3, "Developer");
            statement.executeUpdate();
        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
    }

    public static void removeDeveloper(String userId)
    {
        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM userRank WHERE userId = ? AND userRank = ?");
            statement.setString(1, userId);
            statement.setString(2, "Developer");
            statement.executeUpdate();
        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
    }

    public static void removeAdministrator(String userId, String guildId)
    {
        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM userRank WHERE userId = ? AND guildId = ? AND userRank = ?");
            statement.setString(1, userId);
            statement.setString(2, guildId);
            statement.setString(3, "Administrator");
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
