/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
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
    private Label lblEmailError;
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
    @FXML
    private Label lblDateError;
    @FXML
    private Label lblProfile;
    @FXML
    private TextField txtEmail;
    
    
    private String selectedAvatarPath = null;
    /**
     * Initializes the controller class.
     */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User currentUser = SportActivityApp.getInstance().getCurrentUser(); 
        
        if (currentUser != null) {
            txtNickname.setText(currentUser.getNickName()); 
            txtEmail.setText(currentUser.getEmail()); 
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
            String lblText = lblProfile.getText();
            lblProfile.setText(lblText + imgFile.getName()); 
            imgAvatar.setImage(newAvatarImage);    
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        MapaDemoApp.loadView("MainView.fxml");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        User currentUser = SportActivityApp.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        LocalDate birthDate = dpBirt.getValue();

        lblPassError.setVisible(false);
        lblDateError.setVisible(false);
        lblEmailError.setVisible(false);
        boolean isValid = true;

        if (!User.checkEmail(email)) {
            lblEmailError.setVisible(true);
            isValid = false;
        }

        if (password.isEmpty()) {
            password = currentUser.getPassword();
        } else if (!User.checkPassword(password)) {
            lblPassError.setVisible(true); 
            isValid = false;
        }

        if (birthDate == null || !User.isOlderThan(birthDate, 12)) {
            lblDateError.setVisible(true);
            isValid = false;
        }

        if (isValid) {
            try {
                SportActivityApp.getInstance().updateCurrentUser(
                    email, 
                    password, 
                    birthDate, 
                    selectedAvatarPath
                );

                System.out.println("User account credentials successfully committed to SQL database.");
                
                MapaDemoApp.loadView("MainView.fxml");
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
