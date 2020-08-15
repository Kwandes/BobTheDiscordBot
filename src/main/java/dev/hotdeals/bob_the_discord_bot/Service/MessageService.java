package dev.hotdeals.bob_the_discord_bot.Service;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        embed.setColor(new Color(0x6A2396));

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
        embed.setColor(new Color(0xff0000));
        embed.setDescription(message);
        return sendMessage(channel, embed.build());
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
}
