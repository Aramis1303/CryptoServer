/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.qp.bittrex;

import eva.cryptoserver.Status;
import eva.cryptoserver.threads.TraderProcess;
import eva.cryptoserver.data.Data;
import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Home
 */
public class QueryMarketList implements Runnable {
    
    private TraderProcess tp;
    private Thread process;
    
    public QueryMarketList (TraderProcess tp) {
        this.tp = tp;
        
        process = new Thread (this);
        process.setName(tp.getName() + ".QueryMarketList");
        process.start();
    }

    @Override
    public void run() {
        StringBuilder JSON = new StringBuilder();
        DataInputStream input = null;
        JSONArray array;
        
        try{
            URL url = new URL("https://bittrex.com/api/v1.1/public/getmarkets");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            input = new DataInputStream(con.getInputStream());
            
            byte [] b = new byte[1];
            while(-1 != input.read(b,0,1)) {
               JSON.append(new String(b));
            }
            con.disconnect();
            
            JSONObject jsonObj = new JSONObject(new String(JSON));
            
            synchronized(tp.getSomeData()){
                List <String> marketsForSQL = new ArrayList<>();
                array = jsonObj.getJSONArray("result");
                for (Object o: array){
                    JSONObject jo = (JSONObject)o;
                    
                    // Если такой маркет есть в списке и активен ли маркет в BITTREX
                    if (tp.getSomeData().containsKey(jo.getString("MarketName")) && jo.getBoolean("IsRestricted")) {
                        tp.getSomeData().remove(jo.getString("MarketName"));
                        tp.getSql().updateMarketStatus(jo.getString("MarketName"), Status.DELETED);
                    }
                    else if (!jo.getBoolean("IsRestricted") && jo.getBoolean("IsActive") && !tp.getSomeData().containsKey(jo.getString("MarketName"))){
                        tp.getSomeData().put(jo.getString("MarketName"), new Data(jo.getString("MarketName"), tp.getName()));
                        tp.getSomeData().get(jo.getString("MarketName")).setStatus(Status.STOPED);
                        marketsForSQL.add(jo.getString("MarketName"));
                    }
                }
                
                tp.getSql().writeMarkets(marketsForSQL);
            }
        }
        catch (Exception ex) {
            //ex.printStackTrace();
            return;
        }
    }
}
