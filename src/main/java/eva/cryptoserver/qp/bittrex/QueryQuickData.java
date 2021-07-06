/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.qp.bittrex;
 
import eva.cryptoserver.data.Data;
import eva.cryptoserver.data.QuickData;
import java.io.DataInputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import org.json.JSONObject;
 
/*
 * @author Ermolenko Vadim
 */
public class QueryQuickData implements Runnable, Serializable {
     
    private Data data;
    private Thread process;
     
    public QueryQuickData (Data data) {
        this.data = data;
         
        process = new Thread (this);
        process.setName(data.getName() + ".QueryQuickData");
        process.start();
    }
     
    @Override
    public void run() {
        StringBuilder JSON = new StringBuilder();
        DataInputStream input = null;
         
        try{
            URL url = new URL("https://bittrex.com/api/v1.1/public/getticker?market=" + data.getName());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            input = new DataInputStream(con.getInputStream());
             
            byte [] b = new byte[1];
            while(-1 != input.read(b,0,1)) {
               JSON.append(new String(b));
            }
            con.disconnect();
            
            JSONObject jsonObj = new JSONObject(new String(JSON));
            
            if (jsonObj.getString("message").equals("INVALID_MARKET")){
                data.setInvalidMarket(true);
                return;
            } /*else if (!jsonObj.getString("message").equals("")){
                FileWritter.writeToEvent(data.getBurse() + ": " + data.getName() + ", QueryQuickData jsonObj.message=" + jsonObj.getString("message") + " " + new Date());
            }*/
            
            JSONObject jResult = jsonObj.getJSONObject("result");
             
            QuickData qd = new QuickData(jResult.getDouble("Last"),
                jResult.getDouble("Bid"),
                jResult.getDouble("Ask"),
                new Date().getTime());
            
            synchronized(data){
                data.lastUrlConnection = true;
                data.getQuickData().add(qd);    // 
                data.updateCandles(qd);         // Формируем свечу
            }
            
        }
        catch (Exception ex) {
            data.lastUrlConnection = false;
            //System.out.println("Exception: " + data.getBurse() + ": " + data.getName());
            //ex.printStackTrace();
            return;
        }
    }
}
 