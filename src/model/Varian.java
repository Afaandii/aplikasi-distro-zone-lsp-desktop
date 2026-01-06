package model;

import java.sql.Timestamp;

public class Varian {
    private Long idVarian;
    private Long idProduk;
    private Long idUkuran;
    private Long idWarna;
    private Long stokKaos;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Relasi
    private String namaUkuran;
    private String namaWarna;
    private String namaProduk;
    private Long hargaJual;
    private Long hargaPokok;
    
    public Varian() {}
    
    public Long getIdVarian() { return idVarian; }
    public void setIdVarian(Long idVarian) { this.idVarian = idVarian; }
    
    public Long getIdProduk() { return idProduk; }
    public void setIdProduk(Long idProduk) { this.idProduk = idProduk; }
    
    public Long getIdUkuran() { return idUkuran; }
    public void setIdUkuran(Long idUkuran) { this.idUkuran = idUkuran; }
    
    public Long getIdWarna() { return idWarna; }
    public void setIdWarna(Long idWarna) { this.idWarna = idWarna; }
    
    public Long getStokKaos() { return stokKaos; }
    public void setStokKaos(Long stokKaos) { this.stokKaos = stokKaos; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public String getNamaUkuran() { return namaUkuran; }
    public void setNamaUkuran(String namaUkuran) { this.namaUkuran = namaUkuran; }
    
    public String getNamaWarna() { return namaWarna; }
    public void setNamaWarna(String namaWarna) { this.namaWarna = namaWarna; }
    
    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }
    
    
    public Long getHargaJual() { return hargaJual; }
    public void setHargaJual(Long hargaJual) { this.hargaJual = hargaJual; }
    
    public Long getHargaPokok() { return hargaPokok; }
    public void setHargaPokok(Long hargaPokok) { this.hargaPokok = hargaPokok; }
    
    @Override
    public String toString() {
        return namaUkuran + " - " + namaWarna + " (Stok: " + stokKaos + ")";
    }
    
}