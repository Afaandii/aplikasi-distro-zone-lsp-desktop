package view;

import dao.TipeDAO;
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
import model.Tipe;

import java.util.List;
import javafx.scene.Scene;

public class TipeManagementPanel extends VBox {
    private TipeDAO tipeDAO;
    private TableView<Tipe> tableView;
    private ObservableList<Tipe> tipeList;
    private TextField txtSearch;

    public TipeManagementPanel() {
        this.tipeDAO = new TipeDAO();
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
        
        Label title = new Label("Manajemen Tipe Kaos");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Kelola jenis dan tipe kaos yang tersedia");
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
        txtSearch.setPromptText("Cari tipe kaos...");
        txtSearch.setFont(Font.font("Segoe UI", 13));
        txtSearch.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-border-color: transparent; " +
            "-fx-prompt-text-fill: #95a5a6;"
        );
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.textProperty().addListener((obs, old, newVal) -> loadData(newVal));

        // Focus effect
        txtSearch.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
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
        btnDelete.setOnAction(e -> deleteTipe());
        
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

    private TableView<Tipe> createTable() {
        TableView<Tipe> table = new TableView<>();
        table.setPrefHeight(550);
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-table-cell-border-color: transparent; " +
            "-fx-padding: 15;"
        );

        // ID Column (hidden)
        TableColumn<Tipe, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getIdTipe())
        );
        colId.setVisible(false);

        // Nama Tipe Column
        TableColumn<Tipe, String> colNama = new TableColumn<>("NAMA TIPE");
        colNama.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaTipe())
        );
        colNama.setPrefWidth(300);
        colNama.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Keterangan Column
        TableColumn<Tipe, String> colKet = new TableColumn<>("KETERANGAN");
        colKet.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getKeterangan())
        );
        colKet.setPrefWidth(600);
        colKet.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-size: 13px;");
        
        // Custom cell factory for keterangan with italic style
        colKet.setCellFactory(col -> new TableCell<Tipe, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                }
            }
        });

        table.getColumns().addAll(colId, colNama, colKet);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }

    private void loadData(String keyword) {
        List<Tipe> tipeListData = tipeDAO.getAllTipe(keyword);
        this.tipeList = FXCollections.observableArrayList(tipeListData);
        tableView.setItems(this.tipeList);
    }

    private void showAddDialog() {
        TipeFormDialog dialog = new TipeFormDialog(null);
        boolean success = dialog.showAndWait();
        if (success) {
            loadData("");
        }
    }

    private void showEditDialog() {
        Tipe selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih tipe yang akan diedit terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }
        TipeFormDialog dialog = new TipeFormDialog(selected);
        boolean success = dialog.showAndWait();
        if (success) {
            loadData("");
        }
    }

    private void deleteTipe() {
        Tipe selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih tipe yang akan dihapus terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Tipe Kaos");
        confirm.setContentText("Apakah Anda yakin ingin menghapus tipe \"" + selected.getNamaTipe() + "\"?\nData yang dihapus tidak dapat dikembalikan.");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (tipeDAO.deleteTipe(selected.getIdTipe())) {
                    showModernAlert("✅ Berhasil", "Tipe kaos berhasil dihapus!", Alert.AlertType.INFORMATION);
                    loadData("");
                } else {
                    showModernAlert("❌ Error", "Gagal menghapus tipe! Mungkin masih digunakan oleh produk.", Alert.AlertType.ERROR);
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

    // ================ FORM DIALOG ================
    public static class TipeFormDialog {
        private Tipe tipe;
        private boolean success = false;

        public TipeFormDialog(Tipe tipe) {
            this.tipe = tipe;
        }

        public boolean showAndWait() {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(tipe == null ? "Tambah Tipe Baru" : "Edit Data Tipe");

            VBox root = new VBox(25);
            root.setPadding(new Insets(30));
            root.setStyle("-fx-background-color: #f8f9fa;");

            // Header
            VBox header = new VBox(8);
            Label titleLabel = new Label(tipe == null ? "📋 Tambah Tipe Kaos Baru" : "✏️ Edit Data Tipe Kaos");
            titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
            titleLabel.setTextFill(Color.web("#2c3e50"));
            
            Label subtitleLabel = new Label("Lengkapi informasi tipe kaos");
            subtitleLabel.setFont(Font.font("Segoe UI", 13));
            subtitleLabel.setTextFill(Color.web("#7f8c8d"));
            
            header.getChildren().addAll(titleLabel, subtitleLabel);

            // Form Container
            VBox formContainer = new VBox(15);
            formContainer.setPadding(new Insets(25));
            formContainer.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
            );

            // Form Fields
            VBox namaBox = new VBox(8);
            Label namaLabel = new Label("Nama Tipe");
            namaLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            namaLabel.setTextFill(Color.web("#2c3e50"));
            
            TextField txtNama = new TextField();
            txtNama.setPromptText("Contoh: T-Shirt, Long Sleeve, Oversize, dll");
            txtNama.setFont(Font.font("Segoe UI", 13));
            txtNama.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #e1e8ed; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-padding: 12 15; " +
                "-fx-prompt-text-fill: #95a5a6;"
            );
            
            txtNama.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    txtNama.setStyle(
                        "-fx-background-color: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #3498db; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10; " +
                        "-fx-padding: 12 15; " +
                        "-fx-prompt-text-fill: #95a5a6;"
                    );
                } else {
                    txtNama.setStyle(
                        "-fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10; " +
                        "-fx-padding: 12 15; " +
                        "-fx-prompt-text-fill: #95a5a6;"
                    );
                }
            });
            
            namaBox.getChildren().addAll(namaLabel, txtNama);

            // Keterangan Field
            VBox ketBox = new VBox(8);
            Label ketLabel = new Label("Keterangan");
            ketLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            ketLabel.setTextFill(Color.web("#2c3e50"));
            
            TextArea txtKeterangan = new TextArea();
            txtKeterangan.setPromptText("Deskripsi atau informasi tambahan tentang tipe kaos...");
            txtKeterangan.setFont(Font.font("Segoe UI", 13));
            txtKeterangan.setPrefRowCount(3);
            txtKeterangan.setWrapText(true);
            txtKeterangan.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #e1e8ed; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-padding: 12 15; " +
                "-fx-prompt-text-fill: #95a5a6;"
            );
            
            txtKeterangan.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    txtKeterangan.setStyle(
                        "-fx-background-color: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #3498db; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10; " +
                        "-fx-padding: 12 15; " +
                        "-fx-prompt-text-fill: #95a5a6;"
                    );
                } else {
                    txtKeterangan.setStyle(
                        "-fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10; " +
                        "-fx-padding: 12 15; " +
                        "-fx-prompt-text-fill: #95a5a6;"
                    );
                }
            });
            
            ketBox.getChildren().addAll(ketLabel, txtKeterangan);

            // Load existing data if editing
            if (tipe != null) {
                txtNama.setText(tipe.getNamaTipe());
                txtKeterangan.setText(tipe.getKeterangan());
            }

            formContainer.getChildren().addAll(namaBox, ketBox);

            // Buttons
            HBox buttonBox = new HBox(12);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(15, 0, 0, 0));

            Button btnCancel = new Button("Batal");
            btnCancel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            btnCancel.setPadding(new Insets(12, 30, 12, 30));
            btnCancel.setStyle(
                "-fx-background-color: #95a5a6; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;"
            );
            btnCancel.setOnAction(e -> dialog.close());

            Button btnSave = new Button(tipe == null ? "➕ Tambah" : "💾 Simpan");
            btnSave.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            btnSave.setPadding(new Insets(12, 30, 12, 30));
            btnSave.setStyle(
                "-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;"
            );

            btnSave.setOnAction(e -> {
                String nama = txtNama.getText().trim();
                
                if (nama.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("⚠️ Perhatian");
                    alert.setHeaderText("Data Tidak Lengkap");
                    alert.setContentText("Nama tipe wajib diisi!");
                    alert.showAndWait();
                    return;
                }

                Tipe saveTipe = tipe != null ? tipe : new Tipe();
                saveTipe.setNamaTipe(nama);
                saveTipe.setKeterangan(txtKeterangan.getText().trim());

                boolean result;
                TipeDAO dao = new TipeDAO();
                if (tipe == null) {
                    result = dao.createTipe(saveTipe);
                } else {
                    result = dao.updateTipe(saveTipe);
                }

                if (result) {
                    success = true;
                    dialog.close();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("✅ Berhasil");
                    alert.setHeaderText(tipe == null ? "Tipe Ditambahkan" : "Tipe Diperbarui");
                    alert.setContentText(tipe == null ? 
                        "Tipe kaos baru berhasil ditambahkan ke sistem!" : 
                        "Data tipe kaos berhasil diperbarui!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("❌ Error");
                    alert.setHeaderText("Gagal Menyimpan");
                    alert.setContentText("Terjadi kesalahan saat menyimpan data tipe!");
                    alert.showAndWait();
                }
            });

            buttonBox.getChildren().addAll(btnCancel, btnSave);

            root.getChildren().addAll(header, formContainer, buttonBox);

            Scene scene = new Scene(root, 500, 450);
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();
            return success;
        }
    }
}