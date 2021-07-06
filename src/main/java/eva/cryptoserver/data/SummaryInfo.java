/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.data;

import java.io.Serializable;

/*
 * @author Ermolenko Vadim
 */
public class SummaryInfo {
    private double hight;
    private double low;
    private double volume;
    private long time;
    
    private boolean isWrittedToSQL = false;
    
    public SummaryInfo (double h, double l, double v, long t){
        this.hight = h;
        this.low = l;
        this.volume = v;
        this.time = t;
    }

    public boolean isIsWrittedToSQL() {
        return isWrittedToSQL;
    }
    public void markWrittedToSQL() {
        this.isWrittedToSQL = true;
    }
    
    public double getHight() {
        return this.hight;
    }
    
    public double getLow() {
        return this.low;
    }
    
    public double getVolume() {
        return this.volume;
    }
    
    public long getTime() {
        return this.time;
    }
}
