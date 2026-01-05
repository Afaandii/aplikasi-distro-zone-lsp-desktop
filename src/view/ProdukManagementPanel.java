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
        setPadding(new Insets(0));
        setSpacing(25);
        setStyle("-fx-background-color: transparent;");

        // Header Section
        VBox headerSection = createHeaderSection();
        
        // Search and Action Bar
        HBox actionBar = createActionBar();
        
        // Table Container
        VBox tableContainer = createTableContainer();

        getChildren().addAll(headerSection, actionBar, tableContainer);
    }

    private VBox createHeaderSection() {
        VBox headerBox = new VBox(8);
        
        Label title = new Label("Manajemen Produk");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Kelola data produk dan stok Anda");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#7f8c8d"));
        
        headerBox.getChildren().addAll(title, subtitle);
        return headerBox;
    }

    private HBox createActionBar() {
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(20));
        actionBar.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3);"
        );

        // Search Box
        HBox searchBox = createSearchBox();
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action Buttons
        HBox buttonBox = createActionButtons();

        actionBar.getChildren().addAll(searchBox, spacer, buttonBox);
        return actionBar;
    }

    private HBox createSearchBox() {
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(10, 15, 10, 15));
        searchBox.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #e1e8ed; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10;"
        );
        searchBox.setPrefWidth(350);

        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font(18));

        txtSearch = new TextField();
        txtSearch.setPromptText("Cari produk...");
        txtSearch.setFont(Font.font("Segoe UI", 13));
        txtSearch.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-border-color: transparent; " +
            "-fx-prompt-text-fill: #95a5a6;"
        );
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.textProperty().addListener((obs, old, newVal) -> loadData(newVal));

        // Focus effect
        searchBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (txtSearch.isFocused()) {
                searchBox.setStyle(
                    "-fx-background-color: white; " +
                    "-fx-background-radius: 10; " +
                    "-fx-border-color: #3498db; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 10;"
                );
            } else {
                searchBox.setStyle(
                    "-fx-background-color: #f8f9fa; " +
                    "-fx-background-radius: 10; " +
                    "-fx-border-color: #e1e8ed; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 10;"
                );
            }
        });

        searchBox.getChildren().addAll(searchIcon, txtSearch);
        return searchBox;
    }

    private HBox createActionButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnAdd = createModernButton("➕ Tambah", "#2ecc71", "#27ae60");
        btnAdd.setOnAction(e -> showAddDialog());

        Button btnEdit = createModernButton("✏ Edit", "#3498db", "#2980b9");
        btnEdit.setOnAction(e -> showEditDialog());

        Button btnDelete = createModernButton("🗑 Hapus", "#e74c3c", "#c0392b");
        btnDelete.setOnAction(e -> deleteProduk());
        
        Button btnRefresh = createModernButton("🔄 Refresh", "#95a5a6", "#7f8c8d");
        btnRefresh.setOnAction(e -> loadData(""));

        buttonBox.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnRefresh);
        return buttonBox;
    }

    private Button createModernButton(String text, String normalColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setPadding(new Insets(10, 20, 10, 20));
        btn.setStyle(
            "-fx-background-color: " + normalColor + "; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + hoverColor + "; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-scale-x: 1.02; " +
                "-fx-scale-y: 1.02;"
            );
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + normalColor + "; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;"
            );
        });

        return btn;
    }

    private VBox createTableContainer() {
        VBox container = new VBox();
        container.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3);"
        );

        tableView = createTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        container.getChildren().add(tableView);
        return container;
    }

    private TableView<Produk> createTable() {
        TableView<Produk> table = new TableView<>();
        table.setPrefHeight(550);
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-table-cell-border-color: transparent; " +
            "-fx-padding: 15;"
        );

        // Kolom tetap seperti sebelumnya
        TableColumn<Produk, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getIdProduk())
        );
        colId.setVisible(false);

        TableColumn<Produk, String> colNama = new TableColumn<>("Nama Kaos");
        colNama.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaKaos())
        );
        colNama.setPrefWidth(250);
        styleColumn(colNama);

        TableColumn<Produk, String> colMerk = new TableColumn<>("Merk");
        colMerk.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaMerk())
        );
        colMerk.setPrefWidth(150);
        styleColumn(colMerk);

        TableColumn<Produk, String> colTipe = new TableColumn<>("Tipe");
        colTipe.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaTipe())
        );
        colTipe.setPrefWidth(150);
        styleColumn(colTipe);

        TableColumn<Produk, String> colHargaJual = new TableColumn<>("Harga Jual");
        colHargaJual.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(currencyFormat.format(cell.getValue().getHargaJual()))
        );
        colHargaJual.setPrefWidth(130);
        styleColumn(colHargaJual);

        TableColumn<Produk, String> colHargaPokok = new TableColumn<>("Harga Pokok");
        colHargaPokok.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(currencyFormat.format(cell.getValue().getHargaPokok()))
        );
        colHargaPokok.setPrefWidth(130);
        styleColumn(colHargaPokok);

        TableColumn<Produk, String> colDibuat = new TableColumn<>("Dibuat");
        colDibuat.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(dateFormat.format(cell.getValue().getCreatedAt()))
        );
        colDibuat.setPrefWidth(150);
        styleColumn(colDibuat);

        table.getColumns().addAll(colId, colNama, colMerk, colTipe, colHargaJual, colHargaPokok, colDibuat);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private void styleColumn(TableColumn<Produk, String> column) {
        column.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-size: 13px;");
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
            cbTipe.getItems().addAll(tipeList);
            grid.add(cbTipe, 1, row++);

            grid.add(new Label("Harga Jual:"), 0, row);
            TextField txtHargaJual = new TextField();
            grid.add(txtHargaJual, 1, row++);

            grid.add(new Label("Harga Pokok:"), 0, row);
            TextField txtHargaPokok = new TextField();
            grid.add(txtHargaPokok, 1, row++);

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

                    ProdukDAO dao = new ProdukDAO(); // ✅ Perbaiki: jangan pakai field yang tidak diinisialisasi
                    boolean result;
                    if (produk == null) {
                        result = dao.tambahProduk(p);
                    } else {
                        result = dao.updateProduk(p);
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