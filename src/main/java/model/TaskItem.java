package model;

import java.util.Date;

public class TaskItem {

    private Date startDate;
    private String taskName;
    private int frequencyInDays;

    public TaskItem(Date startDate, String taskName, int frequencyInDays) {
        this.startDate = startDate;
        this.taskName = taskName;
        this.frequencyInDays = frequencyInDays;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getFrequencyInDays() {
        return frequencyInDays;
    }

    public void setFrequencyInDays(int frequencyInDays) {
        this.frequencyInDays = frequencyInDays;
    }
}
