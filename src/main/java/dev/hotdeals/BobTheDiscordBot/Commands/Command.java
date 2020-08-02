/*
    An Annotation interface used for marking command methods and providing information
 */

package dev.hotdeals.BobTheDiscordBot.Commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command
{
    String name();

    String description();

    String structure();
}
