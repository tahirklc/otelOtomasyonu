package view;

import service.ReservationManager;
import model.Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class AdminCustomerSearchPage extends JFrame {

    private JTextField txtName;
    private DefaultTableModel model;
    private JTable table;

    private final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public AdminCustomerSearchPage() {

        setTitle("Yönetici - Müşteri Arama");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        setContentPane(mainPanel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_COLOR);
        topPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("Müşteri Kayıtları");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(50, 50, 50));

        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchContainer.setOpaque(false);

        JLabel lblSearch = new JLabel("Müşteri Adı:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSearch.setForeground(new Color(80, 80, 80));

        txtName = new JTextField(20);
        styleTextField(txtName);
        txtName.setToolTipText("En fazla 30 karakter. Sadece harf ve boşluk.");

        // ✅ 30 karakter + sadece harf/boşluk (tam metin kontrolü)
        ((AbstractDocument) txtName.getDocument()).setDocumentFilter(new NameStrictFilter(30));

        JButton btnSearch = new JButton("Ara");
        styleButton(btnSearch);
        btnSearch.addActionListener(e -> ara());

        searchContainer.add(lblSearch);
        searchContainer.add(txtName);
        searchContainer.add(btnSearch);

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(searchContainer, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        setupTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 30, 30, 30));
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        getRootPane().setDefaultButton(btnSearch);
    }

    private void setupTable() {
        String[] columns = {
                "Müşteri", "Oda Tipi", "Oda No",
                "Kişi Sayısı", "Giriş", "Çıkış", "Fiyat", "Durum"
        };
        model = new DefaultTableModel(columns, 0);

        table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setFont(MAIN_FONT);
        table.setRowHeight(35);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));

                String status = String.valueOf(value);
                if ("Onaylandı".equalsIgnoreCase(status))
                    setForeground(new Color(46, 125, 50));
                else if ("Bekliyor".equalsIgnoreCase(status))
                    setForeground(new Color(255, 143, 0));
                else if ("İptal Edildi".equalsIgnoreCase(status))
                    setForeground(Color.RED);
                else
                    setForeground(Color.GRAY);

                return c;
            }
        });
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(200, 35));
        field.setFont(MAIN_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleButton(JButton btn) {
        btn.setPreferredSize(new Dimension(100, 35));
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(PRIMARY_COLOR.brighter()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(PRIMARY_COLOR); }
        });
    }

    // ✅ SADECE HARF/BOŞLUK + 30 KARAKTER: Son kontrol ve tek popup burada
    private void ara() {
        String nameQuery = txtName.getText().trim();

        if (nameQuery.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen müşteri adı giriniz!",
                    "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tam metin doğrulama (filtreyi by-pass eden durumlara karşı)
        if (!nameQuery.matches("[\\p{L} ]{1,30}")) {
            JOptionPane.showMessageDialog(this,
                    "Sadece harf ve boşluk girebilirsin (maksimum 30 karakter).",
                    "Hatalı Giriş",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        model.setRowCount(0);

        List<Reservation> list = ReservationManager.searchByName(nameQuery.toLowerCase());

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Bu isme ait müşteri bulunamadı.",
                    "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (Reservation r : list) {
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

    // 🔒 Tam metin kontrolü yapan filtre: hem uzunluk hem karakter seti
    private static class NameStrictFilter extends DocumentFilter {
        private final int max;

        NameStrictFilter(int max) { this.max = max; }

        private boolean isValidFullText(String fullText) {
            if (fullText == null) return false;
            if (fullText.length() > max) return false;
            // İzin verilen: harf + boşluk. (Türkçe dahil)
            return fullText.matches("[\\p{L} ]*");
        }

        private void reject() {
            Toolkit.getDefaultToolkit().beep();
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
            if (text == null) return;

            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String candidate = new StringBuilder(current).insert(offset, text).toString();

            if (!isValidFullText(candidate)) {
                reject();
                return;
            }
            super.insertString(fb, offset, text, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) text = "";

            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            StringBuilder sb = new StringBuilder(current);
            sb.replace(offset, offset + length, text);
            String candidate = sb.toString();

            if (!isValidFullText(candidate)) {
                reject();
                return;
            }
            super.replace(fb, offset, length, text, attrs);
        }
    }
}
