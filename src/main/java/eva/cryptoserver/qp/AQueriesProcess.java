/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.qp;

import eva.cryptoserver.data.Data;
import eva.cryptoserver.threads.TraderProcess;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ermolenko
 */
public abstract class AQueriesProcess {
    // List of data of markets
    protected Map <String, Data> someData;
    protected String burse;
    protected TraderProcess tp;
    
    protected AQueriesProcess (TraderProcess tp) {
        this.tp = tp;
        this.burse = tp.getName();
        someData = new HashMap <> ();
    }
    
    // Удаление файла ошибочных маркетов
    public void clearingInvalidMarkets(){
        synchronized (someData) {
            for (Map.Entry<String, Data> entry: someData.entrySet()){
                if (entry.getValue().isInvalidMarket()){
                    someData.remove(entry.getValue());
                }
            }
        }
    }
    
    protected TraderProcess getTraderProcess(){
        return tp;
    }
    
    public Map <String, Data> getSomeData() {
        return someData;
    }
    
    // Run getter last BID, ASK, LAST
    abstract public void qQuickData ();
    // Run getter history of transaction
    abstract public void qHistory ();
    // Run getter summary info (Volume, Low, Hight)
    abstract public void qMarketList ();
    
}
