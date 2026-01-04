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
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Scene;
import java.lang.NumberFormatException;

public class VarianManagementPanel extends VBox {
    private VarianDAO varianDAO;
    private ProdukDAO produkDAO;
    private UkuranDAO ukuranDAO;
    private WarnaDAO warnaDAO;
    private TableView<Varian> tableView;
    private ObservableList<Varian> varianList;
    private TextField txtSearch;

    public VarianManagementPanel() {
        this.varianDAO = new VarianDAO();
        this.produkDAO = new ProdukDAO();
        this.ukuranDAO = new UkuranDAO();
        this.warnaDAO = new WarnaDAO();
        initializeUI();
        loadData("");
    }

    private void initializeUI() {
        setPadding(new Insets(30));
        setSpacing(20);
        setStyle("-fx-background-color: #ecf0f1;");

        // Header
        Label title = new Label("Manajemen Varian Produk");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        // Toolbar
        HBox toolBar = createToolBar();

        // Table
        tableView = createTable();

        // Info panel
        Label infoLabel = new Label("💡 Tip: Varian menentukan ukuran, warna, dan stok untuk setiap produk");
        infoLabel.setFont(Font.font("Segoe UI", 12));
        infoLabel.setTextFill(Color.web("#7f8c8d"));
        infoLabel.setStyle("-fx-font-style: italic;");

        VBox tableContainer = new VBox(10, tableView, infoLabel);
        tableContainer.setPadding(new Insets(0, 0, 0, 0));

        HBox headerBox = new HBox(title);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        VBox contentBox = new VBox(15, toolBar, tableContainer);
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
        txtSearch.setPromptText("Cari varian...");
        txtSearch.setPrefWidth(250);
        txtSearch.textProperty().addListener((obs, old, newVal) -> loadData(newVal));

        HBox searchBox = new HBox(10, searchIcon, txtSearch);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        Button btnAdd = createStyledButton("➕ Tambah Varian", "#2ecc71");
        btnAdd.setOnAction(e -> showAddDialog());

        Button btnEdit = createStyledButton("✏️ Edit", "#3498db");
        btnEdit.setOnAction(e -> showEditDialog());

        Button btnDelete = createStyledButton("🗑️ Hapus", "#e74c3c");
        btnDelete.setOnAction(e -> deleteVarian());

        Button btnRefresh = createStyledButton("🔄 Refresh", "#34495e");
        btnRefresh.setOnAction(e -> loadData(""));

        HBox buttonBox = new HBox(10, btnAdd, btnEdit, btnDelete, btnRefresh);
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

    private TableView<Varian> createTable() {
        TableView<Varian> table = new TableView<>();
        table.setPrefHeight(500);

        // Kolom
        TableColumn<Varian, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getIdVarian())
        );
        colId.setVisible(false);

        TableColumn<Varian, String> colProduk = new TableColumn<>("Produk");
        colProduk.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaProduk())
        );
        colProduk.setPrefWidth(400);

        TableColumn<Varian, String> colUkuran = new TableColumn<>("Ukuran");
        colUkuran.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaUkuran())
        );
        colUkuran.setPrefWidth(100);

        TableColumn<Varian, String> colWarna = new TableColumn<>("Warna");
        colWarna.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaWarna())
        );
        colWarna.setPrefWidth(100);

        TableColumn<Varian, Long> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getStokKaos())
        );
        colStok.setPrefWidth(100);

        table.getColumns().addAll(colId, colProduk, colUkuran, colWarna, colStok);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private void loadData(String keyword) {
        List<Varian> varianList = varianDAO.getAllVarian();
        
        // Filter berdasarkan keyword
        if (keyword != null && !keyword.trim().isEmpty()) {
            String filter = keyword.toLowerCase();
            varianList = varianList.stream()
                .filter(v -> v.getNamaProduk().toLowerCase().contains(filter) ||
                             v.getNamaUkuran().toLowerCase().contains(filter) ||
                             v.getNamaWarna().toLowerCase().contains(filter) ||
                             String.valueOf(v.getStokKaos()).contains(filter))
                .collect(Collectors.toList());
        }

        this.varianList = FXCollections.observableArrayList(varianList);
        tableView.setItems(this.varianList);
    }

    private void showAddDialog() {
        VarianFormDialog dialog = new VarianFormDialog(null);
        boolean success = dialog.showAndWait(
            produkDAO.getAllProduk(""),
            ukuranDAO.getAllUkuran(),
            warnaDAO.getAllWarna()
        );
        if (success) {
            loadData("");
        }
    }

    private void showEditDialog() {
        Varian selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pilih varian yang akan diedit!", Alert.AlertType.WARNING);
            return;
        }
        VarianFormDialog dialog = new VarianFormDialog(selected);
        boolean success = dialog.showAndWait(
            produkDAO.getAllProduk(""),
            ukuranDAO.getAllUkuran(),
            warnaDAO.getAllWarna()
        );
        if (success) {
            loadData("");
        }
    }

    private void deleteVarian() {
        Varian selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pilih varian yang akan dihapus!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText(null);
        confirm.setContentText("Apakah Anda yakin ingin menghapus varian ini?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (varianDAO.deleteVarian(selected.getIdVarian())) {
                    showAlert("Varian berhasil dihapus!", Alert.AlertType.INFORMATION);
                    loadData("");
                } else {
                    showAlert("Gagal menghapus varian!", Alert.AlertType.ERROR);
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

    // ✅ VarianFormDialog sebagai nested static class — satu file!
    public static class VarianFormDialog {
        private Varian varian;
        private boolean success = false;

        public VarianFormDialog(Varian varian) {
            this.varian = varian;
        }

        public boolean showAndWait(List<Produk> produkList, List<Ukuran> ukuranList, List<Warna> warnaList) {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(varian == null ? "Tambah Varian" : "Edit Varian");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(15);
            grid.setPadding(new Insets(20));

            int row = 0;
            Label title = new Label(varian == null ? "Tambah Varian Baru" : "Edit Varian");
            title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            title.setTextFill(Color.web("#34495e"));
            grid.add(title, 0, row++, 2, 1);

            grid.add(new Label("Produk:"), 0, row);
            ComboBox<Produk> cbProduk = new ComboBox<>();
            cbProduk.getItems().addAll(produkList);
            cbProduk.setPrefWidth(400);
            grid.add(cbProduk, 1, row++);

            grid.add(new Label("Ukuran:"), 0, row);
            ComboBox<Ukuran> cbUkuran = new ComboBox<>();
            cbUkuran.getItems().addAll(ukuranList);
            cbUkuran.setPrefWidth(400);
            grid.add(cbUkuran, 1, row++);

            grid.add(new Label("Warna:"), 0, row);
            ComboBox<Warna> cbWarna = new ComboBox<>();
            cbWarna.getItems().addAll(warnaList);
            cbWarna.setPrefWidth(400);
            grid.add(cbWarna, 1, row++);

            grid.add(new Label("Stok:"), 0, row);
            TextField txtStok = new TextField();
            txtStok.setPrefWidth(400);
            grid.add(txtStok, 1, row++);

            if (varian != null) {
                cbProduk.setValue(findProdukById(produkList, varian.getIdProduk()));
                cbUkuran.setValue(findUkuranById(ukuranList, varian.getIdUkuran()));
                cbWarna.setValue(findWarnaById(warnaList, varian.getIdWarna()));
                txtStok.setText(String.valueOf(varian.getStokKaos()));
                cbProduk.setDisable(true); // Tidak bisa ganti produk saat edit
            }

            Button btnCancel = new Button("Batal");
            Button btnSave = new Button(varian == null ? "Tambah" : "Simpan");

            HBox buttonBox = new HBox(10, btnCancel, btnSave);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            VBox root = new VBox(20, grid, buttonBox);
            root.setPadding(new Insets(20));
            Scene scene = new Scene(root);
            dialog.setScene(scene);

            btnCancel.setOnAction(e -> dialog.close());
            btnSave.setOnAction(e -> {
                try {
                    if (cbProduk.getValue() == null || cbUkuran.getValue() == null || cbWarna.getValue() == null) {
                        new Alert(Alert.AlertType.WARNING, "Semua field harus diisi!").showAndWait();
                        return;
                    }

                    Long stok = Long.parseLong(txtStok.getText().trim());
                    if (stok < 0) {
                        throw new NumberFormatException();
                    }

                    VarianDAO dao = new VarianDAO();
                    if (dao.isVarianExists(
                        cbProduk.getValue().getIdProduk(),
                        cbUkuran.getValue().getIdUkuran(),
                        cbWarna.getValue().getIdWarna(),
                        varian != null ? varian.getIdVarian() : null
                    )) {
                        new Alert(Alert.AlertType.WARNING, "Varian dengan kombinasi Produk, Ukuran, dan Warna ini sudah ada!").showAndWait();
                        return;
                    }

                    Varian saveVarian = varian != null ? varian : new Varian();
                    saveVarian.setIdProduk(cbProduk.getValue().getIdProduk());
                    saveVarian.setIdUkuran(cbUkuran.getValue().getIdUkuran());
                    saveVarian.setIdWarna(cbWarna.getValue().getIdWarna());
                    saveVarian.setStokKaos(stok);

                    boolean result;
                    if (varian == null) {
                        result = dao.createVarian(saveVarian);
                    } else {
                        result = dao.updateVarian(saveVarian);
                    }

                    if (result) {
                        success = true;
                        dialog.close();
                        new Alert(Alert.AlertType.INFORMATION, "Varian berhasil disimpan!").showAndWait();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Gagal menyimpan varian!").showAndWait();
                    }
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Stok harus berupa angka yang valid!").showAndWait();
                }
            });

            dialog.showAndWait();
            return success;
        }

        private Produk findProdukById(List<Produk> list, Long id) {
            return list.stream().filter(p -> p.getIdProduk().equals(id)).findFirst().orElse(null);
        }

        private Ukuran findUkuranById(List<Ukuran> list, Long id) {
            return list.stream().filter(u -> u.getIdUkuran().equals(id)).findFirst().orElse(null);
        }

        private Warna findWarnaById(List<Warna> list, Long id) {
            return list.stream().filter(w -> w.getIdWarna().equals(id)).findFirst().orElse(null);
        }
    }
}