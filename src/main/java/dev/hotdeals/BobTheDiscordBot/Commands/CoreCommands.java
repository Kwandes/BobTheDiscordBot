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
import java.util.HashMap;

public class CoreCommands extends ListenerAdapter
{
    final static Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static String defaultCommandPrefix;
    private static String commandPrefix;
    private static HashMap<String, String> guildPrefixes = new HashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return; // don't process messages from bots
        if (!event.isFromType(ChannelType.TEXT)) return; // don't process messages that are not sent in a text channel (No DMs etc)

        Message message = event.getMessage();

        // get guild-specific prefix
        commandPrefix = guildPrefixes.get(event.getGuild().getId());
        if (commandPrefix == null) commandPrefix = defaultCommandPrefix;

        // don't process messages that don't start with the prefix or don't mention the bot directly
        if (!(message.getContentRaw().startsWith(commandPrefix) || message.getContentRaw().equals("<@!" + event.getJDA().getSelfUser().getId() + ">"))) return;

        logger.debug(event.getGuild() + "/" + event.getChannel() + "/" + event.getAuthor() + " called a command `" + message.getContentRaw() + "`");
        if (message.getContentRaw().startsWith(commandPrefix + "tag") || message.getContentRaw().startsWith(commandPrefix + "t"))
        {
            TagCommands.processTagCommand(event);
        } else if (message.getContentRaw().matches("\\A" + commandPrefix + "ping"))
        {
            long time = System.currentTimeMillis();
            event.getChannel().sendMessage("Pong!") /* => RestAction<Message> */
                    .queue(response /* => Message */ ->
                    {
                        response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                    });
        } else if (message.getContentRaw().startsWith(commandPrefix + "prefix"))
        {
            AdministrationCommands.handlePrefix(event);
        } else if (message.getContentRaw().equals("<@!" + event.getJDA().getSelfUser().getId() + ">") ||
                message.getContentRaw().matches("\\A" + commandPrefix + "help") ||
                message.getContentRaw().matches("\\A" + commandPrefix + "status"))
        {
            sendStatusMessage(event);
        }
    }

    //region command logic

    // send an embed containing information regarding the bot
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
                "\n Response Time: " + (System.currentTimeMillis() - responseTime) + "ms" , true);

        // get current Uptime and add it to the embed
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime(); // VM uptime, in milliseconds
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        embed.addField("Uptime", days + "d:" + hours % 24 + "h:" + minutes % 60 + "m:" + seconds % 60 + "s", true);

        // set various project information
        String buildVersion = "0.3.0";
        String jdaVersion = "4.1.1_159";
        String javaVersion = "11";
        embed.addField("Build Info", "```fix\nVersion: " + buildVersion + "\nJDA: " + jdaVersion + "\nJava: " + javaVersion + "```", true);
        embed.addField("Source", "[github.com/Kwandes](https://github.com/Kwandes/BobTheDiscordBot)", false);

        event.getChannel().sendMessage(embed.build()).queue(response /* => Message */ ->
        {
            // update the embed with response time
            embed.getFields().set(1,new MessageEmbed.Field("Ping", "Heartbeat: " + event.getJDA().getGatewayPing() + "ms" +
                    "\n Response Time: " + (System.currentTimeMillis() - responseTime) + "ms" , true));
            response.editMessage(embed.build()).queue();
        });
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