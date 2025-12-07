package view;

import service.ReservationManager;
import model.Reservation;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminRoomManagerPage extends JFrame {

    private JLabel lblStandart;
    private JLabel lblDeluxe;
    private JLabel lblKral;

    private JTextArea areaStandart;
    private JTextArea areaDeluxe;
    private JTextArea areaKral;

    public AdminRoomManagerPage() {

        setTitle("Oda Doluluk Yönetimi");
        setSize(700, 520);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // -------- BAŞLIK --------
        JLabel title = new JLabel("Boş & Dolu Oda Numaraları Yönetimi", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBounds(0, 20, 700, 30);
        add(title);

        // ================== STANDART ==================
        lblStandart = new JLabel();
        lblStandart.setFont(new Font("Arial", Font.BOLD, 14));
        lblStandart.setBounds(50, 80, 400, 25);
        add(lblStandart);

        areaStandart = new JTextArea();
        areaStandart.setEditable(false);
        JScrollPane spStd = new JScrollPane(areaStandart);
        spStd.setBounds(50, 110, 250, 90);
        add(spStd);

        JButton btnStdEkle = new JButton("+ Standart Oda Ekle");
        btnStdEkle.setBounds(350, 130, 220, 30);
        btnStdEkle.addActionListener(e -> {
            ReservationManager.yeniStandartOdaEkle();
            guncelle();
            JOptionPane.showMessageDialog(this, "Yeni Standart oda eklendi.");
        });
        add(btnStdEkle);

        // ================== DELUXE ==================
        lblDeluxe = new JLabel();
        lblDeluxe.setFont(new Font("Arial", Font.BOLD, 14));
        lblDeluxe.setBounds(50, 220, 400, 25);
        add(lblDeluxe);

        areaDeluxe = new JTextArea();
        areaDeluxe.setEditable(false);
        JScrollPane spDel = new JScrollPane(areaDeluxe);
        spDel.setBounds(50, 250, 250, 90);
        add(spDel);

        JButton btnDelEkle = new JButton("+ Deluxe Oda Ekle");
        btnDelEkle.setBounds(350, 270, 220, 30);
        btnDelEkle.addActionListener(e -> {
            ReservationManager.yeniDeluxeOdaEkle();
            guncelle();
            JOptionPane.showMessageDialog(this, "Yeni Deluxe oda eklendi.");
        });
        add(btnDelEkle);

        // ================== KRAL ==================
        lblKral = new JLabel();
        lblKral.setFont(new Font("Arial", Font.BOLD, 14));
        lblKral.setBounds(50, 360, 400, 25);
        add(lblKral);

        areaKral = new JTextArea();
        areaKral.setEditable(false);
        JScrollPane spKral = new JScrollPane(areaKral);
        spKral.setBounds(50, 390, 250, 90);
        add(spKral);

        JButton btnKralEkle = new JButton("+ Kral Oda Ekle");
        btnKralEkle.setBounds(350, 410, 220, 30);
        btnKralEkle.addActionListener(e -> {
            ReservationManager.yeniKralOdaEkle();
            guncelle();
            JOptionPane.showMessageDialog(this, "Yeni Kral oda eklendi.");
        });
        add(btnKralEkle);

        guncelle();
    }

    // ✅ BOŞ & DOLU ODALARI AYRI AYRI GÖSTERİR
    private void guncelle() {

        // ✅ BOŞ ODA SAYILARI
        lblStandart.setText("Standart Odalar | Boş: " + ReservationManager.getStandartKalan());
        lblDeluxe.setText("Deluxe Odalar   | Boş: " + ReservationManager.getDeluxeKalan());
        lblKral.setText("Kral Odalar     | Boş: " + ReservationManager.getKralKalan());

        // ✅ DOLU ODALARI BUL
        List<Integer> doluStandart = new ArrayList<>();
        List<Integer> doluDeluxe = new ArrayList<>();
        List<Integer> doluKral = new ArrayList<>();

        for (Reservation r : ReservationManager.getReservations()) {
            if (r.getDurum().equals("Bekliyor") || r.getDurum().equals("Onaylandı")) {
                if (r.getOdaTipi().contains("Standart")) doluStandart.add(r.getOdaNo());
                if (r.getOdaTipi().contains("Deluxe")) doluDeluxe.add(r.getOdaNo());
                if (r.getOdaTipi().contains("Kral")) doluKral.add(r.getOdaNo());
            }
        }

        // ✅ GÖRÜNTÜYE YAZ
        areaStandart.setText(
                "DOLU: " + doluStandart + "\n\n" +
                "BOŞ: " + ReservationManager.getStandartKalan()
        );

        areaDeluxe.setText(
                "DOLU: " + doluDeluxe + "\n\n" +
                "BOŞ: " + ReservationManager.getDeluxeKalan()
        );

        areaKral.setText(
                "DOLU: " + doluKral + "\n\n" +
                "BOŞ: " + ReservationManager.getKralKalan()
        );
    }
}
