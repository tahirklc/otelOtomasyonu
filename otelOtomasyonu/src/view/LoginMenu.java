package view;

import javax.swing.*;
import java.awt.*;
import model.Kullanici;
import service.KullaniciDosyaIslemleri;

public class LoginMenu extends JFrame {

    private JTextField txtKullaniciAdi;
    private JPasswordField txtSifre;

    public LoginMenu() {
        setTitle("Otel Rezervasyon Giriş");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        // Arka plan resmi
        JLabel background = new JLabel();
        background.setBounds(0, 0, 850, 550);
        background.setIcon(new ImageIcon(new ImageIcon("src/resources/background.png")
                .getImage().getScaledInstance(850, 550, Image.SCALE_SMOOTH)));
        add(background);

        // Giriş paneli
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(255, 255, 255, 230));
        panel.setBounds(80, 80, 350, 330);
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        background.add(panel);

        JLabel baslik = new JLabel("Giriş Yap");
        baslik.setFont(new Font("Arial", Font.BOLD, 22));
        baslik.setBounds(20, 20, 200, 30);
        panel.add(baslik);

        txtKullaniciAdi = new JTextField();
        txtKullaniciAdi.setBounds(20, 80, 300, 35);
        txtKullaniciAdi.setBorder(BorderFactory.createTitledBorder("Kullanıcı Adı"));
        panel.add(txtKullaniciAdi);

        txtSifre = new JPasswordField();
        txtSifre.setBounds(20, 140, 300, 35);
        txtSifre.setBorder(BorderFactory.createTitledBorder("Şifre"));
        panel.add(txtSifre);

        JButton btnGiris = new JButton("Giriş Yap");
        btnGiris.setBounds(20, 190, 300, 35);
        btnGiris.setBackground(new Color(255, 90, 90));
        btnGiris.setFont(new Font("Arial", Font.BOLD, 14));
        btnGiris.setForeground(Color.WHITE);
        btnGiris.addActionListener(e -> normalGiris());
        panel.add(btnGiris);

        JLabel veya = new JLabel("ya da", SwingConstants.CENTER);
        veya.setBounds(20, 225, 300, 20);
        panel.add(veya);

        JButton googleBtn = new JButton(" Google ile devam et");
        googleBtn.setBounds(20, 250, 300, 35);
        googleBtn.setIcon(new ImageIcon("src/resources/google.png"));
        googleBtn.addActionListener(e -> googleGiris());
        panel.add(googleBtn);
    }

    // -----------------------------------------------------
    // NORMAL GİRİŞ
    // -----------------------------------------------------
    private void normalGiris() {
        String kadi = txtKullaniciAdi.getText();
        String sifre = new String(txtSifre.getPassword());

        Kullanici giren = KullaniciDosyaIslemleri.login(kadi, sifre);

        if (giren == null) {
            JOptionPane.showMessageDialog(this, "Hatalı kullanıcı adı veya şifre!");
            return;
        }

        JOptionPane.showMessageDialog(this, "Giriş başarılı!");

        // Rol kontrolü → admin veya user
        if (giren.getRol().equals("admin")) {
            new AdminMenu(giren.getKullaniciAdi()).setVisible(true);
        } else {
            new UserMenu(giren.getKullaniciAdi()).setVisible(true);
        }

        dispose();
    }

    // -----------------------------------------------------
    // GOOGLE GİRİŞ → email ile
    // -----------------------------------------------------
    private void googleGiris() {
        String mail = JOptionPane.showInputDialog(this, "Google E-postanızı girin:");

        if (mail == null || mail.isEmpty()) return;

        for (Kullanici k : KullaniciDosyaIslemleri.getKullanicilar()) {

            if (k.getEmail().equals(mail)) {

                JOptionPane.showMessageDialog(this, "Google girişi başarılı!");

                if (k.getRol().equals("admin")) {
                    new AdminMenu(k.getKullaniciAdi()).setVisible(true);
                } else {
                    new UserMenu(k.getKullaniciAdi()).setVisible(true);
                }

                dispose();
                return;
            }
        }

        JOptionPane.showMessageDialog(this, "Bu mail sisteme kayıtlı değil!");
    }

    public static void main(String[] args) {
        new LoginMenu().setVisible(true);
    }
}
