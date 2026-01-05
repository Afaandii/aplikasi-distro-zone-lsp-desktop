package view;

import dao.RoleDAO;
import dao.UserDAO;
import java.io.File;
import java.net.URI;
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
import model.User;

import java.util.List;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import model.Role;

public class KaryawanManagementPanel extends VBox {
    private User currentUser;
    private UserDAO userDAO;
    private TableView<User> tableView;
    private ObservableList<User> userList;
    private TextField txtSearch;
    private final java.util.Map<String, Image> imageCache = new java.util.concurrent.ConcurrentHashMap<>();

    public KaryawanManagementPanel(User user) {
        this.currentUser = user;
        this.userDAO = new UserDAO();
        initializeUI();
        loadData("");
    }
    
    private void loadImageAsync(String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageView.setImage(new Image(getClass().getResource("/images/default-profile.png").toExternalForm()));
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
                    return new Image(getClass().getResource("/images/default-profile.png").toExternalForm());
                }
            }
        };

        task.setOnSucceeded(e -> {
            Image image = task.getValue();
            imageView.setImage(image);
            imageCache.put(imageUrl, image); // simpan ke cache
        });

        task.setOnFailed(e -> {
            imageView.setImage(new Image(getClass().getResource("/images/default-profile.png").toExternalForm()));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true); // agar tidak menghalangi shutdown
        thread.start();
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
        
        Label title = new Label("Manajemen Karyawan");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Kelola data karyawan dan tim Anda");
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
        txtSearch.setPromptText("Cari nama, username, atau NIK...");
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
        btnDelete.setOnAction(e -> deleteKaryawan());
        
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

    private TableView<User> createTable() {
        TableView<User> table = new TableView<>();
        table.setPrefHeight(550);
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-table-cell-border-color: transparent; " +
            "-fx-padding: 15;"
        );

        // ID Column (hidden)
        TableColumn<User, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getIdUser())
        );
        colId.setVisible(false);

        // Nama Column
        TableColumn<User, String> colNama = new TableColumn<>("NAMA");
        colNama.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNama())
        );
        colNama.setPrefWidth(180);
        styleColumn(colNama);

        // Username Column
        TableColumn<User, String> colUsername = new TableColumn<>("USERNAME");
        colUsername.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getUsername())
        );
        colUsername.setPrefWidth(140);
        styleColumn(colUsername);

        // NIK Column
        TableColumn<User, String> colNik = new TableColumn<>("NIK");
        colNik.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNik())
        );
        colNik.setPrefWidth(130);
        styleColumn(colNik);

        // Alamat Column
        TableColumn<User, String> colAlamat = new TableColumn<>("ALAMAT");
        colAlamat.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getAlamat())
        );
        colAlamat.setPrefWidth(220);
        styleColumn(colAlamat);

        // Kota Column
        TableColumn<User, String> colKota = new TableColumn<>("KOTA");
        colKota.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getKota())
        );
        colKota.setPrefWidth(150);
        styleColumn(colKota);

        // No Telp Column
        TableColumn<User, String> colNoTelp = new TableColumn<>("NO. TELP");
        colNoTelp.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNoTelp())
        );
        colNoTelp.setPrefWidth(130);
        styleColumn(colNoTelp);

        // Role Column with badge
        TableColumn<User, String> colRole = new TableColumn<>("ROLE");
        colRole.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaRole())
        );
        colRole.setPrefWidth(100);
        colRole.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle(
                        "-fx-background-radius: 12; " +
                        (item.equals("Admin") ? 
                            "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;" : 
                            "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;")
                    );
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        
        TableColumn<User, String> colFoto = new TableColumn<>("FOTO");
            colFoto.setCellValueFactory(cell -> 
                new javafx.beans.property.SimpleStringProperty(cell.getValue().getFotoProfile())
            );
            colFoto.setPrefWidth(80);
            colFoto.setCellFactory(col -> new TableCell<User, String>() {
                private final ImageView imageView = new ImageView();

                {
                    imageView.setFitWidth(50);
                    imageView.setFitHeight(50);
                    imageView.setPreserveRatio(true);
                    imageView.setImage(new Image(getClass().getResource("/resource/default.jpg").toExternalForm())); // placeholder awal
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        // Tampilkan placeholder dulu
                        imageView.setImage(new Image(getClass().getResource("/resource/default.jpg").toExternalForm()));
                        setGraphic(imageView);

                        // Load gambar async
                        loadImageAsync(item, imageView);
                    }
                }
            });

        table.getColumns().addAll(colId, colNama, colUsername, colNik, colAlamat, colKota, colNoTelp, colRole, colFoto);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }

    private void styleColumn(TableColumn<User, String> column) {
        column.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-size: 13px;");
    }

    private void loadData(String searchTerm) {
        List<User> allUsers = userDAO.getAllUsers(searchTerm);
        List<User> filtered = allUsers.stream()
            .filter(u -> !"Customer".equalsIgnoreCase(u.getNamaRole()))
            .collect(Collectors.toList());

        userList = FXCollections.observableArrayList(filtered);
        tableView.setItems(userList);
    }

    private void showAddDialog() {
        KaryawanFormDialog dialog = new KaryawanFormDialog(null);
        boolean success = dialog.showAndWait();
        if (success) {
            loadData("");
        }
    }

    private void showEditDialog() {
        User selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih karyawan yang akan diedit terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }
        KaryawanFormDialog dialog = new KaryawanFormDialog(selected);
        boolean success = dialog.showAndWait();
        if (success) {
            loadData("");
        }
    }

    private void deleteKaryawan() {
        User selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih karyawan yang akan dihapus terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Hapus");
        confirm.setHeaderText("Hapus Karyawan");
        confirm.setContentText("Apakah Anda yakin ingin menghapus karyawan \"" + selected.getNama() + "\"?\nData yang dihapus tidak dapat dikembalikan.");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (userDAO.deleteUser(selected.getIdUser())) {
                    showModernAlert("✅ Berhasil", "Karyawan berhasil dihapus!", Alert.AlertType.INFORMATION);
                    loadData("");
                } else {
                    showModernAlert("❌ Error", "Gagal menghapus karyawan!", Alert.AlertType.ERROR);
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
    public static class KaryawanFormDialog {
        private User user;
        private boolean success = false;
        private List<Role> roleList;
        private File selectedPhotoFile;

        public KaryawanFormDialog(User user) {
            this.user = user;
        }

        public boolean showAndWait() {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(user == null ? "Tambah Karyawan Baru" : "Edit Data Karyawan");

            VBox root = new VBox(25);
            root.setPadding(new Insets(30));
            root.setStyle("-fx-background-color: #f8f9fa;");

            // Header
            VBox header = new VBox(8);
            Label titleLabel = new Label(user == null ? "Tambah Karyawan Baru" : "Edit Data Karyawan");
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

            // 🔝 ROLE / JABATAN DI ATAS
            VBox roleBox = new VBox(8);
            Label roleLabel = new Label("Role / Jabatan");
            roleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            roleLabel.setTextFill(Color.web("#2c3e50"));

            ComboBox<Role> cbRole = new ComboBox<>();
            RoleDAO roleDAO = new RoleDAO();
            roleList = roleDAO.getAllRoles();
            cbRole.getItems().addAll(roleList);
            cbRole.setPromptText("Pilih role...");
            cbRole.setPrefWidth(Double.MAX_VALUE);
            cbRole.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #e1e8ed; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-padding: 10;"
            );

            cbRole.setCellFactory(param -> new ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNamaRole());
                    }
                }
            });

            cbRole.setButtonCell(new ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNamaRole());
                    }
                }
            });

            roleBox.getChildren().addAll(roleLabel, cbRole);

            // 📄 NAMA LENGKAP
            VBox namaBox = new VBox(5);
            Label namaLabel = new Label("Nama Lengkap");
            namaLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            namaLabel.setTextFill(Color.web("#2c3e50"));
            TextField txtNama = createStyledTextField("", "👤");
            txtNama.setPromptText("");
            namaBox.getChildren().addAll(namaLabel, txtNama);

            // 🆔 USERNAME
            VBox usernameBox = new VBox(5);
            Label usernameLabel = new Label("Username");
            usernameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            usernameLabel.setTextFill(Color.web("#2c3e50"));
            TextField txtUsername = createStyledTextField("", "🔑");
            txtUsername.setPromptText("");
            usernameBox.getChildren().addAll(usernameLabel, txtUsername);

            // 🆔 NIK
            VBox nikBox = new VBox(5);
            Label nikLabel = new Label("NIK");
            nikLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            nikLabel.setTextFill(Color.web("#2c3e50"));
            TextField txtNik = createStyledTextField("", "🆔");
            txtNik.setPromptText("");
            nikBox.getChildren().addAll(nikLabel, txtNik);

            // 📍 ALAMAT
            VBox alamatBox = new VBox(5);
            Label alamatLabel = new Label("Alamat");
            alamatLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            alamatLabel.setTextFill(Color.web("#2c3e50"));
            TextField txtAlamat = createStyledTextField("", "📍");
            txtAlamat.setPromptText("");
            alamatBox.getChildren().addAll(alamatLabel, txtAlamat);

            // 🏙️ KOTA
            VBox kotaBox = new VBox(5);
            Label kotaLabel = new Label("Kota");
            kotaLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            kotaLabel.setTextFill(Color.web("#2c3e50"));
            TextField txtKota = createStyledTextField("", "🏙️");
            txtKota.setPromptText("");
            kotaBox.getChildren().addAll(kotaLabel, txtKota);

            // 📞 NO. TELEPON
            VBox noTelpBox = new VBox(5);
            Label noTelpLabel = new Label("No. Telepon");
            noTelpLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            noTelpLabel.setTextFill(Color.web("#2c3e50"));
            TextField txtNoTelp = createStyledTextField("", "📱");
            txtNoTelp.setPromptText("");
            noTelpBox.getChildren().addAll(noTelpLabel, txtNoTelp);
            
            // Tambahkan setelah header, sebelum formContent
            VBox photoBox = new VBox(8);
            Label photoLabel = new Label("Foto Profil");
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
                    // Tampilkan preview
                    preview.setImage(new Image(selected.toURI().toString()));
                    // Simpan file untuk diupload nanti
                    this.selectedPhotoFile = selected;
                }
            });

            photoBox.getChildren().addAll(photoLabel, preview, btnUpload);

            // Load existing data if editing
            if (user != null) {
                txtNama.setText(user.getNama());
                txtUsername.setText(user.getUsername());
                txtNik.setText(user.getNik());
                txtAlamat.setText(user.getAlamat());
                txtKota.setText(user.getKota());
                txtNoTelp.setText(user.getNoTelp());

                Role selectedRole = roleList.stream()
                    .filter(r -> r.getNamaRole().equals(user.getNamaRole()))
                    .findFirst()
                    .orElse(null);

                if (selectedRole != null) {
                    cbRole.setValue(selectedRole);
                }
            }

            // Tambahkan semua ke formContent
            formContent.getChildren().addAll(
                roleBox,
                namaBox,
                usernameBox,
                nikBox,
                alamatBox,
                kotaBox,
                noTelpBox,
                photoBox
            );

            // Wrap formContent dalam ScrollPane
            ScrollPane scrollPane = new ScrollPane(formContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefViewportHeight(400); // Tinggi viewport — bisa disesuaikan
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

            Button btnSave = new Button(user == null ? "➕ Tambah" : "💾 Simpan");
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
                String username = txtUsername.getText().trim();
                String nik = txtNik.getText().trim();
                String alamat = txtAlamat.getText().trim();
                String kota = txtKota.getText().trim();
                String noTelp = txtNoTelp.getText().trim();
                String role = cbRole.getValue().getNamaRole();
                Long roleId = cbRole.getValue().getIdRole();

                if (nama.isEmpty() || username.isEmpty() || nik.isEmpty() ||
                    alamat.isEmpty() || kota.isEmpty() || noTelp.isEmpty() || role == null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("⚠️ Perhatian");
                    alert.setHeaderText("Data Tidak Lengkap");
                    alert.setContentText("Semua field wajib diisi!");
                    alert.showAndWait();
                    return;
                }

                UserDAO dao = new UserDAO();
                if (user == null) {
                    User newUser = new User();
                    newUser.setIdRole(cbRole.getValue().getIdRole());
                    newUser.setNama(nama);
                    newUser.setUsername(username);
                    newUser.setNik(nik);
                    newUser.setAlamat(alamat);
                    newUser.setKota(kota);
                    newUser.setNoTelp(noTelp);
                    newUser.setPassword(username); // default password = username

                    // ✅ Simpan foto
                    if (dao.createUser(newUser, selectedPhotoFile)) {
                        success = true;
                        dialog.close();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("✅ Berhasil");
                        alert.setHeaderText("Karyawan Ditambahkan");
                        alert.setContentText("Karyawan baru berhasil ditambahkan ke sistem!");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("❌ Error");
                        alert.setHeaderText("Gagal Menambahkan");
                        alert.setContentText("Terjadi kesalahan saat menambahkan karyawan!");
                        alert.showAndWait();
                    }
                } else {
                    user.setNama(nama);
                    user.setUsername(username);
                    user.setNik(nik);
                    user.setAlamat(alamat);
                    user.setKota(kota);
                    user.setNoTelp(noTelp);
                    user.setIdRole(cbRole.getValue().getIdRole());

                    // ✅ Simpan foto
                    if (dao.updateUser(user, selectedPhotoFile)) {
                        success = true;
                        dialog.close();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("✅ Berhasil");
                        alert.setHeaderText("Data Diperbarui");
                        alert.setContentText("Data karyawan berhasil diperbarui!");
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

            Scene scene = new Scene(root, 550, 700); // Tetap 700px tinggi — scroll akan muncul jika konten lebih panjang
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();
            return success;
        }

        private TextField createStyledTextField(String label, String icon) {
            TextField field = new TextField();
            field.setPromptText(label);
            field.setFont(Font.font("Segoe UI", 13));
            field.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #e1e8ed; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-padding: 12 15; " +
                "-fx-prompt-text-fill: #95a5a6;"
            );

            field.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    field.setStyle(
                        "-fx-background-color: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #3498db; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10; " +
                        "-fx-padding: 12 15; " +
                        "-fx-prompt-text-fill: #95a5a6;"
                    );
                } else {
                    field.setStyle(
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

            return field;
        }
    }
}