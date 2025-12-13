package service;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;   // ✅ DataHandler HATASI İÇİN ZORUNLU

public class EmailService {

    private static final String FROM = "seninmail@gmail.com";  
    private static final String PASSWORD = "uygulama_sifresi"; // ✅ GMAIL APP PASSWORD

    public static void sendMail(String to, String subject, String messageText) {

        if (to == null || to.trim().isEmpty()) {
            System.out.println("⚠️ Alıcı e-posta boş, mail gönderilmedi.");
            return;
        }

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM, PASSWORD);
                }
            });

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // ✅ UTF-8 + Türkçe karakter sorunsuz
            message.setContent(messageText, "text/plain; charset=UTF-8");

            Transport.send(message);

            System.out.println("✅ Mail başarıyla gönderildi → " + to);

        } catch (Exception e) {
            System.out.println("❌ Mail gönderme hatası:");
            e.printStackTrace();
        }
    }
}
