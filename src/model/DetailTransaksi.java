package model;

import java.sql.Timestamp;

public class DetailTransaksi {
    private Long idDetailTransaksi;
    private Long idTransaksi;
    private Long idProduk;
    private Long jumlah;
    private Long hargaSatuan;
    private Long subtotal;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional info
    private String namaProduk;
    
    // Constructors
    public DetailTransaksi() {}
    
    public DetailTransaksi(Long idTransaksi, Long idProduk, Long jumlah, Long hargaSatuan, Long subtotal) {
        this.idTransaksi = idTransaksi;
        this.idProduk = idProduk;
        this.jumlah = jumlah;
        this.hargaSatuan = hargaSatuan;
        this.subtotal = subtotal;
    }
    
    // Getters and Setters
    public Long getIdDetailTransaksi() { return idDetailTransaksi; }
    public void setIdDetailTransaksi(Long idDetailTransaksi) { this.idDetailTransaksi = idDetailTransaksi; }
    
    public Long getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(Long idTransaksi) { this.idTransaksi = idTransaksi; }
    
    public Long getIdProduk() { return idProduk; }
    public void setIdProduk(Long idProduk) { this.idProduk = idProduk; }
    
    public Long getJumlah() { return jumlah; }
    public void setJumlah(Long jumlah) { this.jumlah = jumlah; }
    
    public Long getHargaSatuan() { return hargaSatuan; }
    public void setHargaSatuan(Long hargaSatuan) { this.hargaSatuan = hargaSatuan; }
    
    public Long getSubtotal() { return subtotal; }
    public void setSubtotal(Long subtotal) { this.subtotal = subtotal; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }
}