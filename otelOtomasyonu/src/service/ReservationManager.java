package service;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import javax.swing.JOptionPane;

import model.Reservation;

// ✅ PDF İÇİN iText
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ReservationManager {

    private static ArrayList<Reservation> rezervasyonlar = new ArrayList<>();

    // ✅ ODA NUMARA HAVUZLARI
    private static TreeSet<Integer> standartOdalar = new TreeSet<>();
    private static TreeSet<Integer> deluxeOdalar   = new TreeSet<>();
    private static TreeSet<Integer> kralOdalar     = new TreeSet<>();

    private static final String FILE_PATH = "reservations.txt";

    static {
        // Başlangıç oda aralıkları
        for (int i = 101; i <= 103; i++) standartOdalar.add(i);
        for (int i = 201; i <= 203; i++) deluxeOdalar.add(i);
        for (int i = 301; i <= 303; i++) kralOdalar.add(i);

        loadFromFile();
    }

    // ===================== ✅ REZERVASYON =====================

    public static void addReservation(Reservation r) {
        int odaNo = odaAyirVeNumaraVer(r.getOdaTipi());
        r.setOdaNo(odaNo);
        rezervasyonlar.add(r);
        saveToFile();
    }

    public static ArrayList<Reservation> getReservations() {
        return rezervasyonlar;
    }

    // ===================== ✅ ODA =====================

    public static boolean odaBosMu(String odaTipi) {
        if (odaTipi.contains("Standart")) return !standartOdalar.isEmpty();
        if (odaTipi.contains("Deluxe"))   return !deluxeOdalar.isEmpty();
        if (odaTipi.contains("Kral"))     return !kralOdalar.isEmpty();
        return false;
    }

    private static int odaAyirVeNumaraVer(String odaTipi) {
        if (odaTipi.contains("Standart") && !standartOdalar.isEmpty())
            return standartOdalar.pollFirst();
        if (odaTipi.contains("Deluxe") && !deluxeOdalar.isEmpty())
            return deluxeOdalar.pollFirst();
        if (odaTipi.contains("Kral") && !kralOdalar.isEmpty())
            return kralOdalar.pollFirst();
        return -1;
    }

    public static void odaIade(String odaTipi, int odaNo) {
        if (odaTipi.contains("Standart")) standartOdalar.add(odaNo);
        if (odaTipi.contains("Deluxe"))   deluxeOdalar.add(odaNo);
        if (odaTipi.contains("Kral"))     kralOdalar.add(odaNo);
        saveToFile();
    }

    public static int getStandartKalan() { return standartOdalar.size(); }
    public static int getDeluxeKalan()   { return deluxeOdalar.size(); }
    public static int getKralKalan()     { return kralOdalar.size(); }

    // ===================== ✅ YENİ ODA EKLEME (BUTONLAR İÇİN) =====================

    public static void yeniStandartOdaEkle() {
        int yeniNo = standartOdalar.isEmpty() ? 101 : standartOdalar.last() + 1;
        standartOdalar.add(yeniNo);
        saveToFile();
    }

    public static void yeniDeluxeOdaEkle() {
        int yeniNo = deluxeOdalar.isEmpty() ? 201 : deluxeOdalar.last() + 1;
        deluxeOdalar.add(yeniNo);
        saveToFile();
    }

    public static void yeniKralOdaEkle() {
        int yeniNo = kralOdalar.isEmpty() ? 301 : kralOdalar.last() + 1;
        kralOdalar.add(yeniNo);
        saveToFile();
    }

    // ===================== ✅ MÜŞTERİ ARAMA =====================

    public static List<Reservation> searchByName(String nameQuery) {
        List<Reservation> result = new ArrayList<>();
        if (nameQuery == null || nameQuery.isEmpty()) return result;

        String q = nameQuery.toLowerCase();

        for (Reservation r : rezervasyonlar) {
            if (r.getMusteriAdi() != null &&
                r.getMusteriAdi().toLowerCase().contains(q)) {
                result.add(r);
            }
        }
        return result;
    }

    // ===================== ✅ TARİH =====================

    private static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return null;
        }
    }

    // ===================== ✅ GELİR / HASILAT =====================

    public static Set<Integer> getReservationYears() {
        Set<Integer> years = new TreeSet<>();
        for (Reservation r : rezervasyonlar) {
            LocalDate d = parseDate(r.getGirisTarihi());
            if (d != null) years.add(d.getYear());
        }
        return years;
    }

    public static double getYearlyRevenue(int year) {
        double total = 0;
        for (Reservation r : rezervasyonlar) {
            if (!"Onaylandı".equals(r.getDurum())) continue;
            LocalDate d = parseDate(r.getGirisTarihi());
            if (d != null && d.getYear() == year) {
                total += r.getFiyat();
            }
        }
        return total;
    }

    public static double[] getMonthlyRevenue(int year) {
        double[] monthly = new double[12];
        for (Reservation r : rezervasyonlar) {
            if (!"Onaylandı".equals(r.getDurum())) continue;
            LocalDate d = parseDate(r.getGirisTarihi());
            if (d != null && d.getYear() == year) {
                monthly[d.getMonthValue() - 1] += r.getFiyat();
            }
        }
        return monthly;
    }

    // ===================== ✅ PDF OLUŞTUR =====================

    public static void exportRevenueToPDF(int year) {

        try {
            Document document = new Document();
            PdfWriter.getInstance(document,
                    new FileOutputStream("hasilat_" + year + ".pdf"));

            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

            document.add(new Paragraph("OTEL HASILAT RAPORU", titleFont));
            document.add(new Paragraph("Yıl: " + year, normalFont));
            document.add(new Paragraph("Tarih: " + LocalDate.now(), normalFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.addCell("Ay");
            table.addCell("Hasılat (TL)");

            String[] aylar = {
                    "Ocak","Şubat","Mart","Nisan","Mayıs","Haziran",
                    "Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık"
            };

            double[] monthly = getMonthlyRevenue(year);
            double yearlyTotal = getYearlyRevenue(year);

            for (int i = 0; i < 12; i++) {
                table.addCell(aylar[i]);
                table.addCell(String.valueOf(monthly[i]));
            }

            document.add(table);
            document.add(new Paragraph("\nYILLIK TOPLAM: " + yearlyTotal + " TL", titleFont));
            document.close();

            JOptionPane.showMessageDialog(null,
                    "PDF başarıyla oluşturuldu:\n" +
                            "hasilat_" + year + ".pdf");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "PDF oluşturulurken hata:\n" + e.getMessage());
        }
    }

    // ===================== ✅ DOSYA =====================

    public static void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {

            writer.write(standartOdalar.toString()); writer.newLine();
            writer.write(deluxeOdalar.toString());   writer.newLine();
            writer.write(kralOdalar.toString());     writer.newLine();

            for (Reservation r : rezervasyonlar) {

                String kisilerTekSatir = r.getKisiler()
                        .replace("\n", "<<<>>>")
                        .replace("|", "[PIPE]");

                writer.write(
                        r.getMusteriAdi() + "##" +
                        r.getMusteriEmail() + "##" +       // ✅ EMAIL
                        r.getOdaTipi() + "##" +
                        r.getOdaNo() + "##" +
                        r.getKisiSayisi() + "##" +
                        r.getGirisTarihi() + "##" +
                        r.getCikisTarihi() + "##" +
                        r.getFiyat() + "##" +
                        r.getDurum() + "##" +
                        kisilerTekSatir
                );
                writer.newLine();
            }

        } catch (Exception e) {
            System.out.println("Kayıt hatası: " + e.getMessage());
        }
    }

    public static void loadFromFile() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return;

            BufferedReader reader = new BufferedReader(new FileReader(file));

            standartOdalar = parseSet(reader.readLine());
            deluxeOdalar   = parseSet(reader.readLine());
            kralOdalar     = parseSet(reader.readLine());

            rezervasyonlar.clear();
            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("##");
                if (parts.length < 10) continue;

                String kisiler = parts[9]
                        .replace("<<<>>>", "\n")
                        .replace("[PIPE]", "|");

                Reservation r = new Reservation(
                        parts[0],                    // musteriAdi
                        parts[1],                    // musteriEmail
                        parts[2],                    // odaTipi
                        Integer.parseInt(parts[4]),  // kisiSayisi
                        kisiler,                     // kisiler
                        parts[5],                    // giris
                        parts[6],                    // cikis
                        Double.parseDouble(parts[7]) // fiyat
                );

                r.setOdaNo(Integer.parseInt(parts[3]));
                r.setDurum(parts[8]);

                rezervasyonlar.add(r);
            }

            reader.close();

        } catch (Exception e) {
            System.out.println("Dosya okuma hatası: " + e.getMessage());
        }
    }

    private static TreeSet<Integer> parseSet(String line) {
        TreeSet<Integer> set = new TreeSet<>();
        if (line == null || line.length() < 2) return set;

        line = line.replace("[", "").replace("]", "");
        String[] parts = line.split(",");

        for (String p : parts) {
            if (!p.trim().isEmpty())
                set.add(Integer.parseInt(p.trim()));
        }
        return set;
    }
}
