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
import upv.ipc.sportlib.AnnotationType;

/**
 * FXML Controller class
 *
 * @author damian
 */
public class MainController implements Initializable {

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
    private java.util.Set<Annotation> deletedAnnotations = new java.util.HashSet<>();
    private boolean waitingForLineEnd = false;
    private GeoPoint pendingLineStart = null;
    private Activity pendingActivity = null;
    private javafx.scene.shape.Polyline tempLine = null;
    private boolean waitingForCircleEnd = false;
    private GeoPoint pendingCircleCenter = null;
    private javafx.scene.shape.Circle tempCircle = null;
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
        MapRegion region = SportActivityApp.getInstance().findMapForActivity(activity);
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
        for (Annotation ann : activity.getAnnotations()) {
            if (deletedAnnotations.contains(ann)) continue; 
            
            if (!ann.getGeoPoints().isEmpty()) {
                GeoPoint gp = ann.getGeoPoints().get(0);
                Point2D pixelLocation = projection.project(gp);

                if (pixelLocation != null) {
                    double px = pixelLocation.getX();
                    double py = pixelLocation.getY();
                    javafx.scene.paint.Color color = javafx.scene.paint.Color.web(ann.getColor());
                    
                    javafx.scene.Node shape;
                    
                    if (ann.getType() == AnnotationType.CIRCLE) {
                        if (ann.getGeoPoints().size() > 1) { 
                            Point2D centerPixel = projection.project(ann.getGeoPoints().get(0));
                            Point2D edgePixel = projection.project(ann.getGeoPoints().get(1));

                            if (centerPixel != null && edgePixel != null) {
                                double dx = edgePixel.getX() - centerPixel.getX();
                                double dy = edgePixel.getY() - centerPixel.getY();
                                double radiusPixels = Math.sqrt(dx * dx + dy * dy);

                                javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(centerPixel.getX(), centerPixel.getY(), radiusPixels);
                                circle.setStroke(color);
                                circle.setStrokeWidth(ann.getStrokeWidth()); 
                                circle.setFill(javafx.scene.paint.Color.TRANSPARENT);
                                shape = circle;
                            } else {
                                shape = new javafx.scene.shape.Circle(px, py, 15.0);
                                ((javafx.scene.shape.Circle)shape).setStroke(color);
                                ((javafx.scene.shape.Circle)shape).setFill(javafx.scene.paint.Color.TRANSPARENT);
                            }
                        } else {
                            shape = new javafx.scene.shape.Circle(px, py, 15.0);
                            ((javafx.scene.shape.Circle)shape).setStroke(color);
                            ((javafx.scene.shape.Circle)shape).setFill(javafx.scene.paint.Color.TRANSPARENT);
                        }
                    } else if (ann.getType() == AnnotationType.LINE) {
                        if (ann.getGeoPoints().size() > 1) { 
                            GeoPoint endGp = ann.getGeoPoints().get(1);
                            Point2D endPixel = projection.project(endGp);
                            if (endPixel != null) {
                                javafx.scene.shape.Line line = new javafx.scene.shape.Line(px, py, endPixel.getX(), endPixel.getY());
                                line.setStroke(color);
                                line.setStrokeWidth(4.0);
                                shape = line;
                            } else {
                                shape = new javafx.scene.shape.Line(px - 10, py - 10, px + 10, py + 10);
                                ((javafx.scene.shape.Line)shape).setStroke(color);
                                ((javafx.scene.shape.Line)shape).setStrokeWidth(4.0);
                            }
                        } else {
                            shape = new javafx.scene.shape.Line(px - 10, py - 10, px + 10, py + 10);
                            ((javafx.scene.shape.Line)shape).setStroke(color);
                            ((javafx.scene.shape.Line)shape).setStrokeWidth(4.0);
                        }
                    } else if (ann.getType() == AnnotationType.TEXT) {
                        shape = new javafx.scene.shape.Circle(px, py, 1.0, javafx.scene.paint.Color.TRANSPARENT); 
                    } else {
                        shape = new javafx.scene.shape.Circle(px, py, 5.0, color);
                    }

                    Label markerLabel = new Label(ann.getText());
                    markerLabel.setLayoutX(px + 8);
                    markerLabel.setLayoutY(py - 10);
                    
                    if (ann.getType() == AnnotationType.TEXT) {
                        markerLabel.setStyle("-fx-text-fill: " + ann.getColor() + "; -fx-font-weight: bold; -fx-font-size: 14px;");
                    } else {
                        markerLabel.setStyle("-fx-background-color: white; -fx-padding: 2px; -fx-border-color: black; -fx-font-size: 10px;");
                    }

                    elevationPane.getChildren().addAll(shape, markerLabel);
                }
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
        if ((waitingForLineEnd || waitingForCircleEnd) && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
            GeoPoint pendingEnd = projection.unproject(event.getX(), event.getY());
            map_scrollpane.setCursor(javafx.scene.Cursor.DEFAULT);

            if (waitingForLineEnd) {
                waitingForLineEnd = false;
                if (tempLine != null) {
                    elevationPane.getChildren().remove(tempLine);
                    tempLine = null;
                }
                showLineAnnotationDialog(pendingActivity, pendingLineStart, pendingEnd);
            } 
            else if (waitingForCircleEnd) {
                waitingForCircleEnd = false;
                if (tempCircle != null) {
                    elevationPane.getChildren().remove(tempCircle);
                    tempCircle = null;
                }
                showCircleAnnotationDialog(pendingActivity, pendingCircleCenter, pendingEnd);
            }
            return;
        }

        if (event.isSecondaryButtonDown()) {
            if (waitingForLineEnd || waitingForCircleEnd) {
                waitingForLineEnd = false;
                waitingForCircleEnd = false;
                map_scrollpane.setCursor(javafx.scene.Cursor.DEFAULT);
                if (tempLine != null) { elevationPane.getChildren().remove(tempLine); tempLine = null; }
                if (tempCircle != null) { elevationPane.getChildren().remove(tempCircle); tempCircle = null; }
                return; 
            }

            ActivityWrapper selectedWrapper = activityList.getSelectionModel().getSelectedItem();
            if (selectedWrapper == null || projection == null) return;

            Activity currentActivity = selectedWrapper.getActivity();
            GeoPoint geoPoint = projection.unproject(event.getX(), event.getY());

            javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
            javafx.scene.control.MenuItem ptItem = new javafx.scene.control.MenuItem("Point");
            javafx.scene.control.MenuItem txtItem = new javafx.scene.control.MenuItem("Text");
            javafx.scene.control.MenuItem lineItem = new javafx.scene.control.MenuItem("Line");
            javafx.scene.control.MenuItem circleItem = new javafx.scene.control.MenuItem("Circle");

            javafx.event.EventHandler<javafx.event.ActionEvent> menuHandler = e -> {
                javafx.scene.control.MenuItem source = (javafx.scene.control.MenuItem) e.getSource();
                AnnotationType type = AnnotationType.valueOf(source.getText().toUpperCase());

                if (type == AnnotationType.LINE) {
                    waitingForLineEnd = true;
                    pendingLineStart = geoPoint;
                    pendingActivity = currentActivity;
                    map_scrollpane.setCursor(javafx.scene.Cursor.CROSSHAIR);

                    javafx.geometry.Point2D startPixel = projection.project(geoPoint);
                    tempLine = new javafx.scene.shape.Polyline(startPixel.getX(), startPixel.getY(), startPixel.getX(), startPixel.getY());
                    tempLine.setStroke(javafx.scene.paint.Color.RED);
                    tempLine.setStrokeWidth(2.0);
                    tempLine.getStrokeDashArray().addAll(5d, 5d); 
                    tempLine.setManaged(false);
                    tempLine.setMouseTransparent(true); 

                    elevationPane.getChildren().add(tempLine);
                } 
                else if (type == AnnotationType.CIRCLE) {
                    waitingForCircleEnd = true;
                    pendingCircleCenter = geoPoint;
                    pendingActivity = currentActivity;
                    map_scrollpane.setCursor(javafx.scene.Cursor.CROSSHAIR);

                    javafx.geometry.Point2D startPixel = projection.project(geoPoint);
                    tempCircle = new javafx.scene.shape.Circle(startPixel.getX(), startPixel.getY(), 0);
                    tempCircle.setStroke(javafx.scene.paint.Color.RED);
                    tempCircle.setStrokeWidth(2.0);
                    tempCircle.getStrokeDashArray().addAll(5d, 5d);
                    tempCircle.setFill(javafx.scene.paint.Color.TRANSPARENT);
                    tempCircle.setManaged(false);
                    tempCircle.setMouseTransparent(true);

                    elevationPane.getChildren().add(tempCircle);
                } 
                else {
                    showAnnotationDialog(currentActivity, geoPoint, type);
                }
            };

            ptItem.setOnAction(menuHandler);
            txtItem.setOnAction(menuHandler);
            lineItem.setOnAction(menuHandler);
            circleItem.setOnAction(menuHandler);

            contextMenu.getItems().addAll(ptItem, txtItem, lineItem, circleItem);
            contextMenu.show(elevationPane, event.getScreenX(), event.getScreenY());
        }
    }

    private void showCircleAnnotationDialog(Activity activity, GeoPoint centerPoint, GeoPoint edgePoint) {
        javafx.scene.control.Dialog<Annotation> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("New Annotation");
        dialog.setHeaderText("Create a circle annotation");

        javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 50, 10, 10));

        javafx.scene.control.TextField textNote = new javafx.scene.control.TextField();
        textNote.setPromptText("Enter text");
        javafx.scene.control.ColorPicker colorPicker = new javafx.scene.control.ColorPicker(javafx.scene.paint.Color.web("#E74C3C"));

        grid.add(new Label("Text note:"), 0, 0);
        grid.add(textNote, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                javafx.scene.paint.Color c = colorPicker.getValue();
                String hexColor = String.format("#%02X%02X%02X",
                        (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));

                java.util.List<GeoPoint> points = new java.util.ArrayList<>();
                points.add(centerPoint);  
                points.add(edgePoint);   

                return new Annotation(AnnotationType.CIRCLE, textNote.getText().trim(), hexColor, 3.0, points);
            }
            return null;
        });

        java.util.Optional<Annotation> result = dialog.showAndWait();
        if (result.isPresent()) {
            SportActivityApp.getInstance().addAnnotation(activity, result.get()); 
            displayActivityMap(activity);
            displayActivityAnnotations(activity);
        }
    }
    private void showAnnotationDialog(Activity activity, GeoPoint gp, AnnotationType type) {
        javafx.scene.control.Dialog<Annotation> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("New Annotation");
        dialog.setHeaderText("Create a " + type.name().toLowerCase() + " annotation");

        javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 50, 10, 10));

        javafx.scene.control.TextField textNote = new javafx.scene.control.TextField();
        textNote.setPromptText("Enter text");
        javafx.scene.control.ColorPicker colorPicker = new javafx.scene.control.ColorPicker(javafx.scene.paint.Color.web("#E74C3C"));

        grid.add(new Label("Text note:"), 0, 0);
        grid.add(textNote, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);

        javafx.scene.control.TextField radiusField = new javafx.scene.control.TextField("15.0");

        if (type == AnnotationType.CIRCLE) {
            grid.add(new Label("Radius (px):"), 0, 2);
            grid.add(radiusField, 1, 2);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                javafx.scene.paint.Color c = colorPicker.getValue();
                String hexColor = String.format("#%02X%02X%02X",
                        (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
                        
                double sizeOrRadius = 3.0;
                java.util.List<GeoPoint> points = new java.util.ArrayList<>();
                points.add(gp);

                if (type == AnnotationType.CIRCLE) {
                    try {
                        sizeOrRadius = Double.parseDouble(radiusField.getText().trim().replace(",", "."));
                    } catch (Exception e) {
                        sizeOrRadius = 15.0; 
                    }
                }

                return new Annotation(type, textNote.getText().trim(), hexColor, sizeOrRadius, points);
            }
            return null;
        });

        java.util.Optional<Annotation> result = dialog.showAndWait();
        if (result.isPresent()) {
            SportActivityApp.getInstance().addAnnotation(activity, result.get()); 
            displayActivityMap(activity);
            displayActivityAnnotations(activity);
        }
    }

    private void showLineAnnotationDialog(Activity activity, GeoPoint startPoint, GeoPoint endPoint) {
        javafx.scene.control.Dialog<Annotation> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("New Annotation");
        dialog.setHeaderText("Create a line annotation");

        javafx.scene.control.ButtonType saveButtonType = new javafx.scene.control.ButtonType("Save", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 50, 10, 10));

        javafx.scene.control.TextField textNote = new javafx.scene.control.TextField();
        textNote.setPromptText("Enter text");
        javafx.scene.control.ColorPicker colorPicker = new javafx.scene.control.ColorPicker(javafx.scene.paint.Color.web("#E74C3C"));

        grid.add(new Label("Text note:"), 0, 0);
        grid.add(textNote, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                javafx.scene.paint.Color c = colorPicker.getValue();
                String hexColor = String.format("#%02X%02X%02X",
                        (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
                        
                java.util.List<GeoPoint> points = new java.util.ArrayList<>();
                points.add(startPoint);
                points.add(endPoint); 

                return new Annotation(AnnotationType.LINE, textNote.getText().trim(), hexColor, 3.0, points);
            }
            return null;
        });

        java.util.Optional<Annotation> result = dialog.showAndWait();
        if (result.isPresent()) {
            SportActivityApp.getInstance().addAnnotation(activity, result.get()); 
            displayActivityMap(activity);
            displayActivityAnnotations(activity);
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
    
    @FXML
    private void handleSessionHistory(ActionEvent event) {
        MapaDemoApp.loadView("SessionHistoryView.fxml"); 
    }

    @FXML
    private void handleAddMap(ActionEvent event) {
        MapaDemoApp.loadView("AddMappView.fxml"); 
    }

    @FXML
    private void handleTotals(ActionEvent event) {
        MapaDemoApp.loadView("CumulativeTotalsView.fxml"); 
    }

    @FXML
    private void handleElevation(ActionEvent event) {
        ActivityWrapper selectedWrapper = activityList.getSelectionModel().getSelectedItem();
        if (selectedWrapper == null) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("No Activity Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an activity from the list to view its elevation profile.");
            alert.showAndWait();
            return;
        }
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
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("No Activity Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an activity from the list to view its speed profile.");
            alert.showAndWait();
            return;
        }
        
        Activity activity = selectedWrapper.getActivity();

        if (showingChart && "Speed Profile".equals(elevationChart.getTitle())) {
            elevationChart.setVisible(false);
            elevationChart.setManaged(false);
            
            splitPane.setDividerPositions(1.0);
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

            if (newScale >= 0.2) {
                zoomGroup.setScaleX(newScale);
                zoomGroup.setScaleY(newScale);
            }
        }
    }

    @FXML
    private void showPosition(MouseEvent event) {
        Point2D exactMapPixel = elevationPane.sceneToLocal(event.getSceneX(), event.getSceneY());
        if (exactMapPixel == null) return;

        double mapX = exactMapPixel.getX();
        double mapY = exactMapPixel.getY();

        if (waitingForLineEnd && tempLine != null && tempLine.getPoints().size() == 4) {
            tempLine.getPoints().set(2, mapX);
            tempLine.getPoints().set(3, mapY);
        }
        else if (waitingForCircleEnd && tempCircle != null) {
            double dx = mapX - tempCircle.getCenterX();
            double dy = mapY - tempCircle.getCenterY();
            double r = Math.sqrt(dx * dx + dy * dy); 
            tempCircle.setRadius(r);
        }

        if (projection != null && mousePosition != null) {
            GeoPoint gp = projection.unproject(mapX, mapY);
            mousePosition.setText(String.format("GPS: Lat: %.5f, Lon: %.5f", gp.getLatitude(), gp.getLongitude()));
        } else if (mousePosition != null) {
            mousePosition.setText("");
        }
    }
    
    @FXML
    private void handleDeleteAnnotation(ActionEvent event) {
        AnnotationWrapper selectedWrapper = annotationList.getSelectionModel().getSelectedItem();
        if (selectedWrapper == null) return;

        Annotation annotationToRemove = selectedWrapper.getAnnotation();
        ActivityWrapper trackWrapper = activityList.getSelectionModel().getSelectedItem();
        
        if (trackWrapper != null) {
            Activity activity = trackWrapper.getActivity();
            SportActivityApp.getInstance().removeAnnotation(annotationToRemove);
            deletedAnnotations.add(annotationToRemove);
            annotationList.getItems().remove(selectedWrapper);

            displayActivityMap(trackWrapper.getActivity());
        }
    }
    @FXML
    private void handleAbout(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("AboutView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            javafx.stage.Stage aboutStage = new javafx.stage.Stage();
            aboutStage.setTitle("About");
            aboutStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            aboutStage.setResizable(false);
            aboutStage.setScene(new javafx.scene.Scene(root));
            
            aboutStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        
        for (Annotation ann : activity.getAnnotations()) {
            if (!deletedAnnotations.contains(ann)) {
                annotationList.getItems().add(new AnnotationWrapper(ann));
            }
        }
    }
    class AnnotationWrapper {
        private final Annotation annotation;

        public AnnotationWrapper(Annotation annotation) {
            this.annotation = annotation;
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        @Override
        public String toString() {
            return annotation.getText();
        }
    }
}