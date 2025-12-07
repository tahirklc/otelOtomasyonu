package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import service.ReservationManager;
import model.Reservation;

public class ActiveReservationsPage extends JFrame {

    private String kullaniciAdi;

    public ActiveReservationsPage(String kullaniciAdi) {
        this.kullaniciAdi = kullaniciAdi;

        setTitle("Aktif Rezervasyonlarım");
        setSize(900, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ✅ ODA NO SÜTUNU EKLENDİ
        String[] columns = {"Oda Tipi", "Oda No", "Kişi Sayısı", "Giriş", "Çıkış", "Fiyat", "Durum"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // ✅ SADECE AKTİF REZERVASYONLAR (Bekliyor + Onaylandı)
        for (Reservation r : ReservationManager.getReservations()) {
            if (r.getMusteriAdi().equals(kullaniciAdi)) {
                if (r.getDurum().equals("Bekliyor") || r.getDurum().equals("Onaylandı")) {
                    model.addRow(new Object[]{
                            r.getOdaTipi(),
                            r.getOdaNo(),         // ✅ ODA NUMARASI
                            r.getKisiSayisi(),
                            r.getGirisTarihi(),
                            r.getCikisTarihi(),
                            r.getFiyat(),
                            r.getDurum()
                    });
                }
            }
        }

        // ✅ TABLO DÜZENLENEMEZ
        JTable table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // ✅ SATIRA TIKLANINCA DETAY + İPTAL
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                int row = table.getSelectedRow();
                if (row == -1) return;

                String odaTipi = model.getValueAt(row, 0).toString();
                int odaNo      = Integer.parseInt(model.getValueAt(row, 1).toString());
                String kisiSay = model.getValueAt(row, 2).toString();
                String giris   = model.getValueAt(row, 3).toString();
                String cikis   = model.getValueAt(row, 4).toString();
                String fiyat   = model.getValueAt(row, 5).toString();
                String durum   = model.getValueAt(row, 6).toString();

                String[] options = {"İptal Et", "Kapat"};
                int secim = JOptionPane.showOptionDialog(
                        null,
                        "Oda Tipi: " + odaTipi +
                                "\nOda No: " + odaNo +        // ✅ ODA NUMARASI
                                "\nKişi Sayısı: " + kisiSay +
                                "\nGiriş Tarihi: " + giris +
                                "\nÇıkış Tarihi: " + cikis +
                                "\nFiyat: " + fiyat +
                                "\nDurum: " + durum,
                        "Rezervasyon Detayları",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[1]
                );

                // ✅ İPTAL İŞLEMİ (ODA NO GERİ HAVUZA DÖNER)
                if (secim == 0) {

                    for (Reservation rr : ReservationManager.getReservations()) {
                        if (rr.getMusteriAdi().equals(kullaniciAdi)
                                && rr.getOdaTipi().equals(odaTipi)
                                && rr.getOdaNo() == odaNo
                                && rr.getGirisTarihi().equals(giris)
                                && rr.getCikisTarihi().equals(cikis)) {

                            rr.setDurum("İptal Edildi");

                            // ✅ ODA NUMARASI GERİ VERİLİR
                            ReservationManager.odaIade(rr.getOdaTipi(), rr.getOdaNo());

                            break;
                        }
                    }

                    ReservationManager.saveToFile();

                    JOptionPane.showMessageDialog(null, "Rezervasyon başarıyla iptal edildi!");

                    // ✅ SAYFAYI YENİLE
                    dispose();
                    new ActiveReservationsPage(kullaniciAdi).setVisible(true);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);
    }
}
