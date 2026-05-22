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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author damian
 */
public class RegisterController implements Initializable {

    @FXML
    private TextField txtNickname;
    @FXML
    private Label lblNIckError;
    @FXML
    private TextField txtEmail;
    @FXML
    private Label lblEmailError;
    @FXML
    private Label lblPasswordError;
    @FXML
    private DatePicker dpBirth;
    @FXML
    private Label labelDateError;
    @FXML
    private ImageView imgAvatar;
    @FXML
    private Button btnChooseAv;
    @FXML
    private Button btnCreateAcc;
    @FXML
    private Label lblError;
    @FXML
    private Label lblAvatar;
    private String avatarPath = "";
    /**
     * Initializes the controller class.
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void handleChooseAv(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Avatar Image");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        Stage stage = (Stage) txtNickname.getScene().getWindow();
        File imgFile = fc.showOpenDialog(stage);
        
        if (imgFile != null) {
            avatarPath = imgFile.getAbsolutePath();
            String lblText = lblAvatar.getText();
            lblAvatar.setText(lblText + imgFile.getName()); 
            Image newAvatarImage = new Image(imgFile.toURI().toString());
            imgAvatar.setImage(newAvatarImage);    
        }
    }

    @FXML
    private void handleCreateAcc(ActionEvent event) {
    }
    
}
