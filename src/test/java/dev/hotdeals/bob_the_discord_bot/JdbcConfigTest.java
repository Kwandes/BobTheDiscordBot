package dev.hotdeals.bob_the_discord_bot;

import dev.hotdeals.bob_the_discord_bot.config.Config;
import dev.hotdeals.bob_the_discord_bot.config.JdbcConfig;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JdbcConfigTest
{
    /*
    @Test
    @BeforeAll
    @Order(1)
    @DisplayName("Load Database Connection Properties")
    static void loadJdbcPropertiesTest() throws IOException
    {
        JdbcConfig.getInstance().loadProperties();
    }

    @Test
    @Order(2)
    @DisplayName("Get Jdbc Properties")
    void getJdbcPropertiesTest()
    {
        Properties properties = Config.getInstance().getProperties();
        assertFalse(properties.isEmpty(), "The properties file has failed to load");
    }
    */
}
