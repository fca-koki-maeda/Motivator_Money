// 参考サイト：w.atwiki.jp/chapati4it/pages/124.html

// JavaでGmailからメールを送信するサンプル（JavaMail使用）
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPTransport;

public class MailSender {
  public static void sendMail(String to, String subject, String body) {
    try {
      // プロパティの設定
      Properties props = System.getProperties();
      // ホスト
      props.put("mail.smtp.host", "smtp.gmail.com");
      // 認証（する）
      props.put("mail.smpt.auth", "true");
      // ポート指定（サブミッションポート）
      props.put("mail.smtp.port", "587");
      // STARTTLSによる暗号化（する）
      props.put("mail.smtp.starttls.enable", "true");
 
      // セッションの取得
      Session session = Session.getInstance(props);
 
      // MimeMessageの取得と設定
      Message msg = new MimeMessage(session);
      // 送信者設定
      msg.setFrom(new InternetAddress("差出人メールアドレス"));
      // 宛先設定
      //Settings settings = new Settings(); // 設定オブジェクトを取得
      //String recipient = settings.getRecipientEmail();
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
      // タイトル設定
      msg.setSubject(subject);
      // 本文設定
      msg.setText(body);
      // 送信日時設定
      msg.setSentDate(new Date());
 
      // 送信
      SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
      try {
        t.connect("smtp.gmail.com", "Gmailアドレス", "Gmailアプリパスワード");
        t.sendMessage(msg, msg.getAllRecipients());
        System.out.println("メール送信機能を使用しました。");
      } finally {
        t.close();
      }
 
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
 