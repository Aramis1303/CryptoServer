/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.data;

/**
 *
 * @author username
 */
public class AnaliticData {
    // Start time
    private long time;
    
    // Значения экстремумов сети
    private double value;
    
    public AnaliticData (long t, double i){
        this.time = t;
        this.value = i;
    }
    
    // Геттеры
    public long getTime() {
        return this.time;
    }
    public double getValue() {
        return this.value;
    }
}
