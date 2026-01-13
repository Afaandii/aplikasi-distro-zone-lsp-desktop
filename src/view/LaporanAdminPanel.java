package view;

import dao.TransaksiDAO;
import dao.DetailTransaksiDAO;
import model.Transaksi;
import model.DetailTransaksi;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LaporanAdminPanel extends VBox {
    
    private TransaksiDAO transaksiDAO;
    private DetailTransaksiDAO detailTransaksiDAO;
    private TableView<Transaksi> tableTransaksi;
    private DatePicker dpStartDate;
    private DatePicker dpEndDate;
    private ComboBox<String> cmbMetodePembayaran;
    private ComboBox<String> cmbKasir;
    private TextField txtSearch;
    
    private Label lblTotalPenjualan;
    private Label lblJumlahTransaksi;
    private Label lblRataRata;
    private Label lblLabaRugi;
    
    private NumberFormat currencyFormat;
    private List<Transaksi> allTransaksiData;
    private PieChart pieChart;
    private LineChart<String, Number> lineChart;
    private Label lblCount;
    
    public LaporanAdminPanel() {
        this.transaksiDAO = new TransaksiDAO();
        this.detailTransaksiDAO = new DetailTransaksiDAO();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        this.allTransaksiData = new ArrayList<>();
        
        initializeUI();
            javafx.application.Platform.runLater(() -> {
                loadDefaultData();
            });
    }
    
    private void initializeUI() {
//        this.setSpacing(20);
//        this.setPadding(new Insets(25));
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // tidak bisa ditutup

        Tab tabPenjualan = new Tab("Penjualan", createContentContainer()); // isi dengan semua komponen sekarang
        Tab tabRugiLaba = new Tab("Rugi Laba", new LaporanRugiLaba());

        tabPane.getTabs().addAll(tabPenjualan, tabRugiLaba);
        this.getChildren().add(tabPane);
    }
    
    private ScrollPane createContentContainer() {
        // Buat VBox untuk menampung semua konten
        VBox contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(25));

        // Header
        HBox headerBox = createHeaderSection();

        // Filter Section
        VBox filterSection = createFilterSection();

        // Statistics Cards
        HBox statsCards = createStatsCards();

        // Charts Section
        HBox chartsSection = createChartsSection();

        // Table Section
        VBox tableSection = createTableSection();

        // Tambahkan semua ke container
        contentContainer.getChildren().addAll(headerBox, filterSection, statsCards, chartsSection, tableSection);

        // Bungkus dengan ScrollPane
        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");

        return scrollPane; // Kembalikan ScrollPane, bukan VBox
    }
    
    private HBox createHeaderSection() {
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        
        Label headerLabel = new Label("Laporan Penjualan Admin");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        headerLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Quick action buttons
        Button btnRefresh = new Button("⟳ Refresh");
        btnRefresh.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                           "-fx-font-size: 13px; -fx-background-radius: 5; -fx-cursor: hand; " +
                           "-fx-padding: 8 15 8 15;");
        btnRefresh.setOnAction(e -> loadLaporanData());
        
        Button btnClearFilter = new Button("✕ Reset Filter");
        btnClearFilter.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                               "-fx-font-size: 13px; -fx-background-radius: 5; -fx-cursor: hand; " +
                               "-fx-padding: 8 15 8 15;");
        btnClearFilter.setOnAction(e -> clearAllFilters());
        
        headerBox.getChildren().addAll(headerLabel, spacer, btnRefresh, btnClearFilter);
        return headerBox;
    }
    
    private VBox createFilterSection() {
        VBox filterBox = new VBox(15);
        filterBox.setPadding(new Insets(20));
        filterBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label filterLabel = new Label("Filter & Pencarian");
        filterLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        filterLabel.setStyle("-fx-text-fill: #34495e;");

        // === ROW 1: Tanggal + Periode Cepat + Tombol Filter ===
        HBox row1 = new HBox(15);
        row1.setAlignment(Pos.CENTER_LEFT);

        // Tanggal Mulai
        VBox startDateBox = new VBox(5);
        Label lblStart = new Label("Tanggal Mulai");
        lblStart.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        dpStartDate = new DatePicker(LocalDate.now().withDayOfMonth(1));
        dpStartDate.setPrefWidth(180);
        dpStartDate.setStyle("-fx-background-radius: 5;");
        startDateBox.getChildren().addAll(lblStart, dpStartDate);

        dpStartDate.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && dpEndDate.getValue() != null) {
                if (newVal.isAfter(dpEndDate.getValue())) {
                    showAlert("Error", "Tanggal mulai tidak boleh lebih besar dari tanggal akhir", Alert.AlertType.ERROR);
                    dpStartDate.setValue(old);
                } else {
                    loadLaporanData();
                }
            }
        });

        // Tanggal Akhir
        VBox endDateBox = new VBox(5);
        Label lblEnd = new Label("Tanggal Akhir");
        lblEnd.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        dpEndDate = new DatePicker(LocalDate.now());
        dpEndDate.setPrefWidth(180);
        dpEndDate.setStyle("-fx-background-radius: 5;");
        endDateBox.getChildren().addAll(lblEnd, dpEndDate);

        dpEndDate.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && dpStartDate.getValue() != null) {
                if (newVal.isBefore(dpStartDate.getValue())) {
                    showAlert("Error", "Tanggal akhir tidak boleh lebih kecil dari tanggal mulai", Alert.AlertType.ERROR);
                    dpEndDate.setValue(old);
                } else {
                    loadLaporanData();
                }
            }
        });

        // Periode Cepat
        VBox quickDateBox = new VBox(5);
        Label lblQuick = new Label("Periode Cepat");
        lblQuick.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        HBox quickButtons = new HBox(8);
        Button btnToday = createQuickDateButton("Hari Ini");
        Button btnWeek = createQuickDateButton("Minggu Ini");
        Button btnMonth = createQuickDateButton("Bulan Ini");

        btnToday.setOnAction(e -> {
            dpStartDate.setValue(LocalDate.now());
            dpEndDate.setValue(LocalDate.now());
            loadLaporanData();
        });
        btnWeek.setOnAction(e -> {
            dpStartDate.setValue(LocalDate.now().minusDays(7));
            dpEndDate.setValue(LocalDate.now());
            loadLaporanData();
        });
        btnMonth.setOnAction(e -> {
            dpStartDate.setValue(LocalDate.now().withDayOfMonth(1));
            dpEndDate.setValue(LocalDate.now());
            loadLaporanData();
        });

        quickButtons.getChildren().addAll(btnToday, btnWeek, btnMonth);
        quickDateBox.getChildren().addAll(lblQuick, quickButtons);

        // 🔥 TOMBOL FILTER DI SINI — DI DALAM ROW1
        Button btnFilter = new Button("🔍 Tampilkan Laporan");
        btnFilter.setPrefWidth(180);
        btnFilter.setPrefHeight(35);
        btnFilter.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-font-weight: bold; " +
                          "-fx-background-radius: 5; -fx-cursor: hand;");
        btnFilter.setOnMouseEntered(e -> btnFilter.setStyle(
            "-fx-background-color: #2980b9; -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-font-weight: bold; " +
            "-fx-background-radius: 5; -fx-cursor: hand;"));
        btnFilter.setOnMouseExited(e -> btnFilter.setStyle(
            "-fx-background-color: #3498db; -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-font-weight: bold; " +
            "-fx-background-radius: 5; -fx-cursor: hand;"));
        btnFilter.setOnAction(e -> loadLaporanData());

        // Tambahkan SEMUA ke row1 → termasuk btnFilter di akhir
        row1.getChildren().addAll(startDateBox, endDateBox,btnFilter, quickDateBox);

        // === ROW 2: Metode Pembayaran & Filter Kasir ===
        HBox row2 = new HBox(15);
        row2.setAlignment(Pos.CENTER_LEFT);

        // Metode Pembayaran
        VBox metodeBox = new VBox(5);
        Label lblMetode = new Label("Metode Pembayaran");
        lblMetode.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        cmbMetodePembayaran = new ComboBox<>();
        cmbMetodePembayaran.setPrefWidth(180);
        cmbMetodePembayaran.setStyle("-fx-background-radius: 5;");
        List<String> metodeList = transaksiDAO.getDistinctMetodePembayaran();
        cmbMetodePembayaran.getItems().add("Semua");
        cmbMetodePembayaran.getItems().addAll(metodeList);
        cmbMetodePembayaran.setValue("Semua");
        metodeBox.getChildren().addAll(lblMetode, cmbMetodePembayaran);

        // Kasir
        VBox kasirBox = new VBox(5);
        Label lblKasir = new Label("Filter Kasir");
        lblKasir.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        cmbKasir = new ComboBox<>();
        cmbKasir.setPrefWidth(180);
        cmbKasir.setStyle("-fx-background-radius: 5;");
        List<String> kasirList = transaksiDAO.getDistinctKasir();
        cmbKasir.getItems().add("Semua");
        cmbKasir.getItems().addAll(kasirList);
        cmbKasir.setValue("Semua");
        kasirBox.getChildren().addAll(lblKasir, cmbKasir);

        // Listener otomatis
        cmbMetodePembayaran.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cmbKasir.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        row2.getChildren().addAll(metodeBox, kasirBox);

        // === ROW 3: Pencarian + Export ===
        HBox searchBox = new HBox(15);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        VBox searchFieldBox = new VBox(5);
        Label lblSearch = new Label("Pencarian");
        lblSearch.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        txtSearch = new TextField();
        txtSearch.setPrefWidth(300);
        txtSearch.setPromptText("Cari kode transaksi atau nama customer...");
        txtSearch.setStyle("-fx-background-radius: 5;");
        txtSearch.textProperty().addListener((obs, old, newVal) -> applyFilters());
        searchFieldBox.getChildren().addAll(lblSearch, txtSearch);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 🔥 TOMBOL EXPORT tetap di bawah
        Button btnExport = new Button("📄 Export PDF");
        btnExport.setPrefWidth(140);
        btnExport.setPrefHeight(35);
        btnExport.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-font-weight: bold; " +
                          "-fx-background-radius: 5; -fx-cursor: hand;");
        btnExport.setOnAction(e -> exportToPDF());

        Button btnExportExcel = new Button("📊 Export Excel");
        btnExportExcel.setPrefWidth(140);
        btnExportExcel.setPrefHeight(35);
        btnExportExcel.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; " +
                               "-fx-font-size: 14px; -fx-font-weight: bold; " +
                               "-fx-background-radius: 5; -fx-cursor: hand;");
        btnExportExcel.setOnAction(e -> exportToExcel());

        searchBox.getChildren().addAll(searchFieldBox, spacer, btnExport, btnExportExcel);

        // === GABUNG SEMUA KE filterBox ===
        VBox dateFilterBox = new VBox(15);
        dateFilterBox.setAlignment(Pos.TOP_LEFT);
        dateFilterBox.getChildren().addAll(row1, row2);

        filterBox.getChildren().addAll(filterLabel, dateFilterBox, searchBox);
        return filterBox;
    }
    
    private Button createQuickDateButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #34495e; " +
                    "-fx-font-size: 11px; -fx-background-radius: 5; -fx-cursor: hand; " +
                    "-fx-padding: 5 10 5 10;");
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #bdc3c7; -fx-text-fill: #2c3e50; " +
            "-fx-font-size: 11px; -fx-background-radius: 5; -fx-cursor: hand; " +
            "-fx-padding: 5 10 5 10;"));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #ecf0f1; -fx-text-fill: #34495e; " +
            "-fx-font-size: 11px; -fx-background-radius: 5; -fx-cursor: hand; " +
            "-fx-padding: 5 10 5 10;"));
        return btn;
    }
    
    private HBox createStatsCards() {
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER);
        
        VBox cardPenjualan = createStatCard("💰 Total Penjualan", "Rp 0", "#3498db");
        lblTotalPenjualan = (Label) cardPenjualan.getChildren().get(1);
        
        VBox cardTransaksi = createStatCard("📊 Jumlah Transaksi", "0", "#e74c3c");
        lblJumlahTransaksi = (Label) cardTransaksi.getChildren().get(1);
        
        VBox cardRataRata = createStatCard("📈 Rata-rata Transaksi", "Rp 0", "#f39c12");
        lblRataRata = (Label) cardRataRata.getChildren().get(1);
        
        VBox cardLabaRugi = createStatCard("💵 Laba Bersih", "Rp 0", "#27ae60");
        lblLabaRugi = (Label) cardLabaRugi.getChildren().get(1);
        
        statsBox.getChildren().addAll(cardPenjualan, cardTransaksi, cardRataRata, cardLabaRugi);
        HBox.setHgrow(cardPenjualan, Priority.ALWAYS);
        HBox.setHgrow(cardTransaksi, Priority.ALWAYS);
        HBox.setHgrow(cardRataRata, Priority.ALWAYS);
        HBox.setHgrow(cardLabaRugi, Priority.ALWAYS);
        
        return statsBox;
    }
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25, 20, 25, 20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 13));
        titleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
    
    private HBox createChartsSection() {
        HBox chartsBox = new HBox(15);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.setPrefHeight(250);

        // Pie Chart - Metode Pembayaran
        VBox pieChartBox = new VBox(10);
        pieChartBox.setPadding(new Insets(15));
        pieChartBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label pieTitle = new Label("Metode Pembayaran");
        pieTitle.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        pieTitle.setStyle("-fx-text-fill: #34495e;");

        // 🔥 Simpan ke field kelas: pieChart
        pieChart = new PieChart();
        pieChart.setPrefHeight(200);
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);

        pieChartBox.getChildren().addAll(pieTitle, pieChart);

        // Line Chart - Performa Kasir
        VBox lineChartBox = new VBox(10);
        lineChartBox.setPadding(new Insets(15));
        lineChartBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label lineTitle = new Label("Performa Kasir");
        lineTitle.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        lineTitle.setStyle("-fx-text-fill: #34495e;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total Penjualan (Juta)");

        // 🔥 Simpan ke field kelas: lineChart
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setPrefHeight(200);
        lineChart.setLegendVisible(false);
        lineChart.setCreateSymbols(true);

        lineChartBox.getChildren().addAll(lineTitle, lineChart);

        HBox.setHgrow(pieChartBox, Priority.ALWAYS);
        HBox.setHgrow(lineChartBox, Priority.ALWAYS);

        chartsBox.getChildren().addAll(pieChartBox, lineChartBox);
        return chartsBox;
    }
    
    private VBox createTableSection() {
        VBox tableBox = new VBox(15);
        tableBox.setPadding(new Insets(20));
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        HBox tableHeader = new HBox(10);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label tableLabel = new Label("Rincian Transaksi");
        tableLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        tableLabel.setStyle("-fx-text-fill: #34495e;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        lblCount = new Label("Total: 0 transaksi");
        lblCount.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        
        tableHeader.getChildren().addAll(tableLabel, spacer, lblCount);
        
        tableTransaksi = new TableView<>();
        tableTransaksi.setStyle("-fx-background-color: transparent;");
        tableTransaksi.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableTransaksi.setPlaceholder(new Label("Tidak ada data transaksi"));
        
        // Update count when items change
        tableTransaksi.getItems().addListener((javafx.collections.ListChangeListener.Change<?> c) -> {
            lblCount.setText("Total: " + tableTransaksi.getItems().size() + " transaksi");
        });
        
        // Columns
        TableColumn<Transaksi, String> colNo = new TableColumn<>("No");
        colNo.setMaxWidth(50);
        colNo.setStyle("-fx-alignment: CENTER;");
        colNo.setCellFactory(col -> new TableCell<Transaksi, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        
        TableColumn<Transaksi, String> colKode = new TableColumn<>("Kode Transaksi");
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeTransaksi"));
        colKode.setPrefWidth(170);
        
        TableColumn<Transaksi, String> colKasir = new TableColumn<>("Kasir");
        colKasir.setCellValueFactory(new PropertyValueFactory<>("namaKasir"));
        colKasir.setPrefWidth(120);
        
        TableColumn<Transaksi, Timestamp> colTanggal = new TableColumn<>("Tanggal");
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colTanggal.setPrefWidth(150);
        colTanggal.setCellFactory(col -> new TableCell<Transaksi, Timestamp>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            @Override
            protected void updateItem(Timestamp timestamp, boolean empty) {
                super.updateItem(timestamp, empty);
                if (empty || timestamp == null) {
                    setText(null);
                } else {
                    setText(timestamp.toLocalDateTime().format(formatter));
                }
            }
        });
        
        TableColumn<Transaksi, String> colMetode = new TableColumn<>("Metode");
        colMetode.setCellValueFactory(new PropertyValueFactory<>("metodePembayaran"));
        colMetode.setPrefWidth(90);
        colMetode.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<Transaksi, Long> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(120);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT;");
        colTotal.setCellFactory(col -> new TableCell<Transaksi, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
                }
            }
        });
        
        TableColumn<Transaksi, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusTransaksi"));
        colStatus.setPrefWidth(90);
        colStatus.setStyle("-fx-alignment: CENTER;");
        colStatus.setCellFactory(col -> new TableCell<Transaksi, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    if (item.equalsIgnoreCase("selesai")) {
                        setStyle("-fx-background-color: #d5f4e6; -fx-text-fill: #27ae60; " +
                                "-fx-font-weight: bold; -fx-alignment: CENTER; " +
                                "-fx-background-radius: 5; -fx-padding: 5;");
                    } else if (item.equalsIgnoreCase("pending")) {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #f39c12; " +
                                "-fx-font-weight: bold; -fx-alignment: CENTER; " +
                                "-fx-background-radius: 5; -fx-padding: 5;");
                    } else {
                        setStyle("-fx-background-color: #fadbd8; -fx-text-fill: #e74c3c; " +
                                "-fx-font-weight: bold; -fx-alignment: CENTER; " +
                                "-fx-background-radius: 5; -fx-padding: 5;");
                    }
                }
            }
        });
        
        TableColumn<Transaksi, Void> colAction = new TableColumn<>("Aksi");
        colAction.setPrefWidth(100);
        colAction.setStyle("-fx-alignment: CENTER;");
        colAction.setCellFactory(col -> new TableCell<Transaksi, Void>() {
            private final Button btnDetail = new Button("Detail");
            
            {
                btnDetail.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                  "-fx-font-size: 12px; -fx-background-radius: 5; -fx-cursor: hand;");
                btnDetail.setPrefWidth(70);
                btnDetail.setOnAction(e -> {
                    Transaksi transaksi = getTableView().getItems().get(getIndex());
                    showDetailDialog(transaksi);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDetail);
                }
            }
        });
        
        tableTransaksi.getColumns().addAll(colNo, colKode, colKasir, 
                                          colTanggal, colMetode, colTotal, colStatus, colAction);
        
        VBox.setVgrow(tableTransaksi, Priority.ALWAYS);
        tableBox.getChildren().addAll(tableHeader, tableTransaksi);
        
        return tableBox;
    }
    
    private void loadDefaultData() {
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        loadData(startDate, endDate);
    }
    
    private void loadLaporanData() {
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();
        
        if (startDate == null || endDate == null) {
            showAlert("Error", "Harap pilih tanggal mulai dan tanggal akhir", Alert.AlertType.ERROR);
            return;
        }
        
        if (startDate.isAfter(endDate)) {
            showAlert("Error", "Tanggal mulai tidak boleh lebih besar dari tanggal akhir", Alert.AlertType.ERROR);
            return;
        }
        
        loadData(startDate, endDate);
    }
    
    private void loadData(LocalDate startDate, LocalDate endDate) {
        Date sqlStartDate = Date.valueOf(startDate);
        Date sqlEndDate = Date.valueOf(endDate);
        
        // Load all transaksi data (admin sees all)
        allTransaksiData = transaksiDAO.getAllTransaksi(sqlStartDate, sqlEndDate);
        
        
        // Apply filters
        applyFilters();
        
        // Update statistics
        updateStatistics(sqlStartDate, sqlEndDate);
        
        // Update charts
        updateCharts();
    }
    
    private void updateStatisticsFromFilteredData(List<Transaksi> filteredList) {
    Long totalPenjualan = filteredList.stream()
        .mapToLong(Transaksi::getTotal)
        .sum();
    int jumlahTransaksi = filteredList.size();
    Long rataRata = jumlahTransaksi > 0 ? totalPenjualan / jumlahTransaksi : 0;
    Long labaRugi = calculateLabaRugiFromList(filteredList); // Anda bisa implementasi sendiri
    
    lblTotalPenjualan.setText(currencyFormat.format(totalPenjualan));
    lblJumlahTransaksi.setText(String.valueOf(jumlahTransaksi));
    lblRataRata.setText(currencyFormat.format(rataRata));
    lblLabaRugi.setText(currencyFormat.format(labaRugi));
}

    private void updateChartsFromFilteredData(List<Transaksi> filteredList) {
        if (pieChart == null || lineChart == null) return;

        // Pie Chart - Metode Pembayaran
        Map<String, Long> metodeMap = filteredList.stream()
            .filter(t -> "selesai".equalsIgnoreCase(t.getStatusTransaksi()))
            .collect(Collectors.groupingBy(
                Transaksi::getMetodePembayaran,
                Collectors.summingLong(Transaksi::getTotal)
            ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        metodeMap.forEach((metode, total) -> {
            pieData.add(new PieChart.Data(metode + " (" + currencyFormat.format(total) + ")", total));
        });
        pieChart.setData(pieData);

        // Line Chart - Performa Kasir
        Map<String, Long> kasirMap = filteredList.stream()
            .filter(t -> "selesai".equalsIgnoreCase(t.getStatusTransaksi()))
            .collect(Collectors.groupingBy(
                Transaksi::getNamaKasir,
                Collectors.summingLong(Transaksi::getTotal)
            ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Penjualan");

        kasirMap.forEach((kasir, total) -> {
            series.getData().add(new XYChart.Data<>(kasir, total / 1000000.0)); // Convert to millions
        });

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }

    private Long calculateLabaRugiFromList(List<Transaksi> list) {
        // Implementasi sederhana: asumsi laba = total penjualan (bisa diganti dengan logika sebenarnya)
        return list.stream()
            .mapToLong(Transaksi::getTotal)
            .sum();
    }
    
    private void applyFilters() {
        List<Transaksi> filteredList = new ArrayList<>(allTransaksiData);

        // Filter by search text
        String searchText = txtSearch.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.toLowerCase().trim();
            filteredList = filteredList.stream()
                .filter(t -> 
                    (t.getKodeTransaksi() != null && t.getKodeTransaksi().toLowerCase().contains(search)) ||
                    (t.getNamaKasir() != null && t.getNamaKasir().toLowerCase().contains(search)) ||
                    (t.getMetodePembayaran() != null && t.getMetodePembayaran().toLowerCase().contains(search)) ||
                    (t.getStatusTransaksi() != null && t.getStatusTransaksi().toLowerCase().contains(search))
                )
                .collect(Collectors.toList());
        }

        // Filter by metode pembayaran
        String selectedMetode = cmbMetodePembayaran.getValue();
        if (!"Semua".equals(selectedMetode)) {
            filteredList = filteredList.stream()
                .filter(t -> selectedMetode.equals(t.getMetodePembayaran()))
                .collect(Collectors.toList());
        }

        // Filter by kasir
        String selectedKasir = cmbKasir.getValue();
        if (!"Semua".equals(selectedKasir)) {
            filteredList = filteredList.stream()
                .filter(t -> selectedKasir.equals(t.getNamaKasir()))
                .collect(Collectors.toList());
        }

        ObservableList<Transaksi> observableList = FXCollections.observableArrayList(filteredList);
        tableTransaksi.setItems(observableList);

        // Update statistics & charts
        updateStatisticsFromFilteredData(filteredList);
        updateChartsFromFilteredData(filteredList);
        lblCount.setText("Total: " + tableTransaksi.getItems().size() + " transaksi");
    }
    
    private void updateStatistics(Date startDate, Date endDate) {
        // Get statistics for all kasir (admin view)
        Long totalPenjualan = transaksiDAO.getTotalPenjualan(null, startDate, endDate);
        int jumlahTransaksi = transaksiDAO.getJumlahTransaksi(null, startDate, endDate);
        Long rataRata = jumlahTransaksi > 0 ? totalPenjualan / jumlahTransaksi : 0;
        Long labaRugi = transaksiDAO.getLabaRugi(startDate, endDate);
        
        lblTotalPenjualan.setText(currencyFormat.format(totalPenjualan));
        lblJumlahTransaksi.setText(String.valueOf(jumlahTransaksi));
        lblRataRata.setText(currencyFormat.format(rataRata));
        lblLabaRugi.setText(currencyFormat.format(labaRugi));
    }
    
    private void updateCharts() {
        if (pieChart == null || lineChart == null) {
            return; // Jaga-jaga jika dipanggil sebelum UI siap
        }

        // Update Pie Chart - Metode Pembayaran
        Map<String, Long> metodeMap = allTransaksiData.stream()
            .filter(t -> "selesai".equalsIgnoreCase(t.getStatusTransaksi()))
            .collect(Collectors.groupingBy(
                Transaksi::getMetodePembayaran,
                Collectors.summingLong(Transaksi::getTotal)
            ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        metodeMap.forEach((metode, total) -> {
            pieData.add(new PieChart.Data(metode + " (" + currencyFormat.format(total) + ")", total));
        });
        pieChart.setData(pieData);

        // Update Line Chart - Performa Kasir
        Map<String, Long> kasirMap = allTransaksiData.stream()
            .filter(t -> "selesai".equalsIgnoreCase(t.getStatusTransaksi()))
            .collect(Collectors.groupingBy(
                Transaksi::getNamaKasir,
                Collectors.summingLong(Transaksi::getTotal)
            ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Penjualan");

        kasirMap.forEach((kasir, total) -> {
            series.getData().add(new XYChart.Data<>(kasir, total / 1000000.0)); // Convert to millions
        });

        lineChart.getData().clear();
        lineChart.getData().add(series);
    }
    
    private void clearAllFilters() {
        dpStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        dpEndDate.setValue(LocalDate.now());
        txtSearch.clear();
        loadDefaultData();
    }
    
    private void showDetailDialog(Transaksi transaksi) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detail Transaksi");
        dialog.setHeaderText("Kode Transaksi: " + transaksi.getKodeTransaksi());
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f5f5f5;");
        
        // Info transaksi
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(10);
        infoGrid.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        
        infoGrid.add(createInfoLabel("Kasir:", true), 0, 0);
        infoGrid.add(createInfoLabel(transaksi.getNamaKasir(), false), 1, 0);
        infoGrid.add(createInfoLabel("Customer:", true), 0, 1);
        infoGrid.add(createInfoLabel(transaksi.getNamaCustomer(), false), 1, 1);
        infoGrid.add(createInfoLabel("Tanggal:", true), 0, 2);
        infoGrid.add(createInfoLabel(transaksi.getCreatedAt().toString(), false), 1, 2);
        infoGrid.add(createInfoLabel("Metode:", true), 0, 3);
        infoGrid.add(createInfoLabel(transaksi.getMetodePembayaran(), false), 1, 3);
        infoGrid.add(createInfoLabel("Status:", true), 0, 4);
        infoGrid.add(createInfoLabel(transaksi.getStatusTransaksi().toUpperCase(), false), 1, 4);
        
        // Detail items
        Label lblDetail = new Label("Detail Produk:");
        lblDetail.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        TableView<DetailTransaksi> tableDetail = new TableView<>();
        tableDetail.setPrefHeight(200);
        tableDetail.setStyle("-fx-background-color: white;");
        
        TableColumn<DetailTransaksi, String> colProduk = new TableColumn<>("Produk");
        colProduk.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        
        TableColumn<DetailTransaksi, Long> colJumlah = new TableColumn<>("Jumlah");
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colJumlah.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<DetailTransaksi, Long> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaSatuan"));
        colHarga.setCellFactory(col -> new TableCell<DetailTransaksi, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
            }
        });
        
        TableColumn<DetailTransaksi, Long> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colSubtotal.setCellFactory(col -> new TableCell<DetailTransaksi, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormat.format(item));
                setStyle("-fx-font-weight: bold;");
            }
        });
        
        tableDetail.getColumns().addAll(colProduk, colJumlah, colHarga, colSubtotal);
        
        List<DetailTransaksi> details = detailTransaksiDAO.getDetailByTransaksi(transaksi.getIdTransaksi());
        tableDetail.setItems(FXCollections.observableArrayList(details));
        
        // Total
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        Label lblTotalText = new Label("TOTAL:");
        lblTotalText.setFont(Font.font("System", FontWeight.BOLD, 16));
        Label lblTotalValue = new Label(currencyFormat.format(transaksi.getTotal()));
        lblTotalValue.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblTotalValue.setStyle("-fx-text-fill: #27ae60;");
        totalBox.getChildren().addAll(lblTotalText, lblTotalValue);
        
        content.getChildren().addAll(infoGrid, lblDetail, tableDetail, totalBox);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(650, 550);
        dialog.showAndWait();
    }
    
    private Label createInfoLabel(String text, boolean isBold) {
        Label label = new Label(text);
        if (isBold) {
            label.setFont(Font.font("System", FontWeight.BOLD, 13));
            label.setStyle("-fx-text-fill: #7f8c8d;");
        } else {
            label.setFont(Font.font("System", FontWeight.NORMAL, 13));
            label.setStyle("-fx-text-fill: #2c3e50;");
        }
        return label;
    }
    
    private void exportToPDF() {
        showAlert("Info", "Fitur export PDF akan segera hadir!", Alert.AlertType.INFORMATION);
        // TODO: Implement PDF export functionality
    }
    
    private void exportToExcel() {
        showAlert("Info", "Fitur export Excel akan segera hadir!", Alert.AlertType.INFORMATION);
        // TODO: Implement Excel export functionality
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}