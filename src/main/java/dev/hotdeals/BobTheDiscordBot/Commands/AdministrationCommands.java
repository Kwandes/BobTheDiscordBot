package dev.hotdeals.BobTheDiscordBot.Commands;

import dev.hotdeals.BobTheDiscordBot.Repository.PrefixRepo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;

public class AdministrationCommands
{
    final static Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static void handlePrefix(MessageReceivedEvent event)
    {
        Message message = event.getMessage();

        String commandPrefix = CoreCommands.getGuildPrefixes().get(event.getGuild().getId());
        if (commandPrefix == null) commandPrefix = CoreCommands.getDefaultCommandPrefix();

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

            CoreCommands.setGuildPrefixes(PrefixRepo.fetchPrefixes()); // refresh the list
            // check if the list of prefixes has been updated, aka if the query failed or not
            if (CoreCommands.getGuildPrefixes().get(event.getGuild().getId()) == null || !CoreCommands.getGuildPrefixes().get(event.getGuild().getId()).equals(splitMessage[1]))
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
                CoreCommands.setGuildPrefixes(PrefixRepo.fetchPrefixes()); // refresh the list
            }
        }
    }
}
