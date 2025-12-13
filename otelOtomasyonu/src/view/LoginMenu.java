package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import model.Kullanici;
import service.KullaniciDosyaIslemleri;

public class LoginMenu extends JFrame {

    private JTextField txtKullaniciAdi;
    private JPasswordField txtSifre;
    
    // Modern renk paleti
    private final Color PRIMARY_COLOR = new Color(63, 81, 181); // İndigo Mavisi
    private final Color ACCENT_COLOR = new Color(255, 82, 82);  // Mercan Kırmızısı
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);

    public LoginMenu() {
        setTitle("Otel Rezervasyon Sistemi");
        setSize(900, 600); // Biraz daha geniş modern oran
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Ana Layout: Arka plan resmi üzerine merkezlenmiş panel
        setContentPane(new BackgroundPanel("src/resources/background.png"));
        setLayout(new GridBagLayout()); // Paneli tam ortaya sabitlemek için

        // Giriş Kartı (Login Card)
        JPanel loginCard = new RoundedPanel(25, new Color(255, 255, 255, 245));
        loginCard.setLayout(null);
        loginCard.setPreferredSize(new Dimension(380, 450)); // Kart boyutu
        
        // --- BİLEŞENLER ---

        // 1. Başlık
        JLabel lblTitle = new JLabel("Hoş Geldiniz", SwingConstants.CENTER);
        lblTitle.setFont(TITLE_FONT);
        lblTitle.setForeground(new Color(50, 50, 50));
        lblTitle.setBounds(40, 30, 300, 40);
        loginCard.add(lblTitle);

        JLabel lblSub = new JLabel("Devam etmek için giriş yapın", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        lblSub.setBounds(40, 65, 300, 20);
        loginCard.add(lblSub);

        // 2. Kullanıcı Adı
        JLabel lblUser = new JLabel("Kullanıcı Adı");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(100, 100, 100));
        lblUser.setBounds(40, 110, 300, 20);
        loginCard.add(lblUser);

        txtKullaniciAdi = createModernTextField();
        txtKullaniciAdi.setBounds(40, 135, 300, 40);
        loginCard.add(txtKullaniciAdi);

        // 3. Şifre
        JLabel lblPass = new JLabel("Şifre");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(new Color(100, 100, 100));
        lblPass.setBounds(40, 190, 300, 20);
        loginCard.add(lblPass);

        txtSifre = createModernPasswordField();
        txtSifre.setBounds(40, 215, 300, 40);
        loginCard.add(txtSifre);

        // 4. Giriş Butonu (Custom)
        JButton btnGiris = createModernButton("Giriş Yap", PRIMARY_COLOR, Color.WHITE);
        btnGiris.setBounds(40, 280, 300, 45);
        btnGiris.addActionListener(e -> normalGiris());
        loginCard.add(btnGiris);

        // 5. Ayırıcı (Veya)
        JLabel lblOr = new JLabel("- veya -", SwingConstants.CENTER);
        lblOr.setForeground(Color.GRAY);
        lblOr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblOr.setBounds(40, 335, 300, 20);
        loginCard.add(lblOr);

        // 6. Google Butonu
        JButton btnGoogle = createModernButton("Google ile Devam Et", Color.WHITE, Color.DARK_GRAY);
        btnGoogle.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        // Google ikonu varsa ekle, yoksa boş kalır
        try {
            ImageIcon gIcon = new ImageIcon(new ImageIcon("src/resources/google.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            btnGoogle.setIcon(gIcon);
        } catch (Exception ex) { /* İkon yoksa hata verme */ }
        
        btnGoogle.setBounds(40, 365, 300, 45);
        btnGoogle.addActionListener(e -> googleGiris());
        loginCard.add(btnGoogle);

        // Karta ekle
        add(loginCard);
    }

    // --- YARDIMCI UI METODLARI ---

    private JTextField createModernTextField() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    private JPasswordField createModernPasswordField() {
        JPasswordField field = new JPasswordField();
        styleField(field);
        return field;
    }

    private void styleField(JTextField field) {
        field.setFont(MAIN_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)), // Sadece alt çizgi
                new EmptyBorder(5, 5, 5, 5)));
        field.setBackground(new Color(250, 250, 250));
        
        // Focus olduğunda alt çizgi rengini değiştir
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
                    new EmptyBorder(5, 5, 5, 5)));
                field.setBackground(Color.WHITE);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)),
                    new EmptyBorder(5, 5, 5, 5)));
                field.setBackground(new Color(250, 250, 250));
            }
        });
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- ÖZEL PANELLER (Background & Rounded) ---

    // Arka plan resmini otomatik scale eden panel
    class BackgroundPanel extends JPanel {
        private Image img;
        public BackgroundPanel(String path) {
            try {
                img = new ImageIcon(path).getImage();
            } catch (Exception e) {
                setBackground(new Color(40, 40, 60)); // Resim yoksa koyu bir renk
            }
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    // Yuvarlak köşeli ve yarı saydam panel
    class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false); // Default boyamayı kapat
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }
    }

    // --- MANTIK KISMI (AYNEN KORUNDU) ---
    // -----------------------------------------------------

    private void normalGiris() {
        String kadi = txtKullaniciAdi.getText();
        String sifre = new String(txtSifre.getPassword());

        Kullanici giren = KullaniciDosyaIslemleri.login(kadi, sifre);

        if (giren == null) {
            JOptionPane.showMessageDialog(this, "Hatalı kullanıcı adı veya şifre!", "Giriş Hatası", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Başarılı mesajını biraz daha modern yapalım (Opsiyonel)
        JOptionPane.showMessageDialog(this, "Hoş geldiniz, " + giren.getKullaniciAdi());

        if (giren.getRol().equals("admin")) {
            new AdminMenu(giren.getKullaniciAdi()).setVisible(true);
        } else {
            new UserMenu(giren.getKullaniciAdi()).setVisible(true);
        }
        dispose();
    }

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
        JOptionPane.showMessageDialog(this, "Bu mail sisteme kayıtlı değil!", "Hata", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        // UI Thread güvenliği için
        SwingUtilities.invokeLater(() -> new LoginMenu().setVisible(true));
    }
}