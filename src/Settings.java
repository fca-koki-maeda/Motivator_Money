import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


// 設定画面についてのクラス
public class Settings {
	
	// 受信者のメールアドレス
    public String recipientEmail;  

    // 初期値は空
    public Settings() {
        recipientEmail = loadRecipientEmail();
    }
    
    // 設定メニューの表示と選択処理
    public void showSettingsMenu() {
    	
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n＜設定メニュー＞");
            System.out.println("1. メールアドレスの設定");
            System.out.println("2. テストメールの送信");
            System.out.println("3. アプリの初期化");
            System.out.println("4. 戻る");

            int choice = scanner.nextInt();
            
            // 改行を読み捨てる
            scanner.nextLine();

            switch (choice) {
            
                case 1:
                	// メールアドレスを設定
                    System.out.print("メールアドレスを入力してください: ");
                    String recipientEmail = scanner.nextLine();
                    setRecipientEmail(recipientEmail); 
                    System.out.println("メールアドレスを設定しました: " + recipientEmail);
                    break;
                    
                case 2:
                	// テストメールの送信
                	// アドレスが未設定の場合
                    if (getRecipientEmail().isEmpty()) {
                        System.out.println("メールアドレスが設定されていません。設定してください。");
                        return;  
                    }
                    
                    // 件名
                    String subject = "【モチベーター・マネー】テストメールの確認";  
                    
                    // メール本文
                    String messageBody = 
                    		"ご利用ありがとうございます。\n" +
                    	    "このメールは、お小遣い帳アプリ「モチベーター・マネー」の通知機能テストです。\n" +
                    	    "\n" +
                    	    "メールが問題なく受信できた場合、通知機能は正常に動作しています。\n" +
                    	    "\n" +
                    	    "設定されているメールアドレスは以下の通りです。\n" +
                    	    getRecipientEmail() +
                    	    "\n" +
                    	    "メールが迷惑メールフォルダに入っている場合は、メールアプリの受信設定を確認してください。\n" +
                    	    "今後ともモチベーター・マネーをよろしくお願いいたします。";

                    // MailSenderを使ってメールを送信
                    MailSender.sendMail(getRecipientEmail(), subject, messageBody); 
                    System.out.println("テストメールが" + getRecipientEmail() + "に送信されました。");
                    break;
                    
                case 3:
                	// アプリの初期化（まだ未実装）
                	break;
                	
                case 4:
                	// メニューを終了
                    return;  
                    
                default:
                    System.out.println("無効な選択です。もう一度選択してください。");
            }
        }
    }


    // メールアドレスを設定するメソッド
    public void setRecipientEmail(String email) {
        this.recipientEmail = email;
        saveRecipientEmail(email);
    }
    
    // メールアドレスを取得するメソッド
    public String getRecipientEmail() {
        return recipientEmail;
    }
    
    // メールアドレスを保存するメソッド
    private void saveRecipientEmail(String email) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("recipient_email.txt"))) {
            writer.write(email);
        } catch (IOException e) {
            System.out.println("メールアドレスの保存中にエラーが発生しました。");
            e.printStackTrace();
        }
    }
    
    // メールアドレスを読み込むメソッド
    private String loadRecipientEmail() {
        try (BufferedReader reader = new BufferedReader(new FileReader("recipient_email.txt"))) {
            return reader.readLine();
        } catch (IOException e) {
        	// ファイルが存在しない場合でもエラーを出さず、空文字を返す
            System.out.println("メールアドレスが設定されていないかエラーが発生しています。\nメールアドレスを設定してください。");
            return "";
        }
    }
    
}
