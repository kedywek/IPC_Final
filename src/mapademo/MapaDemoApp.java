/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapademo;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;

/**
 *
 * @author jose
 */
public class MapaDemoApp extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        stage.setTitle("Running la Safor - IPC 2026");
        
        stage.setOnCloseRequest(event -> {
            System.out.println("Application closing via window 'X' button...");
            SportActivityApp.getInstance().logout(); 
            System.out.println("Session successfully closed and saved before exit.");
        });

        loadView("welcomeView.fxml");
    }

    public static void loadView(String fxmlFile) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(MapaDemoApp.class.getResource(fxmlFile));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            
            primaryStage.setScene(scene);
            
            primaryStage.sizeToScene();
            
            primaryStage.centerOnScreen();
            
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading view: " + fxmlFile);
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
