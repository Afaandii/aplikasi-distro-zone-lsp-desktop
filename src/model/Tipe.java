package model;

public class Tipe {
    private Long idTipe;
    private String namaTipe;
    private String keterangan;

    public Tipe() {
    }

    public Tipe(Long idTipe, String namaTipe, String keterangan) {
        this.idTipe = idTipe;
        this.namaTipe = namaTipe;
        this.keterangan = keterangan;
    }

    public Long getIdTipe() {
        return idTipe;
    }

    public void setIdTipe(Long idTipe) {
        this.idTipe = idTipe;
    }

    public String getNamaTipe() {
        return namaTipe;
    }

    public void setNamaTipe(String namaTipe) {
        this.namaTipe = namaTipe;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
    
    @Override
    public String toString() {
        return namaTipe;
    }
}
