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

---

# 🔍Cara Mencoba API dengan Postman
Untuk menguji dan mencoba endpoint dari API ini, Anda dapat menggunakan Postman, sebuah aplikasi yang memudahkan pengiriman permintaan HTTP.

Berikut adalah metode yang tersedia:

## 📥 GET
Digunakan untuk mengambil data dari server.
Contoh:
GET /villas
Menampilkan daftar semua vila.

## ➕ POST
Digunakan untuk menambahkan data baru ke server.
Contoh:
POST /villas
Menambahkan vila baru dengan data yang dikirim melalui body (format JSON).

## ✏️ PUT
Digunakan untuk mengubah data yang sudah ada.
Contoh:
PUT /villas/{id}
Memperbarui informasi vila berdasarkan ID yang diberikan.

## ❌ DELETE
Digunakan untuk menghapus data dari server.
Contoh:
DELETE /villas/{id}
Menghapus vila berdasarkan ID.

## Berikut Screenshoot penggunaan POST MAN

### 🏡Villa
GET VILLA   

<img src="Gambar/villas.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menampilkan semua data villa yang tersedia.

---

<img src="Gambar/villas{id}.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menampilkan data villa berdasarkan id villa.

---

<img src="Gambar/villas{id}-rooms.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menampilkan data suatu villa berdasarkan id dan menampilkan informasi kamar suatau villa, lengkap dengan fasilitas dan harga.

---

<img src="Gambar/villas{id}-bookings.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menampilkan daftar semau booking pada suatu villa.

---

<img src="Gambar/villas{id}-reviews.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menampilkan daftar semua review pada suatu villa.

---

<img src="Gambar/villas-date-get.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menampilkan pencarian ketersediaan villa berdasarkan tanggal check-in dan check-out.

---

POST VILLA

<img src="Gambar/post-villa.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menambahkan data villa.

---

<img src="Gambar/post-villa{id}-rooms.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menambahkan data tipe kamar pada villa.

---
PUT VILLA

<img src="Gambar/put-villa{id}.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk mengubah data suatu villa berdasarkan id.

---

<img src="Gambar/put-villa{id}-room{id}.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk mengubah informasi kamar suatu villa.

---
DELETE VILLA

<img src="Gambar/delete-villa{id}.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menghapus data suatu villa.

---

<img src="Gambar/delete-villa{id}-room{id}.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menghapus kamar suatu villa.

---

### 🧑‍🤝‍👩CUSTOMERS
GET CUSTOMERS

<img src="Gambar/get-customers.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk melihat daftar semua customer.

---

<img src="Gambar/get-customers{id}.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk melihat informasi detail mengenai customer berdasarkan id.

---

<img src="Gambar/get-customers{id}-booking.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk melihat data daftar booking yang telah dilakukan oleh seorang customer.

---

<img src="Gambar/get-customers{id}-reviews.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk melihat data daftar ulasan yang telah diberikan oleh customer.

---
POST CUSTOMERS

<img src="Gambar/post-customers.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menambahkan customer baru (registrasi customer).

---

<img src="Gambar/post-customers{id}-bookings.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk customer melakukan pemesanan villa.

---

<img src="Gambar/post-customers{id}-bookings{id}-reviews.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk customer memberikan ulasan pada vila (berdasarkan informasi booking).

---
PUT CUSTOMERS

<img src="Gambar/put-customers{id}.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk mengubah data customer berdasarkan id.

---
### 📜VOUCHER
GET VOUCHERS

<img src="Gambar/get-vouchers.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk melihat data semua voucher.

---

<img src="Gambar/get-vouchers{id}.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk melihat informasi detail mengenai voucher berdasarkan id.

---
POST VOUCHERS

<img src="Gambar/post-vouchers.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk membuat atau menambahkan voucher baru.

---
PUT VOUCHERS

<img src="Gambar/put-vouchers{id}.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk mengubah data suatu voucher berdasarkan id.

---
DELETE VOUCHERS

<img src="Gambar/delete-vouchers{id}.jpg" width="700"/>
Gambar di atas merupakan tampilan Post Man untuk menghapus suatu data voucher berdasarkan id.



