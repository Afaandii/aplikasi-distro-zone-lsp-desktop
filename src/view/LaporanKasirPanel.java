package view;

import com.itextpdf.io.font.PdfEncodings;
import dao.TransaksiDAO;
import dao.DetailTransaksiDAO;
import java.sql.Timestamp;
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

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import model.User;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.io.font.PdfEncodings;

public class LaporanKasirPanel extends VBox {
    
    private TransaksiDAO transaksiDAO;
    private DetailTransaksiDAO detailTransaksiDAO;
    private TableView<Transaksi> tableTransaksi;
    private DatePicker dpStartDate;
    private DatePicker dpEndDate;
    private ComboBox<String> cmbMetodePembayaran;
    private Label lblTotalPenjualan;
    private Label lblJumlahTransaksi;
    private Label lblRataRata;
    private NumberFormat currencyFormat;
    private User currentUser; // Tambahkan field ini

    public LaporanKasirPanel(User user) {
        this.currentUser = user; // Simpan user yang login
        this.transaksiDAO = new TransaksiDAO();
        this.detailTransaksiDAO = new DetailTransaksiDAO();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        initializeUI();
        loadDefaultData();
    }
    
    private void initializeUI() {
        this.setSpacing(20);
        this.setPadding(new Insets(25));
        this.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        Label headerLabel = new Label("Laporan Keuangan Kasir");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        headerLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        // Filter Section
        VBox filterSection = createFilterSection();
        
        // Statistics Cards
        HBox statsCards = createStatsCards();
        
        // Table Section
        VBox tableSection = createTableSection();
        
        this.getChildren().addAll(headerLabel, filterSection, statsCards, tableSection);
    }
    
    
    
    private VBox createFilterSection() {
        VBox filterBox = new VBox(15);
        filterBox.setPadding(new Insets(20));
        filterBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        Label filterLabel = new Label("Filter Periode");
        filterLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        filterLabel.setStyle("-fx-text-fill: #34495e;");
        
        HBox dateFilterBox = new HBox(15);
        dateFilterBox.setAlignment(Pos.CENTER_LEFT);
        
        // Start Date
        VBox startDateBox = new VBox(5);
        Label lblStart = new Label("Tanggal Mulai");
        lblStart.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        dpStartDate = new DatePicker(LocalDate.now().withDayOfMonth(1));
        dpStartDate.setPrefWidth(200);
        dpStartDate.setStyle("-fx-background-radius: 5;");
        startDateBox.getChildren().addAll(lblStart, dpStartDate);
        
        // End Date
        VBox endDateBox = new VBox(5);
        Label lblEnd = new Label("Tanggal Akhir");
        lblEnd.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        dpEndDate = new DatePicker(LocalDate.now());
        dpEndDate.setPrefWidth(200);
        dpEndDate.setStyle("-fx-background-radius: 5;");
        endDateBox.getChildren().addAll(lblEnd, dpEndDate);
        
        // Filter Button
        Button btnFilter = new Button("Tampilkan Laporan");
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
        
        // Metode Pembayaran Filter
        VBox metodeBox = new VBox(5);
        Label lblMetode = new Label("Metode Pembayaran");
        lblMetode.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        cmbMetodePembayaran = new ComboBox<>();
        cmbMetodePembayaran.getItems().addAll("Semua", "💵 Tunai", "🏦 Transfer Bank", "📱 QRIS");
        cmbMetodePembayaran.setValue("Semua");
        cmbMetodePembayaran.setPrefWidth(180);
        cmbMetodePembayaran.setStyle("-fx-background-radius: 5;");
        metodeBox.getChildren().addAll(lblMetode, cmbMetodePembayaran);
        
        cmbMetodePembayaran.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (dpStartDate.getValue() != null && dpEndDate.getValue() != null) {
                loadData(dpStartDate.getValue(), dpEndDate.getValue());
            }
        });
        
        // Export Button
        Button btnExport = new Button("Export PDF");
        btnExport.setPrefWidth(140);
        btnExport.setPrefHeight(35);
        btnExport.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-font-weight: bold; " +
                          "-fx-background-radius: 5; -fx-cursor: hand;");
        btnExport.setOnMouseEntered(e -> btnExport.setStyle(
            "-fx-background-color: #229954; -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-font-weight: bold; " +
            "-fx-background-radius: 5; -fx-cursor: hand;"));
        btnExport.setOnMouseExited(e -> btnExport.setStyle(
            "-fx-background-color: #27ae60; -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-font-weight: bold; " +
            "-fx-background-radius: 5; -fx-cursor: hand;"));
        btnExport.setOnAction(e -> exportToPDF());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        dateFilterBox.getChildren().addAll(startDateBox, endDateBox, btnFilter, metodeBox, spacer, btnExport);
        filterBox.getChildren().addAll(filterLabel, dateFilterBox);
        
        return filterBox;
    }
    
    private HBox createStatsCards() {
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER);
        
        // Total Penjualan Card
        VBox cardPenjualan = createStatCard("Total Penjualan", "Rp 0", "#3498db");
        lblTotalPenjualan = (Label) cardPenjualan.getChildren().get(1);
        
        // Jumlah Transaksi Card
        VBox cardTransaksi = createStatCard("Jumlah Transaksi", "0", "#e74c3c");
        lblJumlahTransaksi = (Label) cardTransaksi.getChildren().get(1);
        
        // Rata-rata Card
        VBox cardRataRata = createStatCard("Rata-rata per Transaksi", "Rp 0", "#27ae60");
        lblRataRata = (Label) cardRataRata.getChildren().get(1);
        
        statsBox.getChildren().addAll(cardPenjualan, cardTransaksi, cardRataRata);
        HBox.setHgrow(cardPenjualan, Priority.ALWAYS);
        HBox.setHgrow(cardTransaksi, Priority.ALWAYS);
        HBox.setHgrow(cardRataRata, Priority.ALWAYS);
        
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
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
    
    private VBox createTableSection() {
        VBox tableBox = new VBox(15);
        tableBox.setPadding(new Insets(20));
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        Label tableLabel = new Label("Rincian Transaksi");
        tableLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        tableLabel.setStyle("-fx-text-fill: #34495e;");
        
        tableTransaksi = new TableView<>();
        tableTransaksi.setStyle("-fx-background-color: transparent;");
        tableTransaksi.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
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
        colKode.setPrefWidth(180);
        
        TableColumn<Transaksi, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("namaCustomer"));
        colCustomer.setPrefWidth(150);
        
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
        colMetode.setPrefWidth(100);
        colMetode.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<Transaksi, Long> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(130);
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
        colStatus.setPrefWidth(100);
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
        
        tableTransaksi.getColumns().addAll(colNo, colKode, colCustomer, colTanggal, 
                                           colMetode, colTotal, colStatus, colAction);
        
        VBox.setVgrow(tableTransaksi, Priority.ALWAYS);
        tableBox.getChildren().addAll(tableLabel, tableTransaksi);
        
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
        Long idKasir = currentUser.getIdUser();
        Date sqlStartDate = Date.valueOf(startDate);
        Date sqlEndDate = Date.valueOf(endDate);
        String selectedMetode = cmbMetodePembayaran.getValue();
        
        // Load transaksi data
        List<Transaksi> transaksiList = transaksiDAO.getTransaksiByKasir(idKasir, sqlStartDate, sqlEndDate, selectedMetode);
        ObservableList<Transaksi> observableList = FXCollections.observableArrayList(transaksiList);
        tableTransaksi.setItems(observableList);
        
        // Update statistics
        Long totalPenjualan = transaksiDAO.getTotalPenjualan(idKasir, sqlStartDate, sqlEndDate, selectedMetode);
        int jumlahTransaksi = transaksiDAO.getJumlahTransaksi(idKasir, sqlStartDate, sqlEndDate, selectedMetode);
        Long rataRata = jumlahTransaksi > 0 ? totalPenjualan / jumlahTransaksi : 0;
        
        lblTotalPenjualan.setText(currencyFormat.format(totalPenjualan));
        lblJumlahTransaksi.setText(String.valueOf(jumlahTransaksi));
        lblRataRata.setText(currencyFormat.format(rataRata));
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
        
        infoGrid.add(createInfoLabel("Customer:", true), 0, 0);
        infoGrid.add(createInfoLabel(transaksi.getNamaCustomer(), false), 1, 0);
        infoGrid.add(createInfoLabel("Tanggal:", true), 0, 1);
        infoGrid.add(createInfoLabel(transaksi.getCreatedAt().toString(), false), 1, 1);
        infoGrid.add(createInfoLabel("Metode:", true), 0, 2);
        infoGrid.add(createInfoLabel(transaksi.getMetodePembayaran(), false), 1, 2);
        
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
        dialog.getDialogPane().setPrefSize(600, 500);
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
        try {
            // Ambil data terakhir yang ditampilkan
            LocalDate startDate = dpStartDate.getValue();
            LocalDate endDate = dpEndDate.getValue();
            String selectedMetode = cmbMetodePembayaran.getValue();

            if (startDate == null || endDate == null) {
                showAlert("Error", "Harap pilih tanggal mulai dan akhir terlebih dahulu.", Alert.AlertType.ERROR);
                return;
            }

            // Buka dialog save file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Simpan Laporan PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("Laporan_Kasir_" + startDate + "_to_" + endDate + ".pdf");

            Stage stage = (Stage) this.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file == null) {
                return; // User cancel
            }

            // Siapkan writer dan document
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Gunakan font standar Helvetica (aman untuk semua OS)
            PdfFont font = PdfFontFactory.createFont(); // Default Helvetica

            // Header utama
            Paragraph title = new Paragraph("LAPORAN KEUANGAN KASIR")
                    .setFont(font)
                    .setFontSize(20)
                    .setBold()
                    .setMarginBottom(5)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(title);

            // Informasi Periode & Metode
            Paragraph filterInfo = new Paragraph("Periode: " + startDate + " s/d " + endDate +
                    " | Metode Pembayaran: " + selectedMetode)
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginBottom(20)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
            document.add(filterInfo);

            // Statistik (dalam box)
            Table statsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                    .setWidth(500)
                    .setMarginBottom(25);

            // Header statistik
            statsTable.addHeaderCell(createStatsHeaderCell("Total Penjualan"))
                      .addHeaderCell(createStatsHeaderCell("Jumlah Transaksi"))
                      .addHeaderCell(createStatsHeaderCell("Rata-rata per Transaksi"));

            // Data statistik
            Long totalPenjualan = transaksiDAO.getTotalPenjualan(currentUser.getIdUser(), Date.valueOf(startDate), Date.valueOf(endDate), selectedMetode);
            int jumlahTransaksi = transaksiDAO.getJumlahTransaksi(currentUser.getIdUser(), Date.valueOf(startDate), Date.valueOf(endDate), selectedMetode);
            Long rataRata = jumlahTransaksi > 0 ? totalPenjualan / jumlahTransaksi : 0;

            statsTable.addCell(createStatsDataCell(currencyFormat.format(totalPenjualan), ColorConstants.BLUE))
                       .addCell(createStatsDataCell(String.valueOf(jumlahTransaksi), ColorConstants.RED))
                       .addCell(createStatsDataCell(currencyFormat.format(rataRata), ColorConstants.GREEN));

            document.add(statsTable);

            // Judul tabel
            Paragraph tableTitle = new Paragraph("Rincian Transaksi")
                    .setFont(font)
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10);
            document.add(tableTitle);

            // Kolom tabel (tanpa Customer dan Aksi)
            Table transactionTable = new Table(UnitValue.createPercentArray(new float[]{0.5f, 1.5f, 1.5f, 1f, 1f, 1f}))
                    .setWidth(500)
                    .setMarginBottom(20);

            // Header kolom
            transactionTable.addHeaderCell(createHeaderCell("No"))
                            .addHeaderCell(createHeaderCell("Kode Transaksi"))
                            .addHeaderCell(createHeaderCell("Tanggal"))
                            .addHeaderCell(createHeaderCell("Metode"))
                            .addHeaderCell(createHeaderCell("Total"))
                            .addHeaderCell(createHeaderCell("Status"));

            // Data transaksi
            List<Transaksi> transaksiList = transaksiDAO.getTransaksiByKasir(currentUser.getIdUser(), Date.valueOf(startDate), Date.valueOf(endDate), selectedMetode);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

            for (int i = 0; i < transaksiList.size(); i++) {
                Transaksi t = transaksiList.get(i);
                transactionTable.addCell(createDataCell(String.valueOf(i + 1), ColorConstants.BLACK));
                transactionTable.addCell(createDataCell(t.getKodeTransaksi(), ColorConstants.BLACK));
                transactionTable.addCell(createDataCell(t.getCreatedAt().toLocalDateTime().format(formatter), ColorConstants.BLACK));
                transactionTable.addCell(createDataCell(t.getMetodePembayaran(), ColorConstants.BLACK));
                transactionTable.addCell(createDataCell(currencyFormat.format(t.getTotal()), ColorConstants.BLACK));
                String status = t.getStatusTransaksi().toUpperCase();
                transactionTable.addCell(createDataCell(status,
                        "SELESAI".equals(status) ? ColorConstants.GREEN : ColorConstants.RED));
            }

            document.add(transactionTable);

            // Footer
            Paragraph footer = new Paragraph("Dibuat oleh: " + currentUser.getNama() + " | Tanggal: " + LocalDate.now())
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginTop(30)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT);
            document.add(footer);

            // Tutup dokumen
            document.close();

            // Konfirmasi
            showAlert("Berhasil", "Laporan berhasil disimpan ke:\n" + file.getAbsolutePath(), Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuat PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Helper method untuk header statistik
    private Cell createStatsHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(8)
                .setBorder(null);
    }

    // Helper method untuk data statistik
    private Cell createStatsDataCell(String text, com.itextpdf.kernel.colors.Color color) {
        return new Cell()
                .add(new Paragraph(text).setFontColor(color).setBold())
                .setPadding(8)
                .setBorder(null);
    }

    // Helper method untuk header tabel
    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(6)
                .setBorder(null);
    }

    // Helper method untuk data tabel
    private Cell createDataCell(String text, com.itextpdf.kernel.colors.Color color) {
        return new Cell()
                .add(new Paragraph(text).setFontColor(color))
                .setPadding(6)
                .setBorder(null);
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}