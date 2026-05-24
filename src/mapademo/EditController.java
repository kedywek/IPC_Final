/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;
/**
 * FXML Controller class
 *
 * @author damian
 */
public class EditController implements Initializable {


    @FXML
    private TextField txtNickname;
    @FXML
    private TextField lblEmailError;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblPassError;
    @FXML
    private DatePicker dpBirt;
    @FXML
    private ImageView imgAvatar;
    @FXML
    private Button btnChooseAv;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSave;
    
    
    private String selectedAvatarPath = null;
    /**
     * Initializes the controller class.
     */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User currentUser = SportActivityApp.getInstance().getCurrentUser(); 
        
        if (currentUser != null) {
            txtNickname.setText(currentUser.getNickName()); 
            lblEmailError.setText(currentUser.getEmail()); 
            dpBirt.setValue(currentUser.getBirthDate()); 
            
            selectedAvatarPath = currentUser.getAvatarPath(); 
            if (currentUser.getAvatar() != null) {
                imgAvatar.setImage(currentUser.getAvatar()); 
            }
        }
    }
    
    @FXML
    private void handleChooseAvatar(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Avatar Image");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        Stage stage = (Stage) txtNickname.getScene().getWindow();
        File imgFile = fc.showOpenDialog(stage);
        
        if (imgFile != null) {
            selectedAvatarPath = imgFile.getAbsolutePath();
            Image newAvatarImage = new Image(imgFile.toURI().toString());
            imgAvatar.setImage(newAvatarImage);    
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        MapaDemoApp.loadView("MainView.fxml");
    }

    @FXML
    private void handleSave(ActionEvent event) {
    }

}
