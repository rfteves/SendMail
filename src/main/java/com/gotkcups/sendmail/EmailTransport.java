/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gotkcups.sendmail;

/**
 *
 * @author rfteves
 */
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author RICARDO
 */
public class EmailTransport implements Serializable {

  private static Logger logger = Logger.getLogger(EmailTransport.class);
  protected String to, from, subject, textmessage, cc, bcc;
  public String replyto;
  protected Session session;
  protected Store store;
  protected Properties properties = System.getProperties();
  protected String host, protocol;
  Authenticator auth;

  public static void main(String[] s) throws Exception {
    /*System.getProperties().put("mail.smtp.auth", "true");
        System.getProperties().put("mail.smtp.starttls.enable", "true");
        System.getProperties().put("mail.smtp.host", "smtp.gmail.com");
        System.getProperties().put("mail.smtp.port", "587");
        
        /*System.getProperties().setProperty("mail.smtp.host", Utility.getApplicationProperty("mail.smtp.host"));
        System.getProperties().setProperty("mail.username", Utility.getApplicationProperty("mail.username"));
        System.getProperties().setProperty("mail.password", Utility.getApplicationProperty("mail.password"));
        System.getProperties().setProperty("mail.smtp.port", Utility.getApplicationProperty("mail.smtp.port"));
        System.getProperties().put("mail.smtp.auth", "true");
        System.getProperties().put("mail.smtp.starttls.enable", "true");*/

    EmailTransport sendEmail = new EmailTransport("ricardo@drapers.com", "ricardo@drapers.com", "ricardo@drapers.com", "subject test", "message test");
    sendEmail.send();
  }

  public EmailTransport() {
    logger.info(String.format("Mail Server: %s", properties.getProperty("mail.smtp.host")));
    try {
      this.checkAuth();
      session = Session.getInstance(properties, auth);
    } catch (Exception e) {
      logger.error("Session error", e);
    }
  }

  private void checkAuth() {
    final StringBuilder authuser = new StringBuilder();
    final StringBuilder authpwd = new StringBuilder();
    try {
      authuser.append(System.getProperty("mail.username"));
      authpwd.append(System.getProperty("mail.password"));
      auth = new javax.mail.Authenticator() {

        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(authuser.toString(), authpwd.toString());
        }
      };
    } catch (Exception e) {
    }
  }

  /*
     * mail.smtp.host=smtp.bizmail.yahoo.com
    mail.username=support@rebatesfactory.com
    mail.password=zxc321cxzmail.smtp.host

     * */
  public EmailTransport(String server) {
    properties.put("mail.smtp.host", server);
    properties.put("mail.transport.protocol", "smtp");
    logger.info(String.format("Mail Server: %s", properties.getProperty("mail.smtp.host")));
    try {
      this.checkAuth();
      session = Session.getInstance(properties, auth);
    } catch (Exception e) {
      logger.error("Session error", e);
    }
  }

  public EmailTransport(String server, String to, String from, String replyto, String subject,
          String textmessage) {
    this(server);
    setTo(to);
    setFrom(from);
    setReplyto(replyto);
    setSubject(subject);
    setTextmessage(textmessage);
  }

  public EmailTransport(String to, String from, String replyto, String subject,
          String textmessage) {
    this();
    setTo(to);
    setFrom(from);
    setReplyto(replyto);
    setSubject(subject);
    setTextmessage(textmessage);
  }

  public void setCC(String cc) {
    this.cc = cc;
  }

  public void setBCC(String bcc) {
    this.bcc = bcc;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public void setReplyto(String replyto) {
    this.replyto = replyto;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setTextmessage(String textmessage) {
    this.textmessage = textmessage;
  }

  private List<AttachmentEntry> attachments = new ArrayList<>();

  public void addAttachment(AttachmentEntry entry) {
    this.attachments.add(entry);
  }

  public void send() throws Exception {
    Exception error = null;
    try {
      Multipart multi = new MimeMultipart();
      Message message = new MimeMessage(session);
      BodyPart body = new MimeBodyPart();
      message.setFrom(new InternetAddress(this.from));
      if (this.to != null) {
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(this.to));
      }
      if (this.cc != null) {
        message.setRecipients(Message.RecipientType.CC,
                InternetAddress.parse(this.cc));
      }
      if (this.bcc != null) {
        message.setRecipients(Message.RecipientType.BCC,
                InternetAddress.parse(this.bcc));
      }
      if (this.replyto != null) {
        message.setReplyTo(InternetAddress.parse(this.replyto));
      }
      body.setContent(this.textmessage, "text/html; charset=utf-8");
      multi.addBodyPart(body);
      message.setSubject(this.subject);
      message.setContent(multi);
      message.setSentDate(Calendar.getInstance().getTime());
      for (AttachmentEntry atta : this.attachments) {
        try {
          MimeBodyPart attachment = new MimeBodyPart();
          ByteArrayDataSource ds = new ByteArrayDataSource(atta.getData(), atta.getValue());
          attachment.setDataHandler(new DataHandler(ds));
          attachment.setFileName(atta.getKey());
          multi.addBodyPart(attachment);
        } catch (MessagingException ex) {
          java.util.logging.Logger.getLogger(EmailTransport.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

      body.setContent(this.textmessage, "text/html");
      multi.addBodyPart(body);
      message.setSubject(this.subject);
      message.setContent(this.textmessage, "text/html");
      message.setContent(multi);
      message.setSentDate(Calendar.getInstance().getTime());
      Transport.send(message);
      //Transport.send(message);
    } catch (Exception e) {
      error = e;
      logger.error("Session transport error", e);
    } finally {
      to = null;
      from = null;
      subject = null;
      textmessage = null;
      cc = null;
      bcc = null;
      replyto = null;
      if (error != null) {
        throw error;
      }
    }
  }
  
  public static class AttachmentEntry implements Map.Entry<String, String> {

    private String key, value;
    private byte[]data;

    public AttachmentEntry(String filename, String contentType, byte[]data) {
      this.key = filename;
      this.value = contentType;
      this.data = data;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
      return data;
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public String getValue() {
      return value;
    }

    @Override
    public String setValue(String value) {
      this.value = value;
      return value;
    }

  }
}
