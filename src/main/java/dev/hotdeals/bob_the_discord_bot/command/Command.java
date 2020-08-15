/*
    An Annotation interface used for marking command methods and providing information
 */

package dev.hotdeals.bob_the_discord_bot.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command
{
    String name();

    String[] aliases();

    String description();

    String structure();
}
