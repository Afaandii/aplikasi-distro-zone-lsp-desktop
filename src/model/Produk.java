package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Produk {
    private Long idProduk;
    private Long idMerk;
    private Long idTipe;
    private String namaKaos;
    private Long hargaJual;
    private Long hargaPokok;
    private String deskripsi;
    private String spesifikasi;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private BigDecimal berat;
    
    // Relasi
    private String namaMerk;
    private String namaTipe;
    
    public Produk() {}
    
    public Long getIdProduk() { return idProduk; }
    public void setIdProduk(Long idProduk) { this.idProduk = idProduk; }
    
    public Long getIdMerk() { return idMerk; }
    public void setIdMerk(Long idMerk) { this.idMerk = idMerk; }
    
    public Long getIdTipe() { return idTipe; }
    public void setIdTipe(Long idTipe) { this.idTipe = idTipe; }
    
    public String getNamaKaos() { return namaKaos; }
    public void setNamaKaos(String namaKaos) { this.namaKaos = namaKaos; }
    
    public Long getHargaJual() { return hargaJual; }
    public void setHargaJual(Long hargaJual) { this.hargaJual = hargaJual; }
    
    public Long getHargaPokok() { return hargaPokok; }
    public void setHargaPokok(Long hargaPokok) { this.hargaPokok = hargaPokok; }
    
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    
    public String getSpesifikasi() { return spesifikasi; }
    public void setSpesifikasi(String spesifikasi) { this.spesifikasi = spesifikasi; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public BigDecimal getBerat() { return berat; }
    public void setBerat(BigDecimal berat) { this.berat = berat; }
    
    public String getNamaMerk() { return namaMerk; }
    public void setNamaMerk(String namaMerk) { this.namaMerk = namaMerk; }
    
    public String getNamaTipe() { return namaTipe; }
    public void setNamaTipe(String namaTipe) { this.namaTipe = namaTipe; }
    
    @Override
    public String toString() {
        return namaKaos;
    }
}