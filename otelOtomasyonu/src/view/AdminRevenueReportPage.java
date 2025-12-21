package view;

import service.ReservationManager;
import service.EmailService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.Set;

public class AdminRevenueReportPage extends JFrame {

    private JComboBox<Integer> cmbYear;
    private JLabel lblTotalAmount;
    private DefaultTableModel model;
    private JTable table;

    // --- AYARLAR ---
    private final String ADMIN_EMAIL = "tahirxxx63@gmail.com"; 
    private final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color MONEY_COLOR = new Color(46, 125, 50);
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public AdminRevenueReportPage() {
        setTitle("Yönetici - Finansal Raporlar");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        setContentPane(mainPanel);

        // 1. ÜST KISIM (BAŞLIK VE KONTROLLER)
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(BG_COLOR);
        topContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel controlRow = new JPanel(new BorderLayout());
        controlRow.setBackground(BG_COLOR);
        controlRow.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel lblTitle = new JLabel("Hasılat Raporu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(new JLabel("Rapor Yılı:"));

        cmbYear = new JComboBox<>();
        styleComboBox(cmbYear);
        Set<Integer> years = ReservationManager.getReservationYears();
        if (years.isEmpty()) cmbYear.addItem(2025);
        else for (Integer y : years) cmbYear.addItem(y);
        cmbYear.addActionListener(e -> guncelleTablo());
        actionPanel.add(cmbYear);

        // --- PDF OLUŞTUR VE MAİL GÖNDER BUTONU ---
        JButton btnPdf = new JButton("PDF Gönder (Mail)");
        styleButton(btnPdf, new Color(220, 53, 69));
        btnPdf.addActionListener(e -> {
            if (cmbYear.getSelectedItem() != null) {
                int year = (int) cmbYear.getSelectedItem();
                // Dosya adının Manager ile uyumlu olduğundan emin oluyoruz
                String fileName = "hasilat_2025" + ".pdf";

                // 1. PDF'i oluştur (Bu işlem genellikle hızlıdır ve UI thread'de kalabilir)
                ReservationManager.exportRevenueToPDF(year);

                // 2. Arka planda mail gönderme işlemini başlat
                new Thread(() -> {
                    // Dosyanın gerçekten oluşup oluşmadığını kontrol et
                    File file = new File(fileName);
                    if (file.exists()) {
                        String subject = year + " Yılı Finansal Hasılat Raporu";
                        String message = "Sayın Yönetici,\n\n" + year + " yılına ait otel hasılat raporu oluşturulmuş ve ekte sunulmuştur.\n\nİyi çalışmalar dileriz.";

                        // EmailService içindeki ekli dosya gönderim metodunu çağırıyoruz
                        EmailService.sendMailWithAttachment(ADMIN_EMAIL, subject, message, fileName);

                        // Kullanıcıya bilgi ver
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, 
                                "Rapor başarıyla oluşturuldu ve " + ADMIN_EMAIL + " adresine gönderildi.", 
                                "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, 
                                "Hata: PDF dosyası oluşturulamadı, mail gönderilemiyor.", 
                                "Dosya Hatası", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();
            }
        });
        actionPanel.add(btnPdf);

        controlRow.add(lblTitle, BorderLayout.WEST);
        controlRow.add(actionPanel, BorderLayout.EAST);
        topContainer.add(controlRow);

        // TOPLAM HASILAT KARTI
        JPanel infoCard = new JPanel(new BorderLayout());
        infoCard.setBackground(Color.WHITE);
        infoCard.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(220, 220, 220), 1, true), new EmptyBorder(20, 20, 20, 20)));
        infoCard.setMaximumSize(new Dimension(2000, 100));

        JLabel lblInfoTitle = new JLabel("SEÇİLEN YIL TOPLAM HASILAT");
        lblInfoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblInfoTitle.setForeground(Color.GRAY);
        lblTotalAmount = new JLabel("0.0 TL");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTotalAmount.setForeground(MONEY_COLOR);

        infoCard.add(lblInfoTitle, BorderLayout.NORTH);
        infoCard.add(lblTotalAmount, BorderLayout.CENTER);
        topContainer.add(infoCard);
        mainPanel.add(topContainer, BorderLayout.NORTH);

        // 2. TABLO ALANI
        setupTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 30, 30, 30));
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        guncelleTablo();
    }

    // --- TABLO VE GÖRSEL AYARLAR ---
    private void setupTable() {
        String[] cols = {"Ay", "Hasılat (TL)"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model) { public boolean isCellEditable(int r, int c) { return false; } };
        table.setFont(MAIN_FONT);
        table.setRowHeight(35);
        table.setSelectionBackground(new Color(232, 240, 254));
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.RIGHT);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setForeground(MONEY_COLOR);
                setBorder(new EmptyBorder(0,0,0,20));
                return c;
            }
        });
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleComboBox(JComboBox box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(100, 30));
    }

    private void guncelleTablo() {
        if (cmbYear.getItemCount() == 0) return;
        int year = (int) cmbYear.getSelectedItem();
        double[] monthly = ReservationManager.getMonthlyRevenue(year);
        double yearlyTotal = ReservationManager.getYearlyRevenue(year);

        lblTotalAmount.setText(String.format("%,.2f TL", yearlyTotal));

        String[] ayIsimleri = {"Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};
        model.setRowCount(0);
        for (int i = 0; i < 12; i++) {
            model.addRow(new Object[]{ayIsimleri[i], String.format("%,.2f", monthly[i])});
        }
    }
}