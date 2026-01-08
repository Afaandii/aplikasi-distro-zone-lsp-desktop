package view;

import java.io.File;
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
import model.User;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;

// Custom container untuk menggantikan CardLayout
class CardPane extends StackPane {
    private final Map<String, javafx.scene.Node> cards = new HashMap<>();

    public void addCard(String name, javafx.scene.Node node) {
        cards.put(name, node);
        getChildren().add(node);
        node.setVisible(false);
    }

    public void showCard(String name) {
        getChildren().forEach(n -> n.setVisible(false));
        javafx.scene.Node node = cards.get(name);
        if (node != null) {
            node.setVisible(true);
            node.toFront();
        }
    }
}

public class DashboardAdmin extends Application {
    private User currentUser;
    private BorderPane mainContainer;
    private CardPane cardContainer; // Ganti contentArea dengan CardPane
    private HBox selectedMenuItem = null;

    public DashboardAdmin(User user) {
        this.currentUser = user;
    }

    @Override
    public void start(Stage primaryStage) {
        mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: #f0f2f5;");

        // Sidebar
        VBox sidebar = createSidebar(primaryStage);
        mainContainer.setLeft(sidebar);

        // Card Container (menggantikan contentArea)
        cardContainer = new CardPane();
        cardContainer.setPadding(new Insets(30));

        // Tambahkan semua panel sekali
        // 🔥 INI YANG ANDA MAU — MIRIP CONTENTPANEL DI SWING!
        cardContainer.addCard("dashboard", createDashboardNode());
        cardContainer.addCard("karyawan", new KaryawanManagementPanel(currentUser));
        cardContainer.addCard("merk", new MerkManagementPanel());
        cardContainer.addCard("tipe", new TipeManagementPanel());
        cardContainer.addCard("ukuran", new UkuranManagementPanel());
        cardContainer.addCard("warna", new WarnaManagementPanel());
        cardContainer.addCard("produk", new ProdukManagementPanel(currentUser));
        cardContainer.addCard("jam_operasional", new JamOperasionalManagementPanel());
        cardContainer.addCard("laporan", new LaporanAdminPanel());

        // Tampilkan dashboard pertama kali
        cardContainer.showCard("dashboard");
        mainContainer.setCenter(cardContainer);

        Scene scene = new Scene(mainContainer, 1400, 800);
        
        // Di dalam start(), setelah scene dibuat
        scene.getStylesheets().add(
            "data:text/css," +
            ".sidebar-scroll .scroll-bar { -fx-background-color: transparent; -fx-padding: 0; }" +
            ".sidebar-scroll .scroll-bar .thumb { " +
                "-fx-background-color: rgba(255,255,255,0.1); " +
                "-fx-background-radius: 5; " +
                "-fx-min-width: 6px; " +
                "-fx-min-height: 6px; " +
            "}" +
            ".sidebar-scroll .scroll-bar .thumb:hover { " +
                "-fx-background-color: rgba(255,255,255,0.2); " +
                "-fx-min-width: 8px; " +
                "-fx-min-height: 8px; " +
            "}"
        );

        primaryStage.setTitle("DistroZone - Admin Dashboard");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    // Helper: buat node dashboard
    private javafx.scene.Node createDashboardNode() {
        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER); // Pusatkan semua komponen
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #f8f9fa;"); // Warna latar belakang yang lembut

        // Header "Selamat Datang"
        Label welcomeText = new Label("Selamat Datang, " + currentUser.getNama() + "! 👋");
        welcomeText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        welcomeText.setTextFill(Color.web("#7f8c8d"));

        // Judul Dashboard
        Label header = new Label("Dashboard Admin");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        header.setTextFill(Color.web("#2c3e50"));

        // Teks Petunjuk Utama
        Label instruction = new Label("Pilih menu di sebelah kiri untuk mengelola produk");
        instruction.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 18));
        instruction.setTextFill(Color.web("#2c3e50"));
        instruction.setStyle("-fx-font-weight: bold; -fx-text-alignment: center;");

        // Tambahkan semua ke container
        content.getChildren().addAll(welcomeText, header, instruction);

        return content;
    }
    
    private ImageView createDefaultAvatar() {
        Image defaultImage = new Image(
            getClass().getResourceAsStream("/resource/default.jpg")
        );
        ImageView iv = new ImageView(defaultImage);
        iv.setFitWidth(56);
        iv.setFitHeight(56);
        iv.setPreserveRatio(true);
        return iv;
    }


    private VBox createSidebar(Stage primaryStage) {
        // Sidebar utama
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(280);
        sidebar.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #2c3e50 0%, #34495e 100%);"
        );
        sidebar.setPadding(new Insets(0));
        sidebar.setEffect(new javafx.scene.effect.DropShadow(15, Color.rgb(0, 0, 0, 0.3)));

        // Header
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 20, 20, 20));
        header.setStyle("-fx-background-color: rgba(52, 73, 94, 0.8);");

        HBox logoContainer = new HBox(12);
        logoContainer.setAlignment(Pos.CENTER);

        Image logoImage = new Image(
            getClass().getResourceAsStream("/resource/distro-zone.png")
        );

        ImageView logo = new ImageView(logoImage);
        logo.setFitHeight(36);        // atur tinggi logo
        logo.setPreserveRatio(true);
        logo.setSmooth(true);


        logoContainer.getChildren().add(logo);
        header.getChildren().add(logoContainer);

        // User Card
     // User Card — DIPERBAIKI: Avatar + Nama di sebelah kanan
    VBox userCard = new VBox(10);
    userCard.setAlignment(Pos.CENTER);
    userCard.setPadding(new Insets(15));
    userCard.setStyle(
        "-fx-background-color: rgba(52, 152, 219, 0.15); " +
        "-fx-background-radius: 12; " +
        "-fx-border-color: rgba(52, 152, 219, 0.3); " +
        "-fx-border-width: 1; " +
        "-fx-border-radius: 12;"
    );
    userCard.setMaxWidth(240);
    VBox.setMargin(userCard, new Insets(15, 20, 15, 20));

    // Gunakan HBox untuk avatar + teks sejajar horizontal
    HBox userInfo = new HBox(12);
    userInfo.setAlignment(Pos.CENTER_LEFT);
    userInfo.setPadding(new Insets(0)); // Padding sudah diatur di userCard

    StackPane avatar = new StackPane();

    double radius = 28;

    // Border lingkaran
    Circle avatarBorder = new Circle(radius);
    avatarBorder.setFill(Color.TRANSPARENT);
    avatarBorder.setStroke(Color.WHITE);
    avatarBorder.setStrokeWidth(2.5);

    // Load image
    Image image;
    try {
        String fotoUrl = currentUser.getFotoProfile();
        image = (fotoUrl != null && !fotoUrl.isEmpty())
                ? new Image(fotoUrl)
                : new Image(getClass().getResourceAsStream("/resource/default.jpg"));
    } catch (Exception e) {
        image = new Image(getClass().getResourceAsStream("/resource/default.jpg"));
    }

    // Circle dengan image pattern
    Circle avatarImage = new Circle(radius - 2);
    avatarImage.setFill(new ImagePattern(image));

    avatar.getChildren().addAll(avatarBorder, avatarImage);

    Label userName = new Label(currentUser.getNama());
    userName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
    userName.setTextFill(Color.WHITE);

    userInfo.getChildren().addAll(avatar, userName);

    // Masukkan HBox ke dalam VBox (untuk menjaga struktur userCard)
    userCard.getChildren().add(userInfo);

        // Menu Items — DIBUNGKUS SCROLLPANE
        ScrollPane menuScroll = new ScrollPane();
        menuScroll.setFitToWidth(true);
        menuScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        menuScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        menuScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // ⬅️ Wajib!
        menuScroll.setPrefHeight(350); // ⬅️ Atur tinggi agar scroll muncul
        menuScroll.setMinHeight(300);
        menuScroll.setMaxHeight(Double.MAX_VALUE);
        menuScroll.getStyleClass().add("sidebar-scroll");

        // Isi menu items — KELOMPOK BERDASARKAN JUDUL
        VBox menuContainer = new VBox(4);
        menuContainer.setPadding(new Insets(15, 15, 15, 15));

        // --- DASHBOARD (di luar Master) ---
        menuContainer.getChildren().add(createSectionHeader("Dashboard", "🏠"));
        menuContainer.getChildren().add(
            createMenuItem("Dashboard", "dashboard", true)
        );

        // --- MASTER ---
        menuContainer.getChildren().add(createSectionHeader("Master", "📚"));
        menuContainer.getChildren().addAll(
            createMenuItem("Merk", "merk", false),
            createMenuItem("Tipe", "tipe", false),
            createMenuItem("Ukuran", "ukuran", false),
            createMenuItem("Warna", "warna", false),
            createMenuItem("Produk", "produk", false),
            createMenuItem("Jam Operasional", "jam_operasional", false)
        );

        // --- SETTING ---
        menuContainer.getChildren().add(createSectionHeader("Setting", "⚙"));
        menuContainer.getChildren().add(
            createMenuItem("Karyawan", "karyawan", false)
        );

        // --- LAPORAN ---
        menuContainer.getChildren().add(createSectionHeader("Laporan", "📊"));
        menuContainer.getChildren().add(
            createMenuItem("Laporan", "laporan", false)
        );

        menuScroll.setContent(menuContainer);

        // Tombol Logout
        Button btnLogout = new Button("🚪  Keluar");
        btnLogout.setStyle(
            "-fx-background-color: #e74c3c; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 13 0; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand;"
        );
        btnLogout.setMaxWidth(Double.MAX_VALUE);

        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle(
            "-fx-background-color: #c0392b; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 13 0; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-scale-x: 1.02; -fx-scale-y: 1.02;"
        ));

        btnLogout.setOnMouseExited(e -> btnLogout.setStyle(
            "-fx-background-color: #e74c3c; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 13 0; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand;"
        ));

        btnLogout.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Konfirmasi Logout");
            alert.setHeaderText(null);
            alert.setContentText("Apakah Anda yakin ingin keluar?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                // Tutup stage dashboard
                primaryStage.close();

                // Buka kembali halaman login
                try {
                    // Buat instance LoginPage
                    LoginPage loginPage = new LoginPage();

                    // Buat stage baru untuk login
                    Stage loginStage = new Stage();
                    loginPage.start(loginStage); // Panggil start() dari LoginPage

                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println("Gagal membuka halaman login.");
                }
            }
        });

        VBox logoutContainer = new VBox(btnLogout);
        logoutContainer.setPadding(new Insets(15, 20, 20, 20));

        // Gabungkan semua ke sidebar
        sidebar.getChildren().addAll(header, userCard, menuScroll, logoutContainer);

        return sidebar;
    }

// MODIFIKASI: createSectionHeader dengan ikon, tanpa background
private HBox createSectionHeader(String title, String icon) {
    HBox header = new HBox(8);
    header.setAlignment(Pos.CENTER_LEFT);
    header.setPadding(new Insets(8, 15, 8, 15)); // Padding lebih kecil

    Label iconLabel = new Label(icon);
    iconLabel.setFont(Font.font(16));
    iconLabel.setTextFill(Color.web("#bdc3c7"));

    Label titleLabel = new Label(title);
    titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
    titleLabel.setTextFill(Color.web("#bdc3c7"));

    header.getChildren().addAll(iconLabel, titleLabel);

    // Tidak ada background — hanya teks dan ikon
    return header;
}

// MODIFIKASI: createMenuItem TANPA ICON (tetap sama seperti sebelumnya)
private HBox createMenuItem(String text, String id, boolean isActive) {
    HBox menuItem = new HBox(12);
    menuItem.setAlignment(Pos.CENTER_LEFT);
    menuItem.setPadding(new Insets(12, 15, 12, 15));
    menuItem.setStyle(
        isActive ?
        "-fx-background-color: rgba(52, 152, 219, 0.2); " +
        "-fx-background-radius: 10; " +
        "-fx-cursor: hand; " +
        "-fx-border-color: #3498db; " +
        "-fx-border-width: 0 0 0 3; " +
        "-fx-border-radius: 10;" :
        "-fx-background-color: transparent; " +
        "-fx-background-radius: 10; " +
        "-fx-cursor: hand;"
    );

    if (isActive) selectedMenuItem = menuItem;

    Label textLabel = new Label(text);
    textLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
    textLabel.setTextFill(isActive ? Color.WHITE : Color.web("#bdc3c7"));

    menuItem.getChildren().add(textLabel);

    menuItem.setOnMouseEntered(e -> {
        if (menuItem != selectedMenuItem) {
            menuItem.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.08); " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand;"
            );
            textLabel.setTextFill(Color.WHITE);
        }
    });

    menuItem.setOnMouseExited(e -> {
        if (menuItem != selectedMenuItem) {
            menuItem.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand;"
            );
            textLabel.setTextFill(Color.web("#bdc3c7"));
        }
    });

    menuItem.setOnMouseClicked(e -> {
        if (selectedMenuItem != null) {
            selectedMenuItem.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand;"
            );
            Label prevText = (Label) selectedMenuItem.getChildren().get(0);
            prevText.setTextFill(Color.web("#bdc3c7"));
        }

        selectedMenuItem = menuItem;
        menuItem.setStyle(
            "-fx-background-color: rgba(52, 152, 219, 0.2); " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: #3498db; " +
            "-fx-border-width: 0 0 0 3; " +
            "-fx-border-radius: 10;"
        );
        textLabel.setTextFill(Color.WHITE);

        // Tampilkan kartu sesuai id
        cardContainer.showCard(id);
    });

    return menuItem;
}

    private VBox createEnhancedStatCard(String title, String value, String icon, String colorHex) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3);"
        );
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefHeight(185);

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(65, 65);
        iconContainer.setMaxSize(65, 65);
        iconContainer.setStyle(
            "-fx-background-color: " + colorHex + "20; " +
            "-fx-background-radius: 13; " +
            "-fx-border-color: " + colorHex + "30; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 13;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(34));
        iconContainer.getChildren().add(iconLabel);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox textContent = new VBox(6);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
        titleLabel.setTextFill(Color.web("#95a5a6"));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 29));
        valueLabel.setTextFill(Color.web("#2c3e50"));

        Label trendLabel = new Label("↗ +12% dari kemarin");
        trendLabel.setFont(Font.font("Segoe UI", 11));
        trendLabel.setTextFill(Color.web(colorHex));
        trendLabel.setStyle(
            "-fx-background-color: " + colorHex + "15; " +
            "-fx-padding: 3 8; " +
            "-fx-background-radius: 6;"
        );

        textContent.getChildren().addAll(titleLabel, valueLabel, trendLabel);
        card.getChildren().addAll(iconContainer, spacer, textContent);

        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0, 0, 5); " +
                "-fx-cursor: hand;"
            );
            card.setTranslateY(-3);
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3);"
            );
            card.setTranslateY(0);
        });

        return card;
    }
}