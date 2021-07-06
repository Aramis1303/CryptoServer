/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.threads;

import eva.cryptoserver.Status;
import eva.cryptoserver.data.Data;
import eva.cryptoserver.qp.AQueriesProcess;
import eva.cryptoserver.qp.Cleaner;
import eva.cryptoserver.qp.QP_Binance;
import eva.cryptoserver.qp.QP_Bittrex;
import eva.cryptoserver.qp.QP_Exmo;
import eva.cryptoserver.sql.SQLQuery;
import eva.cryptoserver.threads.ThreadForDataProcessing;
import eva.cryptoserver.threads.ThreadForSQLWritting;
import static java.lang.Thread.sleep;
import java.util.Date;
import java.util.Map;


/*
 * @author Ermolenko Vadim
 */

public class TraderProcess implements Runnable {
    
    private String burse;
    private Thread process;
    private AQueriesProcess queries;
    
    private boolean isStop;
    
    private long repTimeHistory;
    private long repTimeQuickData;
    private long repTimeSummaryInfo;
    private long repTimeMarketList;
    
    private long repTimeSQLWrite;
    private long repTimeClearing;
    private long nowTime;
    
    private ThreadForDataProcessing tfac;
    
    private SQLQuery sql;
    
    private Map <String, Status> markerList;
    
    public TraderProcess (String burse) {
        
        this.burse = burse;
        this.isStop = false;
        
        sql = new SQLQuery(this.burse);
        
        switch (burse) {
            case "binance": queries = new QP_Binance(this); break;
            case "bittrex": queries = new QP_Bittrex(this); break;
            case "exmo": queries = new QP_Exmo(this); break;
        }
        
        nowTime = new Date().getTime();
        // сдвигаем время запуска каждого процесса, чтобы не создавать пиковых нагрузок;
        repTimeClearing = nowTime + 15000; 
        repTimeMarketList = nowTime + 1000;
        repTimeSummaryInfo = nowTime + 2000;
        repTimeQuickData = nowTime;
        repTimeHistory = nowTime + 3000;
        repTimeSQLWrite = nowTime + 5000; 
        
        // Список Маркетов (торговые пары)
        if (nowTime > repTimeMarketList) {
            queries.qMarketList();
            repTimeHistory += 12 * 60 * 60 * 1000;      // Обновляем каждые 12 часов
        }
        
        // Формируем список маркетов для работы из SQL
        // STATUS: {DELETED | WORKING | STOPED}
        markerList = sql.readMarkets();
        
        // Если первый запуск, то список пустой, заполняем список 1 значением которое 100% есть на бирже: "USDT-BTC"
        if(markerList.isEmpty()) {
            sql.writeMarkets(null);
        }
        else {
            for (Map.Entry <String, Status> entry: markerList.entrySet()) {
                if (!entry.getValue().equals(Status.DELETED)) {
                    queries.getSomeData().put(entry.getKey(), new Data (entry.getKey(), this.burse));
                    queries.getSomeData().get(entry.getKey()).setStatus(entry.getValue());
                }
            }
        }
        
        process = new Thread (this);
        process.setName(burse);
        process.start();
        
        tfac = new ThreadForDataProcessing(markerList, sql, this);
    }

    @Override
    public void run() {
        
        while (true){
            if (isStop) {
                // Записываем всё в базу данных при остановке программы
                if (!queries.getSomeData().isEmpty()) {
                    new ThreadForSQLWritting(queries, sql);
                }
                break;
            }
            
            nowTime = new Date().getTime();
            //////////////////ЗАПРОСЫ//////////////////////
            // Список Маркетов (торговые пары)
            
            if (nowTime > repTimeMarketList) {
                queries.qMarketList();
                repTimeMarketList += 12 * 60 * 60 * 1000;      // Обновляем каждые 12 часов
            }
            
            if (!queries.getSomeData().isEmpty()) {
                
                // История сделок
                if (nowTime > repTimeHistory) {
                    queries.qHistory();
                    repTimeHistory += 15 * 1000;                // Обновляем каждые 15 сек
                }

                // Цены
                if (nowTime > repTimeQuickData) {
                    queries.qQuickData();
                    repTimeQuickData += 15 * 1000;               // Обновляем каждые 15 сек
                }
                
                // Запись данных в sql каждую минут 
                if (nowTime > repTimeSQLWrite) {
                    repTimeSQLWrite += 1 * 60 * 1000;
                    // запись JapaneseCandles
                    new ThreadForSQLWritting(queries, sql);
                }

                // Очистка данных каждые 5 минут 
                if (nowTime > repTimeClearing) {
                    repTimeClearing += 5 * 60 * 1000;
                    new Cleaner(queries);
                }
                
            }
            try {
                sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            }
        }
    }
    
    public void stop (){
        if (tfac != null) {
            tfac.stop();
        }
        isStop = true;
    }
    
    public Map <String, Data> getSomeData (){
        return queries.getSomeData();
    }
    
    public String getName(){
        return this.burse;
    }

    public SQLQuery getSql() {
        return sql;
    }
    
    
    public void stopMarket(String name) {
        queries.getSomeData().get(name).setStatus(Status.STOPED);
        sql.updateMarketStatus(name, Status.STOPED);
    }
    
    public void startMarket(String name) {
        queries.getSomeData().get(name).setStatus(Status.WORKING);
        sql.updateMarketStatus(name, Status.WORKING);
    }
    
    public void deleteMarket(String name) {
        queries.getSomeData().get(name).setStatus(Status.WORKING);
        sql.updateMarketStatus(name, Status.WORKING);
    }
    
    public ThreadForDataProcessing getDataProcessing() {
        return tfac;
    }
}
