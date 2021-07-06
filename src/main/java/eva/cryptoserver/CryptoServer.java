package eva.cryptoserver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import eva.cryptoserver.threads.TraderProcess;
import eva.cryptoserver.gui.BursePan;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Home
 */
public class CryptoServer extends Application {
    
    private static Map <String, TraderProcess> traders;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        // Устанавливаем временную зону = 0, чтобы избежать путаницы во временных поясах.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        
        // Создаем коллекцию потоков обработки бирж
        traders = new HashMap<>();
        traders.put("bittrex", new TraderProcess("bittrex"));
        
        // Запуск GUI
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        
        TabPane mainPane = new TabPane();
        // Отключаем возможность закрытия вкладок
        mainPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        for (Map.Entry <String, TraderProcess> t: traders.entrySet()){
            mainPane.getTabs().add(new BursePan(t.getValue()));
        }
        mainPane.setId("pane");
        
        Scene scene = new Scene(mainPane);
        // Подключаем таблицу стилей
        //scene.getStylesheets().add(CryptoBot.class.getResource("style.css").toExternalForm()); 
        
        // Начать с полноэкранного режима
        primaryStage.setMaximized(true);
        // Завершение при закрытии окна
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
            @Override
            public void handle(WindowEvent event) {
                for (Map.Entry <String, TraderProcess> t: traders.entrySet()){
                    t.getValue().stop();
                }
                
                for (Tab tb: mainPane.getTabs()) {
                    ((BursePan)tb).kill();
                }
            }
        });
        primaryStage.setTitle("CryptoBot");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
