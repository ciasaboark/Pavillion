package io.phobotic.pavillion.email;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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

import io.phobotic.pavillion.converter.ExcelConverter;
import io.phobotic.pavillion.database.SearchRecord;
import io.phobotic.pavillion.database.SearchesDatabase;
import io.phobotic.pavillion.prefs.Preferences;

/**
 * Created by Jonathan Nelson on 8/10/16.
 */

public class EmailSender {
    private static final String TAG = EmailSender.class.getSimpleName();
    private Context context;
    private EmailStatusListener failedListener;
    private EmailStatusListener successListener;
    private List<Attachment> attachments;
    private Object successTag;
    private Object failedTag;

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
        void onEmailSendResult(Object tag);
    }

    private void notifySuccess() {
        if (successListener != null) {
            successListener.onEmailSendResult(successTag);
        }
    }

    private void notifyFail() {
        if (failedListener != null) {
            failedListener.onEmailSendResult(failedTag);
        }
    }

    public EmailSender send() {
        Preferences prefs = Preferences.getInstance(context);
        String server = prefs.getEmailServer();
        int port = prefs.getEmailPort();
        String username = prefs.getEmailUsername();
        String password = prefs.getEmailPassword();
        String recipients = prefs.getEmailRecipients();
        DateFormat df = SimpleDateFormat.getDateInstance();
        String dateString = df.format(new Date());
        String subject = "Check Digit Lookups for " + dateString;
        String body = "Location check digit lookups for " + dateString;

        Email email = new Email()
                .setServer(server)
                .setPort(port)
                .setUsername(username)
                .setPassword(password)
                .setRecipients(recipients)
                .setSubject(subject)
                .setBody(body);

        SearchesDatabase db = SearchesDatabase.getInstance(context);
        List<SearchRecord> records = db.getUnsentSearches();
        Attachment attachment = null;
        try {
            ExcelConverter converter = new ExcelConverter(context, records);
            File file = converter.convert();
            attachment = new Attachment(file, file.getName());
            email.addAttachment(attachment);
        } catch (IOException e) {
            Log.e(TAG, "Unable to convert records into " +
                    "excel file.\n\nError: " + e.getMessage());
            //todo modify email to send details of error
        }

        EmailSendTask emailSendTask = new EmailSendTask();
        emailSendTask.execute(email);

        return this;
    }

    public EmailSender sendTestEmail() {
        Preferences prefs = Preferences.getInstance(context);
        String server = prefs.getEmailServer();
        int port = prefs.getEmailPort();
        String username = prefs.getEmailUsername();
        String password = prefs.getEmailPassword();
        String recipients = prefs.getEmailRecipients();

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

        return this;
    }

    private class EmailSendTask extends AsyncTask<Email, Void, Boolean>{
        @Override
        protected Boolean doInBackground(Email[] emails) {
            for (Email email: emails) {
                if (!email.isValid()) {
                    Log.d(TAG, "Unable to send email. One or more of server, port, username, or password " +
                            "has not been set in settings");
                    notifyFail();
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

                    //// TODO: 8/13/16

                    Session mailSession = Session.getDefaultInstance(props, null);

                    MimeMessage emailMessage = new MimeMessage(mailSession);
                    try {
                        InternetAddress[] recipientAddresses = InternetAddress.parse(email.getRecipients());
                        emailMessage.setRecipients(Message.RecipientType.TO, recipientAddresses);
                        String fromAddress = "\"Check Digit Lookups\" <" + email.getUsername() + ">";
                        emailMessage.setFrom(new InternetAddress(fromAddress));
                        emailMessage.setSubject(email.getSubject());
                        Message message = new MimeMessage(mailSession);
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

                        emailMessage.setContent(multipart);

                        Transport transport = mailSession.getTransport("smtps");
                        transport.connect(email.getServer(), email.getUsername(), email.getPassword());
                        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
                        transport.close();

                        Log.d(TAG, "email sent");
                        notifySuccess();
                    } catch (AddressException e) {
                        e.printStackTrace();
                        Log.e(TAG, "email could not be sent: " + e.getMessage());
                        notifyFail();
                    } catch (MessagingException e) {
                        e.printStackTrace();
                        Log.e(TAG, "email could not be sent: " + e.getMessage());
                        notifyFail();
                    }
                }
            }



            return true;
        }
    }

}



