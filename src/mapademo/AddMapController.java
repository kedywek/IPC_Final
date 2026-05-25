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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class
 *
 * @author damian
 */
public class AddMapController implements Initializable {

    @FXML
    private TextField txtRegionName;
    @FXML
    private TextField txtImage;
    @FXML
    private Button btnBrowse;
    @FXML
    private TextField txtLatMin;
    @FXML
    private TextField txtLatMax;
    @FXML
    private TextField txtLonMin;
    @FXML
    private TextField txtLonMax;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnAdd;

    private File selectedFile;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void handleBrowse(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png"));
        Stage stage = (Stage) btnBrowse.getScene().getWindow();
        selectedFile = fc.showOpenDialog(stage);
        
        if (selectedFile != null) {
            txtImage.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        MapaDemoApp.loadView("MainView.fxml");
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        try {
            String name = txtRegionName.getText().trim();
            String imgPath = txtImage.getText().trim();
            double latMin = Double.parseDouble(txtLatMin.getText().trim());
            double latMax = Double.parseDouble(txtLatMax.getText().trim());
            double lonMin = Double.parseDouble(txtLonMin.getText().trim());
            double lonMax = Double.parseDouble(txtLonMax.getText().trim());

            if (name.isEmpty() || imgPath.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Missing Data");
                alert.setHeaderText(null);
                alert.setContentText("Please fill in the region name and select an image.");
                alert.showAndWait();
                return;
            }

            File imgFile = new File(imgPath);
            SportActivityApp.getInstance().addMapRegion(name, imgFile, latMin, latMax, lonMin, lonMax);

            MapaDemoApp.loadView("MainView.fxml");

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Format Error");
            alert.setHeaderText(null);
            alert.setContentText("Please enter valid numeric values for coordinates.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}