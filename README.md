# 🏠Project-PBO-2
API Pemesanan Vila ini dikembangkan untuk mempermudah pengelolaan data vila, customer, dan booking secara terintegrasi dengan menggunakan **INTELIJ** dan juga **Postman**. API ini memungkinkan pengguna untuk melakukan operasi CRUD (Create, Read, Update, Delete) terhadap entitas, dengan validasi dan error handling yang sesuai standar. Setiap permintaan dan respons menggunakan format JSON, dan API diamankan menggunakan API key. Proyek ini menggunakan database SQLite untuk menyimpan data dan diuji menggunakan Postman.

---

# 👨‍👩‍👧‍👦Anggota Kelompok
1. Made Sheva Adi Pramana(2405551037)
2. I Gede Pasek Surya Dharma Kesuma(2405551086)
3. Pande Putu Satya Naraya Adyana(2405551087)
4. Dewa Gede Junior Satria Erlangga(2405551096)

---

# 🧩Fitur Utama
Adapun fitur yang terdapat dalam projek API pemesanan vila, yaitu:   
✅  Mendapatkan daftar semua vila beserta detailnya.  
✅  Menambahkan, Memperbarui, serta menghapus data vila.  
✅  Penyimpanan data dengan menggunakan database SQLite.  
✅  Proteksi akses API dengan menggunakan API Key.  
✅  Penanganan eror respons JSON yang informatif.

---

# 📦Entitas Database
- Villas  
- Vouchers  
- Bookings  
- Customers  
- Reviews  
- Room_types  

---

# 📌 Endpoint API

### 🏠 Villa
| Metode | Endpoint | Deskripsi |
| --- | --- | --- |
| GET | /villas | Daftar semua vila |
| GET | /villas/{id} | Informasi detail suatu vila |
| GET | /villas/{id}/rooms | Informasi kamar suatu vila, lengkap dengan fasilitas dan harga |
| GET | /villas/{id}/bookings | Daftar semua booking pada suatu vila |
| GET | /villas/{id}/reviews | Daftar semua review pada suatu vila |
| GET | /villas?ci_date={checkin_date}&co_date={checkout_date} | Pencarian ketersediaan vila berdasarkan tanggal check-in dan checkout |
| POST | /villas | Menambahkan data vila |
| POST | /villas/{id}/rooms | Menambahkan tipe kamar pada vila |
| PUT | /villas/{id} | Mengubah data suatu vila |
| PUT | /villas/{id}/rooms/{id} | Mengubah informasi kamar suatu vila |
| DELETE | /villas/{id}/rooms/{id} | Menghapus kamar suatu vila |
| DELETE | /villas/{id} | Menghapus data suatu vila |

### 👤 Customer
| Metode | Endpoint | Deskripsi |
| --- | --- | --- |
| GET | /customers | Daftar semua customer |
| GET | /customers/{id} | Informasi detail seorang customer |
| GET | /customers/{id}/bookings | Daftar booking yang telah dilakukan oleh seorang customer |
| GET | /customers/{id}/reviews | Daftar ulasan yang telah diberikan oleh customer |
| POST | /customers | Menambahkan customer baru (registrasi customer) |
| POST | /customers/{id}/bookings | Customer melakukan pemesanan vila |
| POST | /customers/{id}/bookings/{id}/reviews | Customer memberikan ulasan pada vila (berdasarkan informasi booking) |
| PUT | /customers/{id} | Mengubah data seorang customer |

### 🎟 Voucher
| Metode | Endpoint | Deskripsi |
| --- | --- | --- |
| GET | /vouchers | Daftar semua voucher |
| GET | /vouchers/{id} | Informasi detail suatu voucher |
| POST | /vouchers | Membuat voucher baru |
| PUT | /vouchers/{id} | Mengubah data suatu voucher |
| DELETE | /vouchers/{id} | Menghapus data suatu voucher |
