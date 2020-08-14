package dev.hotdeals.bob_the_discord_bot.model;

import java.time.LocalDateTime;

public class Reminder
{

    private int reminderId;
    private String userId;
    private String reminder;
    private LocalDateTime dateTime;
    private String status;

    public Reminder()
    {
    }

    public Reminder(int reminderId, String userId, LocalDateTime dateTime, String reminder, String status)
    {
        this.reminderId = reminderId;
        this.userId = userId;
        this.dateTime = dateTime;
        this.reminder = reminder;
        this.status = status;
    }

    public Reminder(int reminderId, String userId, String dateTime, String reminder, String status)
    {
        this(reminderId, userId, LocalDateTime.parse(dateTime), reminder, status);
    }

    public String toString()
    {
        return "Id: " + reminderId + " | " + userId + " | " + dateTime + " | " + reminder;
    }

    public int getReminderId()
    {
        return reminderId;
    }

    public void setReminderId(int reminderId)
    {
        this.reminderId = reminderId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getReminder()
    {
        return reminder;
    }

    public void setReminder(String reminder)
    {
        this.reminder = reminder;
    }

    public LocalDateTime getDateTime()
    {
        return dateTime;
    }

    public void setDateTime(String dateTime)
    {
        this.dateTime = LocalDateTime.parse(dateTime);
    }

    public void setDateTime(LocalDateTime dateTime)
    {
        this.dateTime = dateTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }


}
