package view;

import service.ReservationManager;
import model.Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class AdminRoomManagerPage extends JFrame {

    // Oda Tipi Kartları (Custom Component)
    private RoomCard cardStandart;
    private RoomCard cardDeluxe;
    private RoomCard cardKral;

    // Renk Paleti
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);

    public AdminRoomManagerPage() {
        setTitle("Yönetici - Oda Kapasite Yönetimi");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Ana Scrollable Panel
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(BG_COLOR);
        mainContainer.setBorder(new EmptyBorder(30, 40, 30, 40));

        // 1. Başlık Alanı
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setMaximumSize(new Dimension(2000, 60));
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel lblTitle = new JLabel("Oda Durum Paneli");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(50, 50, 50));
        
        JLabel lblSub = new JLabel("Doluluk oranlarını inceleyin ve yeni oda ekleyin.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.GRAY);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        mainContainer.add(headerPanel);

        // 2. Oda Kartlarının Oluşturulması
        // Standart
        cardStandart = new RoomCard("Standart Oda", new Color(0, 150, 136)); // Teal Rengi
        cardStandart.setAddAction(e -> {
            ReservationManager.yeniStandartOdaEkle();
            guncelle();
            showSuccessMsg("Standart");
        });
        mainContainer.add(cardStandart);
        mainContainer.add(Box.createVerticalStrut(20)); // Boşluk

        // Deluxe
        cardDeluxe = new RoomCard("Deluxe Oda", new Color(255, 152, 0)); // Turuncu
        cardDeluxe.setAddAction(e -> {
            ReservationManager.yeniDeluxeOdaEkle();
            guncelle();
            showSuccessMsg("Deluxe");
        });
        mainContainer.add(cardDeluxe);
        mainContainer.add(Box.createVerticalStrut(20));

        // Kral
        cardKral = new RoomCard("Kral Dairesi", new Color(156, 39, 176)); // Mor
        cardKral.setAddAction(e -> {
            ReservationManager.yeniKralOdaEkle();
            guncelle();
            showSuccessMsg("Kral");
        });
        mainContainer.add(cardKral);

        // ScrollPane İçine Al (Ekrana sığmazsa kaydırılsın)
        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(scrollPane);

        // Verileri Yükle
        guncelle();
    }

    private void showSuccessMsg(String tur) {
        JOptionPane.showMessageDialog(this, "Yeni " + tur + " oda sisteme eklendi!", "İşlem Başarılı", JOptionPane.INFORMATION_MESSAGE);
    }

    // ✅ BOŞ & DOLU ODALARI DOĞRU ŞEKİLDE GÖSTERİR (MANTIK KORUNDU)
    private void guncelle() {

        // 1. Verileri Çek
        int bosStandart = ReservationManager.getStandartKalan();
        int bosDeluxe = ReservationManager.getDeluxeKalan();
        int bosKral = ReservationManager.getKralKalan();

        List<Integer> doluStandart = new ArrayList<>();
        List<Integer> doluDeluxe = new ArrayList<>();
        List<Integer> doluKral = new ArrayList<>();

        for (Reservation r : ReservationManager.getReservations()) {
            if (r == null) continue;
            if (r.getDurum().equals("Bekliyor") || r.getDurum().equals("Onaylandı")) {
                if (r.getOdaTipi().contains("Standart")) doluStandart.add(r.getOdaNo());
                if (r.getOdaTipi().contains("Deluxe")) doluDeluxe.add(r.getOdaNo());
                if (r.getOdaTipi().contains("Kral")) doluKral.add(r.getOdaNo());
            }
        }

        // 2. Toplam Oda Sayılarını Hesapla (Boş + Dolu)
        int topStandart = bosStandart + doluStandart.size();
        int topDeluxe   = bosDeluxe + doluDeluxe.size();
        int topKral     = bosKral + doluKral.size();

        // 3. Kartları Güncelle
        cardStandart.updateData(topStandart, bosStandart, doluStandart);
        cardDeluxe.updateData(topDeluxe, bosDeluxe, doluDeluxe);
        cardKral.updateData(topKral, bosKral, doluKral);
    }

    // =================================================================
    // 🎨 CUSTOM UI: MODERN ODA KARTI PANELİ (REUSABLE COMPONENT)
    // =================================================================
    class RoomCard extends JPanel {
        private JLabel lblTitle;
        private JLabel lblStats; // "Boş: 5 / Toplam: 10"
        private JProgressBar progressBar;
        private JTextArea areaOccupied;
        private JButton btnAdd;
        private Color themeColor;

        public RoomCard(String title, Color themeColor) {
            this.themeColor = themeColor;
            setLayout(new BorderLayout(20, 0));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                    new EmptyBorder(20, 20, 20, 20)
            ));
            setMaximumSize(new Dimension(2000, 180)); // Kart yüksekliği

            // --- SOL TARAFI: BAŞLIK VE BUTON ---
            JPanel leftPanel = new JPanel(new GridLayout(3, 1, 0, 10));
            leftPanel.setBackground(Color.WHITE);
            leftPanel.setPreferredSize(new Dimension(250, 0));

            lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblTitle.setForeground(themeColor);

            btnAdd = new ModernButton("+ Oda Ekle", themeColor);

            lblStats = new JLabel("Veri bekleniyor...");
            lblStats.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblStats.setForeground(Color.GRAY);

            leftPanel.add(lblTitle);
            leftPanel.add(lblStats);
            leftPanel.add(btnAdd);
            add(leftPanel, BorderLayout.WEST);

            // --- ORTA TARAF: PROGRESS BAR VE LİSTE ---
            JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
            centerPanel.setBackground(Color.WHITE);

            // Doluluk Barı
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
            progressBar.setForeground(themeColor);
            progressBar.setBackground(new Color(240, 240, 240));
            progressBar.setBorderPainted(false);
            centerPanel.add(progressBar, BorderLayout.NORTH);

            // Dolu Oda Listesi
            JLabel lblListHeader = new JLabel("Dolu Oda Numaraları:");
            lblListHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            areaOccupied = new JTextArea();
            areaOccupied.setFont(new Font("Consolas", Font.PLAIN, 13));
            areaOccupied.setForeground(new Color(60, 60, 60));
            areaOccupied.setLineWrap(true);
            areaOccupied.setWrapStyleWord(true);
            areaOccupied.setEditable(false);
            areaOccupied.setBackground(new Color(250, 250, 250));
            areaOccupied.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JPanel listWrapper = new JPanel(new BorderLayout());
            listWrapper.setBackground(Color.WHITE);
            listWrapper.add(lblListHeader, BorderLayout.NORTH);
            listWrapper.add(new JScrollPane(areaOccupied), BorderLayout.CENTER);
            
            centerPanel.add(listWrapper, BorderLayout.CENTER);
            add(centerPanel, BorderLayout.CENTER);
        }

        public void setAddAction(java.awt.event.ActionListener action) {
            btnAdd.addActionListener(action);
        }

        public void updateData(int total, int empty, List<Integer> occupiedList) {
            int occupiedCount = occupiedList.size();
            
            // İstatistik Yazısı
            lblStats.setText("<html>Boş: <font color='green'>" + empty + "</font> | Dolu: <font color='red'>" + occupiedCount + "</font></html>");

            // Progress Bar (Doluluk Oranı)
            if (total > 0) {
                int percentage = (int) (((double) occupiedCount / total) * 100);
                progressBar.setValue(percentage);
                progressBar.setString("Doluluk: %" + percentage + " (" + occupiedCount + "/" + total + ")");
            } else {
                progressBar.setValue(0);
                progressBar.setString("Oda Yok");
            }

            // Dolu Oda Listesi
            if (occupiedList.isEmpty()) {
                areaOccupied.setText("- Şu an tüm odalar boş -");
                areaOccupied.setForeground(Color.GRAY);
            } else {
                areaOccupied.setText(occupiedList.toString()); // [101, 102] formatında yazar
                areaOccupied.setForeground(new Color(60, 60, 60));
            }
        }
    }

    // --- MODERN BUTON SINIFI ---
    class ModernButton extends JButton {
        private Color baseColor;
        public ModernButton(String text, Color bg) {
            super(text);
            this.baseColor = bg;
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
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
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}