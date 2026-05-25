package mapademo;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

/**
 * FXML Controller class for browsing historical usage logs with aggregated totals.
 * @author damian
 */
public class SessionHistoryController implements Initializable {

    @FXML
    private TableView<SessionWrapper> sessionTable;
    @FXML
    private TableColumn<SessionWrapper, String> colStart; 
    @FXML
    private TableColumn<SessionWrapper, String> colEnd; 
    @FXML
    private TableColumn<SessionWrapper, String> colDur; 
    @FXML
    private TableColumn<SessionWrapper, Integer> colImported;
    @FXML
    private TableColumn<SessionWrapper, Integer> colViewed;
    @FXML
    private TableColumn<SessionWrapper, Integer> colAnnotations;
    @FXML
    private Label lblTotalDur;
    @FXML
    private Label lblTotalImported;
    @FXML
    private Label lblTotalViewed;
    @FXML
    private Label lblTotalAnnotations;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colStart.setCellValueFactory(new PropertyValueFactory<>("startText"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endText"));
        colDur.setCellValueFactory(new PropertyValueFactory<>("durationText"));
        colImported.setCellValueFactory(new PropertyValueFactory<>("imported"));
        colViewed.setCellValueFactory(new PropertyValueFactory<>("viewed"));
        colAnnotations.setCellValueFactory(new PropertyValueFactory<>("annotations"));

        User currentUser = SportActivityApp.getInstance().getCurrentUser(); 
        if (currentUser != null) {
            List<Session> userSessions = SportActivityApp.getInstance().getSessionsByUser(currentUser); 
            
            ObservableList<SessionWrapper> wrappedSessions = FXCollections.observableArrayList();
            
            long totalMinutesAccumulated = 0;
            int totalImportedCount = 0;
            int totalViewedCount = 0;
            int totalAnnotationsCount = 0;

            for (Session s : userSessions) {
                SessionWrapper wrapper = new SessionWrapper(s);
                wrappedSessions.add(wrapper);
                
                totalMinutesAccumulated += wrapper.getDurationMinutes();
                totalImportedCount += wrapper.getImported();
                totalViewedCount += wrapper.getViewed();
                totalAnnotationsCount += wrapper.getAnnotations();
            }

                lblTotalDur.setText(String.format("Total duration: %d min", totalMinutesAccumulated));
            lblTotalImported.setText("Total imported: " + totalImportedCount);
            lblTotalViewed.setText("Total viewed: " + totalViewedCount);
            lblTotalAnnotations.setText("Total annotations: " + totalAnnotationsCount);
            
            sessionTable.setItems(wrappedSessions);
        } else {
            lblTotalDur.setText("Total duration: -");
            lblTotalImported.setText("Total imported: -");
            lblTotalViewed.setText("Total viewed: -");
            lblTotalAnnotations.setText("Total annotations: -");
        }
    }

    @FXML
    private void handleclose(ActionEvent event) {
        MapaDemoApp.loadView("MainView.fxml");
        System.out.println("Exited session ledger context view. Returned to Main Workspace.");
    }
    public static class SessionWrapper {
        private final String startText;
        private final String endText;
        private final String durationText;
        private final int imported;
        private final int viewed;
        private final int annotations;
        private long durationMinutes = 0;

        public SessionWrapper(Session session) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            this.imported = session.getImportedActivities();
            this.viewed = session.getViewedActivities();
            this.annotations = session.getAnnotationsCreated();

            this.startText = session.getStartTime() != null ? session.getStartTime().format(formatter) : "-";

            this.endText = session.getEndTime() != null ? session.getEndTime().format(formatter) : "-";

            if (session.getStartTime() != null && session.getEndTime() != null) {
                Duration d = Duration.between(session.getStartTime(), session.getEndTime());
                this.durationMinutes = d.toMinutes();
                this.durationText = this.durationMinutes + " min";
            } else {
                this.durationText = "-";
            }
        }

        public String getStartText() { return startText; }
        public String getEndText() { return endText; }
        public String getDurationText() { return durationText; }
        public int getImported() { return imported; }
        public int getViewed() { return viewed; }
        public int getAnnotations() { return annotations; }
        public long getDurationMinutes() { return durationMinutes; }
    }
}