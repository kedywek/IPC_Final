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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class
 *
 * @author damian
 */
public class LoginController implements Initializable {

    @FXML
    private TextField txtUsername;
    @FXML
    private Label lblErrorNick;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblErrorPswd;
    @FXML
    private Button btnSign;
    @FXML
    private Button btnSign1;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    

    @FXML
    private void handleSign(ActionEvent event) {
        String nick = txtUsername.getText().trim();
        String pass = txtPassword.getText();
        lblErrorNick.setText("");
        lblErrorPswd.setText("");
        lblErrorNick.setVisible(false);
        lblErrorPswd.setVisible(false);
        if (nick.isEmpty() || pass.isEmpty()) {
            if(nick.isEmpty()){
                lblErrorNick.setText("Please enter username.");
                lblErrorNick.setVisible(true);
            }
            if(pass.isEmpty()){
                lblErrorPswd.setText("Please enter password.");
                lblErrorPswd.setVisible(true);
            }
            return;
        }

        SportActivityApp app = SportActivityApp.getInstance();
        
        boolean isAuthenticated = app.login(nick, pass);

        if (isAuthenticated) {
            System.out.println("Login successful! Welcome, " + nick);
            
            MapaDemoApp.loadView("MainView.fxml");
        } else {
            lblErrorPswd.setText("Invalid username or password.");
            lblErrorPswd.setVisible(true);
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        MapaDemoApp.loadView("welcomeView.fxml");
    }
    
}
