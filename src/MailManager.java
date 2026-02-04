import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;


// メール送信内容についてのクラス
public class MailManager {

    public static void checkAndSendEmails(IncomeExpenseManager incomeExpenseManager, WishlistManager wishlistManager) {
        Settings settings = new Settings();
        String recipientEmail = settings.getRecipientEmail();
        
        if (recipientEmail.isEmpty()) {
            System.out.println("メールアドレスが設定されていません。");
            return;
        }
        
        // 前日の記録がない場合
        if (!hasEntryForYesterday()) {
            String subject = "【モチベーター・マネー】記録のリマインド";
            String messageBody = "前日の収支記録を忘れていませんか？";
            
            MailSender.sendMail(recipientEmail, subject, messageBody);
        }

        // 残高が欲しいものリストの商品金額より多い場合
        if (incomeExpenseManager.getBalance() > wishlistManager.getTotalWishlistPrice()) {
            String product = wishlistManager.getAffordableProduct(incomeExpenseManager.getBalance());
            if (product != null) {
                String subject = "【モチベーター・マネー】あなたの欲しい商品が購入可能！";
                String messageBody = "あなたの欲しい商品「" + product + "」が購入できる可能性があります！";
                
                MailSender.sendMail(recipientEmail, subject, messageBody);
            }
        }

        // 節約チャレンジ（ランダム）
        String randomSubject = "【モチベーター・マネー】節約チャレンジ！";
        String randomMessageBody = getRandomChallengeMessage();
        
        MailSender.sendMail(recipientEmail, randomSubject, randomMessageBody);

        
        // 欲望バーと冷静バーの状態
        double desireBar = incomeExpenseManager.getDesireBarPercentage();
        double calmnessBar = incomeExpenseManager.getCalmnessBarPercentage();

        
        if (desireBar >= 70) {
        	String subject = "【モチベーター・マネー】欲望が爆発しています！";
        	String messageBody = "あなたの欲望バーが70%以上になりました！";
        	
            MailSender.sendMail(recipientEmail, subject, messageBody);
        }

        if (calmnessBar >= 50) {
        	String subject = "【モチベーター・マネー】良い調子です！";
        	String messageBody = "あなたの冷静バーが50%以上になりました！";
        	
            MailSender.sendMail(recipientEmail, subject, messageBody);
        }
    }

    
    // 節約チャレンジの内容
    private static String getRandomChallengeMessage() {
        String[] challenges = {
            "明日はランチを持参して、外食を1回減らしてみよう。",
            "明日は必要ない買い物を1回減らして、節約してみよう。",
            "明日は水筒を持参して、ペットボトルの購入を減らしてみよう。",
            "明日はコンビニでの無駄遣いを控えてみよう。",
            "明日はネットショッピングを我慢して、節約してみよう。",
            "明日は電車やバスを使わずに歩いて移動してみよう。",
            "明日はカフェでの飲み物を控えて、自宅で作ってみよう。",
            "明日は必要な物だけ買って、衝動買いをしないようにしよう。",
            "明日は毎日のコーヒー代を100円ずつ節約してみよう。",
            "明日はスーパーでの買い物リストを守って、計画的に買い物しよう。"
        };
        Random random = new Random();
        return challenges[random.nextInt(challenges.length)];
    }
    
    
    // 昨日の収支記録があるかどうかチェック
    public static boolean hasEntryForYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1); // 昨日の日付
        String yesterday = String.format("%04d/%02d/%02d", calendar.get(Calendar.YEAR), 
                                         calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        
        return checkIfDataExistsForDate("income", yesterday) || checkIfDataExistsForDate("expense", yesterday);
    }

    
    // 指定した日付のデータが存在するかチェック
    private static boolean checkIfDataExistsForDate(String type, String date) {
        File file = new File(type + "_" + date.substring(0, 7) + ".txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(date)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    
    // その月の最初の実行かどうかCalendarを使ってチェック
    public static boolean isFirstRunOfMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }
}
