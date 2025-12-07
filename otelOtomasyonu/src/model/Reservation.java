package model;

public class Reservation {

    private String musteriAdi;
    private String odaTipi;
    private int odaNo;              // ✅ YENİ: Oda numarası (101, 201, 301...)
    private int kisiSayisi;
    private String kisiler;
    private String girisTarihi;
    private String cikisTarihi;
    private double fiyat;
    private String durum;

    // ✅ GÜNCELLENMİŞ CONSTRUCTOR (odaNo SONRADAN set edilecek)
    public Reservation(String musteriAdi, String odaTipi, int kisiSayisi,
                       String kisiler, String girisTarihi, String cikisTarihi,
                       double fiyat) {

        this.musteriAdi = musteriAdi;
        this.odaTipi = odaTipi;
        this.kisiSayisi = kisiSayisi;
        this.kisiler = kisiler;
        this.girisTarihi = girisTarihi;
        this.cikisTarihi = cikisTarihi;
        this.fiyat = fiyat;
        this.durum = "Bekliyor";
        this.odaNo = -1; // ✅ Henüz atanmadı (ReservationManager atayacak)
    }

    // -------- GETTERLAR --------
    public String getMusteriAdi() { return musteriAdi; }
    public String getOdaTipi() { return odaTipi; }
    public int getOdaNo() { return odaNo; }                 // ✅ YENİ
    public int getKisiSayisi() { return kisiSayisi; }
    public String getKisiler() { return kisiler; }
    public String getGirisTarihi() { return girisTarihi; }
    public String getCikisTarihi() { return cikisTarihi; }
    public double getFiyat() { return fiyat; }
    public String getDurum() { return durum; }

    // -------- SETTERLAR --------
    public void setDurum(String durum) {
        this.durum = durum;
    }

    public void setKisiSayisi(int kisiSayisi) {
        this.kisiSayisi = kisiSayisi;
    }

    public void setGirisTarihi(String girisTarihi) {
        this.girisTarihi = girisTarihi;
    }

    public void setCikisTarihi(String cikisTarihi) {
        this.cikisTarihi = cikisTarihi;
    }

    public void setFiyat(double fiyat) {
        this.fiyat = fiyat;
    }

    // ✅ SORUNU ÇÖZEN METOT (KİŞİ GÜNCELLEME)
    public void setKisiler(String kisiler) {
        this.kisiler = kisiler;
    }

    // ✅ YENİ: ODA NUMARASI SETTER
    public void setOdaNo(int odaNo) {
        this.odaNo = odaNo;
    }
}
