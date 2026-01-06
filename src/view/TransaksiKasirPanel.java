package view;

import dao.*;
import model.*;
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

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TransaksiKasirPanel extends VBox {
    
    private User currentUser;
    private ProdukDAO produkDAO;
    private VarianDAO varianDAO;
    private TransaksiDAO transaksiDAO;
    private DetailTransaksiDAO detailDAO;
    
    private TableView<CartItem> tableCart;
    private ObservableList<CartItem> cartData;
    
    private ComboBox<Produk> cbProduk;
    private ComboBox<Varian> cbVarian;
    private Spinner<Integer> spnJumlah;
    private Label lblTotal;
    private ComboBox<String> cbMetodePembayaran;
    
    private NumberFormat currencyFormat;
    
    public TransaksiKasirPanel(User user) {
        this.currentUser = user;
        this.produkDAO = new ProdukDAO();
        this.varianDAO = new VarianDAO();
        this.transaksiDAO = new TransaksiDAO();
        this.detailDAO = new DetailTransaksiDAO();
        this.cartData = FXCollections.observableArrayList();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        initComponents();
    }
    
    private void initComponents() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #ecf0f1;");
        
        // Header
        Label lblTitle = new Label("Transaksi Penjualan");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        lblTitle.setTextFill(Color.web("#2c3e50"));
        
        Label lblSubtitle = new Label("Buat transaksi penjualan baru");
        lblSubtitle.setFont(Font.font("Segoe UI", 14));
        lblSubtitle.setTextFill(Color.web("#7f8c8d"));
        
        // Main content
        HBox mainContent = new HBox(20);
        
        // Left side - Form
        VBox leftPanel = createFormPanel();
        
        // Right side - Cart
        VBox rightPanel = createCartPanel();
        
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(leftPanel, rightPanel);
        
        getChildren().addAll(lblTitle, lblSubtitle, mainContent);
    }
    
    private VBox createFormPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-border-color: #dcdde1; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Label lblFormTitle = new Label("Pilih Produk");
        lblFormTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblFormTitle.setTextFill(Color.web("#2c3e50"));
        
        // Produk
        Label lblProduk = new Label("Produk");
        lblProduk.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        cbProduk = new ComboBox<>();
        cbProduk.setPromptText("Pilih produk...");
        cbProduk.setPrefWidth(Double.MAX_VALUE);
        cbProduk.setPrefHeight(45);
        cbProduk.setStyle("-fx-font-size: 14px;");
        
        List<Produk> produkList = produkDAO.getAllProduk("");
        cbProduk.getItems().addAll(produkList);
        
        cbProduk.setOnAction(e -> loadVarian());
        
        // Varian
        Label lblVarian = new Label("Varian (Ukuran & Warna)");
        lblVarian.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        cbVarian = new ComboBox<>();
        cbVarian.setPromptText("Pilih varian...");
        cbVarian.setPrefWidth(Double.MAX_VALUE);
        cbVarian.setPrefHeight(45);
        cbVarian.setStyle("-fx-font-size: 14px;");
        cbVarian.setDisable(true);
        
        // Jumlah
        Label lblJumlah = new Label("Jumlah");
        lblJumlah.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        spnJumlah = new Spinner<>(1, 100, 1);
        spnJumlah.setPrefHeight(45);
        spnJumlah.setPrefWidth(Double.MAX_VALUE);
        spnJumlah.setEditable(true);
        spnJumlah.setStyle("-fx-font-size: 14px;");
        
        // Add to cart button
        Button btnAdd = new Button("➕ Tambah ke Keranjang");
        btnAdd.setPrefWidth(Double.MAX_VALUE);
        btnAdd.setPrefHeight(50);
        btnAdd.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        btnAdd.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                       "-fx-background-radius: 8; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> addToCart());
        
        panel.getChildren().addAll(
            lblFormTitle,
            lblProduk, cbProduk,
            lblVarian, cbVarian,
            lblJumlah, spnJumlah,
            btnAdd
        );
        
        return panel;
    }
    
    private VBox createCartPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(25));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-border-color: #dcdde1; -fx-border-width: 1; -fx-border-radius: 10;");
        
        Label lblCartTitle = new Label("Keranjang Belanja");
        lblCartTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblCartTitle.setTextFill(Color.web("#2c3e50"));
        
        // Table
        tableCart = new TableView<>();
        tableCart.setItems(cartData);
        tableCart.setStyle("-fx-font-family: 'Segoe UI';");
        
        TableColumn<CartItem, String> colProduk = new TableColumn<>("PRODUK");
        colProduk.setCellValueFactory(new PropertyValueFactory<>("namaProduk"));
        colProduk.setPrefWidth(200);
        
        TableColumn<CartItem, String> colVarian = new TableColumn<>("VARIAN");
        colVarian.setCellValueFactory(new PropertyValueFactory<>("varian"));
        colVarian.setPrefWidth(120);
        
        TableColumn<CartItem, Integer> colJumlah = new TableColumn<>("QTY");
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colJumlah.setPrefWidth(60);
        
        TableColumn<CartItem, String> colHarga = new TableColumn<>("HARGA");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("hargaFormatted"));
        colHarga.setPrefWidth(120);
        
        TableColumn<CartItem, String> colSubtotal = new TableColumn<>("SUBTOTAL");
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotalFormatted"));
        colSubtotal.setPrefWidth(120);
        
        TableColumn<CartItem, Void> colAction = new TableColumn<>("AKSI");
        colAction.setPrefWidth(80);
        colAction.setCellFactory(col -> new TableCell<CartItem, Void>() {
            private final Button btnDelete = new Button("🗑️");
            
            {
                btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                                  "-fx-font-size: 14px; -fx-cursor: hand;");
                btnDelete.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cartData.remove(item);
                    updateTotal();
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });
        
        tableCart.getColumns().addAll(colProduk, colVarian, colJumlah, colHarga, colSubtotal, colAction);
        
        VBox.setVgrow(tableCart, Priority.ALWAYS);
        
        // Total
        HBox totalBox = new HBox(10);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(15, 0, 0, 0));
        
        Label lblTotalLabel = new Label("TOTAL:");
        lblTotalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        
        lblTotal = new Label("Rp 0");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblTotal.setTextFill(Color.web("#2ecc71"));
        
        totalBox.getChildren().addAll(lblTotalLabel, lblTotal);
        
        // Payment method
        Label lblMetode = new Label("Metode Pembayaran");
        lblMetode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        cbMetodePembayaran = new ComboBox<>();
        cbMetodePembayaran.getItems().addAll("Tunai", "QRIS", "Transfer Bank");
        cbMetodePembayaran.setValue("Tunai");
        cbMetodePembayaran.setPrefWidth(Double.MAX_VALUE);
        cbMetodePembayaran.setPrefHeight(45);
        cbMetodePembayaran.setStyle("-fx-font-size: 14px;");
        
        // Buttons
        HBox buttonBox = new HBox(10);
        
        Button btnClear = new Button("🗑️ Bersihkan");
        btnClear.setPrefHeight(50);
        btnClear.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnClear.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                         "-fx-background-radius: 8; -fx-cursor: hand;");
        btnClear.setOnAction(e -> clearCart());
        HBox.setHgrow(btnClear, Priority.ALWAYS);
        btnClear.setMaxWidth(Double.MAX_VALUE);
        
        Button btnProcess = new Button("💳 Proses Transaksi");
        btnProcess.setPrefHeight(50);
        btnProcess.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnProcess.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                           "-fx-background-radius: 8; -fx-cursor: hand;");
        btnProcess.setOnAction(e -> processTransaction());
        HBox.setHgrow(btnProcess, Priority.ALWAYS);
        btnProcess.setMaxWidth(Double.MAX_VALUE);
        
        buttonBox.getChildren().addAll(btnClear, btnProcess);
        
        panel.getChildren().addAll(
            lblCartTitle,
            tableCart,
            totalBox,
            lblMetode,
            cbMetodePembayaran,
            buttonBox
        );
        
        return panel;
    }
    
    private void loadVarian() {
        cbVarian.getItems().clear();
        cbVarian.setDisable(true);
        
        Produk selected = cbProduk.getValue();
        if (selected != null) {
            List<Varian> varianList = varianDAO.getVarianByProduk(selected.getIdProduk());
            if (!varianList.isEmpty()) {
                cbVarian.getItems().addAll(varianList);
                cbVarian.setDisable(false);
            }
        }
    }
    
    private void addToCart() {
        if (cbProduk.getValue() == null || cbVarian.getValue() == null) {
            showAlert("Validasi", "Pilih produk dan varian terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }
        
        Produk produk = cbProduk.getValue();
        Varian varian = cbVarian.getValue();
        int jumlah = spnJumlah.getValue();
        
        // Check stock
        if (varian.getStokKaos() < jumlah) {
            showAlert("Stok Tidak Cukup", 
                     "Stok tersedia: " + varian.getStokKaos() + " item", 
                     Alert.AlertType.ERROR);
            return;
        }
        
        CartItem item = new CartItem(
            produk.getIdProduk(),
            produk.getNamaKaos(),
            varian.getNamaUkuran() + " - " + varian.getNamaWarna(),
            varian.getIdUkuran(),
            varian.getIdWarna(),
            jumlah,
            produk.getHargaJual()
        );
        
        cartData.add(item);
        updateTotal();
        
        // Reset form
        cbProduk.setValue(null);
        cbVarian.getItems().clear();
        cbVarian.setDisable(true);
        spnJumlah.getValueFactory().setValue(1);
    }
    
    private void updateTotal() {
        long total = 0;
        for (CartItem item : cartData) {
            total += item.getSubtotal();
        }
        lblTotal.setText(currencyFormat.format(total));
    }
    
    private void clearCart() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi");
        alert.setHeaderText(null);
        alert.setContentText("Bersihkan semua item di keranjang?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            cartData.clear();
            updateTotal();
        }
    }
    
    private void processTransaction() {
        if (cartData.isEmpty()) {
            showAlert("Keranjang Kosong", "Tambahkan produk ke keranjang terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            // Create transaction
            String kodeTransaksi = "TRX-" + UUID.randomUUID().toString();
            long total = 0;
            for (CartItem item : cartData) {
                total += item.getSubtotal();
            }
            
            Transaksi transaksi = new Transaksi();
            transaksi.setIdCustomer(3L); // Default customer (walk-in)
            transaksi.setIdKasir(currentUser.getIdUser());
            transaksi.setKodeTransaksi(kodeTransaksi);
            transaksi.setTotal(total);
            transaksi.setMetodePembayaran(cbMetodePembayaran.getValue());
            transaksi.setStatusTransaksi("selesai");
            
            Long idTransaksi = transaksiDAO.createTransaksi(transaksi);
            
            if (idTransaksi != null) {
                // Add details
                for (CartItem item : cartData) {
                    DetailTransaksi detail = new DetailTransaksi();
                    detail.setIdTransaksi(idTransaksi);
                    detail.setIdProduk(item.getIdProduk());
                    detail.setJumlah((long) item.getJumlah());
                    detail.setHargaSatuan(item.getHarga());
                    detail.setSubtotal(item.getSubtotal());
                    
                    detailDAO.createDetailTransaksi(detail);
                    
                    // Update stock
//                    varianDAO.updateStok(item.getIdProduk(), item.getIdUkuran(), 
//                                        item.getIdWarna(), item.getJumlah());
                }
                
                showAlert("Sukses", 
                         "Transaksi berhasil!\nKode: " + kodeTransaksi + "\nTotal: " + currencyFormat.format(total),
                         Alert.AlertType.INFORMATION);
                
                clearCart();
            } else {
                showAlert("Error", "Gagal menyimpan transaksi!", Alert.AlertType.ERROR);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Terjadi kesalahan: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for cart item
    public static class CartItem {
        private Long idProduk;
        private String namaProduk;
        private String varian;
        private Long idUkuran;
        private Long idWarna;
        private int jumlah;
        private Long harga;
        
        public CartItem(Long idProduk, String namaProduk, String varian, 
                       Long idUkuran, Long idWarna, int jumlah, Long harga) {
            this.idProduk = idProduk;
            this.namaProduk = namaProduk;
            this.varian = varian;
            this.idUkuran = idUkuran;
            this.idWarna = idWarna;
            this.jumlah = jumlah;
            this.harga = harga;
        }
        
        public Long getIdProduk() { return idProduk; }
        public String getNamaProduk() { return namaProduk; }
        public String getVarian() { return varian; }
        public Long getIdUkuran() { return idUkuran; }
        public Long getIdWarna() { return idWarna; }
        public int getJumlah() { return jumlah; }
        public Long getHarga() { return harga; }
        
        public Long getSubtotal() {
            return harga * jumlah;
        }
        
        public String getHargaFormatted() {
            return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(harga);
        }
        
        public String getSubtotalFormatted() {
            return NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(getSubtotal());
        }
    }
}