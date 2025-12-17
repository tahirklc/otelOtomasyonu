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

        // SOL: TABLO
        setupTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainContainer.add(scrollPane, BorderLayout.CENTER);

        // SAĞ: KONTROL PANELİ
        JPanel controlPanel = createControlPanel();
        mainContainer.add(controlPanel, BorderLayout.EAST);

        tabloyuDoldur();
    }

    // ======================= NAME FILTER =======================

    // ✅ Sadece harf + boşluk, max 30
    private static class NameOnlyMaxLenFilter extends DocumentFilter {
        private final int max;
        public NameOnlyMaxLenFilter(int max) { this.max = max; }

        private boolean allowed(String s) {
            return s.matches("^[a-zA-ZçğıöşüÇĞİÖŞÜ\\s]+$");
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;
            if (!allowed(string)) return;

            int cur = fb.getDocument().getLength();
            int allowedLen = max - cur;
            if (allowedLen <= 0) return;

            String cut = string.length() > allowedLen ? string.substring(0, allowedLen) : string;
            super.insertString(fb, offset, cut, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) text = "";
            if (!text.isEmpty() && !allowed(text)) return;

            int cur = fb.getDocument().getLength();
            int newLen = cur - length + text.length();
            if (newLen <= max) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }

            int allowedLen = max - (cur - length);
            if (allowedLen <= 0) return;
            String cut = text.length() > allowedLen ? text.substring(0, allowedLen) : text;
            super.replace(fb, offset, length, cut, attrs);
        }
    }

    private String askNameWithFilter(String title) {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new NameOnlyMaxLenFilter(30));

        JPanel p = new JPanel(new BorderLayout(10, 8));
        JLabel info = new JLabel("Sadece harf ve boşluk (maks 30 karakter)");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(Color.GRAY);

        p.add(info, BorderLayout.NORTH);
        p.add(tf, BorderLayout.CENTER);

        int res = JOptionPane.showConfirmDialog(
                this, p, title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (res != JOptionPane.OK_OPTION) return null;

        String val = tf.getText().trim();
        if (val.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "Ad Soyad en az 3 karakter olmalı.",
                    "Hata", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (!val.matches("^[a-zA-ZçğıöşüÇĞİÖŞÜ\\s]+$")) {
            JOptionPane.showMessageDialog(this,
                    "Ad Soyad sadece harf ve boşluk içermeli.",
                    "Hata", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return val;
    }

    // ======================= TABLE =======================

    private void setupTable() {
        String[] columns = {"Müşteri", "Oda Tipi", "Oda No", "Kişi", "Giriş", "Çıkış", "Fiyat", "Durum"};
        model = new DefaultTableModel(columns, 0);

        table = new JTable(model) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 45));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = String.valueOf(value);
                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));

                if ("Onaylandı".equals(status)) setForeground(STATUS_GREEN);
                else if ("Reddedildi".equals(status) || "İptal Edildi".equals(status)) setForeground(STATUS_RED);
                else if ("Bekliyor".equals(status)) setForeground(STATUS_ORANGE);
                else setForeground(Color.GRAY);

                return c;
            }
        });
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SIDEBAR_BG);
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblHeader = new JLabel("İşlemler");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(new Color(50, 50, 50));
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblHeader);
        panel.add(Box.createVerticalStrut(30));

        addSectionTitle(panel, "Durum Yönetimi");

        JButton btnOnayla = new ModernButton("✅ Onayla", STATUS_GREEN, Color.WHITE);
        btnOnayla.addActionListener(e -> durumDegistir("Onaylandı", false));
        panel.add(btnOnayla);
        panel.add(Box.createVerticalStrut(10));

        JButton btnReddet = new ModernButton("❌ Reddedildi", STATUS_RED, Color.WHITE);
        btnReddet.addActionListener(e -> durumDegistir("Reddedildi", true));
        panel.add(btnReddet);
        panel.add(Box.createVerticalStrut(20));

        addSectionTitle(panel, "Düzenleme");

        JButton btnGuncelle = new ModernButton("✏️ Güncelle", PRIMARY_COLOR, Color.WHITE);
        btnGuncelle.addActionListener(e -> rezervasyonGuncelle());
        panel.add(btnGuncelle);
        panel.add(Box.createVerticalStrut(10));

        JButton btnIptal = new ModernButton("🚫 İptal Et", Color.GRAY, Color.WHITE);
        btnIptal.addActionListener(e -> durumDegistir("İptal Edildi", true));
        panel.add(btnIptal);
        panel.add(Box.createVerticalStrut(20));

        addSectionTitle(panel, "Görünüm");

        JButton btnDetay = new ModernButton("📄 Detay Göster", new Color(0, 150, 136), Color.WHITE);
        btnDetay.addActionListener(e -> detayGoster());
        panel.add(btnDetay);
        panel.add(Box.createVerticalStrut(10));

        JButton btnYenile = new ModernButton("🔄 Tabloyu Yenile", new Color(255, 193, 7), Color.BLACK);
        btnYenile.addActionListener(e -> tabloyuDoldur());
        panel.add(btnYenile);

        return panel;
    }

    private void addSectionTitle(JPanel panel, String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.GRAY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(5));
    }

    private Reservation getSecili() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen tablodan bir rezervasyon seçiniz!", "Seçim Yok", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return ReservationManager.getReservations().get(row);
    }

    private void tabloyuDoldur() {
        model.setRowCount(0);
        for (Reservation r : ReservationManager.getReservations()) {
            model.addRow(new Object[]{
                    r.getMusteriAdi(),
                    r.getOdaTipi(),
                    r.getOdaNo(),
                    r.getKisiSayisi(),
                    r.getGirisTarihi(),
                    r.getCikisTarihi(),
                    r.getFiyat(),
                    r.getDurum()
            });
        }
    }

    private void durumDegistir(String durum, boolean odaIade) {
        Reservation r = getSecili();
        if (r == null) return;

        if (odaIade) {
            ReservationManager.odaIade(r.getOdaTipi(), r.getOdaNo());
        }

        r.setDurum(durum);

        if ("Onaylandı".equals(durum)) {
            String mesaj =
                    "Sayın " + r.getMusteriAdi() + ",\n\n" +
                            "Rezervasyonunuz ONAYLANMIŞTIR.\n\n" +
                            "Oda Tipi: " + r.getOdaTipi() + "\n" +
                            "Oda No: " + r.getOdaNo() + "\n" +
                            "Giriş Tarihi: " + r.getGirisTarihi() + "\n" +
                            "Çıkış Tarihi: " + r.getCikisTarihi() + "\n" +
                            "Toplam Ücret: " + r.getFiyat() + " TL\n\n" +
                            "İyi günler dileriz.\nOtel Yönetimi";

            EmailService.sendMail(r.getMusteriEmail(), "Rezervasyonunuz Onaylandı", mesaj);

            JOptionPane.showMessageDialog(this,
                    "Rezervasyon onaylandı ve müşteriye mail gönderildi.", "İşlem Başarılı", JOptionPane.INFORMATION_MESSAGE);
        }

        ReservationManager.saveToFile();
        tabloyuDoldur();
    }

    private void detayGoster() {
        Reservation r = getSecili();
        if (r == null) return;

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setText(
                "Müşteri:    " + r.getMusteriAdi() +
                        "\nOda Tipi:   " + r.getOdaTipi() +
                        "\nOda No:     " + r.getOdaNo() +
                        "\nKişi Sayısı:" + r.getKisiSayisi() +
                        "\nGiriş:      " + r.getGirisTarihi() +
                        "\nÇıkış:      " + r.getCikisTarihi() +
                        "\nFiyat:      " + r.getFiyat() + " TL" +
                        "\nDurum:      " + r.getDurum() +
                        "\n\n--- KONAKLAYACAK KİŞİLER ---\n" +
                        r.getKisiler()
        );
        area.setBorder(new EmptyBorder(10,10,10,10));

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(this, sp, "Rezervasyon Detayı", JOptionPane.PLAIN_MESSAGE);
    }

    private boolean tarihDogrula(String giris, String cikis) {
        try {
            LocalDate g = LocalDate.parse(giris, STRICT_DATE);
            LocalDate c = LocalDate.parse(cikis, STRICT_DATE);

            if (g.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Giriş tarihi geçmiş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (c.isBefore(g)) {
                JOptionPane.showMessageDialog(this, "Çıkış tarihi girişten önce olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Tarih formatı hatalı! (gg.aa.yyyy)", "Hata", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private List<String> parseKisiler(String kisilerStr) {
        List<String> list = new ArrayList<>();
        if (kisilerStr == null) return list;
        String[] satirlar = kisilerStr.split("\n");
        for (String s : satirlar) {
            if (!s.trim().isEmpty()) list.add(s.trim());
        }
        return list;
    }

    private String tarihDialogMaskeliKontrollu(String baslik, String eskiDeger) {
        final Color okBorder = new Color(180, 180, 180);
        final Color errBorder = new Color(220, 0, 0);

        JDialog dialog = new JDialog(this, baslik, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel info = new JLabel("Format: GG.AA.YYYY  (Gün 1–31, Ay 1–12)");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(Color.DARK_GRAY);

        JLabel err = new JLabel(" ");
        err.setFont(new Font("Segoe UI", Font.BOLD, 12));
        err.setForeground(new Color(220, 0, 0));

        JFormattedTextField tf;
        try {
            MaskFormatter mf = new MaskFormatter("##.##.####");
            mf.setPlaceholderCharacter('_');
            mf.setAllowsInvalid(false);
            tf = new JFormattedTextField(mf);
        } catch (Exception ex) {
            return null;
        }

        tf.setColumns(10);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tf.setText(eskiDeger == null ? "" : eskiDeger);
        tf.setBorder(new LineBorder(okBorder, 2, true));

        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("İptal");

        final String[] result = new String[1];

        Runnable validate = () -> {
            String t = tf.getText();

            if (t == null || t.contains("_")) {
                tf.setBorder(new LineBorder(errBorder, 2, true));
                err.setText("Tarih eksik girildi.");
                btnOk.setEnabled(false);
                return;
            }

            int gun, ay, yil;
            try {
                gun = Integer.parseInt(t.substring(0, 2));
                ay  = Integer.parseInt(t.substring(3, 5));
                yil = Integer.parseInt(t.substring(6, 10));
            } catch (Exception e) {
                tf.setBorder(new LineBorder(errBorder, 2, true));
                err.setText("Geçersiz giriş.");
                btnOk.setEnabled(false);
                return;
            }

            if (gun < 1 || gun > 31) {
                tf.setBorder(new LineBorder(errBorder, 2, true));
                err.setText("Gün 1–31 arasında olmalı.");
                btnOk.setEnabled(false);
                return;
            }
            if (ay < 1 || ay > 12) {
                tf.setBorder(new LineBorder(errBorder, 2, true));
                err.setText("Ay 1–12 arasında olmalı.");
                btnOk.setEnabled(false);
                return;
            }

            try {
                LocalDate.parse(t, STRICT_DATE);
            } catch (Exception e) {
                tf.setBorder(new LineBorder(errBorder, 2, true));
                err.setText("Takvimde olmayan bir tarih girdin.");
                btnOk.setEnabled(false);
                return;
            }

            tf.setBorder(new LineBorder(new Color(0, 140, 0), 2, true));
            err.setText(" ");
            btnOk.setEnabled(true);
        };

        tf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { SwingUtilities.invokeLater(validate); }
            public void removeUpdate(DocumentEvent e) { SwingUtilities.invokeLater(validate); }
            public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(validate); }
        });

        btnOk.addActionListener(e -> {
            result[0] = tf.getText();
            dialog.dispose();
        });
        btnCancel.addActionListener(e -> {
            result[0] = null;
            dialog.dispose();
        });

        JPanel center = new JPanel(new GridLayout(3, 1, 5, 5));
        center.add(info);
        center.add(tf);
        center.add(err);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnCancel);
        buttons.add(btnOk);

        root.add(center, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        SwingUtilities.invokeLater(validate);

        dialog.setVisible(true);
        return result[0];
    }

    private void rezervasyonGuncelle() {
        Reservation r = getSecili();
        if (r == null) return;

        // ✅ YENİ KURAL: Reddedildi / İptal Edildi ise güncelleme YOK
        String st = (r.getDurum() == null) ? "" : r.getDurum().trim();
        if (st.equalsIgnoreCase("Reddedildi") || st.equalsIgnoreCase("İptal Edildi")) {
            JOptionPane.showMessageDialog(this,
                    "Bu rezervasyon '" + r.getDurum() + "' durumunda.\nGüncelleme yapılamaz.",
                    "Güncelleme Kapalı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] secenekler = {"Kişi Ekle", "Kişi Sil", "Tarih Güncelle"};
        int secim = JOptionPane.showOptionDialog(this, "Ne yapmak istiyorsun?",
                "Rezervasyon Güncelle",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, secenekler, secenekler[0]);

        List<String> kisiList = parseKisiler(r.getKisiler());

        if (secim == 0) {
            String ad = askNameWithFilter("Ad Soyad Gir");
            if (ad == null) return;

            String tc = "";
            while (true) {
                tc = JOptionPane.showInputDialog(this, "TC Kimlik (11 Hane):");
                if (tc == null) return;

                if (!tc.matches("\\d{11}")) {
                    JOptionPane.showMessageDialog(this, "TC 11 haneli rakam olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                int lastDigit = Character.getNumericValue(tc.charAt(10));
                if (lastDigit % 2 != 0) {
                    JOptionPane.showMessageDialog(this, "TC son hanesi çift olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                break;
            }

            String dogum = "";
            while (true) {
                String tmp = tarihDialogMaskeliKontrollu("Doğum Tarihi", "");
                if (tmp == null) return;
                dogum = tmp;
                try {
                    LocalDate d = LocalDate.parse(dogum, STRICT_DATE);
                    if (d.isAfter(LocalDate.now())) {
                        JOptionPane.showMessageDialog(this, "Doğum tarihi gelecekte olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
                    } else break;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Tarih formatı hatalı! (gg.aa.yyyy)", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }

            kisiList.add("Ad Soyad: " + ad + " | TC: " + tc + " | Doğum: " + dogum);
            r.setKisiSayisi(kisiList.size());
        }
        else if (secim == 1 && !kisiList.isEmpty()) {
            String secilen = (String) JOptionPane.showInputDialog(
                    this, "Silinecek kişiyi seç:",
                    "Kişi Sil", JOptionPane.QUESTION_MESSAGE,
                    null, kisiList.toArray(), kisiList.get(0)
            );

            if (secilen != null) {
                kisiList.remove(secilen);
                r.setKisiSayisi(kisiList.size());
            }
        }
        else if (secim == 2) {
            String giris = tarihDialogMaskeliKontrollu("Yeni Giriş Tarihi", r.getGirisTarihi());
            if (giris == null) return;

            String cikis = tarihDialogMaskeliKontrollu("Yeni Çıkış Tarihi", r.getCikisTarihi());
            if (cikis == null) return;

            if (!tarihDogrla(giris, cikis)) return;

            r.setGirisTarihi(giris);
            r.setCikisTarihi(cikis);
        } else {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String k : kisiList) sb.append(k).append("\n");
        r.setKisiler(sb.toString());

        ReservationManager.saveToFile();
        tabloyuDoldur();

        JOptionPane.showMessageDialog(this, "Güncelleme başarıyla kaydedildi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean tarihDogrla(String giris, String cikis) {
        return tarihDogrula(giris, cikis);
    }

    // --- MODERN BUTON TASARIMI ---
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
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(240, 45));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) g2.setColor(baseColor.darker());
            else if (getModel().isRollover()) g2.setColor(baseColor.brighter());
            else g2.setColor(baseColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
