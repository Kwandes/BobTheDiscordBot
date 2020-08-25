package dev.hotdeals.bob_the_discord_bot.command;

import dev.hotdeals.bob_the_discord_bot.BobTheDiscordBot;
import dev.hotdeals.bob_the_discord_bot.Service.MessageService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class MessageCommand
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Command(name = "sendEmbedMessage", aliases = {"sendEmbed", "messageEmbed", "msgEmbed", "dmEmbed", "embed"}, description = "Sends an embed message to a specified channel or user", structure = "sendEmbedMessage <id> <message>")
    public static void sendEmbedMessage(MessageReceivedEvent event)
    {
        sendMessage(event, true);
    }

    public static void sendMessage(MessageReceivedEvent event)
    {
        sendMessage(event, false);
    }

    @Command(name = "sendMessage", aliases = {"message", "msg", "dm"}, description = "Sends a unformatted message to a specified channel or user", structure = "sendMessage <id> <message>")
    public static void sendMessage(MessageReceivedEvent event, boolean embedMessage)
    {
        List<String> splitMessage = MessageService.formatMessageArguments(event.getMessage().getContentRaw(), 3);
        if (splitMessage.size() < 3)
        {
            LOGGER.debug("The command had too few parameters");
            String commandPrefix = CoreCommands.findGuildCommandPrefix(event.getGuild().getId());
            MessageService.sendErrorMessage(event.getChannel(), "The command has too few parameters! Use `" + commandPrefix + "help sendMessage` to learn more");
            return;
        }
        try
        {
            String channelId = splitMessage.get(1);
            if (channelId.startsWith("<#") && channelId.endsWith(">"))
            {
                channelId = channelId.substring(2, channelId.length() - 1);

            } else if (channelId.startsWith("<@!") && channelId.endsWith(">"))
            {
                channelId = channelId.substring(3, channelId.length() - 1);
            }

            MessageChannel channel = event.getJDA().getTextChannelById(channelId);
            String message = splitMessage.get(2);
            MessageEmbed embed = new EmbedBuilder()
                    .setDescription(splitMessage.get(2))
                    .setColor(MessageService.getEmbedColor())
                    .build();
            if (channel == null)
            {
                JDA jda = BobTheDiscordBot.getJda();

                jda.retrieveUserById(channelId).submit().whenComplete((user, error) -> {
                    if (error == null)
                    {
                        LOGGER.debug("User has been found, sending a DM");

                        boolean sentResult;
                        if (embedMessage)
                            sentResult = MessageService.sendPrivateMessage(user, embed);
                        else
                            sentResult = MessageService.sendPrivateMessage(user, message);

                        if (sentResult)
                        {
                            MessageService.sendEmbedMessage(event.getChannel(), "Message has been sent to " + user.getAsTag());
                        } else
                        {
                            LOGGER.debug("Failed to send a DM");
                            MessageService.sendErrorMessage(event.getChannel(), "Message has failed to send to " + splitMessage.get(1));
                        }
                    } else
                    {
                        LOGGER.debug("Failed to find a user");
                        MessageService.sendErrorMessage(event.getChannel(), "Unknown User " + splitMessage.get(1));
                    }
                });
                return;
            }
            if (embedMessage)
                MessageService.sendMessage(channel, embed);
            else
                MessageService.sendMessage(channel, message);
        } catch (NumberFormatException e)
        {
            LOGGER.info("Provided channel was invalid");
            MessageService.sendErrorMessage(event.getChannel(), "Provided channel was invalid!" +
                    "\nMake sure to provide a `channel id`/`user id`\nor\nmention a channel (<#" + event.getChannel().getId() + ">) / user (" + event.getAuthor().getAsMention() + ")");
            return;
        }
    }

}
