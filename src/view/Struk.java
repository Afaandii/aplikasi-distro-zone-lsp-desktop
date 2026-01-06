package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class Struk {

    private Stage stage;
    private Label lblStruk;
    private Button btnCetakPDF;
    private Button btnTutup;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public Struk(String noTransaksi, String namaKasir, double totalBayar,
                 double uangBayar, double kembalian,
                 String metode, boolean isTunai,
                 List<DashboardKasir.ItemKeranjang> items) {

        StringBuilder content = new StringBuilder();
        content.append("========================================\n");
        content.append("           DISTROZONE\n");
        content.append("========================================\n");
        content.append("No. Transaksi: ").append(noTransaksi).append("\n");
        content.append("Tanggal: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        content.append("Kasir: ").append(namaKasir).append("\n");
        content.append("========================================\n");

        for (DashboardKasir.ItemKeranjang item : items) {
            content.append(item.getNamaProduk()).append("\n")
                   .append("  ").append(item.getWarna()).append(" ").append(item.getUkuran())
                   .append(" x").append(item.getQuantity())
                   .append(" @ ").append(currencyFormat.format(item.getHarga())).append("\n")
                   .append("  Subtotal: ").append(currencyFormat.format(item.getSubtotal())).append("\n");
        }

        content.append("========================================\n");
        content.append("Total: ").append(currencyFormat.format(totalBayar)).append("\n");

        if (isTunai) {
            content.append("Bayar: ").append(currencyFormat.format(uangBayar)).append("\n");
            content.append("Kembalian: ").append(currencyFormat.format(kembalian)).append("\n");
        } else {
            content.append("Metode: ").append(metode).append("\n");
        }

        content.append("========================================\n");
        content.append("   Terima kasih atas kunjungan Anda!\n");
        content.append("========================================\n");

        initUI(content.toString());
    }

    private void initUI(String strukContent) {
        stage = new Stage();
        stage.setTitle("Struk Transaksi");
        stage.initModality(Modality.APPLICATION_MODAL);

        lblStruk = new Label(strukContent);
        lblStruk.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12; -fx-wrap-text: true;");
        lblStruk.setWrapText(true);

        btnCetakPDF = new Button("🖨️ Simpan sebagai PDF");
        btnCetakPDF.setOnAction(e -> {
            // TODO: Implementasi PDF
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setHeaderText(null);
            alert.setContentText("Fitur PDF akan diimplementasikan.");
            alert.showAndWait();
        });

        btnTutup = new Button("Tutup");
        btnTutup.setOnAction(e -> stage.close());

        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.getChildren().addAll(btnCetakPDF, btnTutup);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(lblStruk, buttonBar);

        Scene scene = new Scene(layout, 400, 500);
        stage.setScene(scene);
    }

    public void show() {
        stage.showAndWait();
    }
}