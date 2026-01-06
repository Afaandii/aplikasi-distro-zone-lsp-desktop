package view;

import dao.TransaksiDAO;
import model.Transaksi;
import model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

// Panel untuk laporan kasir
public class LaporanKasirPanel extends VBox {
    
    private User currentUser;
    private TransaksiDAO transaksiDAO;
    private TableView<Transaksi> tableTransaksi;
    private ObservableList<Transaksi> transaksiData;
    
    private DatePicker dpStart;
    private DatePicker dpEnd;
    private Label lblTotalTransaksi;
    private Label lblTotalPenjualan;
    
    public LaporanKasirPanel(User user) {
        this.currentUser = user;
        this.transaksiDAO = new TransaksiDAO();
        this.transaksiData = FXCollections.observableArrayList();
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #ecf0f1;");
        
        // Header
        Label lblTitle = new Label("Laporan Transaksi Saya");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        lblTitle.setTextFill(Color.web("#2c3e50"));
        
        Label lblSubtitle = new Label("Lihat riwayat transaksi yang Anda proses");
        lblSubtitle.setFont(Font.font("Segoe UI", 14));
        lblSubtitle.setTextFill(Color.web("#7f8c8d"));
        
        // Filter panel
        HBox filterBox = new HBox(15);
        filterBox.setPadding(new Insets(20));
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                          "-fx-border-color: #dcdde1; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Label lblPeriode = new Label("Periode:");
        lblPeriode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        dpStart = new DatePicker(LocalDate.now().minusMonths(1));
        dpStart.setPrefWidth(150);
        
        Label lblTo = new Label("s/d");
        
        dpEnd = new DatePicker(LocalDate.now());
        dpEnd.setPrefWidth(150);
        
        Button btnFilter = new Button("🔍 Filter");
        btnFilter.setPrefHeight(35);
        btnFilter.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnFilter.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                          "-fx-background-radius: 5; -fx-cursor: hand;");
        btnFilter.setOnAction(e -> loadData());
        
        filterBox.getChildren().addAll(lblPeriode, dpStart, lblTo, dpEnd, btnFilter);
        
        // Stats
        HBox statsBox = new HBox(20);
        
        VBox stat1 = createStatBox("Total Transaksi", "0", "#3498db");
        VBox stat2 = createStatBox("Total Penjualan", "Rp 0", "#2ecc71");
        
        lblTotalTransaksi = (Label) ((VBox) stat1.getChildren().get(1)).getChildren().get(0);
        lblTotalPenjualan = (Label) ((VBox) stat2.getChildren().get(1)).getChildren().get(0);
        
        HBox.setHgrow(stat1, Priority.ALWAYS);
        HBox.setHgrow(stat2, Priority.ALWAYS);
        
        statsBox.getChildren().addAll(stat1, stat2);
        
        // Table
        VBox tableContainer = new VBox();
        tableContainer.setPadding(new Insets(25));
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                               "-fx-border-color: #dcdde1; -fx-border-width: 1; -fx-border-radius: 10;");
        
        tableTransaksi = new TableView<>();
        tableTransaksi.setItems(transaksiData);
        
        TableColumn<Transaksi, String> colKode = new TableColumn<>("KODE TRANSAKSI");
        colKode.setCellValueFactory(new PropertyValueFactory<>("kodeTransaksi"));
        colKode.setPrefWidth(250);
        
        TableColumn<Transaksi, String> colCustomer = new TableColumn<>("CUSTOMER");
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("namaCustomer"));
        colCustomer.setPrefWidth(150);
        
        TableColumn<Transaksi, Long> colTotal = new TableColumn<>("TOTAL");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setPrefWidth(150);
        colTotal.setCellFactory(col -> new TableCell<Transaksi, Long>() {
            @Override
            protected void updateItem(Long total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(total));
                }
            }
        });
        
        TableColumn<Transaksi, String> colMetode = new TableColumn<>("METODE");
        colMetode.setCellValueFactory(new PropertyValueFactory<>("metodePembayaran"));
        colMetode.setPrefWidth(120);
        
        TableColumn<Transaksi, String> colStatus = new TableColumn<>("STATUS");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusTransaksi"));
        colStatus.setPrefWidth(120);
        
        tableTransaksi.getColumns().addAll(colKode, colCustomer, colTotal, colMetode, colStatus);
        
        VBox.setVgrow(tableTransaksi, Priority.ALWAYS);
        tableContainer.getChildren().add(tableTransaksi);
        
        getChildren().addAll(lblTitle, lblSubtitle, filterBox, statsBox, tableContainer);
    }
    
    private VBox createStatBox(String title, String value, String color) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-border-color: #dcdde1; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Segoe UI", 14));
        lblTitle.setTextFill(Color.web("#7f8c8d"));
        
        VBox valueBox = new VBox();
        Label lblValue = new Label(value);
        lblValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblValue.setTextFill(Color.web(color));
        valueBox.getChildren().add(lblValue);
        
        box.getChildren().addAll(lblTitle, valueBox);
        
        return box;
    }
    
    private void loadData() {
        transaksiData.clear();
        
        Date startDate = Date.valueOf(dpStart.getValue());
        Date endDate = Date.valueOf(dpEnd.getValue());
        
        List<Transaksi> transaksiList = transaksiDAO.getTransaksiByKasir(
            currentUser.getIdUser(), startDate, endDate
        );
        transaksiData.addAll(transaksiList);
        
        // Update stats
        int jumlah = transaksiDAO.getJumlahTransaksi(currentUser.getIdUser(), startDate, endDate);
        Long total = transaksiDAO.getTotalPenjualan(currentUser.getIdUser(), startDate, endDate);
        
        lblTotalTransaksi.setText(String.valueOf(jumlah));
        lblTotalPenjualan.setText(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(total));
    }
}