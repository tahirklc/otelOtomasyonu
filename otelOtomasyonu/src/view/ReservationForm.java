package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;

import service.ReservationManager;
import model.Reservation;

public class ReservationForm extends JFrame {

    private JComboBox<String> odaSecim;
    private JComboBox<Integer> kisiSayisiSecim;

    private JTextField girisTarihi;
    private JTextField cikisTarihi;
    private JTextField emailField;

    private JPanel kisiContainerPanel; // Dinamik kişi alanlarının ekleneceği yer
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
        
        // Ana Panel (BorderLayout)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        setContentPane(mainPanel);

        // 1. HEADER (Başlık)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel("Rezervasyon Oluştur");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. FORM İÇERİĞİ (Scrollable)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // -- Oda ve Tarih Bilgileri Paneli --
        JPanel infoPanel = createSectionPanel("Genel Bilgiler");
        infoPanel.setLayout(new GridLayout(5, 2, 10, 15)); // 5 Satır, 2 Sütun

        // Oda Tipi
        infoPanel.add(createLabel("Oda Tipi:"));
        odaSecim = new JComboBox<>(new String[]{
                "Standart Oda - 1000 TL",
                "Deluxe Oda - 2000 TL",
                "Kral Dairesi - 5000 TL"
        });
        styleComboBox(odaSecim);
        odaSecim.addActionListener(e -> fiyatHesapla());
        infoPanel.add(odaSecim);

        // Kişi Sayısı
        infoPanel.add(createLabel("Kişi Sayısı:"));
        kisiSayisiSecim = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        styleComboBox(kisiSayisiSecim);
        kisiSayisiSecim.addActionListener(e -> {
            kisiAlanlariniOlustur(); // Dinamik alanları yenile
            fiyatHesapla();
        });
        infoPanel.add(kisiSayisiSecim);

        // Tarihler
        infoPanel.add(createLabel("Giriş (dd.MM.yyyy):"));
        girisTarihi = createTextField();
        infoPanel.add(girisTarihi);

        infoPanel.add(createLabel("Çıkış (dd.MM.yyyy):"));
        cikisTarihi = createTextField();
        infoPanel.add(cikisTarihi);

        // Email
        infoPanel.add(createLabel("E-Posta Adresi:"));
        emailField = createTextField();
        infoPanel.add(emailField);

        contentPanel.add(infoPanel);
        contentPanel.add(Box.createVerticalStrut(20)); // Boşluk

        // -- Misafir Bilgileri (Dinamik) --
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

        // İlk açılışta alanları oluştur
        kisiAlanlariniOlustur();

        // Content Paneli ScrollPane içine al
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 3. FOOTER (Fiyat ve Butonlar)
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        // Üstüne ince çizgi
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(15, 20, 15, 20)
        ));

        // Fiyat Alanı
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pricePanel.setBackground(Color.WHITE);
        
        JLabel lblTotal = new JLabel("Toplam Tutar: ");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        fiyatLabel = new JLabel("0 TL");
        fiyatLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        fiyatLabel.setForeground(new Color(46, 125, 50)); // Para yeşili

        pricePanel.add(lblTotal);
        pricePanel.add(fiyatLabel);

        // Butonlar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);

        JButton btnHesapla = new JButton("Fiyat Hesapla");
        styleButton(btnHesapla, new Color(255, 152, 0)); // Turuncu
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
    }

    // --- YARDIMCI UI METODLARI ---

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

    // --- MANTIK KISMI (AYNEN KORUNDU) ---

    private LocalDate parseStrictDate(String dateStr) {
        try {
            LocalDate d = LocalDate.parse(dateStr, STRICT_DATE);
            if (d.isBefore(LocalDate.now())) return null;
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    private void kisiAlanlariniOlustur() {
        kisiContainerPanel.removeAll();
        int kisiSayisi = (int) kisiSayisiSecim.getSelectedItem();

        for (int i = 1; i <= kisiSayisi; i++) {
            // Her kişi için bir "Kart" oluşturuyoruz
            JPanel card = new JPanel(new GridLayout(3, 2, 10, 10));
            card.setBackground(Color.WHITE);
            
            // Şık bir çerçeve (TitledBorder)
            TitledBorder border = BorderFactory.createTitledBorder(
                    new LineBorder(new Color(200, 200, 200), 1, true),
                    i + ". Misafir Bilgileri"
            );
            border.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
            border.setTitleColor(PRIMARY_COLOR);
            card.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(10, 10, 10, 10)));
            card.setMaximumSize(new Dimension(2000, 160)); // Yüksekliği sınırla

            // Inputlar
            card.add(createLabel("Ad Soyad:"));
            card.add(createTextField());

            card.add(createLabel("TC Kimlik:"));
            card.add(createTextField());

            card.add(createLabel("Doğum Tarihi (dd.MM.yyyy):"));
            card.add(createTextField());

            kisiContainerPanel.add(card);
            kisiContainerPanel.add(Box.createVerticalStrut(10)); // Kartlar arası boşluk
        }

        kisiContainerPanel.revalidate();
        kisiContainerPanel.repaint();
    }

    private String kisileriTopla() {
        StringBuilder sb = new StringBuilder();

        // kisiContainerPanel içindeki bileşenler (JPanel kartları) arasında dönüyoruz
        for (Component c : kisiContainerPanel.getComponents()) {
            if (c instanceof JPanel) { // Box.Glue olmayanlar
                JPanel card = (JPanel) c;
                
                // Kart içindeki bileşen sırası: Label, Field, Label, Field...
                // index 1 -> Ad, index 3 -> TC, index 5 -> Doğum
                
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

    private void rezervasyonOlustur() {
        LocalDate giris = parseStrictDate(girisTarihi.getText());
        LocalDate cikis = parseStrictDate(cikisTarihi.getText());

        if (giris == null || cikis == null || cikis.isBefore(giris)) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli giriş ve çıkış tarihleri giriniz!", "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = emailField.getText().trim();
        if (!email.contains("@")) {
            JOptionPane.showMessageDialog(this, "Geçerli bir e-posta adresi giriniz!", "Hata", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String oda = (String) odaSecim.getSelectedItem();

        if (!ReservationManager.odaBosMu(oda)) {
            JOptionPane.showMessageDialog(this, "Seçilen oda türünde şu an boş oda bulunmamaktadır.", "Doluluk Uyarısı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int kisiSayisi = (int) kisiSayisiSecim.getSelectedItem();
        // Fiyat hesaplanmadıysa otomatik hesapla
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
}