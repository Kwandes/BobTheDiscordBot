package dev.hotdeals.bob_the_discord_bot.command;

import dev.hotdeals.bob_the_discord_bot.Service.MessageService;
import dev.hotdeals.bob_the_discord_bot.repository.PrefixRepo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class AdministrationCommands
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Command(name = "prefix", aliases = {}, description = "Displays current bot prefix", structure = "prefix")
    public static void handlePrefix(MessageReceivedEvent event)
    {
        String commandPrefix = CoreCommands.findGuildCommandPrefix(event.getGuild().getId());
        List<String> splitMessage = MessageService.formatMessageArguments(event.getMessage().getContentRaw(), 3);

        if (splitMessage.size() == 1)
        {
            MessageService.sendEmbedMessage(event.getChannel(), "The current Prefix is '" + commandPrefix + "'");
            return;
        }

        if (splitMessage.get(1).length() > 3)
        {
            MessageService.sendErrorMessage(event.getChannel(), "New prefix is too long, max characters: 3, provided prefix: " + splitMessage.get(1).length());
        } else
        {
            changePrefix(event, splitMessage.get(1), commandPrefix);
        }
    }

    @Command(name = "prefix change", aliases = {}, description = "Changes bot prefix to a new one", structure = "prefix <newPrefix>")
    private static void changePrefix(MessageReceivedEvent event, String newPrefix, String commandPrefix)
    {
        PrefixRepo.setPrefixForGuild(event.getGuild().getId(), newPrefix);

        CoreCommands.setGuildPrefixes(PrefixRepo.fetchPrefixes()); // refresh the list
        // check if the list of prefixes has been updated, aka if the query failed or not
        if (CoreCommands.getGuildPrefixes().get(event.getGuild().getId()) == null ||
                !CoreCommands.getGuildPrefixes().get(event.getGuild().getId()).equals(newPrefix))
        {
            LOGGER.warn("Failed to set a new prefix due to DB connection issues");
            MessageService.sendErrorMessage(event.getChannel(), "Failed to set a new prefix due to DB connection issues");
        } else
        {
            LOGGER.info(event.getGuild() + " changed prefix from + " + commandPrefix + " to " + newPrefix);
            commandPrefix = newPrefix;
            MessageService.sendEmbedMessage(event.getChannel(), "Prefix has been set to " + commandPrefix);
            CoreCommands.setGuildPrefixes(PrefixRepo.fetchPrefixes()); // refresh the list
        }
    }
}
