package model;

public class Kullanici {

    private String kullaniciAdi;
    private String email;
    private String sifre;
    private String rol; // <-- BURASI EKLENDİ

    public Kullanici(String kullaniciAdi, String email, String sifre, String rol) {
        this.kullaniciAdi = kullaniciAdi;
        this.email = email;
        this.sifre = sifre;
        this.rol = rol;
    }

    public String getKullaniciAdi() { return kullaniciAdi; }
    public String getEmail() { return email; }
    public String getSifre() { return sifre; }

    public String getRol() { return rol; } // <-- EKLENEN METOD
}
