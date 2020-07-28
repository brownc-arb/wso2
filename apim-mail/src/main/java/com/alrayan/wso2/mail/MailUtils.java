package com.alrayan.wso2.mail;

import com.alrayan.wso2.common.AlRayanConfiguration;
import com.alrayan.wso2.common.AlRayanError;
import com.alrayan.wso2.common.exception.AlRayanMailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * Utility class for managing workflow.
 *
 * @since 1.0.0
 */
public class MailUtils {

    private static Logger log = LoggerFactory.getLogger(MailUtils.class);

    /**
     * Sends an email to the given email addresses with the given parameters.
     *
     * @param fromEmailAddress  from email address
     * @param fromEmailPassword from email password for authenticating to the from email
     * @param toAddressesList   list of "to" recipients (comma separated string)
     * @param subject           email subject
     * @param htmlEmailBody     HTML email content (rendered velocity template)
     * @throws AlRayanMailException thrown when error on sending user email
     */
    public static void sendEmail(String fromEmailAddress, String fromEmailPassword, String toAddressesList,
                                 String subject, String htmlEmailBody, Map<String, String> attachments)
            throws AlRayanMailException {
        try {
            Address[] addresses = InternetAddress.parse(toAddressesList);
            sendEmail(fromEmailAddress, fromEmailPassword, addresses, subject, htmlEmailBody, attachments);
        } catch (AddressException e) {
            throw new AlRayanMailException(AlRayanError.ERROR_CONSTRUCTING_EMAIL.getErrorMessageWithCode(), e);
        }
    }

    /**
     * Sends an email to the given email addresses with the given parameters.
     *
     * @param fromEmailAddress  from email address
     * @param fromEmailPassword from email password for authenticating to the from email
     * @param toAddresses       list of "to" recipients
     * @param subject           email subject
     * @param htmlEmailBody     HTML email content (rendered velocity template)
     * @throws AlRayanMailException thrown when error on sending user email
     */
    private static void sendEmail(String fromEmailAddress, String fromEmailPassword, Address[] toAddresses,
                                  String subject, String htmlEmailBody, Map<String, String> attachments)
            throws AlRayanMailException {
        try {
            Properties properties = new Properties();
            properties.put(AlRayanConfiguration.MAIL_SMTP_AUTH.getProperty(),
                    AlRayanConfiguration.MAIL_SMTP_AUTH.getValue());
            properties.put(AlRayanConfiguration.MAIL_SMTP_STARTTLS_ENABLE.getProperty(),
                    AlRayanConfiguration.MAIL_SMTP_STARTTLS_ENABLE.getValue());
            properties.put(AlRayanConfiguration.MAIL_SMTP_HOST.getProperty(),
                    AlRayanConfiguration.MAIL_SMTP_HOST.getValue());
            properties.put(AlRayanConfiguration.MAIL_SMTP_PORT.getProperty(),
                    AlRayanConfiguration.MAIL_SMTP_PORT.getValue());

            Session session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(fromEmailAddress, fromEmailPassword);
                        }
                    });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmailAddress));
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            message.setSubject(subject);

            Multipart multipart = new MimeMultipart();

            // Email body.
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setContent(htmlEmailBody, "text/html");
            multipart.addBodyPart(textBodyPart);

            // Attachments
            for (Map.Entry<String, String> attachment : attachments.entrySet()) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                if (attachment.getKey().equals("spCertificate")) {
                    String cert =
                            "-----BEGIN CERTIFICATE-----" + System.getProperty("line.separator") +
                                    new String(Base64.getDecoder().decode(attachment.getValue())) +
                                    System.getProperty("line.separator") + "-----END CERTIFICATE-----";
                    DataSource source = new ByteArrayDataSource(cert, "application/x-any");
                    textBodyPart.setFileName("spCertificate.pem");
                    textBodyPart.setDataHandler(new DataHandler(source));
                } else {
                    DataSource source = new FileDataSource(attachment.getValue()); // Attachment path
                    attachmentBodyPart.setDataHandler(new DataHandler(source));
                    attachmentBodyPart.setFileName(attachment.getKey()); // Attaching file name
                    multipart.addBodyPart(attachmentBodyPart); // add the attachment part
                }
            }
            message.setContent(multipart);
            Thread.currentThread().setContextClassLoader(MailUtils.class.getClassLoader());
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
            CommandMap.setDefaultCommandMap(mc);
            Transport.send(message);
        } catch (MessagingException | IOException e) {
            throw new AlRayanMailException(AlRayanError.ERROR_CONSTRUCTING_EMAIL.getErrorMessageWithCode(), e);
        }
    }
}
