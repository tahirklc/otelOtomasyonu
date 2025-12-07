package service;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import model.Reservation;

public class ReservationManager {

    private static ArrayList<Reservation> rezervasyonlar = new ArrayList<>();

    // ✅ ODA NUMARA HAVUZLARI
    private static TreeSet<Integer> standartOdalar = new TreeSet<>();
    private static TreeSet<Integer> deluxeOdalar   = new TreeSet<>();
    private static TreeSet<Integer> kralOdalar     = new TreeSet<>();

    // TXT dosyası
    private static final String FILE_PATH = "reservations.txt";

    static {
        // ✅ BAŞLANGIÇ ODA NUMARALARI (DOSYA YOKSA)
        for (int i = 101; i <= 103; i++) standartOdalar.add(i);
        for (int i = 201; i <= 203; i++) deluxeOdalar.add(i);
        for (int i = 301; i <= 303; i++) kralOdalar.add(i);

        loadFromFile(); // ✅ Dosya varsa bunları ezer
    }

    // ---------------- REZERVASYON ----------------

    public static void addReservation(Reservation r) {
        int odaNo = odaAyirVeNumaraVer(r.getOdaTipi());
        r.setOdaNo(odaNo);

        rezervasyonlar.add(r);
        saveToFile();
    }

    public static ArrayList<Reservation> getReservations() {
        return rezervasyonlar;
    }

    // ---------------- ODA KONTROL ----------------

    public static boolean odaBosMu(String odaTipi) {
        if (odaTipi.contains("Standart")) return !standartOdalar.isEmpty();
        if (odaTipi.contains("Deluxe"))   return !deluxeOdalar.isEmpty();
        if (odaTipi.contains("Kral"))     return !kralOdalar.isEmpty();
        return false;
    }

    // ✅ OTOMATİK ODA NUMARASI VER
    private static int odaAyirVeNumaraVer(String odaTipi) {
        if (odaTipi.contains("Standart") && !standartOdalar.isEmpty()) return standartOdalar.pollFirst();
        if (odaTipi.contains("Deluxe")   && !deluxeOdalar.isEmpty())   return deluxeOdalar.pollFirst();
        if (odaTipi.contains("Kral")     && !kralOdalar.isEmpty())     return kralOdalar.pollFirst();
        return -1;
    }

    // ✅ İPTALDE NUMARAYI GERİ KOY
    public static void odaIade(String odaTipi, int odaNo) {
        if (odaTipi.contains("Standart")) standartOdalar.add(odaNo);
        if (odaTipi.contains("Deluxe"))   deluxeOdalar.add(odaNo);
        if (odaTipi.contains("Kral"))     kralOdalar.add(odaNo);
        saveToFile();
    }

    // ✅ ADMIN GÖRÜNTÜLEME
    public static int getStandartKalan() { return standartOdalar.size(); }
    public static int getDeluxeKalan()   { return deluxeOdalar.size(); }
    public static int getKralKalan()     { return kralOdalar.size(); }

    // ---------------- ✅ YENİ ODA EKLEME ----------------

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

    // ✅ İSME GÖRE ARAMA
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

    // ✅ TC'YE GÖRE ARAMA
    public static List<Reservation> searchByTc(String tc) {
        List<Reservation> result = new ArrayList<>();
        if (tc == null || tc.isEmpty()) return result;

        for (Reservation r : rezervasyonlar) {
            String kisiler = r.getKisiler();
            if (kisiler != null && kisiler.contains("TC: " + tc)) {
                result.add(r);
            }
        }
        return result;
    }

    // ===================== ✅ TARİH YARDIMCI =====================

    private static LocalDate parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            return LocalDate.parse(dateStr, fmt);
        } catch (Exception e) {
            return null;
        }
    }

    // ===================== ✅ HASILAT / GELİR =====================

    // ✅ SİSTEMDEKİ TÜM YILLARI BULUR
    public static Set<Integer> getReservationYears() {
        Set<Integer> years = new TreeSet<>();
        for (Reservation r : rezervasyonlar) {
            LocalDate d = parseDate(r.getGirisTarihi());
            if (d != null) {
                years.add(d.getYear());
            }
        }
        return years;
    }

    // ✅ YILLIK HASILAT (SADECE ONAYLANANLAR)
    public static double getYearlyRevenue(int year) {
        double total = 0;

        for (Reservation r : rezervasyonlar) {
            if (!"Onaylandı".equals(r.getDurum())) continue;

            LocalDate d = parseDate(r.getGirisTarihi());
            if (d == null) continue;

            if (d.getYear() == year) {
                total += r.getFiyat();
            }
        }
        return total;
    }

    // ✅ AYLIK HASILAT
    // index 0 = Ocak, 11 = Aralık
    public static double[] getMonthlyRevenue(int year) {
        double[] monthly = new double[12];

        for (Reservation r : rezervasyonlar) {
            if (!"Onaylandı".equals(r.getDurum())) continue;

            LocalDate d = parseDate(r.getGirisTarihi());
            if (d == null) continue;

            if (d.getYear() == year) {
                int monthIndex = d.getMonthValue() - 1;
                monthly[monthIndex] += r.getFiyat();
            }
        }
        return monthly;
    }

    // ---------------- DOSYAYA KAYDET ----------------

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

    // ---------------- DOSYADAN OKU ----------------

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
                if (parts.length < 9) continue;

                String kisiler = parts[8]
                        .replace("<<<>>>", "\n")
                        .replace("[PIPE]", "|");

                Reservation r = new Reservation(
                        parts[0],                       // musteri
                        parts[1],                       // oda tipi
                        Integer.parseInt(parts[3]),     // kişi sayısı
                        kisiler,
                        parts[4],                       // giriş
                        parts[5],                       // çıkış
                        Double.parseDouble(parts[6])    // fiyat
                );

                r.setOdaNo(Integer.parseInt(parts[2]));
                r.setDurum(parts[7]);

                rezervasyonlar.add(r);
            }

            reader.close();

        } catch (Exception e) {
            System.out.println("Dosya okuma hatası: " + e.getMessage());
        }
    }

    // ✅ "[101, 102, 103]" → TreeSet
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
