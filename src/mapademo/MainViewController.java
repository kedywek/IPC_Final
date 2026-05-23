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
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.TrackPoint;

/**
 * FXML Controller class
 *
 * @author damian
 */
public class MainViewController implements Initializable {

    @FXML
    private ListView<?> activityList;
    @FXML
    private Label mousePosition;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ScrollPane map_scrollpane;
    @FXML
    private Pane elevationPane;
    @FXML
    private Button btnZoomIn;
    @FXML
    private Button btnZoomOut;
    @FXML
    private Label lblDistance;
    @FXML
    private Label lblDuratiom;
    @FXML
    private Label lblSpeed;
    @FXML
    private Label lblPace;
    @FXML
    private Label lblElevUp;
    @FXML
    private Label lblElevDown;
    @FXML
    private Label lblMinAlt;
    @FXML
    private Label lblMaxAlt;
    @FXML
    private ListView<?> annotationList;
    @FXML
    private Group zoomGroup;

    private Pane mapPane;
    private MapProjection projection;
    /**
     * Initializes the controller class.
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    } 
    
    public void displayActivityMap(Activity activity) {
        MapRegion region = activity.getSuggestedMap();
        if (region == null) return;

        File imgFile = new File(region.getImagePath());
        if (!imgFile.exists()) return;

        Image img = new Image(imgFile.toURI().toString(), false);
        double W = img.getWidth();
        double H = img.getHeight();

        elevationPane.getChildren().clear();
        zoomGroup.setScaleX(1.0);
        zoomGroup.setScaleY(1.0);

        elevationPane.setPrefSize(W, H);
        elevationPane.setMinSize(W, H);
        elevationPane.setMaxSize(W, H);

        ImageView iv = new ImageView(img);
        iv.setFitWidth(W);
        iv.setFitHeight(H);
        elevationPane.getChildren().add(iv);

        projection = new MapProjection(region, W, H);
        drawRoute(activity);
        if (!activity.getTrackPoints().isEmpty()) {
            TrackPoint firstPoint = activity.getTrackPoints().get(0);

            javafx.geometry.Point2D pixelPoint = projection.project(firstPoint);

            double hValue = pixelPoint.getX() / W;
            double vValue = pixelPoint.getY() / H;

            map_scrollpane.setHvalue(hValue);
            map_scrollpane.setVvalue(vValue);
        }
    }
    
    private void drawRoute(Activity activity) {
        Polyline routeLine = new Polyline();
        routeLine.setStroke(javafx.scene.paint.Color.BLUE); 
        routeLine.setStrokeWidth(4.0); // Nice and visible thickness

        routeLine.setManaged(false);
        routeLine.setLayoutX(0);
        routeLine.setLayoutY(0);

        for (TrackPoint tp : activity.getTrackPoints()) {
            Point2D pixelPoint = projection.project(tp);
            routeLine.getPoints().addAll(pixelPoint.getX(), pixelPoint.getY());
        }

        elevationPane.getChildren().add(routeLine);

        System.out.println("Route path successfully deployed with point array length: " + routeLine.getPoints().size());
    }
    
    @FXML
    private void handleImportGPX(javafx.event.ActionEvent event) {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Open GPX Run File");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("GPX Files", "*.gpx"));

        javafx.stage.Stage stage = (javafx.stage.Stage) map_scrollpane.getScene().getWindow();

        java.io.File file = fc.showOpenDialog(stage);
        if (file != null) {
            try {
                Activity importedRun = SportActivityApp.getInstance().importActivity(file);

                displayActivityMap(importedRun);
                displayActivityStats(importedRun);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void displayActivityStats(Activity activity) {

        double distanceKm = activity.getTotalDistance() / 1000.0;
        lblDistance.setText(String.format("Distance: %.2f km", distanceKm));

        long totalSeconds = activity.getDuration().toSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        lblDuratiom.setText(String.format("Duration: %d:%02d min", minutes, seconds));

        lblSpeed.setText(String.format("Avg-speed: %.1f km/h", activity.getAverageSpeed()));
        lblPace.setText(String.format("Avg-pace: %.2f min/km", activity.getAveragePace()));

        lblElevUp.setText(String.format("Elevation ↑: %.0fm", activity.getElevationGain()));
        lblElevDown.setText(String.format("Elevation ↓: %.0fm", activity.getElevationLoss()));

        lblMinAlt.setText(String.format("Min altitude ↓: %.0fm", activity.getMinElevation()));
        lblMaxAlt.setText(String.format("Max altitude ↑: %.0fm", activity.getMaxElevation()));
    }
    
    @FXML
    private void handleSignOut(ActionEvent event) {
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
    }

    @FXML
    private void handleSessionHistory(ActionEvent event) {
    }

    @FXML
    private void handleAddMap(ActionEvent event) {
    }

    @FXML
    private void handleTotals(ActionEvent event) {
    }

    @FXML
    private void handleElevation(ActionEvent event) {
    }

    @FXML
    private void handleSpeed(ActionEvent event) {
    }

    @FXML
    private void listClicked(MouseEvent event) {
    }

@FXML
    private void handleZoomIn(ActionEvent event) {
        if (zoomGroup != null) {
            double currentScale = zoomGroup.getScaleX();
            
            double newScale = currentScale + 0.1;
            
            zoomGroup.setScaleX(newScale);
            zoomGroup.setScaleY(newScale);
        }
    }

    @FXML
    private void handleZoomOut(ActionEvent event) {
        if (zoomGroup != null) {
            double currentScale = zoomGroup.getScaleX();
            
            double newScale = currentScale - 0.1;

            zoomGroup.setScaleX(newScale);
            zoomGroup.setScaleY(newScale);
        }
    }

    @FXML
    private void showPosition(MouseEvent event) {
    }
    
}
