import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


// 欲しいものリストについてのクラス
public class WishlistManager {
	
	// ユーザー入力用
    private Scanner scanner = new Scanner(System.in);  
    
    // 欲しいものリストのファイル名
    private final String WISHLIST_FILE = "wishlist.txt";  

    // 選択画面
    public void showWishlistMenu() {
    	
        while (true) {
            System.out.println("\n＜欲しいものリストの管理メニュー＞");
            System.out.println("1. 欲しいものを追加");
            System.out.println("2. Amazonの商品を追加");
            System.out.println("3. 欲しいものリストの表示と削除");
            System.out.println("4. 価格更新");
            System.out.println("5. 戻る");

            int choice = scanner.nextInt();
            
            // 改行を読み捨てる
            scanner.nextLine();
            
            switch (choice) {
            
                case 1:
                	// 欲しいものの商品名と価格の追加
                    addItemToWishlist();  
                    break;
                    
                case 2:
                	// Amazonの商品URLの追加
                    addUrlToWishlist();    
                    break;
                    
                case 3:
                	// 欲しいものリストの表示と削除
                    displayWishlist();      
                    break;
                    
                case 4:
                	// 価格を確認して更新 
                	updateAmazonPrices();    
                    break;
                    
                case 5:
                	// 戻る
                    return;
                    
                default:
                    System.out.println("無効な選択肢です。再度選んでください。");
            }
        }
    }

    
    // 欲しいものの商品名と金額を入力
    public void addItemToWishlist() {
    	
        System.out.println("商品名を入力してください:");
        String name = scanner.next();
        
        System.out.println("価格を入力してください:");
        String price = scanner.next();

        // データをファイルに書き込み
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(WISHLIST_FILE, true))) {
            writer.write(name + "," + price + ",\n");
            System.out.println("欲しいものリストに追加されました。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   
    // Amazonの商品URLを入力
    public void addUrlToWishlist() {
    	
        System.out.println("商品のURLを入力してください:");
        String url = scanner.next();
        
        try {
            // JsoupでHTMLを取得
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();

            // 商品名を取得
            Element productNameElement = doc.selectFirst("#productTitle");
            String productName = productNameElement != null ? productNameElement.text() : "商品名が見つかりません";

            // 商品価格を取得
            Element priceElement = doc.selectFirst(".a-price-whole");
            String price = priceElement != null ? priceElement.text() : "価格が見つかりません";

            // 価格からコンマを削除（価格が見つかった場合のみ）
            if (!price.equals("価格が見つかりません")) {
                price = price.replaceAll(",", "");
            }
            
            // 結果を表示
            System.out.println("商品名: " + productName);
            System.out.println("価格: " + price);
            
            // 欲しいものリストに保存
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(WISHLIST_FILE, true))) {
                writer.write(productName + "," + price + "," + url + "\n");
                System.out.println("Amazonからの情報が欲しいものリストに追加されました。");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.out.println("エラーが発生しました: " + e.getMessage());
        }
    }
    
    
    // Amazon商品の価格を更新
    public void updateAmazonPrices() {
    	System.out.println("欲しいものリストの価格の更新を開始しています。");
    	System.out.println("更新にはインターネット接続を利用します。");
        
    	File file = new File(WISHLIST_FILE);
        
        // ファイルが存在しない場合
        if (!file.exists()) {
            System.out.println("欲しいものリストは作成されていません。");
            return; // 処理を中断
        }
    	
    	List<String> updatedLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(WISHLIST_FILE))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length == 3 && parts[2].startsWith("http")) {
                    String url = parts[2];

                    try {
                        // JsoupでHTMLを取得
                        Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();

                        // 商品価格を取得
                        Element priceElement = doc.selectFirst(".a-price-whole");
                        String price = priceElement != null ? priceElement.text().trim() : parts[1];

                        // 修正箇所: 価格のコンマを削除する処理を追加
                        price = price.replace(",", "");
                        
                        // 更新したデータをリストに追加
                        updatedLines.add(parts[0] + "," + price + "," + url);
                        System.out.println("価格更新: " + parts[0] + " -> " + price);

                    } catch (Exception e) {
                        System.out.println("価格更新失敗: " + parts[0] + " (URL: " + url + ")");
                        // 更新失敗の場合は元の行を追加
                        updatedLines.add(line); 
                    }
                } else {
                	// URLがない場合はそのまま追加
                    updatedLines.add(line); 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ファイルに更新された内容を書き込み
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(WISHLIST_FILE))) {
            for (String updatedLine : updatedLines) {
                writer.write(updatedLine + "\n");
            }
            System.out.println("価格の更新が完了しました。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
   
    // 欲しいものリストを表示
    public void displayWishlist() {
    	
        File file = new File(WISHLIST_FILE);
        
        // 空の場合
        if (!file.exists()) {
            System.out.println("欲しいものリストは空です。");
            return;
        }

        List<String[]> wishlist = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(WISHLIST_FILE))) {
            String line;
            
            // コンマで分割してリストに追加
            while ((line = reader.readLine()) != null) {
                wishlist.add(line.split(","));  
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // リストの表示
        System.out.println("欲しいものリスト一覧:");
        
        for (int i = 0; i < wishlist.size(); i++) {
            String[] item = wishlist.get(i);
            String itemName = item[0].trim();
            String itemPrice = item.length > 1 ? item[1].trim() : "価格未定";
            String itemUrl = item.length > 2 ? item[2].trim() : "URL未定";
            System.out.println("商品番号: " + (i + 1) + ", 商品名: " + itemName + ", 価格: " + itemPrice + ", URL: " + itemUrl);
        }
        
        // データの削除メニュー
        if (!wishlist.isEmpty()) {
        	
        	// 削除データの行番号を指定
            System.out.println("\n＜削除したい場合は行番号を入力してください＞（0: キャンセル）");
            
            Scanner scanner = new Scanner(System.in);
            int itemNumber = scanner.nextInt();

            // キャンセルの０が押された場合
            if (itemNumber == 0) {
                System.out.println("削除をキャンセルしました。");
                return;
            } else if (itemNumber < 1 || itemNumber > wishlist.size()) {
                System.out.println("無効な番号です。");
                return;
            }

            // 指定された項目を削除
            wishlist.remove(itemNumber - 1);

            // ファイルに新しいリストを書き込む
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(WISHLIST_FILE))) {
                for (String[] item : wishlist) {
                    writer.write(String.join(",", item) + "\n");
                }
                System.out.println("項目を削除しました。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    //合計金額を戻り値として返す
    public int getTotalWishlistPrice() {
        File file = new File(WISHLIST_FILE);

        // ファイルが存在しない場合は終了
        if (!file.exists()) {
            System.out.println("欲しいものリストは空です。");
            return 0;
        }

        int totalPrice = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(WISHLIST_FILE))) {
            String line;
            
            // 各商品の価格を合計
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 1) {
                    try {
                    	int price = Integer.parseInt(parts[1].trim().replaceAll(",", ""));
                        totalPrice += price;
                    } catch (NumberFormatException e) {
                        // 価格が不正な場合はスキップ
                        System.out.println("無効な価格形式がありました: " + parts[1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalPrice;
    }
    
    
    // 指定した予算内で購入可能な商品のリストを取得
    public String getAffordableProduct(int balance) {
        File file = new File(WISHLIST_FILE);
        if (!file.exists()) {
            return null;
        }

        List<String> affordableProducts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(WISHLIST_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 1) {
                    try {
                        int price = Integer.parseInt(parts[1].trim().replaceAll(",", ""));
                        if (balance >= price) {
                            affordableProducts.add(parts[0]);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("無効な価格形式がありました: " + parts[1]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (affordableProducts.isEmpty()) {
            return null;
        }

        return affordableProducts.get(new Random().nextInt(affordableProducts.size()));
    }
}