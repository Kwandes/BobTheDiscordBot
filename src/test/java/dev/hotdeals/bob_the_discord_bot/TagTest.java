package dev.hotdeals.bob_the_discord_bot;

import dev.hotdeals.bob_the_discord_bot.command.TagCommands;
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
public class TagTest
{

    private static Stream<Arguments> provideArguments()
    {
        return Stream.of(
                Arguments.of("!foo bar arg1 arg2", new String[]{"!foo", "bar", "arg1", "arg2"}),
                Arguments.of("!foo bar", new String[]{"!foo", "bar"}),
                Arguments.of("!foo", new String[]{"!foo"}),
                Arguments.of("", new String[]{})
        );
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    @DisplayName("convert Command Arguments")
    void convertCommandArgumentsTest(String message, String[] expected)
    {
        assertEquals(Arrays.toString(expected), Arrays.toString(TagCommands.convertCommandArguments(message)));
    }
}
