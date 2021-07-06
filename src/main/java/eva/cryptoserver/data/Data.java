/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.data;

import eva.cryptoserver.Status;
import eva.cryptoserver.data.JapaneseCandle;
import eva.cryptoserver.data.OrderOfHistory;
import eva.cryptoserver.data.QuickData;
import eva.cryptoserver.data.SummaryInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/*
 * @author Ermolenko Vadim
 */
public class Data {
    
    // Market name
    private String name;
    // Burse name
    private String burse;
    
    // History book of transactions
    private List <OrderOfHistory> buyHistory;
    private List <OrderOfHistory> sellHistory;
    
    // Last, Bid, Ask
    private List <QuickData> quickData;
    // Hight, Low, Volume
    private List <SummaryInfo> summaryInfo;
    // Japan candles
    private List <JapaneseCandle> candleBy1m;
    
    // STATUS: {DELETED | WORKING | STOPED}
    private Status status;
    
    public boolean lastSqlConnection;
    public boolean lastUrlConnection;
    
    public Data (String name, String burse) {
        
        this.name = name;
        this.burse = burse;
        
        // Создаем синхронизированные экземпляры
        buyHistory = Collections.synchronizedList(new ArrayList <>());
        sellHistory = Collections.synchronizedList(new ArrayList <>());
        
        quickData = Collections.synchronizedList(new ArrayList <>());

        summaryInfo = Collections.synchronizedList(new ArrayList <>());

        candleBy1m = Collections.synchronizedList(new ArrayList <>());
    }    
    
    // Геттеры
    public List <OrderOfHistory> getBuyHistory () {
        return buyHistory;
    }
    public List <OrderOfHistory> getSellHistory () {
        return sellHistory;
    }
    
    public List <QuickData> getQuickData () {
        return quickData;
    }
    
    public List <SummaryInfo> getSummaryInfo () {
        return summaryInfo;
    }
    
    public String getBurse() {
        return burse;
    }
    public String getName () {
        return this.name;
    }
    
    // gets of candles
    public List <JapaneseCandle> getCandleBy1m () {
        return candleBy1m;
    }
    
    // Invalid market
    public boolean isInvalidMarket() {
        return status.equals(Status.DELETED);
    }
    public void setInvalidMarket(boolean invalidMarket) {
        status = Status.DELETED;
    }
    
    
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    
    
    // Creating finished data of candles
    public void finishinCandle (JapaneseCandle jc, int minutes) {
        
        long start = jc.getTime();
        long end = start + (minutes * 60 * 1000) -1;
        
        synchronized(this) {
            // Перебираем даты от ближайшей к прошлой
            for (int i = buyHistory.size() - 1; i >= 0; i--){
                // Прерываем перебор если дошли до точки отсчета
                if (buyHistory.get(i).getTime() < start) 
                    break;
                // суммируем все даты, что меньше окончания расчетного периуда и больше даты начала периуда
                if (buyHistory.get(i).getTime() < end){
                    
                    jc.addVolumeBid(buyHistory.get(i).getVolume());
                }
                
                jc.update(buyHistory.get(i).getPrice());
            }
            
            
            // Перебираем даты от ближайшей к прошлой
            for (int i = sellHistory.size() - 1; i >= 0; i--){
                // Прерываем перебор если дошли до точки отсчета
                if (sellHistory.get(i).getTime() < start) 
                    break;
                // суммируем все даты, что меньше окончания расчетного периуда и больше даты начала периуда
                if (sellHistory.get(i).getTime() < end){
                    jc.addVolumeAsk(sellHistory.get(i).getVolume());
                }
                
                jc.update(sellHistory.get(i).getPrice());
            }
        }
    }
    
    
    // Обновление данных в свечах
    public void updateCandles (QuickData qd){
        //
        try {
            // Общемы закрытых сделок на покупку и продажу (History)
            long tm;
            
            synchronized (this){
                // ***** Формируем 1-минутные свечи *****
                
                // Обрезаем время до начала минуты
                tm = qd.getTime() / (60 * 1000);
                tm = tm * (60 * 1000);
                
                // Если первая свеча
                if (this.getCandleBy1m().isEmpty()) {
                    JapaneseCandle jc = new JapaneseCandle (tm, qd.getLast());
                    this.getCandleBy1m().add(jc);
                }
                // 
                else if (this.getCandleBy1m().get(this.getCandleBy1m().size() - 1).getTime() < tm) { // Если время последней свечи меньше обрезанного tm, то создаем новую свечу
                    JapaneseCandle jc;
                    // Записываем последнюю свечу из коллекции
                    jc = this.getCandleBy1m().get(this.getCandleBy1m().size() - 1);
                    // Выполняем эти функции имено в минутной свече при создании новой, и ищем объемы за мунуту, 
                    // далее добавляем эти объемы в свечи других диапазонов при новых свечах и при обновлении старых
                    finishinCandle(jc, 1);
                    
                    // Добавляем новую свечу в коллекцию
                    jc = new JapaneseCandle (tm, qd.getLast());
                    this.getCandleBy1m().add(jc);
                }
                else if (this.getCandleBy1m().get(this.getCandleBy1m().size()-1).getTime() == tm) { // Если равно, то обновляем старую свечу
                    JapaneseCandle jc = this.getCandleBy1m().get(this.getCandleBy1m().size()-1);
                    jc.update(qd.getLast());            
                }
            } 
        }catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }
}
