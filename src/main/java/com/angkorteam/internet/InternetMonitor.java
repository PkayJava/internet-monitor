package com.angkorteam.internet;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class InternetMonitor implements Runnable {

    private final String FORMAT = "hh.mm aa dd/MM/yyyy 'Time Zone' ZZ";

    private final File statusFile;
    private final Status status;
    private final Gson gson;
    private final JavaMailSender sender;
    private final String mailTo;
    private final String mailSenderFrom;
    private final String mailSenderName;
    private final String pingUrl;
    private final int alertInterval;

    public InternetMonitor(File statusFile, Status status, Gson gson, JavaMailSender sender, String mailTo, String mailSenderFrom, String mailSenderName, String pingUrl, int alertInterval) {
        this.statusFile = statusFile;
        this.status = status;
        this.gson = gson;
        this.sender = sender;
        this.mailTo = mailTo;
        this.mailSenderFrom = mailSenderFrom;
        this.mailSenderName = mailSenderName;
        this.pingUrl = pingUrl;
        this.alertInterval = alertInterval;
    }

    @Override
    public void run() {
        this.status.setCounter(this.status.getCounter() + 1);
        HttpClientBuilder builder = HttpClientBuilder.create();
        try (CloseableHttpClient client = builder.build()) {
            HttpGet request = new HttpGet(this.pingUrl);
            try (CloseableHttpResponse response = client.execute(request)) {
                EntityUtils.consume(response.getEntity());
            }
            if (this.status.getLastSuccess() == null) {
                this.status.setCounter(0);
                Date lastError = this.status.getLastError();
                Date lastSuccess = new Date();
                writeStatusFile(null, lastSuccess);
                StringBuffer buffer = new StringBuffer();
                if (lastError != null) {
                    buffer.append("last error at " + DateFormatUtils.format(lastError, FORMAT) + "\n");
                }
                buffer.append("working at " + DateFormatUtils.format(lastSuccess, FORMAT) + "\n");
                sendMail(this.sender, " Internet Up", buffer.toString());
            } else {
                if (this.status.getCounter() >= this.alertInterval) {
                    this.status.setCounter(0);
                    Date lastError = this.status.getLastError();
                    Date lastSuccess = new Date();
                    writeStatusFile(null, lastSuccess);
                    StringBuffer buffer = new StringBuffer();
                    if (lastError != null) {
                        buffer.append("last error at " + DateFormatUtils.format(lastError, FORMAT) + "\n");
                    }
                    buffer.append("working at " + DateFormatUtils.format(lastSuccess, FORMAT) + "\n");
                    sendMail(this.sender, " Internet Up", buffer.toString());
                } else {
                    Date lastSuccess = new Date();
                    writeStatusFile(null, lastSuccess);
                }
            }
        } catch (IOException e) {
            if (this.status.getLastError() == null) {
                writeStatusFile(new Date(), null);
            } else {
                writeStatusFile(this.status.getLastError(), null);
            }
        }
    }

    protected void writeStatusFile(Date lastError, Date lastSuccess) {
        try {
            this.status.setLastError(lastError);
            this.status.setLastSuccess(lastSuccess);
            FileUtils.write(this.statusFile, this.gson.toJson(status), "UTF-8");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void sendMail(JavaMailSender sender, String subject, String text) {
        try {
            MimeMessage message = this.sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setSubject(subject);
            helper.setFrom(this.mailSenderFrom, this.mailSenderName);
            helper.setText(text);
            helper.setTo(this.mailTo);
            sender.send(message);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
