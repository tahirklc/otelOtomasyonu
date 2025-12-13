package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class AdminGeneralMenu extends JFrame {

    // Modern Renk Paleti
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private final Color TEXT_DARK = new Color(50, 50, 50);

    public AdminGeneralMenu() {

        setTitle("Yönetici - Genel İşlemler");
        setSize(450, 420); // Biraz daha ferah boyut
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Ana Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(mainPanel);

        // 1. Başlık Alanı
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 25, 0));

        JLabel lblTitle = new JLabel("Genel İşlemler", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSub = new JLabel("Lütfen yapmak istediğiniz işlemi seçin", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);

        headerPanel.add(lblTitle);
        headerPanel.add(lblSub);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Butonlar Alanı
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 15)); // Butonlar arası 15px boşluk
        buttonPanel.setOpaque(false);

        // ✅ MÜŞTERİ ARAMA BUTONU (Özelleştirilmiş)
        ModernMenuButton btnMusteri = new ModernMenuButton(
                "Müşteri Veritabanı", 
                "İsim ile müşteri kaydı arayın ve detayları görüntüleyin.", 
                "🔍"
        );
        btnMusteri.addActionListener(e -> {
            new AdminCustomerSearchPage().setVisible(true);
            // İsterseniz bu menüyü kapatabilirsiniz: dispose();
        });
        buttonPanel.add(btnMusteri);

        // ✅ HASILAT BUTONU (Özelleştirilmiş)
        ModernMenuButton btnHasilat = new ModernMenuButton(
                "Finansal Raporlar", 
                "Aylık ve yıllık hasılat raporlarını inceleyin.", 
                "📈"
        );
        btnHasilat.addActionListener(e -> {
            new AdminRevenueReportPage().setVisible(true);
        });
        buttonPanel.add(btnHasilat);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // 3. Alt Kısım (Kapat Butonu)
        JButton btnKapat = new JButton("Kapat");
        btnKapat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnKapat.setForeground(Color.GRAY);
        btnKapat.setContentAreaFilled(false);
        btnKapat.setBorderPainted(false);
        btnKapat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKapat.addActionListener(e -> dispose());
        
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.add(btnKapat);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    // --- ÖZEL BUTON SINIFI (Başlık + Açıklama + İkon) ---
    class ModernMenuButton extends JButton {
        private Color normalColor = Color.WHITE;
        private Color hoverColor = new Color(235, 240, 255); // Hafif mavi hover
        private Color borderColor = new Color(220, 220, 220);

        public ModernMenuButton(String title, String desc, String emojiIcon) {
            setLayout(new BorderLayout(15, 0));
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Sol taraf: İkon
            JLabel lblIcon = new JLabel(emojiIcon);
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            add(lblIcon, BorderLayout.WEST);

            // Orta kısım: Başlık ve Açıklama
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            
            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblTitle.setForeground(new Color(63, 81, 181)); // İndigo başlık
            
            JLabel lblDesc = new JLabel(desc);
            lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblDesc.setForeground(Color.GRAY);
            
            textPanel.add(lblTitle);
            textPanel.add(lblDesc);
            add(textPanel, BorderLayout.CENTER);

            // Hover Efektleri
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    normalColor = hoverColor;
                    borderColor = PRIMARY_COLOR; // Kenarlık rengi değişsin
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    normalColor = Color.WHITE;
                    borderColor = new Color(220, 220, 220);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Arka Plan
            g2.setColor(normalColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, 15, 15));

            // Kenarlık
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1));
            g2.draw(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, 15, 15));

            g2.dispose();
            super.paintComponent(g);
        }
    }
}