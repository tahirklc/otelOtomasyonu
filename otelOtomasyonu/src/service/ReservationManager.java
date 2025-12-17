package service;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.JOptionPane;

import model.Reservation;

// PDF için iText 5.x
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ReservationManager {

    private static final ArrayList<Reservation> rezervasyonlar = new ArrayList<>();

    // ODA NUMARA HAVUZLARI (BOŞ ODA HAVUZU)
    private static TreeSet<Integer> standartOdalar = new TreeSet<>();
    private static TreeSet<Integer> deluxeOdalar   = new TreeSet<>();
    private static TreeSet<Integer> kralOdalar     = new TreeSet<>();

    private static final String FILE_PATH =
            System.getProperty("user.dir") + File.separator + "reservations.txt";

    // Aktif rezervasyon -> oda dolu sayılır
    private static boolean isActiveStatus(String durum) {
        if (durum == null) return false;
        return durum.equals("Bekliyor") || durum.equals("Onaylandı");
    }

    // Pasif rezervasyon -> oda BOŞ sayılır (iade edilmeli)
    private static boolean isInactiveStatus(String durum) {
        if (durum == null) return false;
        return durum.equalsIgnoreCase("İptal Edildi")
                || durum.equalsIgnoreCase("İptal")
                || durum.equalsIgnoreCase("Reddedildi")
                || durum.equalsIgnoreCase("Red")
                || durum.equalsIgnoreCase("Bitti")
                || durum.equalsIgnoreCase("Tamamlandı")
                || durum.equalsIgnoreCase("Süresi Doldu");
    }

    static {
        // Başlangıç oda aralıkları (boş havuz)
        for (int i = 101; i <= 103; i++) standartOdalar.add(i);
        for (int i = 201; i <= 203; i++) deluxeOdalar.add(i);
        for (int i = 301; i <= 303; i++) kralOdalar.add(i);

        loadFromFile();

        // ✅ program açılır açılmaz: süresi bitenleri kapat + oda iade et
        refreshRoomsByReservations();
    }

    // ===================== REZERVASYON =====================

    public static void addReservation(Reservation r) {
        if (r == null) return;

        // ✅ eklemeden önce sistemde bitenleri temizle
        refreshRoomsByReservations();

        int odaNo = odaAyirVeNumaraVer(r.getOdaTipi());
        if (odaNo == -1) {
            JOptionPane.showMessageDialog(null, "Bu oda tipinde boş oda yok!");
            return;
        }

        r.setOdaNo(odaNo);

        // yeni rezervasyon default durum yoksa "Bekliyor" yap
        if (r.getDurum() == null || r.getDurum().trim().isEmpty()) {
            r.setDurum("Bekliyor");
        }

        rezervasyonlar.add(r);
        saveToFile();
    }

    public static ArrayList<Reservation> getReservations() {
        // ✅ her çağıranda bir güncelleme (admin paneli doğru görsün)
        refreshRoomsByReservations();
        return rezervasyonlar;
    }

    /**
     * ✅ Durum güncelle ve gerekiyorsa oda iade et
     * Bu metodu; iptal/red/onay/bitir işlemlerinde çağır.
     */
    public static void updateReservationStatus(Reservation r, String newStatus) {
        if (r == null || newStatus == null) return;

        // önce süre kontrolü (mesela onaylı ama süresi bitmiş olabilir)
        refreshRoomsByReservations();

        String old = r.getDurum();
        r.setDurum(newStatus);

        // aktiften pasife geçtiyse oda iade
        if (isActiveStatus(old) && isInactiveStatus(newStatus)) {
            odaIade(r.getOdaTipi(), r.getOdaNo());
        }

        saveToFile();
    }

    // ===================== ODA =====================

    public static boolean odaBosMu(String odaTipi) {
        refreshRoomsByReservations();

        if (odaTipi == null) return false;
        if (odaTipi.contains("Standart")) return !standartOdalar.isEmpty();
        if (odaTipi.contains("Deluxe"))   return !deluxeOdalar.isEmpty();
        if (odaTipi.contains("Kral"))     return !kralOdalar.isEmpty();
        return false;
    }

    private static int odaAyirVeNumaraVer(String odaTipi) {
        if (odaTipi == null) return -1;

        if (odaTipi.contains("Standart") && !standartOdalar.isEmpty())
            return standartOdalar.pollFirst();
        if (odaTipi.contains("Deluxe") && !deluxeOdalar.isEmpty())
            return deluxeOdalar.pollFirst();
        if (odaTipi.contains("Kral") && !kralOdalar.isEmpty())
            return kralOdalar.pollFirst();

        return -1;
    }

    public static void odaIade(String odaTipi, int odaNo) {
        if (odaTipi == null) return;
        if (odaNo <= 0) return;

        if (odaTipi.contains("Standart")) standartOdalar.add(odaNo);
        else if (odaTipi.contains("Deluxe")) deluxeOdalar.add(odaNo);
        else if (odaTipi.contains("Kral")) kralOdalar.add(odaNo);

        saveToFile();
    }

    public static int getStandartKalan() {
        refreshRoomsByReservations();
        return standartOdalar.size();
    }
    public static int getDeluxeKalan() {
        refreshRoomsByReservations();
        return deluxeOdalar.size();
    }
    public static int getKralKalan() {
        refreshRoomsByReservations();
        return kralOdalar.size();
    }

    // ✅ Admin ekranı için toplam oda sayısı
    public static int getStandartTotal() {
        refreshRoomsByReservations();
        return standartOdalar.size() + getActiveOccupiedCount("Standart");
    }
    public static int getDeluxeTotal() {
        refreshRoomsByReservations();
        return deluxeOdalar.size() + getActiveOccupiedCount("Deluxe");
    }
    public static int getKralTotal() {
        refreshRoomsByReservations();
        return kralOdalar.size() + getActiveOccupiedCount("Kral");
    }

    private static int getActiveOccupiedCount(String tip) {
        int dolu = 0;
        for (Reservation r : rezervasyonlar) {
            if (r == null) continue;
            if (!isActiveStatus(r.getDurum())) continue;
            if (r.getOdaTipi() != null && r.getOdaTipi().contains(tip)) {
                dolu++;
            }
        }
        return dolu;
    }

    // ===================== BOŞ ODA SİLME =====================

    public static boolean standartOdaSil() {
        refreshRoomsByReservations();
        if (standartOdalar.isEmpty()) return false;
        standartOdalar.pollLast();
        saveToFile();
        return true;
    }

    public static boolean deluxeOdaSil() {
        refreshRoomsByReservations();
        if (deluxeOdalar.isEmpty()) return false;
        deluxeOdalar.pollLast();
        saveToFile();
        return true;
    }

    public static boolean kralOdaSil() {
        refreshRoomsByReservations();
        if (kralOdalar.isEmpty()) return false;
        kralOdalar.pollLast();
        saveToFile();
        return true;
    }

    // ===================== YENİ ODA EKLEME =====================

    public static void yeniStandartOdaEkle() {
        refreshRoomsByReservations();
        int yeniNo = standartOdalar.isEmpty() ? 101 : standartOdalar.last() + 1;
        standartOdalar.add(yeniNo);
        saveToFile();
    }

    public static void yeniDeluxeOdaEkle() {
        refreshRoomsByReservations();
        int yeniNo = deluxeOdalar.isEmpty() ? 201 : deluxeOdalar.last() + 1;
        deluxeOdalar.add(yeniNo);
        saveToFile();
    }

    public static void yeniKralOdaEkle() {
        refreshRoomsByReservations();
        int yeniNo = kralOdalar.isEmpty() ? 301 : kralOdalar.last() + 1;
        kralOdalar.add(yeniNo);
        saveToFile();
    }

    // ===================== OTOMATİK BOŞALTMA (DÜZELTİLDİ) =====================

    /**
     * ✅ Şunları garanti eder:
     * - "Onaylandı" veya "Bekliyor" olup çıkış tarihi bugünden küçük olanlar -> "Bitti" yapılır
     * - "Bitti / İptal / Reddedildi / Süresi Doldu ..." olanların odaları BOŞ havuza iade edilir
     * - Aktif olanların oda numaraları BOŞ havuzda kalmaz
     */
    public static void refreshRoomsByReservations() {
        LocalDate today = LocalDate.now();

        Set<Integer> aktifStandart = new HashSet<>();
        Set<Integer> aktifDeluxe   = new HashSet<>();
        Set<Integer> aktifKral     = new HashSet<>();

        boolean changed = false;

        for (Reservation r : rezervasyonlar) {
            if (r == null) continue;

            // ✅ 1) Onaylandı/Bekliyor ama süresi bitmişse -> Bitti + oda iade
            LocalDate cikis = parseDate(r.getCikisTarihi());
            if (cikis != null && cikis.isBefore(today) && isActiveStatus(r.getDurum())) {
                r.setDurum("Bitti");
                changed = true;
            }

            // ✅ 2) Pasif durum ise oda iade edilmeli
            if (isInactiveStatus(r.getDurum())) {
                // oda numarası boş havuzda yoksa ekle (TreeSet zaten duplicate engeller)
                odaIadeIfMissing(r.getOdaTipi(), r.getOdaNo());
                changed = true;
                continue;
            }

            // ✅ 3) Aktifse dolu kabul et ve boş havuzdan çıkarılmasını garanti et
            if (isActiveStatus(r.getDurum())) {
                if (r.getOdaTipi() == null) continue;

                if (r.getOdaTipi().contains("Standart")) aktifStandart.add(r.getOdaNo());
                else if (r.getOdaTipi().contains("Deluxe")) aktifDeluxe.add(r.getOdaNo());
                else if (r.getOdaTipi().contains("Kral")) aktifKral.add(r.getOdaNo());
            }
        }

        // ✅ 4) Güvenlik: aktif odalar boş havuzda durmasın
        if (standartOdalar.removeAll(aktifStandart)) changed = true;
        if (deluxeOdalar.removeAll(aktifDeluxe)) changed = true;
        if (kralOdalar.removeAll(aktifKral)) changed = true;

        if (changed) saveToFile();
    }

    private static void odaIadeIfMissing(String odaTipi, int odaNo) {
        if (odaTipi == null || odaNo <= 0) return;

        if (odaTipi.contains("Standart")) standartOdalar.add(odaNo);
        else if (odaTipi.contains("Deluxe")) deluxeOdalar.add(odaNo);
        else if (odaTipi.contains("Kral")) kralOdalar.add(odaNo);
    }

    // ===================== MÜŞTERİ ARAMA =====================

    public static List<Reservation> searchByName(String nameQuery) {
        List<Reservation> result = new ArrayList<>();
        if (nameQuery == null || nameQuery.trim().isEmpty()) return result;

        String q = nameQuery.toLowerCase();
        for (Reservation r : rezervasyonlar) {
            String ad = r.getMusteriAdi();
            if (ad != null && ad.toLowerCase().contains(q)) {
                result.add(r);
            }
        }
        return result;
    }

    // ===================== TARİH =====================

    private static LocalDate parseDate(String dateStr) {
        try {
            if (dateStr == null) return null;
            dateStr = dateStr.trim().replace(",", ".");
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (Exception e) {
            return null;
        }
    }

    // ===================== GELİR / HASILAT =====================

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
            if (d != null && d.getYear() == year) total += r.getFiyat();
        }
        return total;
    }

    public static double[] getMonthlyRevenue(int year) {
        double[] monthly = new double[12];
        for (Reservation r : rezervasyonlar) {
            if (!"Onaylandı".equals(r.getDurum())) continue;
            LocalDate d = parseDate(r.getGirisTarihi());
            if (d != null && d.getYear() == year) monthly[d.getMonthValue() - 1] += r.getFiyat();
        }
        return monthly;
    }

    // ===================== PDF OLUŞTUR =====================

    public static void exportRevenueToPDF(int year) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("hasilat_" + year + ".pdf"));
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
                table.addCell(String.format(Locale.US, "%.2f", monthly[i]));
            }

            document.add(table);
            document.add(new Paragraph("\nYILLIK TOPLAM: " + String.format(Locale.US, "%.2f", yearlyTotal) + " TL", titleFont));
            document.close();

            JOptionPane.showMessageDialog(null, "PDF başarıyla oluşturuldu:\n" + "hasilat_" + year + ".pdf");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "PDF oluşturulurken hata:\n" + e.getMessage());
        }
    }

    // ===================== DOSYA =====================

    public static void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {

            writer.write(standartOdalar.toString()); writer.newLine();
            writer.write(deluxeOdalar.toString());   writer.newLine();
            writer.write(kralOdalar.toString());     writer.newLine();

            for (Reservation r : rezervasyonlar) {

                String kisilerTekSatir = String.valueOf(r.getKisiler())
                        .replace("\n", "<<<>>>")
                        .replace("|", "[PIPE]");

                String musteriAdi = String.valueOf(r.getMusteriAdi());
                String musteriEmail = String.valueOf(r.getMusteriEmail());
                String odaTipi = String.valueOf(r.getOdaTipi());
                String giris = String.valueOf(r.getGirisTarihi());
                String cikis = String.valueOf(r.getCikisTarihi());
                String durum = String.valueOf(r.getDurum());

                writer.write(
                        musteriAdi + "##" +
                                musteriEmail + "##" +
                                odaTipi + "##" +
                                r.getOdaNo() + "##" +
                                r.getKisiSayisi() + "##" +
                                giris + "##" +
                                cikis + "##" +
                                r.getFiyat() + "##" +
                                durum + "##" +
                                kisilerTekSatir
                );
                writer.newLine();
            }

        } catch (Exception e) {
            System.out.println("Kayıt hatası: " + e.getMessage());
        }
    }

    public static void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            standartOdalar = parseSet(reader.readLine());
            deluxeOdalar   = parseSet(reader.readLine());
            kralOdalar     = parseSet(reader.readLine());

            rezervasyonlar.clear();
            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("##", -1);
                if (parts.length < 10) continue;

                String kisiler = parts[9]
                        .replace("<<<>>>", "\n")
                        .replace("[PIPE]", "|");

                Reservation r = new Reservation(
                        parts[0],
                        parts[1],
                        parts[2],
                        Integer.parseInt(parts[4]),
                        kisiler,
                        parts[5],
                        parts[6],
                        Double.parseDouble(parts[7])
                );

                r.setOdaNo(Integer.parseInt(parts[3]));
                r.setDurum(parts[8]);

                rezervasyonlar.add(r);
            }

        } catch (Exception e) {
            System.out.println("Dosya okuma hatası: " + e.getMessage());
        }
    }

    private static TreeSet<Integer> parseSet(String line) {
        TreeSet<Integer> set = new TreeSet<>();
        if (line == null) return set;

        line = line.trim();
        if (line.length() < 2) return set;

        line = line.replace("[", "").replace("]", "").trim();
        if (line.isEmpty()) return set;

        String[] parts = line.split(",");
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) set.add(Integer.parseInt(t));
        }
        return set;
    }
}
