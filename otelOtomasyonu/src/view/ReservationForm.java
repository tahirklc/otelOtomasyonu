package view;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import service.ReservationManager;
import model.Reservation;

public class ReservationForm extends JFrame {

    private JComboBox<String> odaSecim;
    private JComboBox<Integer> kisiSayisiSecim;

    private JTextField girisTarihi;
    private JTextField cikisTarihi;

    private JPanel kisiPanel;  
    private JLabel fiyatLabel;

    private String kullaniciAdi;

    public ReservationForm(String kullaniciAdi) {
        this.kullaniciAdi = kullaniciAdi;

        setTitle("Rezervasyon Oluştur");
        setSize(500, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Oda tipi
        JLabel odaLbl = new JLabel("Oda Tipi:");
        odaLbl.setBounds(30, 30, 150, 30);
        add(odaLbl);

        odaSecim = new JComboBox<>(new String[]{
                "Standart Oda - 1000 TL",
                "Deluxe Oda - 2000 TL",
                "Kral Dairesi - 5000 TL"
        });
        odaSecim.setBounds(200, 30, 220, 30);
        odaSecim.addActionListener(e -> fiyatHesapla());
        add(odaSecim);

        // Kişi sayısı
        JLabel kisiSayLbl = new JLabel("Kişi Sayısı:");
        kisiSayLbl.setBounds(30, 80, 150, 30);
        add(kisiSayLbl);

        kisiSayisiSecim = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        kisiSayisiSecim.setBounds(200, 80, 220, 30);
        kisiSayisiSecim.addActionListener(e -> {
            kisiAlanlariniOlustur();
            fiyatHesapla();
        });
        add(kisiSayisiSecim);

        // Dinamik kişi paneli
        kisiPanel = new JPanel();
        kisiPanel.setLayout(new GridLayout(0, 1, 5, 5));
        kisiPanel.setBackground(Color.WHITE);

        JScrollPane kisiScroll = new JScrollPane(kisiPanel);
        kisiScroll.setBounds(30, 130, 420, 150);
        add(kisiScroll);

        kisiAlanlariniOlustur();

        // Tarihler
        JLabel girisLbl = new JLabel("Giriş Tarihi (dd.MM.yyyy):");
        girisLbl.setBounds(30, 300, 180, 30);
        add(girisLbl);

        girisTarihi = new JTextField("01.01.2025");
        girisTarihi.setBounds(200, 300, 220, 30);
        girisTarihi.addActionListener(e -> fiyatHesapla());
        add(girisTarihi);

        JLabel cikisLbl = new JLabel("Çıkış Tarihi:");
        cikisLbl.setBounds(30, 340, 150, 30);
        add(cikisLbl);

        cikisTarihi = new JTextField("05.01.2025");
        cikisTarihi.setBounds(200, 340, 220, 30);
        cikisTarihi.addActionListener(e -> fiyatHesapla());
        add(cikisTarihi);

        // Fiyat
        JLabel fiyatTxt = new JLabel("Toplam Fiyat:");
        fiyatTxt.setBounds(30, 390, 150, 30);
        add(fiyatTxt);

        fiyatLabel = new JLabel("0 TL");
        fiyatLabel.setFont(new Font("Arial", Font.BOLD, 16));
        fiyatLabel.setBounds(200, 390, 200, 30);
        add(fiyatLabel);

        JButton fiyatHesaplaBtn = new JButton("Fiyat Hesapla");
        fiyatHesaplaBtn.setBounds(30, 430, 150, 35);
        fiyatHesaplaBtn.addActionListener(e -> fiyatHesapla());
        add(fiyatHesaplaBtn);

        // Oluştur butonu
        JButton olusturBtn = new JButton("Rezervasyon Oluştur");
        olusturBtn.setBounds(200, 480, 220, 40);
        olusturBtn.addActionListener(e -> rezervasyonOlustur());
        add(olusturBtn);
    }

    // --- Gün sayısı hesaplama ---
    private long gunSayisiHesapla() {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate giris = LocalDate.parse(girisTarihi.getText(), fmt);
            LocalDate cikis = LocalDate.parse(cikisTarihi.getText(), fmt);
            long days = ChronoUnit.DAYS.between(giris, cikis);
            return Math.max(days, 1);
        } catch (Exception e) {
            return 1;
        }
    }

    // --- Dinamik kişi alanları ---
    private void kisiAlanlariniOlustur() {
        kisiPanel.removeAll();

        int kisiSayisi = (int) kisiSayisiSecim.getSelectedItem();

        for (int i = 1; i <= kisiSayisi; i++) {
            JPanel p = new JPanel();
            p.setLayout(new GridLayout(3, 2, 5, 5));
            p.setBorder(BorderFactory.createTitledBorder(i + ". Kişi"));

            p.add(new JLabel("Ad Soyad:"));
            JTextField adSoyad = new JTextField();
            adSoyad.putClientProperty("type", "ad");
            p.add(adSoyad);

            p.add(new JLabel("TC Kimlik:"));
            JTextField tc = new JTextField();
            tc.putClientProperty("type", "tc");
            p.add(tc);

            p.add(new JLabel("Doğum Tarihi:"));
            JTextField dogum = new JTextField("01.01.2000");
            dogum.putClientProperty("type", "dogum");
            p.add(dogum);

            kisiPanel.add(p);
        }

        kisiPanel.revalidate();
        kisiPanel.repaint();
    }

    // --- Kişi bilgilerini toplama ---
    private String kisileriTopla() {
        StringBuilder sb = new StringBuilder();

        for (Component c : kisiPanel.getComponents()) {
            if (c instanceof JPanel p) {

                String ad = "", tc = "", dogum = "";

                for (Component input : p.getComponents()) {
                    if (input instanceof JTextField text) {
                        switch ((String) text.getClientProperty("type")) {
                            case "ad" -> ad = text.getText();
                            case "tc" -> tc = text.getText();
                            case "dogum" -> dogum = text.getText();
                        }
                    }
                }

                sb.append("Ad Soyad: ").append(ad)
                        .append(" | TC: ").append(tc)
                        .append(" | Doğum: ").append(dogum)
                        .append("\n");
            }
        }

        return sb.toString();
    }

    // --- Fiyat Hesaplama ---
    private void fiyatHesapla() {

        String oda = (String) odaSecim.getSelectedItem();
        int kisi = (int) kisiSayisiSecim.getSelectedItem();
        long gun = gunSayisiHesapla();

        int birimFiyat = 0;
        if (oda.contains("Standart")) birimFiyat = 1000;
        else if (oda.contains("Deluxe")) birimFiyat = 2000;
        else if (oda.contains("Kral")) birimFiyat = 5000;

        double toplam = birimFiyat * kisi * gun;
        fiyatLabel.setText(toplam + " TL");
    }

    // ✅✅✅ ODA NUMARALI REZERVASYON OLUŞTURMA ✅✅✅
    private void rezervasyonOlustur() {

        String oda = (String) odaSecim.getSelectedItem();

        if (!ReservationManager.odaBosMu(oda)) {
            JOptionPane.showMessageDialog(this,
                    "Bu oda türünde boş oda kalmamıştır!",
                    "Oda Dolu",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int kisiSayisi = (int) kisiSayisiSecim.getSelectedItem();
        String kisiler = kisileriTopla();
        String giris = girisTarihi.getText();
        String cikis = cikisTarihi.getText();
        double fiyat = Double.parseDouble(fiyatLabel.getText().replace(" TL", ""));

        // ❌ ARTIK ODA AYIRMA YOK!
        Reservation r = new Reservation(
                kullaniciAdi,
                oda,
                kisiSayisi,
                kisiler,
                giris,
                cikis,
                fiyat
        );

        // ✅ BU SATIR ODA NUMARASINI OTOMATİK ATAR
        ReservationManager.addReservation(r);

        // ✅ ATANAN ODA NUMARASINI KULLANICIYA GÖSTER
        JOptionPane.showMessageDialog(this,
                "Rezervasyon başarıyla oluşturuldu!\n" +
                "Size atanan oda numarası: " + r.getOdaNo());

        dispose();
    }
}
