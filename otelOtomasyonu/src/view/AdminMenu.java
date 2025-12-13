package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import view.AdminReservationManagerPage;
import view.AdminRoomManagerPage;
import view.AdminGeneralMenu;

public class AdminMenu extends JFrame {

    private String adminAdi;
    
    // --- RENK PALETİ VE AYARLAR ---
    private final Color PRIMARY_COLOR = new Color(63, 81, 181); // İndigo
    
    // ✅ DEĞİŞİKLİK: Daha şeffaf cam efekti (Alpha: 140)
    private final Color CARD_BG = new Color(255, 255, 255, 140); 
    
    // Hover durumunda biraz daha belirgin olsun (Alpha: 210)
    private final Color CARD_HOVER_BG = new Color(255, 255, 255, 210);

    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private final Font CARD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 17);

    public AdminMenu(String ad) {
        this.adminAdi = ad;

        setTitle("Admin Paneli - Yönetim Panosu");
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

        JLabel lblBaslik = new JLabel("Yönetim Paneli");
        lblBaslik.setFont(TITLE_FONT);
        lblBaslik.setForeground(Color.WHITE);
        // Yazının arkasına hafif gölge atarak okunabilirliği artıralım
        lblBaslik.setBorder(new EmptyBorder(0,0,0,0)); 
        headerPanel.add(lblBaslik, BorderLayout.WEST);

        JPanel profilePanel = createProfilePanel(adminAdi);
        headerPanel.add(profilePanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 3. Orta Alan (Kartlar)
        JPanel dashboardContainer = new JPanel();
        dashboardContainer.setOpaque(false);
        dashboardContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 50));
        dashboardContainer.setBorder(new EmptyBorder(40, 0, 0, 0));

        // KART 1: Rezervasyon
        JPanel pnlRezervasyon = createDashboardCard(
                "Rezervasyonlar", 
                "src/resources/rezervation.png", 
                "Rezervasyon kayıtlarını incele.",
                e -> new AdminReservationManagerPage().setVisible(true)
        );
        dashboardContainer.add(pnlRezervasyon);

        // KART 2: Oda
        JPanel pnlOda = createDashboardCard(
                "Oda Yönetimi", 
                "src/resources/room.png", 
                "Oda durumlarını düzenle.",
                e -> new AdminRoomManagerPage().setVisible(true)
        );
        dashboardContainer.add(pnlOda);

        // KART 3: Genel
        JPanel pnlGenel = createDashboardCard(
                "Genel Ayarlar", 
                "src/resources/settings.png", 
                "Sistem ayarları ve araçlar.",
                e -> new AdminGeneralMenu().setVisible(true)
        );
        dashboardContainer.add(pnlGenel);

        add(dashboardContainer, BorderLayout.CENTER);

        // 4. Alt Bar
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(0, 0, 30, 40));

        JButton btnCikis = new ModernButton("Güvenli Çıkış", new Color(220, 53, 69), Color.WHITE);
        btnCikis.setPreferredSize(new Dimension(140, 40));
        btnCikis.addActionListener(e -> System.exit(0));
        
        footerPanel.add(btnCikis);
        add(footerPanel, BorderLayout.SOUTH);
    }

    // --- PROFİL KARTI ---
    private JPanel createProfilePanel(String name) {
        // Profil kartı daha koyu kalsın ki beyaz kartlarla kontrast oluştursun
        JPanel panel = new RoundedPanel(30, new Color(20, 20, 30, 150)); 
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

    // --- DASHBOARD KARTI OLUŞTURUCU ---
    private JPanel createDashboardCard(String title, String iconPath, String desc, java.awt.event.ActionListener action) {
        // Yuvarlak Panel
        RoundedPanel card = new RoundedPanel(25, CARD_BG);
        card.setLayout(null);
        card.setPreferredSize(new Dimension(260, 330)); 
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // İkon
        JLabel lblIcon = new JLabel();
        try {
            ImageIcon raw = new ImageIcon(iconPath);
            Image img = raw.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            lblIcon.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblIcon.setText("Img Error");
        }
        lblIcon.setBounds(0, 45, 260, 70);
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblIcon);

        // Başlık (Daha koyu renk, şeffaf zeminde okunsun diye)
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(CARD_TITLE_FONT);
        lblTitle.setForeground(new Color(40, 40, 50)); 
        lblTitle.setBounds(10, 135, 240, 30);
        card.add(lblTitle);

        // Açıklama
        JTextArea txtDesc = new JTextArea(desc);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDesc.setForeground(new Color(60, 60, 70)); // Koyu gri
        txtDesc.setWrapStyleWord(true);
        txtDesc.setLineWrap(true);
        txtDesc.setOpaque(false);
        txtDesc.setEditable(false);
        txtDesc.setFocusable(false);
        
        // Metni ortalamak için margin hilesi veya basit bounds
        txtDesc.setBounds(30, 170, 200, 50);
        card.add(txtDesc);

        // Buton
        JButton btnAction = new ModernButton("İşlem yap", PRIMARY_COLOR, Color.WHITE);
        btnAction.setBounds(50, 250, 160, 40);
        btnAction.addActionListener(action);
        card.add(btnAction);

        // Animasyon ve Tıklama
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                btnAction.doClick();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                // Üzerine gelince daha opak (belirgin) olsun
                card.setBackground(CARD_HOVER_BG);
                card.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                // Çıkınca tekrar şeffaf olsun
                card.setBackground(CARD_BG);
                card.repaint();
            }
        });

        return card;
    }

    // --- YARDIMCI GÖRSEL SINIFLAR ---

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
            // Arka planı hafif karart (Okunabilirlik için)
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
            repaint(); // Rengi anlık güncellemek için
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Panelin içini boya
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            
            // ✅ EKLENTİ: İnce beyaz kenarlık (Glass border effect)
            g2.setStroke(new BasicStroke(1));
            g2.setColor(new Color(255, 255, 255, 100)); // Çok silik beyaz çizgi
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminMenu("Yönetici").setVisible(true));
    }
}