package view;

import dao.UkuranDAO;
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
import model.Ukuran;

import java.util.List;
import javafx.scene.Scene;

public class UkuranManagementPanel extends VBox {
    private UkuranDAO ukuranDAO;
    private TableView<Ukuran> tableView;
    private ObservableList<Ukuran> ukuranList;
    private TextField txtSearch;

    public UkuranManagementPanel() {
        this.ukuranDAO = new UkuranDAO();
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
        
        Label title = new Label("Manajemen Ukuran");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Kelola ukuran kaos yang tersedia (S, M, L, XL, XXL)");
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
        txtSearch.setPromptText("Cari ukuran...");
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
        btnDelete.setOnAction(e -> deleteUkuran());
        
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

    private TableView<Ukuran> createTable() {
        TableView<Ukuran> table = new TableView<>();
        table.setPrefHeight(550);
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-table-cell-border-color: transparent; " +
            "-fx-padding: 15;"
        );

        // ID Column (hidden)
        TableColumn<Ukuran, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getIdUkuran())
        );
        colId.setVisible(false);

        // Nama Ukuran Column with badge style
        TableColumn<Ukuran, String> colNama = new TableColumn<>("UKURAN");
        colNama.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaUkuran())
        );
        colNama.setPrefWidth(200);
        colNama.setCellFactory(col -> new TableCell<Ukuran, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Keterangan Column
        TableColumn<Ukuran, String> colKet = new TableColumn<>("KETERANGAN");
        colKet.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getKeterangan())
        );
        colKet.setPrefWidth(700);
        colKet.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-size: 13px;");
        
        // Custom cell factory for keterangan with italic style
        colKet.setCellFactory(col -> new TableCell<Ukuran, String>() {
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
        List<Ukuran> ukuranListData = ukuranDAO.getAllUkuran(keyword);
        this.ukuranList = FXCollections.observableArrayList(ukuranListData);
        tableView.setItems(this.ukuranList);
    }

    private void showAddDialog() {
        UkuranFormDialog dialog = new UkuranFormDialog(null);
        boolean success = dialog.showAndWait();
        if (success) {
            loadData("");
        }
    }

    private void showEditDialog() {
        Ukuran selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih ukuran yang akan diedit terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }
        UkuranFormDialog dialog = new UkuranFormDialog(selected);
        boolean success = dialog.showAndWait();
        if (success) {
            loadData("");
        }
    }

    private void deleteUkuran() {
        Ukuran selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih ukuran yang akan dihapus terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Ukuran");
        confirm.setContentText("Apakah Anda yakin ingin menghapus ukuran \"" + selected.getNamaUkuran() + "\"?\nData yang dihapus tidak dapat dikembalikan.");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (ukuranDAO.deleteUkuran(selected.getIdUkuran())) {
                    showModernAlert("✅ Berhasil", "Ukuran berhasil dihapus!", Alert.AlertType.INFORMATION);
                    loadData("");
                } else {
                    showModernAlert("❌ Error", "Gagal menghapus ukuran! Mungkin masih digunakan oleh varian produk.", Alert.AlertType.ERROR);
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
    public static class UkuranFormDialog {
        private Ukuran ukuran;
        private boolean success = false;

        public UkuranFormDialog(Ukuran ukuran) {
            this.ukuran = ukuran;
        }

        public boolean showAndWait() {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(ukuran == null ? "Tambah Ukuran Baru" : "Edit Data Ukuran");

            VBox root = new VBox(25);
            root.setPadding(new Insets(30));
            root.setStyle("-fx-background-color: #f8f9fa;");

            // Header
            VBox header = new VBox(8);
            Label titleLabel = new Label(ukuran == null ? "📏 Tambah Ukuran Baru" : "✏️ Edit Data Ukuran");
            titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
            titleLabel.setTextFill(Color.web("#2c3e50"));
            
            Label subtitleLabel = new Label("Lengkapi informasi ukuran kaos");
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
            Label namaLabel = new Label("Nama Ukuran");
            namaLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            namaLabel.setTextFill(Color.web("#2c3e50"));
            
            TextField txtNama = new TextField();
            txtNama.setPromptText("Contoh: S, M, L, XL, XXL, XXXL");
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
            txtKeterangan.setPromptText("Deskripsi ukuran atau ukuran dalam cm...");
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
            if (ukuran != null) {
                txtNama.setText(ukuran.getNamaUkuran());
                txtKeterangan.setText(ukuran.getKeterangan());
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

            Button btnSave = new Button(ukuran == null ? "➕ Tambah" : "💾 Simpan");
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
                    alert.setContentText("Nama ukuran wajib diisi!");
                    alert.showAndWait();
                    return;
                }

                Ukuran saveUkuran = ukuran != null ? ukuran : new Ukuran();
                saveUkuran.setNamaUkuran(nama);
                saveUkuran.setKeterangan(txtKeterangan.getText().trim());

                boolean result;
                UkuranDAO dao = new UkuranDAO();
                if (ukuran == null) {
                    result = dao.createUkuran(saveUkuran);
                } else {
                    result = dao.updateUkuran(saveUkuran);
                }

                if (result) {
                    success = true;
                    dialog.close();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("✅ Berhasil");
                    alert.setHeaderText(ukuran == null ? "Ukuran Ditambahkan" : "Ukuran Diperbarui");
                    alert.setContentText(ukuran == null ? 
                        "Ukuran baru berhasil ditambahkan ke sistem!" : 
                        "Data ukuran berhasil diperbarui!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("❌ Error");
                    alert.setHeaderText("Gagal Menyimpan");
                    alert.setContentText("Terjadi kesalahan saat menyimpan data ukuran!");
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