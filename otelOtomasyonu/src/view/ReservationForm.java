package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;

import service.ReservationManager;
import model.Reservation;

public class ReservationForm extends JFrame {

    private JComboBox<String> odaSecim;
    private JComboBox<Integer> kisiSayisiSecim;

    // ✅ Artık mask yok: kilitleme yok, silme serbest
    private JTextField girisTarihi;
    private JTextField cikisTarihi;

    private JTextField emailField;

    private JPanel kisiContainerPanel;
    private JLabel fiyatLabel;

    private String kullaniciAdi;

    // Renk Paleti
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private final DateTimeFormatter STRICT_DATE =
            DateTimeFormatter.ofPattern("dd.MM.uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    public ReservationForm(String kullaniciAdi) {
        this.kullaniciAdi = kullaniciAdi;

        setTitle("Yeni Rezervasyon");
        setSize(550, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        setContentPane(mainPanel);

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Rezervasyon Oluştur");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // CONTENT
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Genel bilgiler
        JPanel infoPanel = createSectionPanel("Genel Bilgiler");
        infoPanel.setLayout(new GridLayout(5, 2, 10, 15));

        // Oda tipi
        infoPanel.add(createLabel("Oda Tipi:"));
        odaSecim = new JComboBox<>(new String[]{
                "Standart Oda - 1000 TL",
                "Deluxe Oda - 2000 TL",
                "Kral Dairesi - 5000 TL"
        });
        styleComboBox(odaSecim);
        odaSecim.addActionListener(e -> fiyatHesapla());
        infoPanel.add(odaSecim);

        // Kişi sayısı
        infoPanel.add(createLabel("Kişi Sayısı:"));
        kisiSayisiSecim = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        styleComboBox(kisiSayisiSecim);
        kisiSayisiSecim.addActionListener(e -> {
            kisiAlanlariniOlustur();
            fiyatHesapla();
        });
        infoPanel.add(kisiSayisiSecim);

        // ✅ Giriş / Çıkış tarih (kilitlemez, silme serbest, otomatik nokta, canlı kontrol)
        infoPanel.add(createLabel("Giriş (GG.AA.YYYY):"));
        girisTarihi = createDateField();
        infoPanel.add(girisTarihi);

        infoPanel.add(createLabel("Çıkış (GG.AA.YYYY):"));
        cikisTarihi = createDateField();
        infoPanel.add(cikisTarihi);

        // Email
        infoPanel.add(createLabel("E-Posta Adresi:"));
        emailField = createTextField();
        ((AbstractDocument) emailField.getDocument()).setDocumentFilter(new MaxLenFilter(60));
        autoFocusWhenLength(emailField, 60);
        infoPanel.add(emailField);

        contentPanel.add(infoPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Misafir alanları
        JLabel lblGuestHeader = new JLabel("Misafir Bilgileri");
        lblGuestHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGuestHeader.setForeground(PRIMARY_COLOR);
        lblGuestHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblGuestHeader);
        contentPanel.add(Box.createVerticalStrut(10));

        kisiContainerPanel = new JPanel();
        kisiContainerPanel.setLayout(new BoxLayout(kisiContainerPanel, BoxLayout.Y_AXIS));
        kisiContainerPanel.setBackground(BG_COLOR);
        contentPanel.add(kisiContainerPanel);

        kisiAlanlariniOlustur();

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // FOOTER
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pricePanel.setBackground(Color.WHITE);

        JLabel lblTotal = new JLabel("Toplam Tutar: ");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        fiyatLabel = new JLabel("0 TL");
        fiyatLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        fiyatLabel.setForeground(new Color(46, 125, 50));

        pricePanel.add(lblTotal);
        pricePanel.add(fiyatLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnHesapla = new JButton("Fiyat Hesapla");
        styleButton(btnHesapla, new Color(255, 152, 0));
        btnHesapla.addActionListener(e -> fiyatHesapla());

        JButton btnOlustur = new JButton("Tamamla");
        styleButton(btnOlustur, PRIMARY_COLOR);
        btnOlustur.setPreferredSize(new Dimension(120, 35));
        btnOlustur.addActionListener(e -> rezervasyonOlustur());

        btnPanel.add(btnHesapla);
        btnPanel.add(btnOlustur);

        footerPanel.add(pricePanel, BorderLayout.WEST);
        footerPanel.add(btnPanel, BorderLayout.EAST);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // tarih değişince fiyatı da güncelle
        attachPriceAutoUpdate(girisTarihi);
        attachPriceAutoUpdate(cikisTarihi);
    }

    // ---------------- UI ----------------

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));
        return p;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LABEL_FONT);
        l.setForeground(Color.GRAY);
        return l;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(INPUT_FONT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return tf;
    }

    private void styleComboBox(JComboBox box) {
        box.setFont(INPUT_FONT);
        box.setBackground(Color.WHITE);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setFieldBorder(JComponent c, Color borderColor) {
        c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 2, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
    }

    // ✅ Tarih alanı: kilit yok, silme serbest, sadece rakam + otomatik nokta, canlı kontrol
    private JTextField createDateField() {
        JTextField tf = createTextField();
        tf.setToolTipText("Format: GG.AA.YYYY (Gün 1–31, Ay 1–12)");

        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DateTypingFilter());

        tf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { SwingUtilities.invokeLater(() -> validateDateTextField(tf)); }
            public void removeUpdate(DocumentEvent e) { SwingUtilities.invokeLater(() -> validateDateTextField(tf)); }
            public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(() -> validateDateTextField(tf)); }
        });

        // 10 karakter olunca focus geç
        autoFocusWhenLength(tf, 10);

        SwingUtilities.invokeLater(() -> validateDateTextField(tf));
        return tf;
    }

    // ✅ JTextField için tarih doğrulama (silme her zaman serbest)
    private void validateDateTextField(JTextField tf) {
        String t = tf.getText();

        // daha tamamlanmadıysa kırmızı
        if (t == null || t.length() < 10) {
            setFieldBorder(tf, new Color(220, 0, 0));
            return;
        }

        // format kontrolü
        if (t.length() != 10 || t.charAt(2) != '.' || t.charAt(5) != '.') {
            setFieldBorder(tf, new Color(220, 0, 0));
            return;
        }

        try {
            int gun = Integer.parseInt(t.substring(0, 2));
            int ay = Integer.parseInt(t.substring(3, 5));

            if (gun < 1 || gun > 31) { setFieldBorder(tf, new Color(220, 0, 0)); return; }
            if (ay < 1 || ay > 12) { setFieldBorder(tf, new Color(220, 0, 0)); return; }

            LocalDate.parse(t, STRICT_DATE); // STRICT takvim kontrol
            setFieldBorder(tf, new Color(0, 140, 0));
        } catch (Exception ex) {
            setFieldBorder(tf, new Color(220, 0, 0));
        }
    }

    // ✅ maxLen dolunca focus geç (silmede geçmez)
    private void autoFocusWhenLength(JTextField field, int maxLen) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            private void check() {
                SwingUtilities.invokeLater(() -> {
                    String t = field.getText();
                    if (t != null && t.length() >= maxLen) {
                        field.transferFocus();
                    }
                });
            }
            public void insertUpdate(DocumentEvent e) { check(); }
            public void removeUpdate(DocumentEvent e) { }
            public void changedUpdate(DocumentEvent e) { check(); }
        });
    }

    private void attachPriceAutoUpdate(JTextField tf) {
        tf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { SwingUtilities.invokeLater(() -> fiyatHesapla()); }
            public void removeUpdate(DocumentEvent e) { SwingUtilities.invokeLater(() -> fiyatHesapla()); }
            public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(() -> fiyatHesapla()); }
        });
    }

    // ---------------- MANTIK ----------------

    private LocalDate parseStrictDate(String dateStr) {
        try {
            if (dateStr == null) return null;
            if (dateStr.length() != 10) return null;
            return LocalDate.parse(dateStr, STRICT_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    private void kisiAlanlariniOlustur() {
        kisiContainerPanel.removeAll();
        int kisiSayisi = (int) kisiSayisiSecim.getSelectedItem();

        for (int i = 1; i <= kisiSayisi; i++) {
            JPanel card = new JPanel(new GridLayout(3, 2, 10, 10));
            card.setBackground(Color.WHITE);

            TitledBorder border = BorderFactory.createTitledBorder(
                    new LineBorder(new Color(200, 200, 200), 1, true),
                    i + ". Misafir Bilgileri"
            );
            border.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
            border.setTitleColor(PRIMARY_COLOR);
            card.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(10, 10, 10, 10)));
            card.setMaximumSize(new Dimension(2000, 160));

            // ✅ Ad Soyad: sadece harf+boşluk, max 30, dolunca focus TC'ye geç
            card.add(createLabel("Ad Soyad:"));
            JTextField nameField = createTextField();
            ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new NameFilter(30));
            autoFocusWhenLength(nameField, 30);
            card.add(nameField);

            // ✅ TC: sadece rakam, max 11, ilk hane 0 olamaz, 11 dolunca focus doğum'a geç
            card.add(createLabel("TC Kimlik:"));
            JTextField tcField = createTextField();
            ((AbstractDocument) tcField.getDocument()).setDocumentFilter(new TcKimlikFilter());
            autoFocusWhenLength(tcField, 11);
            card.add(tcField);

            // ✅ Doğum tarihi: aynı tarih alanı (kilitlemez)
            card.add(createLabel("Doğum Tarihi (GG.AA.YYYY):"));
            JTextField birthField = createDateField();
            card.add(birthField);

            kisiContainerPanel.add(card);
            kisiContainerPanel.add(Box.createVerticalStrut(10));
        }

        kisiContainerPanel.revalidate();
        kisiContainerPanel.repaint();
    }

    private String kisileriTopla() {
        StringBuilder sb = new StringBuilder();

        for (Component c : kisiContainerPanel.getComponents()) {
            if (c instanceof JPanel) {
                JPanel card = (JPanel) c;

                String ad = ((JTextField) card.getComponent(1)).getText().trim();
                String tc = ((JTextField) card.getComponent(3)).getText().trim();
                String dogumStr = ((JTextField) card.getComponent(5)).getText().trim();

                sb.append("Ad Soyad: ").append(ad)
                        .append(" | TC: ").append(tc)
                        .append(" | Doğum: ").append(dogumStr)
                        .append("\n");
            }
        }
        return sb.toString();
    }

    private void fiyatHesapla() {
        LocalDate giris = parseStrictDate(girisTarihi.getText());
        LocalDate cikis = parseStrictDate(cikisTarihi.getText());

        if (giris == null || cikis == null || cikis.isBefore(giris)) return;

        String oda = (String) odaSecim.getSelectedItem();
        int kisi = (int) kisiSayisiSecim.getSelectedItem();

        long gun = Math.max(ChronoUnit.DAYS.between(giris, cikis), 1);

        int birimFiyat = oda.contains("Standart") ? 1000 :
                oda.contains("Deluxe") ? 2000 : 5000;

        fiyatLabel.setText((birimFiyat * kisi * gun) + " TL");
    }

    private String validateGuestInputs() {
        int index = 1;
        for (Component c : kisiContainerPanel.getComponents()) {
            if (c instanceof JPanel) {
                JPanel card = (JPanel) c;

                JTextField txtName = (JTextField) card.getComponent(1);
                JTextField txtTC = (JTextField) card.getComponent(3);
                JTextField txtBirth = (JTextField) card.getComponent(5);

                String name = txtName.getText().trim();
                String tc = txtTC.getText().trim();
                String birth = txtBirth.getText().trim();

                if (name.isEmpty() || tc.isEmpty() || birth.isEmpty() || birth.length() != 10) {
                    return index + ". Misafir için tüm alanlar doldurulmalıdır.";
                }

                if (name.length() < 3 || !name.matches("^[a-zA-ZçğıöşüÇĞİÖŞÜ\\s]+$")) {
                    return index + ". Misafir Adı geçersiz! (En az 3 karakter, sadece harf)";
                }

                if (!tc.matches("\\d{11}")) {
                    return index + ". Misafir TC Kimlik No 11 haneli ve sadece rakam olmalıdır.";
                }
                if (tc.charAt(0) == '0') {
                    return index + ". Misafir TC Kimlik No 0 ile başlayamaz.";
                }
                int lastDigit = Character.getNumericValue(tc.charAt(10));
                if (lastDigit % 2 != 0) {
                    return index + ". Misafir TC Kimlik No son hanesi çift olmalıdır.";
                }

                try {
                    LocalDate birthDate = LocalDate.parse(birth, STRICT_DATE);
                    if (birthDate.isAfter(LocalDate.now())) {
                        return index + ". Misafir doğum tarihi gelecekte olamaz.";
                    }
                } catch (Exception e) {
                    return index + ". Misafir doğum tarihi geçersiz (GG.AA.YYYY).";
                }

                index++;
            }
        }
        return null;
    }

    private void rezervasyonOlustur() {
        LocalDate giris = parseStrictDate(girisTarihi.getText());
        LocalDate cikis = parseStrictDate(cikisTarihi.getText());
        LocalDate bugun = LocalDate.now();

        if (giris == null || cikis == null) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli giriş ve çıkış tarihleri giriniz (GG.AA.YYYY)!", "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (giris.isBefore(bugun)) {
            JOptionPane.showMessageDialog(this, "Geçmiş bir tarihe rezervasyon yapılamaz!", "Tarih Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cikis.isBefore(giris) || cikis.isEqual(giris)) {
            JOptionPane.showMessageDialog(this, "Çıkış tarihi, giriş tarihinden en az 1 gün sonra olmalıdır.", "Tarih Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (giris.isAfter(bugun.plusMonths(6))) {
            JOptionPane.showMessageDialog(this, "En fazla 6 ay sonrasına rezervasyon yapabilirsiniz!", "İleri Tarih Sınırı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long gunSayisi = ChronoUnit.DAYS.between(giris, cikis);
        if (gunSayisi > 30) {
            JOptionPane.showMessageDialog(this, "Maksimum konaklama süresi 30 gündür.\nSeçilen süre: " + gunSayisi + " gün.", "Süre Sınırı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = emailField.getText().trim();
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Geçerli bir e-posta adresi giriniz!", "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String validationError = validateGuestInputs();
        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError, "Eksik/Hatalı Bilgi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String oda = (String) odaSecim.getSelectedItem();

        if (!ReservationManager.odaBosMu(oda)) {
            JOptionPane.showMessageDialog(this, "Seçilen oda türünde şu an boş oda bulunmamaktadır.", "Doluluk Uyarısı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int kisiSayisi = (int) kisiSayisiSecim.getSelectedItem();
        fiyatHesapla();
        double fiyat = Double.parseDouble(fiyatLabel.getText().replace(" TL", ""));

        String kisiler = kisileriTopla();

        Reservation r = new Reservation(
                kullaniciAdi,
                email,
                oda,
                kisiSayisi,
                kisiler,
                girisTarihi.getText(),
                cikisTarihi.getText(),
                fiyat
        );

        ReservationManager.addReservation(r);

        JOptionPane.showMessageDialog(this,
                "Rezervasyonunuz başarıyla oluşturuldu!\n\n" +
                        "Oda Numaranız: " + r.getOdaNo() + "\n" +
                        "Kayıtlı E-Posta: " + email + "\n" +
                        "Toplam Tutar: " + fiyat + " TL",
                "Başarılı", JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }

    // ---------------- FILTERLAR ----------------

    // ✅ sadece max uzunluk
    private static class MaxLenFilter extends DocumentFilter {
        private final int max;
        public MaxLenFilter(int max) { this.max = max; }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;
            if (fb.getDocument().getLength() + string.length() <= max) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) text = "";
            if (fb.getDocument().getLength() - length + text.length() <= max) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    // ✅ Ad Soyad: sadece harf+boşluk, maxLen
    private static class NameFilter extends DocumentFilter {
        private final int max;
        public NameFilter(int max) { this.max = max; }

        private boolean isAllowed(String s) {
            return s.matches("^[a-zA-ZçğıöşüÇĞİÖŞÜ\\s]+$");
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;
            if (!isAllowed(string)) return;
            if (fb.getDocument().getLength() + string.length() <= max) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) text = "";
            if (!text.isEmpty() && !isAllowed(text)) return;
            if (fb.getDocument().getLength() - length + text.length() <= max) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    // ✅ TC: sadece rakam, max 11, ilk hane 0 olamaz
    private static class TcKimlikFilter extends DocumentFilter {

        private boolean firstDigitZeroWouldHappen(FilterBypass fb, int offset, int length, String text)
                throws BadLocationException {

            if (text == null || text.isEmpty()) return false;

            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            StringBuilder sb = new StringBuilder(current);
            sb.replace(offset, offset + length, text);

            if (sb.length() == 0) return false;
            return sb.charAt(0) == '0';
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {

            if (string == null) return;
            if (!string.matches("\\d+")) return;
            if (fb.getDocument().getLength() + string.length() > 11) return;
            if (firstDigitZeroWouldHappen(fb, offset, 0, string)) return;

            super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {

            if (text == null) text = "";
            if (!text.isEmpty() && !text.matches("\\d+")) return;
            if (fb.getDocument().getLength() - length + text.length() > 11) return;
            if (!text.isEmpty() && firstDigitZeroWouldHappen(fb, offset, length, text)) return;

            super.replace(fb, offset, length, text, attrs);
        }
    }

    // ✅ Tarih yazımı filtresi: sadece rakam, otomatik nokta, silme serbest
    private static class DateTypingFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {

            if (text == null) text = "";

            // silme serbest
            if (text.isEmpty()) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }

            // sadece rakam kabul (noktayı biz koyuyoruz)
            String onlyDigits = text.replaceAll("[^0-9]", "");
            if (onlyDigits.isEmpty()) return;

            String current = fb.getDocument().getText(0, fb.getDocument().getLength());

            // mevcut metni digit'e indir
            String currDigits = current.replaceAll("[^0-9]", "");

            // offset/length ile uğraşmadan: en stabil yöntem -> mevcut digit sonuna ekle
            // (kullanıcı araya yazmak isterse de yazsın, biz yine formatlayıp koyuyoruz)
            String mergedDigits = (currDigits + onlyDigits);
            if (mergedDigits.length() > 8) mergedDigits = mergedDigits.substring(0, 8);

            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < mergedDigits.length(); i++) {
                if (i == 2 || i == 4) formatted.append('.');
                formatted.append(mergedDigits.charAt(i));
            }

            String finalText = formatted.toString();
            if (finalText.length() > 10) finalText = finalText.substring(0, 10);

            fb.replace(0, fb.getDocument().getLength(), finalText, attrs);
        }
    }
}
