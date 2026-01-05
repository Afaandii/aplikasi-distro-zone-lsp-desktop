package view;

import dao.FotoProdukDAO;
import dao.ProdukDAO;
import dao.WarnaDAO;
import model.FotoProduk;
import model.Produk;
import model.Warna;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.concurrent.Task;

public class FotoProdukManagementPanel extends VBox {
    
    private FotoProdukDAO fotoProdukDAO;
    private ProdukDAO produkDAO;
    private WarnaDAO warnaDAO;
    
    private TableView<FotoProduk> tableView;
    private ObservableList<FotoProduk> fotoData;
    private TextField txtSearch;
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    
    public FotoProdukManagementPanel() {
        this.fotoProdukDAO = new FotoProdukDAO();
        this.produkDAO = new ProdukDAO();
        this.warnaDAO = new WarnaDAO();
        this.fotoData = FXCollections.observableArrayList();        
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
        
        Label title = new Label("Manajemen Foto Produk");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Kelola foto produk untuk setiap warna yang tersedia");
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
        txtSearch.setPromptText("Cari nama produk atau warna...");
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
        btnDelete.setOnAction(e -> deleteFoto());
        
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

    private TableView<FotoProduk> createTable() {
        TableView<FotoProduk> table = new TableView<>();
        table.setPrefHeight(550);
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-table-cell-border-color: transparent; " +
            "-fx-padding: 15;"
        );

        // ID Column (hidden)
        TableColumn<FotoProduk, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getIdFotoProduk())
        );
        colId.setVisible(false);

        // Produk Column
        TableColumn<FotoProduk, String> colProduk = new TableColumn<>("NAMA PRODUK");
        colProduk.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaProduk())
        );
        colProduk.setPrefWidth(350);
        styleColumn(colProduk);

        // Warna Column
        TableColumn<FotoProduk, String> colWarna = new TableColumn<>("WARNA");
        colWarna.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaWarna())
        );
        colWarna.setPrefWidth(150);
        styleColumn(colWarna);

        // URL Column
        TableColumn<FotoProduk, String> colUrl = new TableColumn<>("URL FOTO");
        colUrl.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getUrlFoto())
        );
        colUrl.setPrefWidth(400);
        styleColumn(colUrl);

        table.getColumns().addAll(colId, colProduk, colWarna, colUrl);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }

    private void styleColumn(TableColumn<FotoProduk, String> column) {
        column.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-size: 13px;");
    }

    private void loadData(String searchTerm) {
        List<FotoProduk> allFotos = fotoProdukDAO.getAllFotoProduk();
        List<FotoProduk> filtered = allFotos.stream()
            .filter(f -> {
                String namaProduk = f.getNamaProduk() != null ? f.getNamaProduk().toLowerCase() : "";
                String namaWarna = f.getNamaWarna() != null ? f.getNamaWarna().toLowerCase() : "";
                String search = searchTerm.toLowerCase();
                return namaProduk.contains(search) || namaWarna.contains(search);
            })
            .collect(Collectors.toList());

        fotoData = FXCollections.observableArrayList(filtered);
        tableView.setItems(fotoData);
    }

    private void showAddDialog() {
        FotoProdukFormDialog dialog = new FotoProdukFormDialog(null);
        boolean success = dialog.showAndWait();
        if (success) {
            loadData("");
        }
    }

    private void showEditDialog() {
        FotoProduk selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih foto yang akan diedit terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }
        FotoProdukFormDialog dialog = new FotoProdukFormDialog(selected);
        boolean success = dialog.showAndWait();
        if (success) {
            loadData("");
        }
    }

    private void deleteFoto() {
        FotoProduk selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih foto yang akan dihapus terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Foto Produk");
        confirm.setContentText("Apakah Anda yakin ingin menghapus foto produk \"" + selected.getNamaProduk() + " - " + selected.getNamaWarna() + "\"?\nData yang dihapus tidak dapat dikembalikan.");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (fotoProdukDAO.deleteFotoProduk(selected.getIdFotoProduk())) {
                    showModernAlert("✅ Berhasil", "Foto produk berhasil dihapus!", Alert.AlertType.INFORMATION);
                    loadData("");
                } else {
                    showModernAlert("❌ Error", "Gagal menghapus foto produk!", Alert.AlertType.ERROR);
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
    public static class FotoProdukFormDialog {
        private FotoProduk foto;
        private boolean success = false;
        private List<Produk> produkList;
        private List<Warna> warnaList;
        private File selectedPhotoFile;

        public FotoProdukFormDialog(FotoProduk foto) {
            this.foto = foto;
        }

        public boolean showAndWait() {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(foto == null ? "Tambah Foto Produk" : "Edit Foto Produk");

            VBox root = new VBox(25);
            root.setPadding(new Insets(30));
            root.setStyle("-fx-background-color: #f8f9fa;");

            // Header
            VBox header = new VBox(8);
            Label titleLabel = new Label(foto == null ? "Tambah Foto Produk Baru" : "Edit Foto Produk");
            titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
            titleLabel.setTextFill(Color.web("#2c3e50"));

            Label subtitleLabel = new Label("Lengkapi formulir di bawah ini");
            subtitleLabel.setFont(Font.font("Segoe UI", 13));
            subtitleLabel.setTextFill(Color.web("#7f8c8d"));

            header.getChildren().addAll(titleLabel, subtitleLabel);

            // Scrollable Form Container
            VBox formContent = new VBox(15);
            formContent.setPadding(new Insets(25));
            formContent.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);"
            );

            // 🔝 PRODUK DI ATAS
            VBox produkBox = new VBox(8);
            Label produkLabel = new Label("Produk");
            produkLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            produkLabel.setTextFill(Color.web("#2c3e50"));

            ComboBox<Produk> cbProduk = new ComboBox<>();
            ProdukDAO produkDAO = new ProdukDAO();
            produkList = produkDAO.getAllProduk("");
            cbProduk.getItems().addAll(produkList);
            cbProduk.setPromptText("Pilih produk");
            cbProduk.setPrefWidth(Double.MAX_VALUE);
            cbProduk.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #e1e8ed; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-padding: 10;"
            );

            cbProduk.setCellFactory(param -> new ListCell<Produk>() {
                @Override
                protected void updateItem(Produk item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNamaKaos());
                    }
                }
            });

            cbProduk.setButtonCell(new ListCell<Produk>() {
                @Override
                protected void updateItem(Produk item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNamaKaos());
                    }
                }
            });

            produkBox.getChildren().addAll(produkLabel, cbProduk);

            // 📄 WARNA
            VBox warnaBox = new VBox(8);
            Label warnaLabel = new Label("Warna");
            warnaLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            warnaLabel.setTextFill(Color.web("#2c3e50"));

            ComboBox<Warna> cbWarna = new ComboBox<>();
            WarnaDAO warnaDAO = new WarnaDAO();
            warnaList = warnaDAO.getAllWarna();
            cbWarna.getItems().addAll(warnaList);
            cbWarna.setPromptText("Pilih warna");
            cbWarna.setPrefWidth(Double.MAX_VALUE);
            cbWarna.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #e1e8ed; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-padding: 10;"
            );

            cbWarna.setCellFactory(param -> new ListCell<Warna>() {
                @Override
                protected void updateItem(Warna item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNamaWarna());
                    }
                }
            });

            cbWarna.setButtonCell(new ListCell<Warna>() {
                @Override
                protected void updateItem(Warna item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNamaWarna());
                    }
                }
            });

            warnaBox.getChildren().addAll(warnaLabel, cbWarna);

            // 🖼️ FOTO PRODUK
            VBox photoBox = new VBox(8);
            Label photoLabel = new Label("Foto Produk");
            photoLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            photoLabel.setTextFill(Color.web("#2c3e50"));

            ImageView preview = new ImageView();
            preview.setFitWidth(100);
            preview.setFitHeight(100);
            preview.setPreserveRatio(true);
            preview.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e1e8ed; -fx-border-width: 1; -fx-border-radius: 8;");

            Button btnUpload = new Button("📁 Pilih Foto");
            btnUpload.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
                );
                File selected = fileChooser.showOpenDialog(btnUpload.getScene().getWindow());
                if (selected != null) {
                    preview.setImage(new Image(selected.toURI().toString()));
                    this.selectedPhotoFile = selected;
                }
            });

            photoBox.getChildren().addAll(photoLabel, preview, btnUpload);

            // Load existing data if editing
            if (foto != null) {
                for (Produk p : cbProduk.getItems()) {
                    if (p.getIdProduk().equals(foto.getIdProduk())) {
                        cbProduk.setValue(p);
                        break;
                    }
                }
                for (Warna w : cbWarna.getItems()) {
                    if (w.getIdWarna().equals(foto.getIdWarna())) {
                        cbWarna.setValue(w);
                        break;
                    }
                }
            }

            // Tambahkan semua ke formContent
            formContent.getChildren().addAll(
                produkBox,
                warnaBox,
                photoBox
            );

            // Wrap formContent dalam ScrollPane
            ScrollPane scrollPane = new ScrollPane(formContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefViewportHeight(400);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

            // Buttons di bawah scroll
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

            Button btnSave = new Button(foto == null ? "➕ Tambah" : "💾 Simpan");
            btnSave.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            btnSave.setPadding(new Insets(12, 30, 12, 30));
            btnSave.setStyle(
                "-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;"
            );

            btnSave.setOnAction(e -> {
                if (cbProduk.getValue() == null || cbWarna.getValue() == null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("⚠️ Perhatian");
                    alert.setHeaderText("Data Tidak Lengkap");
                    alert.setContentText("Produk dan Warna wajib diisi!");
                    alert.showAndWait();
                    return;
                }

                FotoProduk saveFoto = foto != null ? foto : new FotoProduk();
                saveFoto.setIdProduk(cbProduk.getValue().getIdProduk());
                saveFoto.setIdWarna(cbWarna.getValue().getIdWarna());

                // ✅ Simpan foto
                FotoProdukDAO dao = new FotoProdukDAO();
                if (foto == null) {
                    if (dao.createFotoProduk(saveFoto, selectedPhotoFile)) {
                        success = true;
                        dialog.close();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("✅ Berhasil");
                        alert.setHeaderText("Foto Produk Ditambahkan");
                        alert.setContentText("Foto produk baru berhasil ditambahkan!");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("❌ Error");
                        alert.setHeaderText("Gagal Menambahkan");
                        alert.setContentText("Terjadi kesalahan saat menambahkan foto produk!");
                        alert.showAndWait();
                    }
                } else {
                    if (dao.updateFotoProduk(saveFoto, selectedPhotoFile)) {
                        success = true;
                        dialog.close();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("✅ Berhasil");
                        alert.setHeaderText("Data Diperbarui");
                        alert.setContentText("Data foto produk berhasil diperbarui!");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("❌ Error");
                        alert.setHeaderText("Gagal Memperbarui");
                        alert.setContentText("Terjadi kesalahan saat memperbarui data!");
                        alert.showAndWait();
                    }
                }
            });

            buttonBox.getChildren().addAll(btnCancel, btnSave);

            // Masukkan semua ke root
            root.getChildren().addAll(header, scrollPane, buttonBox);

            Scene scene = new Scene(root, 550, 700);
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();
            return success;
        }
    }

    // Async loader untuk foto
    private void loadImageAsync(String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageView.setImage(new Image(getClass().getResource("/resource/default.jpg").toExternalForm()));
            return;
        }

        // Cek cache dulu
        Image cachedImage = imageCache.get(imageUrl);
        if (cachedImage != null) {
            imageView.setImage(cachedImage);
            return;
        }

        // Load async
        Task<Image> task = new Task<>() {
            @Override
            protected Image call() throws Exception {
                try {
                    return new Image(imageUrl, 50, 50, true, true);
                } catch (Exception e) {
                    System.err.println("Gagal load gambar: " + imageUrl);
                    return new Image(getClass().getResource("/resource/default.jpg").toExternalForm());
                }
            }
        };

        task.setOnSucceeded(e -> {
            Image image = task.getValue();
            imageView.setImage(image);
            imageCache.put(imageUrl, image); // simpan ke cache
        });

        task.setOnFailed(e -> {
            imageView.setImage(new Image(getClass().getResource("/resource/default.jpg").toExternalForm()));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}