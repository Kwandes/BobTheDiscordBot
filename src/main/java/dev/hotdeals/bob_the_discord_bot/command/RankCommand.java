package dev.hotdeals.bob_the_discord_bot.command;

import dev.hotdeals.bob_the_discord_bot.Service.MessageService;
import dev.hotdeals.bob_the_discord_bot.repository.RankRepo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class RankCommand
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Command(name = "changeRank", aliases = {"cr"}, description = "Changes the bot usage rank of a given user. Ranks: `User`, `Administrator`, `Developer`", structure = "changeRank <userID> <rank>")
    public static void changeRank(MessageReceivedEvent event)
    {
        List<String> splitMessage = MessageService.formatMessageArguments(event.getMessage().getContentRaw(), 4);

        if (splitMessage.size() == 2)
        {
            String userRank = "User";
            String userId = MessageService.stripMentionSymbols(splitMessage.get(1));
            if (RankRepo.isAdmin(userId, event.getGuild().getId())) userRank = "Administrator";
            if (RankRepo.isDeveloper(userId)) userRank = "Developer";
            LOGGER.info("User " + userId + " is a " + userRank);
            MessageService.sendEmbedMessage(event.getChannel(), "User " + userId + " is a " + userRank);
            return;
        }

        if (splitMessage.size() < 3)
        {
            LOGGER.debug("The command had too few parameters");
            MessageService.sendErrorMessage(event.getChannel(), "The command has too few parameters! Use `" +
                    CoreCommands.findGuildCommandPrefix(event.getGuild().getId()) + "help changeRank` to learn more");
            return;
        }

        String userId = MessageService.stripMentionSymbols(splitMessage.get(1));

        if (!userId.matches("^\\d{1,64}$"))
        {
            LOGGER.info("Provided user ID is invalid: " + userId);
            MessageService.sendErrorMessage(event.getChannel(), "Provided user ID is invalid");
            return;
        }

        switch (splitMessage.get(2))
        {
            case "administrator":
                if (RankRepo.isAdmin(userId, event.getGuild().getId()))
                {
                    LOGGER.info("User is already marked as an Administrator!");
                    MessageService.sendErrorMessage(event.getChannel(), "User is already marked as an Administrator!");
                    return;
                }
                LOGGER.info(userId + " has been set as an Administrator");
                RankRepo.addAdmin(userId, event.getGuild().getId());
                MessageService.sendEmbedMessage(event.getChannel(), userId + " has been set as an Administrator");
                break;
            case "developer":
                if (!isDeveloper(event))
                    return;

                if (RankRepo.isDeveloper(userId))
                {
                    LOGGER.info("User is already marked as a Developer!");
                    MessageService.sendErrorMessage(event.getChannel(), "User is already marked as a Developer!");
                    return;
                }
                LOGGER.info(userId + " has been set as a Developer");
                RankRepo.addDeveloper(userId);
                MessageService.sendEmbedMessage(event.getChannel(), userId + " has been set as a Developer");
                break;
            case "user":
                if (RankRepo.isDeveloper(userId))
                {
                    if (!RankRepo.isDeveloper(event.getAuthor().getId()))
                    {
                        LOGGER.info("Message Author is is lower rank than Developer, their rank has not been removed");
                        MessageService.sendErrorMessage(event.getChannel(), "You are lower rank than the person you're trying to modify, their rank has not been removed");
                    } else
                    {
                        RankRepo.removeDeveloper(userId);
                    }
                }
                RankRepo.removeAdministrator(userId, event.getGuild().getId());
                MessageService.sendEmbedMessage(event.getChannel(), userId + " has been set as a User");
                LOGGER.info(userId + " has been set as a User");
                break;
            default:
            {
                LOGGER.info("Provided rank was invalid. User rank has not been changed");
                MessageService.sendErrorMessage(event.getChannel(), "Provided rank was invalid. Valid ranks:, `User`, `Administrator`, `Developer`");
            }
        }

    }

    public static boolean isAdministrator(MessageReceivedEvent event)
    {
        Member member = event.getMember();
        if (member.getPermissions().contains(Permission.ADMINISTRATOR) ||
                RankRepo.isAdmin(event.getGuild().getId(), event.getAuthor().getId()) ||
                RankRepo.isDeveloper(event.getAuthor().getId()))
        {
            return true;
        }
        LOGGER.debug("User does not have admin perms");
        MessageService.sendErrorMessage(event.getChannel(), "You don't have permissions to use this command. You need to be an Administrator");
        return false;
    }

    public static boolean isDeveloper(MessageReceivedEvent event)
    {
        String userId = event.getAuthor().getId();
        if (!RankRepo.isDeveloper(userId))
        {
            LOGGER.debug("User does not have developer perms");
            MessageService.sendErrorMessage(event.getChannel(), "You don't have permissions to use this command. You need to be a bot Developer");
            return false;
        }
        return true;
    }
}
