/*
    Basic command handling
    Only handles mapping of the commands
 */

package dev.hotdeals.bob_the_discord_bot.command;

import dev.hotdeals.bob_the_discord_bot.Service.MessageService;
import dev.hotdeals.bob_the_discord_bot.repository.StatusRepo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

public class CoreCommands extends ListenerAdapter
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static String defaultCommandPrefix;
    private static HashMap<String, String> guildPrefixes = new HashMap<>();

    // receive new message events and call corresponding methods
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;
        if (!validateMessage(event)) return;
        String commandPrefix = findGuildCommandPrefix(event.getGuild().getId());

        LOGGER.debug(event.getGuild() + "/" + event.getChannel() + "/" + event.getAuthor() +
                " called a command `" + event.getMessage().getContentRaw() + "`");

        String command = getFirstArgument(event.getMessage().getContentRaw(), commandPrefix, event.getJDA().getSelfUser().getId());
        List<String> splitMessage;
        switch (command)
        {
            case "tag":
            case "t":
                TagCommands.processTagCommand(event.getMessage().getContentRaw(), commandPrefix, event.getGuild().getId(), event.getChannel());
                break;
            case "ping":
                sendPing(event);
                break;
            case "prefix":
                if (RankCommand.isAdministrator(event))
                    AdministrationCommands.handlePrefix(event);
                break;
            case "status":
                sendStatusMessage(event, commandPrefix);
                break;
            case "help":
                splitMessage = MessageService.formatMessageArguments(event.getMessage().getContentRaw(), 2);
                if (splitMessage.size() == 1)
                {
                    sendEmptyHelpMessage(event, commandPrefix);
                } else if (splitMessage.size() >= 2)
                {
                    sendHelpMessage(event, splitMessage.get(1), commandPrefix);
                }
                break;
            case "remindme":
            case "remind":
            case "reminder":
                ReminderCommand.processReminderCommand(event);
                break;
            case "message":
            case "msg":
            case "dm":
            case "sendmessage":
                if (RankCommand.isAdministrator(event))
                    MessageCommand.sendMessage(event);
                break;
            case "sendembed":
            case "messageembed":
            case "msgembed":
            case "dmembed":
            case "embed":
            case "sendembedmessage":
                if (RankCommand.isAdministrator(event))
                    MessageCommand.sendEmbedMessage(event);
                break;
            case "reload":
            case "reset":
            case "restart":
                if (RankCommand.isDeveloper(event))
                    AdministrationCommands.restartBot(event.getChannel());
                break;
            case "throw":
            case "throwlogs":
                if (RankCommand.isDeveloper(event))
                    AdministrationCommands.throwLogs(event.getChannel());
                break;
            case "cr":
            case "changerank":
                if (RankCommand.isAdministrator(event))
                    RankCommand.changeRank(event);
                break;
            case "debug":
                if (RankCommand.isDeveloper(event))
                    DebugCommand.debugProgram(event.getChannel());
                break;
            case "":
                break;
            default:
                StringBuilder newString = new StringBuilder(event.getMessage().getContentRaw());
                newString.insert(1, "tag ");
                TagCommands.processTagCommand(newString.toString(), commandPrefix, event.getGuild().getId(), event.getChannel());
                break;
            // do nothing, such command doesn't exist / is invalid
        }
    }

    //region Message processing

    /**
     * Checks for message type and the test itself to validate whether or not to continue processing this event
     *
     * @param event the MessageReceived event to be validated
     * @return whether or not to continue processing this event
     */
    public boolean validateMessage(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return false;

        String guildId = "";
        if (event.getChannelType() == ChannelType.TEXT) guildId = event.getGuild().getId();

        return checkMessageType(event.getMessage().getContentRaw(), findGuildCommandPrefix(guildId), event.getJDA().getSelfUser().getId(), event.getChannelType());
    }

    public boolean checkMessageType(String message, String commandPrefix, String botId, ChannelType channelType)
    {
        if (channelType != ChannelType.TEXT) return false;
        return (message.startsWith(commandPrefix) || message.equals("<@!" + botId + ">"));
    }

    public static String findGuildCommandPrefix(String guildID)
    {
        String prefix = guildPrefixes.get(guildID);
        if (prefix == null) prefix = defaultCommandPrefix;
        return prefix;
    }


    public String getFirstArgument(String message, String commandPrefix, String botId)
    {
        String firstArg = message.split(" ")[0];
        String command = "";
        if (firstArg.startsWith(commandPrefix))
            command = firstArg.substring(commandPrefix.length());
        // if the message simply mentions the bot, set the command to trigger !status
        if (firstArg.equals("<@!" + botId + ">"))
            command = "status";

        return command.toLowerCase();
    }
    //endregion

    //region command logic

    @Command(name = "ping", aliases = {}, description = "Displays latency between the client and the bot", structure = "ping")
    private void sendPing(MessageReceivedEvent event)
    {
        long time = System.currentTimeMillis();
        event.getChannel().sendMessage("Pong!") /* => RestAction<Message> */
                .queue(response /* => Message */ ->
                        response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue());
    }

    // send an embed containing information regarding the bot
    @Command(name = "status", aliases = {}, description = "Displays information about the bot", structure = "status")
    private void sendStatusMessage(MessageReceivedEvent event, String commandPrefix)
    {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(MessageService.getEmbedColor()); // set color to purple
        embed.setAuthor(event.getJDA().getSelfUser().getAsTag(), "https://github.com/Kwandes/BobTheDiscordBot");
        embed.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl());
        embed.setDescription("A Discord bot made in Java for learning purposes");
        embed.addField("Prefix", commandPrefix, true);

        long dbResponseTime = System.currentTimeMillis(); // used for calculating latency
        String dbLatency = "N/A";
        if (StatusRepo.getConnectionStatus())
        {
            dbLatency = System.currentTimeMillis() - dbResponseTime + "ms";
        }
        long responseTime = System.currentTimeMillis();
        embed.addField("Ping", "Heartbeat: " + event.getJDA().getGatewayPing() + "ms" +
                "\n Response Time: " + (System.currentTimeMillis() - responseTime) + "ms" +
                "\n Database Connection: " + dbLatency, true);

        // get current Uptime and add it to the embed
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime(); // VM uptime, in milliseconds
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        embed.addField("Uptime", days + "d:" + hours % 24 + "h:" + minutes % 60 + "m:" + seconds % 60 + "s", true);

        // set various project information
        String buildVersion = "N/A";
        String jdaVersion = "N/A";
        String javaVersion = "N/A";

        // get project information from the maven pom.xml file
        Model model = MessageService.getPomModel();
        if (model != null)
        {
            buildVersion = model.getVersion();
            jdaVersion = model.getDependencies().get(0).getVersion();
            javaVersion = model.getProperties().getProperty("maven.compiler.source");
        } else
            LOGGER.warn("Failed to read the pom file");

        embed.addField("Build Info", "```fix\nVersion: " + buildVersion + "\nJDA: " + jdaVersion + "\nJava: " + javaVersion + "```", false);
        embed.addField("Source", "[github.com/Kwandes](https://github.com/Kwandes/BobTheDiscordBot)", false);
        embed.setFooter("use " + commandPrefix + "help for more information");

        String finalDbLatency = dbLatency; // variables used in a lambda expression have to be effectively final
        event.getChannel().sendMessage(embed.build()).queue(response /* => Message */ ->
        {
            // update the embed with response time
            embed.getFields().set(1, new MessageEmbed.Field("Ping", "Heartbeat: " + event.getJDA().getGatewayPing() + "ms" +
                    "\n Response Time: " + (System.currentTimeMillis() - responseTime) + "ms" +
                    "\n Database Connection: " + finalDbLatency, true));
            response.editMessage(embed.build()).queue();
        });
    }

    //region help command
    private void sendEmptyHelpMessage(MessageReceivedEvent event, String commandPrefix)
    {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(MessageService.getEmbedColor()); // set color to purple
        embed.setTitle("Here are all the available commands:");
        embed.setFooter("Use " + commandPrefix + "help <command> for more information");
        List<List<Command>> commandList = getCommands();
        StringBuilder commandNames = new StringBuilder("**Commands:**");
        for (List<Command> commandType : commandList)
        {
            for (Command command : commandType)
            {
                // skip sub-commands, like `tag create`
                if (!command.name().contains(" "))
                {
                    commandNames.append(" `").append(command.name()).append("`,");
                }
            }
        }
        commandNames.deleteCharAt(commandNames.length() - 1);
        embed.setDescription(commandNames.toString());
        MessageService.sendMessage(event.getChannel(), embed.build());
    }

    @Command(name = "help", aliases = {}, description = "Displays information about a specific command", structure = "help <command>")
    private void sendHelpMessage(MessageReceivedEvent event, String commandName, String commandPrefix)
    {
        EmbedBuilder embed = new EmbedBuilder();

        List<Command> commandList = getCommandInformation(commandName);
        if (commandList.size() != 0)
        {
            embed.setColor(MessageService.getEmbedColor()); // set color to purple
            embed.setDescription("Here is some commands related to \"" + commandName + "\":");
            for (Command command : commandList)
            {
                String aliases = "";
                if (command.aliases().length != 0)
                {
                    aliases = Arrays.toString(command.aliases());
                    aliases = "**Aliases:** " + Arrays.toString(command.aliases()).substring(1, aliases.length() - 1) + "\n";
                }
                embed.addField(commandPrefix + command.structure(), aliases + command.description(), false);
            }
            MessageService.sendMessage(event.getChannel(), embed.build());
        } else
        {
            MessageService.sendErrorMessage(event.getChannel(), "This command does not seem to exist");
        }
    }

    public List<Command> getCommandInformation(String commandName)
    {
        List<Command> matchingCommands = new ArrayList<>();
        for (List<Command> commandList : getCommands())
        {
            for (Command command : commandList)
                try
                {
                    if (command.name().toLowerCase().contains(commandName.toLowerCase()) ||
                            Arrays.asList(command.aliases()).contains(commandName))
                    {
                        matchingCommands.add(command);
                    }
                } catch (NullPointerException e)
                {
                    // very unlikely unless a human error happens in the validation code
                    // critical if it does get through
                    LOGGER.error("An invalid method has been looked up during Command Annotation search", e);
                }
        }
        return matchingCommands;
    }


    public List<List<Command>> getCommands()
    {
        // need to manually add classes with commands
        Class<? extends CoreCommands> coreCommands = CoreCommands.class;
        Class<? extends TagCommands> tagCommands = TagCommands.class;
        Class<? extends AdministrationCommands> adminCommands = AdministrationCommands.class;
        Class<? extends ReminderCommand> reminderCommand = ReminderCommand.class;
        Class<? extends RankCommand> rankCommand = RankCommand.class;
        Class<? extends MessageCommand> messageCommand = MessageCommand.class;
        Class<? extends DebugCommand> debugCommand = DebugCommand.class;

        List<List<Method>> methods = new ArrayList<>();

        methods.add(Arrays.asList(coreCommands.getDeclaredMethods()));
        methods.add(Arrays.asList(tagCommands.getDeclaredMethods()));
        methods.add(Arrays.asList(adminCommands.getDeclaredMethods()));
        methods.add(Arrays.asList(reminderCommand.getDeclaredMethods()));
        methods.add(Arrays.asList(rankCommand.getDeclaredMethods()));
        methods.add(Arrays.asList(messageCommand.getDeclaredMethods()));
        methods.add(Arrays.asList(debugCommand.getDeclaredMethods()));

        List<List<Command>> commands = new ArrayList<>();
        for (List<Method> methodList : methods)
        {
            List<Command> commandList = new ArrayList<>();
            for (Method method : methodList)
            {
                if (method.isAnnotationPresent(Command.class))
                {
                    commandList.add(method.getAnnotation(Command.class));
                }
            }
            commands.addAll(Collections.singleton(commandList));
        }
        return commands;
    }
    //endregion

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