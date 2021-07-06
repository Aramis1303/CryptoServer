/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.qp;

import eva.cryptoserver.data.Data;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author username
 */
public class Cleaner implements Runnable{
    
    private AQueriesProcess queries;
    private Thread process;
    
    public Cleaner (AQueriesProcess queries) {
        this.queries = queries;
        
        process = new Thread (this);
        process.setName("Cleaner");
        process.start();
    }
    
    @Override
    public void run() {
           
        long time5minAgo = new Date().getTime() - 5 * 60 * 1000;
        // запись JapaneseCandles
        synchronized (queries.getSomeData()) {
            for (Map.Entry <String, Data> entry: queries.getSomeData().entrySet()) {
                if (entry.getValue().getBuyHistory().size() > 2) {
                    for (int i = entry.getValue().getBuyHistory().size() - 2; i >= 0 ; i--) {
                        if (entry.getValue().getBuyHistory().get(i).getTime() < time5minAgo) {
                            entry.getValue().getBuyHistory().remove(i);
                        }
                    }
                }
                
                if (entry.getValue().getSellHistory().size() > 2) {
                    for (int i = entry.getValue().getSellHistory().size() - 2; i >= 0 ; i--) {
                        if (entry.getValue().getSellHistory().get(i).getTime() < time5minAgo) {
                            entry.getValue().getSellHistory().remove(i);
                        }
                    }
                }
                
                if (entry.getValue().getCandleBy1m().size() > 2) {
                    for (int i = entry.getValue().getCandleBy1m().size() - 2; i >= 0 ; i--) {
                        if (entry.getValue().getCandleBy1m().get(i).getTime() < time5minAgo) {
                            entry.getValue().getCandleBy1m().remove(i);
                        }
                    }
                }
                
                
                if (entry.getValue().getQuickData().size() > 2) {
                    for (int i = entry.getValue().getQuickData().size() - 2; i >= 0 ; i--) {
                        if (/*entry.getValue().getQuickData().get(i).isIsWrittedToSQL() && */entry.getValue().getQuickData().get(i).getTime() < time5minAgo) {
                            entry.getValue().getQuickData().remove(i);
                        }
                    }
                }
                
                /*
                if (entry.getValue().getSummaryInfo().size() > 2) {
                    for (int i = entry.getValue().getSummaryInfo().size() - 2; i >= 0 ; i--) {
                        if (entry.getValue().getSummaryInfo().get(i).isIsWrittedToSQL()) {
                            entry.getValue().getSummaryInfo().remove(i);
                        }
                    }
                }
                */
            }
        }
    }
    
}
