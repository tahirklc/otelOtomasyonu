package view;

import javax.swing.*;
import java.awt.*;

public class UserMenu extends JFrame {

    private String kullaniciAdi;

    public UserMenu(String ad) {
        this.kullaniciAdi = ad;

        setTitle("Kullanıcı Paneli");
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

        // Başlık
        JLabel baslik = new JLabel("Kullanıcı Paneli", SwingConstants.CENTER);
        baslik.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 32));
        baslik.setForeground(Color.WHITE);
        baslik.setBounds(0, 30, 1000, 50);
        background.add(baslik);

        // Profil
        JLabel profil = new JLabel(" " + kullaniciAdi);
        profil.setFont(new Font("Arial", Font.BOLD, 16));
        profil.setForeground(Color.WHITE);

        ImageIcon icon = new ImageIcon("src/resources/user.png");
        Image img = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        profil.setIcon(new ImageIcon(img));
        profil.setBounds(830, 20, 200, 40);
        background.add(profil);

        // --- KARTLAR ---

        // Rezervasyon Oluştur
        JPanel rezervasyonOlustur = kartOlustur("Rezervasyon Oluştur", "src/resources/calendar.jpg");
        rezervasyonOlustur.setBounds(150, 180, 220, 200);
        background.add(rezervasyonOlustur);

        JButton btnRez = (JButton) rezervasyonOlustur.getComponent(1);
        btnRez.addActionListener(e -> new ReservationForm(kullaniciAdi).setVisible(true));

        // Aktif Rezervasyonlar
        JPanel aktifRez = kartOlustur("Aktif Rezervasyonlar", "src/resources/list.png");
        aktifRez.setBounds(390, 180, 220, 200);
        background.add(aktifRez);

        JButton btnAktif = (JButton) aktifRez.getComponent(1);
        btnAktif.addActionListener(e -> new ActiveReservationsPage(kullaniciAdi).setVisible(true));

        // Eski Rezervasyonlar
        JPanel eskiRez = kartOlustur("Eski Rezervasyonlar", "src/resources/history.png");
        eskiRez.setBounds(630, 180, 220, 200);
        background.add(eskiRez);

        JButton btnEski = (JButton) eskiRez.getComponent(1);
        btnEski.addActionListener(e -> new OldReservationsPage(kullaniciAdi).setVisible(true));

        // Çıkış
        JButton btnCikis = new JButton("Çıkış");
        btnCikis.setFont(new Font("Arial", Font.BOLD, 14));
        btnCikis.setBounds(830, 540, 120, 35);
        btnCikis.addActionListener(e -> System.exit(0));
        background.add(btnCikis);
    }

    // Kart oluşturma metodu
    private JPanel kartOlustur(String yazi, String iconPath) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(255, 255, 255, 210));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(img));
        iconLabel.setBounds(75, 20, 64, 64);
        panel.add(iconLabel);

        JButton btn = new JButton(yazi);
        btn.setBounds(30, 110, 160, 35);
        panel.add(btn);

        return panel;
    }
}
