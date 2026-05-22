/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapademo;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author jose
 */
public class MapaDemoApp extends Application {
    
    private static BorderPane rootLayout;
    
    @Override
    public void start(Stage stage) throws Exception {
        rootLayout = new BorderPane();
        
        Scene scene = new Scene(rootLayout, 900, 600);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        stage.setTitle("Running la Safor - IPC 2026");
        stage.setScene(scene);
        stage.show();
        loadView("welcomeView.fxml");
    }


    public static void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(MapaDemoApp.class.getResource(fxmlFile));
            Parent view = loader.load();
            rootLayout.setCenter(view);
            
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlFile);
            e.printStackTrace();
        }
    }
     /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
