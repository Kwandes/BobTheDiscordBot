package dev.hotdeals.bob_the_discord_bot;

import dev.hotdeals.bob_the_discord_bot.Service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageTest
{
    private static Stream<Arguments> formatMessageArguments()
    {
        return Stream.of(
                Arguments.of("!foo", 0, new String[]{"!foo"}),
                Arguments.of("!foo", 1, new String[]{"!foo"}),
                Arguments.of("!foo", 99, new String[]{"!foo"}),
                Arguments.of("!foo arg1", 1, new String[]{"!foo arg1"}),
                Arguments.of("!foo arg1", 2, new String[]{"!foo", "arg1"}),
                Arguments.of("!foo arg1", -1, new String[]{"!foo arg1"}),
                Arguments.of("!foo arg1 arg2", 1, new String[]{"!foo arg1 arg2"}),
                Arguments.of("!foo arg1 arg2", 2, new String[]{"!foo", "arg1 arg2"}),
                Arguments.of("!foo arg1 arg2 arg3", 2, new String[]{"!foo", "arg1 arg2 arg3"}),
                Arguments.of("!tag create test Message Contents stuff", 4, new String[]{"!tag", "create", "test", "Message Contents stuff"}),
                Arguments.of("", 0, new String[]{""}),
                Arguments.of("", 1, new String[]{""}),
                Arguments.of("", -1, new String[]{""}),
                Arguments.of("!Foo arg1 Arg2", 2, new String[]{"!foo", "arg1 Arg2"}),
                Arguments.of("!Foo arg1 arg2", 1, new String[]{"!foo arg1 arg2"}),
                Arguments.of("!foo \narg1 arg2 \narg3", 3, new String[]{"!foo", "arg1", "arg2 \narg3"}),
                Arguments.of("!foo\narg1 arg2\narg3", 3, new String[]{"!foo", "arg1", "arg2\narg3"}),
                Arguments.of("!FOO ARG1 ARG2\nARG3 arg4 arg5 arg6", 2, new String[]{"!foo", "ARG1 ARG2\nARG3 arg4 arg5 arg6"})
        );
    }

    private static Stream<Arguments> stripMentionSymbolsArguments()
    {
        return Stream.of(
                Arguments.of("232921997514899457", "232921997514899457"),
                Arguments.of("<@!232921997514899457>", "232921997514899457"),
                Arguments.of("718584675576315956", "718584675576315956"),
                Arguments.of("<#718584675576315956>", "718584675576315956"),
                Arguments.of("<@!1>", "1"),
                Arguments.of("<#1>", "1"),
                Arguments.of("", "")
        );
    }

    @ParameterizedTest
    @MethodSource("formatMessageArguments")
    @DisplayName("Format Message Arguments")
    void formatMessageArgumentsTest(String message, int argCount, String[] expectedArray)
    {
        assertEquals(Arrays.toString(expectedArray), MessageService.formatMessageArguments(message, argCount).toString());
    }

    @ParameterizedTest
    @MethodSource("stripMentionSymbolsArguments")
    @DisplayName("Strip mention symbols")
    void stripMentionSymbolsTest(String id, String expectedId)
    {
        assertEquals(expectedId, MessageService.stripMentionSymbols(id));
    }
}
