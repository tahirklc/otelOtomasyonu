package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.*;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

import service.ReservationManager;
import service.EmailService;
import model.Reservation;

public class AdminReservationManagerPage extends JFrame {

    private DefaultTableModel model;
    private JTable table;

    // --- RENK PALETİ ---
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color SIDEBAR_BG = new Color(255, 255, 255);

    // Durum Renkleri
    private final Color STATUS_GREEN = new Color(46, 125, 50);
    private final Color STATUS_RED = new Color(198, 40, 40);
    private final Color STATUS_ORANGE = new Color(239, 108, 0);

    private final DateTimeFormatter STRICT_DATE =
            DateTimeFormatter.ofPattern("dd.MM.uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    public AdminReservationManagerPage() {
        setTitle("Admin - Rezervasyon Yönetim Paneli");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_COLOR);
        setContentPane(mainContainer);

        setupTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainContainer.add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = createControlPanel();
        mainContainer.add(controlPanel, BorderLayout.EAST);

        tabloyuDoldur();
    }

    // ======================= HELPER METHODS =======================

    // Arka planda mail gönderme yardımcısı (UI donmasını engeller)
    private void mailGonderArkaplan(String kime, String konu, String icerik) {
        new Thread(() -> {
            EmailService.sendMail(kime, konu, icerik);
        }).start();
    }

    // Ortak mail gövdesi oluşturucu
    private String rezervasyonDetayMetni(Reservation r) {
        return "\nOda Tipi: " + r.getOdaTipi() +
               "\nOda No: " + r.getOdaNo() +
               "\nGiriş Tarihi: " + r.getGirisTarihi() +
               "\nÇıkış Tarihi: " + r.getCikisTarihi() +
               "\nToplam Ücret: " + r.getFiyat() + " TL\n";
    }

    // ======================= STATUS ACTIONS =======================

    private void durumDegistir(String durum, boolean odaIade) {
        Reservation r = getSecili();
        if (r == null) return;

        if (odaIade) {
            ReservationManager.odaIade(r.getOdaTipi(), r.getOdaNo());
        }

        r.setDurum(durum);
        String subject = "";
        String messageBody = "Sayın " + r.getMusteriAdi() + ",\n\n";

        if ("Onaylandı".equals(durum)) {
            subject = "Rezervasyonunuz ONAYLANMIŞTIR ✅";
            messageBody += "Otelimize yaptığınız rezervasyon onaylanmıştır. Sizi ağırlamaktan mutluluk duyacağız.\n" 
                        + rezervasyonDetayMetni(r);
        } 
        else if ("Reddedildi".equals(durum)) {
            subject = "Rezervasyon Talebiniz Hakkında ❌";
            messageBody += "Üzgünüz, otelimizdeki yoğunluk veya teknik nedenlerden dolayı rezervasyon talebinizi onaylayamıyoruz.\n"
                        + "Ödemeniz yapıldıysa iadesi gerçekleştirilecektir.";
        } 
        else if ("İptal Edildi".equals(durum)) {
            subject = "Rezervasyon İptal Onayı 🚫";
            messageBody += "Talebiniz üzerine rezervasyonunuz sistemimizden iptal edilmiştir.\n"
                        + rezervasyonDetayMetni(r) + "\nTekrar görüşmek dileğiyle.";
        }

        // Maili gönder
        mailGonderArkaplan(r.getMusteriEmail(), subject, messageBody + "\n\nİyi günler dileriz.\nOtel Yönetimi");

        ReservationManager.saveToFile();
        tabloyuDoldur();
        JOptionPane.showMessageDialog(this, "İşlem başarılı. Müşteriye bildirim maili gönderiliyor.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void rezervasyonGuncelle() {
        Reservation r = getSecili();
        if (r == null) return;

        String st = (r.getDurum() == null) ? "" : r.getDurum().trim();
        if (st.equalsIgnoreCase("Reddedildi") || st.equalsIgnoreCase("İptal Edildi")) {
            JOptionPane.showMessageDialog(this, "Pasif rezervasyonlar güncellenemez.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] secenekler = {"Kişi Ekle", "Kişi Sil", "Tarih Güncelle"};
        int secim = JOptionPane.showOptionDialog(this, "Hangi bilgiyi güncellemek istersiniz?", "Güncelle",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, secenekler, secenekler[0]);

        List<String> kisiList = parseKisiler(r.getKisiler());
        boolean degisti = false;

        if (secim == 0) { // Kişi Ekle
            String ad = askNameWithFilter("Yeni Kişi Adı");
            if (ad != null) {
                kisiList.add("Ad Soyad: " + ad);
                degisti = true;
            }
        } else if (secim == 1 && !kisiList.isEmpty()) { // Kişi Sil
            String secilen = (String) JOptionPane.showInputDialog(this, "Silinecek kişi:", "Sil", 
                    JOptionPane.QUESTION_MESSAGE, null, kisiList.toArray(), kisiList.get(0));
            if (secilen != null) {
                kisiList.remove(secilen);
                degisti = true;
            }
        } else if (secim == 2) { // Tarih
            String giris = tarihDialogMaskeliKontrollu("Yeni Giriş", r.getGirisTarihi());
            String cikis = tarihDialogMaskeliKontrollu("Yeni Çıkış", r.getCikisTarihi());
            if (giris != null && cikis != null && tarihDogrula(giris, cikis)) {
                r.setGirisTarihi(giris);
                r.setCikisTarihi(cikis);
                degisti = true;
            }
        }

        if (degisti) {
            r.setKisiSayisi(kisiList.size());
            StringBuilder sb = new StringBuilder();
            for (String k : kisiList) sb.append(k).append("\n");
            r.setKisiler(sb.toString());

            ReservationManager.saveToFile();
            tabloyuDoldur();

            // Güncelleme Maili
            String updateMail = "Sayın " + r.getMusteriAdi() + ",\n\nRezervasyon bilgileriniz güncellenmiştir.\n"
                                + "Güncel Bilgileriniz:\n" + rezervasyonDetayMetni(r)
                                + "\nKişi Sayısı: " + r.getKisiSayisi() + "\n\nKeyifli konaklamalar dileriz.";
            
            mailGonderArkaplan(r.getMusteriEmail(), "Rezervasyon Bilgileriniz Güncellendi ✏️", updateMail);
            
            JOptionPane.showMessageDialog(this, "Güncellendi ve müşteriye bilgi verildi.");
        }
    }

    // ======================= UI COMPONENTS =======================

    private void setupTable() {
        String[] columns = {"Müşteri", "Oda Tipi", "Oda No", "Kişi", "Giriş", "Çıkış", "Fiyat", "Durum"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model) { public boolean isCellEditable(int row, int col) { return false; } };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.setSelectionBackground(new Color(232, 240, 254));
        
        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(center);

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                String status = String.valueOf(v);
                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                if ("Onaylandı".equals(status)) setForeground(STATUS_GREEN);
                else if ("Reddedildi".equals(status) || "İptal Edildi".equals(status)) setForeground(STATUS_RED);
                else if ("Bekliyor".equals(status)) setForeground(STATUS_ORANGE);
                return comp;
            }
        });
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SIDEBAR_BG);
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(230,230,230)), new EmptyBorder(20,20,20,20)));

        JLabel lblHeader = new JLabel("İşlemler");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblHeader); panel.add(Box.createVerticalStrut(30));

        addSectionTitle(panel, "Durum Yönetimi");
        panel.add(new ModernButton("✅ Onayla", STATUS_GREEN, Color.WHITE, e -> durumDegistir("Onaylandı", false)));
        panel.add(Box.createVerticalStrut(10));
        panel.add(new ModernButton("❌ Reddet", STATUS_RED, Color.WHITE, e -> durumDegistir("Reddedildi", true)));
        panel.add(Box.createVerticalStrut(20));

        addSectionTitle(panel, "Düzenleme");
        panel.add(new ModernButton("✏️ Güncelle", PRIMARY_COLOR, Color.WHITE, e -> rezervasyonGuncelle()));
        panel.add(Box.createVerticalStrut(10));
        panel.add(new ModernButton("🚫 İptal Et", Color.GRAY, Color.WHITE, e -> durumDegistir("İptal Edildi", true)));
        panel.add(Box.createVerticalStrut(20));

        addSectionTitle(panel, "Görünüm");
        panel.add(new ModernButton("📄 Detay Göster", new Color(0,150,136), Color.WHITE, e -> detayGoster()));
        panel.add(Box.createVerticalStrut(10));
        panel.add(new ModernButton("🔄 Yenile", new Color(255,193,7), Color.BLACK, e -> tabloyuDoldur()));

        return panel;
    }

    private void addSectionTitle(JPanel p, String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Color.GRAY); l.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(l); p.add(Box.createVerticalStrut(5));
    }

    // ======================= DIALOGS & UTILS =======================

    private Reservation getSecili() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen bir kayıt seçin.");
            return null;
        }
        return ReservationManager.getReservations().get(row);
    }

    private void tabloyuDoldur() {
        model.setRowCount(0);
        for (Reservation r : ReservationManager.getReservations()) {
            model.addRow(new Object[]{r.getMusteriAdi(), r.getOdaTipi(), r.getOdaNo(), r.getKisiSayisi(), r.getGirisTarihi(), r.getCikisTarihi(), r.getFiyat(), r.getDurum()});
        }
    }

    private void detayGoster() {
        Reservation r = getSecili();
        if (r == null) return;
        JTextArea area = new JTextArea(15, 30);
        area.setEditable(false);
        area.setText("Müşteri: " + r.getMusteriAdi() + "\nEmail: " + r.getMusteriEmail() + "\nOda: " + r.getOdaNo() + " (" + r.getOdaTipi() + ")\nFiyat: " + r.getFiyat() + " TL\n\nKişiler:\n" + r.getKisiler());
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Detay", JOptionPane.PLAIN_MESSAGE);
    }

    private List<String> parseKisiler(String s) {
        List<String> l = new ArrayList<>();
        if (s == null) return l;
        for (String line : s.split("\n")) if (!line.trim().isEmpty()) l.add(line.trim());
        return l;
    }

    private boolean tarihDogrula(String g, String c) {
        try {
            LocalDate d1 = LocalDate.parse(g, STRICT_DATE);
            LocalDate d2 = LocalDate.parse(c, STRICT_DATE);
            if (d1.isBefore(LocalDate.now())) { JOptionPane.showMessageDialog(this, "Giriş geçmiş olamaz."); return false; }
            if (d2.isBefore(d1)) { JOptionPane.showMessageDialog(this, "Çıkış girişten önce olamaz."); return false; }
            return true;
        } catch (Exception e) { return false; }
    }

    private String askNameWithFilter(String title) {
        JTextField tf = new JTextField(20);
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new NameOnlyMaxLenFilter(30));
        int res = JOptionPane.showConfirmDialog(this, tf, title, JOptionPane.OK_CANCEL_OPTION);
        return (res == JOptionPane.OK_OPTION) ? tf.getText().trim() : null;
    }

    private String tarihDialogMaskeliKontrollu(String baslik, String eski) {
        try {
            MaskFormatter mf = new MaskFormatter("##.##.####");
            mf.setPlaceholderCharacter('_');
            JFormattedTextField tf = new JFormattedTextField(mf);
            tf.setText(eski);
            int res = JOptionPane.showConfirmDialog(this, tf, baslik, JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                String val = tf.getText();
                LocalDate.parse(val, STRICT_DATE); // Geçerlilik testi
                return val;
            }
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Geçersiz Tarih"); }
        return null;
    }

    // --- INNER CLASSES ---

    private static class NameOnlyMaxLenFilter extends DocumentFilter {
        private int max;
        public NameOnlyMaxLenFilter(int m) { this.max = m; }
        @Override
        public void replace(FilterBypass fb, int o, int l, String t, AttributeSet a) throws BadLocationException {
            if (t.matches("^[a-zA-ZçğıöşüÇĞİÖŞÜ\\s]*$") && (fb.getDocument().getLength() - l + t.length() <= max))
                super.replace(fb, o, l, t, a);
        }
    }

    class ModernButton extends JButton {
        private Color base;
        public ModernButton(String t, Color bg, Color fg, java.awt.event.ActionListener al) {
            super(t); this.base = bg;
            setFont(new Font("Segoe UI", Font.BOLD, 14)); setForeground(fg);
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setAlignmentX(CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(240, 40)); addActionListener(al);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? base.darker() : getModel().isRollover() ? base.brighter() : base);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            g2.dispose(); super.paintComponent(g);
        }
    }
}