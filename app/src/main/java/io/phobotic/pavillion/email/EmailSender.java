package io.phobotic.pavillion.email;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import io.phobotic.pavillion.prefs.Preferences;

/**
 * Created by Jonathan Nelson on 8/10/16.
 */

public class EmailSender {
    public static final String EMAIL_SEND_SUCCESS = "email_sent";
    public static final String EMAIL_SEND_FAILED = "email_failed";
    public static final String EMAIL_SEND_START = "email_start";

    private static final String TAG = EmailSender.class.getSimpleName();
    private Context context;
    private EmailStatusListener failedListener;
    private EmailStatusListener successListener;
    private List<Attachment> attachments;
    private Object successTag;
    private Object failedTag;

    private String subject = "";
    private String body = "";

    public EmailSender setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public EmailSender setBody(String body) {
        this.body = body;
        return this;
    }

    public EmailSender(Context context) {
        this.context = context;
    }

    public EmailSender withAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public EmailSender setFailedListener(EmailStatusListener failedListener, @Nullable Object tag) {
        this.failedListener = failedListener;
        this.failedTag = tag;
        return this;
    }

    public EmailSender setSuccessListener(EmailStatusListener successListener, @Nullable Object tag) {
        this.successListener = successListener;
        this.successTag = tag;
        return this;
    }

    public interface EmailStatusListener {
        void onEmailSendResult(@Nullable String message, @Nullable Object tag);
    }

    private void notifyStart() {
        Intent i = new Intent(EMAIL_SEND_START);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    private void notifySuccess() {
        //send broadcast notification
        Intent i = new Intent(EMAIL_SEND_SUCCESS);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
        if (successListener != null) {
            successListener.onEmailSendResult(null, successTag);
        }
    }

    private void notifyFail(String message) {
        //send broadcast notification
        Intent i = new Intent(EMAIL_SEND_FAILED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
        if (failedListener != null) {
            failedListener.onEmailSendResult(message, failedTag);
        }
    }

    public EmailSender send() {
        Preferences prefs = Preferences.getInstance(context);
        try {
            String server = prefs.getEmailServer();
            int port = prefs.getEmailPort();
            String username = prefs.getEmailUsername();
            String password = prefs.getEmailPassword();
            List<EmailRecipient> recipientList = prefs.getEmailRecipients();
            String recipients = "";
            String prefix = "";
            for (EmailRecipient recipient: recipientList) {
                recipients += prefix + recipient.getEmail();
                prefix = ",";
            }

            Email email = new Email()
                    .setServer(server)
                    .setPort(port)
                    .setUsername(username)
                    .setPassword(password)
                    .setRecipients(recipients)
                    .setSubject(subject)
                    .setBody(body);

            if (attachments == null) {
                Log.d(TAG, "no attachments given");
            } else {
                for (Attachment attachment : attachments) {
                    email.addAttachment(attachment);
                }
            }

            EmailSendTask emailSendTask = new EmailSendTask();
            emailSendTask.execute(email);
        } catch (Exception e) {
            String message = "Caught exception while trying to send email: " + e.getMessage();
            Log.e(TAG, message);
            e.printStackTrace();
            notifyFail(message);
        }



        return this;
    }

    public EmailSender sendTestEmail() {
        Preferences prefs = Preferences.getInstance(context);
        try {
            String server = prefs.getEmailServer();
            int port = prefs.getEmailPort();
            String username = prefs.getEmailUsername();
            String password = prefs.getEmailPassword();
            List<EmailRecipient> recipientList = prefs.getEmailRecipients();
            String recipients = "";
            String prefix = "";
            for (EmailRecipient recipient: recipientList) {
                recipients += prefix + recipient.getEmail();
                prefix = ",";
            }

            Email email = new Email()
                    .setServer(server)
                    .setPort(port)
                    .setUsername(username)
                    .setPassword(password)
                    .setRecipients(recipients)
                    .setSubject("Test message")
                    .setBody("This is a test message from the Check Digits android app.  You can " +
                            "safely ignore this message");
            EmailSendTask emailSendTask = new EmailSendTask();
            emailSendTask.execute(email);
        } catch (Exception e) {
            String message = "Caught exception while sending test email: " + e.getMessage();
            Log.d(TAG, message);
            e.printStackTrace();
            notifyFail(message);
        }

        return this;
    }

    private class EmailSendTask extends AsyncTask<Email, Void, Boolean>{
        @Override
        protected Boolean doInBackground(Email[] emails) {
            notifyStart();
            for (Email email: emails) {
                if (!email.isValid()) {
                    String message = "Unable to send email. One or more of server, port, username, or password " +
                            "has not been set in settings";
                    Log.d(TAG, message);
                    notifyFail(message);
                } else {
                    Properties props = new Properties();
                    props.put("mail.smtp.user", email.getUsername());
                    props.put("mail.smtp.host", email.getServer());
                    props.put("mail.smtp.port", email.getPort());
                    props.put("mail.debug", "true");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable","true");
                    props.put("mail.smtp.EnableSSL.enable","true");

                    props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.setProperty("mail.smtp.socketFactory.fallback", "false");
                    props.setProperty("mail.smtp.socketFactory.port", "465");

                    Session mailSession = Session.getDefaultInstance(props, null);

                    MimeMessage emailMessage = new MimeMessage(mailSession);
                    try {
                        InternetAddress[] recipientAddresses = InternetAddress.parse(email.getRecipients());
                        emailMessage.setRecipients(Message.RecipientType.TO, recipientAddresses);
                        String fromAddress = "\"Check Digit Lookups\" <" + email.getUsername() + ">";
                        emailMessage.setFrom(new InternetAddress(fromAddress));
                        emailMessage.setSubject(email.getSubject());
                        Multipart multipart = new MimeMultipart();
                        MimeBodyPart messageBodyPart = new MimeBodyPart();
                        messageBodyPart.setContent(email.getBody(), "text/plain");
                        multipart.addBodyPart(messageBodyPart);

                        for (Attachment attachment: email.getAttachments()) {
                            MimeBodyPart attachmentPart = new MimeBodyPart();
                            try {
                                attachmentPart.attachFile(attachment.getFile());
                                multipart.addBodyPart(attachmentPart);
                            } catch (IOException e) {
                                Log.e(TAG, "unable to attach file '" + attachment.getFile() +"' to email");
                            }
                        }

                        Address[] recipients = emailMessage.getAllRecipients();
                        if (recipients == null) {
                            throw new AddressException("No recipients defined");
                        }

                        emailMessage.setContent(multipart);

                        Transport transport = mailSession.getTransport("smtps");
                        transport.connect(email.getServer(), email.getUsername(), email.getPassword());
                        transport.sendMessage(emailMessage, recipients);
                        transport.close();

                        Log.d(TAG, "email sent");
                        notifySuccess();
                    } catch (AddressException e) {
                        e.printStackTrace();
                        String message = e.getMessage();
                        Log.e(TAG, message);
                        notifyFail(message);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                        String message = e.getMessage();
                        Log.e(TAG, message);
                        notifyFail(message);
                    }
                }
            }



            return true;
        }
    }

}



