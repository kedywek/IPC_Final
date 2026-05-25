/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class
 *
 * @author damian
 */
public class CumulativeTotalsController implements Initializable {

    @FXML
    private Label lblTotalTime;
    @FXML
    private Label lblotalDistance;
    @FXML
    private Label lblTotalAscent;
    @FXML
    private Label lblTotaldescent;
    @FXML
    private DatePicker dpStart;
    @FXML
    private DatePicker dpEnd;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        LocalDate today = LocalDate.now();
        dpStart.setValue(today.withDayOfMonth(1));
        dpEnd.setValue(today);
        calculateTotalsForDateRange(dpStart.getValue(), dpEnd.getValue());
    }

    @FXML
    private void handleDateChange(ActionEvent event) {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();
        
        if (start != null && end != null) {
            if (start.isAfter(end)) {
                resetLabelsToZero();
                return;
            }
            calculateTotalsForDateRange(start, end);
        }
    }

    private void calculateTotalsForDateRange(LocalDate start, LocalDate end) {
        List<Activity> activities = SportActivityApp.getInstance().getUserActivities();
        if (activities == null) {
            return;
        }

        long totalSeconds = 0;
        double totalDistance = 0;
        double totalAscent = 0;
        double totalDescent = 0;

        for (Activity activity : activities) {
            LocalDate activityDate = activity.getStartTime().toLocalDate();
            
            if (!activityDate.isBefore(start) && !activityDate.isAfter(end)) {
                totalSeconds += activity.getDuration().toSeconds();
                totalDistance += activity.getTotalDistance();
                totalAscent += activity.getElevationGain();
                totalDescent += activity.getElevationLoss();
            }
        }

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        lblTotalTime.setText(String.format("Total time: %dh %02dmin", hours, minutes));
        lblotalDistance.setText(String.format("Total distance: %.2f km", totalDistance / 1000.0));
        lblTotalAscent.setText(String.format("Total ascent: %.0f m", totalAscent));
        lblTotaldescent.setText(String.format("Total descent: %.0f m", totalDescent));
    }

    private void resetLabelsToZero() {
        lblTotalTime.setText("Total time: 0h 00min");
        lblotalDistance.setText("Total distance: 0.00 km");
        lblTotalAscent.setText("Total ascent: 0 m");
        lblTotaldescent.setText("Total descent: 0 m");
    }

    @FXML
    private void handleCls(ActionEvent event) {
        MapaDemoApp.loadView("MainView.fxml");
    }
}