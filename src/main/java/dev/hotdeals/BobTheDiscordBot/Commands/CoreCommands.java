/*
    Basic command handling
    Only handles mapping of the commands
 */

package dev.hotdeals.BobTheDiscordBot.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CoreCommands extends ListenerAdapter
{
    final static Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static String defaultCommandPrefix;
    private static String commandPrefix;
    private static HashMap<String, String> guildPrefixes = new HashMap<>();

    // receive new message events and call corresponding methods
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return; // don't process messages from bots
        if (!event.isFromType(ChannelType.TEXT))
            return; // don't process messages that are not sent in a text channel (No DMs etc)

        Message message = event.getMessage();

        // get guild-specific prefix
        commandPrefix = guildPrefixes.get(event.getGuild().getId());
        if (commandPrefix == null) commandPrefix = defaultCommandPrefix;

        // don't process messages that don't start with the prefix or don't mention the bot directly
        if (!(message.getContentRaw().startsWith(commandPrefix) || message.getContentRaw().equals("<@!" + event.getJDA().getSelfUser().getId() + ">")))
            return;

        logger.debug(event.getGuild() + "/" + event.getChannel() + "/" + event.getAuthor() + " called a command `" + message.getContentRaw() + "`");
        if (message.getContentRaw().startsWith(commandPrefix + "tag") || message.getContentRaw().startsWith(commandPrefix + "t"))
        {
            TagCommands.processTagCommand(event);
        } else if (message.getContentRaw().startsWith(commandPrefix + "ping"))
        {
            sendPing(event);
        } else if (message.getContentRaw().startsWith(commandPrefix + "prefix"))
        {
            AdministrationCommands.handlePrefix(event);
        } else if (message.getContentRaw().equals("<@!" + event.getJDA().getSelfUser().getId() + ">") ||
                message.getContentRaw().startsWith(commandPrefix + "status"))
        {
            sendStatusMessage(event);
        } else if (message.getContentRaw().startsWith(commandPrefix + "help"))
        {

            String[] splitMessage = message.getContentRaw().split(" ");
            if (splitMessage.length == 1)
            {
                sendEmptyHelpMessage(event);
            } else if (splitMessage.length >= 2)
            {
                sendHelpMessage(event, splitMessage[1]);
            }
        }
    }

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

        long responseTime = System.currentTimeMillis(); // used for calculating latency
        embed.addField("Ping", "Heartbeat: " + event.getJDA().getGatewayPing() + "ms" +
                "\n Response Time: " + (System.currentTimeMillis() - responseTime) + "ms", true);

        // get current Uptime and add it to the embed
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime(); // VM uptime, in milliseconds
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        embed.addField("Uptime", days + "d:" + hours % 24 + "h:" + minutes % 60 + "m:" + seconds % 60 + "s", true);

        // set various project information
        String buildVersion = "0.4.1";
        String jdaVersion = "4.1.1_159";
        String javaVersion = "11";
        embed.addField("Build Info", "```fix\nVersion: " + buildVersion + "\nJDA: " + jdaVersion + "\nJava: " + javaVersion + "```", false);
        embed.addField("Source", "[github.com/Kwandes](https://github.com/Kwandes/BobTheDiscordBot)", false);
        embed.setFooter("use " + commandPrefix + "help for more information");

        event.getChannel().sendMessage(embed.build()).queue(response /* => Message */ ->
        {
            // update the embed with response time
            embed.getFields().set(1, new MessageEmbed.Field("Ping", "Heartbeat: " + event.getJDA().getGatewayPing() + "ms" +
                    "\n Response Time: " + (System.currentTimeMillis() - responseTime) + "ms", true));
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
                logger.error("An invalid method has been looked up during Command Annotation search", e);
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