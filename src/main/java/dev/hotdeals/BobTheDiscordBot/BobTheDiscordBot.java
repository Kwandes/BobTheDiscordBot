/*
    Main class for the project
 */

package dev.hotdeals.BobTheDiscordBot;

import dev.hotdeals.BobTheDiscordBot.Commands.CoreCommands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;

public class BobTheDiscordBot
{
    private static final Logger logger = LogManager.getLogger(BobTheDiscordBot.class);

    @SuppressWarnings("deprecation") // JDABuilder is deprecated
    public static void main(String[] args)
    {
        // Actual discord
        try
        {
            JDA jda = new JDABuilder("botToken")
                    .setActivity(Activity.playing("PingPong"))
                    .build();
            jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.
            logger.info("JDA has finished loading and has successfully logged in");

            jda.addEventListener(new CoreCommands());

        } catch (LoginException e)
        {
            //If anything goes wrong in terms of authentication, this is the exception that will represent it
            logger.fatal("Failed to log in", e);

        } catch (InterruptedException e)
        {
            //Due to the fact that awaitReady is a blocking method, one which waits until JDA is fully loaded,
            // the waiting can be interrupted. This is the exception that would fire in that situation.
            //As a note: in this extremely simplified example this will never occur. In fact, this will never occur unless
            // you use awaitReady in a thread that has the possibility of being interrupted (async thread usage and interrupts)
            logger.warn("Program runtime was interrupted", e);
        }
    }
}