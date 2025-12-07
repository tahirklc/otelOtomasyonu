package view;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    private String kullaniciAdi;

    public MainMenu(String ad) {
        this.kullaniciAdi = ad;

        setTitle("Otel Otomasyon Sistemi");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Arka plan
        JLabel background = new JLabel();
        background.setBounds(0, 0, 1000, 650);
        background.setIcon(new ImageIcon(
                new ImageIcon("src/resources/background.png")
                        .getImage().getScaledInstance(1000, 650, Image.SCALE_SMOOTH)
        ));
        add(background);

        // ANA BAŞLIK
        JLabel baslik = new JLabel("Otel Otomasyon Sistemi", SwingConstants.CENTER);
        baslik.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 32));
        baslik.setForeground(Color.WHITE);
        baslik.setBounds(0, 30, 1000, 50);
        background.add(baslik);

        // SAĞ ÜST PROFİL (Giriş yapan kullanıcı)
        JLabel profil = new JLabel(" " + kullaniciAdi);
        profil.setFont(new Font("Arial", Font.BOLD, 16));
        profil.setForeground(Color.WHITE);

        ImageIcon icon = new ImageIcon("src/resources/user.png");
        Image img = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        profil.setIcon(new ImageIcon(img));

        profil.setBounds(830, 20, 200, 40);
        background.add(profil);

        // BUTON KART PANELLERİ
        JPanel rezervasyonPanel = kartOlustur("Rezervasyon İşlemleri", "src/resources/calendar.jpg");
        rezervasyonPanel.setBounds(150, 180, 220, 200);
        background.add(rezervasyonPanel);

        JPanel odaPanel = kartOlustur("Oda İşlemleri", "src/resources/bed.jpg");
        odaPanel.setBounds(390, 180, 220, 200);
        background.add(odaPanel);

        JPanel genelPanel = kartOlustur("Genel İşlemler", "src/resources/settings.png");
        genelPanel.setBounds(630, 180, 220, 200);
        background.add(genelPanel);

        // ÇIKIŞ BUTONU
        JButton btnCikis = new JButton("Çıkış");
        btnCikis.setFont(new Font("Arial", Font.BOLD, 14));
        btnCikis.setBounds(830, 540, 120, 35);
        btnCikis.addActionListener(e -> System.exit(0));
        background.add(btnCikis);
    }

    private JPanel kartOlustur(String yazi, String iconPath) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(255, 255, 255, 210));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        // ICON
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(img));
        iconLabel.setBounds(75, 20, 64, 64);
        panel.add(iconLabel);

        // BUTON
        JButton btn = new JButton(yazi);
        btn.setBounds(30, 110, 160, 35);
        panel.add(btn);

        return panel;
    }
}
