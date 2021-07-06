/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.threads;

import eva.cryptoserver.data.Data;
import eva.cryptoserver.qp.AQueriesProcess;
import eva.cryptoserver.data.Period;
import eva.cryptoserver.sql.SQLQuery;
import java.util.Map;

/**
 *
 * @author username
 */
public class ThreadForSQLWritting  implements Runnable{
    
    private AQueriesProcess queries;
    private Thread process;
    private SQLQuery sql;
    
    public ThreadForSQLWritting (AQueriesProcess queries, SQLQuery sql) {
        this.queries = queries;
        this.sql = sql;
        
        process = new Thread (this);
        process.setName("ThreadForSQLWritting");
        process.start();
    }

    @Override
    public void run() {
        synchronized (queries.getSomeData()) {
            for (Map.Entry <String, Data> entry: queries.getSomeData().entrySet()) {
                // Запись минутных свечей
                if (sql.writeJapaneseCandles(entry.getValue().getCandleBy1m(), entry.getKey(), Period.MINUTE) /* && 
                    sql.writeQuickData(entry.getValue().getQuickData(), entry.getKey()) &&     
                    sql.writeSummaryInfo(entry.getValue().getSummaryInfo(), entry.getKey()) */) 
                {
                    entry.getValue().lastSqlConnection = true;
                } 
            }
        }
    }
}
