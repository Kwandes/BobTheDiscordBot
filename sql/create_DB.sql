#-------------------------------------------
-- Schema: discord
-- Project: https://github.com/Kwandes/BobTheDiscordBot
-- DB version: MySQL 5.7.26
-- Description: Creation scripts for all the tables
#-------------------------------------------

DROP SCHEMA IF EXISTS discord;
CREATE SCHEMA IF NOT EXISTS discord;
USE discord;

-- Contains per-guild messages called 'tags'
CREATE TABLE IF NOT EXISTS discord.tag (
    id          INT AUTO_INCREMENT NOT NULL,
    guild_id    VARCHAR(64)        NOT NULL,
    tag_name    VARCHAR(100)       NOT NULL,
    tag_content VARCHAR(2000)      NOT NULL,
    CONSTRAINT pk_tag PRIMARY KEY (id)
);

-- Contains per-guild prefixes used to trigger commands
CREATE TABLE IF NOT EXISTS discord.prefix (
    id       INT AUTO_INCREMENT NOT NULL,
    guild_id VARCHAR(64)        NOT NULL UNIQUE,
    prefix   VARCHAR(3)         NOT NULL,
    CONSTRAINT pk_prefix PRIMARY KEY (id),
    CONSTRAINT uc_guild_id UNIQUE (guild_id)
);

-- Contains reminders sent to the users at a specified time
CREATE TABLE IF NOT EXISTS discord.reminder (
    id       INT AUTO_INCREMENT NOT NULL,
    user_id  TEXT,
    datetime VARCHAR(32),
    reminder TEXT,
    status   VARCHAR(8),
    CONSTRAINT ph_reminder PRIMARY KEY (id)
);

-- Contains changes done to the tables in this schema. Used by the triggers
CREATE TABLE IF NOT EXISTS discord.log (
    id         INT AUTO_INCREMENT NOT NULL,
    user_id    TEXT,
    action     VARCHAR(10),
    table_name VARCHAR(15),
    log_time   DATETIME(6),
    data       TEXT,
    CONSTRAINT ph_log PRIMARY KEY (id)
);
