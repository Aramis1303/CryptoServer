/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.threads;

import eva.cryptoserver.Status;
import eva.cryptoserver.data.Candle;
import eva.cryptoserver.data.JapaneseCandle;
import eva.cryptoserver.data.NeuralNetworkComplex;
import eva.cryptoserver.functions.AnaliticFunctions;
import eva.cryptoserver.functions.AnaliticType;
import eva.cryptoserver.functions.ProcessingFunctions;
import eva.cryptoserver.data.Period;
import eva.cryptoserver.sql.SQLQuery;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author username
 */
public class ThreadForDataProcessing implements Runnable{
    
    private Map <String, Status> markets;
    private Thread process;
    private SQLQuery sql;
    private TraderProcess tp;
    
    private boolean isStoped = false;
    
    private boolean calculateAggregation = false;
    
    private boolean calculateNEUNET = false;
    private boolean calculateSMA = false;
    private boolean calculateWMA = false;
    private boolean calculateATR = false;
    private boolean calculateSTDev = false;
    private boolean calculateDI = false;
    private boolean calculateBollinger = false;
    private boolean calculateADX = false;
    private boolean calculateMACD = false;
    
    private int INPUTS_LENGHT_OF_NEURONS = 60;
    private final String PATH_1D = System.getProperty("user.dir") + System.getProperty("file.separator") + "nn_d" + System.getProperty("file.separator");
    private final String PATH_1H = System.getProperty("user.dir") + System.getProperty("file.separator") + "nn_h" + System.getProperty("file.separator");
    private final String FILE_NEUNET = "neunet_comlex.zip";
    private final String FILE_NORMAL = "normal_comlex.zip";
    
    public ThreadForDataProcessing (Map <String, Status> markets, SQLQuery sql, TraderProcess tp) {
        this.markets = markets;
        this.sql = sql;
        this.tp = tp;
        
        process = new Thread (this);
        process.setName("DataProcessing");
        process.start();
    }
    
    @Override
    public void run() {
        while (!isStoped) {
            
            ///////////////// ДНЕВНЫЕ СВЕЧИ /////////////////
            /////////////////////////////////////////////////
            synchronized (markets) {    
                for (Map.Entry <String, Status> entry: markets.entrySet()) {
                    if (entry.getValue().equals(Status.WORKING)) {
                                               
                        // -- ОЧЕРЕДЬ №1 -- //
                        // Агрегирование свечей
                        if (calculateAggregation){
                            if (sql.getTimeOfLastJapaneseCandle(entry.getKey(), Period.DAY) + (24 * 60 * 60 * 1000) < new Date().getTime()) {
                                ProcessingFunctions.aggregateCandles1D(entry.getKey(), sql);
                            }
                        }
                        
                        // -- ОЧЕРЕДЬ №5 -- //
                        // ПРОВЕРКА НА ПРОПУЩЕННЫЕ ДАННЫЕ
                        
                        
                        
                        // Аналитика
                        // -- ОЧЕРЕДЬ №6 -- //
                        //Neuron Network
                        if (calculateNEUNET) {
                            if (new File(PATH_1D + FILE_NEUNET).exists()) {
                                NeuralNetworkComplex nn = null;
                                
                                try {
                                    nn = new NeuralNetworkComplex(INPUTS_LENGHT_OF_NEURONS, PATH_1D + FILE_NEUNET);
                                    nn.loadNormalizer(PATH_1D + FILE_NORMAL);
                                    System.out.println("Комплекс-модель загружена из файла.");
                                } catch (IOException ex) {
                                    System.out.println(ex);
                                } catch (Exception ex) {
                                    System.out.println(ex);
                                }
                                
                                if (nn != null) {
                                    AnaliticFunctions.writeNeuron(nn, INPUTS_LENGHT_OF_NEURONS, entry.getKey(), Period.DAY, sql);
                                }
                            }
                        }
                        
                        //SMA
                        if (calculateSMA) {
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.DAY, AnaliticType.SMA_5, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.DAY, AnaliticType.SMA_6, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.DAY, AnaliticType.SMA_7, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.DAY, AnaliticType.SMA_12, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.DAY, AnaliticType.SMA_13, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.DAY, AnaliticType.SMA_14, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.DAY, AnaliticType.SMA_28, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.DAY, AnaliticType.SMA_30, sql);
                        }
                        
                        //WMA
                        if (calculateWMA) {
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.DAY, AnaliticType.WMA_5, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.DAY, AnaliticType.WMA_6, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.DAY, AnaliticType.WMA_7, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.DAY, AnaliticType.WMA_12, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.DAY, AnaliticType.WMA_13, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.DAY, AnaliticType.WMA_14, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.DAY, AnaliticType.WMA_26, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.DAY, AnaliticType.WMA_28, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.DAY, AnaliticType.WMA_30, sql);
                        } 
                        
                        //ATR
                        if (calculateATR) {
                            AnaliticFunctions.writeATR(entry.getKey(), Period.DAY, AnaliticType.ATR_7, sql);
                            AnaliticFunctions.writeATR(entry.getKey(), Period.DAY, AnaliticType.ATR_14, sql);
                        } 
                        
                        //STDev
                        if (calculateSTDev) {
                            AnaliticFunctions.writeSTDev(entry.getKey(), Period.DAY, AnaliticType.STDev_7, sql);
                            AnaliticFunctions.writeSTDev(entry.getKey(), Period.DAY, AnaliticType.STDev_14, sql);
                            AnaliticFunctions.writeSTDev(entry.getKey(), Period.DAY, AnaliticType.STDev_30, sql);
                        }
                        
                        // -- ОЧЕРЕДЬ №7 -- //
                        //DI PLUS / DI MINUS / DX
                        if (calculateDI) {
                            AnaliticFunctions.writeDI(entry.getKey(), Period.DAY, AnaliticType.DX_7, sql);
                            AnaliticFunctions.writeDI(entry.getKey(), Period.DAY, AnaliticType.DX_14, sql);
                        }
                        
                        //Bollinger
                        if (calculateBollinger) {
                            AnaliticFunctions.writeBollinger(entry.getKey(), Period.DAY, AnaliticType.Bollinger_7_Hight, sql);
                            AnaliticFunctions.writeBollinger(entry.getKey(), Period.DAY, AnaliticType.Bollinger_14_Hight, sql);
                            AnaliticFunctions.writeBollinger(entry.getKey(), Period.DAY, AnaliticType.Bollinger_30_Hight, sql);
                        }
                        
                        // -- ОЧЕРЕДЬ №8 -- //
                        //ADX
                        if (calculateADX) {
                            AnaliticFunctions.writeADX(entry.getKey(), Period.DAY, AnaliticType.ADX_7, sql);
                            AnaliticFunctions.writeADX(entry.getKey(), Period.DAY, AnaliticType.ADX_14, sql);
                        }
                        
                        //MACD
                        if (calculateMACD) {
                            AnaliticFunctions.writeMACD(entry.getKey(), Period.DAY, AnaliticType.MACD_6_13_5, sql);
                            AnaliticFunctions.writeMACD(entry.getKey(), Period.DAY, AnaliticType.MACD_12_26_9, sql);
                        } 
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ThreadForDataProcessing.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            ///////////////// ЧАСОВЫЕ СВЕЧИ /////////////////
            /////////////////////////////////////////////////
            synchronized (markets) {
                for (Map.Entry <String, Status> entry: markets.entrySet()) {
                    if (entry.getValue().equals(Status.WORKING)) {
                        // -- ОЧЕРЕДЬ №1 -- //
                        // Агрегирование свечей
                        if (calculateAggregation) {
                            if (sql.getTimeOfLastJapaneseCandle(entry.getKey(), Period.HOUR) + (60 * 60 * 1000) < new Date().getTime()) {
                                ProcessingFunctions.aggregateCandles1H(entry.getKey(), sql);
                            }
                        }
                        
                        // -- ОЧЕРЕДЬ №5 -- //
                        // ПРОВЕРКА НА ПРОПУЩЕННЫЕ ДАННЫЕ
                        
                        
                        
                        // Аналитика
                        // -- ОЧЕРЕДЬ №6 -- //
                        //Neuron Network
                        if (calculateNEUNET) {
                            if (new File(PATH_1H + FILE_NEUNET).exists()) {
                                NeuralNetworkComplex nn = null;
                                
                                try {
                                    nn = new NeuralNetworkComplex(INPUTS_LENGHT_OF_NEURONS, PATH_1H + FILE_NEUNET);
                                    nn.loadNormalizer(PATH_1H + entry.getKey() + FILE_NORMAL);
                                    System.out.println("Комплекс-модель загружена из файла.");
                                } catch (IOException ex) {
                                    System.out.println(ex);
                                } catch (Exception ex) {
                                    System.out.println(ex);
                                }
                                
                                if (nn != null) {
                                    AnaliticFunctions.writeNeuron(nn, INPUTS_LENGHT_OF_NEURONS, entry.getKey(), Period.HOUR, sql);
                                }
                            }
                        }
                        //SMA
                        if (calculateSMA) {
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.HOUR, AnaliticType.SMA_5, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.HOUR, AnaliticType.SMA_6, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.HOUR, AnaliticType.SMA_7, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.HOUR, AnaliticType.SMA_12, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.HOUR, AnaliticType.SMA_13, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.HOUR, AnaliticType.SMA_14, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.HOUR, AnaliticType.SMA_28, sql);
                            AnaliticFunctions.writeSMA(entry.getKey(), Period.HOUR, AnaliticType.SMA_30, sql);
                        }
                        
                        //WMA
                        if (calculateWMA) {
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.HOUR, AnaliticType.WMA_5, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.HOUR, AnaliticType.WMA_6, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.HOUR, AnaliticType.WMA_7, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.HOUR, AnaliticType.WMA_12, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.HOUR, AnaliticType.WMA_13, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.HOUR, AnaliticType.WMA_14, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.HOUR, AnaliticType.WMA_26, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.HOUR, AnaliticType.WMA_28, sql);
                            AnaliticFunctions.writeWMA(entry.getKey(), Period.HOUR, AnaliticType.WMA_30, sql);
                        } 
                        
                        //ATR
                        if (calculateATR) {
                            AnaliticFunctions.writeATR(entry.getKey(), Period.HOUR, AnaliticType.ATR_7, sql);
                            AnaliticFunctions.writeATR(entry.getKey(), Period.HOUR, AnaliticType.ATR_14, sql);
                        } 
                        
                        //STDev
                        if (calculateSTDev) {
                            AnaliticFunctions.writeSTDev(entry.getKey(), Period.HOUR, AnaliticType.STDev_7, sql);
                            AnaliticFunctions.writeSTDev(entry.getKey(), Period.HOUR, AnaliticType.STDev_14, sql);
                            AnaliticFunctions.writeSTDev(entry.getKey(), Period.HOUR, AnaliticType.STDev_30, sql);
                        }
                        
                        // -- ОЧЕРЕДЬ №7 -- //
                        //DI PLUS / DI MINUS / DX
                        if (calculateDI) {
                            AnaliticFunctions.writeDI(entry.getKey(), Period.HOUR, AnaliticType.DX_7, sql);
                            AnaliticFunctions.writeDI(entry.getKey(), Period.HOUR, AnaliticType.DX_14, sql);
                        }
                        
                        //Bollinger
                        if (calculateBollinger) {
                            AnaliticFunctions.writeBollinger(entry.getKey(), Period.HOUR, AnaliticType.Bollinger_7_Hight, sql);
                            AnaliticFunctions.writeBollinger(entry.getKey(), Period.HOUR, AnaliticType.Bollinger_14_Hight, sql);
                            AnaliticFunctions.writeBollinger(entry.getKey(), Period.HOUR, AnaliticType.Bollinger_30_Hight, sql);
                        }
                        
                        // -- ОЧЕРЕДЬ №8 -- //
                        //ADX
                        if (calculateADX) {
                            AnaliticFunctions.writeADX(entry.getKey(), Period.HOUR, AnaliticType.ADX_7, sql);
                            AnaliticFunctions.writeADX(entry.getKey(), Period.HOUR, AnaliticType.ADX_14, sql);
                        }
                        
                        //MACD
                        if (calculateMACD) {
                            AnaliticFunctions.writeMACD(entry.getKey(), Period.HOUR, AnaliticType.MACD_6_13_5, sql);
                            AnaliticFunctions.writeMACD(entry.getKey(), Period.HOUR, AnaliticType.MACD_12_26_9, sql);
                        }
                        
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ThreadForDataProcessing.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ThreadForDataProcessing.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void stop() {
        isStoped = true;
    }
    
    // SET/GET

    public void setCalculateAggregation(boolean calculateAggregation) {
        this.calculateAggregation = calculateAggregation;
    }

    public void setCalculateNEUNET(boolean calculateNEUNET) {
        this.calculateNEUNET = calculateNEUNET;
    }

    public void setCalculateSMA(boolean calculateSMA) {
        this.calculateSMA = calculateSMA;
    }

    public void setCalculateWMA(boolean calculateWMA) {
        this.calculateWMA = calculateWMA;
    }

    public void setCalculateATR(boolean calculateATR) {
        this.calculateATR = calculateATR;
    }
    
    public void setCalculateSTDev(boolean calculateSTDev) {
        this.calculateSTDev = calculateSTDev;
    }
    
    public void setCalculateDI(boolean calculateDI) {
        this.calculateDI = calculateDI;
    }
    
    public void setCalculateBollinger(boolean calculateBollinger) {
        this.calculateBollinger = calculateBollinger;
    }
    
    public void setCalculateADX(boolean calculateADX) {
        this.calculateADX = calculateADX;
    }

    public void setCalculateMACD(boolean calculateMACD) {
        this.calculateMACD = calculateMACD;
    }
    
}
