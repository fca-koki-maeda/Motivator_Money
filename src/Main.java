import java.util.Scanner;


// メインクラス
public class Main {
	
    // 各機能を管理するクラスのインスタンス
	// 先に wishlistManager を定義
	private static WishlistManager wishlistManager = new WishlistManager();
	private static IncomeExpenseManager incomeExpenseManager = new IncomeExpenseManager(wishlistManager);
	private static MailManager mailManager = new MailManager();
    
    public static void main(String[] args) {
    	
        Scanner scanner = new Scanner(System.in);
        Settings settings = new Settings();
        
        System.out.println("起動中...しばらくお待ちください...");
        
        // 起動時にAmazon商品価格をチェックして更新
        wishlistManager.updateAmazonPrices();
        
        // 起動時にメール送信条件をチェックして送信
        mailManager.checkAndSendEmails(incomeExpenseManager, wishlistManager);
        
        while (true) {
        	
        	// トップ画面を表示させる
        	displaySummary();
        	 
            // メインメニュー
            System.out.println("\n＝＝＝＝＝ 操作を選択してください！ ＝＝＝＝＝");
            System.out.println("1. 収入の記録");
            System.out.println("2. 支出の記録");
            System.out.println("3. 収支データの管理");
            System.out.println("4. 欲しいものリストの管理");
            System.out.println("5. 設定");
            System.out.println("6. 終了");
            System.out.println("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");

            // 選択画面
            int choice = scanner.nextInt();
            
            switch (choice) {
            
                case 1:
                	// 収入の記録
                    incomeExpenseManager.recordIncome();
                    break;
                    
                case 2:
                	// 支出の記録
                    incomeExpenseManager.recordExpense();
                    break;
                    
                case 3:
                	// 収支データの管理
                    incomeExpenseManager.manageData();
                    break;
                    
                case 4:
                	// 欲しいものリストの管理
                    wishlistManager.showWishlistMenu();
                    break;
                    
                case 5:
                	// 設定
                    settings.showSettingsMenu();
                    break;
                    
                case 6:
                	// 終了
                    System.exit(0);
                    break;
                    
                default:
                	// その他
                    System.out.println("無効な選択肢です。再度選んでください。");
            }
            
        }
        
    }

    // トップ画面
    private static void displaySummary() {
    	
        int totalIncome = incomeExpenseManager.getTotalIncome();
        int totalExpense = incomeExpenseManager.getTotalExpense();
        int balance = totalIncome - totalExpense;
        
        int totalWishlistPrice = wishlistManager.getTotalWishlistPrice();
        
        // 欲望バー、冷静バー、感情アイコンの割合を取得
        double desireBar = incomeExpenseManager.getDesireBarPercentage();
        double calmBar = incomeExpenseManager.getCalmnessBarPercentage();
        double[] emotionRatios = incomeExpenseManager.calculateEmotionIconRatio();
        
        System.out.println("\n＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
        System.out.println("＝＝＝ 俺のためのお小遣い帳へようこそ！ ＝＝＝");
        System.out.println("＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝");
        System.out.println("今月の収入額合計: " + totalIncome);
        System.out.println("今月の支出額合計: " + totalExpense);
        System.out.println("今月の残高: " + balance);
        System.out.println("欲しいものの合計: " + totalWishlistPrice);
        System.out.println("今月の欲望度: " + incomeExpenseManager.getVisualBar(desireBar) + " (" + String.format("%.1f", desireBar) + "%)");
        System.out.println("今月の冷静度: " + incomeExpenseManager.getVisualBar(calmBar) + " (" + String.format("%.1f", calmBar) + "%)");
        System.out.println("今月の感情アイコンの割合");
        System.out.println("嬉しい: " + incomeExpenseManager.getVisualBar(emotionRatios[0]) + " (" + String.format("%.1f", emotionRatios[0]) + "%)");
        System.out.println("ふつう: " + incomeExpenseManager.getVisualBar(emotionRatios[1]) + " (" + String.format("%.1f", emotionRatios[1]) + "%)");
        System.out.println("悲しい: " + incomeExpenseManager.getVisualBar(emotionRatios[2]) + " (" + String.format("%.1f", emotionRatios[2]) + "%)");

    }
    
}