/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.qp.bittrex;

import eva.cryptoserver.data.Data;
import eva.cryptoserver.data.OrderOfHistory;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * @author Ermolenko Vadim
 */
public class QueryHistory implements Runnable, Serializable {
    
    private Data data;
    private Thread process;
    
    public QueryHistory (Data data) {
        this.data = data;
        
        process = new Thread (this);
        process.setName(data.getName() + ".QueryHistory");
        process.start();
    }
    
    @Override
    public void run() {
        StringBuilder JSON = new StringBuilder();
        DataInputStream input = null;
        JSONArray array;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            
        try{
            URL url = new URL("https://bittrex.com/api/v1.1/public/getmarkethistory?market=" + data.getName());
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
            }/* else if (!jsonObj.getString("message").equals("")){
                FileWritter.writeToEvent(data.getBurse() + ": " + data.getName() + ", QueryHistory jsonObj.message=" + jsonObj.getString("message") + " " + new Date());
            }*/
            
            synchronized(data){
                Date dt;
                array = jsonObj.getJSONArray("result");
                // Запись данных в коллекции
                // Первый элемент будет самый старый, новые в конце коллекции
                for(int i = array.length() - 1; i >= 0 ; i--){
                    // Отбираем закрытие Покупок (Bid)
                    if (array.getJSONObject(i).getString("OrderType").equals("SELL")){
                        dt = formatter.parse(array.getJSONObject(i).getString("TimeStamp"));
                        
                        if (data.getBuyHistory().isEmpty()) 
                        {
                            data.getBuyHistory().add(new OrderOfHistory(
                                array.getJSONObject(i).getDouble("Price"), 
                                array.getJSONObject(i).getDouble("Quantity"),
                                dt.getTime(),
                                array.getJSONObject(i).getInt("Id"))
                            );
                        }
                        else if ( (dt.getTime() > data.getBuyHistory().get(data.getBuyHistory().size() - 1).getTime()) ||
                                  (dt.getTime() == data.getBuyHistory().get(data.getBuyHistory().size() - 1).getTime() && 
                                   array.getJSONObject(i).getInt("Id") > data.getBuyHistory().get(data.getBuyHistory().size() - 1).getId()) )
                        {   
                            
                            data.getBuyHistory().add(new OrderOfHistory(
                                array.getJSONObject(i).getDouble("Price"), 
                                array.getJSONObject(i).getDouble("Quantity"),
                                dt.getTime(),
                                array.getJSONObject(i).getInt("Id"))
                            );
                        }
                    }
                    // Отбираем закрытие Продаж (ASK)
                    if (array.getJSONObject(i).getString("OrderType").equals("BUY")){
                        dt = formatter.parse(array.getJSONObject(i).getString("TimeStamp"));

                        if (data.getSellHistory().isEmpty())
                        {
                            data.getSellHistory().add(new OrderOfHistory(
                                array.getJSONObject(i).getDouble("Price"), 
                                array.getJSONObject(i).getDouble("Quantity"),
                                dt.getTime(),
                                array.getJSONObject(i).getInt("Id"))
                            ); 
                        }
                        else if ( (dt.getTime() > data.getSellHistory().get(data.getSellHistory().size() - 1).getTime()) ||
                                  (dt.getTime() == data.getSellHistory().get(data.getSellHistory().size() - 1).getTime() && 
                                   array.getJSONObject(i).getInt("Id") > data.getSellHistory().get(data.getSellHistory().size() - 1).getId()) )
                        {
                            data.getSellHistory().add(new OrderOfHistory(
                                array.getJSONObject(i).getDouble("Price"), 
                                array.getJSONObject(i).getDouble("Quantity"),
                                dt.getTime(),
                                array.getJSONObject(i).getInt("Id"))
                            ); 
                        }
                    }
                }
                data.lastUrlConnection = true;
            }
            
        }
        catch (IOException | ParseException | JSONException ex) {
            data.lastUrlConnection = false;
            //System.out.println("Exception: " + data.getBurse() + ": " + data.getName());
            //ex.printStackTrace();
            return;
        }
    }
    
}
