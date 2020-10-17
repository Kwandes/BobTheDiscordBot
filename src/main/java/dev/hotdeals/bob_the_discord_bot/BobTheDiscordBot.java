/*
    Main class for the project
 */

package dev.hotdeals.bob_the_discord_bot;

import dev.hotdeals.bob_the_discord_bot.Service.MessageService;
import dev.hotdeals.bob_the_discord_bot.command.CoreCommands;
import dev.hotdeals.bob_the_discord_bot.command.ReminderCommand;
import dev.hotdeals.bob_the_discord_bot.config.Config;
import dev.hotdeals.bob_the_discord_bot.config.JdbcConfig;
import dev.hotdeals.bob_the_discord_bot.listener.GithubBotListener;
import dev.hotdeals.bob_the_discord_bot.repository.PrefixRepo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class BobTheDiscordBot
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    private static Config config = null;
    private static JdbcConfig jdbcConfig = null;
    private static JDA jda = null;

    public static void main(String[] args)
    {
        LOGGER.info("Initializing the bot startup procedure");
        try
        {
            runBot();
        } catch (Exception e)
        {
            LOGGER.error("An unexpected exception occurred during bot runtime", e);
        }

        startBotCheckTimer();
    }

    public static void runBot()
    {
        setupConfig();

        try
        {
            String botToken = config.getProperties().getProperty("botToken");

            if (botToken == null)
            {
                LOGGER.fatal("Failed to retrieve the Bot Token from the config. Closing the application");
                System.exit(41); // 41 - config property is null
            }

            String displayedActivity = config.getProperties().getProperty("activity");
            if (displayedActivity == null)
            {
                LOGGER.warn("Failed to retrieve the 'displayedActivity' property from the config. Setting it to 'Database Connection Failed'");
                displayedActivity = "Database Connection Failed";
            }

            CoreCommands.setDefaultCommandPrefix(config.getProperties().getProperty("defaultCommandPrefix"));
            if (CoreCommands.getDefaultCommandPrefix() == null)
            {
                LOGGER.warn("Failed to retrieve the 'defaultCommandPrefix' property from the config. Setting it to '!'");
                CoreCommands.setDefaultCommandPrefix("!");
            }

            CoreCommands.setGuildPrefixes(PrefixRepo.fetchPrefixes());
            if (CoreCommands.getGuildPrefixes() == null)
            {
                LOGGER.warn("Failed to retrieve the guild command prefixes! All guilds will be using the default prefix");
            } else
            {
                LOGGER.debug("Command prefixes have been loaded");
            }

            ReminderCommand.checkReminders();

            jda = JDABuilder.createDefault(botToken)
                    .setActivity(Activity.playing(displayedActivity))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build();
            jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.
            LOGGER.info("JDA has finished loading and has successfully logged in");

            jda.addEventListener(new CoreCommands());
            jda.addEventListener(new GithubBotListener());

            MessageService.sendBootMessage();
        } catch (LoginException e)
        {
            //If anything goes wrong in terms of authentication, this is the exception that will represent it
            LOGGER.fatal("Failed to log in", e);

        } catch (InterruptedException e)
        {
            //Due to the fact that awaitReady is a blocking method, one which waits until JDA is fully loaded,
            // the waiting can be interrupted. This is the exception that would fire in that situation.
            //As a note: in this extremely simplified example this will never occur. In fact, this will never occur unless
            // you use awaitReady in a thread that has the possibility of being interrupted (async thread usage and interrupts)
            LOGGER.warn("Program runtime was interrupted", e);
        }
    }

    private static void setupConfig()
    {
        jdbcConfig = JdbcConfig.getInstance();
        try
        {
            jdbcConfig.loadProperties();
            LOGGER.debug("Connection to the database has been configured");
        } catch (IOException e)
        {
            LOGGER.error("Database connection properties have failed to load: " + e);
        }

        config = Config.getInstance();
        try
        {
            config.loadProperties();
        } catch (Exception e)
        {
            LOGGER.fatal("Failed to initialize the application config. CLosing the application", e);
            System.exit(40); // 40 - failed to initialize the config
        }
    }

    /**
     * Checks the JDA status and re-runs it, if needed
     */
    private static void startBotCheckTimer()
    {
        TimerTask checkBotStatus = new TimerTask()
        {
            @Override
            public void run()
            {
                if (jda == null)
                {
                    LOGGER.warn("The JDA was null, restarting the bot");
                    LOGGER.info("Collecting thread dump data");
                    StringBuilder threadDump = new StringBuilder(System.lineSeparator());
                    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                    for (ThreadInfo threadInfo : threadMXBean.dumpAllThreads(true, true))
                    {
                        threadDump.append(threadInfo.toString());
                    }
                    LOGGER.info(threadDump);
                    LOGGER.info("Shutting down the bot");
                    jda.shutdownNow();
                    LOGGER.info("Starting the bot after the JDA becoming null");
                    runBot();
                    return;
                }

                Status jdaStatus = jda.getStatus();
                switch (jdaStatus)
                {
                    case CONNECTED:
                        break;
                    case DISCONNECTED:
                        LOGGER.warn("The JDA is currently in a disconnected state, restarting");
                        jda.shutdown();
                        runBot();
                        break;
                    case SHUTDOWN:
                        LOGGER.warn("The JDA is currently in a shutdown state, restarting");
                        jda.shutdownNow();
                        runBot();
                        break;
                    default:
                        LOGGER.info("Current JDA status: " + jdaStatus);
                        break;
                }
            }
        };

        new Timer("checkBotStatus").schedule(checkBotStatus, TimeUnit.SECONDS.toMillis(60), TimeUnit.MINUTES.toMillis(1));
    }

    public static Config getConfig()
    {
        return config;
    }

    public static JdbcConfig getJdbcConfig()
    {
        return jdbcConfig;
    }

    public static JDA getJda()
    {
        return jda;
    }

}