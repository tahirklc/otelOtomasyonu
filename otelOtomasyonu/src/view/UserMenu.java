package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class UserMenu extends JFrame {

    private String kullaniciAdi;
    
    // Tasarım Sabitleri
    private final Color PRIMARY_COLOR = new Color(63, 81, 181); // İndigo
    private final Color CARD_BG = new Color(255, 255, 255, 140); // Cam efekti (Şeffaf)
    private final Color CARD_HOVER = new Color(255, 255, 255, 220); // Hover (Daha opak)
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 17);

    public UserMenu(String ad) {
        this.kullaniciAdi = ad;

        setTitle("Kullanıcı Paneli - Otel Rezervasyon Sistemi");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 1. Arka Plan
        setContentPane(new BackgroundPanel("src/resources/background.png"));
        setLayout(new BorderLayout());

        // 2. Üst Bar (Header)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(30, 40, 20, 40));

        // Sol: Başlık
        JLabel lblBaslik = new JLabel("Hoş Geldiniz");
        lblBaslik.setFont(TITLE_FONT);
        lblBaslik.setForeground(Color.WHITE);
        headerPanel.add(lblBaslik, BorderLayout.WEST);

        // Sağ: Profil Kartı
        JPanel profilePanel = createProfilePanel(kullaniciAdi);
        headerPanel.add(profilePanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 3. Orta Alan (Kartlar)
        JPanel cardsContainer = new JPanel();
        cardsContainer.setOpaque(false);
        cardsContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 60)); // Kartlar arası boşluk
        cardsContainer.setBorder(new EmptyBorder(40, 0, 0, 0));

        // ✅ KART 1: Rezervasyon Oluştur
        JPanel pnlCreate = createDashboardCard(
                "Rezervasyon Oluştur", 
                "src/resources/rezervation.png", 
                "Yeni bir tatil planlayın ve oda ayırtın.",
                e -> new ReservationForm(kullaniciAdi).setVisible(true)
        );
        cardsContainer.add(pnlCreate);

        // ✅ KART 2: Aktif Rezervasyonlar
        JPanel pnlActive = createDashboardCard(
                "Aktif Rezervasyonlar", 
                "src/resources/list.png", 
                "Onaylanan veya bekleyen tatillerinizi görün.",
                e -> new ActiveReservationsPage(kullaniciAdi).setVisible(true)
        );
        cardsContainer.add(pnlActive);

        // ✅ KART 3: Geçmiş (Eski) Rezervasyonlar
        JPanel pnlHistory = createDashboardCard(
                "Geçmiş İşlemler", 
                "src/resources/history.png", 
                "İptal edilen veya tamamlanan kayıtlarınız.",
                e -> new OldReservationsPage(kullaniciAdi).setVisible(true)
        );
        cardsContainer.add(pnlHistory);

        add(cardsContainer, BorderLayout.CENTER);

        // 4. Alt Bar (Çıkış)
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(0, 0, 30, 40));

        JButton btnCikis = new ModernButton("Çıkış Yap", new Color(220, 53, 69), Color.WHITE);
        btnCikis.setPreferredSize(new Dimension(140, 40));
        btnCikis.addActionListener(e -> System.exit(0));
        
        footerPanel.add(btnCikis);
        add(footerPanel, BorderLayout.SOUTH);
    }

    // -------------------------------------------------
    // ✅ Profil Kartı Oluşturucu (Sağ Üst)
    // -------------------------------------------------
    private JPanel createProfilePanel(String name) {
        JPanel panel = new RoundedPanel(30, new Color(20, 20, 30, 160)); // Koyu şeffaf
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));
        
        JLabel iconLabel = new JLabel();
        try {
            ImageIcon rawIcon = new ImageIcon("src/resources/user.png");
            Image scaled = rawIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            iconLabel.setText("👤"); 
            iconLabel.setForeground(Color.WHITE);
        }
        
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(Color.WHITE);

        panel.add(iconLabel);
        panel.add(nameLabel);
        return panel;
    }

    // -------------------------------------------------
    // ✅ Dashboard Kartı Oluşturucu
    // -------------------------------------------------
    private JPanel createDashboardCard(String title, String iconPath, String desc, java.awt.event.ActionListener action) {
        // Kart Paneli
        RoundedPanel card = new RoundedPanel(25, CARD_BG);
        card.setLayout(null);
        card.setPreferredSize(new Dimension(280, 340)); 
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // İkon
        JLabel lblIcon = new JLabel();
        try {
            ImageIcon raw = new ImageIcon(iconPath);
            Image img = raw.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            lblIcon.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblIcon.setText("Img Error");
        }
        lblIcon.setBounds(0, 45, 280, 80);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblIcon);

        // Başlık
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(CARD_TITLE_FONT);
        lblTitle.setForeground(new Color(50, 50, 60));
        lblTitle.setBounds(10, 140, 260, 30);
        card.add(lblTitle);

        // Açıklama
        JTextArea txtDesc = new JTextArea(desc);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDesc.setForeground(new Color(80, 80, 80));
        txtDesc.setWrapStyleWord(true);
        txtDesc.setLineWrap(true);
        txtDesc.setOpaque(false);
        txtDesc.setEditable(false);
        txtDesc.setFocusable(false);
        txtDesc.setBounds(30, 175, 220, 50);
        // Basit ortalama
        card.add(txtDesc);

        // Buton
        JButton btnAction = new ModernButton("İşlem Yap", PRIMARY_COLOR, Color.WHITE);
        btnAction.setBounds(50, 260, 180, 40);
        btnAction.addActionListener(action);
        card.add(btnAction);

        // --- Etkileşimler (Hover & Click) ---
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                btnAction.doClick(); // Karta tıklayınca butona basmış say
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(CARD_HOVER); // Rengi aç
                card.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BG); // Eski haline dön
                card.repaint();
            }
        });

        return card;
    }

    // --- YARDIMCI GÖRSEL SINIFLAR (AdminMenu ile aynı standartta) ---

    class BackgroundPanel extends JPanel {
        private Image img;
        public BackgroundPanel(String path) {
            try {
                img = new ImageIcon(path).getImage();
            } catch (Exception e) {
                setBackground(new Color(45, 52, 54)); 
            }
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            // Hafif karartma katmanı
            g.setColor(new Color(0, 0, 0, 40));
            g.fillRect(0,0, getWidth(), getHeight());
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
        public void setBackground(Color bg) {
            this.bgColor = bg;
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Arka plan
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            
            // İnce Cam Kenarlık
            g2.setStroke(new BasicStroke(1));
            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, radius, radius);
            
            super.paintComponent(g);
        }
    }

    class ModernButton extends JButton {
        private Color baseColor;
        public ModernButton(String text, Color bg, Color fg) {
            super(text);
            this.baseColor = bg;
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(fg);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) g2.setColor(baseColor.darker());
            else if (getModel().isRollover()) g2.setColor(baseColor.brighter());
            else g2.setColor(baseColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}