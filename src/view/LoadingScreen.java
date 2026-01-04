package view;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LoadingScreen extends Application {
    private Stage loadingStage;
    private Label loadingText;
    private ProgressBar progressBar;
    
    @Override
    public void start(Stage primaryStage) {
        this.loadingStage = primaryStage;
        
        // Main Container
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #667eea;");
        
        // Add gradient overlay
        StackPane gradientOverlay = new StackPane();
        gradientOverlay.setStyle("-fx-background-color: #764ba2;");
        gradientOverlay.setOpacity(0.5);
        
        // Content VBox
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        
        // Animated Logo
        StackPane logoContainer = createAnimatedLogo();
        
        // App Name
        Label appName = new Label("DistroZone");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 42));
        appName.setTextFill(Color.WHITE);
        appName.setEffect(new javafx.scene.effect.DropShadow(8, Color.rgb(0, 0, 0, 0.3)));
        
        // Subtitle
        Label subtitle = new Label("Management System");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 16));
        subtitle.setTextFill(Color.rgb(255, 255, 255, 0.9));
        
        // Loading Text
        loadingText = new Label("Memuat aplikasi...");
        loadingText.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        loadingText.setTextFill(Color.rgb(255, 255, 255, 0.85));
        
        // Progress Bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(6);
        progressBar.setStyle(
            "-fx-accent: white; " +
            "-fx-background-radius: 10; " +
            "-fx-background-color: rgba(255, 255, 255, 0.2);"
        );
        
        // Version Label
        Label version = new Label("Version 1.0.0");
        version.setFont(Font.font("Segoe UI", 11));
        version.setTextFill(Color.rgb(255, 255, 255, 0.6));
        
        VBox versionBox = new VBox(version);
        versionBox.setAlignment(Pos.CENTER);
        versionBox.setPadding(new javafx.geometry.Insets(20, 0, 0, 0));
        
        content.getChildren().addAll(
            logoContainer,
            appName,
            subtitle,
            loadingText,
            progressBar,
            versionBox
        );
        
        root.getChildren().addAll(gradientOverlay, content);
        
        Scene scene = new Scene(root, 600, 500);
        scene.setFill(Color.TRANSPARENT);
        
        loadingStage.initStyle(StageStyle.TRANSPARENT);
        loadingStage.setScene(scene);
        loadingStage.setResizable(false);
        loadingStage.centerOnScreen();
        loadingStage.show();
        
        // Start loading simulation
        startLoading();
    }
    
    private StackPane createAnimatedLogo() {
        StackPane logoStack = new StackPane();
        
        // Outer Circle (rotating)
        Circle outerCircle = new Circle(70);
        outerCircle.setFill(Color.TRANSPARENT);
        outerCircle.setStroke(Color.rgb(255, 255, 255, 0.3));
        outerCircle.setStrokeWidth(2);
        outerCircle.getStrokeDashArray().addAll(10d, 5d);
        
        // Middle Circle
        Circle middleCircle = new Circle(60);
        middleCircle.setFill(Color.TRANSPARENT);
        middleCircle.setStroke(Color.rgb(255, 255, 255, 0.2));
        middleCircle.setStrokeWidth(1.5);
        
        // Inner Circle (pulsing)
        Circle innerCircle = new Circle(55);
        innerCircle.setFill(Color.rgb(255, 255, 255, 0.15));
        innerCircle.setStroke(Color.WHITE);
        innerCircle.setStrokeWidth(2.5);
        
        // Logo Icon
        Label logoIcon = new Label("🛍️");
        logoIcon.setFont(Font.font(50));
        
        logoStack.getChildren().addAll(outerCircle, middleCircle, innerCircle, logoIcon);
        
        // Rotation Animation for outer circle
        RotateTransition rotate = new RotateTransition(Duration.seconds(3), outerCircle);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();
        
        // Reverse rotation for middle circle
        RotateTransition rotateMiddle = new RotateTransition(Duration.seconds(4), middleCircle);
        rotateMiddle.setByAngle(-360);
        rotateMiddle.setCycleCount(Animation.INDEFINITE);
        rotateMiddle.setInterpolator(Interpolator.LINEAR);
        rotateMiddle.play();
        
        // Pulse Animation for inner circle
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), innerCircle);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
        
        // Float Animation for icon
        TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(2), logoIcon);
        floatAnim.setFromY(0);
        floatAnim.setToY(-8);
        floatAnim.setCycleCount(Animation.INDEFINITE);
        floatAnim.setAutoReverse(true);
        floatAnim.play();
        
        // Fade animation for icon
        FadeTransition fade = new FadeTransition(Duration.seconds(1.5), logoIcon);
        fade.setFromValue(1.0);
        fade.setToValue(0.7);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
        
        return logoStack;
    }
    
    private void startLoading() {
        String[] loadingSteps = {
            "Memuat aplikasi...",
            "Menginisialisasi database...",
            "Memuat komponen UI...",
            "Menyiapkan koneksi...",
            "Hampir selesai..."
        };
        
        Timeline timeline = new Timeline();
        
        for (int i = 0; i < loadingSteps.length; i++) {
            final int index = i;
            final double progress = (i + 1) / (double) loadingSteps.length;
            
            KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(i * 0.8),
                e -> {
                    loadingText.setText(loadingSteps[index]);
                    
                    // Smooth progress animation
                    Timeline progressAnim = new Timeline(
                        new KeyFrame(Duration.ZERO, 
                            new KeyValue(progressBar.progressProperty(), progressBar.getProgress())),
                        new KeyFrame(Duration.seconds(0.6), 
                            new KeyValue(progressBar.progressProperty(), progress))
                    );
                    progressAnim.play();
                }
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        // After loading complete
        KeyFrame finalFrame = new KeyFrame(
            Duration.seconds(loadingSteps.length * 0.8 + 0.5),
            e -> transitionToLogin()
        );
        timeline.getKeyFrames().add(finalFrame);
        
        timeline.play();
    }
    
    private void transitionToLogin() {
        // Fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), loadingStage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            loadingStage.close();
            
            // Open Login Page
            try {
                Stage loginStage = new Stage();
                new LoginPage().start(loginStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        fadeOut.play();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}