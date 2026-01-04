package view;

import dao.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import javafx.scene.Scene;

public class ProdukManagementPanel extends VBox {
    private User currentUser;
    private ProdukDAO produkDAO;
    private MerkDAO merkDAO;
    private TipeDAO tipeDAO;
    private TableView<Produk> tableView;
    private ObservableList<Produk> produkList;
    private TextField txtSearch;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    public ProdukManagementPanel(User user) {
        this.currentUser = user;
        this.produkDAO = new ProdukDAO();
        this.merkDAO = new MerkDAO();
        this.tipeDAO = new TipeDAO();
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        this.dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        initializeUI();
        loadData("");
    }

    private void initializeUI() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #ecf0f1;");

        // Header
        Label title = new Label("Manajemen Produk");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        // Toolbar
        HBox toolBar = createToolBar();

        // Table
        tableView = createTable();

        HBox headerBox = new HBox(title);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        VBox contentBox = new VBox(15, toolBar, tableView);
        getChildren().addAll(headerBox, contentBox);
    }

    private HBox createToolBar() {
        HBox toolBar = new HBox(15);
        toolBar.setPadding(new Insets(15, 20, 15, 20));
        toolBar.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 0);");

        // Search
        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font(20));

        txtSearch = new TextField();
        txtSearch.setPromptText("Cari produk...");
        txtSearch.setPrefWidth(300);
        txtSearch.textProperty().addListener((obs, old, newVal) -> loadData(newVal));

        HBox searchBox = new HBox(10, searchIcon, txtSearch);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        Button btnAdd = createStyledButton("➕ Tambah", "#2ecc71");
        btnAdd.setOnAction(e -> showAddDialog());

        Button btnEdit = createStyledButton("✏️ Edit", "#3498db");
        btnEdit.setOnAction(e -> showEditDialog());

        Button btnDelete = createStyledButton("🗑️ Hapus", "#e74c3c");
        btnDelete.setOnAction(e -> deleteProduk());

        HBox buttonBox = new HBox(10, btnAdd, btnEdit, btnDelete);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        HBox.setHgrow(searchBox, Priority.ALWAYS);
        toolBar.getChildren().addAll(searchBox, buttonBox);
        return toolBar;
    }

    private Button createStyledButton(String text, String bgColor) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8 16; " +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + darken(bgColor) + "; -fx-text-fill: white; -fx-font-weight: bold;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-font-weight: bold;"));
        return btn;
    }

    private String darken(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        r = Math.max(0, (int)(r * 0.8));
        g = Math.max(0, (int)(g * 0.8));
        b = Math.max(0, (int)(b * 0.8));
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private TableView<Produk> createTable() {
        TableView<Produk> table = new TableView<>();
        table.setPrefHeight(500);

        // Kolom
        TableColumn<Produk, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getIdProduk())
        );
        colId.setVisible(false);

        TableColumn<Produk, String> colNama = new TableColumn<>("Nama Kaos");
        colNama.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaKaos())
        );

        TableColumn<Produk, String> colMerk = new TableColumn<>("Merk");
        colMerk.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaMerk())
        );

        TableColumn<Produk, String> colTipe = new TableColumn<>("Tipe");
        colTipe.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaTipe())
        );

        TableColumn<Produk, String> colHargaJual = new TableColumn<>("Harga Jual");
        colHargaJual.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(currencyFormat.format(cell.getValue().getHargaJual()))
        );

        TableColumn<Produk, String> colHargaPokok = new TableColumn<>("Harga Pokok");
        colHargaPokok.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(currencyFormat.format(cell.getValue().getHargaPokok()))
        );

        TableColumn<Produk, String> colDibuat = new TableColumn<>("Dibuat");
        colDibuat.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(dateFormat.format(cell.getValue().getCreatedAt()))
        );

        table.getColumns().addAll(colId, colNama, colMerk, colTipe, colHargaJual, colHargaPokok, colDibuat);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private void loadData(String keyword) {
        List<Produk> list = produkDAO.getAllProduk(keyword);
        this.produkList = FXCollections.observableArrayList(list);
        tableView.setItems(this.produkList);
    }

    private void showAddDialog() {
        ProdukFormDialog dialog = new ProdukFormDialog(null);
        boolean success = dialog.showAndWait(merkDAO.getAllMerk(), tipeDAO.getAllTipe());
        if (success) {
            loadData("");
        }
    }

    private void showEditDialog() {
        Produk selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pilih produk yang akan diedit!", Alert.AlertType.WARNING);
            return;
        }
        ProdukFormDialog dialog = new ProdukFormDialog(selected);
        boolean success = dialog.showAndWait(merkDAO.getAllMerk(), tipeDAO.getAllTipe());
        if (success) {
            loadData("");
        }
    }

    private void deleteProduk() {
        Produk selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pilih produk yang akan dihapus!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText(null);
        confirm.setContentText("Apakah Anda yakin ingin menghapus produk ini?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (produkDAO.deleteProduk(selected.getIdProduk())) {
                    showAlert("Produk berhasil dihapus!", Alert.AlertType.INFORMATION);
                    loadData("");
                } else {
                    showAlert("Gagal menghapus produk!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : 
                      type == Alert.AlertType.WARNING ? "Peringatan" : "Sukses");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ✅ ProdukFormDialog sebagai nested static class — satu file!
    public static class ProdukFormDialog {
        private Produk produk;
        private boolean success = false;
        private ProdukDAO produkDAO;

        public ProdukFormDialog(Produk produk) {
            this.produk = produk;
        }

        public boolean showAndWait(List<Merk> merkList, List<Tipe> tipeList) {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(produk == null ? "Tambah Produk" : "Edit Produk");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            int row = 0;
            grid.add(new Label("Nama Kaos:"), 0, row);
            TextField txtNama = new TextField();
            grid.add(txtNama, 1, row++);

            grid.add(new Label("Merk:"), 0, row);
            ComboBox<Merk> cbMerk = new ComboBox<>();
            cbMerk.getItems().addAll(merkList);
            grid.add(cbMerk, 1, row++);

            grid.add(new Label("Tipe:"), 0, row);
            ComboBox<Tipe> cbTipe = new ComboBox<>();
            cbTipe.getItems().addAll(tipeList); // DINAMIS — diambil dari database
            grid.add(cbTipe, 1, row++);

            grid.add(new Label("Harga Jual:"), 0, row);
            TextField txtHargaJual = new TextField();
            grid.add(txtHargaJual, 1, row++);

            grid.add(new Label("Harga Pokok:"), 0, row);
            TextField txtHargaPokok = new TextField();
            grid.add(txtHargaPokok, 1, row++);

            // 💡 HAPUS BERAT — karena tidak dipakai di tabel produk
            // grid.add(new Label("Berat (kg):"), 0, row);
            // TextField txtBerat = new TextField();
            // grid.add(txtBerat, 1, row++);

            grid.add(new Label("Deskripsi:"), 0, row);
            TextArea txtDeskripsi = new TextArea();
            txtDeskripsi.setPrefRowCount(3);
            grid.add(txtDeskripsi, 1, row++);

            grid.add(new Label("Spesifikasi:"), 0, row);
            TextArea txtSpesifikasi = new TextArea();
            txtSpesifikasi.setPrefRowCount(3);
            grid.add(txtSpesifikasi, 1, row++);

            if (produk != null) {
                txtNama.setText(produk.getNamaKaos());
                cbMerk.setValue(findMerkById(merkList, produk.getIdMerk()));
                cbTipe.setValue(findTipeById(tipeList, produk.getIdTipe()));
                txtHargaJual.setText(produk.getHargaJual().toString());
                txtHargaPokok.setText(produk.getHargaPokok().toString());
                txtDeskripsi.setText(produk.getDeskripsi());
                txtSpesifikasi.setText(produk.getSpesifikasi());
            }

            Button btnCancel = new Button("Batal");
            Button btnSave = new Button(produk == null ? "Tambah" : "Simpan");

            HBox buttonBox = new HBox(10, btnCancel, btnSave);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            VBox root = new VBox(15, grid, buttonBox);
            root.setPadding(new Insets(20));
            Scene scene = new Scene(root);
            dialog.setScene(scene);

            btnCancel.setOnAction(e -> dialog.close());
            btnSave.setOnAction(e -> {
                try {
                    Produk p = produk != null ? produk : new Produk();
                    p.setNamaKaos(txtNama.getText().trim());
                    p.setIdMerk(cbMerk.getValue().getIdMerk());
                    p.setIdTipe(cbTipe.getValue().getIdTipe());
                    p.setHargaJual(Long.parseLong(txtHargaJual.getText().trim()));
                    p.setHargaPokok(Long.parseLong(txtHargaPokok.getText().trim()));
                    p.setDeskripsi(txtDeskripsi.getText().trim());
                    p.setSpesifikasi(txtSpesifikasi.getText().trim());

                    boolean result;
                    if (produk == null) {
                        result = produkDAO.tambahProduk(p); 
                    } else {
                        result = produkDAO.updateProduk(p);
                    }

                    if (result) {
                        success = true;
                        dialog.close();
                        new Alert(Alert.AlertType.INFORMATION, "Produk berhasil disimpan!").showAndWait();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Gagal menyimpan produk!").showAndWait();
                    }
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Input tidak valid: " + ex.getMessage()).showAndWait();
                }
            });

            dialog.showAndWait();
            return success;
        }

        private Merk findMerkById(List<Merk> list, Long id) {
            return list.stream().filter(m -> m.getIdMerk().equals(id)).findFirst().orElse(null);
        }

        private Tipe findTipeById(List<Tipe> list, Long id) {
            return list.stream().filter(t -> t.getIdTipe().equals(id)).findFirst().orElse(null);
        }
    }
}