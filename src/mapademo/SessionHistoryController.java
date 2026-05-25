/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author damian
 */
public class SessionHistoryController implements Initializable {

    @FXML
    private TableView<?> sessionTable;
    @FXML
    private TableColumn<?, ?> colStart;
    @FXML
    private TableColumn<?, ?> colEnd;
    @FXML
    private TableColumn<?, ?> colDur;
    @FXML
    private TableColumn<?, ?> colImported;
    @FXML
    private TableColumn<?, ?> colViewed;
    @FXML
    private TableColumn<?, ?> colAnnotations;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void handleclose(ActionEvent event) {
    }
    
}
