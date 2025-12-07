package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import service.ReservationManager;
import model.Reservation;

public class AdminReservationManagerPage extends JFrame {

    private DefaultTableModel model;
    private JTable table;

    public AdminReservationManagerPage() {

        setTitle("Admin - Rezervasyon Yönetimi");
        setSize(1050, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ✅ ODA NO SÜTUNU EKLENDİ
        String[] columns = {"Müşteri", "Oda", "Oda No", "Kişi Sayısı", "Giriş", "Çıkış", "Fiyat", "Durum"};
        model = new DefaultTableModel(columns, 0);

        table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panel = new JPanel();

        JButton btnOnayla   = new JButton("Onayla");
        JButton btnReddet   = new JButton("Reddet");
        JButton btnIptal    = new JButton("İptal Et");
        JButton btnDetay    = new JButton("Detay Göster");
        JButton btnGuncelle = new JButton("Rezervasyon Güncelle");
        JButton btnYenile   = new JButton("Yenile");

        panel.add(btnOnayla);
        panel.add(btnReddet);
        panel.add(btnIptal);
        panel.add(btnDetay);
        panel.add(btnGuncelle);
        panel.add(btnYenile);

        add(panel, BorderLayout.SOUTH);

        tabloyuDoldur();

        btnOnayla.addActionListener(e -> durumDegistir("Onaylandı", false));
        btnReddet.addActionListener(e -> durumDegistir("Reddedildi", true));
        btnIptal.addActionListener(e -> durumDegistir("İptal Edildi", true));
        btnDetay.addActionListener(e -> detayGoster());
        btnGuncelle.addActionListener(e -> rezervasyonGuncelle());
        btnYenile.addActionListener(e -> tabloyuDoldur());
    }

    // ----------------------------------------------------
    private Reservation getSecili() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Rezervasyon seçiniz!");
            return null;
        }
        return ReservationManager.getReservations().get(row);
    }

    // ✅ ODA NO DA TABLOYA EKLENDİ
    private void tabloyuDoldur() {
        model.setRowCount(0);
        for (Reservation r : ReservationManager.getReservations()) {
            model.addRow(new Object[]{
                    r.getMusteriAdi(),
                    r.getOdaTipi(),
                    r.getOdaNo(),          // ✅ ODA NUMARASI
                    r.getKisiSayisi(),
                    r.getGirisTarihi(),
                    r.getCikisTarihi(),
                    r.getFiyat(),
                    r.getDurum()
            });
        }
    }

    // ✅ İPTAL / RED DURUMUNDA ODA NO HAVUZA GERİ GİDER
    private void durumDegistir(String durum, boolean odaIade) {
        Reservation r = getSecili();
        if (r == null) return;

        if (odaIade) {
            ReservationManager.odaIade(r.getOdaTipi(), r.getOdaNo());
        }

        r.setDurum(durum);
        ReservationManager.saveToFile();
        tabloyuDoldur();
    }

    // ----------------------------------------------------
    //   DETAY GÖSTER  (ODA NO DA DAHİL)
    // ----------------------------------------------------
    private void detayGoster() {
        Reservation r = getSecili();
        if (r == null) return;

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Arial", Font.PLAIN, 14));

        String detay =
                "Müşteri: " + r.getMusteriAdi() +
                "\nOda Tipi: " + r.getOdaTipi() +
                "\nOda No: " + r.getOdaNo() +          // ✅ ODA NUMARASI
                "\nKişi Sayısı: " + r.getKisiSayisi() +
                "\nGiriş: " + r.getGirisTarihi() +
                "\nÇıkış: " + r.getCikisTarihi() +
                "\nFiyat: " + r.getFiyat() +
                "\nDurum: " + r.getDurum() +
                "\n\n--- KİŞİLER ---\n" +
                r.getKisiler();

        area.setText(detay);

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, sp, "Rezervasyon Detayı", JOptionPane.INFORMATION_MESSAGE);
    }

    // ----------------------------------------------------
    //   YARDIMCI: kisiler String'ini listeye çevir
    // ----------------------------------------------------
    private List<String> parseKisiler(String kisilerStr) {
        List<String> list = new ArrayList<>();
        if (kisilerStr == null) return list;

        String[] satirlar = kisilerStr.split("\n");
        for (String s : satirlar) {
            if (s != null && !s.trim().isEmpty()) {
                list.add(s.trim());
            }
        }
        return list;
    }

    // ----------------------------------------------------
    //   REZERVASYON GÜNCELLE  (KİŞİ EKLE / SİL / TARİH)
    // ----------------------------------------------------
    private void rezervasyonGuncelle() {
        Reservation r = getSecili();
        if (r == null) return;

        String[] secenekler = {"Kişi Ekle", "Kişi Sil", "Tarih Güncelle"};
        int secim = JOptionPane.showOptionDialog(
                this,
                "Ne yapmak istiyorsun?",
                "Rezervasyon Güncelle",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                secenekler,
                secenekler[0]
        );

        if (secim == JOptionPane.CLOSED_OPTION) {
            return;
        }

        List<String> kisiList = parseKisiler(r.getKisiler());

        // ✅ KİŞİ EKLE
        if (secim == 0) {
            String ad    = JOptionPane.showInputDialog(this, "Ad Soyad:");
            if (ad == null || ad.isEmpty()) return;

            String tc    = JOptionPane.showInputDialog(this, "TC:");
            if (tc == null || tc.isEmpty()) return;

            String dogum = JOptionPane.showInputDialog(this, "Doğum Tarihi:");
            if (dogum == null || dogum.isEmpty()) return;

            String yeniKisi = "Ad Soyad: " + ad + " | TC: " + tc + " | Doğum: " + dogum;
            kisiList.add(yeniKisi);
            r.setKisiSayisi(r.getKisiSayisi() + 1);
        }

        // ✅ KİŞİ SİL
        else if (secim == 1) {

            if (kisiList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Silinecek kişi yok.");
                return;
            }

            String[] kisilerArray = kisiList.toArray(new String[0]);

            String secilen = (String) JOptionPane.showInputDialog(
                    this,
                    "Silinecek kişiyi seç:",
                    "Kişi Sil",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    kisilerArray,
                    kisilerArray[0]
            );

            if (secilen == null) return;

            kisiList.remove(secilen);
            r.setKisiSayisi(kisiList.size());
        }

        // ✅ TARİH GÜNCELLE
        else if (secim == 2) {

            String giris = JOptionPane.showInputDialog(this, "Yeni giriş tarihi:", r.getGirisTarihi());
            if (giris == null || giris.isEmpty()) return;

            String cikis = JOptionPane.showInputDialog(this, "Yeni çıkış tarihi:", r.getCikisTarihi());
            if (cikis == null || cikis.isEmpty()) return;

            r.setGirisTarihi(giris);
            r.setCikisTarihi(cikis);
        }

        StringBuilder sb = new StringBuilder();
        for (String satir : kisiList) {
            sb.append(satir).append("\n");
        }
        r.setKisiler(sb.toString());

        ReservationManager.saveToFile();
        tabloyuDoldur();

        JOptionPane.showMessageDialog(this, "Güncelleme kaydedildi.");
    }
}
