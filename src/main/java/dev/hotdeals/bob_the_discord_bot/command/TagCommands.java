/*
    Handles per-guild messages called 'tags'
    Tags consist of a name and a message.
    This class handles retrieval, creation, edition and removal of tags as well as sending them to the guild
 */

package dev.hotdeals.bob_the_discord_bot.command;

import dev.hotdeals.bob_the_discord_bot.Service.MessageService;
import dev.hotdeals.bob_the_discord_bot.repository.TagRepo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;

public class TagCommands
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    public static void processTagCommand(MessageReceivedEvent event)
    {
        String commandPrefix = CoreCommands.findGuildCommandPrefix(event.getGuild().getId());

        List<String> splitMessage = MessageService.formatMessageArguments(event.getMessage().getContentRaw(), 4);

        if (splitMessage.size() < 2)
        {
            LOGGER.debug("The command had too few parameters");
            MessageService.sendErrorMessage(event.getChannel(), "The command has too few parameters! Use `" + commandPrefix + "help tag` to learn more");
            return;
        }

        // process the tag command depending on the behaviour type (display, create, edit, remove, list)
        switch (splitMessage.get(1))
        {
            case "create":
                tagCreate(event, splitMessage, commandPrefix);
                break;
            case "edit":
                tagEdit(event, splitMessage, commandPrefix);
                break;
            case "rename":
                tagRename(event, commandPrefix);
                break;
            case "delete":
            case "remove":
                tagRemove(event, splitMessage, commandPrefix);
                break;
            case "help":
            case "list":
                tagList(event);
                break;
            default:
                tagDisplay(event, splitMessage);
        }
    }

    @Command(name = "tag create", aliases = {"t create"}, description = "Creates a tag", structure = "tag create <trigger> <response>")
    private static void tagCreate(MessageReceivedEvent event, List<String> splitMessage, String commandPrefix)
    {
        if (splitMessage.size() < 4)
        {
            LOGGER.debug("The command had too few parameters");
            MessageService.sendErrorMessage(event.getChannel(), "The command has too few parameters! Use `" + commandPrefix + "help tag create` to learn more");
            return;
        }
        // check for existing tags to avoid overlap
        if (TagRepo.fetchTagsForGuild(event.getGuild().getId()).containsKey(splitMessage.get(2)))
        {
            LOGGER.debug("A tag with the same name already exists.");
            MessageService.sendErrorMessage(event.getChannel(), "A tag with the same name already exists!");
            return;
        }

        // validate input
        if (splitMessage.get(2).length() > 100)
        {
            LOGGER.debug("The provided tag name was too long. Max limit: 100");
            MessageService.sendErrorMessage(event.getChannel(), "The provided tag name was too long! Max limit: `100`");
            return;
        } else if (splitMessage.get(3).length() > 2000)
        {
            // technically impossible as a discord message can't be more than 2000 but if it changes in the future, it would break the query
            LOGGER.debug("The provided tag content was too long. Max limit: 2000");
            MessageService.sendErrorMessage(event.getChannel(), "The provided tag content was too long! Max limit: `2000`");
            return;
        }

        if (TagRepo.createTag(event.getGuild().getId(), splitMessage.get(2), splitMessage.get(3)))
        {
            LOGGER.debug("Adding the new tag to the DB: " + splitMessage.get(2));
            MessageService.sendEmbedMessage(event.getChannel(), "A new tag has been added: `" + splitMessage.get(2) + " - " + splitMessage.get(3) + "`");
        } else
        {
            LOGGER.debug("Failed to add a tag: " + event.getGuild().getId() + "/" + splitMessage.get(2));
            MessageService.sendErrorMessage(event.getChannel(), "Failed to add a tag `" + splitMessage.get(2) + "`! Possible causes: issues with database connection");
        }
    }

    @Command(name = "tag edit", aliases = {"t edit"}, description = "Edit a tag", structure = "tag edit <trigger> <response>")
    private static void tagEdit(MessageReceivedEvent event, List<String> splitMessage, String commandPrefix)
    {
        if (splitMessage.size() < 4)
        {
            LOGGER.debug("The command had too few parameters");
            MessageService.sendErrorMessage(event.getChannel(), "The command has too few parameters! Use `" + commandPrefix + "help tag edit` to learn more");
            return;
        }

        // check if the tag exists
        if (!TagRepo.fetchTagsForGuild(event.getGuild().getId()).containsKey(splitMessage.get(2)))
        {
            LOGGER.debug("A tag with the name '" + splitMessage.get(2) + "' doesn't exist.");
            MessageService.sendErrorMessage(event.getChannel(), "A tag with the name `" + splitMessage.get(2) + "` doesn't exist!");
            return;
        }

        // validate input
        if (splitMessage.get(2).length() > 100)
        {
            LOGGER.debug("The provided tag name was too long. Max limit: 100");
            MessageService.sendErrorMessage(event.getChannel(), "The provided tag name was too long. Max limit: `100`");
        } else if (splitMessage.get(3).length() > 2000)
        {
            // technically impossible as a discord message can't be more than 2000 but if it changes in the future, it would break the query
            LOGGER.debug("The provided tag content was too long. Max limit: 2000");
            MessageService.sendErrorMessage(event.getChannel(), "The provided tag content was too long. Max limit: `2000`");
        }

        if (TagRepo.updateTag(event.getGuild().getId(), splitMessage.get(2), splitMessage.get(3)))
        {
            LOGGER.debug("Edited an existing tag: " + event.getGuild().getId() + "/" + splitMessage.get(2));
            MessageService.sendEmbedMessage(event.getChannel(), "The tag has been edited: `" + splitMessage.get(2) + " - " + splitMessage.get(3) + "`");
        } else
        {
            LOGGER.debug("Failed to edit a tag: " + event.getGuild().getId() + "/" + splitMessage.get(2));
            MessageService.sendErrorMessage(event.getChannel(), "Failed to edit a tag `" + splitMessage.get(2) + "`! Possible causes: issues with database connection");
        }
    }

    @Command(name = "tag rename", aliases = {"t rename"}, description = "Renames a tag while preserving its contents", structure = "tag rename <trigger> <newTrigger>")
    private static void tagRename(MessageReceivedEvent event, String commandPrefix)
    {
        // passed split message gas only four arguments, with the fourth being combined remainder
        // this command requires the last element to be a single word
        List<String> splitMessage = MessageService.formatMessageArguments(event.getMessage().getContentRaw(), 5);

        if (splitMessage.size() < 4)
        {
            LOGGER.debug("The command had too few parameters");
            MessageService.sendErrorMessage(event.getChannel(), "The command has too few parameters! Use `" + commandPrefix + "help tag rename` to learn more");
            return;
        }

        // check if the tag exists
        if (!TagRepo.fetchTagsForGuild(event.getGuild().getId()).containsKey(splitMessage.get(2)))
        {
            LOGGER.debug("A tag with the name '" + splitMessage.get(2) + "' doesn't exist.");
            MessageService.sendErrorMessage(event.getChannel(), "A tag with the name `" + splitMessage.get(2) + "` doesn't exist!");
            return;
        }

        // validate input
        if (splitMessage.get(2).length() > 100 || splitMessage.get(3).length() > 100)
        {
            LOGGER.debug("The provided tag name was too long. Max limit: 100");
            MessageService.sendErrorMessage(event.getChannel(), "The provided tag name was too long. Max limit: `100`");
        } else if (splitMessage.get(3).length() > 2000)
        {
            // technically impossible as a discord message can't be more than 2000 but if it changes in the future, it would break the query
            LOGGER.debug("The provided tag content was too long. Max limit: 2000");
            MessageService.sendErrorMessage(event.getChannel(), "The provided tag content was too long. Max limit: `2000`");
        }

        if (TagRepo.renameTag(event.getGuild().getId(), splitMessage.get(2), splitMessage.get(3)))
        {
            LOGGER.debug("Renamed an existing tag: " + event.getGuild().getId() + "/" + splitMessage.get(2) + " to " + splitMessage.get(3));
            MessageService.sendEmbedMessage(event.getChannel(), "The tag has been renamed from `" + splitMessage.get(2) + "` to `" + splitMessage.get(3) + "`");
        } else
        {
            LOGGER.debug("Failed to rename a tag: " + event.getGuild().getId() + "/" + splitMessage.get(2));
            MessageService.sendErrorMessage(event.getChannel(), "Failed to rename a tag `" + splitMessage.get(2) + "`! Possible causes: issues with database connection");
        }
    }

    @Command(name = "tag remove", aliases = {"t remove", "tag delete", "t delete"}, description = "Remove a tag", structure = "tag remove <trigger>")
    private static void tagRemove(MessageReceivedEvent event, List<String> splitMessage, String commandPrefix)
    {
        if (splitMessage.size() < 3)
        {
            LOGGER.debug("The command had too few parameters");
            MessageService.sendErrorMessage(event.getChannel(), "The command has too few parameters! Use `" + commandPrefix + "help tag remove` to learn more");
            return;
        }

        // check if the tag exists
        if (!TagRepo.fetchTagsForGuild(event.getGuild().getId()).containsKey(splitMessage.get(2)))
        {
            LOGGER.debug("A tag with the name '" + splitMessage.get(2) + "' doesn't exists.");
            MessageService.sendErrorMessage(event.getChannel(), "A tag with the name `" + splitMessage.get(2) + "` doesn't exists!");
            return;
        }

        // send a query
        if (TagRepo.deleteTag(event.getGuild().getId(), splitMessage.get(2)))
        {
            LOGGER.debug("Removed a tag: " + event.getGuild().getId() + "/" + splitMessage.get(2));
            MessageService.sendEmbedMessage(event.getChannel(), "The tag `" + splitMessage.get(2) + "` has been removed");
        } else
        {
            LOGGER.debug("Failed to remove a tag: " + event.getGuild().getId() + "/" + splitMessage.get(2));
            MessageService.sendErrorMessage(event.getChannel(), "Failed to remove a tag `" + splitMessage.get(2) + "`! Possible causes: issues with database connection");
        }
    }

    @Command(name = "tag list", aliases = {"t list"}, description = "Displays a list of existing tags", structure = "tag list")
    private static void tagList(MessageReceivedEvent event)
    {
        StringBuilder tags = new StringBuilder();
        for (String tag : TagRepo.fetchTagsForGuild(event.getGuild().getId()).keySet())
        {
            tags.append("`").append(tag).append("`, ");
        }
        // remove the extra ', ' at the end of the list
        if (tags.length() > 0) tags = new StringBuilder(tags.substring(0, tags.length() - 2));

        MessageService.sendEmbedMessage(event.getChannel(), "**Here's a list of tags:**\n" + tags);
    }

    @Command(name = "tag", aliases = {"t"}, description = "Displays a tag", structure = "tag <trigger>")
    public static void tagDisplay(MessageReceivedEvent event, List<String> splitMessage)
    {
        HashMap<String, String> tagList = TagRepo.fetchTagsForGuild(event.getGuild().getId());

        if (tagList.isEmpty())
        {
            LOGGER.debug("The tag list is empty.");
            MessageService.sendErrorMessage(event.getChannel(), "The tag list is empty! This might be due to invalid Database connection");
            return;
        }

        if (tagList.containsKey(splitMessage.get(1)))
        {
            event.getChannel().sendMessage(TagRepo.fetchTagsForGuild(event.getGuild().getId()).get(splitMessage.get(1)))
                    .queue();
        } else
        {
            LOGGER.debug("A tag with the name '" + splitMessage.get(1) + "' doesn't exists.");
            MessageService.sendErrorMessage(event.getChannel(), "A tag with the name `" + splitMessage.get(1) + "` doesn't exists!");
        }
    }

    public static String[] convertCommandArguments(String message)
    {
        String[] splitMessage = message.split(" ");

        if (splitMessage.length > 1) splitMessage[1] = splitMessage[1].toLowerCase();
        if (splitMessage.length > 2) splitMessage[2] = splitMessage[2].toLowerCase();

        // combine the remainder, if it exists
        for (int i = 0; i < splitMessage.length; i++)
        {
            if (i > 3) splitMessage[3] += " " + splitMessage[i];
        }

        return splitMessage;
    }
}
