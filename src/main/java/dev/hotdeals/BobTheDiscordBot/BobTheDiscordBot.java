/*
    Main class for the project
 */

package dev.hotdeals.BobTheDiscordBot;

import dev.hotdeals.BobTheDiscordBot.Commands.CoreCommands;
import dev.hotdeals.BobTheDiscordBot.Config.Config;
import dev.hotdeals.BobTheDiscordBot.Config.JdbcConfig;
import dev.hotdeals.BobTheDiscordBot.Repository.PrefixRepo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;

public class BobTheDiscordBot
{
    final static Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
    private static String botToken;
    private static String displayedAcitivity;

    @SuppressWarnings("deprecation") // JDABuilder is deprecated
    public static void main(String[] args) throws Exception
    {
        // setup the config
        try
        {
            Config.loadProperties();
        } catch (Exception e)
        {
            logger.fatal("Failed to initialize the application config. CLosing the application", e);
            System.exit(40); // 40 - failed to initialize the config
        }

        try
        {
            JdbcConfig.loadProperties();
            logger.debug("Connection to the database has been configured");
        } catch (SQLException e)
        {
            logger.error("Failed to establish connection to the database: " + e);
        }

        // Actual discord
        try
        {
            botToken = Config.getProperties().getProperty("botToken");

            if (botToken == null)
            {
                logger.fatal("Failed to retrieve the Bot Token from the config. Closing the application");
                System.exit(41); // 41 - config property is null
            }

            displayedAcitivity = Config.getProperties().getProperty("displayedActivity");
            if (displayedAcitivity == null)
            {
                logger.warn("Failed to retrieve the 'displayedActivity' property from the config. Setting it to 'Ping Pong'");
                displayedAcitivity = "Ping Pong";
            }

            // set a default command prefix
            CoreCommands.setDefaultCommandPrefix(Config.getProperties().getProperty("commandPrefix"));
            if (CoreCommands.getDefaultCommandPrefix() == null)
            {
                logger.warn("Failed to retrieve the 'commandPrefix' property from the config. Setting it to '!'");
                CoreCommands.setDefaultCommandPrefix("!");
            }
            CoreCommands.setGuildPrefixes(PrefixRepo.fetchPrefixes());
            logger.debug("Command prefixes have been loaded");

            JDA jda = new JDABuilder(botToken)
                    .setActivity(Activity.playing(displayedAcitivity))
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