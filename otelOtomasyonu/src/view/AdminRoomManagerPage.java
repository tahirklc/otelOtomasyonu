package view;

import service.ReservationManager;
import model.Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class AdminRoomManagerPage extends JFrame {

    private RoomCard cardStandart;
    private RoomCard cardDeluxe;
    private RoomCard cardKral;

    private final Color BG_COLOR = new Color(245, 247, 250);
    private final int MAX_ROOM_LIMIT = 5;

    public AdminRoomManagerPage() {
        setTitle("Yönetici - Oda Kapasite Yönetimi");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(BG_COLOR);
        mainContainer.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_COLOR);
        header.setMaximumSize(new Dimension(2000, 70));

        JLabel lblTitle = new JLabel("Oda Durum Paneli");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(50, 50, 50));

        JLabel lblSub = new JLabel("Doluluk oranlarını inceleyin. (Her kategori için maks. " + MAX_ROOM_LIMIT + " oda)");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.GRAY);

        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.SOUTH);

        mainContainer.add(header);
        mainContainer.add(Box.createVerticalStrut(20));

        // STANDART
        cardStandart = new RoomCard("Standart Oda", new Color(0, 150, 136));
        cardStandart.setActions(
                () -> { // ekle
                    if (ReservationManager.getStandartTotal() >= MAX_ROOM_LIMIT) {
                        warn("Standart", "Maksimum oda limitine ulaşıldı!");
                        return;
                    }
                    ReservationManager.yeniStandartOdaEkle();
                    guncelle();
                },
                () -> { // sil (sadece boş)
                    boolean ok = ReservationManager.standartOdaSil();
                    if (!ok) warn("Standart", "Boş oda yok! Dolu odayı silemezsin.");
                    guncelle();
                }
        );
        mainContainer.add(cardStandart);
        mainContainer.add(Box.createVerticalStrut(20));

        // DELUXE
        cardDeluxe = new RoomCard("Deluxe Oda", new Color(255, 152, 0));
        cardDeluxe.setActions(
                () -> {
                    if (ReservationManager.getDeluxeTotal() >= MAX_ROOM_LIMIT) {
                        warn("Deluxe", "Maksimum oda limitine ulaşıldı!");
                        return;
                    }
                    ReservationManager.yeniDeluxeOdaEkle();
                    guncelle();
                },
                () -> {
                    boolean ok = ReservationManager.deluxeOdaSil();
                    if (!ok) warn("Deluxe", "Boş oda yok! Dolu odayı silemezsin.");
                    guncelle();
                }
        );
        mainContainer.add(cardDeluxe);
        mainContainer.add(Box.createVerticalStrut(20));

        // KRAL
        cardKral = new RoomCard("Kral Dairesi", new Color(156, 39, 176));
        cardKral.setActions(
                () -> {
                    if (ReservationManager.getKralTotal() >= MAX_ROOM_LIMIT) {
                        warn("Kral", "Maksimum oda limitine ulaşıldı!");
                        return;
                    }
                    ReservationManager.yeniKralOdaEkle();
                    guncelle();
                },
                () -> {
                    boolean ok = ReservationManager.kralOdaSil();
                    if (!ok) warn("Kral", "Boş oda yok! Dolu odayı silemezsin.");
                    guncelle();
                }
        );
        mainContainer.add(cardKral);

        JScrollPane sp = new JScrollPane(mainContainer);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(sp);

        guncelle();
    }

    private void warn(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    private boolean isActiveStatus(String durum) {
        return durum != null && (durum.equals("Bekliyor") || durum.equals("Onaylandı"));
    }

    private void guncelle() {
        updateCard(cardStandart, "Standart",
                ReservationManager.getStandartTotal(),
                ReservationManager.getStandartKalan());

        updateCard(cardDeluxe, "Deluxe",
                ReservationManager.getDeluxeTotal(),
                ReservationManager.getDeluxeKalan());

        updateCard(cardKral, "Kral",
                ReservationManager.getKralTotal(),
                ReservationManager.getKralKalan());
    }

    private void updateCard(RoomCard card, String tip, int total, int empty) {
        List<Integer> dolu = new ArrayList<>();
        for (Reservation r : ReservationManager.getReservations()) {
            if (r == null) continue;
            if (!isActiveStatus(r.getDurum())) continue; // iptal/red/bitti -> boş say
            if (r.getOdaTipi() != null && r.getOdaTipi().contains(tip)) {
                dolu.add(r.getOdaNo());
            }
        }
        card.updateData(total, empty, dolu);
    }

    // ================= ROOM CARD =================
    class RoomCard extends JPanel {
        private JLabel lblStats;
        private JProgressBar bar;
        private JTextArea area;
        private ModernButton btnAdd, btnRemove;

        RoomCard(String title, Color color) {
            setLayout(new BorderLayout(15, 10));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                    new EmptyBorder(20, 20, 20, 20)
            ));
            setMaximumSize(new Dimension(2000, 190));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblTitle.setForeground(color);

            lblStats = new JLabel("Veri bekleniyor...");
            lblStats.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblStats.setForeground(Color.GRAY);

            btnAdd = new ModernButton("+ Oda Ekle", color);
            btnRemove = new ModernButton("- Boş Oda Sil", new Color(120, 120, 120));

            JPanel left = new JPanel(new GridLayout(4, 1, 0, 8));
            left.setBackground(Color.WHITE);
            left.setPreferredSize(new Dimension(260, 0));
            left.add(lblTitle);
            left.add(lblStats);
            left.add(btnAdd);
            left.add(btnRemove);

            bar = new JProgressBar(0, MAX_ROOM_LIMIT);
            bar.setStringPainted(true);
            bar.setFont(new Font("Segoe UI", Font.BOLD, 12));
            bar.setBorderPainted(false);
            bar.setBackground(new Color(240, 240, 240));

            area = new JTextArea();
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(new Font("Consolas", Font.PLAIN, 13));
            area.setBackground(new Color(250, 250, 250));
            area.setBorder(new EmptyBorder(8, 8, 8, 8));

            JPanel center = new JPanel(new BorderLayout(0, 8));
            center.setBackground(Color.WHITE);
            center.add(bar, BorderLayout.NORTH);
            center.add(new JScrollPane(area), BorderLayout.CENTER);

            add(left, BorderLayout.WEST);
            add(center, BorderLayout.CENTER);
        }

        void setActions(Runnable add, Runnable remove) {
            btnAdd.addActionListener(e -> add.run());
            btnRemove.addActionListener(e -> remove.run());
        }

        void updateData(int total, int empty, List<Integer> occupied) {
            lblStats.setText("Boş: " + empty + " | Toplam: " + total + "/" + MAX_ROOM_LIMIT);
            bar.setValue(total);
            bar.setString("Kapasite: " + total + " / " + MAX_ROOM_LIMIT);

            if (occupied.isEmpty()) {
                area.setForeground(Color.GRAY);
                area.setText("- Aktif rezervasyon yok (hepsi boş sayılır) -");
            } else {
                area.setForeground(new Color(60, 60, 60));
                area.setText("Dolu oda numaraları: " + occupied);
            }

            // İstersen butonu otomatik pasifle:
            // btnAdd.setEnabled(total < MAX_ROOM_LIMIT);
            // btnRemove.setEnabled(empty > 0);
        }
    }

    // ================= MODERN BUTTON =================
    class ModernButton extends JButton {
        private final Color c;

        ModernButton(String t, Color c) {
            super(t);
            this.c = c;
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(160, 34));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? c.darker() : c);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
