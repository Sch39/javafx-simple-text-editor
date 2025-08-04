# Simple Text Editor
Sebuah editor teks sederhana yang dibangun menggunakan JavaFX, dirancang untuk belajar implementasi konkurensi (concurrency) dan arsitektur kode yang bersih. Aplikasi ini memungkinkan pengguna untuk membuat, mengedit, membuka file, menyimpan file teks tanpa mengunci (freezing) antarmuka pengguna dan lainnya.

## Fitur Utama

- Konkurensi: Semua operasi file yang memakan waktu (membaca, menulis file) dijalankan di thread latar belakang untuk menjaga UI tetap responsif.

- Manajemen File:
  - Buat file baru.
  - Simpan file yang sedang diedit.
  - Simpan file dengan nama baru (Save As).
  - Buka folder dan tampilkan daftar file di dalamnya (v).
  - rename file name with 1 click mouse (ongoing).
  
- Antarmuka Pengguna Intuitif:
    - Tanda * pada nama file menunjukkan adanya perubahan yang belum disimpan.
    - Progress bar yang muncul saat operasi file sedang berlangsung.
    - Pintasan keyboard (Ctrl + S, Ctrl + Shift + S, Ctrl + Z, Ctrl + Y, Ctrl+O)
## Teknologi yang Digunakan
  - Bahasa Pemprograman: Java 21+
  - Framework UI: JavaFX 21+
  - Alat Build: Maven

## Cara Menjalankan Aplikasi
Aplikasi ini dibangun dengan Maven. Anda memerlukan JDK 21 atau versi yang lebih baru dan JavaFX SDK.

1. Klon Repositori
```
https://github.com/Sch39/javafx-simple-text-editor.git

cd javafx-simple-text-editor
```

2. Jalankan dengan Maven

Gunakan plugin JavaFX Maven untuk menjalankan aplikasi.
```
mvn clean javafx:run
```

## Lisensi
Proyek ini dilisensikan di bawah [Lisensi MIT](./LICENSE).