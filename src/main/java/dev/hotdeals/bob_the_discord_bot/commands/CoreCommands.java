/*
    Basic command handling
    Only handles mapping of the commands
 */

package dev.hotdeals.bob_the_discord_bot.commands;

import dev.hotdeals.bob_the_discord_bot.repository.StatusRepo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CoreCommands extends ListenerAdapter
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static String defaultCommandPrefix;
    private static String commandPrefix;
    private static HashMap<String, String> guildPrefixes = new HashMap<>();

    // receive new message events and call corresponding methods
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;

        commandPrefix = findGuildCommandPrefix(event.getGuild().getId());
        if (validateMessage(event)) return;

        LOGGER.debug(event.getGuild() + "/" + event.getChannel() + "/" + event.getAuthor() +
                " called a command `" + event.getMessage().getContentRaw() + "`");

        String command = getFirstArgument(event.getMessage().getContentRaw(), event.getJDA().getSelfUser().getId());

        switch (command)
        {
            case "tag":
            case "t":
                TagCommands.processTagCommand(event);
                break;
            case "ping":
                sendPing(event);
                break;
            case "prefix":
                AdministrationCommands.handlePrefix(event);
                break;
            case "status":
                sendStatusMessage(event);
                break;
            case "help":
                String[] splitMessage = event.getMessage().getContentRaw().toLowerCase().split(" ");
                if (splitMessage.length == 1)
                {
                    sendEmptyHelpMessage(event);
                } else if (splitMessage.length >= 2)
                {
                    sendHelpMessage(event, splitMessage[1]);
                }
                break;
            case "":
            default:
                // do nothing, such command doesn't exist / is invalid
        }
    }

    //region Message processing
    private boolean validateMessage(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return false;
        Message message = event.getMessage();

        return (checkMessageType(message.getContentRaw(), event.getJDA().getSelfUser().getId(), message.getChannelType()));
    }

    public static String findGuildCommandPrefix(String guildID)
    {
        String prefix = guildPrefixes.get(guildID);
        if (prefix == null) prefix = defaultCommandPrefix;
        return prefix;
    }

    private boolean checkMessageType(String message, String botId, ChannelType channelType)
    {
        if (channelType != ChannelType.TEXT) return false;
        return (!(message.startsWith(commandPrefix) || message.equals("<@!" + botId + ">")));
    }

    private String getFirstArgument(String message, String botId)
    {
        String firstArg = message.toLowerCase().split(" ")[0];
        String command = "";

        if (firstArg.startsWith(commandPrefix))
            command = firstArg.substring(commandPrefix.length());
        // if the message simply mentions the bot, set the command to trigger !status
        if (firstArg.equals("<@!" + botId + ">"))
            command = "status";

        return command;
    }
    //endregion

    //region command logic

    @Command(name = "ping", description = "Displays latency between the client and the bot", structure = "ping")
    private void sendPing(MessageReceivedEvent event)
    {
        long time = System.currentTimeMillis();
        event.getChannel().sendMessage("Pong!") /* => RestAction<Message> */
                .queue(response /* => Message */ ->
                {
                    response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                });
    }

    // send an embed containing information regarding the bot
    @Command(name = "status", description = "Displays information about the bot", structure = "status")
    private void sendStatusMessage(MessageReceivedEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(new Color(0x6A2396)); // set color to purple
        embed.setAuthor(event.getJDA().getSelfUser().getAsTag(), "https://github.com/Kwandes/BobTheDiscordBot");
        embed.setThumbnail(event.getJDA().getSelfUser().getAvatarUrl());
        embed.setDescription("A Discord bot made in Java for learning purposes");
        embed.addField("Prefix", commandPrefix, true);

        long dbResponseTime = System.currentTimeMillis(); // used for calculating latency
        String dbLatency  = "N/A";
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
        try (FileReader fr = new FileReader("pom.xml"))
        {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(fr);
            buildVersion = model.getVersion();
            jdaVersion = model.getDependencies().get(0).getVersion();
            javaVersion = model.getProperties().getProperty("maven.compiler.source");
        } catch (IOException | XmlPullParserException e)
        {
            LOGGER.error("An error occurred during parsing of the pom.xml data", e);
        }

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
    private void sendEmptyHelpMessage(MessageReceivedEvent event)
    {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setColor(new Color(0x6A2396)); // set color to purple
        embed.setAuthor("Help Menu");
        embed.setDescription("Use `" + commandPrefix + "help <command>` for more information");

        event.getChannel().sendMessage(embed.build()).queue();
    }

    @Command(name = "help", description = "Displays information about a specific command", structure = "help <command>")
    private void sendHelpMessage(MessageReceivedEvent event, String commandName)
    {
        EmbedBuilder embed = new EmbedBuilder();

        ArrayList<Command> commandList = getCommandInformation(commandName);
        if (commandList.size() != 0)
        {
            embed.setColor(new Color(0x6A2396)); // set color to purple
            embed.setDescription("Here is some commands related to \"" + commandName + "\":");
            for (Command command : commandList)
            {
                embed.addField(commandPrefix + command.structure(), command.description(), false);
            }
        } else
        {
            embed.setAuthor("Yikes...");
            embed.setColor(new Color(0xff0000)); // set color to red, for an error
            embed.setDescription("This command does not seem to exist");
        }

        event.getChannel().sendMessage(embed.build()).queue();
    }

    public ArrayList<Command> getCommandInformation(String commandName)
    {
        // need to manually add classes with commands
        ArrayList<Method> methods = new ArrayList<>();

        Class<? extends CoreCommands> coreCommands = CoreCommands.class;
        Class<? extends TagCommands> tagCommands = TagCommands.class;
        Class<? extends AdministrationCommands> adminCommands = AdministrationCommands.class;

        methods.addAll(Arrays.asList(coreCommands.getDeclaredMethods()));
        methods.addAll(Arrays.asList(tagCommands.getDeclaredMethods()));
        methods.addAll(Arrays.asList(adminCommands.getDeclaredMethods()));

        // iterate through the methods and filter for matching commands
        ArrayList<Command> matchingCommands = new ArrayList<>();
        for (Method method : methods)
        {
            if (!method.isAnnotationPresent(Command.class))
                continue; // skip methods that are not annotated as a @Command
            try
            {
                if (method.getAnnotation(Command.class).name().contains(commandName))
                {
                    matchingCommands.add(method.getAnnotation(Command.class));
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