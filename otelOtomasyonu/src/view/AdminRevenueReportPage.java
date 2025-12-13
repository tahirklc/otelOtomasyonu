package view;

import service.ReservationManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

public class AdminRevenueReportPage extends JFrame {

    private JComboBox<Integer> cmbYear;
    private JLabel lblTotalAmount; // Büyük toplam yazısı
    private DefaultTableModel model;
    private JTable table;

    // --- RENK PALETİ ---
    private final Color PRIMARY_COLOR = new Color(63, 81, 181); // İndigo
    private final Color BG_COLOR = new Color(245, 247, 250);    // Açık Gri
    private final Color MONEY_COLOR = new Color(46, 125, 50);   // Koyu Yeşil (Para)
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public AdminRevenueReportPage() {

        setTitle("Yönetici - Finansal Raporlar");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Ana Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        setContentPane(mainPanel);

        // 1. ÜST KISIM (Header + Kontroller + Toplam Kartı)
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(BG_COLOR);
        topContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        // -- 1.1 Başlık ve Kontroller Satırı --
        JPanel controlRow = new JPanel(new BorderLayout());
        controlRow.setBackground(BG_COLOR);
        controlRow.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel lblTitle = new JLabel("Hasılat Raporu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(50, 50, 50));
        
        // Sağ Taraf: Yıl Seçimi + PDF Butonu
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        actionPanel.add(new JLabel("Rapor Yılı:"));
        
        cmbYear = new JComboBox<>();
        styleComboBox(cmbYear);
        
        // Yılları Yükle
        Set<Integer> years = ReservationManager.getReservationYears();
        if (years.isEmpty()) cmbYear.addItem(2025);
        else for (Integer y : years) cmbYear.addItem(y);

        cmbYear.addActionListener(e -> guncelleTablo());
        actionPanel.add(cmbYear);

        // PDF Butonu
        JButton btnPdf = new JButton("PDF İndir");
        styleButton(btnPdf, new Color(220, 53, 69)); // Kırmızı tonu (PDF rengi)
        btnPdf.addActionListener(e -> {
            if (cmbYear.getSelectedItem() != null) {
                int year = (int) cmbYear.getSelectedItem();
                ReservationManager.exportRevenueToPDF(year);
                JOptionPane.showMessageDialog(this, "PDF Raporu başarıyla oluşturuldu!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        actionPanel.add(btnPdf);

        controlRow.add(lblTitle, BorderLayout.WEST);
        controlRow.add(actionPanel, BorderLayout.EAST);
        topContainer.add(controlRow);

        // -- 1.2 Toplam Tutar Kartı (Big Info Card) --
        JPanel infoCard = new JPanel(new BorderLayout());
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        infoCard.setMaximumSize(new Dimension(2000, 100)); // Genişlemesine izin ver

        JLabel lblInfoTitle = new JLabel("SEÇİLEN YIL TOPLAM HASILAT");
        lblInfoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblInfoTitle.setForeground(Color.GRAY);
        
        lblTotalAmount = new JLabel("0.0 TL");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTotalAmount.setForeground(MONEY_COLOR); // Yeşil renk

        infoCard.add(lblInfoTitle, BorderLayout.NORTH);
        infoCard.add(lblTotalAmount, BorderLayout.CENTER);
        
        topContainer.add(infoCard);
        
        mainPanel.add(topContainer, BorderLayout.NORTH);

        // 2. TABLO ALANI
        setupTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 30, 30, 30)); // Yanlardan boşluk
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // İlk yükleme
        guncelleTablo();
    }

    private void setupTable() {
        String[] cols = {"Ay", "Hasılat (TL)"};
        model = new DefaultTableModel(cols, 0);

        table = new JTable(model) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        // Tablo Görsel Ayarları
        table.setFont(MAIN_FONT);
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);

        // Header Tasarımı
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));

        // Hücre Hizalamaları
        // 1. Sütun (Ay) -> Ortala
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // 2. Sütun (Para) -> Sağa Yasla ve Renklendir
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setForeground(MONEY_COLOR); // Tablo içindeki paralar da yeşil olsun
                setBorder(new EmptyBorder(0,0,0,20)); // Sağdan biraz boşluk bırak
                return c;
            }
        });
    }

    // --- GÖRSEL YARDIMCILAR ---
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleComboBox(JComboBox box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(100, 30));
    }

    // --- MANTIK METODU (AYNEN KORUNDU) ---
    private void guncelleTablo() {
        if (cmbYear.getItemCount() == 0) return;

        int year = (int) cmbYear.getSelectedItem();

        double[] monthly = ReservationManager.getMonthlyRevenue(year);
        double yearlyTotal = ReservationManager.getYearlyRevenue(year);

        // Karttaki büyük yazıyı güncelle
        lblTotalAmount.setText(String.format("%,.2f TL", yearlyTotal)); // Para formatı (örn: 12.500,00 TL)

        String[] ayIsimleri = {
                "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
                "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        };

        model.setRowCount(0);
        for (int i = 0; i < 12; i++) {
            model.addRow(new Object[]{
                    ayIsimleri[i],
                    String.format("%,.2f", monthly[i]) // Tabloda da formatlı göster
            });
        }
    }
}