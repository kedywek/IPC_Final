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
    private PasswordField txtPass;
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
        String nick = txtNickname.getText().trim();
        String email = txtEmail.getText().trim();
        String pass = txtPass.getText();
        LocalDate dob = dpBirth.getValue();

        lblError.setVisible(false); 
        lblNIckError.setVisible(false);
        lblEmailError.setVisible(false);
        lblPasswordError.setVisible(false);
        labelDateError.setVisible(false);

        if (nick.isEmpty() || email.isEmpty() || pass.isEmpty() || dob == null) {
            lblError.setText("Please fill in all required fields.");
            lblError.setVisible(true);
            return;
        }

        if (!User.checkNickName(nick)) {
            lblNIckError.setVisible(true);
            return;
        }
        if (!User.checkEmail(email)) {
            lblEmailError.setVisible(true);
            return;
        }
        if (!User.checkPassword(pass)) {
            lblPasswordError.setVisible(true);
            return;
        }
        if (!User.isOlderThan(dob, 12)) {
            labelDateError.setVisible(true);
            return;
        }

        SportActivityApp app = SportActivityApp.getInstance();
        boolean success = app.registerUser(nick, email, pass, dob, avatarPath);

        if (success) {
            System.out.println("User registered successfully!");
            MapaDemoApp.loadView("LoginView.fxml");
        } else {
            lblError.setText("Registration failed. Nickname might already be taken.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        MapaDemoApp.loadView("welcome.fxml");
    }
    
}
