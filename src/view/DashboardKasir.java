package view;

import dao.DetailTransaksiDAO;
import dao.ProdukDAO;
import dao.TransaksiDAO;
import dao.VarianDAO;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.beans.binding.BooleanBinding;
import javafx.stage.Modality;
import model.DetailTransaksi;
import model.Transaksi;
import model.User;
import model.Varian; // Kita gunakan Varian sebagai data utama

/**
 * Dashboard Kasir - Point of Sale DistroZone
 * Aplikasi desktop kasir untuk transaksi penjualan toko distro
 * Data diambil langsung dari tabel VARIAN via DAO
 */
public class DashboardKasir {
    // Stage dan Scene
    private Stage primaryStage;
    private Scene scene;
    // Data kasir yang login
    private String namaKasir = "Budi Handoko";
    private Long idKasir = 1L;
    // Collections
    private ObservableList<Varian> varianList = FXCollections.observableArrayList();
    private FilteredList<Varian> filteredVarian;
    private ObservableList<ItemKeranjang> keranjangList = FXCollections.observableArrayList();
    // Properties untuk binding
    private IntegerProperty totalItemProperty = new SimpleIntegerProperty(0);
    private DoubleProperty totalBayarProperty = new SimpleDoubleProperty(0.0);
    private DoubleProperty uangBayarProperty = new SimpleDoubleProperty(0.0);
    private DoubleProperty kembalianProperty = new SimpleDoubleProperty(0.0);
    // Components - Header
    private Label lblJamRealtime;
    private Label lblStatusToko;
    private Label lblNamaKasir;
    // Components - Work Area
    private TextField txtSearchProduk;
    private TableView<Varian> tableVarian; // Tabel sekarang pakai Varian
    private TableView<ItemKeranjang> tableKeranjang;
    // Components - Pembayaran
    private ToggleGroup metodePembayaranGroup;
    private RadioButton rbTunai, rbQRIS, rbTransfer;
    private VBox panelTunai, panelNonTunai;
    private TextField txtUangBayar;
    private Label lblKembalian;
    private Label lblTotalBayar;
    private Button btnSimpanTransaksi;
    private Button btnBatal;
    // Components - Status Bar
    private Label lblStatusIcon;
    private Label lblStatusMessage;
    private Label lblStatusTime;
    // Format
    private NumberFormat currencyFormat;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
    // Jam Operasional
    private LocalTime jamBuka = LocalTime.of(10, 0); // Sesuai dokumen LSP
    private LocalTime jamTutup = LocalTime.of(20, 0);
    // State transaksi
    private boolean transaksiSaved = false;
    private String noTransaksiTerakhir = "";
    // Executor untuk background loading
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Timeline clock;
    private BooleanProperty transaksiAktifProperty = new SimpleBooleanProperty(false);
    private ItemKeranjang lastRemovedItem = null;
    private Timer undoTimer = null;
    private User currentUser;

    public DashboardKasir(Stage stage, User user) {
        this.primaryStage = stage;
        this.currentUser = user;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        if (user != null) {
            this.namaKasir = user.getNama();
            this.idKasir = user.getIdUser();
        } else {
            this.namaKasir = "Budi Handoko";
            this.idKasir = 1L;
        }
        // Load data dari database
        loadDataFromDatabase();
        // Build UI
        BorderPane root = buildLayout();
        scene = new Scene(root, 1366, 768);
        applyStyles(root);
        primaryStage.setTitle("DistroZone - Kasir POS");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        startRealtimeClock();
        Platform.runLater(() -> txtSearchProduk.requestFocus());
        updateStatusBar("ℹ️", "Siap melayani transaksi", false);
    }

    /**
     * Load data varian dari database
     */
    private void loadDataFromDatabase() {
        executor.submit(() -> {
            VarianDAO varianDAO = new VarianDAO();
            List<Varian> listVarian = varianDAO.getAllVarian(); // Ambil semua varian
            Platform.runLater(() -> {
                varianList.clear();
                varianList.addAll(listVarian);
                if (filteredVarian != null) {
                    filteredVarian.setPredicate(p -> true);
                }
            });
        });
    }

    /**
     * Build main layout structure
     */
    private BorderPane buildLayout() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        // TOP - Header
        root.setTop(buildHeader());
        // CENTER - Work Area (Produk + Keranjang)
        root.setCenter(buildWorkArea());
        // RIGHT - Panel Pembayaran
        root.setRight(buildPanelPembayaran());
        // BOTTOM - Status Bar
        root.setBottom(buildStatusBar());
        return root;
    }

    /**
     * BUILD HEADER
     */
    private HBox buildHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #2c3e50; -fx-border-width: 0 0 2 0; -fx-border-color: #3498db;");
        // Logo/Brand
        Label lblLogo = new Label("DistroZone");
        lblLogo.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblLogo.setTextFill(Color.WHITE);
        // Separator
        Separator sep1 = new Separator();
        sep1.setOrientation(javafx.geometry.Orientation.VERTICAL);
        // Nama Kasir
        lblNamaKasir = new Label("Kasir: " + namaKasir);
        lblNamaKasir.setFont(Font.font("System", FontWeight.NORMAL, 13));
        lblNamaKasir.setTextFill(Color.web("#ecf0f1"));
        // Tanggal & Jam
        lblJamRealtime = new Label();
        lblJamRealtime.setFont(Font.font("System", FontWeight.NORMAL, 13));
        lblJamRealtime.setTextFill(Color.web("#ecf0f1"));
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        // Status Toko
        lblStatusToko = new Label("BUKA");
        lblStatusToko.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblStatusToko.setPadding(new Insets(5, 10, 5, 10));
        lblStatusToko.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 3;");
        // Button Laporan
        Button btnLaporan = new Button("📊 Laporan Saya");
        btnLaporan.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        btnLaporan.setOnAction(e -> bukaHalamanLaporan());
        // Button Logout
        Button btnLogout = new Button("🚪 Logout");
        btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
        btnLogout.setOnAction(e -> logout());
        header.getChildren().addAll(
                lblLogo, sep1, lblNamaKasir, lblJamRealtime,
                spacer, lblStatusToko, btnLaporan, btnLogout
        );
        return header;
    }

    /**
     * BUILD WORK AREA (Varian + Keranjang)
     */
    private HBox buildWorkArea() {
        HBox workArea = new HBox(10);
        workArea.setPadding(new Insets(10));
        // Panel Varian (LEFT)
        VBox panelVarian = buildPanelVarian();
        HBox.setHgrow(panelVarian, Priority.ALWAYS);
        // Panel Keranjang (RIGHT)
        VBox panelKeranjang = buildPanelKeranjang();
        HBox.setHgrow(panelKeranjang, Priority.ALWAYS);
        workArea.getChildren().addAll(panelVarian, panelKeranjang);
        return workArea;
    }

    /**
     * BUILD PANEL VARIAN
     */
    private VBox buildPanelVarian() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        // Header
        Label lblHeader = new Label("🔍 CARI PRODUK");
        lblHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        // Search box
        HBox searchBox = new HBox(5);
        txtSearchProduk = new TextField();
        txtSearchProduk.setPromptText("Ketik nama produk, warna, atau ukuran...");
        txtSearchProduk.setPrefHeight(35);
        HBox.setHgrow(txtSearchProduk, Priority.ALWAYS);
        Button btnRefresh = new Button("⟳");
        btnRefresh.setPrefHeight(35);
        btnRefresh.setOnAction(e -> refreshVarian());
        searchBox.getChildren().addAll(txtSearchProduk, btnRefresh);
        // Table Varian
        tableVarian = new TableView<>();
        tableVarian.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableVarian, Priority.ALWAYS);

        // Kolom tanpa Kode
        TableColumn<Varian, String> colNama = new TableColumn<>("Nama Produk");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaProduk")); // Dari relasi di model Varian
        colNama.setPrefWidth(200);

        TableColumn<Varian, String> colWarna = new TableColumn<>("Warna");
        colWarna.setCellValueFactory(new PropertyValueFactory<>("namaWarna")); // Dari model Varian
        colWarna.setPrefWidth(80);

        TableColumn<Varian, String> colUkuran = new TableColumn<>("Ukuran");
        colUkuran.setCellValueFactory(new PropertyValueFactory<>("namaUkuran")); // Dari model Varian
        colUkuran.setPrefWidth(60);

        TableColumn<Varian, Long> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(new PropertyValueFactory<>("stokKaos"));
        colStok.setPrefWidth(60);
        colStok.setStyle("-fx-alignment: CENTER;");

        TableColumn<Varian, Long> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaJual")); // Perlu ditambahkan di model Varian
        colHarga.setPrefWidth(100);
        colHarga.setStyle("-fx-alignment: CENTER-RIGHT;");
        colHarga.setCellFactory(col -> new TableCell<Varian, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });

        tableVarian.getColumns().addAll(colNama, colWarna, colUkuran, colStok, colHarga);

        // Setup filtered list
        filteredVarian = new FilteredList<>(varianList, p -> true);
        tableVarian.setItems(filteredVarian);

        // Live search
        txtSearchProduk.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                filteredVarian.setPredicate(p -> true);
            } else {
                String lowerCase = newVal.toLowerCase();
                filteredVarian.setPredicate(varian -> {
                    return varian.getNamaProduk().toLowerCase().contains(lowerCase) ||
                           varian.getNamaWarna().toLowerCase().contains(lowerCase) ||
                           varian.getNamaUkuran().toLowerCase().contains(lowerCase);
                });
            }
        });

        // Double click to add
        tableVarian.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Varian selected = tableVarian.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    tambahKeKeranjang(selected);
                }
            }
        });

        // Enter key to add
        tableVarian.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                Varian selected = tableVarian.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    tambahKeKeranjang(selected);
                }
            }
        });

        panel.getChildren().addAll(lblHeader, searchBox, tableVarian);
        return panel;
    }

    /**
     * BUILD PANEL KERANJANG
     */
    private VBox buildPanelKeranjang() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        // Header
        Label lblHeader = new Label("🛒 KERANJANG BELANJA");
        lblHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        // Table Keranjang
        tableKeranjang = new TableView<>();
        tableKeranjang.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableKeranjang.setItems(keranjangList);
        VBox.setVgrow(tableKeranjang, Priority.ALWAYS);

        TableColumn<ItemKeranjang, String> colProduk = new TableColumn<>("Produk");
        colProduk.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colProduk.setPrefWidth(150);

        TableColumn<ItemKeranjang, String> colWarna = new TableColumn<>("Warna");
        colWarna.setCellValueFactory(new PropertyValueFactory<>("warna"));
        colWarna.setPrefWidth(70);

        TableColumn<ItemKeranjang, String> colSize = new TableColumn<>("Size");
        colSize.setCellValueFactory(new PropertyValueFactory<>("ukuran"));
        colSize.setPrefWidth(50);

        TableColumn<ItemKeranjang, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQty.setPrefWidth(110);
        colQty.setStyle("-fx-alignment: CENTER;");
        colQty.setCellFactory(col -> new TableCell<ItemKeranjang, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ItemKeranjang keranjang = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(5);
                    box.setAlignment(Pos.CENTER);
                    Button btnMin = new Button("−");
                    btnMin.setPrefSize(32, 32);
                    btnMin.setStyle("-fx-font-size: 18px; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
                    btnMin.setOnAction(e -> kurangiQty(keranjang));
                    Label lblQty = new Label(String.valueOf(keranjang.getQuantity()));
                    lblQty.setPrefWidth(30);
                    lblQty.setAlignment(Pos.CENTER);
                    lblQty.setStyle("-fx-font-weight: bold;");
                    Button btnPlus = new Button("+");
                    btnPlus.setPrefSize(32, 32);
                    btnPlus.setStyle("-fx-font-size: 18px; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
                    btnPlus.setOnAction(e -> tambahQty(keranjang));
                    box.getChildren().addAll(btnMin, lblQty, btnPlus);
                    setGraphic(box);
                }
            }
        });

        TableColumn<ItemKeranjang, Double> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colHarga.setPrefWidth(80);
        colHarga.setStyle("-fx-alignment: CENTER-RIGHT;");
        colHarga.setCellFactory(col -> new TableCell<ItemKeranjang, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });

        TableColumn<ItemKeranjang, Double> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setPrefWidth(100);
        colSubtotal.setStyle("-fx-alignment: CENTER-RIGHT;");
        colSubtotal.setCellFactory(col -> new TableCell<ItemKeranjang, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });

        TableColumn<ItemKeranjang, Void> colHapus = new TableColumn<>("Aksi");
        colHapus.setPrefWidth(50);
        colHapus.setCellFactory(col -> new TableCell<ItemKeranjang, Void>() {
            private final Button btnHapus = new Button("✕");
            {
                btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                btnHapus.setOnAction(e -> {
                    ItemKeranjang item = getTableView().getItems().get(getIndex());
                    hapusDariKeranjang(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnHapus);
            }
        });

        tableKeranjang.getColumns().addAll(colProduk, colWarna, colSize, colQty, colHarga, colSubtotal, colHapus);

        // Placeholder
        Label placeholder = new Label("Belum ada produk.\nPilih produk dari daftar.");
        placeholder.setStyle("-fx-text-alignment: center; -fx-text-fill: #95a5a6;");
        tableKeranjang.setPlaceholder(placeholder);

        // Ringkasan
        VBox ringkasan = new VBox(5);
        ringkasan.setPadding(new Insets(10));
        ringkasan.setStyle("-fx-background-color: white; -fx-background-radius: 3;");
        HBox boxTotalItem = new HBox();
        boxTotalItem.setAlignment(Pos.CENTER_LEFT);
        Label lblTotalItemLabel = new Label("Total Item: ");
        Label lblTotalItem = new Label();
        lblTotalItem.setStyle("-fx-font-weight: bold;");
        lblTotalItem.textProperty().bind(totalItemProperty.asString());
        boxTotalItem.getChildren().addAll(lblTotalItemLabel, lblTotalItem);

        HBox boxTotalBayarRingkasan = new HBox();
        boxTotalBayarRingkasan.setAlignment(Pos.CENTER_LEFT);
        Label lblTotalBayarLabel = new Label("TOTAL BAYAR: ");
        lblTotalBayarLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        Label lblTotalBayarValue = new Label();
        lblTotalBayarValue.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTotalBayarValue.setTextFill(Color.web("#e74c3c"));
        lblTotalBayarValue.textProperty().bind(Bindings.createStringBinding(() ->
                currencyFormat.format(totalBayarProperty.get()), totalBayarProperty
        ));
        
        
        boxTotalBayarRingkasan.getChildren().addAll(lblTotalBayarLabel, lblTotalBayarValue);
        Button btnBatalTransaksi = new Button("🗑 Batalkan Transaksi");
        btnBatalTransaksi.setPrefHeight(30);
        btnBatalTransaksi.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnBatalTransaksi.setOnAction(e -> batalkanTransaksi());

        ringkasan.getChildren().addAll(boxTotalItem, new Separator(), boxTotalBayarRingkasan, btnBatalTransaksi);
        panel.getChildren().addAll(lblHeader, tableKeranjang, ringkasan);
        return panel;
    }

    /**
     * BUILD PANEL PEMBAYARAN
     */
    private VBox buildPanelPembayaran() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(280);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #d5e8f7; -fx-border-width: 0 0 0 2; -fx-border-color: #3498db;");
        // Header
        Label lblHeader = new Label("💳 PEMBAYARAN");
        lblHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        Separator sep1 = new Separator();
        // Metode Pembayaran
        Label lblMetode = new Label("Metode Pembayaran:");
        lblMetode.setFont(Font.font("System", FontWeight.BOLD, 12));
        metodePembayaranGroup = new ToggleGroup();
        rbTunai = new RadioButton("💵 Tunai");
        rbTunai.setToggleGroup(metodePembayaranGroup);
        rbTunai.setSelected(true);
        rbQRIS = new RadioButton("📱 QRIS");
        rbQRIS.setToggleGroup(metodePembayaranGroup);
        rbTransfer = new RadioButton("🏦 Transfer Bank");
        rbTransfer.setToggleGroup(metodePembayaranGroup);
        VBox boxMetode = new VBox(5);
        boxMetode.getChildren().addAll(rbTunai, rbQRIS, rbTransfer);
        Separator sep2 = new Separator();
        // Panel detail pembayaran
        panelTunai = buildPanelTunai();
        panelNonTunai = buildPanelNonTunai();
        panelTunai.setVisible(true);
        panelNonTunai.setVisible(false);
        // Toggle visibility
        metodePembayaranGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == rbTunai) {
                panelTunai.setVisible(true);
                panelNonTunai.setVisible(false);
            } else {
                panelTunai.setVisible(false);
                panelNonTunai.setVisible(true);
            }
        });
        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Separator sep3 = new Separator();
        // Total Display
        HBox totalDisplay = new HBox(5);
        totalDisplay.setAlignment(Pos.CENTER_LEFT);
        totalDisplay.setPadding(new Insets(10));
        totalDisplay.setStyle("-fx-background-color: #3498db; -fx-background-radius: 5;");
        Label lblTotalLabel = new Label("TOTAL:");
        lblTotalLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTotalLabel.setTextFill(Color.WHITE);
        lblTotalBayar = new Label("Rp 0");
        lblTotalBayar.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblTotalBayar.setTextFill(Color.WHITE);
        lblTotalBayar.textProperty().bind(Bindings.createStringBinding(() ->
                currencyFormat.format(totalBayarProperty.get()), totalBayarProperty
        ));
        totalDisplay.getChildren().addAll(lblTotalLabel, lblTotalBayar);
        // Buttons
        btnSimpanTransaksi = new Button("💾 SIMPAN TRANSAKSI");
        btnSimpanTransaksi.setPrefHeight(40);
        btnSimpanTransaksi.setMaxWidth(Double.MAX_VALUE);
        btnSimpanTransaksi.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSimpanTransaksi.setOnAction(e -> simpanTransaksi());
        btnSimpanTransaksi.disableProperty().bind(
                Bindings.size(keranjangList).isEqualTo(0)
        );
        
        panel.getChildren().addAll(
                lblHeader, sep1,
                lblMetode, boxMetode, sep2,
                panelTunai, panelNonTunai,
                spacer, sep3,
                totalDisplay,
                btnSimpanTransaksi
        );
        return panel;
    }

    /**
     * Panel Tunai
     */
    private VBox buildPanelTunai() {
        VBox panel = new VBox(8);
        Label lblUangBayar = new Label("Uang Bayar:");
        lblUangBayar.setFont(Font.font("System", FontWeight.BOLD, 12));
        txtUangBayar = new TextField();
        txtUangBayar.setPromptText("0");
        txtUangBayar.setPrefHeight(35);
        txtUangBayar.setStyle("-fx-font-size: 14; -fx-alignment: center-right;");
        // Format dan hitung kembalian
        txtUangBayar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                uangBayarProperty.set(0);
                return;
            }
            try {
                double nilai = Double.parseDouble(newVal.replaceAll("[^0-9.]", ""));
                uangBayarProperty.set(nilai);
            } catch (NumberFormatException e) {
                // Ignore
            }
        });
        uangBayarProperty.addListener((obs, oldVal, newVal) -> {
            double kembalian = newVal.doubleValue() - totalBayarProperty.get();
            kembalianProperty.set(kembalian);
        });
        totalBayarProperty.addListener((obs, oldVal, newVal) -> {
            double kembalian = uangBayarProperty.get() - newVal.doubleValue();
            kembalianProperty.set(kembalian);
        });
        Separator sep = new Separator();
        HBox boxKembalian = new HBox(5);
        boxKembalian.setAlignment(Pos.CENTER_LEFT);
        Label lblKembalianLabel = new Label("Kembalian:");
        lblKembalianLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblKembalian = new Label("Rp 0");
        lblKembalian.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblKembalian.setTextFill(Color.web("#27ae60"));
        lblKembalian.textProperty().bind(Bindings.createStringBinding(() -> {
            double kembalian = kembalianProperty.get();
            return kembalian >= 0 ? currencyFormat.format(kembalian) : "Kurang!";
        }, kembalianProperty));
        boxKembalian.getChildren().addAll(lblKembalianLabel, lblKembalian);
        Label lblNote = new Label("*Pastikan uang bayar cukup");
        lblNote.setStyle("-fx-font-size: 10; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        panel.getChildren().addAll(lblUangBayar, txtUangBayar, sep, boxKembalian, lblNote);
        return panel;
    }

    /**
     * Panel Non-Tunai (QRIS/Transfer)
     */
    private VBox buildPanelNonTunai() {
        VBox panel = new VBox(8);
        Label lblStatus = new Label("Status: Menunggu Konfirmasi");
        lblStatus.setFont(Font.font("System", FontWeight.BOLD, 12));
        lblStatus.setTextFill(Color.web("#f39c12"));
        Label lblInfo = new Label("Pastikan pembayaran sudah diterima sebelum menyimpan transaksi.");
        lblInfo.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");
        lblInfo.setWrapText(true);
        Label lblCatatan = new Label("💡 Untuk QRIS dan Transfer, transaksi akan langsung dianggap LUNAS.");
        lblCatatan.setStyle("-fx-font-size: 10; -fx-text-fill: #3498db; -fx-wrap-text: true; -fx-font-style: italic;");
        lblCatatan.setWrapText(true);
        panel.getChildren().addAll(lblStatus, lblInfo, lblCatatan);
        return panel;
    }

    /**
     * BUILD STATUS BAR
     */
    private HBox buildStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(8, 15, 8, 15));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #34495e; -fx-border-width: 2 0 0 0; -fx-border-color: #2c3e50;");
        lblStatusIcon = new Label("ℹ️");
        lblStatusIcon.setFont(Font.font("System", 12));
        lblStatusMessage = new Label("Siap melayani transaksi");
        lblStatusMessage.setTextFill(Color.web("#ecf0f1"));
        lblStatusMessage.setFont(Font.font("System", 11));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        lblStatusTime = new Label();
        lblStatusTime.setTextFill(Color.web("#95a5a6"));
        lblStatusTime.setFont(Font.font("System", 10));
        statusBar.getChildren().addAll(lblStatusIcon, lblStatusMessage, spacer, lblStatusTime);
        return statusBar;
    }

    /**
     * LOGIC METHODS
     */
    private void tambahKeKeranjang(Varian varian) {
        // Validasi stok
        if (varian.getStokKaos() <= 0) {
            updateStatusBar("⚠️", "Stok tidak tersedia untuk " + varian.getNamaProduk() +
                    " (" + varian.getNamaWarna() + " " + varian.getNamaUkuran() + ")", true);
            return;
        }

        // Cek apakah produk dengan kombinasi warna & ukuran sudah ada di keranjang
        Optional<ItemKeranjang> existing = keranjangList.stream()
                .filter(item -> item.getIdVarian() == varian.getIdVarian())
                .findFirst();

        if (existing.isPresent()) {
            ItemKeranjang item = existing.get();
            if (item.getQuantity() < varian.getStokKaos()) {
                item.setQuantity(item.getQuantity() + 1);
                tableKeranjang.refresh();
                updateStatusBar("✓", "Quantity ditambah: " + varian.getNamaProduk(), false);
            } else {
                updateStatusBar("⚠️", "Stok tidak cukup untuk " + varian.getNamaProduk(), true);
            }
        } else {
            // Ambil harga dari varian (harus ditambahkan di model Varian)
            long hargaJual = varian.getHargaJual(); // Pastikan model Varian memiliki field ini
            ItemKeranjang newItem = new ItemKeranjang(
                    varian.getIdVarian(),
                    varian.getIdProduk(),
                    varian.getNamaProduk(),
                    varian.getNamaWarna(),
                    varian.getNamaUkuran(),
                    1,
                    hargaJual, // Konversi Long ke Double
                    varian.getStokKaos()
            );
            keranjangList.add(newItem);
            Platform.runLater(() -> {
                tableKeranjang.scrollTo(keranjangList.size() - 1); // Scroll ke baris terbaru
                int targetIndex = keranjangList.size() - 1;
                if (targetIndex >= 0) {
                    // Sorot baris selama 500 ms
                    tableKeranjang.getSelectionModel().select(targetIndex);
                    TableRow<ItemKeranjang> row = tableKeranjang.getSkin().getNode().lookupAll(".table-row-cell")
                        .stream()
                        .filter(node -> node instanceof TableRow)
                        .map(node -> (TableRow<ItemKeranjang>) node)
                        .filter(row2 -> row2.getIndex() == targetIndex)
                        .findFirst()
                        .orElse(null);

                    if (row != null) {
                        row.setStyle("-fx-background-color: #e8f4f8;"); // highlight biru muda
                        Timeline unhighlight = new Timeline(
                            new KeyFrame(Duration.millis(500), e -> row.setStyle(""))
                        );
                        unhighlight.setCycleCount(1);
                        unhighlight.play();
                    }
                }
            });
            updateStatusBar("✓", "Ditambahkan: " + varian.getNamaProduk(), false);
        }
        hitungTotal();
    }

    private void tambahQty(ItemKeranjang item) {
        if (item.getQuantity() < item.getStokTersedia()) {
            item.setQuantity(item.getQuantity() + 1);
            tableKeranjang.refresh();
            hitungTotal();
        } else {
            updateStatusBar("⚠️", "Stok maksimal: " + item.getStokTersedia(), true);
        }
    }

    private void kurangiQty(ItemKeranjang item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            tableKeranjang.refresh();
            hitungTotal();
        } else {
            hapusDariKeranjang(item);
        }
    }

    private void hapusDariKeranjang(ItemKeranjang item) {
        // Simpan item yang dihapus
        lastRemovedItem = item;
        keranjangList.remove(item);
        hitungTotal();

        // Tampilkan notifikasi dengan tombol Undo
        String message = "Item dihapus: " + item.getNamaProduk();
        ButtonType btnUndo = new ButtonType("Undo");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, btnUndo, ButtonType.CLOSE);
        alert.setTitle("Item Dihapus");
        alert.setHeaderText(null);
        alert.initModality(Modality.APPLICATION_MODAL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnUndo) {
            // Undo: kembalikan item
            keranjangList.add(lastRemovedItem);
            hitungTotal();
            tableKeranjang.scrollTo(keranjangList.size() - 1);
            updateStatusBar("✓", "Item dikembalikan ke keranjang", false);
        } else {
            // Batalkan undo: hapus referensi
            lastRemovedItem = null;
        }
    }

    private void hitungTotal() {
        int totalItem = 0;
        double totalBayar = 0;
        for (ItemKeranjang item : keranjangList) {
            totalItem += item.getQuantity();
            totalBayar += item.getSubtotal();
        }
        totalItemProperty.set(totalItem);
        totalBayarProperty.set(totalBayar);
    }

    private void simpanTransaksi() {
        // Validasi keranjang
        if (keranjangList.isEmpty()) {
            updateStatusBar("⚠️", "Keranjang kosong!", true);
            return;
        }

        // Validasi jam operasional
        if (!isJamOperasional()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Transaksi Ditolak");
            alert.setHeaderText("Diluar Jam Operasional");
            alert.setContentText("Transaksi hanya dapat dilakukan pada jam " +
                jamBuka + " - " + jamTutup);
            alert.showAndWait();
            updateStatusBar("⚠️", "Transaksi ditolak. Toko tutup.", true);
            return;
        }

        // Validasi pembayaran tunai
        RadioButton selected = (RadioButton) metodePembayaranGroup.getSelectedToggle();
        String metode = selected.getText();
        if (selected == rbTunai) {
            if (uangBayarProperty.get() < totalBayarProperty.get()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Pembayaran Tidak Cukup");
                alert.setHeaderText("Uang bayar kurang!");
                alert.setContentText("Total: " + currencyFormat.format(totalBayarProperty.get()) +
                    "\nUang Bayar: " + currencyFormat.format(uangBayarProperty.get()));
                alert.showAndWait();
                updateStatusBar("⚠️", "Uang bayar kurang dari total!", true);
                return;
            }
        }

        // Validasi stok
        for (ItemKeranjang item : keranjangList) {
            Varian varian = varianList.stream()
                .filter(v -> v.getIdVarian() == item.getIdVarian())
                .findFirst()
                .orElse(null);
            if (varian != null && varian.getStokKaos() < item.getQuantity()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Stok Tidak Cukup");
                alert.setHeaderText("Gagal menyimpan transaksi");
                alert.setContentText("Stok tidak mencukupi untuk: " + item.getNamaProduk() +
                    "\nStok tersedia: " + varian.getStokKaos() +
                    "\nQuantity diminta: " + item.getQuantity());
                alert.showAndWait();
                updateStatusBar("⚠️", "Stok tidak cukup: " + item.getNamaProduk(), true);
                return;
            }
        }

        // Konfirmasi simpan
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Transaksi");
        confirm.setHeaderText("Simpan transaksi ini?");
        confirm.setContentText("Total: " + currencyFormat.format(totalBayarProperty.get()) +
        "\n" + "Metode: " + metode);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // --- SIMPAN KE DATABASE ---
                    TransaksiDAO transaksiDAO = new TransaksiDAO();
                    DetailTransaksiDAO detailDAO = new DetailTransaksiDAO();
                    VarianDAO varianDAO = new VarianDAO();

                    // Buat objek Transaksi
                    Transaksi transaksi = new Transaksi();
                    transaksi.setIdCustomer(idKasir); // Untuk offline, customer = kasir
                    transaksi.setIdKasir(idKasir);
                    transaksi.setKodeTransaksi(generateNoTransaksi());
                    transaksi.setTotal((long) totalBayarProperty.get());
                    transaksi.setMetodePembayaran(metode);
                    transaksi.setStatusTransaksi("selesai");

                    // Simpan ke tabel transaksi
                    Long idTransaksi = transaksiDAO.createTransaksi(transaksi);

                    if (idTransaksi == null) {
                        updateStatusBar("❌", "Gagal menyimpan transaksi!", true);
                        return;
                    }

                    // Simpan detail transaksi & update stok
                    for (ItemKeranjang item : keranjangList) {
                        // Ambil Varian
                        Varian varian = varianList.stream()
                            .filter(v -> v.getIdVarian() == item.getIdVarian())
                            .findFirst()
                            .orElse(null);

                        if (varian != null) {
                            // Simpan detail transaksi
                            DetailTransaksi detail = new DetailTransaksi();
                            detail.setIdTransaksi(idTransaksi);
                            detail.setIdProduk(varian.getIdProduk()); // Gunakan id_produk dari varian
                            detail.setJumlah((long) item.getQuantity());
                            detail.setHargaSatuan((long) item.getHarga());
                            detail.setSubtotal((long) item.getSubtotal());

                            if (!detailDAO.createDetailTransaksi(detail)) {
                                updateStatusBar("❌", "Gagal menyimpan detail transaksi!", true);
                                return;
                            }

                            // Kurangi stok di varian
                            if (!varianDAO.updateStokVarian(varian.getIdVarian(), item.getQuantity())) {
                                updateStatusBar("❌", "Gagal mengupdate stok!", true);
                                return;
                            }
                        }
                    }

                    // --- SETELAH SUKSES ---
                    noTransaksiTerakhir = transaksi.getKodeTransaksi();
                    transaksiSaved = true;

                    // Ambil nilai-nilai penting SEBELUM reset form
                    String noTrans = noTransaksiTerakhir;
                    String namaKsr = namaKasir;
                    double totalBayar = totalBayarProperty.get();
                    double uangBayar = uangBayarProperty.get();
                    double kembalian = kembalianProperty.get();
                    boolean isTunai = (selected == rbTunai);
                    java.util.List<ItemKeranjang> snapshotKeranjang = new java.util.ArrayList<>(keranjangList);

                    BooleanBinding disableBinding = Bindings.or(
                        Bindings.size(keranjangList).isEqualTo(0),
                        transaksiAktifProperty
                    );
                    btnSimpanTransaksi.disableProperty().bind(disableBinding);

                    loadDataFromDatabase();

                    // ✅ BERSIHKAN KERANJANG
                    keranjangList.clear();
                    txtUangBayar.clear();
                    rbTunai.setSelected(true);
                    hitungTotal(); // Update total
                    tableVarian.refresh(); // Refresh tampilan stok

                    updateStatusBar("✓", "Transaksi berhasil! ID: " + noTransaksiTerakhir, false);

                    // --- TAMPILKAN STRUK ---
                    Platform.runLater(() -> {
                        Struk strukWindow = new Struk(
                            noTrans,
                            namaKsr,
                            totalBayar,
                            uangBayar,
                            kembalian,
                            metode, // ✅ Variabel ini sudah dideklarasikan di atas
                            isTunai,
                            snapshotKeranjang
                        );
                        strukWindow.show(); // blocking sampai ditutup
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    updateStatusBar("❌", "Terjadi kesalahan saat menyimpan transaksi!", true);
                }
            }
        });
    }

    private void batalkanTransaksi() {
        if (!keranjangList.isEmpty()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Konfirmasi Batal");
            confirm.setHeaderText("Batalkan transaksi ini?");
            confirm.setContentText("Semua item di keranjang akan dihapus.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    resetForm();
                }
            });
        } else {
            resetForm();
        }
    }

    private void resetForm() {
        keranjangList.clear();
        txtUangBayar.clear();
        rbTunai.setSelected(true);
        transaksiSaved = false;
        noTransaksiTerakhir = "";
        hitungTotal();
        txtSearchProduk.clear();
        txtSearchProduk.requestFocus();
        updateStatusBar("ℹ️", "Siap melayani transaksi", false);
    }

    private void refreshVarian() {
        txtSearchProduk.clear();
        filteredVarian.setPredicate(p -> true);
        updateStatusBar("ℹ️", "Data varian di-refresh", false);
    }

    private boolean isJamOperasional() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(jamBuka) && !now.isAfter(jamTutup);
    }

    private String generateNoTransaksi() {
        return "TRX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private void startRealtimeClock() {
        clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            lblJamRealtime.setText(now.format(dateFormatter) + " | " + now.format(timeFormatter));
            // Update status toko
            if (isJamOperasional()) {
                lblStatusToko.setText("BUKA");
                lblStatusToko.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 5 10 5 10; -fx-font-weight: bold;");
            } else {
                lblStatusToko.setText("TUTUP");
                lblStatusToko.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 5 10 5 10; -fx-font-weight: bold;");
            }
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void updateStatusBar(String icon, String message, boolean isError) {
        lblStatusIcon.setText(icon);
        lblStatusMessage.setText(message);
        lblStatusMessage.setTextFill(isError ? Color.web("#e74c3c") : Color.web("#ecf0f1"));
        lblStatusTime.setText(LocalTime.now().format(timeFormatter));
        // Auto-hide after 5 seconds
        if (isError || icon.equals("✓")) {
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                updateStatusBar("ℹ️", "Siap melayani transaksi", false);
            }));
            timeline.play();
        }
    }

    private void bukaHalamanLaporan() {
         if (currentUser == null) {
            // Jika tidak ada user, tampilkan error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("User tidak ditemukan");
            alert.setContentText("Tidak dapat membuka laporan karena tidak ada sesi login.");
            alert.showAndWait();
            return;
        }
        // Buat instance dari LaporanKasirPanel
         LaporanKasirPanel laporanPanel = new LaporanKasirPanel(currentUser);

        // Buat stage baru untuk laporan
        Stage laporanStage = new Stage();
        laporanStage.setTitle("DistroZone - Laporan Kasir");
        laporanStage.setMaximized(true); // Agar fullscreen seperti dashboard

        // Buat scene baru dengan laporanPanel sebagai root
        Scene laporanScene = new Scene(laporanPanel, primaryStage.getWidth(), primaryStage.getHeight());

        // Set scene ke stage
        laporanStage.setScene(laporanScene);

        // Tampilkan stage
        laporanStage.show();

        // Optional: Jika ingin menutup dashboard saat laporan dibuka (tidak wajib)
        // primaryStage.hide(); // Uncomment jika ingin sembunyikan dashboard
    }

    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin keluar?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // Hentikan timeline jam real-time
            if (clock != null) {
                clock.stop();
            }
            // Hentikan executor
            if (!executor.isShutdown()) {
                executor.shutdownNow();
            }

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
    }

    private void applyStyles(BorderPane root) {
        // Custom CSS styling (optional)
        // Bisa ditambahkan external CSS file
    }

    public void show() {
        primaryStage.show();
    }

    public Scene getScene() {
        return this.scene;
    }

    /**
     * MODEL CLASSES
     */
    public static class ItemKeranjang {
        private Long idVarian; // Sekarang pakai ID Varian, bukan ID Produk
        private Long idProduk;
        private String namaProduk;
        private String warna;
        private String ukuran;
        private int quantity;
        private double harga;
        private Long stokTersedia;

        public ItemKeranjang(Long idVarian, Long idProduk, String namaProduk, String warna, String ukuran,
                              int quantity, double harga, Long stokTersedia) {
            this.idVarian = idVarian;
            this.idProduk = idProduk;
            this.namaProduk = namaProduk;
            this.warna = warna;
            this.ukuran = ukuran;
            this.quantity = quantity;
            this.harga = harga;
            this.stokTersedia = stokTersedia;
        }

        public double getSubtotal() {
            return quantity * harga;
        }

        // Getters & Setters
        public Long getIdVarian() { return idVarian; }
         public Long getIdProduk() { return idProduk; }

        public String getNamaProduk() {
            return namaProduk;
        }

        public String getWarna() {
            return warna;
        }

        public String getUkuran() {
            return ukuran;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getHarga() {
            return harga;
        }

        public long getStokTersedia() {
            return stokTersedia;
        }
    }
}