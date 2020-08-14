package dev.hotdeals.bob_the_discord_bot.repository;

import dev.hotdeals.bob_the_discord_bot.config.JdbcConfig;
import dev.hotdeals.bob_the_discord_bot.model.Reminder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReminderRepo
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static ArrayList<Reminder> fetchReminders()
    {
        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("SELECT id, user_id, datetime, reminder, status FROM reminder WHERE status = 'active'");
            ResultSet rs = statement.executeQuery();

            ArrayList<Reminder> result = new ArrayList<>();
            while (rs.next())
            {
                result.add(new Reminder(rs.getInt("id"), rs.getString("user_id"),
                        rs.getString("dateTime"), rs.getString("reminder"), rs.getString("status")));
            }
            return result;

        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
        return new ArrayList<>();
    }

    public static boolean addReminder(Reminder reminder)
    {
        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO reminder (user_id, datetime, reminder, status) VALUES (?, ?, ?, ?)");
            statement.setString(1, reminder.getUserId());
            statement.setString(2, reminder.getDateTime().toString());
            statement.setString(3, reminder.getReminder());
            statement.setString(4, reminder.getStatus());
            statement.executeUpdate();
            return true;
        } catch (SQLException e)
        {
            LOGGER.error("An error occurred while performing a query", e);
        } catch (NullPointerException e)
        {
            LOGGER.error("Database connection is null, probably due to invalid configuration");
        }
        return false;
    }

    public static void updateReminder(Reminder reminder)
    {
        try (Connection connection = JdbcConfig.getInstance().getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("UPDATE reminder SET user_id = ?, datetime = ?, reminder = ?, status = ? WHERE id = ?");
            statement.setString(1, reminder.getUserId());
            statement.setString(2, reminder.getDateTime().toString());
            statement.setString(3, reminder.getReminder());
            statement.setString(4, reminder.getStatus());
            statement.setString(5, Integer.toString(reminder.getReminderId()));
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
