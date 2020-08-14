#-------------------------------------------
-- Schema: discord
-- Project: https://github.com/Kwandes/BobTheDiscordBot
-- DB version: MySQL 5.7.26
-- Description: Creation scripts for all the triggers. The triggers log all of the table changes to discord.log
#-------------------------------------------

-- -----------------------------------------------------
-- Table discord.tag
-- -----------------------------------------------------
CREATE TRIGGER tr_discord_tag_ins
    AFTER INSERT
    ON discord.tag
    FOR EACH ROW
    INSERT INTO discord.log(user_id, action, table_name, log_time, data)
    VALUES (USER(),
            'insert',
            'tag',
            CURRENT_TIME(6),
            CONCAT(NEW.guild_id, ' | ', NEW.tag_name, ' | ', NEW.tag_content));

CREATE TRIGGER tr_discord_tag_upd
    BEFORE UPDATE
    ON discord.tag
    FOR EACH ROW
BEGIN
    INSERT INTO discord.log(user_id, action, table_name, log_time, data)
    VALUES (USER(),
            'update',
            'tag',
            CURRENT_TIME(6),
            CONCAT(NEW.guild_id, ' | ', NEW.tag_name, ' | ', NEW.tag_content));
END;

CREATE TRIGGER tr_discord_tag_del
    BEFORE DELETE
    ON discord.tag
    FOR EACH ROW
BEGIN
    INSERT INTO discord.log(user_id, action, table_name, log_time, data)
    VALUES (USER(),
            'delete',
            'tag',
            CURRENT_TIME(6),
            CONCAT(OLD.guild_id, ' | ', OLD.tag_name, ' | ', OLD.tag_content));
END;

-- -----------------------------------------------------
-- Table discord.prefix
-- -----------------------------------------------------
CREATE TRIGGER tr_discord_prefix_ins
    AFTER INSERT
    ON discord.prefix
    FOR EACH ROW
    INSERT INTO discord.log(user_id, action, table_name, log_time, data)
    VALUES (USER(),
            'insert',
            'prefix',
            CURRENT_TIME(6),
            CONCAT(NEW.guild_id, ' | ', NEW.prefix));

CREATE TRIGGER tr_discord_prefix_upd
    BEFORE UPDATE
    ON discord.prefix
    FOR EACH ROW
BEGIN
    INSERT INTO discord.log(user_id, action, table_name, log_time, data)
    VALUES (USER(),
            'update',
            'prefix',
            CURRENT_TIME(6),
            CONCAT(NEW.guild_id, ' | ', NEW.prefix));
END;

CREATE TRIGGER tr_discord_prefix_del
    BEFORE DELETE
    ON discord.prefix
    FOR EACH ROW
BEGIN
    INSERT INTO discord.log(user_id, action, table_name, log_time, data)
    VALUES (USER(),
            'delete',
            'prefix',
            CURRENT_TIME(6),
            CONCAT(OLD.guild_id, ' | ', OLD.prefix));
END;

-- -----------------------------------------------------
-- Table discord.reminder
-- -----------------------------------------------------
CREATE TRIGGER tr_discord_reminder_ins
    AFTER INSERT
    ON discord.reminder
    FOR EACH ROW
    INSERT INTO discord.log(user_id, action, table_name, log_time, data)
    VALUES (USER(),
            'insert',
            'reminder',
            CURRENT_TIME(6),
            CONCAT(NEW.user_id, ' | ', NEW.datetime, ' | ', NEW.reminder, ' | ', New.reminder));

CREATE TRIGGER tr_discord_reminder_upd
    BEFORE UPDATE
    ON discord.reminder
    FOR EACH ROW
BEGIN
    INSERT INTO discord.log(user_id, action, table_name, log_time, data)
    VALUES (USER(),
            'update',
            'reminder',
            CURRENT_TIME(6),
            CONCAT(NEW.user_id, ' | ', NEW.datetime, ' | ', NEW.reminder, ' | ', New.reminder));
END;

CREATE TRIGGER tr_discord_reminder_del
    BEFORE DELETE
    ON discord.reminder
    FOR EACH ROW
BEGIN
    INSERT INTO discord.log(user_id, action, table_name, log_time, data)
    VALUES (USER(),
            'delete',
            'reminder',
            CURRENT_TIME(6),
            CONCAT(OLD.user_id, ' | ', OLD.datetime, ' | ', OLD.reminder, ' | ', OLD.status));
END;


