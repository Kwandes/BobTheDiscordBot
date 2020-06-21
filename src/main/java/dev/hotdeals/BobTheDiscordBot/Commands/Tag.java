/*
    Handles per-guild messages called 'tags'
 */

package dev.hotdeals.BobTheDiscordBot.Commands;

import dev.hotdeals.BobTheDiscordBot.Repository.TagRepo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;

public class Tag
{
    final static Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static void processTagCommand(MessageReceivedEvent event)
    {
        String commandPrefix = CoreCommands.getGuildPrefixes().get(event.getGuild().getId());
        if (commandPrefix == null) commandPrefix = CoreCommands.getDefaultCommandPrefix();

        Message message = event.getMessage();
        String[] splitMessage = message.getContentRaw().split(" ");

        if (splitMessage.length < 2)
        {
            logger.debug("The command had too few parameters");
            event.getChannel().sendMessage("The command has too few parameters! Use `" + commandPrefix + "help tag` to learn more")
                    .queue();
            return;
        }

        // combine the remainder, if it exists
        for (int i = 0; i < splitMessage.length; i++)
        {
            if (i > 3) splitMessage[3] += " " + splitMessage[i];
        }

        // convert the command and the tag name to lower case
        splitMessage[1] = splitMessage[1].toLowerCase();
        if (splitMessage.length > 2) splitMessage[2] = splitMessage[2].toLowerCase();

        switch (splitMessage[1])
        {
            case "create":
                if (splitMessage.length < 4)
                {
                    logger.debug("The command had too few parameters");
                    event.getChannel().sendMessage("The command has too few parameters! Use `" + commandPrefix + "help tag` to learn more")
                            .queue();
                    return;
                }
                // check for existing tags to avoid overlap
                if (TagRepo.fetchTagsForGuild(event.getGuild().getId()).containsKey(splitMessage[2]))
                {
                    logger.debug("A tag with the same name already exists.");
                    event.getChannel().sendMessage("A tag with the same name already exists!").queue();
                    return;
                }

                // validate input
                if (splitMessage[2].length() > 100)
                {
                    logger.debug("The provided tag name was too long. Max limit: 100");
                    event.getChannel().sendMessage("The provided tag name was too long! Max limit: `100`")
                            .queue();
                    return;
                } else if (splitMessage[3].length() > 2000)
                {
                    // technically impossible as a discord message can't be more than 2000 but if it changes in the future, it would break the query
                    logger.debug("The provided tag content was too long. Max limit: 2000");
                    event.getChannel().sendMessage("The provided tag content was too long! Max limit: `2000`")
                            .queue();
                    return;
                }

                if (TagRepo.createTag(event.getGuild().getId(), splitMessage[2], splitMessage[3]))
                {
                    logger.debug("Adding the new tag to the DB: " + splitMessage[2]);
                    event.getChannel().sendMessage("A new tag has been added: `" + splitMessage[2] + " - " + splitMessage[3] + "`").queue();
                } else
                {
                    logger.debug("Failed to add a tag: " + event.getGuild().getId() + "/" + splitMessage[2]);
                    event.getChannel().sendMessage("Failed to add a tag `" + splitMessage[2] + "`! Possible causes: issues with database connection").queue();
                }
                break;
            case "edit":
                if (splitMessage.length < 4)
                {
                    logger.debug("The command had too few parameters");
                    event.getChannel().sendMessage("The command has too few parameters! Use `" + commandPrefix + "help tag` to learn more")
                            .queue();
                    return;
                }

                // check if the tag exists
                if (!TagRepo.fetchTagsForGuild(event.getGuild().getId()).containsKey(splitMessage[2]))
                {
                    logger.debug("A tag with the name '" + splitMessage[2] + "' doesn't exists.");
                    event.getChannel().sendMessage("A tag with the name `" + splitMessage[2] + "` doesn't exists!").queue();
                    return;
                }

                // validate input
                if (splitMessage[2].length() > 100)
                {
                    logger.debug("The provided tag name was too long. Max limit: 100");
                    event.getChannel().sendMessage("The provided tag name was too long. Max limit: `100`")
                            .queue();
                } else if (splitMessage[3].length() > 2000)
                {
                    // technically impossible as a discord message can't be more than 2000 but if it changes in the future, it would break the query
                    logger.debug("The provided tag content was too long. Max limit: 2000");
                    event.getChannel().sendMessage("The provided tag content was too long. Max limit: `2000`")
                            .queue();
                }

                if (TagRepo.updateTag(event.getGuild().getId(), splitMessage[2], splitMessage[3]))
                {
                    logger.debug("Edited an existing tag: " + event.getGuild().getId() + "/" + splitMessage[2]);
                    event.getChannel().sendMessage(
                            "The tag has been edited: `" + splitMessage[2] + " - " + splitMessage[3] + "`").queue();
                } else
                {
                    logger.debug("Failed to edit a tag: " + event.getGuild().getId() + "/" + splitMessage[2]);
                    event.getChannel().sendMessage(
                            "Failed to edit a tag `" + splitMessage[2] + "`! Possible causes: issues with database connection").queue();
                }
                break;
            case "remove":
                if (splitMessage.length < 3)
                {
                    logger.debug("The command had too few parameters");
                    event.getChannel().sendMessage(
                            "The command has too few parameters! Use `" + commandPrefix + "help tag` to learn more").queue();
                    return;
                }

                // check if the tag exists
                if (!TagRepo.fetchTagsForGuild(event.getGuild().getId()).containsKey(splitMessage[2]))
                {
                    logger.debug("A tag with the name '" + splitMessage[2] + "' doesn't exists.");
                    event.getChannel().sendMessage("A tag with the name `" + splitMessage[2] + "` doesn't exists!").queue();
                    return;
                }

                // send a query
                if (TagRepo.deleteTag(event.getGuild().getId(), splitMessage[2]))
                {
                    logger.debug("Removed a tag: " + event.getGuild().getId() + "/" + splitMessage[2]);
                    event.getChannel().sendMessage("The tag `" + splitMessage[2] + "` has been removed").queue();
                } else
                {
                    logger.debug("Failed to remove a tag: " + event.getGuild().getId() + "/" + splitMessage[2]);
                    event.getChannel().sendMessage(
                            "Failed to remove a tag `" + splitMessage[2] + "`! Possible causes: issues with database connection").queue();
                }
                break;
            case "list":

                String tags = "";
                for (String tag : TagRepo.fetchTagsForGuild(event.getGuild().getId()).keySet())
                {
                    tags += "`" + tag + "`, ";
                }
                // remove the extra ', ' at the end of the list
                if (tags.length() > 0) tags = tags.substring(0, tags.length() - 2);

                event.getChannel().sendMessage("**Here's a list of tags:**\n" + tags).queue();
                break;
            default:
                HashMap<String, String> tagList = TagRepo.fetchTagsForGuild(event.getGuild().getId());

                if (tagList.isEmpty())
                {
                    logger.debug("The tag list is empty.");
                    event.getChannel().sendMessage("The tag list is empty! This might be due to invalid Database connection")
                            .queue();
                    return;
                }

                if (tagList.containsKey(splitMessage[1]))
                {
                    event.getChannel().sendMessage(TagRepo.fetchTagsForGuild(event.getGuild().getId()).get(splitMessage[1]))
                            .queue();
                } else
                {
                    logger.debug("A tag with the name '" + splitMessage[1] + "' doesn't exists.");
                    event.getChannel().sendMessage(
                            "A tag with the name `" + splitMessage[1] + "` doesn't exists!").queue();
                }
        }
    }
}
