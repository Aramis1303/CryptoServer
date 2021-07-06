/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.sql;

import eva.cryptoserver.data.Period;
import eva.cryptoserver.Status;
import eva.cryptoserver.data.AnaliticData;
import eva.cryptoserver.data.Candle;
import eva.cryptoserver.data.JapaneseCandle;
import eva.cryptoserver.data.QuickData;
import eva.cryptoserver.data.SummaryInfo;
import eva.cryptoserver.functions.AnaliticType;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author username
 */
public class SQLQuery {
    // JDBC URL, username and password of MySQL server
    private static String url;
    
    private static String user;
    private static String password;
    
    private Statement stmt;
    
    public SQLQuery(String db) {
        
        //url = "jdbc:mariadb://79.120.44.138:33306/" + db;
        url = "jdbc:mariadb://192.168.0.9:3306/" + db;
        user = "root";
        password = "[htydfv1303";
        
        try {
            // MariaDB
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            System.out.println(SQLQuery.class.getName() + " -> " + ex);
        }
        
        try {
            stmt = DriverManager.getConnection(url + "?useSSL=false", user, password).createStatement();
        }
        catch (Exception e) {
            stmt = null;
        }
        
    }
    // Записать список свечей
    public synchronized boolean writeJapaneseCandles(List <JapaneseCandle> jc, String tbl, Period p){
        
        if (jc.size() < 2) return false;
        
        String period = null;
            
        switch (p) {
            case DAY: period = "1_day"; break;
            case HOUR: period = "1_hour"; break;
            case MINUTE: period = "1_min"; break;
            default: new RuntimeException (p + " doesn't support yet.");
        }

        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + tbl + "_candles_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `in` DOUBLE, `out` DOUBLE, `low` DOUBLE,`hight` DOUBLE, `volume` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            // Последняя свеча не сформирована, по этому её не записываем
            for (int i = 0; i < jc.size() - 1; i++) {
                try {
                    // time, in, out, hight, low, vask, vbid
                    if (!jc.get(i).isIsWrittedToSQL()) {
                        stmt.execute("INSERT INTO `" + tbl + "_candles_" + period + "` (`time`, `in`, `out`, `low`, `hight`, `volume`) VALUES (" +
                                jc.get(i).getTime() + ", " +
                                jc.get(i).getIn()+ ", " +
                                jc.get(i).getOut()+ ", " +
                                jc.get(i).getLow()+ ", " +
                                jc.get(i).getHight()+ ", " +
                                (jc.get(i).getVolumeAsk() + jc.get(i).getVolumeBid()) + ");"
                        );

                        jc.get(i).markWrittedToSQL();
                    }
                } catch (SQLIntegrityConstraintViolationException ex) {
                    continue;
                } 
            } 
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " writeJapaneseCandles -> " + ex);
            return false;
        }
        return true;
    }
    
    public synchronized boolean writeCandles(Set <Candle> jc, String tbl, Period p) {
        
        if (jc.size() < 2) return false;
        
        String period = null;
            
        switch (p) {
            case DAY: period = "1_day"; break;
            case HOUR: period = "1_hour"; break;
            case MINUTE: period = "1_min"; break;
            default: new RuntimeException (p + " doesn't support yet.");
        }
        
        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + tbl + "_candles_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `in` DOUBLE, `out` DOUBLE, `low` DOUBLE,`hight` DOUBLE, `volume` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            // Последняя свеча не сформирована, по этому её не записываем
            for (Candle c: jc) { 

                try {
                    // time, in, out, low, hight, volume
                    stmt.execute("INSERT INTO `" + tbl + "_candles_" + period + "` (`time`, `in`, `out`, `low`, `hight`, `volume`) VALUES (" +
                            c.getTime() + ", " +
                            c.getIn()+ ", " +
                            c.getOut()+ ", " +
                            c.getLow()+ ", " +
                            c.getHight()+ ", " +
                            c.getVolume()+ ");"
                    );
                } catch (SQLIntegrityConstraintViolationException ex) {
                    continue;
                }
            }
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " writeJapaneseCandles -> " + ex);
            return false;
        }
        return true;
    }
    
    // Взять время последней свечи
    public synchronized long getTimeOfLastJapaneseCandle(String tbl, Period p) {
        
        String period = null;
            
        switch (p) {
            case DAY: period = "1_day"; break;
            case HOUR: period = "1_hour"; break;
            case MINUTE: period = "1_min"; break;
            default: new RuntimeException (p + " doesn't support yet.");
        }
        
        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + tbl + "_candles_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `in` DOUBLE, `out` DOUBLE, `low` DOUBLE,`hight` DOUBLE, `volume` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            ResultSet rs = stmt.executeQuery("SELECT `time` FROM `" + tbl + "_candles_" + period + "` ORDER BY `time` DESC LIMIT 1;");
            
            if (rs != null) {
                if(rs.next()) {
                    stmt.close();
                    return rs.getLong("time");
                }
            }
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " getTimeOfLastJapaneseCandle -> " + ex);
        }
        return 0;
    }
    
    // Взять время первой свечи
    public synchronized long getTimeOfFirstJapaneseCandle(String tbl, Period p) {
        
        String period = null;
            
        switch (p) {
            case DAY: period = "1_day"; break;
            case HOUR: period = "1_hour"; break;
            case MINUTE: period = "1_min"; break;
            default: new RuntimeException (p + " doesn't support yet.");
        }
        
        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + tbl + "_candles_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `in` DOUBLE, `out` DOUBLE, `low` DOUBLE,`hight` DOUBLE, `volume` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            ResultSet rs = stmt.executeQuery("SELECT `time` FROM `" + tbl + "_candles_" + period + "` ORDER BY `time` ASC LIMIT 1;");
            
            if (rs != null) {
                if(rs.next()) {
                    stmt.close();
                    return rs.getLong("time");
                }
            }
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " getTimeOfLastJapaneseCandle -> " + ex);
        }
        return 0;
    }
    
    // Получить JapaneseCandles в заданном диапазоне
    public List <JapaneseCandle> getJapaneseCandleByTimePeriod (String tbl, Period p, long since, long till) {
        
        String period = null;
            
        switch (p) {
            case DAY: period = "1_day"; break;
            case HOUR: period = "1_hour"; break;
            case MINUTE: period = "1_min"; break;
            default: new RuntimeException (p + " doesn't support yet.");
        }
        
        try { 
            stmt = getConnection();
            
            createTable(stmt, "`" + tbl + "_candles_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `in` DOUBLE, `out` DOUBLE, `low` DOUBLE,`hight` DOUBLE, `volume` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            List <JapaneseCandle> listJc = new ArrayList <> ();
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM `" + tbl + "_candles_" + period + "` WHERE `time` >= " + since + " AND `time` <= " + till + " ORDER BY `time` ASC;");
            
            if (rs != null) {
                JapaneseCandle jc;
                while(rs.next()) {
                    jc = new JapaneseCandle(rs.getLong("time"), rs.getDouble("in"));
                    jc.update(rs.getDouble("hight"));
                    jc.update(rs.getDouble("low"));
                    jc.update(rs.getDouble("out"));
                    jc.addVolumeAsk(rs.getDouble("volume")/2);
                    jc.addVolumeBid(rs.getDouble("volume")/2);
                    listJc.add(jc);
                }
            }
            return listJc;
            
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " getJapaneseCandle -> " + ex);
        }
        return null;
    }
    
    // Получить JapaneseCandles в заданном диапазоне
    public List <JapaneseCandle> getJapaneseCandleLimitedBeforeTime (String tbl, Period p, int limit, long till) {
        
        String period = null;
            
        switch (p) {
            case DAY: period = "1_day"; break;
            case HOUR: period = "1_hour"; break;
            case MINUTE: period = "1_min"; break;
            default: new RuntimeException (p + " doesn't support yet.");
        }
        
        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + tbl + "_candles_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `in` DOUBLE, `out` DOUBLE, `low` DOUBLE,`hight` DOUBLE, `volume` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            List <JapaneseCandle> listJc = new ArrayList <> ();
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM (SELECT * FROM `" + tbl + "_candles_" + period + "` WHERE `time` <= " + till + " ORDER BY `time` DESC LIMIT " + limit + ") AS temptbl ORDER BY `time` ASC;");
            
            if (rs != null) {
                JapaneseCandle jc;
                while(rs.next()) {
                    jc = new JapaneseCandle(rs.getLong("time"), rs.getDouble("in"));
                    jc.update(rs.getDouble("hight"));
                    jc.update(rs.getDouble("low"));
                    jc.update(rs.getDouble("out"));
                    jc.addVolumeAsk(rs.getDouble("volume")/2);
                    jc.addVolumeBid(rs.getDouble("volume")/2);
                    listJc.add(jc);
                }
            }
            return listJc;
            
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " getJapaneseCandle -> " + ex);
        }
        return null;
    }
    
    // Get Candle
    public synchronized List <Candle> getCandles(String tbl, Period p) {
        
        List <Candle> candles = new ArrayList <>();
        
        String period = null;
            
        switch (p) {
            case DAY: period = "1_day"; break;
            case HOUR: period = "1_hour"; break;
            case MINUTE: period = "1_min"; break;
            default: new RuntimeException (p + " doesn't support yet.");
        }
        
        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + tbl + "_candles_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `in` DOUBLE, `out` DOUBLE, `low` DOUBLE,`hight` DOUBLE, `volume` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM `" + tbl + "_candles_" + period +  "` ORDER BY `time` ASC;");
            
            if (rs != null) {
                while (rs.next()) {
                    Candle c = new Candle(
                        rs.getLong("time"),
                        rs.getDouble("hight"),
                        rs.getDouble("low"),
                        rs.getDouble("in"),
                        rs.getDouble("out"),
                        rs.getDouble("volume")
                    );
                    candles.add(c);
                }
            }
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " -> " + ex);
            return candles;
        }
        
        return candles;
    }
    
    public synchronized List <Candle> getCandleLimitedBeforeTime (String tbl, Period p, int limit, long till) {
        
        List <Candle> candles = new ArrayList <>();
        
        String period = null;
            
        switch (p) {
            case DAY: period = "1_day"; break;
            case HOUR: period = "1_hour"; break;
            case MINUTE: period = "1_min"; break;
            default: new RuntimeException (p + " doesn't support yet.");
        }
        
        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + tbl + "_candles_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `in` DOUBLE, `out` DOUBLE, `low` DOUBLE,`hight` DOUBLE, `volume` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM (SELECT * FROM `" + tbl + "_candles_" + period + "` WHERE `time` <= " + till + " ORDER BY `time` DESC LIMIT " + limit + ") AS temptbl ORDER BY `time` ASC;");
            
            if (rs != null) {
                while (rs.next()) {
                    Candle c = new Candle(
                        rs.getLong("time"),
                        rs.getDouble("hight"),
                        rs.getDouble("low"),
                        rs.getDouble("in"),
                        rs.getDouble("out"),
                        rs.getDouble("volume")
                    );
                    candles.add(c);
                }
            }
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " -> " + ex);
            return candles;
        }
        
        return candles;
    }
    
    //
    public synchronized boolean writeQuickData(List <QuickData> qd, String tbl){
        
        try {
            stmt = getConnection();
            
            if (qd.isEmpty()) return false;
            
            // TODO: Проверить таблицу
            createTable(stmt, "`" + tbl + "_quick`", "`time` BIGINT NOT NULL UNIQUE, `last` DOUBLE, `ask` DOUBLE, `bid` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            for (int i = 0; i < qd.size(); i++) {
                try {
                    if (!qd.get(i).isIsWrittedToSQL()) {
                        // time, last, ask, bid
                        stmt.execute("INSERT INTO `" + tbl + "_quick` (`time`, `last`, `ask`, `bid`) VALUES (" +
                                qd.get(i).getTime() + ", " +
                                qd.get(i).getLast()+ ", " +
                                qd.get(i).getAsk()+ ", " +
                                qd.get(i).getBid()+ ");"
                        );
                        
                        qd.get(i).markWrittedToSQL();
                    }
                } catch (SQLIntegrityConstraintViolationException ex) {
                    continue;
                }
            }
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " writeQuickData -> " + ex);
            return false;
        }
        return true;
    }
    //
    public synchronized boolean writeSummaryInfo(List <SummaryInfo> si, String tbl){
        
        try {
            stmt = getConnection();
            
            if (si.isEmpty()) return false;
            
            // TODO: Проверить таблицу
            createTable(stmt, "`" + tbl + "_summary`", "`time` BIGINT NOT NULL UNIQUE, `volume` DOUBLE, `low` DOUBLE, `hight` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            for (int i = 0; i < si.size() ; i++) {
                try {
                    if (!si.get(i).isIsWrittedToSQL()) {
                        // time, volume, low, hight
                        stmt.execute("INSERT INTO `" + tbl + "_summary` (`time`, `volume`, `low`, `hight`) VALUES (" +
                                si.get(i).getTime() + ", " +
                                si.get(i).getVolume() + ", " +
                                si.get(i).getLow() + ", " +
                                si.get(i).getHight()+ ");"
                        );
                        
                        si.get(i).markWrittedToSQL();
                    }
                } catch (SQLIntegrityConstraintViolationException ex) {
                    continue;
                } 
            }
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " writeSummaryInfo -> " + ex);
            return false;
        }
        return true;
    }
    
    //
    public synchronized void writeMarkets(List <String> ml){
        try {
            stmt = getConnection();
            
            createTable(stmt, "`markets`", "`name` VARCHAR(11) NOT NULL UNIQUE, `status` TEXT, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            if (ml != null) {
                for (String m: ml) {
                    try{
                        stmt.execute("INSERT INTO `markets` (name, status) VALUE (\""+ m + "\", \"" + Status.STOPED + "\");");
                    } catch (SQLIntegrityConstraintViolationException ex) {
                        System.out.println(SQLQuery.class.getName() + " writeMarkets -> " + ex);
                        continue;
                    }
                }
            } 
        } catch (SQLException ex) {
            Logger.getLogger(SQLQuery.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }
    //
    public synchronized Map <String, Status> readMarkets(){
        
        Map <String, Status> markets = new HashMap <>();
        
        try {
            stmt = getConnection();
            
            createTable(stmt, "`markets`", "`name` VARCHAR(11) NOT NULL UNIQUE, `status` TEXT, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            ResultSet rs = stmt.executeQuery("SELECT `name`, `status` FROM `markets` WHERE `status` != \"" + Status.DELETED + "\";");
            
            if (rs != null) {
                while (rs.next()) {
                    markets.put(rs.getString("name"), Status.valueOf(rs.getString("status")));
                }
            }
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " readMarkets -> " + ex);
            return markets;
        }
        
        return markets;
    }
    //
    public synchronized void updateMarketStatus(String market, Status status){
        
        try {
            stmt = getConnection();
            
            createTable(stmt, "`markets`", "`name` VARCHAR(11) NOT NULL UNIQUE, `status` TEXT, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            stmt.execute("UPDATE `markets` SET status = \"" + status + "\" WHERE name = \"" + market + "\";");
            
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " -> " + ex);
            return;
        }
    }
    
    // Создаем таблицу если она не существует
    public void createTable (Statement stmt, String data_table, String param){
        try {
            // executing SELECT query
            stmt.execute("CREATE TABLE IF NOT EXISTS " + data_table + " (" + param +  ");");
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " -> " + ex);
            return;
        }
    }
    
    public synchronized boolean writeAnaliticData(String market, AnaliticType type, Period period, List <AnaliticData> aData) {
        
        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + market + "_" + type + "_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `value` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            for (AnaliticData ad: aData) {
                try {
                    // time, value
                    stmt.execute("INSERT INTO `" + market + "_" + type + "_" + period + "` (`time`, `value`) VALUES (" +
                            Long.toString(ad.getTime())+ ", " +
                            Double.toString(ad.getValue())+ ");"
                    );
                } catch (SQLIntegrityConstraintViolationException ex) {
                    continue;
                }
            }
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " writeAnaliticData -> " + ex);
            return false;
        }
        return true;
    }
    
    // Взять время последнего анализа
    public synchronized long getTimeOfLastAnaliticData(String market, AnaliticType type, Period period) {
        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + market + "_" + type + "_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `value` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            ResultSet rs = stmt.executeQuery("SELECT `time` FROM `" + market + "_" + type + "_" + period + "` ORDER BY `time` DESC LIMIT 1;");
            
            if (rs != null) {
                if(rs.next()) {
                    stmt.close();
                    return rs.getLong("time");
                }
            }
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " getTimeOfLastAnaliticData -> " + ex);
        } 
        return 0;
    }
    
    // Взять время первого анализа
    public synchronized long getTimeOfFirstAnaliticData(String market, AnaliticType type, Period period) {
        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + market + "_" + type + "_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `value` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            ResultSet rs = stmt.executeQuery("SELECT `time` FROM `" + market + "_" + type + "_" + period + "` ORDER BY `time` ASC LIMIT 1;");
            
            if (rs != null) {
                if(rs.next()) {
                    stmt.close();
                    return rs.getLong("time");
                }
            }
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " getTimeOfLastAnaliticData -> " + ex);
        } 
        return 0;
    }
    
    // Получить AnaliticData во временом диапазоне
    public List <AnaliticData> getAnaliticDataByTimePeriod (String market, AnaliticType type, Period period, long since, long till) {
        
        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + market + "_" + type + "_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `value` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            List <AnaliticData> adList = new ArrayList<>();
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM `" + market + "_" + type + "_" + period + "` WHERE `time` >= " + since + " AND `time` <= " + till + " ORDER BY `time` ASC;");
            
            if (rs != null) {
                while(rs.next()) {
                    adList.add(new AnaliticData(rs.getLong("time"), rs.getDouble("value")));
                }
            }
            return adList;
            
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " getJapaneseCandle -> " + ex);
        }
        return null;
    }
    
    public List <AnaliticData> getAnaliticDataLimitedBeforeTime (String market, AnaliticType type, Period period, int limit, long till) {
        
        try {
            stmt = getConnection();
            
            createTable(stmt, "`" + market + "_" + type + "_" + period + "`", "`time` BIGINT NOT NULL UNIQUE, `value` DOUBLE, `id_key` INT NOT NULL AUTO_INCREMENT, PRIMARY KEY (`id_key`)");
            
            List <AnaliticData> adList = new ArrayList<>();
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM (SELECT * FROM `" + market + "_" + type + "_" + period + "` WHERE `time` <= " + till + " ORDER BY `time` DESC LIMIT " + limit + ") AS temptbl ORDER BY `time` ASC;");
            
            if (rs != null) {
                while(rs.next()) {
                    adList.add(new AnaliticData(rs.getLong("time"), rs.getDouble("value")));
                }
            }
            return adList;
            
        } catch (SQLException ex) {
            System.out.println(SQLQuery.class.getName() + " getJapaneseCandle -> " + ex);
        }
        return null;
    }
    
    private Statement getConnection () {
        
        for (int i = 0; i < 5; i++) {
            
            try {
                if (stmt == null) {
                    try {
                        stmt = DriverManager.getConnection(url + "?useSSL=false", user, password).createStatement();
                        return stmt;
                    }
                    catch (Exception e) {
                        stmt = null;
                    }
                }
                else if (stmt.isClosed()) {
                    try {
                        stmt = DriverManager.getConnection(url + "?useSSL=false", user, password).createStatement();
                        return stmt;
                    }
                    catch (Exception e) {
                        stmt = null;
                    }
                } 
                else return stmt;
                
            } catch (SQLException ex) {
                System.out.println(ex);
                continue;
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SQLQuery.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return null;
    }
    
}
