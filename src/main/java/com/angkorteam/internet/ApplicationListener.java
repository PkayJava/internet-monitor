package com.angkorteam.internet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.core.StandardServer;
import org.apache.commons.io.FileUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ApplicationListener implements LifecycleListener {

    private static final Log LOGGER = LogFactory.getLog(ApplicationListener.class);

    private static final String TAG = ApplicationListener.class.getSimpleName();

    private String mailSenderHost;
    private int mailSenderPort;
    private String mailSenderProtocol;
    private boolean mailSenderSsl;
    private String mailSenderUsername;
    private String mailSenderPassword;
    private String mailSenderFrom;
    private String mailSenderName;
    private int interval;
    private String mailTo;
    private String pingUrl;
    private int alertInterval;

    private JavaMailSender sender;

    private Gson gson;

    private Status status;

    private ScheduledExecutorService executor;

    private ScheduledFuture<?> task;

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

        if (event.getSource() != null) {
            if (event.getSource() instanceof StandardServer) {
                this.executor = Executors.newSingleThreadScheduledExecutor();

                this.gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ").create();

                File statusFile = new File(FileUtils.getTempDirectory(), ApplicationListener.class.getName() + ".json");
                if (!statusFile.exists()) {
                    this.status = new Status();
                    this.status.setLastSuccess(new Date());
                    try {
                        FileUtils.write(statusFile, this.gson.toJson(status), "UTF-8");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        this.status = gson.fromJson(FileUtils.readFileToString(statusFile, "UTF-8"), Status.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JsonSyntaxException e) {
                        this.status = new Status();
                        this.status.setLastSuccess(new Date());
                        try {
                            FileUtils.write(statusFile, this.gson.toJson(status), "UTF-8");
                        } catch (IOException e1) {
                            throw new RuntimeException(e1);
                        }
                    }
                }
                if (event.getLifecycle().getState() == LifecycleState.INITIALIZING) {
                    JavaMailSenderImpl sender = new JavaMailSenderImpl();
                    sender.setHost(this.mailSenderHost);
                    sender.setUsername(this.mailSenderUsername);
                    sender.setPassword(this.mailSenderPassword);
                    sender.setPort(this.mailSenderPort);
                    sender.setProtocol(this.mailSenderProtocol);
                    Properties properties = new Properties();
                    if (this.mailSenderUsername != null || this.mailSenderPassword != null) {
                        properties.put("mail.smtp.auth", true);
                    }
                    if (this.mailSenderSsl) {
                        properties.put("mail.smtp.ssl.enable", true);
                        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    }
                    sender.setJavaMailProperties(properties);
                    this.sender = sender;
                    this.task = this.executor.scheduleAtFixedRate(new InternetMonitor(statusFile, this.status, this.gson, this.sender, this.mailTo, this.mailSenderFrom, this.mailSenderName, this.pingUrl, this.alertInterval), 0, this.interval, TimeUnit.MINUTES);
                } else if (event.getLifecycle().getState() == LifecycleState.DESTROYING) {
                    if (this.task != null) {
                        this.task.cancel(true);
                    }
                    if (this.executor != null) {
                        this.executor.shutdown();
                    }
                }
            }
        }
    }

    public String getMailSenderHost() {
        return mailSenderHost;
    }

    public void setMailSenderHost(String mailSenderHost) {
        this.mailSenderHost = mailSenderHost;
    }

    public int getMailSenderPort() {
        return mailSenderPort;
    }

    public void setMailSenderPort(int mailSenderPort) {
        this.mailSenderPort = mailSenderPort;
    }

    public String getMailSenderProtocol() {
        return mailSenderProtocol;
    }

    public void setMailSenderProtocol(String mailSenderProtocol) {
        this.mailSenderProtocol = mailSenderProtocol;
    }

    public boolean isMailSenderSsl() {
        return mailSenderSsl;
    }

    public void setMailSenderSsl(boolean mailSenderSsl) {
        this.mailSenderSsl = mailSenderSsl;
    }

    public String getMailSenderUsername() {
        return mailSenderUsername;
    }

    public void setMailSenderUsername(String mailSenderUsername) {
        this.mailSenderUsername = mailSenderUsername;
    }

    public String getMailSenderPassword() {
        return mailSenderPassword;
    }

    public void setMailSenderPassword(String mailSenderPassword) {
        this.mailSenderPassword = mailSenderPassword;
    }

    public String getMailSenderFrom() {
        return mailSenderFrom;
    }

    public void setMailSenderFrom(String mailSenderFrom) {
        this.mailSenderFrom = mailSenderFrom;
    }

    public String getMailSenderName() {
        return mailSenderName;
    }

    public void setMailSenderName(String mailSenderName) {
        this.mailSenderName = mailSenderName;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public String getPingUrl() {
        return pingUrl;
    }

    public void setPingUrl(String pingUrl) {
        this.pingUrl = pingUrl;
    }

    public int getAlertInterval() {
        return alertInterval;
    }
}
