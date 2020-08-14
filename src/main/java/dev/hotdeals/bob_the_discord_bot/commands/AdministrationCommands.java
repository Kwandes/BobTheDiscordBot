package dev.hotdeals.bob_the_discord_bot.commands;

import dev.hotdeals.bob_the_discord_bot.repository.PrefixRepo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

public class AdministrationCommands
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Command(name = "prefix", aliases = {}, description = "Displays current bot prefix", structure = "prefix")
    public static void handlePrefix(MessageReceivedEvent event)
    {
        String commandPrefix = CoreCommands.findGuildCommandPrefix(event.getGuild().getId());
        String[] splitMessage = event.getMessage().getContentRaw().split(" ");

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
            changePrefix(event, splitMessage, commandPrefix);
        }
    }

    @Command(name = "prefix change", aliases = {}, description = "Changes bot prefix to a new one", structure = "prefix <newPrefix>")
    private static void changePrefix(MessageReceivedEvent event, String[] splitMessage, String commandPrefix)
    {
        PrefixRepo.setPrefixForGuild(event.getGuild().getId(), splitMessage[1]);

        CoreCommands.setGuildPrefixes(PrefixRepo.fetchPrefixes()); // refresh the list
        // check if the list of prefixes has been updated, aka if the query failed or not
        if (CoreCommands.getGuildPrefixes().get(event.getGuild().getId()) == null ||
                !CoreCommands.getGuildPrefixes().get(event.getGuild().getId()).equals(splitMessage[1]))
        {
            LOGGER.warn("Failed to set a new prefix due to DB connection issues");
            event.getChannel().sendMessage("Failed to set a new prefix due to DB connection issues")
                    .queue();
        } else
        {
            LOGGER.info(event.getGuild() + " changed prefix from + " + commandPrefix + " to " + splitMessage[1]);
            commandPrefix = splitMessage[1];
            event.getChannel().sendMessage("Prefix has been set to " + commandPrefix)
                    .queue();
            CoreCommands.setGuildPrefixes(PrefixRepo.fetchPrefixes()); // refresh the list
        }
    }
}
