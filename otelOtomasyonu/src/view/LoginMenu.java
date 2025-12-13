package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
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
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Ana Layout
        setContentPane(new BackgroundPanel("src/resources/background.png"));
        setLayout(new GridBagLayout()); 

        // Giriş Kartı
        JPanel loginCard = new RoundedPanel(25, new Color(255, 255, 255, 245));
        loginCard.setLayout(null);
        loginCard.setPreferredSize(new Dimension(380, 450));
        
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
        // KARAKTER SINIRI EKLEME (30 Karakter)
        addCharacterLimit(txtKullaniciAdi, 30, "Kullanıcı Adı");
        loginCard.add(txtKullaniciAdi);

        // 3. Şifre
        JLabel lblPass = new JLabel("Şifre");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(new Color(100, 100, 100));
        lblPass.setBounds(40, 190, 300, 20);
        loginCard.add(lblPass);

        txtSifre = createModernPasswordField();
        txtSifre.setBounds(40, 215, 300, 40);
        // KARAKTER SINIRI EKLEME (30 Karakter)
        addCharacterLimit(txtSifre, 30, "Şifre");
        loginCard.add(txtSifre);

        // 4. Giriş Butonu
        JButton btnGiris = createModernButton("Giriş Yap", PRIMARY_COLOR, Color.WHITE);
        btnGiris.setBounds(40, 280, 300, 45);
        btnGiris.addActionListener(e -> normalGiris());
        loginCard.add(btnGiris);

        // 5. Ayırıcı
        JLabel lblOr = new JLabel("- veya -", SwingConstants.CENTER);
        lblOr.setForeground(Color.GRAY);
        lblOr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblOr.setBounds(40, 335, 300, 20);
        loginCard.add(lblOr);

        // 6. Google Butonu
        JButton btnGoogle = createModernButton("Google ile Devam Et", Color.WHITE, Color.DARK_GRAY);
        btnGoogle.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        try {
            ImageIcon gIcon = new ImageIcon(new ImageIcon("src/resources/google.png").getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            btnGoogle.setIcon(gIcon);
        } catch (Exception ex) { }
        
        btnGoogle.setBounds(40, 365, 300, 45);
        btnGoogle.addActionListener(e -> googleGiris());
        loginCard.add(btnGoogle);

        add(loginCard);
    }

    // --- ÖNEMLİ: KARAKTER SINIRI VE CANLI ENGELLEME MANTIĞI ---
    
    private void addCharacterLimit(JTextField textField, int limit, String fieldName) {
        AbstractDocument doc = (AbstractDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String string = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
                
                // Silme işlemi (Backspace) her zaman serbest olmalı
                if (text.length() == 0) {
                     super.replace(fb, offset, length, text, attrs);
                     return;
                }

                if ((fb.getDocument().getLength() + text.length() - length) <= limit) {
                    super.replace(fb, offset, length, text, attrs);
                } else {
                    // Sınır aşıldı! Yazmayı engelle ve uyarı ver.
                    showErrorDialog(fieldName + " en fazla " + limit + " karakter olabilir!");
                }
            }
            
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                 if ((fb.getDocument().getLength() + string.length()) <= limit) {
                    super.insertString(fb, offset, string, attr);
                } else {
                    showErrorDialog(fieldName + " en fazla " + limit + " karakter olabilir!");
                }
            }
        });
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
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 200, 200)),
                new EmptyBorder(5, 5, 5, 5)));
        field.setBackground(new Color(250, 250, 250));
        
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

    // --- MODERN POPUP'LAR ---

    private void showWelcomeDialog(String kullaniciAdi) {
        JDialog dialog = createBaseDialog(180);
        JPanel panel = createBasePanel(Color.WHITE);

        JLabel lblIcon = new JLabel("✔", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblIcon.setForeground(new Color(76, 175, 80)); 
        lblIcon.setBounds(0, 15, 350, 50);
        panel.add(lblIcon);

        JLabel lblMsg = new JLabel("Giriş Başarılı!", SwingConstants.CENTER);
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMsg.setForeground(new Color(50,50,50));
        lblMsg.setBounds(0, 65, 350, 25);
        panel.add(lblMsg);

        JLabel lblName = new JLabel("Hoş geldiniz, " + kullaniciAdi, SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblName.setForeground(Color.GRAY);
        lblName.setBounds(0, 90, 350, 20);
        panel.add(lblName);

        JButton btnOk = createModernButton("Devam Et", PRIMARY_COLOR, Color.WHITE);
        btnOk.setBounds(100, 130, 150, 35);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOk.addActionListener(e -> dialog.dispose());
        panel.add(btnOk);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showErrorDialog(String hataMesaji) {
        // Eğer zaten bir hata penceresi açıksa tekrar açma (Spam engelleme)
        Window[] windows = Window.getWindows();
        for (Window w : windows) {
            if (w instanceof JDialog && w.isVisible() && "Hata".equals(((JDialog)w).getTitle())) {
                return; 
            }
        }

        JDialog dialog = createBaseDialog(190);
        dialog.setTitle("Hata"); // Kontrol için başlık set ediyoruz (görünmese bile)
        JPanel panel = createBasePanel(Color.WHITE);

        JLabel lblIcon = new JLabel("!", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 40));
        lblIcon.setForeground(ACCENT_COLOR);
        lblIcon.setBounds(0, 15, 350, 50);
        panel.add(lblIcon);

        JLabel lblTitle = new JLabel("Uyarı!", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(50,50,50));
        lblTitle.setBounds(0, 65, 350, 25);
        panel.add(lblTitle);

        JLabel lblMsg = new JLabel("<html><center>" + hataMesaji + "</center></html>", SwingConstants.CENTER);
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMsg.setForeground(Color.GRAY);
        lblMsg.setBounds(20, 90, 310, 40);
        panel.add(lblMsg);

        JButton btnOk = createModernButton("Tamam", ACCENT_COLOR, Color.WHITE);
        btnOk.setBounds(100, 135, 150, 35);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOk.addActionListener(e -> dialog.dispose());
        panel.add(btnOk);

        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    // Google Girişi için Modern ve Limitli Input Dialog
    private String showModernInputDialog(String message, int limit) {
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 220);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0,0,0,0));
        
        JPanel panel = createBasePanel(Color.WHITE);
        panel.setSize(400, 220);
        
        JLabel lblMsg = new JLabel(message, SwingConstants.CENTER);
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMsg.setForeground(new Color(50,50,50));
        lblMsg.setBounds(0, 20, 400, 30);
        panel.add(lblMsg);
        
        JTextField txtInput = createModernTextField();
        txtInput.setBounds(50, 70, 300, 40);
        addCharacterLimit(txtInput, limit, "E-posta"); // Buraya da limit ekliyoruz
        panel.add(txtInput);
        
        final String[] result = {null};
        
        JButton btnOk = createModernButton("Tamam", PRIMARY_COLOR, Color.WHITE);
        btnOk.setBounds(50, 140, 140, 40);
        btnOk.addActionListener(e -> {
            result[0] = txtInput.getText();
            dialog.dispose();
        });
        panel.add(btnOk);
        
        JButton btnCancel = createModernButton("İptal", Color.LIGHT_GRAY, Color.WHITE);
        btnCancel.setBounds(210, 140, 140, 40);
        btnCancel.addActionListener(e -> dialog.dispose());
        panel.add(btnCancel);
        
        dialog.add(panel);
        dialog.setVisible(true);
        
        return result[0];
    }

    private JDialog createBaseDialog(int height) {
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setSize(350, height);
        dialog.setLocationRelativeTo(this);
        dialog.setBackground(new Color(0,0,0,0));
        return dialog;
    }

    private JPanel createBasePanel(Color color) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(230, 230, 230));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        panel.setLayout(null);
        return panel;
    }

    // --- ÖZEL PANELLER ---

    class BackgroundPanel extends JPanel {
        private Image img;
        public BackgroundPanel(String path) {
            try {
                img = new ImageIcon(path).getImage();
            } catch (Exception e) {
                setBackground(new Color(40, 40, 60));
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

    class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
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

    // --- MANTIK KISMI ---

    private void normalGiris() {
        String kadi = txtKullaniciAdi.getText();
        String sifre = new String(txtSifre.getPassword());

        Kullanici giren = KullaniciDosyaIslemleri.login(kadi, sifre);

        if (giren == null) {
            showErrorDialog("Hatalı kullanıcı adı veya şifre!");
            return;
        }

        showWelcomeDialog(giren.getKullaniciAdi());

        if (giren.getRol().equals("admin")) {
            new AdminMenu(giren.getKullaniciAdi()).setVisible(true);
        } else {
            new UserMenu(giren.getKullaniciAdi()).setVisible(true);
        }
        dispose();
    }

    private void googleGiris() {
        // Eski JOptionPane yerine yeni metodumuz (Limitli: 40)
        String mail = showModernInputDialog("Google E-postanızı girin:", 40);

        if (mail == null || mail.isEmpty()) return;

        for (Kullanici k : KullaniciDosyaIslemleri.getKullanicilar()) {
            if (k.getEmail().equals(mail)) {
                showWelcomeDialog(k.getKullaniciAdi());
                
                if (k.getRol().equals("admin")) {
                    new AdminMenu(k.getKullaniciAdi()).setVisible(true);
                } else {
                    new UserMenu(k.getKullaniciAdi()).setVisible(true);
                }
                dispose();
                return;
            }
        }
        showErrorDialog("Bu mail sisteme kayıtlı değil!");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginMenu().setVisible(true));
    }
}