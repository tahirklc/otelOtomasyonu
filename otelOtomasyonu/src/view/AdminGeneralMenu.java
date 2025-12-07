package view;

import javax.swing.*;
import java.awt.*;

public class AdminGeneralMenu extends JFrame {

    public AdminGeneralMenu() {

        setTitle("Genel İşlemler");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel title = new JLabel("GENEL İŞLEMLER", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBounds(0, 20, 400, 30);
        add(title);

        // ✅ MÜŞTERİ ARAMA
        JButton btnMusteriAra = new JButton("Müşteri Ara (TC / İsim)");
        btnMusteriAra.setBounds(80, 80, 240, 40);
        btnMusteriAra.addActionListener(e -> {
            new AdminCustomerSearchPage().setVisible(true);
        });
        add(btnMusteriAra);

        // ✅ HASILAT
        JButton btnHasilat = new JButton("Aylık & Yıllık Hasılat");
        btnHasilat.setBounds(80, 140, 240, 40);
        btnHasilat.addActionListener(e -> {
            new AdminRevenueReportPage().setVisible(true);
        });
        add(btnHasilat);
    }
}
