package view;

import dao.ProdukDAO;
import dao.TransaksiDAO;
import model.Produk;
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

// Panel untuk view produk (read-only) - Kasir
class ProdukViewKasirPanel extends VBox {
    
    private ProdukDAO produkDAO;
    private TableView<Produk> tableProduk;
    private ObservableList<Produk> produkData;
    private TextField txtSearch;
    
    public ProdukViewKasirPanel() {
        this.produkDAO = new ProdukDAO();
        this.produkData = FXCollections.observableArrayList();
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #ecf0f1;");
        
        // Header
        Label lblTitle = new Label("Daftar Produk");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        lblTitle.setTextFill(Color.web("#2c3e50"));
        
        Label lblSubtitle = new Label("Lihat informasi produk yang tersedia");
        lblSubtitle.setFont(Font.font("Segoe UI", 14));
        lblSubtitle.setTextFill(Color.web("#7f8c8d"));
        
        // Search
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(15, 20, 15, 20));
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                          "-fx-border-color: #dcdde1; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font(18));
        
        txtSearch = new TextField();
        txtSearch.setPromptText("Cari nama produk atau merk...");
        txtSearch.setPrefWidth(300);
        txtSearch.setPrefHeight(38);
        txtSearch.setStyle("-fx-font-size: 14px;");
        txtSearch.textProperty().addListener((obs, old, newVal) -> searchProduk(newVal));
        
        searchBox.getChildren().addAll(searchIcon, txtSearch);
        
        // Table
        VBox tableContainer = new VBox();
        tableContainer.setPadding(new Insets(25));
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                               "-fx-border-color: #dcdde1; -fx-border-width: 1; -fx-border-radius: 10;");
        
        tableProduk = new TableView<>();
        tableProduk.setItems(produkData);
        
        TableColumn<Produk, String> colNama = new TableColumn<>("NAMA PRODUK");
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaKaos"));
        colNama.setPrefWidth(300);
        
        TableColumn<Produk, String> colMerk = new TableColumn<>("MERK");
        colMerk.setCellValueFactory(new PropertyValueFactory<>("namaMerk"));
        colMerk.setPrefWidth(150);
        
        TableColumn<Produk, String> colTipe = new TableColumn<>("TIPE");
        colTipe.setCellValueFactory(new PropertyValueFactory<>("namaTipe"));
        colTipe.setPrefWidth(150);
        
        TableColumn<Produk, Long> colHarga = new TableColumn<>("HARGA");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaJual"));
        colHarga.setPrefWidth(150);
        colHarga.setCellFactory(col -> new TableCell<Produk, Long>() {
            @Override
            protected void updateItem(Long price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(price));
                }
            }
        });
        
        TableColumn<Produk, Integer> colStok = new TableColumn<>("STOK");
        colStok.setCellValueFactory(new PropertyValueFactory<>("totalStok"));
        colStok.setPrefWidth(100);
        
        tableProduk.getColumns().addAll(colNama, colMerk, colTipe, colHarga, colStok);
        
        VBox.setVgrow(tableProduk, Priority.ALWAYS);
        tableContainer.getChildren().add(tableProduk);
        
        getChildren().addAll(lblTitle, lblSubtitle, searchBox, tableContainer);
    }
    
    private void loadData() {
        produkData.clear();
        List<Produk> produkList = produkDAO.getAllProduk("");
        produkData.addAll(produkList);
    }
    
    private void searchProduk(String keyword) {
        produkData.clear();
        List<Produk> produkList = produkDAO.getAllProduk(keyword);
        produkData.addAll(produkList);
    }
}