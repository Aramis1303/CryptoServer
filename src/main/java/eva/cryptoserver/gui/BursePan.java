/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.gui;

import eva.cryptoserver.threads.TraderProcess;
import eva.cryptoserver.data.Data;
import java.util.Map;
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import eva.cryptoserver.Status;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;

/**
 *
 * @author username
 */
public class BursePan extends Tab implements Runnable {
    
    private VBox all;
    private GridPane menu;
    private HBox flows;
    private FlowPane markets_working;
    private FlowPane markets_stoped;
    private TraderProcess tp;
    private ScrollPane scroll_working;
    private ScrollPane scroll_stoped;
    
    private Button btnAggregation;
    private Button btnNEUNET;
    private Button btnSMA;
    private Button btnWMA;
    private Button btnATR;
    private Button btnSTDev;
    private Button btnDI;
    private Button btnBollinger;
    private Button btnADX;
    private Button btnMACD;
    
    private boolean isKilled;
    
    public BursePan (TraderProcess tp) {
        
        this.tp = tp;        
        this.setText(tp.getName());
        
        isKilled = false;
        
        all = new VBox();
        menu = new GridPane();
        flows = new HBox(); 
        
        markets_working = new FlowPane();
        markets_stoped = new FlowPane();
        
        scroll_working = new ScrollPane();
        scroll_stoped = new ScrollPane();
        scroll_working.setContent(markets_working);
        scroll_stoped.setContent(markets_stoped);
        scroll_working.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scroll_working.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll_stoped.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scroll_stoped.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        this.setContent(all);
        
        this.btnAggregation = new Button("START");
        btnAggregation.setOnAction(e -> {
            if (btnAggregation.getText().equals("START")) {
                btnAggregation.setText("STOP");
                tp.getDataProcessing().setCalculateAggregation(true);
            }
            else {
                btnAggregation.setText("START");
                tp.getDataProcessing().setCalculateAggregation(false);
            }
        });
        
        this.btnNEUNET = new Button("START");
        btnNEUNET.setOnAction(e -> {
            if (btnNEUNET.getText().equals("START")) {
                btnNEUNET.setText("STOP");
                tp.getDataProcessing().setCalculateNEUNET(true);
            }
            else {
                btnNEUNET.setText("START");
                tp.getDataProcessing().setCalculateNEUNET(false);
            }
        });
        
        this.btnSMA = new Button("START");
        btnSMA.setOnAction(e -> {
            if (btnSMA.getText().equals("START")) {
                btnSMA.setText("STOP");
                tp.getDataProcessing().setCalculateSMA(true);
            }
            else {
                btnSMA.setText("START");
                tp.getDataProcessing().setCalculateSMA(false);
            }
        });
        
        this.btnWMA = new Button("START");
        btnWMA.setOnAction(e -> {
            if (btnWMA.getText().equals("START")) {
                btnWMA.setText("STOP");
                tp.getDataProcessing().setCalculateWMA(true);
            }
            else {
                btnWMA.setText("START");
                tp.getDataProcessing().setCalculateWMA(false);
            }
        });
        
        this.btnATR = new Button("START");
        btnATR.setOnAction(e -> {
            if (btnATR.getText().equals("START")) {
                btnATR.setText("STOP");
                tp.getDataProcessing().setCalculateATR(true);
            }
            else {
                btnATR.setText("START");
                tp.getDataProcessing().setCalculateATR(false);
            }
        });
        
        this.btnSTDev = new Button("START");
        btnSTDev.setOnAction(e -> {
            if (btnSTDev.getText().equals("START")) {
                btnSTDev.setText("STOP");
                tp.getDataProcessing().setCalculateSTDev(true);
            }
            else {
                btnSTDev.setText("START");
                tp.getDataProcessing().setCalculateSTDev(false);
            }
        });
        
        this.btnDI = new Button("START");
        btnDI.setOnAction(e -> {
            if (btnDI.getText().equals("START")) {
                btnDI.setText("STOP");
                tp.getDataProcessing().setCalculateDI(true);
            }
            else {
                btnDI.setText("START");
                tp.getDataProcessing().setCalculateDI(false);
            }
        });
        
        this.btnBollinger = new Button("START");
        btnBollinger.setOnAction(e -> {
            if (btnBollinger.getText().equals("START")) {
                btnBollinger.setText("STOP");
                tp.getDataProcessing().setCalculateBollinger(true);
            }
            else {
                btnBollinger.setText("START");
                tp.getDataProcessing().setCalculateBollinger(false);
            }
        });
        
        this.btnADX = new Button("START");
        btnADX.setOnAction(e -> {
            if (btnADX.getText().equals("START")) {
                btnADX.setText("STOP");
                tp.getDataProcessing().setCalculateADX(true);
            }
            else {
                btnADX.setText("START");
                tp.getDataProcessing().setCalculateADX(false);
            }
        });
        
        this.btnMACD = new Button("START");
        btnMACD.setOnAction(e -> {
            if (btnMACD.getText().equals("START")) {
                btnMACD.setText("STOP");
                tp.getDataProcessing().setCalculateMACD(true);
            }
            else {
                btnMACD.setText("START");
                tp.getDataProcessing().setCalculateMACD(false);
            }
        });
        
        menu.add(new Label("Aggregation: "), 0, 0);
        menu.add(btnAggregation, 1, 0);
        menu.add(new Label("Neuron Network:"), 2, 0);
        menu.add(btnNEUNET, 3, 0);
        menu.add(new Label("SMA:"), 4, 0);
        menu.add(btnSMA, 5, 0);
        menu.add(new Label("WMA:"), 6, 0);
        menu.add(btnWMA, 7, 0);
        
        menu.add(new Label("ATR:"), 0, 1);
        menu.add(btnATR, 1, 1);
        menu.add(new Label("DI +/-, DX:"), 2, 1);
        menu.add(btnDI, 3, 1);
        menu.add(new Label("ADX:"), 4, 1);
        menu.add(btnADX, 5, 1);
        menu.add(new Label("MACD:"), 6, 1);
        menu.add(btnMACD, 7, 1);
        
        menu.add(new Label("STDev:"), 0, 2);
        menu.add(btnSTDev, 1, 2);
        menu.add(new Label("Bollinger:"), 2, 2);
        menu.add(btnBollinger, 3, 2);
        //menu.add(new Label(":"), 4, 2);
        //menu.add(, 5, 2);
        //menu.add(new Label(":"), 6, 2);
        //menu.add(, 7, 2);
        
        
        all.getChildren().addAll(menu, flows);
        flows.getChildren().addAll(scroll_working, scroll_stoped);
        
        for (Map.Entry <String, Data> entry: tp.getSomeData().entrySet()) {
            if (entry.getValue().getStatus().equals(Status.WORKING)) {
                markets_working.getChildren().add(new WorkingPane(entry.getValue(), this));
            }
            else {
                markets_stoped.getChildren().add(new WorkingPane(entry.getValue(), this));
            }
        }
        
        new Thread(this).start();
    }
    
    public void wpSwitch (Data d, WorkingPane wp) {
        if (d.getStatus().equals(Status.STOPED)) {
            d.setStatus(Status.WORKING);
            markets_stoped.getChildren().remove(wp);
            markets_working.getChildren().add(wp);
            tp.getSql().updateMarketStatus(wp.getName(), Status.WORKING);
        }
        else {
            d.setStatus(Status.STOPED);
            markets_working.getChildren().remove(wp);
            markets_stoped.getChildren().add(wp);
            tp.getSql().updateMarketStatus(wp.getName(), Status.STOPED);
        }
    }

    
    @Override
    public void run() {
        while (!isKilled) {
            
            for (Node n: markets_working.getChildren()) {
                ((WorkingPane)n).udate();
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(WorkingPane.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void kill() {
        isKilled = true;
    }
}
