package service;

import model.Kullanici;

import java.io.*;
import java.util.ArrayList;

public class KullaniciDosyaIslemleri {

    private static final String FILE_PATH = "users.txt";

    private static ArrayList<Kullanici> kullanicilar = new ArrayList<>();

    static {
        loadUsers();
    }

    public static ArrayList<Kullanici> getKullanicilar() {
        return kullanicilar;
    }

    // Kullanıcı giriş kontrolü
    public static Kullanici login(String kullaniciAdi, String sifre) {
        for (Kullanici k : kullanicilar) {
            if (k.getKullaniciAdi().equals(kullaniciAdi) &&
                k.getSifre().equals(sifre)) {
                return k;
            }
        }
        return null;
    }

    // Yeni kullanıcı ekleme
    public static void addUser(Kullanici k) {
        kullanicilar.add(k);
        saveUsers();
    }

    // ---------- TXT'YE KAYDETME ----------
    public static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {

            for (Kullanici k : kullanicilar) {
                writer.write(
                        k.getKullaniciAdi() + "|" +
                        k.getEmail() + "|" +
                        k.getSifre() + "|" +
                        k.getRol() +
                        "\n"
                );
            }

        } catch (Exception e) {
            System.out.println("Kullanıcı kaydetme hatası: " + e.getMessage());
        }
    }

    // ---------- TXT'DEN OKUMA ----------
    public static void loadUsers() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                // dosya yoksa varsayılan kullanıcılar
                kullanicilar.add(new Kullanici("tahir", "tahir@gmail.com", "1234", "user"));
                kullanicilar.add(new Kullanici("admin", "admin@otel.com", "admin", "admin"));
                kullanicilar.add(new Kullanici("kullanici", "test@otel.com", "1111", "user"));
                saveUsers();
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));

            kullanicilar.clear();

            String line;
            while ((line = reader.readLine()) != null) {

                String[] p = line.split("\\|");

                if (p.length < 4) continue;

                kullanicilar.add(new Kullanici(
                        p[0], p[1], p[2], p[3]
                ));
            }

            reader.close();

        } catch (Exception e) {
            System.out.println("Kullanıcı okuma hatası: " + e.getMessage());
        }
    }
}
