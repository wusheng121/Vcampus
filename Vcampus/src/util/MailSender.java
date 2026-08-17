package util;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 邮件发送（javax.mail / SMTP），配置来自 config.properties。
 * 主要用途：找回密码时向用户邮箱发送验证码。
 */
public final class MailSender {
    private MailSender() {
    }

    /**
     * 发送找回密码验证码邮件。
     *
     * @return 配置缺失或发送失败返回 false
     */
    public static boolean sendVerifyCode(String toEmail, String code, int ttlMinutes) {
        String host = Config.get("mail.smtp.host");
        String port = Config.get("mail.smtp.port", "465");
        String user = Config.get("mail.username");
        String pass = Config.get("mail.password");
        String from = Config.get("mail.from", user);
        String fromName = Config.get("mail.from-name", "虚拟校园");
        boolean ssl = Boolean.parseBoolean(Config.get("mail.smtp.ssl", "true"));

        if (host == null || host.isBlank() || user == null || user.isBlank()
                || pass == null || pass.isBlank()) {
            System.err.println("[MailSender] SMTP 配置不完整，跳过发信");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        if (ssl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", port);
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from, fromName, "UTF-8"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject("虚拟校园 - 找回密码验证码", "UTF-8");
            msg.setContent("您的找回密码验证码是：<b style='font-size:22px'>" + code + "</b><br/>"
                            + "验证码 " + ttlMinutes + " 分钟内有效，请勿向他人泄露。<br/>"
                            + "如非本人操作请忽略此邮件。",
                    "text/html;charset=UTF-8");
            Transport.send(msg);
            return true;
        } catch (Exception e) {
            System.err.println("[MailSender] 发送失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
