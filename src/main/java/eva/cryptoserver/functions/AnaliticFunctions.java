/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.functions;

import eva.cryptoserver.data.AnaliticData;
import eva.cryptoserver.data.Candle;
import eva.cryptoserver.data.JapaneseCandle;
import eva.cryptoserver.data.NeuralNetworkComplex;
import eva.cryptoserver.data.Period;
import eva.cryptoserver.sql.SQLQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Natali
 */
public class AnaliticFunctions {
    /* 
    Для часовых диапазонов:
    MACD:   12 24 12
    ADX:    12 12
    
    Для дневных диапазонов:
    MACD:   6 13 5
    ADX:    7 7
    */
    
    /* */
    // Оценка нейронной сетью (по методу рекурсии)
    public static boolean writeNeuron (NeuralNetworkComplex nn, int inputs, String market, Period period, SQLQuery sql) {
        
        //System.out.println(market + ": writeNeuron.");
        
        List <AnaliticData> hights = new ArrayList<>();
        List <AnaliticData> lows = new ArrayList<>();
        
        boolean result = true;
        
        long time_step = getStepByPeriod(period);
        
        long last_time_analitic_h = sql.getTimeOfLastAnaliticData(market, AnaliticType.Neuron_Hight, period);
        long last_time_analitic_l = sql.getTimeOfLastAnaliticData(market, AnaliticType.Neuron_Low, period);
        
        long last_time_candle = sql.getTimeOfLastJapaneseCandle(market, period);
        
        // На случай, если данные не равны (мало вероятно, т.к. записываются одновременно)
        long last_time_analitic = 0;
        if (last_time_analitic_h < last_time_analitic_l) {
            last_time_analitic = last_time_analitic_h;
        }
        else {
            last_time_analitic = last_time_analitic_l;
        }
        
        List<Candle> candles;
        
        if (last_time_analitic == 0) {
            candles = sql.getCandles(market, period);
            last_time_analitic = candles.get(0 + inputs -1).getTime();
        } 
        
        last_time_analitic = last_time_analitic + time_step;
        while (last_time_candle >= last_time_analitic) {
            candles = sql.getCandleLimitedBeforeTime(market, period, inputs, last_time_analitic);
            if (candles.size() == inputs) {
                Candle cndl = nn.prediction(candles);
                hights.add(new AnaliticData(last_time_analitic, cndl.getHight()));
                lows.add(new AnaliticData(last_time_analitic, cndl.getLow()));
            }
            else System.out.println("writeNeuron > " + market + ": " + candles.size() + " != " + inputs + ". " + new Date(last_time_analitic));
            
            last_time_analitic = last_time_analitic + time_step;
        }
            
        if (!sql.writeAnaliticData(market, AnaliticType.Neuron_Hight, period, hights)) {
            result = false;
        }
        if (!sql.writeAnaliticData(market, AnaliticType.Neuron_Hight, period, lows)) {
            result = false;
        }
        
        return result;
    }
    
    // расчет и запись SMA (104)
    public static boolean writeSMA (String market, Period period, AnaliticType at, SQLQuery sql) {
        
        //System.out.println(market + ": writeSMA.");
        
        int depth = 0;
        
        switch (at) {
            case SMA_5 : depth = 5; break;
            case SMA_6 : depth = 6; break;
            case SMA_7 : depth = 7; break;
            case SMA_12 : depth = 12; break;
            case SMA_13 : depth = 13; break;
            case SMA_14 : depth = 14; break;
            case SMA_28 : depth = 28; break;
            case SMA_30 : depth = 30; break;
            default: throw new RuntimeException("writeSMA is not compatible with " + at);
        }
        
        long time_step = getStepByPeriod(period);
        
        long last_time_analitic = sql.getTimeOfLastAnaliticData(market, at, period);
        long last_time_candle = sql.getTimeOfLastJapaneseCandle(market, period);
        
        List <Candle> candles;
        List <AnaliticData> aData = new ArrayList<>();
        
        if (last_time_analitic == 0) {
            last_time_analitic = sql.getTimeOfFirstJapaneseCandle(market, period) + (time_step * depth);
        } 
        
        last_time_analitic = last_time_analitic + time_step;
        while (last_time_candle >= last_time_analitic) {
            candles = sql.getCandleLimitedBeforeTime(market, period, depth, last_time_analitic);
            if (candles.size() == depth) {
                double average = 0;
                
                for (int i = 0; i < depth; i++) {
                    average = average + (candles.get(i).getHight() + candles.get(i).getLow()) / 2;
                }
                average = (average / depth);

                aData.add(new AnaliticData(last_time_analitic, average));
                
            }
            else System.out.println("writeSMA > " + market + ": " + candles.size() + " != " + depth + ". " + new Date(last_time_analitic));
            last_time_analitic = last_time_analitic + time_step;
        }
        
        return sql.writeAnaliticData(market, at, period, aData);
    }
    
    // расчет и запись WMA (105)
    public static boolean writeWMA (String market, Period period, AnaliticType at, SQLQuery sql) {
        
        //System.out.println(market + ": writeWMA.");
        
        int depth = 0;
        
        switch (at) {
            case WMA_5 : depth = 5; break;
            case WMA_6 : depth = 6; break;
            case WMA_7 : depth = 7; break;
            case WMA_12 : depth = 12; break;
            case WMA_13 : depth = 13; break;
            case WMA_14 : depth = 14; break;
            case WMA_26 : depth = 26; break;
            case WMA_28 : depth = 28; break;
            case WMA_30 : depth = 30; break;
            default: throw new RuntimeException("writeWMA is not compatible with " + at);
        }
        
        long time_step = getStepByPeriod(period);
        
        long last_time_analitic = sql.getTimeOfLastAnaliticData(market, at, period);
        long last_time_candle = sql.getTimeOfLastJapaneseCandle(market, period);
        
        List <Candle> candles;
        List <AnaliticData> aData = new ArrayList<>();
        
        if (last_time_analitic == 0) {
            last_time_analitic = sql.getTimeOfFirstJapaneseCandle(market, period) + (time_step * depth);
        } 
        
        last_time_analitic = last_time_analitic + time_step;
        while (last_time_candle >= last_time_analitic) {
            candles = sql.getCandleLimitedBeforeTime(market, period, depth, last_time_analitic);
            if (candles.size() == depth) {
                double average = 0;
                int sum_w = 0;
                for (int i = 0; i < depth; i++) {
                    average = average + (candles.get(i).getHight() + candles.get(i).getLow()) * (i+1) / 2;
                    sum_w += (i+1);
                }
                average = (average / sum_w);

                aData.add(new AnaliticData(last_time_analitic, average));
                
            }
            else System.out.println("writeWMA > " + market + ": " + candles.size() + " != " + depth + ". " + new Date(last_time_analitic));
            last_time_analitic = last_time_analitic + time_step;
        }
        
        return sql.writeAnaliticData(market, at, period, aData);
    }
    
    
    // Средний истинный диапазон ATR (111)
    public static boolean writeATR (String market, Period period, AnaliticType at, SQLQuery sql) {
        
        //System.out.println(market + ": writeATR.");
        
        int depth = 0;
        
        switch (at) {
            case ATR_7: depth = 7; break;
            case ATR_14: depth = 14 ;break;
            default: throw new RuntimeException("writeATR is not compatible with " + at);
        } 
        
        long time_step = getStepByPeriod(period);
        
        long last_time_analitic = sql.getTimeOfLastAnaliticData(market, at, period);
        long last_time_candle = sql.getTimeOfLastJapaneseCandle(market, period);
        
        List <Candle> candles;
        List <AnaliticData> aData = new ArrayList<>();
        
        if (last_time_analitic == 0) {
            last_time_analitic = sql.getTimeOfFirstJapaneseCandle(market, period) + (time_step * depth);
        } 
        
        last_time_analitic = last_time_analitic + time_step;
        while (last_time_candle >= last_time_analitic) {
            candles = sql.getCandleLimitedBeforeTime(market, period, depth, last_time_analitic);
            if (candles.size() == depth) {
                double atr = 0;
                double sum_w = 0;

                for (int i = 0; i <depth; i++) {
                    atr = atr + (candles.get(i).getHight() - candles.get(i).getLow()) * (i+1);
                    sum_w += (i+1);
                }

                atr = atr / sum_w;
                aData.add(new AnaliticData(last_time_analitic, atr));
            }
            else System.out.println("writeATR > " + market + ": " + candles.size() + " != " + depth + ". " + new Date(last_time_analitic));
            last_time_analitic = last_time_analitic + time_step;
        }
        
        return sql.writeAnaliticData(market, at, period, aData);
    }
    
    
    // Среднее квадратичное отклонение
    public static boolean writeSTDev (String market, Period period, AnaliticType at, SQLQuery sql) {
        
        //System.out.println(market + ": writeSTDev.");
        
        int depth = 0;
        
        switch (at) {
            case STDev_7: depth = 7; break;
            case STDev_14: depth = 14; break;
            case STDev_30: depth = 30; break;
            default: throw new RuntimeException("writeSTDev is not compatible with " + at);
        } 
        
        long time_step = getStepByPeriod(period);
        
        long last_time_analitic = sql.getTimeOfLastAnaliticData(market, at, period);
        long last_time_candle = sql.getTimeOfLastJapaneseCandle(market, period);
        
        List <Candle> candles;
        List <AnaliticData> aData = new ArrayList<>();
        
        if (last_time_analitic == 0) {
            last_time_analitic = sql.getTimeOfFirstJapaneseCandle(market, period) + (time_step * depth);
        } 
        
        last_time_analitic = last_time_analitic + time_step;
        while (last_time_candle >= last_time_analitic) {
            candles = sql.getCandleLimitedBeforeTime(market, period, depth, last_time_analitic);
            if (candles.size() == depth) {
                double average = 0;
                for (int i = 0; i < depth; i++) {
                    average = average + (candles.get(i).getHight() + candles.get(i).getLow()) / 2;
                }
                average = average / depth;
                
                double std = 0;
                for (int i = 0; i < depth; i++) {
                    std = std + Math.pow((average - (candles.get(i).getHight() + candles.get(i).getLow()) / 2), 2);
                }
                
                std = Math.sqrt(std / depth);
                
                aData.add(new AnaliticData(last_time_analitic, std));
            }
            else System.out.println("writeSTDev > " + market + ": " + candles.size() + " != " + depth + ". " + new Date(last_time_analitic));
            last_time_analitic = last_time_analitic + time_step;
        }
        
        return sql.writeAnaliticData(market, at, period, aData);
    }
    
    //Направленость движения (рост)
    public static boolean writeDI(String market, Period period, AnaliticType at, SQLQuery sql) {
        
        //System.out.println(market + ": writeDI.");
        
        boolean result = true;
        
        int depth = 0;
        
        AnaliticType plus;
        AnaliticType minus;
        AnaliticType dx;
        
        switch (at) {
            case DX_7 : {
                depth = 7;
                plus = AnaliticType.DIPlus_7;
                minus = AnaliticType.DIMinus_7;
                dx = AnaliticType.DX_7;
            } break;
            case DX_14 : {
                depth = 14;
                plus = AnaliticType.DIPlus_14;
                minus = AnaliticType.DIMinus_14;
                dx = AnaliticType.DX_14;
            }; break;
            default: throw new RuntimeException("writeDI is not compatible with " + at);
        } 
        
        long time_step = getStepByPeriod(period);
        
        long last_time_analitic = sql.getTimeOfLastAnaliticData(market, at, period);
        long last_time_candle = sql.getTimeOfLastJapaneseCandle(market, period);
        
        List <Candle> candles;
        List <AnaliticData> aDataPl = new ArrayList<>();
        List <AnaliticData> aDataMi = new ArrayList<>();
        List <AnaliticData> aDataDX = new ArrayList<>();
        
        if (last_time_analitic == 0) {
            last_time_analitic = sql.getTimeOfFirstJapaneseCandle(market, period) + (time_step * depth);
        } 
        
        last_time_analitic = last_time_analitic + time_step;
        while (last_time_candle >= last_time_analitic) {
            candles = sql.getCandleLimitedBeforeTime(market, period, (depth+1), last_time_analitic);
            
            List <Double> SDIPlus = new ArrayList<>();
            List <Double> SDIMinus = new ArrayList<>();
            
            if (candles.size() == (depth + 1)) {
                for (int i = 0; i < depth; i++) {
                    double dmplus = candles.get(i+1).getHight() - candles.get(i).getHight();
                    double dmminus = candles.get(i).getLow() - candles.get(i+1).getLow();

                    if (dmplus < 0) dmplus = 0;
                    if (dmminus < 0) dmminus = 0;
                    double TR = (candles.get(i+1).getHight() - candles.get(i+1).getLow());

                    if (TR != 0) {
                        SDIPlus.add(dmplus / TR);
                        SDIMinus.add(dmminus / TR);
                    }
                    else {
                        SDIPlus.add(.0);
                        SDIMinus.add(.0);
                    }
                }

                double DX = 0;
                double DIPlus = 0;
                double DIMinus = 0;
                double sum_w = 0;
                for (int i = 0; i < depth; i++) {
                    DIPlus = DIPlus + SDIPlus.get(i) * (i+1);
                    DIMinus = DIMinus + SDIMinus.get(i) * (i+1);

                    sum_w += (i+1);
                }
                
                DIPlus = DIPlus / sum_w;
                DIMinus = DIMinus / sum_w;
                
                if (DIMinus == 0 && DIPlus == 0) {
                    DX = 0;
                }
                else {
                    DX = Math.abs((DIPlus - DIMinus) / (DIPlus + DIMinus) * 100);
                }
                
                aDataPl.add(new AnaliticData(last_time_analitic, (DIPlus * 100)));
                aDataMi.add(new AnaliticData(last_time_analitic, (DIMinus * 100)));
                aDataDX.add(new AnaliticData(last_time_analitic, DX));
            }
            else System.out.println("writeDI > " + market + ": " + candles.size() + " != (" + depth + "+1). " + new Date(last_time_analitic));
            last_time_analitic = last_time_analitic + time_step;
        }
        
        if (!sql.writeAnaliticData(market, plus, period, aDataPl)) {
            result = false;
        }
        if (!sql.writeAnaliticData(market, minus, period, aDataMi)) {
            result = false;
        }
        if (!sql.writeAnaliticData(market, dx, period, aDataDX)) {
            result = false;
        }

        return result;
    }
    
    
    // Индикатор среднего направления движения ADX (110)
    public static boolean writeADX (String market, Period period, AnaliticType at, SQLQuery sql) {
        
        //System.out.println(market + ": writeADX.");
        
        int depth_dx = 0;
        
        long time_step = 0;
        
        AnaliticType dx = null;
        
        switch (at) {
            case ADX_7: {
                depth_dx = 7;
                dx = AnaliticType.DX_7;
            } break;
            case ADX_14: {
                depth_dx = 14;
                dx = AnaliticType.DX_14;
            } break;
            default: new RuntimeException("writeADX is not compatible AnaliticType: " + at);
        } 
        
        time_step = getStepByPeriod(period);
        
        long last_time_dx = sql.getTimeOfLastAnaliticData(market, dx, period);
        long last_time_analitic = sql.getTimeOfLastAnaliticData(market, at, period);
        
        List <AnaliticData> dxList;
        List <AnaliticData> aData = new ArrayList<>();
        
        if (last_time_analitic == 0) {
            last_time_analitic = sql.getTimeOfFirstAnaliticData(market, dx, period) + (time_step * depth_dx);
        }
        
        last_time_analitic = last_time_analitic + time_step;
        while (last_time_dx >= last_time_analitic) {
            dxList = sql.getAnaliticDataLimitedBeforeTime(market, dx, period, depth_dx, last_time_analitic);
            if (dxList.size() == depth_dx) {
                // Расчет ADX
                double ADX = 0;
                double sum_w = 0;
                for (int i = 0; i < depth_dx; i++) {
                    ADX = ADX + dxList.get(i).getValue() * (i+1);

                    sum_w += (i+1);
                }
                ADX = ADX / sum_w;

                aData.add(new AnaliticData(last_time_analitic, ADX));
                
            }
            else System.out.println("writeADX > " + market + ": " + dxList.size() + " != " + depth_dx + ". " + new Date(last_time_analitic));
            last_time_analitic = last_time_analitic + time_step;
        }
        
        return sql.writeAnaliticData(market, at, period, aData);
    }
    
    // Индикатор MACD (107)
    public static boolean writeMACD (String market, Period period, AnaliticType at, SQLQuery sql) {
        
        //System.out.println(market + ": writeMACD.");
        
        boolean result = true;
        
        long time_step = 0;
        
        AnaliticType wma_long = null; 
        AnaliticType wma_short = null;
        AnaliticType signal_at = null;
        
        int period_different = 0;
        
        switch (at) {
            case MACD_6_13_5: {
                wma_short = AnaliticType.WMA_6;
                wma_long = AnaliticType.WMA_13; 
                signal_at = AnaliticType.MACD_6_13_5_Signal;
                period_different = 5; 
            } break;
            case MACD_12_26_9: {
                wma_short = AnaliticType.WMA_12;
                wma_long = AnaliticType.WMA_26; 
                signal_at = AnaliticType.MACD_12_26_9_Signal;
                period_different = 9; 
            } break;
            default: new RuntimeException("writeMACD is not compatible with " + at);
        } 
        
        time_step = getStepByPeriod(period);
        
        long last_time_analitic = sql.getTimeOfLastAnaliticData(market, at, period);
        long last_time_long = sql.getTimeOfLastAnaliticData(market, wma_long, period);
        long last_time_short = sql.getTimeOfLastAnaliticData(market, wma_short, period);
        
        long last_time = 0;
        
        if (last_time_long < last_time_short) {
            last_time = last_time_long;
        }
        else {
            last_time = last_time_short;
        }
        
        List <AnaliticData> shortList;
        List <AnaliticData> longList;
        // MACD является разницей между скользящими средними и сигнальной линией по графику этой разницы
        List <AnaliticData> aData_Signal = new ArrayList<>();
        List <AnaliticData> aData_Diff = new ArrayList<>();
        
        if (last_time_analitic == 0) {
            last_time_analitic = sql.getTimeOfFirstAnaliticData(market, wma_short, period) + (time_step * period_different);
        } 
        
        
        last_time_analitic = last_time_analitic + time_step;
        while (last_time >= last_time_analitic) {
            shortList = sql.getAnaliticDataLimitedBeforeTime(market, wma_short, period, period_different, last_time_analitic);
            longList = sql.getAnaliticDataLimitedBeforeTime (market, wma_long, period, period_different, last_time_analitic);
            
            if (shortList.size() == period_different && longList.size() == period_different) {
                double signal = 0;
                double sum_w = 0;

                for (int i = 0; i <period_different; i++) {
                    signal = signal + (shortList.get(i).getValue() - longList.get(i).getValue()) * (i+1);
                    sum_w += (i+1);
                }
                
                signal = signal / sum_w;
                aData_Signal.add(new AnaliticData(last_time_analitic, signal));
                aData_Diff.add(new AnaliticData( last_time_analitic, (shortList.get(shortList.size() -1).getValue() - longList.get(longList.size() -1).getValue()) ));
            }
            else System.out.println("writeMACD > " + market + ": (" + shortList.size() + " | " + longList.size() + ") != " + period_different + ". " + new Date(last_time_analitic));
            last_time_analitic = last_time_analitic + time_step;
        }
        
        if (!sql.writeAnaliticData(market, signal_at, period, aData_Signal)) {
            result = false;
        }
        if (!sql.writeAnaliticData(market, at, period, aData_Diff)) {
            result = false;
        }
        
        return result;
    }
    
    // Уровни Боллинжера
    public static boolean writeBollinger (String market, Period period, AnaliticType at, SQLQuery sql) {
        
        //System.out.println(market + ": writeBollinger.");
        
        boolean result = true;
        
        long time_step = 0;
        
        AnaliticType sma = null; 
        AnaliticType std = null;
        
        AnaliticType bllngr_hi = null;
        AnaliticType bllngr_lo = null;
        
        switch (at) {
            case Bollinger_7_Hight:
            case Bollinger_7_Low:{
                sma = AnaliticType.SMA_7;
                std = AnaliticType.STDev_7; 
                bllngr_hi = AnaliticType.Bollinger_7_Hight;
                bllngr_lo = AnaliticType.Bollinger_7_Low;
            } break;
            case Bollinger_14_Hight:
            case Bollinger_14_Low: {
                sma = AnaliticType.SMA_14;
                std = AnaliticType.STDev_14; 
                bllngr_hi = AnaliticType.Bollinger_14_Hight;
                bllngr_lo = AnaliticType.Bollinger_14_Low;
            } break;
            case Bollinger_30_Hight:
            case Bollinger_30_Low: {
                sma = AnaliticType.SMA_30;
                std = AnaliticType.STDev_30; 
                bllngr_hi = AnaliticType.Bollinger_30_Hight;
                bllngr_lo = AnaliticType.Bollinger_30_Low;
            } break;
            default: new RuntimeException("writeBollinger is not compatible with " + at);
        } 
        
        time_step = getStepByPeriod(period);
        
        long last_time_analitic = sql.getTimeOfLastAnaliticData(market, at, period);
        long last_time_long = sql.getTimeOfLastAnaliticData(market, sma, period);
        long last_time_short = sql.getTimeOfLastAnaliticData(market, std, period);
        
        long last_time = 0;
        
        if (last_time_long < last_time_short) {
            last_time = last_time_long;
        }
        else {
            last_time = last_time_short;
        }
        
        List <AnaliticData> smaList;
        List <AnaliticData> stdList;
        List <AnaliticData> aData_hi = new ArrayList<>();
        List <AnaliticData> aData_lo = new ArrayList<>();
        
        if (last_time_analitic == 0) {
            last_time_analitic = sql.getTimeOfFirstAnaliticData(market, std, period) + (time_step);
        } 
        
        last_time_analitic = last_time_analitic + time_step;
        while (last_time >= last_time_analitic) {
            smaList = sql.getAnaliticDataLimitedBeforeTime(market, sma, period, 1, last_time_analitic);
            stdList = sql.getAnaliticDataLimitedBeforeTime (market, std, period, 1, last_time_analitic);
            
            if (smaList.size() == 1 && stdList.size() == 1) {
                aData_hi.add(new AnaliticData(last_time_analitic, (smaList.get(0).getValue() + 2 * stdList.get(0).getValue())));
                aData_lo.add(new AnaliticData(last_time_analitic, (smaList.get(0).getValue() - 2 * stdList.get(0).getValue())));
            }
            else System.out.println("writeBollinger > " + market + ": (" + smaList.size() + " | " + stdList.size() + ") != 1. " + new Date(last_time_analitic));
            last_time_analitic = last_time_analitic + time_step;
        }
        
        if (!sql.writeAnaliticData(market, bllngr_hi, period, aData_hi)) {
            result = false;
        }
        if (!sql.writeAnaliticData(market, bllngr_lo, period, aData_lo)) {
            result = false;
        }
        
        return result;
    }
    
    
    // Уровень RSI
    public static boolean writeRSI (String market, Period period, AnaliticType at, SQLQuery sql) {
        return true;
    }
    
    // Стахостический осцилятор (115)
    public static boolean writeStochasticOscillator (String market, Period period, AnaliticType at, SQLQuery sql) {
        return true;
    }
    
    // Ишимоку Кинко Хайо (117)
    public static boolean writeIchimokuKinkoHyo (String market, Period period, AnaliticType at, SQLQuery sql) {
        return true;
    }
    
    // Прогноз временных рядов TSF (147)
    public static boolean writeTSF (String market, Period period, AnaliticType at, SQLQuery sql) {
        return true;
    }
    
    // Метод опорных точек PP (149)
    public static boolean writePP (String market, Period period, AnaliticType at, SQLQuery sql) {
        return true;
    }
    
    // Канал стандартных отклонений SDC (220)
    public static boolean writeSDC (String market, Period period, AnaliticType at, SQLQuery sql) {
        return true;
    }
    
    // Адаптивный стахостический осцилятор ASO
    public static boolean writeASO (String market, Period period, AnaliticType at, SQLQuery sql) {
        return true;
    }
    
    
    
    //////////////// ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ //////////////// 
    ///////////////////////////////////////////////////////// 
    private static long getStepByPeriod(Period p) {
        long time_step = 0;
        switch (p) {
            case DAY: {
                time_step = 24L * 60 * 60 * 1000;
            } break;
            case HOUR: {
                time_step = 60L * 60 * 1000;
            } break;
            default: new RuntimeException("Period doesn't compatible " + p);
        } 
        
        return time_step;
    }
}
