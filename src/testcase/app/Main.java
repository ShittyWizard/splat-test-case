package testcase.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 *Class "Main" launch the whole javafx-application
 * */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../layouts/layout.fxml"));
        primaryStage.setTitle("Search text in log-files");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();

//        for closing all threads after closing app-window
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
