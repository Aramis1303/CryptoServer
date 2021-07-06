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
public class QuickData {
    private double last;
    private double bid;
    private double ask;
    
    private long time;
    
    private boolean isWrittedToSQL = false;
    
    public QuickData (double l, double  b, double a, long t){
        this.last = l;
        this.bid = b;
        this.ask = a;
        this.time = t;
    }   
    
    public boolean isIsWrittedToSQL() {
        return isWrittedToSQL;
    }
    public void markWrittedToSQL() {
        this.isWrittedToSQL = true;
    }
    
    public double getLast (){
        return this.last;
    }
    
    public double getAsk (){
        return this.ask;
    }
    
    public double getBid (){
        return this.bid;
    }
    
    public long getTime() {
        return this.time;
    }
}
