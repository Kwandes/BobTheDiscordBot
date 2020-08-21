package dev.hotdeals.bob_the_discord_bot.Service;

import dev.hotdeals.bob_the_discord_bot.BobTheDiscordBot;
import dev.hotdeals.bob_the_discord_bot.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.awt.Color;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.*;

public class MessageService
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static boolean sendMessage(MessageChannel channel, String message)
    {
        try
        {
            channel.sendMessage(message).queue();
            LOGGER.debug("Message `" + message + "` sent to " + channel);
            return true;
        } catch (ErrorResponseException e)
        {
            LOGGER.error("Message has failed to send", e);
            return false;
        }
    }

    public static boolean sendMessage(MessageChannel channel, MessageEmbed embed)
    {
        try
        {
            channel.sendMessage(embed).queue();
            LOGGER.debug("Embed sent to " + channel);
            return true;
        } catch (ErrorResponseException e)
        {
            LOGGER.error("Embed has failed to send", e);
            return false;
        }
    }

    /**
     * Converts a string to an embed and sends it
     *
     * @param channel a text channel that the message will be sent to
     * @param message a string that is going to be converted to an embed and sent
     * @return whether or not the message was sent successfully
     */
    public static boolean sendEmbedMessage(MessageChannel channel, String message)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setDescription(message);
        embed.setColor(getEmbedColor());

        return sendMessage(channel, embed.build());
    }

    public static boolean sendPrivateMessage(User user, String message)
    {
        try
        {
            user.openPrivateChannel().queue((privateChannel) ->
                    privateChannel.sendMessage(message).queue());
            LOGGER.debug("Private Message sent to " + user);
            return true;
        } catch (ErrorResponseException e)
        {
            LOGGER.error("Private message has failed to send", e);
            return false;
        }
    }

    public static boolean sendErrorMessage(MessageChannel channel, String message)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Yikes...");
        embed.setColor(getEmbedErrorColor());
        embed.setDescription(message);
        return sendMessage(channel, embed.build());
    }

    public static void sendBootMessage()
    {
        Config config = BobTheDiscordBot.getConfig();
        JDA jda = BobTheDiscordBot.getJda();
        if (!config.getProperties().getProperty("monitoringChannel").isEmpty())
        {
            try
            {
                TextChannel monitoringChannel = jda.getTextChannelById(config.getProperties().getProperty("monitoringChannel"));

                if (monitoringChannel != null)
                {
                    String buildVersion = "N/A";
                    String jdaVersion = "N/A";
                    String javaVersion = "N/A";
                    Model model = getPomModel();
                    if (model != null)
                    {
                        buildVersion = model.getVersion();
                        jdaVersion = model.getDependencies().get(0).getVersion();
                        javaVersion = model.getProperties().getProperty("maven.compiler.source");
                    } else
                        LOGGER.warn("Failed to read the pom file");

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("The bot has booted up");
                    if (jda.getPresence().getActivity() != null)
                        embed.setDescription(jda.getPresence().getActivity().getName() + "!");
                    embed.addField("Build Info", "```fix\nVersion: " + buildVersion + "\nJDA: " + jdaVersion + "\nJava: " + javaVersion + "```", false);
                    embed.setFooter(LocalDateTime.now().toString());
                    embed.setColor(MessageService.getEmbedColor());
                    MessageService.sendMessage(monitoringChannel, embed.build());
                } else LOGGER.warn("The monitoring channel is not valid!");
            } catch (NumberFormatException e)
            {
                LOGGER.warn("The provided channel ID was invalid", e);
            }
        }
    }

    /**
     * Sends a log message to the monitoring channel
     *
     * @param logEvent the log to be sent
     */
    public static void sendLogMessage(LogEvent logEvent)
    {
        Config config = BobTheDiscordBot.getConfig();
        JDA jda = BobTheDiscordBot.getJda();
        if (!config.getProperties().getProperty("monitoringChannel").isEmpty())
        {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(getEmbedErrorColor());
            embed.setTitle("[" + logEvent.getLevel().toString() + "]");
            if (logEvent.getMessage().getThrowable() != null)
            {
                embed.setDescription(logEvent.getMessage().getFormattedMessage() + " - " + logEvent.getMessage().getThrowable().getMessage());
            } else
                embed.setDescription(logEvent.getMessage().getFormattedMessage());
            embed.setFooter(LocalDateTime.now().toString());
            TextChannel monitoringChannel = jda.getTextChannelById(config.getProperties().getProperty("monitoringChannel"));
            if (monitoringChannel != null)
                monitoringChannel.sendMessage(embed.build()).queue();
        }
    }

    /**
     * Splits a message and combines the remainder, if it exists
     * <p>
     * All arguments except for the remainder are converted to lower cas
     *
     * @param message       the message to be formatted
     * @param argumentCount how many arguments are supposed to exist. All extra arguments are merged with the last one
     * @return split and formatted message, as a string array
     */
    public static List<String> formatMessageArguments(String message, int argumentCount)
    {
        if (argumentCount < 1) argumentCount = 1;
        List<String> splitMessage = new ArrayList<>(Arrays.asList(message.split(" ")));
        for (int i = 0; i < splitMessage.size(); i++)
        {
            if (i >= argumentCount)
            {
                splitMessage.set(argumentCount - 1, splitMessage.get(argumentCount - 1) + " " + splitMessage.get(i));
                splitMessage.set(i, "");
            }
        }
        splitMessage.removeAll(Collections.singletonList(""));

        for (int i = 0; i < splitMessage.size(); i++)
        {
            if (i == 0)
                splitMessage.set(i, splitMessage.get(i).toLowerCase());
            if (i < argumentCount - 1)
            {
                splitMessage.set(i, splitMessage.get(i).toLowerCase());
            }
        }
        return splitMessage;
    }

    public static Color getEmbedColor()
    {
        Color color = new Color(0x6A2396);
        try
        {
            color = new Color(Integer.decode(Config.getInstance().getProperties().getProperty("embedColor")));
        } catch (NumberFormatException e)
        {
            LOGGER.warn("Failed to parse embed color from the config: " + Config.getInstance().getProperties().getProperty("embedColor"));
        }
        return color;
    }

    public static Color getEmbedErrorColor()
    {
        Color color = Color.RED;
        try
        {
            color = new Color(Integer.decode(Config.getInstance().getProperties().getProperty("embedErrorColor")));
        } catch (NumberFormatException e)
        {
            LOGGER.warn("Failed to parse embed Error color from the config");
        }
        return color;
    }

    public static Model getPomModel()
    {
        try
        {
            Model model;
            InputStream stream;
            if ((new File("pom.xml")).exists())
                stream = new FileInputStream("pom.xml");
            else
                stream = MessageService.class.getResourceAsStream("/META-INF/maven/dev.hotdeals/bob_the_discord_bot/pom.xml");
            model = new MavenXpp3Reader().read(stream);
            stream.close();
            return model;
        } catch (XmlPullParserException | IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
