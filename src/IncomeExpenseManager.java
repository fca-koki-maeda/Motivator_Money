import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;


// 収入と支出についてのクラス
public class IncomeExpenseManager {
	
    private Scanner scanner = new Scanner(System.in);
    private String currentExpenseFile;
    private String currentIncomeFile;
    private final String balanceHistoryFile = "balance_history.txt";
    private final WishlistManager wishlistManager;

    
    public IncomeExpenseManager(WishlistManager wishlistManager) {
    	
    	this.wishlistManager = wishlistManager;
    	
    	// 初期化時に現在のファイルを更新
        updateCurrentFiles();  
    }

    
    // 収入の記録
    public void recordIncome() {
    	
    	// 日付を入力
        String date = getDateFromUser();
        
        System.out.println("収入額を入力してください:");
        String amount = scanner.next();
        amount = amount.trim();
        
        System.out.println("内容を入力してください:");
        String description = scanner.next();
        
        System.out.println("感情を選択してください:");
        System.out.println("[1. 嬉しい(^_^)  2. ふつう(._.),  3. 悲しい(T_T) ]:");
        int emotion = scanner.nextInt();

        // 収入データをファイルに書き込み
        String incomeFile = generateFileName("income", date);
        
        // ファイルが存在しない場合に新規作成を呼び出す
        ensureFileExists(incomeFile); 
        writeDataToFile(incomeFile, date, amount, description, emotion);
        
        System.out.println("収入が記録されました。");
    }

    
    // 支出の記録
    public void recordExpense() {
    	
    	// 日付を入力
        String date = getDateFromUser();
        
        System.out.println("支出額を入力してください:");
        String amount = scanner.next();
        amount = amount.trim();
        
        System.out.println("内容を入力してください:");
        String description = scanner.next();
        
        System.out.println("感情を選択してください");
        System.out.println("[1. 嬉しい(＾_＾)  2. ふつう(・_・),  3. 悲しい(Ｔ_Ｔ) ]:");
        int emotion = scanner.nextInt();

        // 支出データをファイルに書き込み
        String expenseFile = generateFileName("expense", date);
        
        // ファイルが存在しない場合に新規作成を呼び出す
        ensureFileExists(expenseFile); 
        writeDataToFile(expenseFile, date, amount, description, emotion);
        
        System.out.println("支出が記録されました。");
    }
    
    
    // 年月に基づいたファイル名を生成
    private String generateFileName(String type, String date) {
    	
    	// 日付を「年」「月」「日」に分割
        String[] dateParts = date.split("/");  
        
        // 年月部分を取得
        String yearMonth = dateParts[0] + dateParts[1];  
        
        // income_YYYYMM.txt または expense_YYYYMM.txt の形式で作成
        return type + "_" + yearMonth + ".txt";  
    }
    
    
    // ファイルが存在しない場合に新規作成
    private void ensureFileExists(String fileName) {
    	
        File file = new File(fileName);
        
        // ファイルが存在しない場合
        if (!file.exists()) { 
            try {
                file.createNewFile();
                System.out.println(fileName + " が新しく作成されました。");
            } catch (IOException e) {
                System.out.println(fileName + " の作成に失敗しました。");
                e.printStackTrace();
            }
        }
    }
    
    
    // ユーザーに日付を入力させる
    private String getDateFromUser() {
    	
        System.out.println("年を入力してください (例: 2024):");
        int year = scanner.nextInt();
        
        System.out.println("月を入力してください (例: 10):");
        int month = scanner.nextInt();
        
        System.out.println("日を入力してください (例: 1):");
        int day = scanner.nextInt();

        // 日付のフォーマット
        return String.format("%04d/%02d/%02d", year, month, day);
    }
    

    // 指定されたファイルにデータを書き込む
    private void writeDataToFile(String fileName, String date, String amount, String description, int emotion) {
    	
    	// trueで既存のファイルに追記する
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {  
            writer.write(date + "," + amount + "," + description + "," + emotion + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    // 収支データの管理
    public void manageData() {
    	
    	// メール送信条件をチェックするためにMailManagerを呼び出す
        MailManager mailManager = new MailManager();

        // 1. 昨日の収支データがあるか確認
        if (mailManager.hasEntryForYesterday()) {
            System.out.println("昨日の収支データがあります。");
        } else {
            System.out.println("昨日の収支データはありません。");
        }
        
        // 2. 残高を表示
        int balance = getBalance();
        System.out.println("現在の残高は: " + balance + "円です。");

        // 3. 月初のチェック
        if (mailManager.isFirstRunOfMonth()) {
            System.out.println("新しい月の開始です。");
        }
        
    	// 選択画面
    	System.out.println("\n＜収支データの管理メニュー＞");
    	
    	// 収入データをすべて表示
    	System.out.println("\n収入データ一覧:");
    	List<FileDataEntry> incomeEntries = displayDataWithIndex("income");

    	// 支出データをすべて表示
        System.out.println("\n支出データ一覧:");
        List<FileDataEntry> expenseEntries = displayDataWithIndex("expense");
        
        // データの削除メニュー
        System.out.println("\n＜削除したい場合はデータ種類を選択してください＞");
        System.out.println("1: 収入データ");
        System.out.println("2: 支出データ");
        System.out.println("3: 戻る");
        int dataType = scanner.nextInt();  
        
        // 戻るが押された場合
        if (dataType == 3) {
            System.out.println("トップ画面に戻ります。");
            return;
        }

        // データがない場合
        List<FileDataEntry> selectedEntries = (dataType == 1) ? incomeEntries : expenseEntries;
        
        if (selectedEntries.isEmpty()) {
            System.out.println("選択されたデータは存在しません。");
            return;
        }

        // 削除データの行番号を指定
        System.out.println("＜削除したいデータの行番号を入力してください＞ (0: キャンセル)");
        int lineNumber = scanner.nextInt();

        // キャンセルの０が押された場合
        if (lineNumber == 0) {
            System.out.println("操作がキャンセルされました。");
            return;
        }
        
        // データがない場合
        if (lineNumber > 0 && lineNumber <= selectedEntries.size()) {
            FileDataEntry entryToDelete = selectedEntries.get(lineNumber - 1);
            deleteData(entryToDelete);
        } else {
            System.out.println("無効な行番号です。");
        }
        
    }

    
    // データを種類ごとに通し番号を付けて表示
    // 年月順で通し番号付きデータリストを返す
    private List<FileDataEntry> displayDataWithIndex(String type) {
    	
        List<FileDataEntry> entries = new ArrayList<>();
        File directory = new File(".");
        
        //ファイルを種類でフィルタリング
        File[] files = directory.listFiles((dir, name) -> name.startsWith(type + "_"));  

        
        if (files != null && files.length > 0) {
        	
        	// 年月順（ファイル名順）にソート
            Arrays.sort(files, Comparator.comparing(File::getName)); 
            int index = 1;
            
            for (File file : files) {
                System.out.println(type.equals("income") ? file.getName().substring(7, 13) : file.getName().substring(8, 14) );

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    int lineNumber = 1;
                    while ((line = reader.readLine()) != null) {
                        FileDataEntry entry = new FileDataEntry(file.getName(), line, lineNumber);
                        entries.add(entry);
                        System.out.println(index + ": " + entry.data);
                        index++;
                        lineNumber++;
                    }
                } catch (IOException e) {
                    System.out.println(file.getName() + " の読み込みに失敗しました。");
                }
                System.out.println();
            }
        } else {
            System.out.println(type.equals("income") ? "収入データが存在しません。" : "支出データが存在しません。");
        }
        return entries;
    }
    
    
    // 指定したデータを削除
    private void deleteData(FileDataEntry entryToDelete) {
    	
        File file = new File(entryToDelete.fileName);
        List<String> lines = new ArrayList<>();

        // ファイルを読み込み→指定された行を削除→残りをリストに保持
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;
            int lineNumber = 1;
            
            while ((line = reader.readLine()) != null) {
                if (lineNumber != entryToDelete.lineNumber) {
                	// 指定された行番号以外はリストに保持
                    lines.add(line); 
                }
                lineNumber++;
            }
        } catch (IOException e) {
            System.out.println(file.getName() + " の読み込みに失敗しました。");
            return;
        }

        // ファイルを書き直して内容が空の場合は削除
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            // 明示的にバッファをフラッシュ
            writer.flush(); 
            
        } catch (IOException e) {
            System.out.println("データの削除に失敗しました。");
            e.printStackTrace();
            return;
        }
            
        // ファイルサイズが０バイトの場合はファイルごと削除
        if (file.length() == 0) { 
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("ファイル " + file.getName() + " が削除されました。");
            } else {
                System.out.println("ファイル " + file.getName() + " の削除に失敗しました。");
            }
        }
    }
    
    
    // 前月のファイル名を取得
    private String getPreviousMonthFileName(String type) {
    	
        Calendar calendar = Calendar.getInstance();
        
        // 前月に移動
        calendar.add(Calendar.MONTH, -1);  
        
        int year = calendar.get(Calendar.YEAR);
        
        // 月は0ベースのため1を加算
        int month = calendar.get(Calendar.MONTH) + 1;
        
        return type + "_" + String.format("%04d%02d", year, month) + ".txt";
    }
    
    
    // 今月のファイル名を更新
    private void updateCurrentFiles() {
    	
        Calendar calendar = Calendar.getInstance();
        
        int year = calendar.get(Calendar.YEAR);
        
        // 月は0ベースのため1を加算
        int month = calendar.get(Calendar.MONTH) + 1;
        
        String monthString = String.format("%04d%02d", year, month);
        this.currentIncomeFile = "income_" + monthString + ".txt";
        this.currentExpenseFile = "expense_" + monthString + ".txt";

        // 日にちが1の場合は新しいファイルを作成
        if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
        	
            File newIncomeFile = new File(currentIncomeFile);
            File newExpenseFile = new File(currentExpenseFile);
            
            try {
                if (newIncomeFile.createNewFile()) {
                    System.out.println(currentIncomeFile + " が新しく作成されました。");

                    // 前月の残高を繰り越しとして新しい収入に追加する機能を呼び出す
                    carryOverBalance();
                }
                if (newExpenseFile.createNewFile()) {
                    System.out.println(currentExpenseFile + " が新しく作成されました。");
                    
                    // 前月の残高を繰り越しとして新しい収入に追加する機能を呼び出す
                    carryOverBalance();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    
    // 前月の残高を繰り越す
    private void carryOverBalance() {
    	
        String prevIncomeFile = getPreviousMonthFileName("income");
        String prevExpenseFile = getPreviousMonthFileName("expense");

        // 前月の収入合計
        int prevIncome = calculateTotal(prevIncomeFile); 
        
        // 前月の支出合計
        int prevExpense = calculateTotal(prevExpenseFile);  
        
        // 残高を計算
        int balance = prevIncome - prevExpense;  

        // 残高がある場合
        if (balance > 0) {
        	
        	// 繰り越しするか選択させる
            System.out.println("前月の残高は " + balance + " 円です。繰り越しますか？ (1: はい, 2: いいえ)");
            int choice = scanner.nextInt();
            
            // はいが押されたら残高を新しい収入ファイルに書き込む（感情アイコンはふつう）
            if (choice == 1) {
                writeDataToFile(currentIncomeFile, getTodayDate(), String.valueOf(balance), "前月からの繰り越し", 2);
                System.out.println("繰り越しが完了しました。");
            } else {
                System.out.println("繰り越しがキャンセルされました。");
            }
            
        } else {
            System.out.println("前月の残高はありません。繰り越しは不要です。");
        }
    }
    
    
    // 今日の日付を取得
    private String getTodayDate() {
    	
        Calendar calendar = Calendar.getInstance();
        
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        
        return String.format("%04d/%02d/%02d", year, month, day);
    }
    
    
    // 収入の合計を取得
    public int getTotalIncome() {
    	
        return calculateTotal(currentIncomeFile);
    }

    
    // 支出の合計を取得
    public int getTotalExpense() {
    	
        return calculateTotal(currentExpenseFile);
    }
    
    // 残高を取得
    public int getBalance() {
    	
    	int totalIncome = calculateTotal(currentIncomeFile);
        int totalExpense = calculateTotal(currentExpenseFile);
        return totalIncome - totalExpense;
     }

    
    // 特定のファイルの合計を計算
    private int calculateTotal(String fileName) {
    	
        int total = 0;
        File file = new File(fileName);
        
        // ファイルが存在しない場合は合計を０とする
        if (!file.exists()) return total; 
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
            	
                String[] parts = line.split(",");
                
                // 収入・支出額を取り出す
                String amountString = parts[1].trim(); 
                
                // 数字をそのまま加算
                total += Integer.parseInt(amountString); 
            }
            
        } catch (IOException e) {
            System.out.println("データ " + fileName + " が見つかりません。");
            
        } catch (NumberFormatException e) {
            System.out.println("数値の変換エラー: " + e.getMessage());
            
        }
        return total;
    }
    
    
    // ファイル名と行データを保持する内部クラス
    private static class FileDataEntry {
        String fileName;
        String data;
        int lineNumber;

        FileDataEntry(String fileName, String data, int lineNumber) {
            this.fileName = fileName;
            this.data = data;
            this.lineNumber = lineNumber;
        }

        public String toString() {
            return fileName + " (" + lineNumber + "): " + data;
        }
    }
 
    
    // 月ごとの収支データをすべて表示
    public void displayAllMonthlyData() {
    	System.out.println("\n＜月ごとの収支データ一覧＞");

    	// 収入データを表示
        System.out.println("\n収入データ一覧:");
        displayDataWithIndex("income"); 

        // 支出データを表示
        System.out.println("\n支出データ一覧:");
        displayDataWithIndex("expense");  
    }
    
    
    // 欲望バーの割合を取得
    public double getDesireBarPercentage() {
        int totalWishlistPrice = wishlistManager.getTotalWishlistPrice();
        int balance = getBalance();
        
        // 残高が0以下なら欲望バーは0%
        if (balance <= 0) return 0.0; 
        return Math.min(100.0, ((double) totalWishlistPrice / balance) * 100); // 最大100%
    }
    
    
    // 冷静バーの割合を取得
    public double getCalmnessBarPercentage() {
        List<Integer> last10Balances = getLast10DayBalances();
        if (last10Balances.size() < 2) return 100.0; // データ不足時は100%

        int stableDays = 0;
        for (int i = 1; i < last10Balances.size(); i++) {
            int diff = Math.abs(last10Balances.get(i) - last10Balances.get(i - 1));
            if (diff <= 1000) {
                stableDays++;
            }
        }
        return ((double) stableDays / (last10Balances.size() - 1)) * 100;
    }
    
    
    // 視覚的なバーを表示するメソッド（30%なら■■■、100%なら■■■■■■■■■■みたいな感じ）
    public String getVisualBar(double percentage) {
    	// 10%ごとに■を1つ表示
        int barLength = (int) (percentage / 10); 
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            bar.append("■");
        }
        return bar.toString();
    }
    
    
    // 直近10日間の残高を取得
    private List<Integer> getLast10DayBalances() {
        List<Integer> balances = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(balanceHistoryFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        balances.add(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.out.println("残高履歴ファイルの読み込みに失敗しました。");
        }
        
        // 直近10日分のみ保持
        int startIndex = Math.max(0, balances.size() - 10);
        return balances.subList(startIndex, balances.size());
    }
    


    // 感情アイコン割合を計算するメソッド
    public double[] calculateEmotionIconRatio() {
        int happyCount = 0;
        int neutralCount = 0;
        int sadCount = 0;

        // 収入ファイルの読み込み
        try (BufferedReader reader = new BufferedReader(new FileReader(currentIncomeFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                
                // 4つ以上のデータがある場合に処理
                if (parts.length >= 4) {  
                    try {
                    	// 感情アイコンのデータを数値に変換
                        int emotion = Integer.parseInt(parts[3].trim());
                        if (emotion == 1) {
                            happyCount++;
                        } else if (emotion == 2) {
                            neutralCount++;
                        } else if (emotion == 3) {
                            sadCount++;
                        }
                        
                    // 数値変換エラーがあった場合    
                    } catch (NumberFormatException e) {
                    	
                        System.out.println("不正な感情アイコンデータ: " + parts[3]);  
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 支出ファイルの読み込み
        try (BufferedReader reader = new BufferedReader(new FileReader(currentExpenseFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                
                // 4つ以上のデータがある場合に処理
                if (parts.length >= 4) {  
                    try {
                    	// 感情アイコンのデータを数値に変換
                        int emotion = Integer.parseInt(parts[3].trim()); 
                        if (emotion == 1) {
                            happyCount++;
                        } else if (emotion == 2) {
                            neutralCount++;
                        } else if (emotion == 3) {
                            sadCount++;
                        }
                        
                    // 数値変換エラーがあった場合
                    } catch (NumberFormatException e) {
                        System.out.println("不正な感情アイコンデータ: " + parts[3]);  
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        // 合計エントリ数を計算
        int totalEntries = happyCount + neutralCount + sadCount;
        
        // [0] = 嬉しい, [1] = ふつう, [2] = 悲しい
        double[] emotionRatios = new double[3]; 

        // 合計が0の場合、割合を0に設定
        if (totalEntries == 0) {
            emotionRatios[0] = emotionRatios[1] = emotionRatios[2] = 0.0;
        } else {
            // 各感情アイコンの割合を計算
            // 整数の割り算を避けるため、double型にキャストしてから計算する
            emotionRatios[0] = (happyCount / (double) totalEntries) * 100;    // 嬉しい
            emotionRatios[1] = (neutralCount / (double) totalEntries) * 100;  // ふつう
            emotionRatios[2] = (sadCount / (double) totalEntries) * 100;      // 悲しい
        }

        return emotionRatios;
        
    }

}