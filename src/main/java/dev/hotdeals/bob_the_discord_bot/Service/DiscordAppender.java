package dev.hotdeals.bob_the_discord_bot.Service;

import dev.hotdeals.bob_the_discord_bot.BobTheDiscordBot;
import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

/**
 * Custom appender for logging to a specified Discord monitoring channel
 */
@Plugin(
        name = "DiscordAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE)
public class DiscordAppender extends AbstractAppender
{
    protected DiscordAppender(String name, Filter filter)
    {
        super(name, filter, null);
    }

    @PluginFactory
    public static DiscordAppender createAppender(@PluginAttribute("name") String name, @PluginElement("Filter") Filter filer)
    {
        return new DiscordAppender(name, filer);
    }

    @Override
    public void append(LogEvent logEvent)
    {
        if (logEvent.getLevel().isMoreSpecificThan(Level.WARN))
        {
            if (BobTheDiscordBot.getJda() == null || !BobTheDiscordBot.getJda().getStatus().equals(JDA.Status.CONNECTED))
            {
                return;
            }
            MessageService.sendLogMessage(logEvent);
        }
    }
}
