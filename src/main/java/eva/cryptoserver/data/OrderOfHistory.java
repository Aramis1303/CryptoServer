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
public class OrderOfHistory {
    private double price;
    private double volume;
    private long time;
    private int id;
    
    private boolean isWrittedToSQL = false;
    
    public OrderOfHistory (double p, double v, long t, int i){
        this.price = p;
        this.volume = v;
        this.time = t;
        this.id = i;
    }
    
    public boolean isIsWrittedToSQL() {
        return isWrittedToSQL;
    }
    public void markWrittedToSQL() {
        this.isWrittedToSQL = true;
    }
    
    public double getPrice () {
        return this.price;
    }
    
    public double getVolume () {
        return this.volume;
    }
    
    public long getTime () {
        return this.time;
    }
    
    public int getId () {
        return this.id;
    }
}
