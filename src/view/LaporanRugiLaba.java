package view;

import dao.TransaksiDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.chart.*;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import javafx.collections.FXCollections;

public class LaporanRugiLaba extends ScrollPane {
    
    private DatePicker dpTanggalMulai;
    private DatePicker dpTanggalAkhir;
    private Button btnTampilkan;
    private Label lblTotalPenjualan;
    private Label lblLabaKotor;
    private Label lblPersentaseLabaKotor;
    private TransaksiDAO transaksiDAO;
    private NumberFormat currencyFormat;
    private LineChart<String, Number> lineChart;
    
    public LaporanRugiLaba() {
        transaksiDAO = new TransaksiDAO();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        currencyFormat.setMaximumFractionDigits(0);
        currencyFormat.setMinimumFractionDigits(0); 

        // Buat VBox untuk menampung semua komponen
        VBox contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(30));
        contentContainer.setStyle("-fx-background-color: #f5f5f5;");

        initializeComponents(contentContainer);
        setupLayout(contentContainer);

        // Set content ke ScrollPane
        this.setContent(contentContainer);
        this.setFitToWidth(true);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setStyle("-fx-background-color: #f5f5f5;");
    }
    
    private void initializeComponents(VBox container) {
        // Date pickers dengan tanggal default bulan ini
        dpTanggalMulai = new DatePicker(LocalDate.now().withDayOfMonth(1));
        dpTanggalAkhir = new DatePicker(LocalDate.now());
        
        dpTanggalMulai.setPromptText("Tanggal Mulai");
        dpTanggalAkhir.setPromptText("Tanggal Akhir");
        
        dpTanggalMulai.setMaxWidth(200);
        dpTanggalAkhir.setMaxWidth(200);
        
        // Button
        btnTampilkan = new Button("Tampilkan Laporan");
        btnTampilkan.setStyle(
            "-fx-background-color: #2196F3; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 5;"
        );
        
        btnTampilkan.setOnMouseEntered(e -> 
            btnTampilkan.setStyle(
                "-fx-background-color: #1976D2; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 20; " +
                "-fx-cursor: hand; " +
                "-fx-background-radius: 5;"
            )
        );
        
        btnTampilkan.setOnMouseExited(e -> 
            btnTampilkan.setStyle(
                "-fx-background-color: #2196F3; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 10 20; " +
                "-fx-cursor: hand; " +
                "-fx-background-radius: 5;"
            )
        );
        
        btnTampilkan.setOnAction(e -> loadLaporanData());
        
        // Labels untuk hasil
        lblTotalPenjualan = createResultLabel("Rp 0");
        lblLabaKotor = createResultLabel("Rp 0");
        lblPersentaseLabaKotor = createResultLabel("0%");
        
        // Initialize Chart
        initializeChart();
    }
    
    private void initializeChart() {
        // Line Chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Metrik");
        yAxis.setLabel("Nilai (Rp)");
        
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Grafik Analisis Keuangan");
        lineChart.setPrefHeight(400);
        lineChart.setCreateSymbols(true);
        lineChart.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
    }
    
    private Label createResultLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 32));
        label.setTextFill(Color.web("#2196F3"));
        return label;
    }
    
    private void setupLayout(VBox container) {
        // Header
        Label lblTitle = new Label("LAPORAN RUGI LABA");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web("#333333"));
        
        // Filter Section
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.getChildren().addAll(
            createLabel("Dari:"), dpTanggalMulai,
            createLabel("Sampai:"), dpTanggalAkhir,
            btnTampilkan
        );
        
        // Card untuk Total Penjualan
        VBox cardPenjualan = createCard(
            "Total Penjualan",
            lblTotalPenjualan,
            "#4CAF50"
        );
        
        // Card untuk Laba Kotor
        VBox cardLabaKotor = createCard(
            "Laba Kotor",
            lblLabaKotor,
            "#2196F3"
        );
        
        // Card untuk Persentase Laba
        VBox cardPersentase = createCard(
            "Margin Laba Kotor",
            lblPersentaseLabaKotor,
            "#FF9800"
        );
        
        // Grid untuk cards
        GridPane gridCards = new GridPane();
        gridCards.setHgap(20);
        gridCards.setVgap(20);
        gridCards.add(cardPenjualan, 0, 0);
        gridCards.add(cardLabaKotor, 1, 0);
        gridCards.add(cardPersentase, 2, 0);
        
        // Set column constraints untuk responsive
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33.33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.33);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33.33);
        gridCards.getColumnConstraints().addAll(col1, col2, col3);
        
        // Chart Section dengan size yang lebih besar
        VBox chartContainer = new VBox(lineChart);
        chartContainer.setPadding(new Insets(20, 0, 20, 0));
        VBox.setVgrow(lineChart, Priority.ALWAYS);
        
        // Info Section
        VBox infoBox = createInfoBox();
        
        // Tambahkan semua ke container
        container.getChildren().addAll(
            lblTitle,
            createSeparator(),
            filterBox,
            gridCards,
            chartContainer,
            infoBox
        );
    }
    
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.NORMAL, 14));
        return label;
    }
    
    private VBox createCard(String title, Label valueLabel, String accentColor) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        
        // Title dengan accent color
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        lblTitle.setTextFill(Color.web("#666666"));
        
        // Accent bar
        Region accentBar = new Region();
        accentBar.setPrefHeight(4);
        accentBar.setPrefWidth(60);
        accentBar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 2;");
        
        card.getChildren().addAll(lblTitle, accentBar, valueLabel);
        
        return card;
    }
    
    private VBox createInfoBox() {
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(20));
        infoBox.setStyle(
            "-fx-background-color: #E3F2FD; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #2196F3; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8;"
        );
        
        Label lblInfoTitle = new Label("ℹ Informasi Perhitungan");
        lblInfoTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblInfoTitle.setTextFill(Color.web("#1976D2"));
        
        Label lblInfo = new Label(
            """
            \u2022 Laba Kotor = Total Penjualan - Total Harga Pokok Penjualan
            \u2022 Margin Laba Kotor = (Laba Kotor / Total Penjualan) \u00d7 100%
            \u2022 Hanya menghitung transaksi dengan status 'selesai'""");
        lblInfo.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblInfo.setTextFill(Color.web("#424242"));
        lblInfo.setWrapText(true);
        
        infoBox.getChildren().addAll(lblInfoTitle, lblInfo);
        
        return infoBox;
    }
    
    private Region createSeparator() {
        Region separator = new Region();
        separator.setPrefHeight(2);
        separator.setStyle("-fx-background-color: #e0e0e0;");
        return separator;
    }
    
    private void loadLaporanData() {
        try {
            LocalDate startDate = dpTanggalMulai.getValue();
            LocalDate endDate = dpTanggalAkhir.getValue();
            
            if (startDate == null || endDate == null) {
                showAlert("Error", "Silakan pilih tanggal mulai dan tanggal akhir!");
                return;
            }
            
            if (startDate.isAfter(endDate)) {
                showAlert("Error", "Tanggal mulai tidak boleh lebih besar dari tanggal akhir!");
                return;
            }
            
            Date sqlStartDate = Date.valueOf(startDate);
            Date sqlEndDate = Date.valueOf(endDate);
            
            // Ambil data dari database
            Long totalPenjualan = transaksiDAO.getTotalPenjualan(null, sqlStartDate, sqlEndDate);
            Long labaKotor = transaksiDAO.getLabaRugi(sqlStartDate, sqlEndDate);
            
            // Update UI
            lblTotalPenjualan.setText(currencyFormat.format(totalPenjualan));
            lblLabaKotor.setText(currencyFormat.format(labaKotor));
            
            // Hitung persentase margin laba kotor
            double persentase = 0;
            if (totalPenjualan > 0) {
                persentase = ((double) labaKotor / totalPenjualan) * 100;
            }
            
            lblPersentaseLabaKotor.setText(String.format("%.2f%%", persentase));
            
            // Update warna laba kotor berdasarkan nilai
            if (labaKotor >= 0) {
                lblLabaKotor.setTextFill(Color.web("#4CAF50")); // Hijau untuk profit
            } else {
                lblLabaKotor.setTextFill(Color.web("#F44336")); // Merah untuk loss
            }
            
            // Update Chart
            updateChart(totalPenjualan, labaKotor);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Terjadi kesalahan saat memuat data: " + e.getMessage());
        }
    }
    
    private void updateChart(Long totalPenjualan, Long labaKotor) {
        lineChart.getData().clear();

        Long hpp = totalPenjualan - labaKotor;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Alur Keuangan");

        // Data points
        series.getData().add(new XYChart.Data<>("Awal", 0L));
        series.getData().add(new XYChart.Data<>("Total Penjualan", totalPenjualan));
        series.getData().add(new XYChart.Data<>("Harga Pokok Penjualan", hpp));
        series.getData().add(new XYChart.Data<>("Laba Kotor", labaKotor));

        lineChart.getData().add(series);

        // Styling
        if (!lineChart.getData().isEmpty()) {
            series.getNode().setStyle("-fx-stroke: #2196F3; -fx-stroke-width: 3px;");
        }

        // Set X-axis labels
        CategoryAxis xAxis = (CategoryAxis) lineChart.getXAxis();
        xAxis.setCategories(FXCollections.observableArrayList(
            "Awal", "Total Penjualan", "Harga Pokok Penjualan", "Laba Kotor"
        ));

        // Set Y-axis scale
        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
        yAxis.setUpperBound(Math.ceil((totalPenjualan.doubleValue() * 1.1) / 1000000) * 1000000);
        yAxis.setTickUnit(1000000); // 1 juta per tick
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}