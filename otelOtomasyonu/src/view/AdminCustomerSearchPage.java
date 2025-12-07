package view;

import service.ReservationManager;
import model.Reservation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminCustomerSearchPage extends JFrame {

    private JTextField txtName;
    private JTextField txtTc;
    private DefaultTableModel model;

    public AdminCustomerSearchPage() {

        setTitle("Müşteri Arama");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ------- ÜST ARAMA PANELİ -------
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        searchPanel.add(new JLabel("İsim:"));
        txtName = new JTextField(15);
        searchPanel.add(txtName);

        searchPanel.add(new JLabel("TC:"));
        txtTc = new JTextField(15);
        searchPanel.add(txtTc);

        JButton btnSearch = new JButton("Ara");
        btnSearch.addActionListener(e -> ara());
        searchPanel.add(btnSearch);

        add(searchPanel, BorderLayout.NORTH);

        // ------- TABLO -------
        String[] columns = {
                "Müşteri", "TC (ilk kişi)", "Oda Tipi", "Oda No",
                "Kişi Sayısı", "Giriş", "Çıkış", "Fiyat", "Durum"
        };
        model = new DefaultTableModel(columns, 0);

        JTable table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void ara() {
        String nameQuery = txtName.getText().trim();
        String tcQuery   = txtTc.getText().trim();

        model.setRowCount(0); // tabloyu temizle

        // Hem isim hem TC girildiyse: intersection yap
        List<Reservation> list;

        if (!tcQuery.isEmpty()) {
            list = ReservationManager.searchByTc(tcQuery);
            // İsim de doluysa filtreyi daralt
            if (!nameQuery.isEmpty()) {
                list.removeIf(r ->
                        r.getMusteriAdi() == null ||
                        !r.getMusteriAdi().toLowerCase().contains(nameQuery.toLowerCase())
                );
            }
        } else if (!nameQuery.isEmpty()) {
            list = ReservationManager.searchByName(nameQuery);
        } else {
            JOptionPane.showMessageDialog(this,
                    "En azından isim veya TC alanından birini doldurun.",
                    "Uyarı",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Bu kriterlere uygun müşteri bulunamadı.",
                    "Bilgi",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (Reservation r : list) {
            String firstTc = extractFirstTc(r.getKisiler());

            model.addRow(new Object[]{
                    r.getMusteriAdi(),
                    firstTc,
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

    // kisiler stringinden ilk "TC: xxx" ifadesini çeker
    private String extractFirstTc(String kisiler) {
        if (kisiler == null) return "";
        String[] lines = kisiler.split("\n");
        for (String line : lines) {
            int index = line.indexOf("TC:");
            if (index != -1) {
                return line.substring(index + 3).trim();
            }
        }
        return "";
    }
}
