package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class Struk {

    private Stage stage;
    private VBox strukContainer;
    private Button btnCetakPDF;
    private Button btnTutup;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public Struk(String noTransaksi, String namaKasir, double totalBayar,
                 double uangBayar, double kembalian,
                 String metode, boolean isTunai,
                 List<DashboardKasir.ItemKeranjang> items) {

        initUI(noTransaksi, namaKasir, totalBayar, uangBayar, kembalian, metode, isTunai, items);
    }

    private void initUI(String noTransaksi, String namaKasir, double totalBayar,
                       double uangBayar, double kembalian,
                       String metode, boolean isTunai,
                       List<DashboardKasir.ItemKeranjang> items) {
        
        stage = new Stage();
        stage.setTitle("Struk Transaksi");
        stage.initModality(Modality.APPLICATION_MODAL);

        strukContainer = new VBox(5);
        strukContainer.setPadding(new Insets(20));
        strukContainer.setStyle("-fx-background-color: white;");

        // Header - Nama Toko
        Label lblNamaToko = new Label("DISTROZONE");
        lblNamaToko.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblNamaToko.setAlignment(Pos.CENTER);
        lblNamaToko.setMaxWidth(Double.MAX_VALUE);

        // Info Transaksi
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER);
        
        Label lblNoTransaksi = new Label("No. Transaksi: " + noTransaksi);
        lblNoTransaksi.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        String tanggal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Label lblTanggal = new Label(tanggal);
        lblTanggal.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        Label lblKasir = new Label("Kasir: " + namaKasir);
        lblKasir.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        infoBox.getChildren().addAll(lblNoTransaksi, lblTanggal, lblKasir);

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-style: dashed;");

        // Items
        VBox itemsBox = new VBox(10);
        itemsBox.setPadding(new Insets(10, 0, 10, 0));
        
        for (DashboardKasir.ItemKeranjang item : items) {
            VBox itemBox = new VBox(3);
            
            // Nama produk
            Label lblNama = new Label(item.getNamaProduk());
            lblNama.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            lblNama.setWrapText(true);
            
            // Detail (warna, ukuran, qty, harga satuan)
            HBox detailBox = new HBox();
            Label lblDetail = new Label(item.getWarna() + " | " + item.getUkuran() + 
                                       " | " + item.getQuantity() + "x");
            lblDetail.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Label lblHargaSatuan = new Label(currencyFormat.format(item.getHarga()));
            lblHargaSatuan.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
            
            detailBox.getChildren().addAll(lblDetail, spacer, lblHargaSatuan);
            
            // Subtotal
            HBox subtotalBox = new HBox();
            Label lblSubtotalLabel = new Label("Subtotal:");
            lblSubtotalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");
            
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            
            Label lblSubtotal = new Label(currencyFormat.format(item.getSubtotal()));
            lblSubtotal.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
            
            subtotalBox.getChildren().addAll(lblSubtotalLabel, spacer2, lblSubtotal);
            
            itemBox.getChildren().addAll(lblNama, detailBox, subtotalBox);
            itemsBox.getChildren().add(itemBox);
        }

        Separator sep2 = new Separator();
        sep2.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-style: dashed;");

        // Total Section
        VBox totalBox = new VBox(8);
        totalBox.setPadding(new Insets(10, 0, 10, 0));
        
        HBox totalRow = new HBox();
        Label lblTotalLabel = new Label("TOTAL");
        lblTotalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        
        Label lblTotal = new Label(currencyFormat.format(totalBayar));
        lblTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        
        totalRow.getChildren().addAll(lblTotalLabel, spacer3, lblTotal);
        totalBox.getChildren().add(totalRow);

        // Pembayaran
        if (isTunai) {
            HBox bayarRow = new HBox();
            Label lblBayarLabel = new Label("Bayar");
            lblBayarLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
            
            Region spacer4 = new Region();
            HBox.setHgrow(spacer4, Priority.ALWAYS);
            
            Label lblBayar = new Label(currencyFormat.format(uangBayar));
            lblBayar.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
            
            bayarRow.getChildren().addAll(lblBayarLabel, spacer4, lblBayar);
            
            HBox kembalianRow = new HBox();
            Label lblKembalianLabel = new Label("Kembalian");
            lblKembalianLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
            
            Region spacer5 = new Region();
            HBox.setHgrow(spacer5, Priority.ALWAYS);
            
            Label lblKembalian = new Label(currencyFormat.format(kembalian));
            lblKembalian.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
            
            kembalianRow.getChildren().addAll(lblKembalianLabel, spacer5, lblKembalian);
            
            totalBox.getChildren().addAll(bayarRow, kembalianRow);
        } else {
            HBox metodeRow = new HBox();
            Label lblMetodeLabel = new Label("Metode Pembayaran");
            lblMetodeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
            
            Region spacer4 = new Region();
            HBox.setHgrow(spacer4, Priority.ALWAYS);
            
            Label lblMetode = new Label(metode);
            lblMetode.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
            
            metodeRow.getChildren().addAll(lblMetodeLabel, spacer4, lblMetode);
            totalBox.getChildren().add(metodeRow);
        }

        Separator sep3 = new Separator();
        sep3.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-style: dashed;");

        // Footer
        Label lblFooter = new Label("Terima kasih atas kunjungan Anda!");
        lblFooter.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        lblFooter.setAlignment(Pos.CENTER);
        lblFooter.setMaxWidth(Double.MAX_VALUE);

        // Add all to container
        strukContainer.getChildren().addAll(
            lblNamaToko,
            infoBox,
            sep1,
            itemsBox,
            sep2,
            totalBox,
            sep3,
            lblFooter
        );

        // Scroll pane untuk struk
        ScrollPane scrollPane = new ScrollPane(strukContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-background: white;");

        // Buttons
        btnCetakPDF = new Button("🖨️ Simpan sebagai PDF");
        btnCetakPDF.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                            "-fx-font-size: 13px; -fx-padding: 10 20; -fx-cursor: hand;");
        btnCetakPDF.setOnMouseEntered(e -> 
            btnCetakPDF.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; " +
                                "-fx-font-size: 13px; -fx-padding: 10 20; -fx-cursor: hand;"));
        btnCetakPDF.setOnMouseExited(e -> 
            btnCetakPDF.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                                "-fx-font-size: 13px; -fx-padding: 10 20; -fx-cursor: hand;"));
        btnCetakPDF.setOnAction(e -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setHeaderText(null);
            alert.setContentText("Fitur PDF akan diimplementasikan.");
            alert.showAndWait();
        });

        btnTutup = new Button("Tutup");
        btnTutup.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                         "-fx-font-size: 13px; -fx-padding: 10 20; -fx-cursor: hand;");
        btnTutup.setOnMouseEntered(e -> 
            btnTutup.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; " +
                             "-fx-font-size: 13px; -fx-padding: 10 20; -fx-cursor: hand;"));
        btnTutup.setOnMouseExited(e -> 
            btnTutup.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
                             "-fx-font-size: 13px; -fx-padding: 10 20; -fx-cursor: hand;"));
        btnTutup.setOnAction(e -> stage.close());

        HBox buttonBar = new HBox(15);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.setPadding(new Insets(15));
        buttonBar.setStyle("-fx-background-color: white;");
        buttonBar.getChildren().addAll(btnCetakPDF, btnTutup);

        VBox mainLayout = new VBox();
        mainLayout.setStyle("-fx-background-color: white;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        mainLayout.getChildren().addAll(scrollPane, buttonBar);

        Scene scene = new Scene(mainLayout, 450, 600);
        stage.setScene(scene);
    }

    public void show() {
        stage.showAndWait();
    }
}