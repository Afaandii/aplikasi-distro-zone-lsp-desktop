package view;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.User;
import dao.UserDAO;

public class LoginPage extends Application {
    private TextField txtUsername;
    private PasswordField txtPassword;
    private Label lblError;
    private UserDAO userDAO = new UserDAO();
    private Button btnLogin;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DistroZone - Login");

        // Main Container dengan background gradient
        StackPane root = new StackPane();
        root.setStyle(
            "-fx-background-color: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);"
        );

        // Login Card Container
        VBox loginCard = createLoginCard(primaryStage);

        // Add to root
        root.getChildren().add(loginCard);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        // ⬇️ TUNJUKKAN JENDELA SETELAH SEMUA KOMPONEN SIAP
        primaryStage.show();

        // ⬇️ SESUAIKAN UKURAN JENDELA DENGAN KONTEN
        primaryStage.sizeToScene();

        // Entrance animation
        playEntranceAnimation(loginCard);
    }

    private VBox createLoginCard(Stage primaryStage) {
        VBox card = new VBox(25);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(50, 60, 50, 60));
        card.setMaxWidth(450);
        card.setMaxHeight(600);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 10);"
        );

        // Logo Section
        VBox logoSection = createLogoSection();

        // Title Section
        VBox titleSection = createTitleSection();

        // Username Field
        HBox usernameField = createInputField("👤", "Username");
        txtUsername = (TextField) ((VBox) usernameField.getChildren().get(1)).getChildren().get(0);

        // Password Field
        HBox passwordField = createInputField("🔒", "Password");
        txtPassword = (PasswordField) ((VBox) passwordField.getChildren().get(1)).getChildren().get(0);
        txtPassword.setOnAction(e -> btnLogin.fire());

        // Error Label
        lblError = new Label();
        lblError.setFont(Font.font("Segoe UI", 12));
        lblError.setTextFill(Color.web("#e74c3c"));
        lblError.setVisible(false);
        lblError.setWrapText(true);
        lblError.setMaxWidth(330);
        lblError.setAlignment(Pos.CENTER);

        // Login Button
        btnLogin = createLoginButton(primaryStage);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(
            logoSection,
            titleSection,
            usernameField,
            passwordField,
            lblError,
            btnLogin,
            spacer
        );

        return card;
    }

    private VBox createLogoSection() {
        VBox logoBox = new VBox(10);
        logoBox.setAlignment(Pos.CENTER);

        // Logo Circle
        StackPane logoCircle = new StackPane();
        Circle circle = new Circle(50);
        circle.setFill(Color.web("#667eea"));
        circle.setStroke(Color.web("#764ba2"));
        circle.setStrokeWidth(3);
        circle.setEffect(new javafx.scene.effect.DropShadow(15, Color.rgb(102, 126, 234, 0.4)));

        Label icon = new Label("🛍️");
        icon.setFont(Font.font(40));

        logoCircle.getChildren().addAll(circle, icon);

        logoBox.getChildren().add(logoCircle);

        return logoBox;
    }

    private VBox createTitleSection() {
        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER);

        Label title = new Label("DistroZone");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#2c3e50"));

        Label subtitle = new Label("Masuk ke akun Anda");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#7f8c8d"));

        titleBox.getChildren().addAll(title, subtitle);

        return titleBox;
    }

    private HBox createInputField(String icon, String placeholder) {
        HBox fieldContainer = new HBox(12);
        fieldContainer.setAlignment(Pos.CENTER_LEFT);
        fieldContainer.setPadding(new Insets(10, 15, 10, 15));
        fieldContainer.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: #e1e8ed; " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 12;"
        );

        // Icon
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));
        iconLabel.setTextFill(Color.web("#95a5a6"));
        iconLabel.setMinWidth(40);
        iconLabel.setAlignment(Pos.CENTER);

        // Input
        VBox inputBox = new VBox();
        if (placeholder.equals("Username")) {
            TextField textField = new TextField();
            textField.setPromptText(placeholder);
            textField.setFont(Font.font("Segoe UI", 14));
            textField.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-border-color: transparent; " +
                "-fx-text-fill: #2c3e50; " +
                "-fx-prompt-text-fill: #95a5a6;"
            );
            textField.setPrefWidth(280);
            HBox.setHgrow(textField, Priority.ALWAYS);
            inputBox.getChildren().add(textField);
            txtUsername = textField;
        } else {
            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText(placeholder);
            passwordField.setFont(Font.font("Segoe UI", 14));
            passwordField.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-border-color: transparent; " +
                "-fx-text-fill: #2c3e50; " +
                "-fx-prompt-text-fill: #95a5a6;"
            );
            passwordField.setPrefWidth(280);
            HBox.setHgrow(passwordField, Priority.ALWAYS);
            inputBox.getChildren().add(passwordField);
            txtPassword = passwordField;
        }

        fieldContainer.getChildren().addAll(iconLabel, inputBox);

        // Focus effect
        if (placeholder.equals("Username")) {
            txtUsername.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    fieldContainer.setStyle(
                        "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #667eea; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.2), 8, 0, 0, 2);"
                    );
                    iconLabel.setTextFill(Color.web("#667eea"));
                } else {
                    fieldContainer.setStyle(
                        "-fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1.5; " +
                        "-fx-border-radius: 12;"
                    );
                    iconLabel.setTextFill(Color.web("#95a5a6"));
                }
            });
        } else {
            txtPassword.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    fieldContainer.setStyle(
                        "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #667eea; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.2), 8, 0, 0, 2);"
                    );
                    iconLabel.setTextFill(Color.web("#667eea"));
                } else {
                    fieldContainer.setStyle(
                        "-fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1.5; " +
                        "-fx-border-radius: 12;"
                    );
                    iconLabel.setTextFill(Color.web("#95a5a6"));
                }
            });
        }

        return fieldContainer;
    }

    private Button createLoginButton(Stage primaryStage) {
        Button button = new Button("MASUK");
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        button.setTextFill(Color.WHITE);
        button.setPrefWidth(330);
        button.setPrefHeight(50);
        button.setStyle(
            "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.4), 10, 0, 0, 4);"
        );

        // Hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: linear-gradient(to right, #5568d3 0%, #6a3f8f 100%); " +
                "-fx-background-radius: 12; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.6), 15, 0, 0, 6);"
            );
        });

        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%); " +
                "-fx-background-radius: 12; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.4), 10, 0, 0, 4);"
            );
        });

        button.setOnAction(e -> handleLogin(primaryStage));

        return button;
    }

    private void handleLogin(Stage loginStage) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // Reset error
        lblError.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            showError("⚠️ Username dan password tidak boleh kosong!");
            shakeAnimation(btnLogin);
            return;
        }

        // Disable button saat loading
        btnLogin.setDisable(true);
        btnLogin.setText("MEMPROSES...");

        // Simulasi loading
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulasi delay

                User user = userDAO.login(username, password);

                javafx.application.Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    btnLogin.setText("MASUK");

                    if (user != null) {
                        // Success animation
                        FadeTransition fade = new FadeTransition(Duration.millis(300), loginStage.getScene().getRoot());
                        fade.setFromValue(1.0);
                        fade.setToValue(0.0);
                        fade.setOnFinished(e -> {
                            loginStage.close();
                            try {
                                Stage dashboardStage = new Stage();
                                new DashboardAdmin(user).start(dashboardStage);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                new Alert(Alert.AlertType.ERROR, "Gagal membuka dashboard: " + ex.getMessage()).show();
                            }
                        });
                        fade.play();
                    } else {
                        showError("❌ Username atau password salah!");
                        txtPassword.clear();
                        shakeAnimation(btnLogin);
                    }
                });
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);

        // Error shake animation
        shakeAnimation(lblError);
    }

    private void shakeAnimation(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void playEntranceAnimation(VBox loginCard) {
        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), loginCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Slide up
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), loginCard);
        slideUp.setFromY(50);
        slideUp.setToY(0);

        fadeIn.play();
        slideUp.play();
    }
}