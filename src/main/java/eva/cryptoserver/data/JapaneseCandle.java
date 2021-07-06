/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.data;

/**
 *
 * @author ermolenko
 */
public class JapaneseCandle {
    // Start time
    private long time;
    
    // Значения экстремумов сети
    private double hight;
    private double low;
    
    // Значения входа и выхода свечи
    private double in;
    private double out;
    
    // Объемы закрытых лотов в свече на покупки и продажи
    private double volumeBid;
    private double volumeAsk;
    
    private boolean isWrittedToSQL = false;
    
    public JapaneseCandle (long t, double i){
        this.time = t;
        this.hight = i;
        this.low = i;
        this.in = i;
        this.out = i;
        
        this.volumeBid = .0;
        this.volumeAsk = .0;
    }
    
    public boolean isIsWrittedToSQL() {
        return isWrittedToSQL;
    }
    public void markWrittedToSQL() {
        this.isWrittedToSQL = true;
    }
    
    // Геттеры
    public long getTime() {
        return this.time;
    }
    public double getHight() {
        return this.hight;
    }
    public double getLow() {
        return this.low;
    }
    public double getIn() {
        return this.in;
    }
    public double getOut() {
        return this.out;
    }
    // Установка 
    public double getVolumeBid() {
        return volumeBid;
    }
    public double getVolumeAsk() {
        return volumeAsk;
    }
    
    
    // Сеттеры
    public void update(double c) {
        if (c > hight) hight = c;
        if (c < low) low = c;
        out = c;
    }
    public void addVolumeBid(double volumeBid) {
        this.volumeBid += volumeBid;
    }
    public void addVolumeAsk(double volumeAsk) {
        this.volumeAsk += volumeAsk;
    }

    
}
