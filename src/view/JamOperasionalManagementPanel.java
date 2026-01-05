package view;

import dao.JamOperasionalDAO;
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
import model.JamOperasional;
import java.sql.Time;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Scene;

public class JamOperasionalManagementPanel extends VBox {
    private JamOperasionalDAO jamOperasionalDAO;
    private TableView<JamOperasional> tableView;
    private ObservableList<JamOperasional> jamList;
    private TextField txtSearch;

    public JamOperasionalManagementPanel() {
        this.jamOperasionalDAO = new JamOperasionalDAO();
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
        
        Label title = new Label("Manajemen Jam Operasional");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Atur jadwal operasional toko untuk layanan web dan desktop");
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
        txtSearch.setPromptText("Cari hari, tipe layanan...");
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

        Button btnEdit = createModernButton("✏️ Edit", "#3498db", "#2980b9");
        btnEdit.setOnAction(e -> showEditDialog());

        Button btnDelete = createModernButton("🚫 Tutup", "#e74c3c", "#c0392b");
        btnDelete.setOnAction(e -> closeJamOperasional());
        
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

    private TableView<JamOperasional> createTable() {
        TableView<JamOperasional> table = new TableView<>();
        table.setPrefHeight(550);
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-table-cell-border-color: transparent; " +
            "-fx-padding: 15;"
        );

        // ID Column (hidden)
        TableColumn<JamOperasional, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getIdJamOperasional())
        );
        colId.setVisible(false);

        // Tipe Layanan Column with badge
        TableColumn<JamOperasional, String> colTipe = new TableColumn<>("LAYANAN");
        colTipe.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getTipeLayanan())
        );
        colTipe.setPrefWidth(150);
        colTipe.setCellFactory(col -> new TableCell<JamOperasional, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                    badge.setPadding(new Insets(5, 14, 5, 14));
                    
                    if (item.equalsIgnoreCase("web")) {
                        badge.setStyle(
                            "-fx-background-color: #e3f2fd; " +
                            "-fx-text-fill: #1565c0; " +
                            "-fx-background-radius: 12;"
                        );
                    } else {
                        badge.setStyle(
                            "-fx-background-color: #f3e5f5; " +
                            "-fx-text-fill: #7b1fa2; " +
                            "-fx-background-radius: 12;"
                        );
                    }
                    
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Hari Column
        TableColumn<JamOperasional, String> colHari = new TableColumn<>("HARI");
        colHari.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getHari())
        );
        colHari.setPrefWidth(120);
        colHari.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Jam Buka Column with clock icon
        TableColumn<JamOperasional, String> colBuka = new TableColumn<>("JAM BUKA");
        colBuka.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(formatTime(cell.getValue().getJamBuka()))
        );
        colBuka.setPrefWidth(130);
        colBuka.setCellFactory(col -> new TableCell<JamOperasional, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);
                    
                    Label icon = new Label("🕐");
                    icon.setFont(Font.font(16));
                    
                    Label time = new Label(item);
                    time.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                    time.setTextFill(Color.web("#2c3e50"));
                    
                    box.getChildren().addAll(icon, time);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        // Jam Tutup Column with clock icon
        TableColumn<JamOperasional, String> colTutup = new TableColumn<>("JAM TUTUP");
        colTutup.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(formatTime(cell.getValue().getJamTutup()))
        );
        colTutup.setPrefWidth(130);
        colTutup.setCellFactory(col -> new TableCell<JamOperasional, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);
                    
                    Label icon = new Label("🕐");
                    icon.setFont(Font.font(16));
                    
                    Label time = new Label(item);
                    time.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
                    time.setTextFill(Color.web("#2c3e50"));
                    
                    box.getChildren().addAll(icon, time);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        // Status Column with colored badge
        TableColumn<JamOperasional, String> colStatus = new TableColumn<>("STATUS");
        colStatus.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus())
        );
        colStatus.setPrefWidth(120);
        colStatus.setCellFactory(col -> new TableCell<JamOperasional, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                    badge.setPadding(new Insets(5, 14, 5, 14));
                    
                    if (item.equalsIgnoreCase("buka")) {
                        badge.setStyle(
                            "-fx-background-color: #e8f5e9; " +
                            "-fx-text-fill: #2e7d32; " +
                            "-fx-background-radius: 12;"
                        );
                        badge.setText("✓ BUKA");
                    } else {
                        badge.setStyle(
                            "-fx-background-color: #ffebee; " +
                            "-fx-text-fill: #c62828; " +
                            "-fx-background-radius: 12;"
                        );
                        badge.setText("✕ TUTUP");
                    }
                    
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        table.getColumns().addAll(colId, colTipe, colHari, colBuka, colTutup, colStatus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }

    private String formatTime(Time time) {
        if (time == null) return "";
        return time.toString().substring(0, 5);
    }

    private void loadData(String keyword) {
        List<JamOperasional> list = jamOperasionalDAO.getAllJamOperasional();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            String filter = keyword.toLowerCase();
            list = list.stream()
                .filter(j -> j.getTipeLayanan().toLowerCase().contains(filter) ||
                             j.getHari().toLowerCase().contains(filter) ||
                             j.getStatus().toLowerCase().contains(filter))
                .collect(Collectors.toList());
        }

        this.jamList = FXCollections.observableArrayList(list);
        tableView.setItems(this.jamList);
    }

    private void showAddDialog() {
        JamOperasionalFormDialog dialog = new JamOperasionalFormDialog(null);
        boolean success = dialog.showAndWait();
        if (success) {
            loadData("");
        }
    }

    private void showEditDialog() {
        JamOperasional selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih jam operasional yang akan diedit terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }
        JamOperasionalFormDialog dialog = new JamOperasionalFormDialog(selected);
        boolean success = dialog.showAndWait();
        if (success) {
            loadData("");
        }
    }

    private void closeJamOperasional() {
        JamOperasional selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showModernAlert("⚠️ Perhatian", "Pilih jam operasional yang akan ditutup terlebih dahulu!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Tutup");
        confirm.setHeaderText("Tutup Jam Operasional");
        confirm.setContentText("Apakah Anda yakin ingin menutup jam operasional \"" + selected.getHari() + " - " + selected.getTipeLayanan() + "\"?");
        
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (jamOperasionalDAO.updateJamOperasional(
                    selected.getTipeLayanan(),
                    selected.getHari(),
                    selected.getJamBuka(),
                    selected.getJamTutup(),
                    "tutup"
                )) {
                    showModernAlert("✅ Berhasil", "Jam operasional berhasil ditutup!", Alert.AlertType.INFORMATION);
                    loadData("");
                } else {
                    showModernAlert("❌ Error", "Gagal menutup jam operasional!", Alert.AlertType.ERROR);
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
    public static class JamOperasionalFormDialog {
        private JamOperasional jam;
        private boolean success = false;

        public JamOperasionalFormDialog(JamOperasional jam) {
            this.jam = jam;
        }

        public boolean showAndWait() {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(jam == null ? "Tambah Jam Operasional" : "Edit Jam Operasional");

            VBox root = new VBox(25);
            root.setPadding(new Insets(30));
            root.setStyle("-fx-background-color: #f8f9fa;");

            // Header
            VBox header = new VBox(8);
            Label titleLabel = new Label(jam == null ? "🕐 Tambah Jam Operasional" : "✏️ Edit Jam Operasional");
            titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
            titleLabel.setTextFill(Color.web("#2c3e50"));
            
            Label subtitleLabel = new Label("Atur jadwal operasional toko");
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

            // Tipe Layanan
            VBox tipeBox = createFormField("Tipe Layanan");
            ComboBox<String> cbTipe = new ComboBox<>();
            cbTipe.getItems().addAll("web", "desktop");
            cbTipe.setPromptText("Pilih tipe layanan");
            cbTipe.setMaxWidth(Double.MAX_VALUE);
            styleComboBox(cbTipe);
            tipeBox.getChildren().add(cbTipe);

            // Hari
            VBox hariBox = createFormField("Hari");
            ComboBox<String> cbHari = new ComboBox<>();
            cbHari.getItems().addAll("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu");
            cbHari.setPromptText("Pilih hari");
            cbHari.setMaxWidth(Double.MAX_VALUE);
            styleComboBox(cbHari);
            hariBox.getChildren().add(cbHari);

            // Jam Buka
            VBox bukaBox = createFormField("Jam Buka (HH:MM)");
            TextField txtBuka = createStyledTextField("10:00");
            bukaBox.getChildren().add(txtBuka);

            // Jam Tutup
            VBox tutupBox = createFormField("Jam Tutup (HH:MM)");
            TextField txtTutup = createStyledTextField("17:00");
            tutupBox.getChildren().add(txtTutup);

            // Status
            VBox statusBox = createFormField("Status");
            ComboBox<String> cbStatus = new ComboBox<>();
            cbStatus.getItems().addAll("buka", "tutup");
            cbStatus.setPromptText("Pilih status");
            cbStatus.setMaxWidth(Double.MAX_VALUE);
            styleComboBox(cbStatus);
            statusBox.getChildren().add(cbStatus);

            // Load existing data
            if (jam != null) {
                cbTipe.setValue(jam.getTipeLayanan());
                cbHari.setValue(jam.getHari());
                cbStatus.setValue(jam.getStatus());
                if (jam.getJamBuka() != null) txtBuka.setText(formatTime(jam.getJamBuka()));
                if (jam.getJamTutup() != null) txtTutup.setText(formatTime(jam.getJamTutup()));
            }

            formContainer.getChildren().addAll(tipeBox, hariBox, bukaBox, tutupBox, statusBox);

            // Buttons
            HBox buttonBox = new HBox(12);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(15, 0, 0, 0));

            Button btnCancel = new Button("Batal");
            styleButton(btnCancel, "#95a5a6");
            btnCancel.setOnAction(e -> dialog.close());

            Button btnSave = new Button(jam == null ? "➕ Tambah" : "💾 Simpan");
            styleButton(btnSave, "#3498db");

            btnSave.setOnAction(e -> {
                String tipe = cbTipe.getValue();
                String hari = cbHari.getValue();
                String buka = txtBuka.getText().trim();
                String tutup = txtTutup.getText().trim();
                String status = cbStatus.getValue();

                if (tipe == null || hari == null || status == null || buka.isEmpty() || tutup.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("⚠️ Perhatian");
                    alert.setHeaderText("Data Tidak Lengkap");
                    alert.setContentText("Semua field wajib diisi!");
                    alert.showAndWait();
                    return;
                }

                if (!isValidTime(buka) || !isValidTime(tutup)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("❌ Error");
                    alert.setHeaderText("Format Waktu Salah");
                    alert.setContentText("Format jam harus HH:MM (contoh: 10:00)");
                    alert.showAndWait();
                    return;
                }

                try {
                    Time jamBuka = Time.valueOf(buka + ":00");
                    Time jamTutup = Time.valueOf(tutup + ":00");

                    boolean result = new JamOperasionalDAO().updateJamOperasional(
                        tipe, hari, jamBuka, jamTutup, status
                    );

                    if (result) {
                        success = true;
                        dialog.close();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("✅ Berhasil");
                        alert.setHeaderText("Jam Operasional Disimpan");
                        alert.setContentText("Jam operasional berhasil disimpan!");
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("❌ Error");
                        alert.setHeaderText("Gagal Menyimpan");
                        alert.setContentText("Terjadi kesalahan saat menyimpan!");
                        alert.showAndWait();
                    }
                } catch (IllegalArgumentException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("❌ Error");
                    alert.setHeaderText("Format Waktu Tidak Valid");
                    alert.setContentText("Pastikan format waktu benar (HH:MM)");
                    alert.showAndWait();
                }
            });

            buttonBox.getChildren().addAll(btnCancel, btnSave);
            root.getChildren().addAll(header, formContainer, buttonBox);

            Scene scene = new Scene(root, 500, 620);
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();
            return success;
        }

        private VBox createFormField(String labelText) {
            VBox box = new VBox(8);
            Label label = new Label(labelText);
            label.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 13));
            label.setTextFill(Color.web("#2c3e50"));
            box.getChildren().add(label);
            return box;
        }

        private TextField createStyledTextField(String placeholder) {
            TextField field = new TextField();
            field.setPromptText(placeholder);
            field.setFont(Font.font("Segoe UI", 13));
            field.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #e1e8ed; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-padding: 12 15;"
            );
            field.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    field.setStyle(
                        "-fx-background-color: white; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #3498db; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 10; " +
                        "-fx-padding: 12 15;"
                    );
                } else {
                    field.setStyle(
                        "-fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #e1e8ed; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 10; " +
                        "-fx-padding: 12 15;"
                    );
                }
            });
            return field;
        }

        private void styleComboBox(ComboBox<String> cb) {
            cb.setStyle(
                "-fx-background-color: #f8f9fa; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #e1e8ed; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10;"
            );
        }

        private void styleButton(Button btn, String color) {
            btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            btn.setPadding(new Insets(12, 30, 12, 30));
            btn.setStyle(
                "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;"
            );
        }

        private boolean isValidTime(String time) {
            if (time == null || time.length() != 5) return false;
            if (!time.matches("\\d{2}:\\d{2}")) return false;
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        }

        private String formatTime(Time time) {
            if (time == null) return "";
            return time.toString().substring(0, 5);
        }
    }
}