package view;

import service.ReservationManager;
import model.Reservation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class AdminCustomerSearchPage extends JFrame {

    private JTextField txtName;
    private DefaultTableModel model;
    private JTable table;

    // Renk Paleti
    private final Color PRIMARY_COLOR = new Color(63, 81, 181); // İndigo
    private final Color BG_COLOR = new Color(245, 247, 250);    // Açık Gri Arka Plan
    private final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public AdminCustomerSearchPage() {

        setTitle("Yönetici - Müşteri Arama");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Ana Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);
        setContentPane(mainPanel);

        // ------- 1. ÜST ARAMA PANELİ (HEADER) -------
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_COLOR);
        topPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Başlık Kısmı
        JLabel lblTitle = new JLabel("Müşteri Kayıtları");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(50, 50, 50));
        
        // Arama Kutusu Paneli
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchContainer.setOpaque(false);

        JLabel lblSearch = new JLabel("Müşteri Adı:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSearch.setForeground(new Color(80, 80, 80));

        txtName = new JTextField(20);
        styleTextField(txtName);

        JButton btnSearch = new JButton("Ara");
        styleButton(btnSearch);
        btnSearch.addActionListener(e -> ara());

        searchContainer.add(lblSearch);
        searchContainer.add(txtName);
        searchContainer.add(btnSearch);

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(searchContainer, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ------- 2. TABLO -------
        setupTable();
        
        // Tabloyu ScrollPane içine al (Kenarlıksız)
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(0, 30, 30, 30)); // Yanlardan boşluk
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Enter tuşuna basınca arama yapsın
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

        // Hücreleri Ortala
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // "Durum" sütunu için özel renklendirme (Son sütun)
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) value;
                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                if ("Onaylandı".equalsIgnoreCase(status)) setForeground(new Color(46, 125, 50));
                else if ("Bekliyor".equalsIgnoreCase(status)) setForeground(new Color(255, 143, 0));
                else if ("İptal Edildi".equalsIgnoreCase(status)) setForeground(Color.RED);
                else setForeground(Color.GRAY);
                
                return c;
            }
        });
    }

    // ------- YARDIMCI METODLAR (STİL) -------

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
        
        // Hover Efekti
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(PRIMARY_COLOR.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(PRIMARY_COLOR); }
        });
    }

    // ------- MANTIK (DEĞİŞMEDİ) -------
    // ✅ SADECE İSİME GÖRE ARAMA
    private void ara() {
        String nameQuery = txtName.getText().trim().toLowerCase();

        model.setRowCount(0); // tabloyu temizle

        if (nameQuery.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen müşteri adı giriniz!",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Reservation> list = ReservationManager.searchByName(nameQuery);

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Bu isme ait müşteri bulunamadı.",
                    "Bilgi",
                    JOptionPane.INFORMATION_MESSAGE);
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
}