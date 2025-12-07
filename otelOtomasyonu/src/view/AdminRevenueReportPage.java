package view;

import service.ReservationManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Set;

public class AdminRevenueReportPage extends JFrame {

    private JComboBox<Integer> cmbYear;
    private JLabel lblYearTotal;
    private DefaultTableModel model;

    public AdminRevenueReportPage() {

        setTitle("Otel Hasılat Raporu (Aylık & Yıllık)");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ------- ÜST PANEL (YIL SEÇİMİ) -------
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        topPanel.add(new JLabel("Yıl:"));

        Set<Integer> years = ReservationManager.getReservationYears();
        cmbYear = new JComboBox<>();

        if (years.isEmpty()) {
            cmbYear.addItem(2025); // boşsa bile bir şey görünsün
        } else {
            for (Integer y : years) cmbYear.addItem(y);
        }

        cmbYear.addActionListener(e -> guncelleTablo());
        topPanel.add(cmbYear);

        lblYearTotal = new JLabel("Yıllık Toplam: 0 TL");
        lblYearTotal.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(lblYearTotal);

        add(topPanel, BorderLayout.NORTH);

        // ------- AYLIK TABLO -------
        String[] cols = {"Ay", "Hasılat (TL)"};
        model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        add(new JScrollPane(table), BorderLayout.CENTER);

        // İlk yükleme
        guncelleTablo();
    }

    private void guncelleTablo() {
        if (cmbYear.getItemCount() == 0) return;

        int year = (int) cmbYear.getSelectedItem();

        double[] monthly = ReservationManager.getMonthlyRevenue(year);
        double yearlyTotal = ReservationManager.getYearlyRevenue(year);

        lblYearTotal.setText("Yıllık Toplam (" + year + "): " + yearlyTotal + " TL");

        String[] ayIsimleri = {
                "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
                "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        };

        model.setRowCount(0);
        for (int i = 0; i < 12; i++) {
            model.addRow(new Object[]{
                    ayIsimleri[i],
                    monthly[i]
            });
        }
    }
}
