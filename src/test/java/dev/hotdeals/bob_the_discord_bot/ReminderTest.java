package dev.hotdeals.bob_the_discord_bot;

import dev.hotdeals.bob_the_discord_bot.command.ReminderCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReminderTest
{
    private static Stream<Arguments> convertTextToSecondsArguments()
    {
        return Stream.of(
                Arguments.of("1s", "1"),
                Arguments.of("60s", "60"),
                Arguments.of("1m", "60"),
                Arguments.of("1h", "3600"),
                Arguments.of("1d", "86400"),
                Arguments.of("1y", "31536000"),
                Arguments.of("1d1m1s", "86461"),
                Arguments.of("0", "0"),
                Arguments.of("s", "0"),
                Arguments.of("asdasd", "0"),
                Arguments.of("1ss", "1"),
                Arguments.of("1mm", "60"),
                Arguments.of("1dd", "86400"),
                Arguments.of("1ms", "60"),
                Arguments.of("1m60s", "120"),
                Arguments.of("1m60", "120"),
                Arguments.of("1 m60", "120"),
                Arguments.of("1 m 60", "120"),
                Arguments.of("1 minute 60 seconds", "120"),
                Arguments.of("", "0"),
                Arguments.of("!", "0"),
                Arguments.of("-1", "1"),
                Arguments.of("-1m", "60"),
                Arguments.of("10y", "315360000"),
                Arguments.of("10y1s", "0")
        );
    }

    @ParameterizedTest
    @MethodSource("convertTextToSecondsArguments")
    @DisplayName("Convert Text To Seconds")
    void convertTextToSecondsTest(String time, String expectedSeconds)
    {
        assertEquals(expectedSeconds, Long.toString(ReminderCommand.convertTextToSeconds(time)));
    }

    private static Stream<Arguments> convertSecondsToTimePeriodArguments()
    {
        return Stream.of(
                Arguments.of("1", "1 second"),
                Arguments.of("2", "2 seconds"),
                Arguments.of("59", "59 seconds"),
                Arguments.of("60", "1 minute"),
                Arguments.of("61", "1 minute"),
                Arguments.of("119", "1 minute"),
                Arguments.of("120", "2 minutes"),
                Arguments.of("3599", "59 minutes"),
                Arguments.of("3600", "1 hour"),
                Arguments.of("3601", "1 hour"),
                Arguments.of("7200", "2 hours"),
                Arguments.of("7201", "2 hours"),
                Arguments.of("86399", "23 hours"),
                Arguments.of("86400", "1 day"),
                Arguments.of("86401", "1 day"),
                Arguments.of("172799", "1 day"),
                Arguments.of("172800", "2 days"),
                Arguments.of("31535999", "364 days"),
                Arguments.of("31536000", "1 year"),
                Arguments.of("63071999", "1 year"),
                Arguments.of("63072000", "2 years"),
                Arguments.of("63072001", "2 years")
        );
    }

    @ParameterizedTest
    @MethodSource("convertSecondsToTimePeriodArguments")
    @DisplayName("Convert Seconds To Time Period")
    void convertSecondsToTimePeriodTest(long seconds, String expectedString)
    {
        assertEquals(expectedString, ReminderCommand.convertSecondsToTimePeriod(seconds));
    }

}
