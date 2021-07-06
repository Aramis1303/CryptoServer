/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.functions;

import eva.cryptoserver.data.Candle;
import eva.cryptoserver.data.JapaneseCandle;
import eva.cryptoserver.data.Period;
import eva.cryptoserver.sql.SQLQuery;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author username
 */
public class ProcessingFunctions {
    
    public static boolean aggregateCandles1H (String market, SQLQuery sql) {
        // Запись часовых свечей
        long limit_1h = sql.getTimeOfLastJapaneseCandle(market, Period.MINUTE);
        limit_1h = limit_1h / (60 * 60 * 1000);
        limit_1h = limit_1h * (60 * 60 * 1000);
        limit_1h -= 1;
        long last_1h = sql.getTimeOfLastJapaneseCandle(market, Period.HOUR);
        long step_1h = (60 * 60 * 1000);
        
        if (last_1h != 0) {
            last_1h += step_1h;
            Set <Candle> candles = new HashSet<>();
            
            while (last_1h < limit_1h) {
                List <JapaneseCandle> listJc = sql.getJapaneseCandleByTimePeriod(market, Period.MINUTE, last_1h, last_1h + step_1h -1);
                if (listJc != null && !listJc.isEmpty()) {
                    JapaneseCandle jCandle = new JapaneseCandle(last_1h, listJc.get(0).getIn());
                    jCandle.update(listJc.get(0).getHight());
                    jCandle.update(listJc.get(0).getLow());
                    jCandle.update(listJc.get(0).getOut());
                    jCandle.addVolumeAsk(listJc.get(0).getVolumeAsk());
                    jCandle.addVolumeBid(listJc.get(0).getVolumeBid());

                    for (int i = 1; i < listJc.size(); i++) {
                        jCandle.update(listJc.get(i).getIn());
                        jCandle.update(listJc.get(i).getHight());
                        jCandle.update(listJc.get(i).getLow());
                        jCandle.update(listJc.get(i).getOut());
                        jCandle.addVolumeAsk(listJc.get(i).getVolumeAsk());
                        jCandle.addVolumeBid(listJc.get(i).getVolumeBid());
                    }

                    Candle c = new Candle  (
                        jCandle.getTime(),
                        jCandle.getHight(),
                        jCandle.getLow(),
                        jCandle.getIn(),
                        jCandle.getOut(),
                        jCandle.getVolumeAsk() + jCandle.getVolumeBid()
                    );

                    candles.add(c);
                }

                last_1h += step_1h;
            }
            
            if (!sql.writeCandles(candles, market, Period.HOUR)){
                return false;
            }
        }
        return true;
    }
    
    public static boolean aggregateCandles1D (String market, SQLQuery sql) {
        // Запись часовых свечей
        long limit_1d = sql.getTimeOfLastJapaneseCandle(market, Period.MINUTE);
        limit_1d = limit_1d / (24 * 60 * 60 * 1000);
        limit_1d = limit_1d * (24 * 60 * 60 * 1000);
        limit_1d -= 1;
        long last_1d = sql.getTimeOfLastJapaneseCandle(market, Period.DAY);
        long step_1d = (24 * 60 * 60 * 1000);
        
        if (last_1d != 0) {
            last_1d += step_1d;
            Set <Candle> candles = new HashSet<>();
            while (last_1d < limit_1d) {
                List <JapaneseCandle> listJc = sql.getJapaneseCandleByTimePeriod(market, Period.MINUTE, last_1d, last_1d + step_1d -1);
                if (listJc != null && !listJc.isEmpty()) {
                    JapaneseCandle jCandle = new JapaneseCandle(last_1d, listJc.get(0).getIn());
                    jCandle.update(listJc.get(0).getHight());
                    jCandle.update(listJc.get(0).getLow());
                    jCandle.update(listJc.get(0).getOut());
                    jCandle.addVolumeAsk(listJc.get(0).getVolumeAsk());
                    jCandle.addVolumeBid(listJc.get(0).getVolumeBid());

                    for (int i = 1; i < listJc.size(); i++) {
                        jCandle.update(listJc.get(i).getIn());
                        jCandle.update(listJc.get(i).getHight());
                        jCandle.update(listJc.get(i).getLow());
                        jCandle.update(listJc.get(i).getOut());
                        jCandle.addVolumeAsk(listJc.get(i).getVolumeAsk());
                        jCandle.addVolumeBid(listJc.get(i).getVolumeBid());
                    }

                    Candle c = new Candle  (
                        jCandle.getTime(),
                        jCandle.getHight(),
                        jCandle.getLow(),
                        jCandle.getIn(),
                        jCandle.getOut(),
                        jCandle.getVolumeAsk() + jCandle.getVolumeBid()
                    );

                    candles.add(c);
                }

                last_1d += step_1d;
            }
            
            if (!sql.writeCandles(candles, market, Period.DAY)) {
                return false;
            }
        }
        return true;
        
    }
    
}
