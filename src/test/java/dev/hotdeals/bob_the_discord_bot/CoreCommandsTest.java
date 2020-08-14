package dev.hotdeals.bob_the_discord_bot;

import dev.hotdeals.bob_the_discord_bot.commands.Command;
import dev.hotdeals.bob_the_discord_bot.commands.CoreCommands;
import net.dv8tion.jda.api.entities.ChannelType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CoreCommandsTest
{
    private static Stream<Arguments> checkMessageTypeValidArguments()
    {
        return Stream.of(
                Arguments.of("!foo", "!", "123456", ChannelType.TEXT),
                Arguments.of("!foo arg1 arg2", "!", "123456", ChannelType.TEXT),
                Arguments.of("!", "!", "123456", ChannelType.TEXT),
                Arguments.of("<@!123456>", "!", "123456", ChannelType.TEXT)
        );
    }

    @ParameterizedTest
    @MethodSource("checkMessageTypeValidArguments")
    @DisplayName("Check Message Type - Valid version")
    void checkMessageTypeValidTest(String message, String commandPrefix, String botId, ChannelType channelType)
    {
        assertTrue(new CoreCommands().checkMessageType(message, commandPrefix, botId, channelType));
    }

    private static Stream<Arguments> checkMessageTypeInvalidArguments()
    {
        return Stream.of(
                Arguments.of("!foo", "!", "123456", ChannelType.GROUP),
                Arguments.of("!foo", "!", "123456", ChannelType.PRIVATE),
                Arguments.of("?foo", "!", "123456", ChannelType.TEXT),
                Arguments.of("?foo", "!", "123456", ChannelType.GROUP),
                Arguments.of("?foo", "!", "123456", ChannelType.PRIVATE),
                Arguments.of("foo", "!", "123456", ChannelType.TEXT),
                Arguments.of("foo", "!", "123456", ChannelType.GROUP),
                Arguments.of("foo", "!", "123456", ChannelType.PRIVATE),
                Arguments.of("!", "!", "123456", ChannelType.GROUP),
                Arguments.of("!", "!", "123456", ChannelType.PRIVATE),
                Arguments.of("", "!", "123456", ChannelType.TEXT),
                Arguments.of("", "!", "123456", ChannelType.GROUP),
                Arguments.of("", "!", "123456", ChannelType.PRIVATE),
                Arguments.of("<@!123456>", "!", "123456", ChannelType.GROUP),
                Arguments.of("<@!123456>", "!", "123456", ChannelType.PRIVATE),
                Arguments.of("<@!1>", "!", "123456", ChannelType.TEXT),
                Arguments.of("<@!1>", "!", "123456", ChannelType.GROUP),
                Arguments.of("<@!1>", "!", "123456", ChannelType.PRIVATE)
        );
    }

    @ParameterizedTest
    @MethodSource("checkMessageTypeInvalidArguments")
    @DisplayName("Check Message Type - Invalid version")
    void checkMessageTypeInvalidTest(String message, String commandPrefix, String botId, ChannelType channelType)
    {
        assertFalse(new CoreCommands().checkMessageType(message, commandPrefix, botId, channelType));
    }

    @Test
    @DisplayName("Find Guild Command Prefix")
    void findGuildCommandPrefixTest()
    {
        String guildId = "718584674850701415";
        String commandPrefix = "!";
        var guildPrefixes = new HashMap<String, String>();
        guildPrefixes.put(guildId, commandPrefix);
        CoreCommands.setDefaultCommandPrefix("?");
        CoreCommands.setGuildPrefixes(guildPrefixes);

        assertEquals(commandPrefix, CoreCommands.findGuildCommandPrefix(guildId));
        // if guildID is not found, the command prefix will be the default one
        assertEquals("?", CoreCommands.findGuildCommandPrefix(guildId + "fake"));
    }

    private static Stream<Arguments> getFirstArgumentArguments()
    {
        return Stream.of(
                Arguments.of("!foo", "foo", "123456"),
                Arguments.of("!foo arg1", "foo", "123456"),
                Arguments.of("!foo arg1 arg2", "foo", "123456"),
                Arguments.of("", "", "123456"),
                Arguments.of("<@!123456>", "status", "123456")
        );
    }

    @ParameterizedTest
    @MethodSource("getFirstArgumentArguments")
    @DisplayName("Get First Argument")
    void getFirstArgument(String message, String expectedArgument, String botId)
    {
        String commandPrefix = "!";
        assertEquals(expectedArgument, new CoreCommands().getFirstArgument(message, commandPrefix, botId));
    }

    private static Stream<Arguments> getCommandInformationArguments()
    {
        return Stream.of(
                Arguments.of("ping", "ping | [] | Displays latency between the client and the bot | ping"),
                Arguments.of("help", "help | [] | Displays information about a specific command | help <command>"),
                Arguments.of("reminder", "reminder | [remind, remindme] | Sends a reminder via a private message after the specified period | reminder <time period> <message>"),
                Arguments.of("remindme", "reminder | [remind, remindme] | Sends a reminder via a private message after the specified period | reminder <time period> <message>")
        );
    }

    @ParameterizedTest
    @MethodSource("getCommandInformationArguments")
    @DisplayName("Get Command Information")
    void getCommandInformation(String command, String information)
    {
        ArrayList<Command> commandList = new CoreCommands().getCommandInformation(command);
        assertTrue(commandList.size() > 0, "No command called `" + command + "´ has been found");
        assertEquals(commandList.get(0).name() + " | " + Arrays.toString(commandList.get(0).aliases()) + " | " +
                commandList.get(0).description() + " | " + commandList.get(0).structure(), information);
    }
}
