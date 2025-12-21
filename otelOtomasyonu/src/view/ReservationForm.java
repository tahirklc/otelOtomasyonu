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
import java.util.ArrayList;
import java.util.List;

import service.ReservationManager;
import service.EmailService; // Mail servisi eklendi
import model.Reservation;

public class ReservationForm extends JFrame {

    private JComboBox<String> odaSecim;
    private JComboBox<Integer> kisiSayisiSecim;
    private JTextField girisTarihi;
    private JTextField cikisTarihi;
    private JTextField emailField;
    private JPanel kisiContainerPanel;
    private JLabel fiyatLabel;
    private String kullaniciAdi;

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

        JPanel infoPanel = createSectionPanel("Genel Bilgiler");
        infoPanel.setLayout(new GridLayout(5, 2, 10, 15));

        infoPanel.add(createLabel("Oda Tipi:"));
        odaSecim = new JComboBox<>(new String[]{
                "Standart Oda - 1000 TL",
                "Deluxe Oda - 2000 TL",
                "Kral Dairesi - 5000 TL"
        });
        styleComboBox(odaSecim);
        odaSecim.addActionListener(e -> fiyatHesapla());
        infoPanel.add(odaSecim);

        infoPanel.add(createLabel("Kişi Sayısı:"));
        kisiSayisiSecim = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        styleComboBox(kisiSayisiSecim);
        kisiSayisiSecim.addActionListener(e -> {
            kisiAlanlariniOlustur();
            fiyatHesapla();
        });
        infoPanel.add(kisiSayisiSecim);

        infoPanel.add(createLabel("Giriş (GG.AA.YYYY):"));
        girisTarihi = createDateField();
        infoPanel.add(girisTarihi);

        infoPanel.add(createLabel("Çıkış (GG.AA.YYYY):"));
        cikisTarihi = createDateField();
        infoPanel.add(cikisTarihi);

        infoPanel.add(createLabel("E-Posta Adresi:"));
        emailField = createTextField();
        ((AbstractDocument) emailField.getDocument()).setDocumentFilter(new MaxLenFilter(60));
        infoPanel.add(emailField);

        contentPanel.add(infoPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        JLabel lblGuestHeader = new JLabel("Misafir Bilgileri");
        lblGuestHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGuestHeader.setForeground(PRIMARY_COLOR);
        contentPanel.add(lblGuestHeader);
        contentPanel.add(Box.createVerticalStrut(10));

        kisiContainerPanel = new JPanel();
        kisiContainerPanel.setLayout(new BoxLayout(kisiContainerPanel, BoxLayout.Y_AXIS));
        kisiContainerPanel.setBackground(BG_COLOR);
        contentPanel.add(kisiContainerPanel);

        kisiAlanlariniOlustur();

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // FOOTER
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pricePanel.setBackground(Color.WHITE);
        pricePanel.add(new JLabel("Toplam Tutar: "));
        fiyatLabel = new JLabel("0 TL");
        fiyatLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        fiyatLabel.setForeground(new Color(46, 125, 50));
        pricePanel.add(fiyatLabel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnHesapla = new JButton("Fiyat Hesapla");
        styleButton(btnHesapla, new Color(255, 152, 0));
        btnHesapla.addActionListener(e -> fiyatHesapla());

        JButton btnOlustur = new JButton("Tamamla");
        styleButton(btnOlustur, PRIMARY_COLOR);
        btnOlustur.addActionListener(e -> rezervasyonOlustur());

        btnPanel.add(btnHesapla);
        btnPanel.add(btnOlustur);

        footerPanel.add(pricePanel, BorderLayout.WEST);
        footerPanel.add(btnPanel, BorderLayout.EAST);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        attachPriceAutoUpdate(girisTarihi);
        attachPriceAutoUpdate(cikisTarihi);
    }

    // ✅ REZERVASYON OLUŞTUR VE MAİL GÖNDER
    private void rezervasyonOlustur() {
        LocalDate giris = parseStrictDate(girisTarihi.getText());
        LocalDate cikis = parseStrictDate(cikisTarihi.getText());
        LocalDate bugun = LocalDate.now();

        if (giris == null || cikis == null) {
            JOptionPane.showMessageDialog(this, "Geçerli tarihler giriniz!", "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (giris.isBefore(bugun)) {
            JOptionPane.showMessageDialog(this, "Geçmiş tarihe rezervasyon yapılamaz!", "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!cikis.isAfter(giris)) {
            JOptionPane.showMessageDialog(this, "Çıkış tarihi girişten sonra olmalıdır!", "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = emailField.getText().trim();
        if (!email.contains("@") || email.length() < 5) {
            JOptionPane.showMessageDialog(this, "Geçerli bir e-posta adresi giriniz!", "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String validationError = validateGuestInputs();
        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError, "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String odaTipi = (String) odaSecim.getSelectedItem();
        if (!ReservationManager.odaBosMu(odaTipi)) {
            JOptionPane.showMessageDialog(this, "Bu oda tipinde boş yer kalmadı!", "Doluluk", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int kisiSayisi = (int) kisiSayisiSecim.getSelectedItem();
        fiyatHesapla();
        double fiyat = Double.parseDouble(fiyatLabel.getText().replace(" TL", ""));
        String misafirler = kisileriTopla();

        // Rezervasyon nesnesini oluştur ve kaydet
        Reservation r = new Reservation(kullaniciAdi, email, odaTipi, kisiSayisi, misafirler, girisTarihi.getText(), cikisTarihi.getText(), fiyat);
        ReservationManager.addReservation(r);

        // ✅ ARKA PLANDA MAİL GÖNDERİMİ (Thread kullanarak UI donmasını engelliyoruz)
        new Thread(() -> {
            String subject = "Rezervasyon Talebiniz Alındı - " + r.getOdaNo();
            String message = "Sayın " + kullaniciAdi + ",\n\n" +
                    "Rezervasyon talebiniz başarıyla oluşturulmuştur. Yönetici onayından sonra size tekrar bilgi verilecektir.\n\n" +
                    "--- Rezervasyon Detayları ---\n" +
                    "Oda Numarası: " + r.getOdaNo() + "\n" +
                    "Oda Tipi: " + r.getOdaTipi() + "\n" +
                    "Giriş Tarihi: " + r.getGirisTarihi() + "\n" +
                    "Çıkış Tarihi: " + r.getCikisTarihi() + "\n" +
                    "Toplam Tutar: " + r.getFiyat() + " TL\n\n" +
                    "Bizi tercih ettiğiniz için teşekkür ederiz.\nOtel Yönetimi";

            EmailService.sendMail(email, subject, message);
        }).start();

        JOptionPane.showMessageDialog(this,
                "Rezervasyonunuz oluşturuldu!\nOda No: " + r.getOdaNo() + "\nKonfirme maili adresinize gönderiliyor.",
                "Başarılı", JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }

    // ---------------- UI & MANTIK YARDIMCILARI ----------------

    private void fiyatHesapla() {
        LocalDate giris = parseStrictDate(girisTarihi.getText());
        LocalDate cikis = parseStrictDate(cikisTarihi.getText());
        if (giris == null || cikis == null || !cikis.isAfter(giris)) {
            fiyatLabel.setText("0 TL");
            return;
        }
        long gun = ChronoUnit.DAYS.between(giris, cikis);
        String oda = (String) odaSecim.getSelectedItem();
        int birim = oda.contains("Standart") ? 1000 : oda.contains("Deluxe") ? 2000 : 5000;
        int kisi = (int) kisiSayisiSecim.getSelectedItem();
        fiyatLabel.setText((birim * kisi * gun) + " TL");
    }

    private void kisiAlanlariniOlustur() {
        kisiContainerPanel.removeAll();
        int kisiSayisi = (int) kisiSayisiSecim.getSelectedItem();
        for (int i = 1; i <= kisiSayisi; i++) {
            JPanel card = new JPanel(new GridLayout(3, 2, 10, 10));
            card.setBackground(Color.WHITE);
            TitledBorder b = BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), i + ". Misafir");
            card.setBorder(BorderFactory.createCompoundBorder(b, new EmptyBorder(10, 10, 10, 10)));
            
            card.add(createLabel("Ad Soyad:"));
            JTextField n = createTextField();
            ((AbstractDocument) n.getDocument()).setDocumentFilter(new NameFilter(30));
            card.add(n);

            card.add(createLabel("TC Kimlik:"));
            JTextField t = createTextField();
            ((AbstractDocument) t.getDocument()).setDocumentFilter(new TcKimlikFilter());
            card.add(t);

            card.add(createLabel("Doğum (GG.AA.YYYY):"));
            card.add(createDateField());

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
                JPanel p = (JPanel) c;
                String ad = ((JTextField) p.getComponent(1)).getText();
                String tc = ((JTextField) p.getComponent(3)).getText();
                String d = ((JTextField) p.getComponent(5)).getText();
                sb.append(ad).append(" (TC: ").append(tc).append(", Doğum: ").append(d).append(")\n");
            }
        }
        return sb.toString();
    }

    private String validateGuestInputs() {
        for (Component c : kisiContainerPanel.getComponents()) {
            if (c instanceof JPanel) {
                JPanel p = (JPanel) c;
                if (((JTextField) p.getComponent(1)).getText().length() < 3) return "Tüm misafir isimlerini giriniz.";
                if (((JTextField) p.getComponent(3)).getText().length() != 11) return "TC No 11 hane olmalıdır.";
                if (((JTextField) p.getComponent(5)).getText().length() != 10) return "Doğum tarihlerini tam giriniz.";
            }
        }
        return null;
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(); p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(220, 220, 220), 1, true), new EmptyBorder(15, 15, 15, 15)));
        return p;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(LABEL_FONT); l.setForeground(Color.GRAY); return l;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField(); tf.setFont(INPUT_FONT);
        tf.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.LIGHT_GRAY, 1), new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    private void styleComboBox(JComboBox box) { box.setFont(INPUT_FONT); box.setBackground(Color.WHITE); }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JTextField createDateField() {
        JTextField tf = createTextField();
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DateTypingFilter());
        return tf;
    }

    private LocalDate parseStrictDate(String s) {
        try { return (s.length() == 10) ? LocalDate.parse(s, STRICT_DATE) : null; } catch (Exception e) { return null; }
    }

    private void attachPriceAutoUpdate(JTextField tf) {
        tf.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { fiyatHesapla(); }
            public void removeUpdate(DocumentEvent e) { fiyatHesapla(); }
            public void changedUpdate(DocumentEvent e) { fiyatHesapla(); }
        });
    }

    // ---------------- FİLTRELER ----------------

    private static class NameFilter extends DocumentFilter {
        private int m; public NameFilter(int m) { this.m = m; }
        @Override
        public void replace(FilterBypass fb, int o, int l, String t, AttributeSet a) throws BadLocationException {
            if (t.matches("^[a-zA-ZçğıöşüÇĞİÖŞÜ\\s]*$") && (fb.getDocument().getLength() - l + t.length() <= m)) super.replace(fb, o, l, t, a);
        }
    }

    private static class TcKimlikFilter extends DocumentFilter {
        @Override
        public void replace(FilterBypass fb, int o, int l, String t, AttributeSet a) throws BadLocationException {
            if (t.matches("\\d*") && (fb.getDocument().getLength() - l + t.length() <= 11)) super.replace(fb, o, l, t, a);
        }
    }

    private static class MaxLenFilter extends DocumentFilter {
        private int m; public MaxLenFilter(int m) { this.m = m; }
        @Override
        public void replace(FilterBypass fb, int o, int l, String t, AttributeSet a) throws BadLocationException {
            if (fb.getDocument().getLength() - l + t.length() <= m) super.replace(fb, o, l, t, a);
        }
    }

    private static class DateTypingFilter extends DocumentFilter {
        @Override
        public void replace(FilterBypass fb, int o, int l, String t, AttributeSet a) throws BadLocationException {
            if (t.isEmpty()) { super.replace(fb, o, l, t, a); return; }
            String digits = t.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) return;
            String curr = fb.getDocument().getText(0, fb.getDocument().getLength()).replaceAll("[^0-9]", "");
            String merged = (curr + digits);
            if (merged.length() > 8) merged = merged.substring(0, 8);
            StringBuilder res = new StringBuilder();
            for (int i = 0; i < merged.length(); i++) {
                if (i == 2 || i == 4) res.append('.');
                res.append(merged.charAt(i));
            }
            fb.replace(0, fb.getDocument().getLength(), res.toString(), a);
        }
    }
}