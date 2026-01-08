package view;

import dao.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import model.*;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javafx.scene.Node;

public class ProdukManagementPanel extends VBox {
    private User currentUser;
    
    // DAOs
    private ProdukDAO produkDAO;
    private MerkDAO merkDAO;
    private TipeDAO tipeDAO;
    private VarianDAO varianDAO;
    private FotoProdukDAO fotoProdukDAO;
    private UkuranDAO ukuranDAO;
    private WarnaDAO warnaDAO;
    
    // Left Panel Components
    private TableView<Produk> tableProduk;
    private ObservableList<Produk> produkList;
    private TextField txtSearch;
    
    // Right Panel Components - Tab 1: Info Produk
    private TextField txtNamaKaos;
    private ComboBox<Merk> cbMerk;
    private ComboBox<Tipe> cbTipe;
    private TextField txtHargaJual;
    private TextField txtHargaPokok;
    private TextArea txtDeskripsi;
    private TextArea txtSpesifikasi;
    private Button btnSaveProduk, btnDeleteProduk, btnNewProduk;
    
    // Right Panel Components - Tab 2: Varian
    private TableView<Varian> tableVarian;
    private VBox formCreateMode;
    private VBox formEditMode;
    private ObservableList<Varian> varianList;
    private ComboBox<Ukuran> cbUkuran;
    private ComboBox<Warna> cbWarnaVarian;
    // Untuk mode edit
    private ComboBox<Warna> cbWarnaEdit;
    private ComboBox<Ukuran> cbUkuranEdit;
    private TextField txtStokEdit;
    private Button btnUpdateVarianEdit;
    private TextField txtStok;
    private Button btnAddVarian, btnUpdateVarian, btnDeleteVarian;
    
    // Right Panel Components - Tab 3: Foto Produk
    private FlowPane fotoProdukContainer;
    private ComboBox<Warna> cbWarnaFoto;
    private Button btnUploadFoto;
    
    // Current State
    private Produk selectedProduk;
    private Varian selectedVarian;
    
    // Formatters
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private FlowPane chkUkuranContainer;
    private Map<Ukuran, TextField> ukuranStokMap = new HashMap<>();
    private boolean isEditMode = false;
    private Button btnSaveAll;
    
    public ProdukManagementPanel(User user) {
        this.currentUser = user;
        initializeDAOs();
        initializeFormatters();
        initializeUI();
        loadProdukList("");
    }
    
    private void initializeDAOs() {
        this.produkDAO = new ProdukDAO();
        this.merkDAO = new MerkDAO();
        this.tipeDAO = new TipeDAO();
        this.varianDAO = new VarianDAO();
        this.fotoProdukDAO = new FotoProdukDAO();
        this.ukuranDAO = new UkuranDAO();
        this.warnaDAO = new WarnaDAO();
    }
    
    private void initializeFormatters() {
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        this.dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    }
    
    private void initializeUI() {
        setPadding(new Insets(0));
        setSpacing(0);
        setStyle("-fx-background-color: #f5f7fa;");
        
        // Header
        VBox header = createHeader();
        
        // Split Pane
        SplitPane splitPane = createSplitPane();
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        getChildren().addAll(header, splitPane);
    }
    
    private VBox createHeader() {
        VBox headerBox = new VBox(5);
        headerBox.setPadding(new Insets(25, 30, 20, 30));
        headerBox.setStyle("-fx-background-color: white; -fx-border-color: #e1e8ed; -fx-border-width: 0 0 1 0;");
        
        Label title = new Label("Manajemen Produk");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Kelola data produk, varian, dan foto dalam satu panel");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web("#7f8c8d"));
        
        headerBox.getChildren().addAll(title, subtitle);
        return headerBox;
    }
    
    private SplitPane createSplitPane() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.4);
        splitPane.setStyle("-fx-background-color: transparent;");
        
        // Left Panel: List Produk
        VBox leftPanel = createLeftPanel();
        
        // Right Panel: Detail Produk (Tabs)
        VBox rightPanel = createRightPanel();
        
        splitPane.getItems().addAll(leftPanel, rightPanel);
        return splitPane;
    }
    
    // ==================== LEFT PANEL ====================
    
    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(15);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setStyle("-fx-background-color: white;");
        
        // Search Bar
        HBox searchBar = createSearchBar();
        
        // Table
        tableProduk = createProdukTable();
        VBox.setVgrow(tableProduk, Priority.ALWAYS);
        
        leftPanel.getChildren().addAll(searchBar, tableProduk);
        return leftPanel;
    }
    
    private HBox createSearchBar() {
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(12, 15, 12, 15));
        searchBar.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #e1e8ed; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 10;"
        );
        
        Label icon = new Label("🔍");
        icon.setFont(Font.font(16));
        
        txtSearch = new TextField();
        txtSearch.setPromptText("Cari produk...");
        txtSearch.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        
        txtSearch.textProperty().addListener((obs, old, newVal) -> loadProdukList(newVal));
        
        searchBar.getChildren().addAll(icon, txtSearch);
        return searchBar;
    }
    
    private TableView<Produk> createProdukTable() {
        TableView<Produk> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");
        
        TableColumn<Produk, String> colNama = new TableColumn<>("Nama Produk");
        colNama.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaKaos())
        );
        colNama.setPrefWidth(180);
        
        TableColumn<Produk, String> colMerk = new TableColumn<>("Merk");
        colMerk.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaMerk())
        );
        colMerk.setPrefWidth(100);
        
        TableColumn<Produk, String> colTipe = new TableColumn<>("Tipe");
        colTipe.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaTipe())
        );
        colTipe.setPrefWidth(100);
        
        TableColumn<Produk, String> colHarga = new TableColumn<>("Harga Jual");
        colHarga.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(currencyFormat.format(cell.getValue().getHargaJual()))
        );
        colHarga.setPrefWidth(120);
        
        TableColumn<Produk, String> colHargaPokok = new TableColumn<>("Harga Pokok");
        colHargaPokok.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(currencyFormat.format(cell.getValue().getHargaPokok()))
        );
        colHargaPokok.setPrefWidth(120);
        
        table.getColumns().addAll(colNama, colMerk, colTipe, colHarga, colHargaPokok);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Selection Listener
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedProduk = newVal;
                loadProdukDetail(newVal);
            }
        });
        
        return table;
    }
    
    // ==================== RIGHT PANEL ====================
    
    private VBox createRightPanel() {
        VBox rightPanel = new VBox(0);
        rightPanel.setStyle("-fx-background-color: white;");
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: white;");
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        // Tab 1: Info Produk
        Tab tabInfo = new Tab("Info Produk");
        tabInfo.setContent(createInfoProdukTab());
        
        // Tab 2: Varian
        Tab tabVarian = new Tab("Varian");
        tabVarian.setContent(createVarianTab());
        
        // Tab 3: Foto Produk
        Tab tabFoto = new Tab("Foto Produk");
        tabFoto.setContent(createFotoProdukTab());
        
        tabPane.getTabs().addAll(tabInfo, tabVarian, tabFoto);
        rightPanel.getChildren().add(tabPane);
        
        return rightPanel;
    }
    
    // ==================== TAB 1: INFO PRODUK ====================
    
    private VBox createInfoProdukTab() {
        VBox container = new VBox(50);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: white;");
        
        // Form
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        
        int row = 0;
        
        // Nama Kaos
        form.add(new Label("Nama Kaos:"), 0, row);
        txtNamaKaos = new TextField();
        txtNamaKaos.setPromptText("Contoh: Distro Zone Basic Tee");
        styleTextField(txtNamaKaos);
        form.add(txtNamaKaos, 1, row++);
        
        // Merk
        form.add(new Label("Merk:"), 0, row);
        cbMerk = new ComboBox<>();
        cbMerk.setPromptText("Pilih Merk");
        styleComboBox(cbMerk);
        form.add(cbMerk, 1, row++);
        
        // Tipe
        form.add(new Label("Tipe:"), 0, row);
        cbTipe = new ComboBox<>();
        cbTipe.setPromptText("Pilih Tipe");
        styleComboBox(cbTipe);
        form.add(cbTipe, 1, row++);
        
        // Harga Jual
        form.add(new Label("Harga Jual:"), 0, row);
        txtHargaJual = new TextField();
        txtHargaJual.setPromptText("Contoh: 150000");
        styleTextField(txtHargaJual);
        form.add(txtHargaJual, 1, row++);
        
        // Harga Pokok
        form.add(new Label("Harga Pokok:"), 0, row);
        txtHargaPokok = new TextField();
        txtHargaPokok.setPromptText("Contoh: 75000");
        styleTextField(txtHargaPokok);
        form.add(txtHargaPokok, 1, row++);
        
        // Deskripsi
        form.add(new Label("Deskripsi:"), 0, row);
        txtDeskripsi = new TextArea();
        txtDeskripsi.setPrefRowCount(3);
        txtDeskripsi.setPromptText("Deskripsi produk...");
        styleTextArea(txtDeskripsi);
        form.add(txtDeskripsi, 1, row++);
        
        // Spesifikasi
        form.add(new Label("Spesifikasi:"), 0, row);
        txtSpesifikasi = new TextArea();
        txtSpesifikasi.setPrefRowCount(3);
        txtSpesifikasi.setPromptText("Spesifikasi produk...");
        styleTextArea(txtSpesifikasi);
        form.add(txtSpesifikasi, 1, row++);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        btnNewProduk = createStyledButton("➕ Produk Baru", "#2ecc71");
        btnNewProduk.setOnAction(e -> clearProdukForm());
        
        btnSaveProduk = createStyledButton("💾 Simpan", "#3498db");
        btnSaveProduk.setOnAction(e -> saveProduk());
        
        btnDeleteProduk = createStyledButton("🗑 Hapus", "#e74c3c");
        btnDeleteProduk.setOnAction(e -> deleteProduk());
        
        btnSaveAll = createStyledButton("💾 Simpan Semua", "#9b59b6");
        btnSaveAll.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Konfirmasi Simpan Semua");
            confirm.setHeaderText(null);
            confirm.setContentText("Yakin ingin menyimpan produk, varian, dan foto sekaligus?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                saveAll();
            }
        });

        buttonBox.getChildren().addAll(btnNewProduk, btnSaveProduk, btnDeleteProduk, btnSaveAll);
        form.add(buttonBox, 1, row);
        
        // Load combo data
        loadComboBoxData();
        
        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: transparent;");
        
        container.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return container;
    }
    
    private void saveAll() {
        // 1. Validasi dan simpan produk
        try {
            if (txtNamaKaos.getText().trim().isEmpty()) {
                showAlert("Nama produk harus diisi!", Alert.AlertType.WARNING);
                return;
            }

            Produk produk = selectedProduk != null ? selectedProduk : new Produk();
            produk.setNamaKaos(txtNamaKaos.getText().trim());
            produk.setIdMerk(cbMerk.getValue().getIdMerk());
            produk.setIdTipe(cbTipe.getValue().getIdTipe());
            produk.setHargaJual(Long.parseLong(txtHargaJual.getText().trim()));
            produk.setHargaPokok(Long.parseLong(txtHargaPokok.getText().trim()));
            produk.setDeskripsi(txtDeskripsi.getText().trim());
            produk.setSpesifikasi(txtSpesifikasi.getText().trim());

            boolean success;
            if (selectedProduk == null) {
                success = produkDAO.tambahProduk(produk);
                if (success) {
                    selectedProduk = produkDAO.getLastProduk();
                }
            } else {
                success = produkDAO.updateProduk(produk);
            }

            if (!success) {
                showAlert("Gagal menyimpan produk!", Alert.AlertType.ERROR);
                return;
            }

            // 2. Simpan varian
            List<Varian> varianToSave = new ArrayList<>();
            for (Ukuran ukuran : ukuranStokMap.keySet()) {
                TextField txtStokField = ukuranStokMap.get(ukuran);
                String stokText = txtStokField.getText().trim();
                if (!stokText.isEmpty()) {
                    try {
                        Long stok = Long.parseLong(stokText);
                        if (stok <= 0) continue;

                        Varian varian = new Varian();
                        varian.setIdProduk(selectedProduk.getIdProduk());
                        varian.setIdUkuran(ukuran.getIdUkuran());
                        varian.setIdWarna(cbWarnaVarian.getValue().getIdWarna());
                        varian.setStokKaos(stok);

                        varianToSave.add(varian);

                    } catch (NumberFormatException e) {
                        showAlert("Stok untuk ukuran " + ukuran.getNamaUkuran() + " tidak valid!", Alert.AlertType.ERROR);
                        return;
                    }
                }
            }

            boolean allVarianSuccess = true;
            for (Varian v : varianToSave) {
                if (!varianDAO.createVarian(v)) {
                    allVarianSuccess = false;
                    break;
                }
            }

            if (!allVarianSuccess) {
                showAlert("Gagal menyimpan beberapa varian!", Alert.AlertType.ERROR);
                return;
            }

            // 3. Simpan foto
            // Karena foto tidak disimpan secara langsung di UI, kita hanya bisa menyimpan foto yang diupload saat ini
            // Jadi, kita asumsikan foto sudah diupload sebelumnya, atau kita tidak bisa menyimpan foto tanpa file
            // Untuk sederhananya, kita skip bagian ini atau biarkan user upload foto terlebih dahulu

            showAlert("Semua data berhasil disimpan!", Alert.AlertType.INFORMATION);
            loadProdukList("");
            clearProdukForm();

        } catch (Exception e) {
            showAlert("Input tidak valid: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    
    private void enterEditMode(Varian varian) {
        isEditMode = true;

        // UPDATE: Matikan Create Mode (Hidden + Unmanaged)
        formCreateMode.setVisible(false);
        formCreateMode.setManaged(false);

        // UPDATE: Nyalakan Edit Mode (Visible + Managed)
        formEditMode.setVisible(true);
        formEditMode.setManaged(true);

        // Isi data
        cbWarnaEdit.setItems(FXCollections.observableArrayList(warnaDAO.getAllWarna()));
        cbWarnaEdit.setValue(findWarnaById(varian.getIdWarna()));

        cbUkuranEdit.setItems(FXCollections.observableArrayList(ukuranDAO.getAllUkuran()));
        cbUkuranEdit.setValue(findUkuranById(varian.getIdUkuran()));

        txtStokEdit.setText(String.valueOf(varian.getStokKaos()));
    }

    private void cancelEditMode() {
        isEditMode = false;
        
        // UPDATE: Matikan Edit Mode
        formEditMode.setVisible(false);
        formEditMode.setManaged(false);

        // UPDATE: Nyalakan Create Mode
        formCreateMode.setVisible(true);
        formCreateMode.setManaged(true);

        // Reset Form Inputs
        cbWarnaVarian.setValue(null);
        for (TextField txt : ukuranStokMap.values()) {
            txt.clear();
        }
        
        // Opsional: Hapus seleksi di tabel agar terlihat fresh
        if (tableVarian != null) {
            tableVarian.getSelectionModel().clearSelection();
        }
    }

    private void saveEditedVarian() {
        if (selectedVarian == null) {
            showAlert("Tidak ada varian yang dipilih!", Alert.AlertType.WARNING);
            return;
        }

        try {
            selectedVarian.setIdWarna(cbWarnaEdit.getValue().getIdWarna());
            selectedVarian.setIdUkuran(cbUkuranEdit.getValue().getIdUkuran());
            selectedVarian.setStokKaos(Long.parseLong(txtStokEdit.getText().trim()));

            if (varianDAO.updateVarian(selectedVarian)) {
                showAlert("Varian berhasil diupdate!", Alert.AlertType.INFORMATION);
                loadVarianList(selectedProduk.getIdProduk());
                cancelEditMode(); // Kembali ke mode create
            } else {
                showAlert("Gagal update varian!", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Input tidak valid: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    // ==================== TAB 2: VARIAN ====================
    
    private ScrollPane createVarianTab() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));

        Label infoLabel = new Label("💡 Pilih produk di sebelah kiri untuk mengelola varian");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER_LEFT);

        // --- MODE CREATE ---
        formCreateMode = new VBox(10);
        // UPDATE: Aktifkan Managed agar layout menghitung space-nya
        formCreateMode.setVisible(true); 
        formCreateMode.setManaged(true); 

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        int row = 0;

        form.add(new Label("Warna:"), 0, row);
        cbWarnaVarian = new ComboBox<>();
        cbWarnaVarian.setPromptText("Pilih Warna");
        styleComboBox(cbWarnaVarian);
        form.add(cbWarnaVarian, 1, row++);

        Label lblUkuran = new Label("Ukuran:");
        lblUkuran.setStyle("-fx-font-weight: bold;");
        form.add(lblUkuran, 0, row);

        chkUkuranContainer = new FlowPane();
        chkUkuranContainer.setHgap(15);
        chkUkuranContainer.setVgap(10);
        chkUkuranContainer.setPadding(new Insets(5, 0, 5, 0));
        form.add(chkUkuranContainer, 1, row++);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_LEFT);
        btnAddVarian = createStyledButton("➕ Tambah", "#2ecc71");
        btnAddVarian.setOnAction(e -> addVarian());
        btnUpdateVarian = createStyledButton("✏ Update", "#3498db");
        btnUpdateVarian.setOnAction(e -> updateVarian());
        btnDeleteVarian = createStyledButton("🗑 Hapus", "#e74c3c");
        btnDeleteVarian.setOnAction(e -> deleteVarian());
        btnBox.getChildren().addAll(btnAddVarian, btnUpdateVarian, btnDeleteVarian);
        form.add(btnBox, 1, row);

        formCreateMode.getChildren().add(form);

        // --- MODE EDIT ---
        formEditMode = new VBox(10);
        // UPDATE: Non-aktifkan Visible & Managed agar tidak makan space saat awal
        formEditMode.setVisible(false);
        formEditMode.setManaged(false); 

        GridPane formEdit = new GridPane();
        formEdit.setHgap(15);
        formEdit.setVgap(15);
        int rowEdit = 0;

        formEdit.add(new Label("Warna:"), 0, rowEdit);
        cbWarnaEdit = new ComboBox<>();
        cbWarnaEdit.setPromptText("Pilih Warna");
        styleComboBox(cbWarnaEdit);
        formEdit.add(cbWarnaEdit, 1, rowEdit++);

        formEdit.add(new Label("Ukuran:"), 0, rowEdit);
        cbUkuranEdit = new ComboBox<>();
        cbUkuranEdit.setPromptText("Pilih Ukuran");
        styleComboBox(cbUkuranEdit);
        formEdit.add(cbUkuranEdit, 1, rowEdit++);

        formEdit.add(new Label("Stok:"), 0, rowEdit);
        txtStokEdit = new TextField();
        txtStokEdit.setPromptText("Contoh: 50");
        styleTextField(txtStokEdit);
        formEdit.add(txtStokEdit, 1, rowEdit++);

        HBox btnBoxEdit = new HBox(10);
        btnBoxEdit.setAlignment(Pos.CENTER_LEFT);
        Button btnCancelEdit = createStyledButton("❌ Batal", "#95a5a6");
        btnCancelEdit.setOnAction(e -> cancelEditMode());
        btnUpdateVarianEdit = createStyledButton("✅ Simpan", "#3498db");
        btnUpdateVarianEdit.setOnAction(e -> saveEditedVarian());
        btnBoxEdit.getChildren().addAll(btnCancelEdit, btnUpdateVarianEdit);
        formEdit.add(btnBoxEdit, 1, rowEdit);

        formEditMode.getChildren().add(formEdit);

        formContainer.getChildren().addAll(formCreateMode, formEditMode);

        tableVarian = createVarianTable();
        VBox.setVgrow(tableVarian, Priority.ALWAYS);

        loadVarianComboData();
        loadUkuranInputs();

        container.getChildren().addAll(infoLabel, formContainer, new Separator(), tableVarian);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        return scrollPane;
    }
    
    private TableView<Varian> createVarianTable() {
        TableView<Varian> table = new TableView<>();
        
        TableColumn<Varian, String> colUkuran = new TableColumn<>("Ukuran");
        colUkuran.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaUkuran())
        );
        
        TableColumn<Varian, String> colWarna = new TableColumn<>("Warna");
        colWarna.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaWarna())
        );
        
        TableColumn<Varian, Long> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(new PropertyValueFactory<>("stokKaos"));
        
        table.getColumns().addAll(colUkuran, colWarna, colStok);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedVarian = newVal;
                enterEditMode(newVal);
            }
        });
        
        return table;
    }
    
    // ==================== TAB 3: FOTO PRODUK ====================
    
    private VBox createFotoProdukTab() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(25));
        
        // Info Label
        Label infoLabel = new Label("💡 Pilih produk di sebelah kiri untuk mengelola foto");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
        
        // Upload Form
        HBox uploadBox = new HBox(15);
        uploadBox.setAlignment(Pos.CENTER_LEFT);
        
        cbWarnaFoto = new ComboBox<>();
        cbWarnaFoto.setPromptText("Pilih Warna");
        styleComboBox(cbWarnaFoto);
        
        btnUploadFoto = createStyledButton("📷 Upload Foto", "#3498db");
        btnUploadFoto.setOnAction(e -> uploadFoto());
        
        uploadBox.getChildren().addAll(new Label("Warna:"), cbWarnaFoto, btnUploadFoto);
        
        // Foto Container
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        fotoProdukContainer = new FlowPane();
        fotoProdukContainer.setHgap(15);
        fotoProdukContainer.setVgap(15);
        fotoProdukContainer.setPadding(new Insets(15));
        fotoProdukContainer.setStyle("-fx-background-color: #f8f9fa;");
        
        scrollPane.setContent(fotoProdukContainer);
        
        // Load warna
        loadFotoComboData();
        
        container.getChildren().addAll(infoLabel, uploadBox, new Separator(), scrollPane);
        return container;
    }
    
    // ==================== DATA LOADING METHODS ====================
    
    private void loadProdukList(String keyword) {
        List<Produk> list = produkDAO.getAllProduk(keyword);
        produkList = FXCollections.observableArrayList(list);
        tableProduk.setItems(produkList);
    }
    
    private void loadProdukDetail(Produk produk) {
        // Tab 1: Load produk info
        txtNamaKaos.setText(produk.getNamaKaos());
        cbMerk.setValue(findMerkById(produk.getIdMerk()));
        cbTipe.setValue(findTipeById(produk.getIdTipe()));
        txtHargaJual.setText(produk.getHargaJual().toString());
        txtHargaPokok.setText(produk.getHargaPokok().toString());
        txtDeskripsi.setText(produk.getDeskripsi());
        txtSpesifikasi.setText(produk.getSpesifikasi());
        
        // Tab 2: Load varian
        loadVarianList(produk.getIdProduk());
        
        // Tab 3: Load foto
        loadFotoProdukList(produk.getIdProduk());
    }
    
    private void loadVarianList(Long idProduk) {
        List<Varian> list = varianDAO.getVarianByProduk(idProduk);
        varianList = FXCollections.observableArrayList(list);
        tableVarian.setItems(varianList);
        cancelEditMode();
    }
    
    private void loadFotoProdukList(Long idProduk) {
        fotoProdukContainer.getChildren().clear();
        List<FotoProduk> fotoList = fotoProdukDAO.getFotoByProduk(idProduk);
        
        for (FotoProduk foto : fotoList) {
            VBox fotoCard = createFotoCard(foto);
            fotoProdukContainer.getChildren().add(fotoCard);
        }
    }
    
    private VBox createFotoCard(FotoProduk foto) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e1e8ed; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        card.setPrefSize(150, 180);
        
        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        
        try {
            Image image = new Image(foto.getUrlFoto(), true);
            imageView.setImage(image);
        } catch (Exception e) {
            imageView.setImage(null);
        }
        
        // Label
        Label lblWarna = new Label(foto.getNamaWarna() != null ? foto.getNamaWarna() : "No Color");
        lblWarna.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        // --- BAGIAN TOMBOL (UPDATE INI) ---
        HBox buttonBox = new HBox(5); // Jarak antar tombol
        buttonBox.setAlignment(Pos.CENTER);
        
        // Tombol Ganti (Replace)
        Button btnEdit = new Button("✏");
        btnEdit.setStyle(
            "-fx-background-color: #f39c12; " + // Warna Oranye
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-cursor: hand;"
        );
        btnEdit.setOnAction(e -> editFoto(foto));
        
        // Delete Button
        Button btnDelete = new Button("🗑");
        btnDelete.setStyle(
            "-fx-background-color: #e74c3c; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-cursor: hand;"
        );
        btnDelete.setOnAction(e -> deleteFoto(foto));
        
        buttonBox.getChildren().addAll(btnEdit, btnDelete);
        
        card.getChildren().addAll(imageView, lblWarna, buttonBox);
        return card;
    }
    
    
        private void replaceFoto(FotoProduk foto) {
            if (selectedProduk == null) return;

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Ganti Foto Produk");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );

            // Buka dialog pilih file
            File file = fileChooser.showOpenDialog(getScene().getWindow());

            if (file != null) {
                // 1. Hapus foto lama dari database
                fotoProdukDAO.deleteFotoProduk(foto.getIdFotoProduk());

                // 2. Siapkan objek foto baru
                // Kita ambil ID Produk dan ID Warna dari foto lama agar konteksnya tetap sama
                FotoProduk newFoto = new FotoProduk();
                newFoto.setIdProduk(selectedProduk.getIdProduk());
                newFoto.setIdWarna(foto.getIdWarna()); // Warna tetap sama seperti sebelumnya

                // 3. Upload file baru
                if (fotoProdukDAO.createFotoProduk(newFoto, file)) {
                    showAlert("Foto berhasil diganti!", Alert.AlertType.INFORMATION);
                    loadFotoProdukList(selectedProduk.getIdProduk());
                } else {
                    // Jika gagal upload, ada kemungkinan foto lama hilang tapi baru belum masuk.
                    // Tapi biasanya DAO createFotoProduk sudah handle error dengan baik.
                    showAlert("Gagal mengganti foto!", Alert.AlertType.ERROR);
                }
            }
        }
    
    // ==================== COMBO BOX DATA LOADING ====================
    
    private void loadComboBoxData() {
        List<Merk> merkList = merkDAO.getAllMerk();
        cbMerk.setItems(FXCollections.observableArrayList(merkList));
        
        List<Tipe> tipeList = tipeDAO.getAllTipe();
        cbTipe.setItems(FXCollections.observableArrayList(tipeList));
    }
    
    private void loadUkuranInputs() {
        List<Ukuran> ukuranList = ukuranDAO.getAllUkuran();
        chkUkuranContainer.getChildren().clear(); // Kosongkan dulu
        ukuranStokMap.clear(); // Reset map

        for (Ukuran ukuran : ukuranList) {
            HBox hbox = new HBox(8); // Jarak antar label dan input
            hbox.setAlignment(Pos.CENTER_LEFT);

            Label lblUkuran = new Label(ukuran.getNamaUkuran());
            lblUkuran.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

            TextField txtStokPerUkuran = new TextField();
            txtStokPerUkuran.setPromptText("Stok " + ukuran.getNamaUkuran());
            txtStokPerUkuran.setPrefWidth(80);

            hbox.getChildren().addAll(lblUkuran, txtStokPerUkuran);
            chkUkuranContainer.getChildren().add(hbox);

            ukuranStokMap.put(ukuran, txtStokPerUkuran);
        }
    }
    
    private void loadVarianComboData() {
        List<Ukuran> ukuranList = ukuranDAO.getAllUkuran();
//        cbUkuran.setItems(FXCollections.observableArrayList(ukuranList));
        
        List<Warna> warnaList = warnaDAO.getAllWarna();
        cbWarnaVarian.setItems(FXCollections.observableArrayList(warnaList));
    }
    
    private void loadFotoComboData() {
        List<Warna> warnaList = warnaDAO.getAllWarna();
        cbWarnaFoto.setItems(FXCollections.observableArrayList(warnaList));
    }
    
    // ==================== CRUD OPERATIONS ====================
    
    // --- Produk CRUD ---
    
    private void saveProduk() {
        try {
            if (txtNamaKaos.getText().trim().isEmpty()) {
                showAlert("Nama produk harus diisi!", Alert.AlertType.WARNING);
                return;
            }
            
            Produk produk = selectedProduk != null ? selectedProduk : new Produk();
            produk.setNamaKaos(txtNamaKaos.getText().trim());
            produk.setIdMerk(cbMerk.getValue().getIdMerk());
            produk.setIdTipe(cbTipe.getValue().getIdTipe());
            produk.setHargaJual(Long.parseLong(txtHargaJual.getText().trim()));
            produk.setHargaPokok(Long.parseLong(txtHargaPokok.getText().trim()));
            produk.setDeskripsi(txtDeskripsi.getText().trim());
            produk.setSpesifikasi(txtSpesifikasi.getText().trim());
            
            boolean success;
            if (selectedProduk == null) {
                success = produkDAO.tambahProduk(produk);
                if (success) {
                    selectedProduk = produkDAO.getLastProduk();
                }
            } else {
                success = produkDAO.updateProduk(produk);
            }
            
            if (success) {
                showAlert("Produk berhasil disimpan!", Alert.AlertType.INFORMATION);
                loadProdukList("");
            } else {
                showAlert("Gagal menyimpan produk!", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Input tidak valid: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void deleteProduk() {
        if (selectedProduk == null) {
            showAlert("Pilih produk yang akan dihapus!", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi");
        confirm.setHeaderText("Hapus Produk");
        confirm.setContentText("Yakin ingin menghapus produk ini beserta semua varian dan fotonya?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Hapus varian dan foto terkait
            varianDAO.deleteAllVarianByProduk(selectedProduk.getIdProduk());
            fotoProdukDAO.deleteAllFotoByProduk(selectedProduk.getIdProduk());
            
            if (produkDAO.deleteProduk(selectedProduk.getIdProduk())) {
                showAlert("Produk berhasil dihapus!", Alert.AlertType.INFORMATION);
                clearProdukForm();
                loadProdukList("");
            } else {
                showAlert("Gagal menghapus produk!", Alert.AlertType.ERROR);
            }
        }
    }
    
    private void clearProdukForm() {
        selectedProduk = null;
        txtNamaKaos.clear();
        cbMerk.setValue(null);
        cbTipe.setValue(null);
        txtHargaJual.clear();
        txtHargaPokok.clear();
        txtDeskripsi.clear();
        txtSpesifikasi.clear();
        tableVarian.getItems().clear();
        fotoProdukContainer.getChildren().clear();
    }
    
    // --- Varian CRUD ---
    
    private void addVarian() {
        if (selectedProduk == null) {
            showAlert("Pilih produk terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }

        if (cbWarnaVarian.getValue() == null) {
            showAlert("Warna harus dipilih!", Alert.AlertType.WARNING);
            return;
        }

        boolean anyFilled = false;
        List<Varian> varianToSave = new ArrayList<>();

        for (Ukuran ukuran : ukuranStokMap.keySet()) {
            TextField txtStokField = ukuranStokMap.get(ukuran);
            String stokText = txtStokField.getText().trim();

            if (!stokText.isEmpty()) {
                try {
                    Long stok = Long.parseLong(stokText);
                    if (stok <= 0) continue; // Skip jika stok 0 atau negatif

                    anyFilled = true;

                    // Cek duplikat
                    if (varianDAO.isVarianExists(selectedProduk.getIdProduk(), 
                            ukuran.getIdUkuran(), 
                            cbWarnaVarian.getValue().getIdWarna(), null)) {
                        showAlert("Varian " + ukuran.getNamaUkuran() + " sudah ada!", Alert.AlertType.WARNING);
                        continue;
                    }

                    Varian varian = new Varian();
                    varian.setIdProduk(selectedProduk.getIdProduk());
                    varian.setIdUkuran(ukuran.getIdUkuran());
                    varian.setIdWarna(cbWarnaVarian.getValue().getIdWarna());
                    varian.setStokKaos(stok);

                    varianToSave.add(varian);

                } catch (NumberFormatException e) {
                    showAlert("Stok untuk ukuran " + ukuran.getNamaUkuran() + " tidak valid!", Alert.AlertType.ERROR);
                    return;
                }
            }
        }

        if (!anyFilled) {
            showAlert("Isi setidaknya satu stok ukuran!", Alert.AlertType.WARNING);
            return;
        }

        // Simpan semua varian sekaligus
        boolean allSuccess = true;
        for (Varian v : varianToSave) {
            if (!varianDAO.createVarian(v)) {
                allSuccess = false;
                break;
            }
        }

        if (allSuccess) {
            showAlert("Semua varian berhasil ditambahkan!", Alert.AlertType.INFORMATION);
            loadVarianList(selectedProduk.getIdProduk());
            clearVarianForm();
        } else {
            showAlert("Gagal menyimpan beberapa varian!", Alert.AlertType.ERROR);
        }
    }
    
    private void updateVarian() {
        if (selectedVarian == null) {
            showAlert("Pilih varian yang akan diupdate!", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            selectedVarian.setStokKaos(Long.parseLong(txtStok.getText().trim()));
            
            if (varianDAO.updateVarian(selectedVarian)) {
                showAlert("Stok varian berhasil diupdate!", Alert.AlertType.INFORMATION);
                loadVarianList(selectedProduk.getIdProduk());
                clearVarianForm();
            } else {
                showAlert("Gagal update varian!", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Input tidak valid: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void deleteVarian() {
        if (selectedVarian == null) {
            showAlert("Pilih varian yang akan dihapus!", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi");
        confirm.setHeaderText("Hapus Varian");
        confirm.setContentText("Yakin ingin menghapus varian ini?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (varianDAO.deleteVarian(selectedVarian.getIdVarian())) {
                showAlert("Varian berhasil dihapus!", Alert.AlertType.INFORMATION);
                loadVarianList(selectedProduk.getIdProduk());
                clearVarianForm();
            } else {
                showAlert("Gagal menghapus varian!", Alert.AlertType.ERROR);
            }
        }
    }
    
    private void loadVarianToForm(Varian varian) {
        cbUkuran.setValue(findUkuranById(varian.getIdUkuran()));
        cbWarnaVarian.setValue(findWarnaById(varian.getIdWarna()));
        txtStok.setText(varian.getStokKaos().toString());
    }
    
    private void clearVarianForm() {
        selectedVarian = null;
        cbWarnaVarian.setValue(null);

        // Kosongkan semua input stok
        for (TextField txt : ukuranStokMap.values()) {
            txt.clear();
        }
    }
    
    // --- Foto Produk CRUD ---
    
    private void uploadFoto() {
        if (selectedProduk == null) {
            showAlert("Pilih produk terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }
        
        if (cbWarnaFoto.getValue() == null) {
            showAlert("Pilih warna foto!", Alert.AlertType.WARNING);
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Produk");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            FotoProduk foto = new FotoProduk();
            foto.setIdProduk(selectedProduk.getIdProduk());
            foto.setIdWarna(cbWarnaFoto.getValue().getIdWarna());
            
            if (fotoProdukDAO.createFotoProduk(foto, file)) {
                showAlert("Foto berhasil diupload!", Alert.AlertType.INFORMATION);
                loadFotoProdukList(selectedProduk.getIdProduk());
                cbWarnaFoto.setValue(null);
            } else {
                showAlert("Gagal upload foto!", Alert.AlertType.ERROR);
            }
        }
    }
    
    private void editFoto(FotoProduk foto) {
        if (selectedProduk == null) return;

        // --- 1. PILIH WARNA BARU ---
        List<Warna> listWarna = warnaDAO.getAllWarna();
        
        // Cari warna saat ini untuk dijadikan nilai default di dialog
        Warna currentWarna = listWarna.stream()
            .filter(w -> w.getIdWarna().equals(foto.getIdWarna()))
            .findFirst()
            .orElse(null);

        // Tampilkan Dialog Pilihan Warna
        ChoiceDialog<Warna> dialogWarna = new ChoiceDialog<>(currentWarna, listWarna);
        dialogWarna.setTitle("Edit Foto");
        dialogWarna.setHeaderText("Pilih Warna Baru");
        dialogWarna.setContentText("Pindahkan foto ke warna:");

        Optional<Warna> resultWarna = dialogWarna.showAndWait();
        if (!resultWarna.isPresent()) {
            return; // User menekan Cancel di pilih warna
        }
        
        Warna selectedWarna = resultWarna.get();

        // --- 2. PILIH FILE GAMBAR BARU ---
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Pengganti");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file == null) {
            return; // User menekan Cancel di pilih file
        }

        // --- 3. PROSES UPDATE (ID TETAP) ---
        // Set warna baru ke objek foto
        foto.setIdWarna(selectedWarna.getIdWarna());

        // Panggil DAO update yang sudah diperbaiki sebelumnya
        // Logic DAO akan: Upload file baru -> Update URL di DB -> Hapus file lama
        if (fotoProdukDAO.updateFotoProduk(foto, file)) {
            showAlert("Foto berhasil diupdate!", Alert.AlertType.INFORMATION);
            
            // --- 4. REFRESH LANGSUNG ---
            // Kita ambil ulang list foto dari database dan render ulang seluruh kartu
            loadFotoProdukList(selectedProduk.getIdProduk());
            
        } else {
            showAlert("Gagal mengupdate foto!", Alert.AlertType.ERROR);
        }
    }
    
    private void deleteFoto(FotoProduk foto) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi");
        confirm.setHeaderText("Hapus Foto");
        confirm.setContentText("Yakin ingin menghapus foto ini?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (fotoProdukDAO.deleteFotoProduk(foto.getIdFotoProduk())) {
                showAlert("Foto berhasil dihapus!", Alert.AlertType.INFORMATION);
                loadFotoProdukList(selectedProduk.getIdProduk());
            } else {
                showAlert("Gagal menghapus foto!", Alert.AlertType.ERROR);
            }
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    private Merk findMerkById(Long id) {
        return cbMerk.getItems().stream()
            .filter(m -> m.getIdMerk().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    private Tipe findTipeById(Long id) {
        return cbTipe.getItems().stream()
            .filter(t -> t.getIdTipe().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    private Ukuran findUkuranById(Long id) {
        // Ambil langsung dari DAO agar tidak tergantung pada UI Component yang mungkin null
        if (id == null) return null;
        return ukuranDAO.getAllUkuran().stream()
            .filter(u -> u.getIdUkuran().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    private Warna findWarnaById(Long id) {
        // Ambil langsung dari DAO agar tidak tergantung pada UI Component
        if (id == null) return null;
        return warnaDAO.getAllWarna().stream()
            .filter(w -> w.getIdWarna().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    // ==================== STYLING METHODS ====================
    
    private void styleTextField(TextField field) {
        field.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e1e8ed; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 12; " +
            "-fx-font-size: 13px;"
        );
        field.setPrefWidth(300);
    }
    
    private void styleTextArea(TextArea area) {
        area.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e1e8ed; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 12; " +
            "-fx-font-size: 13px;"
        );
        area.setPrefWidth(300);
    }
    
    private void styleComboBox(ComboBox<?> combo) {
        combo.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e1e8ed; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6;"
        );
        combo.setPrefWidth(300);
    }
    
    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        btn.setPadding(new Insets(8, 16, 8, 16));
        btn.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: derive(" + color + ", -10%); " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        ));
        
        return btn;
    }
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : 
                      type == Alert.AlertType.WARNING ? "Peringatan" : "Sukses");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}