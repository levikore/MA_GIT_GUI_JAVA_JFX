import components.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader();
        URL mainFXML = getClass().getResource("components/MainGui.fxml");
        loader.setLocation(mainFXML);
        BorderPane root = loader.load();

        // wire up controller
        MainController mainController = loader.getController();
        mainController.setPrimaryStage(primaryStage);

        // set stage
        primaryStage.setTitle("M.A GIT");
        final Scene scene = new Scene(root, 1050, 600);


        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

