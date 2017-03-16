package io.phobotic.pavillion.email;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by Jonathan Nelson on 11/12/16.
 */

public class EmailRecipient implements Comparable<EmailRecipient>, Serializable {
    private boolean receiveDailyReport = false;
    private boolean receiveWeeklyReport = false;
    private boolean receiveMonthlyReport = false;
    private String email;
    private boolean saved;

    public EmailRecipient(@NotNull String email) {
        if (email == null) throw new IllegalArgumentException("Email address can not be null");
        this.email = email;
        this.saved = false;
    }

    public boolean isReceiveDailyReport() {
        return receiveDailyReport;
    }

    public EmailRecipient setReceiveDailyReport(boolean receiveDailyReport) {
        this.receiveDailyReport = receiveDailyReport;
        return this;
    }

    public boolean isReceiveWeeklyReport() {
        return receiveWeeklyReport;
    }

    public EmailRecipient setReceiveWeeklyReport(boolean receiveWeeklyReport) {
        this.receiveWeeklyReport = receiveWeeklyReport;
        return this;
    }

    public boolean isReceiveMonthlyReport() {
        return receiveMonthlyReport;
    }

    public EmailRecipient setReceiveMonthlyReport(boolean receiveMonthlyReport) {
        this.receiveMonthlyReport = receiveMonthlyReport;
        return this;
    }

    public boolean isSaved() {
        return saved;
    }

    public EmailRecipient setSaved(boolean saved) {
        this.saved = saved;
        return this;
    }

    @Override
    public int compareTo(EmailRecipient o) {
        return email.compareTo(o.getEmail());
    }

    public String getEmail() {
        return email;
    }

    public EmailRecipient setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EmailRecipient)) return false;

        return (this.getEmail().equals(((EmailRecipient) obj).getEmail()));
    }
}
