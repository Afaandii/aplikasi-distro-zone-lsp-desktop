package view;

import dao.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Scene;

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
        setPadding(new Insets(0));
        setSpacing(25);
        setStyle("-fx-background-color: transparent;");

        VBox headerSection = createHeaderSection();
        HBox actionBar = createActionBar();
        VBox tableContainer = createTableContainer();

        getChildren().addAll(headerSection, actionBar, tableContainer);
    }

    private VBox createHeaderSection() {
        VBox headerBox = new VBox(8);
        Label title = new Label("Manajemen Varian Produk");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Kelola kombinasi ukuran, warna, dan stok untuk setiap produk");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#7f8c8d"));
        
        headerBox.getChildren().addAll(title, subtitle);
        return headerBox;
    }

    private HBox createActionBar() {
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(20));
        actionBar.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3);");

        HBox searchBox = createSearchBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttonBox = createActionButtons();

        actionBar.getChildren().addAll(searchBox, spacer, buttonBox);
        return actionBar;
    }

    private HBox createSearchBox() {
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(10, 15, 10, 15));
        searchBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-color: #e1e8ed; -fx-border-width: 1; -fx-border-radius: 10;");
        searchBox.setPrefWidth(350);

        Label searchIcon = new Label("🔍");
        searchIcon.setFont(Font.font(18));

        txtSearch = new TextField();
        txtSearch.setPromptText("Cari produk, ukuran, warna...");
        txtSearch.setFont(Font.font("Segoe UI", 13));
        txtSearch.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-prompt-text-fill: #95a5a6;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.textProperty().addListener((obs, old, newVal) -> loadData(newVal));

        txtSearch.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 10;");
            } else {
                searchBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-color: #e1e8ed; -fx-border-width: 1; -fx-border-radius: 10;");
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
        btnDelete.setOnAction(e -> deleteVarian());

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
        btn.setStyle("-fx-background-color: " + normalColor + "; -fx-background-radius: 8; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + hoverColor + "; -fx-background-radius: 8; -fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + normalColor + "; -fx-background-radius: 8; -fx-cursor: hand;"));

        return btn;
    }

    private VBox createTableContainer() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 3);");

        tableView = createTable();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        HBox tipBox = new HBox(10);
        tipBox.setAlignment(Pos.CENTER_LEFT);
        tipBox.setPadding(new Insets(10, 15, 10, 15));
        tipBox.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 8;");
        
        Label tipIcon = new Label("💡");
        tipIcon.setFont(Font.font(16));
        
        Label tipText = new Label("Tip: Varian menentukan ukuran, warna, dan stok untuk setiap produk");
        tipText.setFont(Font.font("Segoe UI", 12));
        tipText.setTextFill(Color.web("#1565c0"));
        tipText.setStyle("-fx-font-style: italic;");
        
        tipBox.getChildren().addAll(tipIcon, tipText);
        container.getChildren().addAll(tableView, tipBox);
        return container;
    }

    private TableView<Varian> createTable() {
        TableView<Varian> table = new TableView<>();
        table.setPrefHeight(480);
        table.setStyle("-fx-background-color: transparent; -fx-table-cell-border-color: transparent;");

        TableColumn<Varian, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getIdVarian()));
        colId.setVisible(false);

        TableColumn<Varian, String> colProduk = new TableColumn<>("PRODUK");
        colProduk.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaProduk()));
        colProduk.setPrefWidth(300);
        colProduk.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-size: 13px; -fx-font-weight: bold;");

        TableColumn<Varian, String> colUkuran = new TableColumn<>("UKURAN");
        colUkuran.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaUkuran()));
        colUkuran.setPrefWidth(120);
        colUkuran.setCellFactory(col -> new TableCell<Varian, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                    badge.setPadding(new Insets(5, 14, 5, 14));
                    badge.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-background-radius: 12;");
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Varian, String> colWarna = new TableColumn<>("WARNA");
        colWarna.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaWarna()));
        colWarna.setPrefWidth(150);
        colWarna.setCellFactory(col -> new TableCell<Varian, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Circle colorCircle = new Circle(10);
                    colorCircle.setStroke(Color.web("#e1e8ed"));
                    colorCircle.setStrokeWidth(1.5);
                    colorCircle.setFill(Color.web(getColorHex(item)));
                    Label nameLabel = new Label(item);
                    nameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                    box.getChildren().addAll(colorCircle, nameLabel);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        TableColumn<Varian, Long> colStok = new TableColumn<>("STOK");
        colStok.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getStokKaos()));
        colStok.setPrefWidth(130);
        colStok.setCellFactory(col -> new TableCell<Varian, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER);
                    Circle indicator = new Circle(5);
                    if (item == 0) indicator.setFill(Color.web("#e74c3c"));
                    else if (item <= 10) indicator.setFill(Color.web("#f39c12"));
                    else indicator.setFill(Color.web("#2ecc71"));
                    
                    Label stokLabel = new Label(String.valueOf(item));
                    stokLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                    if (item == 0) stokLabel.setTextFill(Color.web("#e74c3c"));
                    else if (item <= 10) stokLabel.setTextFill(Color.web("#f39c12"));
                    else stokLabel.setTextFill(Color.web("#2ecc71"));
                    
                    box.getChildren().addAll(indicator, stokLabel);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        table.getColumns().addAll(colId, colProduk, colUkuran, colWarna, colStok);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private String getColorHex(String colorName) {
        String name = colorName.toLowerCase();
        if (name.contains("merah") || name.contains("red")) return "#e74c3c";
        if (name.contains("biru") || name.contains("blue")) return "#3498db";
        if (name.contains("hijau") || name.contains("green")) return "#2ecc71";
        if (name.contains("kuning") || name.contains("yellow")) return "#f1c40f";
        if (name.contains("hitam") || name.contains("black")) return "#2c3e50";
        if (name.contains("putih") || name.contains("white")) return "#ecf0f1";
        if (name.contains("abu") || name.contains("gray")) return "#95a5a6";
        if (name.contains("pink")) return "#e91e63";
        if (name.contains("ungu") || name.contains("purple")) return "#9b59b6";
        if (name.contains("orange")) return "#e67e22";
        return "#95a5a6";
    }

    private void loadData(String keyword) {
        List<Varian> varianListData = varianDAO.getAllVarian();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String filter = keyword.toLowerCase();
            varianListData = varianListData.stream()
                .filter(v -> v.getNamaProduk().toLowerCase().contains(filter) ||
                             v.getNamaUkuran().toLowerCase().contains(filter) ||
                             v.getNamaWarna().toLowerCase().contains(filter) ||
                             String.valueOf(v.getStokKaos()).contains(filter))
                .collect(Collectors.toList());
        }
        this.varianList = FXCollections.observableArrayList(varianListData);
        tableView.setItems(this.varianList);
    }

    private void showAddDialog() {
        VarianFormDialog dialog = new VarianFormDialog(null);
        boolean success = dialog.showAndWait(produkDAO.getAllProduk(""), ukuranDAO.getAllUkuran(""), warnaDAO.getAllWarna(""));
        if (success) loadData("");
    }

    private void showEditDialog() {
        Varian selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih varian yang akan diedit!", Alert.AlertType.WARNING);
            return;
        }
        VarianFormDialog dialog = new VarianFormDialog(selected);
        boolean success = dialog.showAndWait(produkDAO.getAllProduk(""), ukuranDAO.getAllUkuran(""), warnaDAO.getAllWarna(""));
        if (success) loadData("");
    }

    private void deleteVarian() {
        Varian selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih varian yang akan dihapus!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Varian");
        confirm.setContentText("Hapus varian \"" + selected.getNamaProduk() + " - " + selected.getNamaUkuran() + " - " + selected.getNamaWarna() + "\"?");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (varianDAO.deleteVarian(selected.getIdVarian())) {
                    showModernAlert("✅ Berhasil", "Varian berhasil dihapus!", Alert.AlertType.INFORMATION);
                    loadData("");
                } else {
                    showModernAlert("❌ Error", "Gagal menghapus varian!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showModernAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class VarianFormDialog {
        private Varian varian;
        private boolean success = false;

        public VarianFormDialog(Varian varian) {
            this.varian = varian;
        }

        public boolean showAndWait(List<Produk> produkList, List<Ukuran> ukuranList, List<Warna> warnaList) {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(varian == null ? "Tambah Varian Baru" : "Edit Varian");

            VBox root = new VBox(25);
            root.setPadding(new Insets(30));
            root.setStyle("-fx-background-color: #f8f9fa;");

            VBox header = new VBox(8);
            Label titleLabel = new Label(varian == null ? "🎯 Tambah Varian Baru" : "✏️ Edit Varian");
            titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
            titleLabel.setTextFill(Color.web("#2c3e50"));
            
            Label subtitleLabel = new Label("Kombinasi produk, ukuran, dan warna");
            subtitleLabel.setFont(Font.font("Segoe UI", 13));
            subtitleLabel.setTextFill(Color.web("#7f8c8d"));
            
            header.getChildren().addAll(titleLabel, subtitleLabel);

            VBox formContainer = new VBox(15);
            formContainer.setPadding(new Insets(25));
            formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");

            ComboBox<Produk> cbProduk = new ComboBox<>();
            cbProduk.getItems().addAll(produkList);
            cbProduk.setPromptText("Pilih produk");
            cbProduk.setMaxWidth(Double.MAX_VALUE);
            styleComboBox(cbProduk);

            ComboBox<Ukuran> cbUkuran = new ComboBox<>();
            cbUkuran.getItems().addAll(ukuranList);
            cbUkuran.setPromptText("Pilih ukuran");
            cbUkuran.setMaxWidth(Double.MAX_VALUE);
            styleComboBox(cbUkuran);

            ComboBox<Warna> cbWarna = new ComboBox<>();
            cbWarna.getItems().addAll(warnaList);
            cbWarna.setPromptText("Pilih warna");
            cbWarna.setMaxWidth(Double.MAX_VALUE);
            styleComboBox(cbWarna);

            TextField txtStok = new TextField();
            txtStok.setPromptText("Contoh: 50");
            txtStok.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-color: #e1e8ed; -fx-border-width: 1; -fx-border-radius: 10; -fx-padding: 12 15;");

            if (varian != null) {
                cbProduk.setValue(findProdukById(produkList, varian.getIdProduk()));
                cbUkuran.setValue(findUkuranById(ukuranList, varian.getIdUkuran()));
                cbWarna.setValue(findWarnaById(warnaList, varian.getIdWarna()));
                txtStok.setText(String.valueOf(varian.getStokKaos()));
                cbProduk.setDisable(true);
            }

            formContainer.getChildren().addAll(
                createLabel("Produk"), cbProduk,
                createLabel("Ukuran"), cbUkuran,
                createLabel("Warna"), cbWarna,
                createLabel("Stok"), txtStok
            );

            HBox buttonBox = new HBox(12);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(15, 0, 0, 0));

            Button btnCancel = new Button("Batal");
            styleButton(btnCancel, "#95a5a6");
            btnCancel.setOnAction(e -> dialog.close());

            Button btnSave = new Button(varian == null ? "➕ Tambah" : "💾 Simpan");
            styleButton(btnSave, "#3498db");

            btnSave.setOnAction(e -> {
                try {
                    if (cbProduk.getValue() == null || cbUkuran.getValue() == null || cbWarna.getValue() == null || txtStok.getText().trim().isEmpty()) {
                        showAlert("⚠️ Perhatian", "Semua field wajib diisi!", Alert.AlertType.WARNING);
                        return;
                    }

                    Long stok = Long.parseLong(txtStok.getText().trim());
                    if (stok < 0) throw new NumberFormatException();

                    VarianDAO dao = new VarianDAO();
                    if (dao.isVarianExists(cbProduk.getValue().getIdProduk(), cbUkuran.getValue().getIdUkuran(), cbWarna.getValue().getIdWarna(), varian != null ? varian.getIdVarian() : null)) {
                        showAlert("⚠️ Duplikat", "Varian dengan kombinasi ini sudah ada!", Alert.AlertType.WARNING);
                        return;
                    }

                    Varian saveVarian = varian != null ? varian : new Varian();
                    saveVarian.setIdProduk(cbProduk.getValue().getIdProduk());
                    saveVarian.setIdUkuran(cbUkuran.getValue().getIdUkuran());
                    saveVarian.setIdWarna(cbWarna.getValue().getIdWarna());
                    saveVarian.setStokKaos(stok);

                    boolean result = varian == null ? dao.createVarian(saveVarian) : dao.updateVarian(saveVarian);

                    if (result) {
                        success = true;
                        dialog.close();
                        showAlert("✅ Berhasil", varian == null ? "Varian baru berhasil ditambahkan!" : "Data varian berhasil diperbarui!", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("❌ Error", "Gagal menyimpan varian!", Alert.AlertType.ERROR);
                    }
                } catch (NumberFormatException ex) {
                    showAlert("❌ Error", "Stok harus berupa angka positif!", Alert.AlertType.ERROR);
                }
            });

            buttonBox.getChildren().addAll(btnCancel, btnSave);
            root.getChildren().addAll(header, formContainer, buttonBox);

            Scene scene = new Scene(root, 520, 560);
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();
            return success;
        }

        private Label createLabel(String text) {
            Label label = new Label(text);
            label.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            label.setTextFill(Color.web("#2c3e50"));
            return label;
        }

        private void styleComboBox(ComboBox<?> cb) {
            cb.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-color: #e1e8ed; -fx-border-width: 1; -fx-border-radius: 10;");
        }

        private void styleButton(Button btn, String color) {
            btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            btn.setPadding(new Insets(12, 30, 12, 30));
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        }

        private void showAlert(String title, String message, Alert.AlertType type) {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
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