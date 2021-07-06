/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.qp;

import eva.cryptoserver.Status;
import eva.cryptoserver.threads.TraderProcess;
import eva.cryptoserver.data.Data;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * @author Ermolenko Vadim
 */
public class QP_Bittrex extends AQueriesProcess {
    
    public QP_Bittrex(TraderProcess tp) {
        super(tp);
    }
    
    @Override
    public void qQuickData() {
        for (Map.Entry<String, Data> entry : someData.entrySet()) {
            if (entry.getValue().getStatus().equals(Status.WORKING)) {
                new eva.cryptoserver.qp.bittrex.QueryQuickData(entry.getValue());
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TraderProcess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void qHistory() {
        for (Map.Entry<String, Data> entry : someData.entrySet()) {
            if (entry.getValue().getStatus().equals(Status.WORKING)) {
                new eva.cryptoserver.qp.bittrex.QueryHistory(entry.getValue());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TraderProcess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public void qMarketList() {
        new eva.cryptoserver.qp.bittrex.QueryMarketList(tp);
    }
    
}
