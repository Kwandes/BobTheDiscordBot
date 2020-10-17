package dev.hotdeals.bob_the_discord_bot.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;

public class GithubBotListener extends ListenerAdapter
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        // Discard events outside of the specified channel and not from a bot
        if (!event.getChannel().getId().equals("749332729341935776") ||
                !event.getAuthor().isBot() ||
                event.getMessage().getEmbeds().size() == 0)
        {
            return;
        }

        try
        {
            if (event.getMessage().getEmbeds().get(0).getTitle().contains("success"))
            {
                LOGGER.info("Removing a CI success message");
                event.getMessage().delete().queue();
            }
        } catch (IndexOutOfBoundsException | NullPointerException e)
        {
            LOGGER.warn("Shit is wrong boi", e);
        }
    }

}
