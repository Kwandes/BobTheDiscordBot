/*
    Handles a RemindMe command. User sets a time after which the bot is supposed to message them with a custom reminder
 */

package dev.hotdeals.bob_the_discord_bot.command;

import dev.hotdeals.bob_the_discord_bot.BobTheDiscordBot;
import dev.hotdeals.bob_the_discord_bot.Service.MessageService;
import dev.hotdeals.bob_the_discord_bot.config.Config;
import dev.hotdeals.bob_the_discord_bot.model.Reminder;
import dev.hotdeals.bob_the_discord_bot.repository.ReminderRepo;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReminderCommand
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static Timer timer = null;

    public static void processReminderCommand(MessageReceivedEvent event)
    {
        List<String> splitMessage = MessageService.formatMessageArguments(event.getMessage().getContentRaw(), 3);

        if (splitMessage.size() < 3)
        {
            LOGGER.debug("The command had too few parameters");
            MessageService.sendErrorMessage(event.getChannel(), "The command has too few parameters! Use `" +
                    CoreCommands.findGuildCommandPrefix(event.getGuild().getId()) + "help reminder` to learn more");
            return;
        }

        if (ReminderCommand.convertTextToSeconds(splitMessage.get(1)) == 0)
        {
            LOGGER.debug("The reminder date is right now or the time format is invalid! Denying it");
            MessageService.sendErrorMessage(event.getChannel(), "Invalid time format. Format example: `1y4d3h2m1s` *(1 year 4 days 3 hours 2 minutes 1 second)*!" +
                    "\nMaximum period: `10 years`\nMinimum period: `1 second`" +
                    "\nUse `" + CoreCommands.findGuildCommandPrefix(event.getGuild().getId()) + "help reminder` to learn more");
            return;
        }

        LocalDateTime reminderDate = LocalDateTime.now().plusSeconds(ReminderCommand.convertTextToSeconds(splitMessage.get(1)));

        if (ReminderCommand.addReminder(event.getAuthor().getId(), reminderDate, splitMessage.get(2)))
        {
            LOGGER.info("Reminder added: `" + splitMessage.get(2) + "` on " + reminderDate + " by " + event.getAuthor());
            MessageService.sendEmbedMessage(event.getChannel(), "I'll remind you to `" + splitMessage.get(2) + "` in `" +
                    convertSecondsToTimePeriod(ReminderCommand.convertTextToSeconds(splitMessage.get(1))) + "`");
        } else
        {
            LOGGER.warn("An issue occurred while adding a reminder");
            MessageService.sendErrorMessage(event.getChannel(), "Failed to add a reminder, an issue occurred while processing it. Try again later");
        }
    }

    public static void checkReminders()
    {
        TimerTask sendReminders = new TimerTask()
        {
            @Override
            public void run()
            {
                LOGGER.debug("Checking reminders");
                ArrayList<Reminder> reminderList = ReminderRepo.fetchReminders();
                for (Reminder reminder : reminderList)
                {
                    if (reminder.getDateTime().isBefore(LocalDateTime.now()))
                    {
                        sendReminder(reminder);
                        deactivateReminder(reminder);
                    }
                }
            }
        };
        long timerFrequency = TimeUnit.SECONDS.toMillis(60);
        try
        {
            timerFrequency = TimeUnit.SECONDS.toMillis(Long.parseLong(Config.getInstance().getProperties().getProperty("reminderFrequency")));
        } catch (NumberFormatException e)
        {
            LOGGER.error("Frequency provided by the config was invalid", e);
        }
        timer = new Timer("Reminder Check");
        timer.schedule(sendReminders, TimeUnit.SECONDS.toMillis(10), timerFrequency);
        LOGGER.info("Reminder Timer has been initiated");
    }

    public static void cancelReminderTimer()
    {
        timer.cancel();
        LOGGER.info("Reminder timer has been canceled");
    }

    @Command(name = "reminder", aliases = {"remind", "remindme"}, description = "Sends a reminder via a private message after the specified period", structure = "reminder <time period> <message>")
    public static boolean addReminder(String userId, LocalDateTime time, String contents)
    {
        Reminder reminder = new Reminder(0, userId, time, contents, "active");
        return ReminderRepo.addReminder(reminder);
    }

    public static void sendReminder(Reminder reminder)
    {
        User user = BobTheDiscordBot.getJda().getUserById(reminder.getUserId());
        if (user == null)
        {
            LOGGER.warn("Failed to find a user " + reminder.getUserId() + ". Reminder " + reminder.getReminderId() + " has not been sent");
        } else
        {
            if (MessageService.sendPrivateMessage(user, "You've asked me to remind you: `" + reminder.getReminder() + "`"))
            {
                LOGGER.info("Reminder has been sent: " + reminder);
            } else
            {
                LOGGER.warn("Failed to send a reminder to " + reminder.getUserId());
            }
        }
    }

    public static void deactivateReminder(Reminder reminder)
    {
        reminder.setStatus("inactive");
        ReminderRepo.updateReminder(reminder);
        LOGGER.debug("Reminder " + reminder.getReminderId() + " has been deactivated");
    }

    public static long convertTextToSeconds(String time)
    {
        String pattern = "(\\d+)\\s?([a-zA-Z]*)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(time);
        long timeSum = 0;

        while (matcher.find())
        {
            try
            {
                if (matcher.group(2).isEmpty())
                {
                    timeSum += Long.parseLong(matcher.group(1));
                } else
                {
                    switch (matcher.group(2).substring(0, 1))
                    {
                        case "y":
                            timeSum += Long.parseLong(matcher.group(1)) * 86400 * 365;
                            break;
                        case "d":
                            timeSum += Long.parseLong(matcher.group(1)) * 86400;
                            break;
                        case "h":
                            timeSum += Long.parseLong(matcher.group(1)) * 3600;
                            break;
                        case "m":
                            timeSum += Long.parseLong(matcher.group(1)) * 60;
                            break;
                        case "s":
                        default:
                            timeSum += Long.parseLong(matcher.group(1));
                            break;
                    }
                }
            } catch (NumberFormatException e)
            {
                LOGGER.error("Failed to parse to a Long", e);
                return 0;
            }
        }
        if (timeSum > 315360000) return 0; // longer than 10 years
        return timeSum;
    }

    public static String convertSecondsToTimePeriod(long seconds)
    {
        if (seconds < 60)
        {
            if (seconds == 1)
                return seconds + " second";
            else
                return seconds + " seconds";

        } else if (seconds < 3600)
        {
            if (seconds / 60 == 1)
                return seconds / 60 + " minute";
            else
                return seconds / 60 + " minutes";

        } else if (seconds < 86400)
        {
            if (seconds / 3600 == 1)
                return seconds / 3600 + " hour";
            else
                return seconds / 3600 + " hours";

        } else if (seconds < 31536000)
        {
            if (seconds / 86400 == 1)
                return seconds / 86400 + " day";
            else
                return seconds / 86400 + " days";

        } else
        {
            if (seconds / 31536000 == 1)
                return seconds / 31536000 + " year";
            else
                return seconds / 31536000 + " years";
        }
    }
}
