package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.User;

public class DashboardKasirApp extends Application {
    private User user;

    // Constructor untuk menerima user
    public DashboardKasirApp(User user) {
        this.user = user;
    }

    // Default constructor wajib ada untuk JavaFX (jika dipanggil via `launch()`)
    public DashboardKasirApp() {}

    @Override
    public void start(Stage primaryStage) {
        if (user == null) {
            throw new IllegalStateException("User tidak boleh null saat memulai DashboardKasirApp");
        }

        DashboardKasir dashboard = new DashboardKasir(primaryStage, user);
        primaryStage.setScene(dashboard.getScene());
        primaryStage.setTitle("DistroZone - Dashboard Kasir");
        primaryStage.show();
    }
}