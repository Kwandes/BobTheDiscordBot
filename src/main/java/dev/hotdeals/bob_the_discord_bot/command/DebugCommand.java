package dev.hotdeals.bob_the_discord_bot.command;

import dev.hotdeals.bob_the_discord_bot.Service.MessageService;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;

public class DebugCommand
{
    private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

    @Command(name = "debug", aliases = {}, description = "Performs multiple actions like thread dump etc to help troubleshoot", structure = "debug")
    public static void debugProgram(MessageChannel channel)
    {
        StringBuilder threadDump = new StringBuilder(System.lineSeparator());
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for (ThreadInfo threadInfo : threadMXBean.dumpAllThreads(true, true))
        {
            threadDump.append(threadInfo.toString());
        }
        LOGGER.debug(threadDump.toString());

        try
        {
            InputStream dumpFile = new ByteArrayInputStream(threadDump.toString().getBytes(StandardCharsets.UTF_8));
            MessageService.sendFileMessage(channel, dumpFile, "threadDump.txt");

            InputStream programLog = new FileInputStream(new File("logs/program.log"));
            MessageService.sendFileMessage(channel, programLog, "program.log");
        } catch (FileNotFoundException e)
        {
            LOGGER.error("Failed to find the program.log file", e);
        } catch (IOException e)
        {
            LOGGER.error("Failed to access the program.log file", e);
        }
    }
}
