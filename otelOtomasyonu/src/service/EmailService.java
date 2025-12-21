package service;

import java.io.File;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class EmailService {

    private static final String FROM = "tahirrkilicc8@gmail.com";
    private static final String PASSWORD = "nasehtvvnqfpvbcs"; // Gmail APP PASSWORD

    /**
     * Ortak mail oturum yapılandırması (TLS 587)
     */
    private static Session createSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM, PASSWORD);
            }
        });
    }

    /**
     * Standart metin içeriğine sahip e-posta gönderir.
     */
    public static void sendMail(String to, String subject, String messageText) {
        if (to == null || to.trim().isEmpty()) {
            System.out.println("⚠️ Alıcı e-posta boş, mail gönderilmedi.");
            return;
        }

        try {
            Session session = createSession();
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject != null ? subject : "");
            message.setText(messageText != null ? messageText : "", "UTF-8");

            Transport.send(message);
            System.out.println("✅ Mail başarıyla gönderildi → " + to);

        } catch (Exception e) {
            System.out.println("❌ Mail gönderme hatası:");
            e.printStackTrace();
        }
    }

    /**
     * İçerisinde PDF veya başka bir dosya eki olan e-posta gönderir.
     * @param filePath Gönderilecek dosyanın tam yolu (Örn: "hasilat_raporu_2025.pdf")
     */
    public static void sendMailWithAttachment(String to, String subject, String messageText, String filePath) {
        if (to == null || to.trim().isEmpty()) {
            System.out.println("⚠️ Alıcı e-posta boş, mail gönderilmedi.");
            return;
        }

        try {
            Session session = createSession();
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject != null ? subject : "");

            // 1. Metin kısmını oluştur
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(messageText != null ? messageText : "", "UTF-8");

            // 2. Dosya ekini oluştur
            MimeBodyPart attachmentPart = new MimeBodyPart();
            File file = new File(filePath);
            if (file.exists()) {
                attachmentPart.attachFile(file);
            } else {
                System.out.println("⚠️ Dosya bulunamadı, eklenmeden gönderiliyor: " + filePath);
            }

            // 3. Parçaları birleştir (Multipart)
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            if (file.exists()) {
                multipart.addBodyPart(attachmentPart);
            }

            // İçeriği mesaja set et
            message.setContent(multipart);

            // Gönder
            Transport.send(message);
            System.out.println("✅ Ekli mail başarıyla gönderildi → " + to + " (Dosya: " + filePath + ")");

        } catch (Exception e) {
            System.out.println("❌ Ekli mail gönderme hatası:");
            e.printStackTrace();
        }
    }
}