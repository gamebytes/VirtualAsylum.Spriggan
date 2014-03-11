package org.virtualAsylum.spriggan;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        SprigganRepositoryManager.loadRepository();
        Scene primaryScene = new Scene(MainInterface.instance, 800, 600);
        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
