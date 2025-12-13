package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

import service.ReservationManager;
import model.Reservation;

public class OldReservationsPage extends JFrame {

    // Renk Paleti
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(63, 81, 181); // İndigo
    private final Color TEXT_RED = new Color(198, 40, 40);  // Kırmızı (Reddedildi için)

    public OldReservationsPage(String kullaniciAdi) {

        setTitle("Geçmiş İşlemler");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Ana Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        // 1. Üst Başlık
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel lblTitle = new JLabel("İşlem Geçmişi (Reddedilenler)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(50, 50, 50));

        JLabel lblSub = new JLabel("Aşağıda tarafınızca iptal edilen veya otel tarafından reddedilen kayıtlar listelenmektedir.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 2. Tablo Verileri
        // "Oda No" sütununu da ekledim ki müşteri hangi odaydı görebilsin.
        String[] columns = {"Oda Tipi", "Oda No", "Kişi", "Giriş", "Çıkış", "Fiyat", "Durum"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // Verileri Çek (Mantık Korundu: Sadece 'Reddedildi' olanlar)
        boolean kayitVar = false;
        for (Reservation r : ReservationManager.getReservations()) {
            if (r.getMusteriAdi().equals(kullaniciAdi)) {
                // Kod mantığı gereği sadece "Reddedildi" filtrelemesi yapılıyor
                if (r.getDurum().equals("Reddedildi")) {
                    model.addRow(new Object[]{
                            r.getOdaTipi(),
                            r.getOdaNo(),
                            r.getKisiSayisi(),
                            r.getGirisTarihi(),
                            r.getCikisTarihi(),
                            r.getFiyat(),
                            r.getDurum()
                    });
                    kayitVar = true;
                }
            }
        }

        // Eğer hiç kayıt yoksa kullanıcıyı bilgilendir
        if (!kayitVar) {
            model.addRow(new Object[]{"-", "-", "-", "-", "-", "-", "Kayıt Yok"});
        }

        // 3. Tablo Tasarımı
        JTable table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(255, 235, 238)); // Açık kırmızı seçim rengi
        table.setSelectionForeground(Color.BLACK);

        // Header Tasarımı
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));

        // Hücreleri Ortala
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // DURUM Sütunu (En sağdaki) -> Kırmızı Yap
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String status = (String) value;
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));

                if ("Reddedildi".equals(status)) {
                    setForeground(TEXT_RED);
                } else if ("Kayıt Yok".equals(status)) {
                    setForeground(Color.GRAY);
                    setFont(new Font("Segoe UI", Font.ITALIC, 13));
                } else {
                    setForeground(Color.BLACK);
                }
                return c;
            }
        });

        // ScrollPane (Kenarlıksız)
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        mainPanel.add(scroll, BorderLayout.CENTER);
    }
}