/*
    Basic command handling
    Only handles mapping of the commands
 */

package dev.hotdeals.BobTheDiscordBot.Commands;

import dev.hotdeals.BobTheDiscordBot.Repository.PrefixRepo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;

public class CoreCommands extends ListenerAdapter
{
    final static Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static String defaultCommandPrefix;
    private static String commandPrefix;
    private static HashMap<String, String> guildPrefixes = new HashMap<String, String>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return; // don't process messages from bots

        Message message = event.getMessage();

        // get guild-specific prefix
        commandPrefix = guildPrefixes.get(event.getGuild().getId());
        if (commandPrefix == null) commandPrefix = defaultCommandPrefix;

        if (!message.getContentRaw().startsWith(commandPrefix)) return;

        logger.debug(event.getGuild() + "/" + event.getChannel() + "/" + event.getAuthor() + " called a command `" + message.getContentRaw() + "`");
        if (message.getContentRaw().matches("\\A" + commandPrefix + "ping"))
        {
            long time = System.currentTimeMillis();
            event.getChannel().sendMessage("Pong!") /* => RestAction<Message> */
                    .queue(response /* => Message */ ->
                    {
                        response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                    });
        } else if (message.getContentRaw().startsWith(commandPrefix + "prefix"))
        {
            handlePrefix(event);
        }
    }

    //region command logic
    private void handlePrefix(MessageReceivedEvent event)
    {
        Message message = event.getMessage();

        String[] splitMessage = message.getContentRaw().split(" ");

        if (splitMessage.length == 1)
        {
            event.getChannel().sendMessage("The current Prefix is '" + commandPrefix + "'") /* => RestAction<Message> */
                    .queue();
            return;
        }

        if (splitMessage[1].length() > 3)
        {
            event.getChannel().sendMessage("New prefix is too long, max characters: 3, provided prefix: " + splitMessage[1].length())
                    .queue();
        } else
        {
            PrefixRepo.setPrefixForGuild(event.getGuild().getId(), splitMessage[1]);

            guildPrefixes = PrefixRepo.fetchPrefixes(); // refresh the list
            // check if the list of prefixes has been updated, aka if the query failed or not
            if (guildPrefixes.get(event.getGuild().getId()) == null || !guildPrefixes.get(event.getGuild().getId()).equals(splitMessage[1]))
            {
                logger.warn("Failed to set a new prefix due to DB connection issues");
                event.getChannel().sendMessage("Failed to set a new prefix due to DB connection issues")
                        .queue();
            } else
            {
                logger.info(event.getGuild() + " changed prefix from + " + commandPrefix + " to " + splitMessage[1]);
                commandPrefix = splitMessage[1];
                event.getChannel().sendMessage("Prefix has been set to " + commandPrefix)
                        .queue();
                guildPrefixes = PrefixRepo.fetchPrefixes(); // refresh the list

            }
        }
    }

    //endregion

    //region Getters and Setter
    public static String getDefaultCommandPrefix()
    {
        return defaultCommandPrefix;
    }

    public static void setDefaultCommandPrefix(String prefix)
    {
        defaultCommandPrefix = prefix;
    }

    public static HashMap<String, String> getGuildPrefixes()
    {
        return guildPrefixes;
    }

    public static void setGuildPrefixes(HashMap<String, String> guildPrefixes)
    {
        CoreCommands.guildPrefixes = guildPrefixes;
    }
    //endregion
}