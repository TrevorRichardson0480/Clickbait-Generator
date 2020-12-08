import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javafx.scene.control.Button;

public class ClickbaitGeneratorGui extends Application {
    String countryCode = "US";
    String topic = "None";
    String topicName = "None";
    int numTitles = 10;


    public void start(Stage appStage) throws IOException {
        // make the main grid, add the grid to the main scene
        GridPane mainGrid = new GridPane();
        Scene mainScene = new Scene(mainGrid);

        // text field
        String output = "";
        List<String> lines = Files.readAllLines(Paths.get("output/titles.txt"), StandardCharsets.US_ASCII);

        while (!lines.isEmpty()) {
            output += lines.remove(0) + "\n";
        }

        TextArea outputField = new TextArea();
        outputField.setPrefColumnCount(20);
        outputField.setEditable(false);
        outputField.setText(output);

        // create buttons
        Button clearBTN = new Button("Clear");
        Button startBTN = new Button("Start");
        Button settingsBTN = new Button("Settings");
        clearBTN.setMinWidth(75);
        settingsBTN.setMinWidth(75);
        startBTN.setMinWidth(75);

        // create insets and grid padding
        Insets mainGridPadding = new Insets(10, 10, 10, 10);
        mainGrid.setPadding(mainGridPadding);
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);

        // add all elements to the grid
        mainGrid.add(outputField, 0, 0, 4, 1);
        mainGrid.add(clearBTN, 2, 1);
        mainGrid.add(startBTN, 3, 1);
        mainGrid.add(settingsBTN, 1, 1);

        clearBTN.setOnAction(event -> {
            outputField.setText("");
        });

        settingsBTN.setOnAction(event -> {
            showSettingsWindow();
        });

        startBTN.setOnAction(event -> {
            try {
                for (int i = 0; i < 20; i++) {
                    File file = new File("../outputFiles/outputFile" + i + ".txt");
                    file.delete();
                }

                FileWriter execute = new FileWriter("run.bat");
                execute.write("python YTAPI.py " + countryCode + " " + topic + "\n");
                execute.close();

                processCommandStage("wscript run.vbs", "../outputFiles", 20);
                appStage.close();
                return;

            } catch (IOException e) {
                e.printStackTrace();

            }
        });

        // display window
        appStage.setScene(mainScene);
        appStage.setTitle("Clickbait Generator");
        appStage.setResizable(false);
        appStage.setWidth(appStage.getWidth() + 100);

        // open application windows
        appStage.show();
    }


    public void showSettingsWindow() {
        GridPane gridPane = new GridPane();

        Insets paddingField = new Insets(10, 10, 10, 10);

        gridPane.setPadding(paddingField);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label numberOfTitlesLabel = new Label("Number of Titles:");
        Label countryCodeLabel = new Label("ISO 3166 Alpha-2 Country Code:");
        Label topicLabel = new Label ("Topic:");

        Button saveBTN = new Button("Save");
        Button cancelBTN = new Button("Cancel");
        saveBTN.setMinWidth(75);
        cancelBTN.setMinWidth(75);

        ObservableList<String> options =
                FXCollections.observableArrayList(
                        "None",
                        "Autos & Vehicles",
                        "Film & Animation",
                        "Music",
                        "Pets & Animals",
                        "Sports",
                        "Travel & Events",
                        "Gaming",
                        "People & Blogs",
                        "Comedy",
                        "Entertainment",
                        "News & Politics",
                        "Howto & Style",
                        "Education",
                        "Science & Technology",
                        "Nonprofits & Activism"
                );

        ComboBox comboBox = new ComboBox(options);
        comboBox.getSelectionModel().select(topicName);

        Spinner<Integer> numberOfTitlesField = new Spinner<Integer>(1, 20, numTitles, 1);
        TextField countryCodeField = new TextField(countryCode);

        gridPane.add(numberOfTitlesLabel, 0, 0);
        gridPane.add(numberOfTitlesField, 1, 0, 2, 1);
        gridPane.add(countryCodeLabel, 0, 1);
        gridPane.add(countryCodeField, 1, 1, 2, 1);
        gridPane.add(topicLabel, 0, 2);
        gridPane.add(comboBox, 1, 2, 2, 1);
        gridPane.add(saveBTN, 1, 3);
        gridPane.add(cancelBTN, 2, 3);

        // Show loading Stage
        Scene scene = new Scene(gridPane);
        Stage settingsStage = new Stage();
        settingsStage.setScene(scene);
        settingsStage.setResizable(false);
        settingsStage.show();
        settingsStage.setTitle("Settings");

        saveBTN.setOnAction(event -> {
            if (countryCodeField.getText().length() == 2 && Character.isLetter(countryCodeField.getText().charAt(0)) && Character.isLetter(countryCodeField.getText().charAt(1))) {
                numTitles = numberOfTitlesField.getValue();
                countryCode = countryCodeField.getText();

                if (comboBox.getValue().toString().equals("None")) {
                    topic = "None";
                    topicName = "None";

                } else {
                    topicName = comboBox.getValue().toString();
                    topic = getVideoIDMap().get(comboBox.getValue().toString()).toString();
                }

                settingsStage.close();

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Incorrect Country Code");
                alert.setHeaderText("Incorrect Country Code");
                alert.setContentText("Please enter a valid country code.");
                alert.showAndWait();

            }
        });

        cancelBTN.setOnAction(event -> {
            settingsStage.close();
        });
    }


    public void processCommandStage(String cmd, String lookingIn, int numLooking) throws IOException {
        final Task<Void> task = new Task<Void>() {
            File directory = new File(lookingIn);
            int numFiles = 0;

            protected Void call() throws Exception {
                while(numFiles < numLooking) {
                    numFiles = Objects.requireNonNull(directory.list()).length;
                    updateProgress(numFiles, numLooking);
                }

                return null;
            }
        };

        ProgressBar progress = new ProgressBar();
        progress.progressProperty().bind(task.progressProperty());
        ProgressIndicator loadingIndicator = new ProgressIndicator(-1.0);
        
        Label loadingLabel = new Label("Loading...");
        Label youTubeLabel = new Label("Connecting to YouTube...");
        Label blankLabel = new Label("");
        blankLabel.setMinWidth(200);

        GridPane layout = new GridPane();
        layout.add(loadingIndicator, 0, 0);
        layout.add(loadingLabel, 0, 1);
        layout.add(youTubeLabel, 0, 2);
        layout.add(blankLabel, 0, 3);
        layout.add(progress, 0, 4);
        layout.setPadding(new Insets(10, 0, 0, 0));
        GridPane.setHalignment(loadingIndicator, HPos.CENTER);
        GridPane.setHalignment(loadingLabel, HPos.CENTER);
        GridPane.setHalignment(youTubeLabel, HPos.CENTER);
        GridPane.setHalignment(progress, HPos.CENTER);

        Stage progressState = new Stage();
        progressState.setScene(new Scene(layout));
        progressState.setResizable(false);
        progressState.show();

        progress.progressProperty().addListener(observable -> {
            if (progress.getProgress() >= 1) {
                try {
                    processAlgorithmStage("ClickbaitGenerator.exe " + numTitles, "output", 1, progressState);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        Process process = Runtime.getRuntime().exec(cmd);
    }

    public void processAlgorithmStage(String cmd, String lookingIn, int numLooking, Stage progressState) throws IOException {
        final Task<Void> task = new Task<Void>() {
            File directory = new File(lookingIn);
            int numFiles = 0;

            protected Void call() throws Exception {
                File file = new File("output/titles.txt");
                file.delete();

                while(numFiles < numLooking) {
                    numFiles = Objects.requireNonNull(directory.list()).length;
                    updateProgress(numFiles, numLooking + 1);
                    Thread.sleep(1000);
                }

                Process process = Runtime.getRuntime().exec("wscript restart.vbs");
                updateProgress(1, 1);
                return null;
            }
        };


        ProgressBar progress = new ProgressBar();
        progress.setStyle("-fx-accent: green");
        progress.progressProperty().bind(task.progressProperty());
        progress.setPadding(new Insets(0, 0, 10, 0));
        ProgressIndicator loadingIndicator = new ProgressIndicator(-1.0);


        Label loadingLabel = new Label("Loading...");
        Label dataLabel = new Label("Processing Data...");
        Label blankLabel = new Label("");
        blankLabel.setMinWidth(200);

        GridPane layout = new GridPane();
        layout.add(loadingIndicator, 0, 0);
        layout.add(loadingLabel, 0, 1);
        layout.add(dataLabel, 0, 2);
        layout.add(blankLabel, 0, 3);
        layout.add(progress, 0, 4);
        layout.setPadding(new Insets(10, 0, 10, 0));
        GridPane.setHalignment(loadingIndicator, HPos.CENTER);
        GridPane.setHalignment(loadingLabel, HPos.CENTER);
        GridPane.setHalignment(dataLabel, HPos.CENTER);
        GridPane.setHalignment(progress, HPos.CENTER);

        progressState.setScene(new Scene(layout));

        progress.progressProperty().addListener(observable -> {
            if (progress.getProgress() >= 1) {
                progressState.close();
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        Process process = Runtime.getRuntime().exec(cmd);
    }


    public HashMap<String, Integer> getVideoIDMap(){
        HashMap<String, Integer> videoIDs = new HashMap<>();

        videoIDs.put("Autos & Vehicles", 2);
        videoIDs.put("Film & Animation", 1 );
        videoIDs.put("Music", 10);
        videoIDs.put("Pets & Animals", 15);
        videoIDs.put("Sports", 17);
        videoIDs.put("Travel & Events", 19);
        videoIDs.put("Gaming", 20);
        videoIDs.put("People & Blogs", 22);
        videoIDs.put("Comedy", 23);
        videoIDs.put("Entertainment", 24);
        videoIDs.put("News & Politics", 25);
        videoIDs.put("Howto & Style", 26);
        videoIDs.put("Education", 27);
        videoIDs.put("Science & Technology", 28);
        videoIDs.put("Nonprofits & Activism", 29);

        return videoIDs;
    }


    public static void main(String args[]) {
        launch(args);
    }
}