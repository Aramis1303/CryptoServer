/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eva.cryptoserver.gui;

import eva.cryptoserver.Status;
import eva.cryptoserver.data.Data;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 *
 * @author username
 */
public class WorkingPane extends VBox {
    
    private Button switcher;
    
    private String name;
    
    private Label lName;
    private Label sqlStatus;
    private Label urlStatus;
    private Label lLast;
    
    private Data data;
    private BursePan bp;
    
    public WorkingPane (Data data, BursePan bp) {
        
        this.data = data;
        this.bp = bp;
        
        name = data.getName();
        lName = new Label (name);
        switcher = new Button ();
        
        sqlStatus = new Label("SQL");
        urlStatus = new Label("URL");
        
        lLast = new Label(new String("LAST: "));
        
        this.getChildren().addAll(lName, switcher);
        
        if (data.getStatus().equals(Status.WORKING)) {
            switcher.setText(Status.WORKING.toString());
            this.setStyle("-fx-background-color: #DCDCDC; -fx-border-color: #696969 ; -fx-border-width: 2");
            this.getChildren().addAll(lLast, sqlStatus, urlStatus);
            this.setPrefSize(150, 100);
        }
        else {
            switcher.setText(Status.STOPED.toString());
            this.setStyle("-fx-background-color: #696969; -fx-border-color: #DCDCDC ; -fx-border-width: 2");
            this.getChildren().removeAll(lLast, sqlStatus, urlStatus);
            this.setPrefSize(150, 20);
        }
        
        switcher.setOnAction(e -> {
            if (switcher.getText().equals(Status.WORKING.toString())) {
                switcher.setText(Status.STOPED.toString());
                switchOff();
            }
            else {
                switcher.setText(Status.WORKING.toString());
                switchOn();
            }
        });
        switcher.setPrefSize(150, 10);
    }
    
    public String getName() {
        return name;
    }
    
    private void switchOff () {
        this.setStyle("-fx-background-color: #696969");
        this.getChildren().removeAll(lLast, sqlStatus, urlStatus);
        this.setPrefSize(150, 20);
        bp.wpSwitch(data, this);
    }
    
    private void switchOn () {
        this.setStyle("-fx-background-color: #DCDCDC");
        this.getChildren().addAll(lLast, sqlStatus, urlStatus);
        this.setPrefSize(150, 100);
        bp.wpSwitch(data, this);
    }
    
    public void udate () {
        if (data.getStatus().equals(Status.WORKING)) {
            Platform.runLater(new Runnable(){
                @Override
                public void run() {
                    
                    if (!data.getQuickData().isEmpty()) {
                        lLast.setText(new String("LAST: " + String.format ("%.8f", data.getQuickData().get(data.getQuickData().size()-1).getLast())));
                    }

                    if(data.lastSqlConnection) {
                        sqlStatus.setStyle("-fx-text-fill: #008000");
                    }else {
                        sqlStatus.setStyle("-fx-text-fill: #8B0000");
                    }

                    if(data.lastUrlConnection) {
                        urlStatus.setStyle("-fx-text-fill: #008000");
                    }else {
                        urlStatus.setStyle("-fx-text-fill: #8B0000");
                    }
                }
            });
        }
    }

}
