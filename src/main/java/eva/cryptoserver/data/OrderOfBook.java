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
public class OrderOfBook {
    private double price;
    private double volume;
    
    private boolean isWrittedToSQL = false;
    
    public OrderOfBook (double p, double v){
        this.price = p;
        this.volume = v;
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
}
