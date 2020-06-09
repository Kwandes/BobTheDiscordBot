/*
    Basic command handling
    Only handles mapping of the commands
 */

package dev.hotdeals.BobTheDiscordBot.Commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CoreCommands extends ListenerAdapter
{
    private static String commandPrefix = "!";
    private final Logger logger = LogManager.getLogger(CoreCommands.class);

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return; // don't process messages from bots

        Message message = event.getMessage();

        if (message.getContentRaw().startsWith(commandPrefix))
        {
            if (message.getContentRaw().matches("\\A" + commandPrefix + "ping"))
            {
                MessageChannel channel = event.getChannel();    // the channel in which the message was sent
                User author = event.getAuthor();                // the user that sent the message
                logger.info(author + " called !ping in a channel " + channel);

                long time = System.currentTimeMillis();
                channel.sendMessage("Pong!") /* => RestAction<Message> */
                        .queue(response /* => Message */ ->
                        {
                            response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                        });
            }
        }
    }

    public static String getCommandPrefix()
    {
        return commandPrefix;
    }

    public static void setCommandPrefix(String prefix)
    {
        commandPrefix = prefix;
    }
}