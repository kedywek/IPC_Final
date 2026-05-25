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
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
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
import upv.ipc.sportlib.GeoPoint;
import upv.ipc.sportlib.Annotation;

/**
 * FXML Controller class
 *
 * @author damian
 */
public class MainViewController implements Initializable {

    @FXML
    private ListView<ActivityWrapper> activityList;
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
    private ListView<AnnotationWrapper> annotationList;
    @FXML
    private Group zoomGroup;
    @FXML
    private AreaChart<Number, Number> elevationChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    
    
    private boolean showingChart = false;
    private MapProjection projection;
    /**
     * Initializes the controller class.
     */
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            java.util.List<Activity> savedActivities = SportActivityApp.getInstance().getUserActivities();
            
            if (savedActivities != null) {
                for (Activity activity : savedActivities) {
                    ActivityWrapper wrapper = new ActivityWrapper(activity);
                    activityList.getItems().add(wrapper);
                }
            }
            System.out.println("Successfully restored historical activities from database.");
            
        } catch (Exception e) {
            System.out.println("No active user session found to load activities on startup.");
        } 
    } 
    
    public void displayActivityMap(Activity activity) {
        if (elevationChart != null) {
            elevationChart.setVisible(false);
            elevationChart.setManaged(false);
            if (splitPane != null) {
                splitPane.setDividerPositions(1.0);
            }
            showingChart = false;
        }
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
            double minX = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;

            for (TrackPoint tp : activity.getTrackPoints()) {
                javafx.geometry.Point2D p = projection.project(tp);
                if (p.getX() < minX) minX = p.getX();
                if (p.getX() > maxX) maxX = p.getX();
                if (p.getY() < minY) minY = p.getY();
                if (p.getY() > maxY) maxY = p.getY();
            }

            double midX = (minX + maxX) / 2.0;
            double midY = (minY + maxY) / 2.0;

            map_scrollpane.setHvalue(midX / W);
            map_scrollpane.setVvalue(midY / H);
        }
    }
    
    private void drawRoute(Activity activity) {
        Polyline routeLine = new Polyline();
        routeLine.setStroke(javafx.scene.paint.Color.BLUE); 
        routeLine.setStrokeWidth(4.0); 

        routeLine.setManaged(false);
        routeLine.setLayoutX(0);
        routeLine.setLayoutY(0);

        for (TrackPoint tp : activity.getTrackPoints()) {
            Point2D pixelPoint = projection.project(tp);
            routeLine.getPoints().addAll(pixelPoint.getX(), pixelPoint.getY());
        }

        elevationPane.getChildren().add(routeLine);
        for (upv.ipc.sportlib.Annotation ann : activity.getAnnotations()) {
            if (!ann.getGeoPoints().isEmpty()) {
                upv.ipc.sportlib.GeoPoint gp = ann.getGeoPoints().get(0);
                Point2D pixelLocation = projection.project(gp);

                javafx.scene.shape.Circle markerDot = new javafx.scene.shape.Circle(pixelLocation.getX(), pixelLocation.getY(), 5.0);
                markerDot.setFill(javafx.scene.paint.Color.web(ann.getColor()));

                Label markerLabel = new Label(ann.getText());
                markerLabel.setLayoutX(pixelLocation.getX() + 8);
                markerLabel.setLayoutY(pixelLocation.getY() - 10);
                markerLabel.setStyle("-fx-background-color: white; -fx-padding: 2px; -fx-border-color: black; -fx-font-size: 10px;");

                elevationPane.getChildren().addAll(markerDot, markerLabel);
            }
        }

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
                
                ActivityWrapper wrapper = new ActivityWrapper(importedRun);
                activityList.getItems().add(wrapper);
                
                activityList.getSelectionModel().select(wrapper);
                
                displayActivityMap(importedRun);
                displayActivityStats(importedRun);
                displayActivityAnnotations(importedRun);

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
        SportActivityApp.getInstance().logout(); 
        
        javafx.stage.Stage stage = (javafx.stage.Stage) map_scrollpane.getScene().getWindow();
        stage.close();
        
        System.out.println("User successfully signed out. Session persisted.");
    }
    @FXML
    private void handleMapClick(MouseEvent event) {
        if (event.isSecondaryButtonDown()) { 
            ActivityWrapper selectedWrapper = activityList.getSelectionModel().getSelectedItem();
            if (selectedWrapper == null || projection == null) return;
            
            Activity currentActivity = selectedWrapper.getActivity();

            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("Dangerous Area");
            dialog.setTitle("New Annotation");
            dialog.setHeaderText("Create a map marker note");
            dialog.setContentText("Enter annotation text:");

            java.util.Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                GeoPoint geoPoint = projection.unproject(event.getX(), event.getY());

                upv.ipc.sportlib.Annotation newAnnotation = new upv.ipc.sportlib.Annotation(
                    upv.ipc.sportlib.AnnotationType.POINT,
                    result.get().trim(),
                    "#E74C3C",
                    3.0,
                    java.util.List.of(geoPoint)
                ); 

                SportActivityApp.getInstance().addAnnotation(currentActivity, newAnnotation); 
                displayActivityMap(currentActivity);
                displayActivityAnnotations(currentActivity);
            }
        }
    }
    @FXML
    private void handleEditProfile(ActionEvent event) {
        MapaDemoApp.loadView("EditView.fxml");
    }
    @FXML
    private void handleDeleteActivity(ActionEvent event) {
        ActivityWrapper selectedWrapper = activityList.getSelectionModel().getSelectedItem();
        if (selectedWrapper == null) return;

        SportActivityApp.getInstance().removeActivity(selectedWrapper.getActivity());
        annotationList.getItems().clear();
        activityList.getItems().remove(selectedWrapper);

        elevationPane.getChildren().clear();
        clearStatsHUD();
    }
    
    private void clearStatsHUD() {
        lblDistance.setText("Distance: -");
        lblDuratiom.setText("Duration: -");
        lblSpeed.setText("Avg-speed: -");
        lblPace.setText("Avg-pace: -");
        lblElevUp.setText("Elevation ↑: -");
        lblElevDown.setText("Elevation ↓: -");
        lblMinAlt.setText("Min altitude ↓: -");
        lblMaxAlt.setText("Max altitude ↑: -");
    }
    private void handleSessionHistory(ActionEvent event) {
        MapaDemoApp.loadView("SessionHistory.fxml"); // Or whatever your FXML file is named!
    }

    @FXML
    private void handleAddMap(ActionEvent event) {
    }

    @FXML
    private void handleTotals(ActionEvent event) {
    }

    @FXML
    private void handleElevation(ActionEvent event) {
        ActivityWrapper selectedWrapper = activityList.getSelectionModel().getSelectedItem();
        if (selectedWrapper == null) return;

        Activity activity = selectedWrapper.getActivity();

        if (showingChart && "Elevation Profile".equals(elevationChart.getTitle())) {
            elevationChart.setVisible(false);
            elevationChart.setManaged(false); 
            
            splitPane.setDividerPositions(1.0);
            showingChart = false;
        } else {
            elevationChart.setTitle("Elevation Profile");
            xAxis.setLabel("Distance (km)");
            yAxis.setLabel("Altitude (m)");

            elevationChart.getData().clear();
            elevationChart.setAnimated(false);
            xAxis.setAnimated(false);
            yAxis.setAnimated(false);

            javafx.scene.chart.XYChart.Series<Number, Number> series = new javafx.scene.chart.XYChart.Series<>();
            double accumulatedDistanceMeters = 0;
            TrackPoint lastPoint = null;

            for (TrackPoint tp : activity.getTrackPoints()) { 
                if (lastPoint != null) {
                    accumulatedDistanceMeters += tp.distanceTo(lastPoint); 
                }
                double distanceKm = accumulatedDistanceMeters / 1000.0;
                double altitudeMeters = tp.getElevation();

                series.getData().add(new javafx.scene.chart.XYChart.Data<>(distanceKm, altitudeMeters));
                lastPoint = tp;
            }

            elevationChart.getData().add(series);
            
            elevationChart.setManaged(true);
            elevationChart.setVisible(true);
            
            splitPane.setDividerPositions(0.65);
            showingChart = true;
        }
    }

    @FXML
    private void handleSpeed(ActionEvent event) {
        ActivityWrapper selectedWrapper = activityList.getSelectionModel().getSelectedItem();
        if (selectedWrapper == null) {
            System.out.println("Cannot display chart: No activity selected.");
            return;
        }

        Activity activity = selectedWrapper.getActivity();

        if (showingChart && "Speed Profile".equals(elevationChart.getTitle())) {
            elevationChart.setVisible(false);
            elevationChart.setManaged(false);
            
            splitPane.setDividerPositions(1.0); // Powrót do pełnej mapy
            showingChart = false;
        } else {
            elevationChart.setTitle("Speed Profile");
            xAxis.setLabel("Distance (km)");
            yAxis.setLabel("Speed (km/h)");

            elevationChart.getData().clear();
            elevationChart.setAnimated(false);
            xAxis.setAnimated(false);
            yAxis.setAnimated(false);

            javafx.scene.chart.XYChart.Series<Number, Number> series = new javafx.scene.chart.XYChart.Series<>();

            double accumulatedDistanceMeters = 0;
            java.util.List<TrackPoint> points = activity.getTrackPoints();

            for (int i = 0; i < points.size() - 1; i++) {
                TrackPoint current = points.get(i);
                TrackPoint next = points.get(i + 1);

                double segmentSpeed = current.speedTo(next);
                accumulatedDistanceMeters += current.distanceTo(next);

                double distanceKm = accumulatedDistanceMeters / 1000.0;

                series.getData().add(new javafx.scene.chart.XYChart.Data<>(distanceKm, segmentSpeed));
            }

            elevationChart.getData().add(series);
            
            elevationChart.setManaged(true);
            elevationChart.setVisible(true);
            
            splitPane.setDividerPositions(0.65);
            showingChart = true;
            System.out.println("Speed profile chart successfully rendered.");
        }
    }

    @FXML
    private void listClicked(javafx.scene.input.MouseEvent event) {
        ActivityWrapper selectedWrapper = activityList.getSelectionModel().getSelectedItem();

        if (selectedWrapper != null) {
            Activity selectedActivity = selectedWrapper.getActivity();

            displayActivityMap(selectedActivity);
            displayActivityStats(selectedActivity);
            displayActivityAnnotations(selectedActivity);
        }
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
    @FXML
    private void handleDeleteAnnotation(ActionEvent event) {
        AnnotationWrapper selectedWrapper = annotationList.getSelectionModel().getSelectedItem();
        if (selectedWrapper == null) return;

        Annotation annotationToRemove = selectedWrapper.getAnnotation();

        SportActivityApp.getInstance().removeAnnotation(annotationToRemove);

        annotationList.getItems().remove(selectedWrapper);

        ActivityWrapper trackWrapper = activityList.getSelectionModel().getSelectedItem();
        if (trackWrapper != null) {
            displayActivityMap(trackWrapper.getActivity());
        }
        
        System.out.println("Annotation successfully removed from database, sidebar, and map grid canvas.");
    }
    
    class ActivityWrapper {
        private final Activity activity;

        public ActivityWrapper(Activity activity) {
            this.activity = activity;
        }

        public Activity getActivity() {
            return activity;
        }

        public String toString() {
            String name = activity.getName();
            String date = activity.getStartTime().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            );
            return name + " - " + date;
        }
    }
    private void displayActivityAnnotations(Activity activity) {
        annotationList.getItems().clear();
        
        for (upv.ipc.sportlib.Annotation ann : activity.getAnnotations()) {
            annotationList.getItems().add(new AnnotationWrapper(ann));
        }
    }
    class AnnotationWrapper {
        private final upv.ipc.sportlib.Annotation annotation;

        public AnnotationWrapper(upv.ipc.sportlib.Annotation annotation) {
            this.annotation = annotation;
        }

        public upv.ipc.sportlib.Annotation getAnnotation() {
            return annotation;
        }

        @Override
        public String toString() {
            return annotation.getText();
        }
    }
}
