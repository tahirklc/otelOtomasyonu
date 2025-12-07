package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import service.ReservationManager;
import model.Reservation;

public class OldReservationsPage extends JFrame {

    public OldReservationsPage(String kullaniciAdi) {

        setTitle("Eski Rezervasyonlarım");
        setSize(800, 400);
        setLocationRelativeTo(null);

        String[] columns = {"Oda", "Kişi Sayısı", "Giriş", "Çıkış", "Fiyat", "Durum"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (Reservation r : ReservationManager.getReservations()) {
            if (r.getMusteriAdi().equals(kullaniciAdi)) {

                if (r.getDurum().equals("Reddedildi")) {
                    model.addRow(new Object[]{
                            r.getOdaTipi(),
                            r.getKisiSayisi(),
                            r.getGirisTarihi(),
                            r.getCikisTarihi(),
                            r.getFiyat(),
                            r.getDurum()
                    });
                }
            }
        }

        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        add(scroll, BorderLayout.CENTER);
    }
}
