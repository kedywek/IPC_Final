/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.net.URL;
import java.time.YearMonth;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        calculateCurrentMonthTotals();
    }

    private void calculateCurrentMonthTotals() {
        List<Activity> activities = SportActivityApp.getInstance().getUserActivities();
        if (activities == null) {
            return;
        }

        YearMonth currentMonth = YearMonth.now();

        long totalSeconds = 0;
        double totalDistance = 0;
        double totalAscent = 0;
        double totalDescent = 0;

        for (Activity activity : activities) {
            if (YearMonth.from(activity.getStartTime()).equals(currentMonth)) {
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

    @FXML
    private void handleCls(ActionEvent event) {
        MapaDemoApp.loadView("MainView.fxml");
    }
}