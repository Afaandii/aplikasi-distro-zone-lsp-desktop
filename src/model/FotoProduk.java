package model;

import java.sql.Timestamp;

public class FotoProduk {
    private Long idFotoProduk;
    private Long idProduk;
    private Long idWarna;
    private String urlFoto;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional info from joins
    private String namaProduk;
    private String namaWarna;
    
    // Constructors
    public FotoProduk() {}
    
    public FotoProduk(Long idFotoProduk, Long idProduk, Long idWarna, String urlFoto) {
        this.idFotoProduk = idFotoProduk;
        this.idProduk = idProduk;
        this.idWarna = idWarna;
        this.urlFoto = urlFoto;
    }
    
    // Getters and Setters
    public Long getIdFotoProduk() { return idFotoProduk; }
    public void setIdFotoProduk(Long idFotoProduk) { this.idFotoProduk = idFotoProduk; }
    
    public Long getIdProduk() { return idProduk; }
    public void setIdProduk(Long idProduk) { this.idProduk = idProduk; }
    
    public Long getIdWarna() { return idWarna; }
    public void setIdWarna(Long idWarna) { this.idWarna = idWarna; }
    
    public String getUrlFoto() { return urlFoto; }
    public void setUrlFoto(String urlFoto) { this.urlFoto = urlFoto; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }
    
    public String getNamaWarna() { return namaWarna; }
    public void setNamaWarna(String namaWarna) { this.namaWarna = namaWarna; }
}