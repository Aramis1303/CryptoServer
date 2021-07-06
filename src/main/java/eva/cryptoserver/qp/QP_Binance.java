/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.qp;

import eva.cryptoserver.threads.TraderProcess;
import java.util.List;

/*
 * @author Ermolenko Vadim
 */
public class QP_Binance extends AQueriesProcess {
    
    TraderProcess tp;
    
    public QP_Binance(TraderProcess tp) {
        super(tp);
        this.tp = tp;
    }
    
    @Override
    public void qQuickData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void qHistory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void qMarketList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
