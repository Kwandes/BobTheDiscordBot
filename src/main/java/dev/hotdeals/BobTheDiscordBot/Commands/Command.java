/*
    An Annotation interface used for marking command methods and providing information
 */

package dev.hotdeals.BobTheDiscordBot.Commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command
{
    String name();

    String description();

    String structure();
}
