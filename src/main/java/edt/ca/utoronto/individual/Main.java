package edt.ca.utoronto.individual;

import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String uri = getClass().getResource("/css/stylecomp.css").toExternalForm();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        JFXDecorator decorator = new JFXDecorator(primaryStage, root);
        decorator.setText("PascalCoinFX Client");
        ImageView ico = new ImageView("image/favicon.png");
        ico.setFitHeight(30);
        ico.setFitWidth(30);
        decorator.setGraphic(ico);
        decorator.setCustomMaximize(true);
        Scene sc = new Scene(decorator, 900, 600);
        sc.getStylesheets().add(uri);
        primaryStage.setScene(sc);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
