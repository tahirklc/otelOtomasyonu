package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import service.ReservationManager;
import model.Reservation;

public class ActiveReservationsPage extends JFrame {

    private String kullaniciAdi;
    private JTable table;
    private DefaultTableModel model;

    // Tasarım Sabitleri
    private final Color PRIMARY_COLOR = new Color(63, 81, 181); // İndigo
    private final Color ACCENT_COLOR = new Color(255, 64, 129); // Pembe
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public ActiveReservationsPage(String kullaniciAdi) {
        this.kullaniciAdi = kullaniciAdi;

        setTitle("Aktif Rezervasyonlarım");
        setSize(950, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Ana Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250)); // Çok açık gri arka plan
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        // 1. Üst Başlık (Header)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel lblTitle = new JLabel("Rezervasyon Listesi");
        lblTitle.setFont(HEADER_FONT);
        lblTitle.setForeground(new Color(50, 50, 50));
        
        JLabel lblSub = new JLabel("İptal etmek istediğiniz rezervasyonun üzerine tıklayın.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Tablo Kurulumu
        setupTable();

        // 3. Tabloyu ScrollPane içine koy (Modern Görünüm)
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Çerçeve çizgisini kaldır
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void setupTable() {
        // ✅ ODA NO SÜTUNU VAR
        String[] columns = {"Oda Tipi", "Oda No", "Kişi Sayısı", "Giriş", "Çıkış", "Fiyat", "Durum"};
        model = new DefaultTableModel(columns, 0);

        // Verileri Yükle (Mantık Korundu)
        for (Reservation r : ReservationManager.getReservations()) {
            if (r.getMusteriAdi().equals(kullaniciAdi)) {
                if (r.getDurum().equals("Bekliyor") || r.getDurum().equals("Onaylandı")) {
                    model.addRow(new Object[]{
                            r.getOdaTipi(),
                            r.getOdaNo(),       // ✅ ODA NUMARASI
                            r.getKisiSayisi(),
                            r.getGirisTarihi(),
                            r.getCikisTarihi(),
                            r.getFiyat(),
                            r.getDurum()
                    });
                }
            }
        }

        // Tabloyu Oluştur
        table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // --- GÖRSEL İYİLEŞTİRMELER ---
        table.setFont(TABLE_FONT);
        table.setRowHeight(40); // Satırları genişlet
        table.setShowVerticalLines(false); // Dikey çizgileri kaldır
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(232, 240, 254)); // Seçim rengi (açık mavi)
        table.setSelectionForeground(Color.BLACK);

        // Başlık (Header) Tasarımı
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(PRIMARY_COLOR); // İndigo başlık
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));

        // Sütun Hizalamaları
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        // Tüm sütunları ortala
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // "Durum" Sütunu için Özel Renklendirme (En sağdaki sütun)
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusColumnRenderer());

        // --- TIKLAMA VE İPTAL MANTIĞI (MANTIK KORUNDU) ---
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = table.getSelectedRow();
                if (row == -1) return;

                // Verileri al
                String odaTipi = model.getValueAt(row, 0).toString();
                int odaNo      = Integer.parseInt(model.getValueAt(row, 1).toString());
                String kisiSay = model.getValueAt(row, 2).toString();
                String giris   = model.getValueAt(row, 3).toString();
                String cikis   = model.getValueAt(row, 4).toString();
                String fiyat   = model.getValueAt(row, 5).toString();
                String durum   = model.getValueAt(row, 6).toString();

                String[] options = {"İptal Et", "Vazgeç"};
                
                // Modern bir Dialog gösterimi için JLabel kullanalım
                JLabel msgLabel = new JLabel("<html><body style='width: 250px'>" +
                        "<b>Oda Tipi:</b> " + odaTipi + "<br>" +
                        "<b>Oda No:</b> " + odaNo + "<br>" +
                        "<b>Tarih:</b> " + giris + " - " + cikis + "<br><br>" +
                        "Bu rezervasyonu iptal etmek istediğinize emin misiniz?</body></html>");
                msgLabel.setFont(TABLE_FONT);

                int secim = JOptionPane.showOptionDialog(
                        ActiveReservationsPage.this,
                        msgLabel,
                        "Rezervasyon İşlemi",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, // İkon
                        options,
                        options[1]
                );

                // İPTAL İŞLEMİ (MANTIK AYNEN KORUNDU)
                if (secim == 0) {
                    boolean silindi = false;
                    for (Reservation rr : ReservationManager.getReservations()) {
                        if (rr.getMusteriAdi().equals(kullaniciAdi)
                                && rr.getOdaTipi().equals(odaTipi)
                                && rr.getOdaNo() == odaNo
                                && rr.getGirisTarihi().equals(giris)
                                && rr.getCikisTarihi().equals(cikis)) {

                            rr.setDurum("İptal Edildi");
                            // ODA NUMARASI GERİ VERİLİR
                            ReservationManager.odaIade(rr.getOdaTipi(), rr.getOdaNo());
                            silindi = true;
                            break;
                        }
                    }

                    if (silindi) {
                        ReservationManager.saveToFile();
                        JOptionPane.showMessageDialog(ActiveReservationsPage.this, "Rezervasyon başarıyla iptal edildi!");
                        
                        // Sayfayı kapatıp yeniden açarak yenile
                        dispose();
                        new ActiveReservationsPage(kullaniciAdi).setVisible(true);
                    }
                }
            }
        });
    }

    // --- ÖZEL HÜCRE BOYAYICI (RENKLİ DURUM KUTUCUKLARI) ---
    class StatusColumnRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            String status = (String) value;
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));

            // Duruma göre renk değiştir
            if ("Onaylandı".equals(status)) {
                setForeground(new Color(46, 125, 50)); // Koyu Yeşil
            } else if ("Bekliyor".equals(status)) {
                setForeground(new Color(255, 143, 0)); // Turuncu
            } else {
                setForeground(Color.GRAY);
            }
            return c;
        }
    }
}