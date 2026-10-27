# ISFT PREMIUM V2 — ishga tushirish

1. Papkani VS Code dasturida oching. Live Server bilan `index.html` ni ishga tushiring (yoki terminalda `python -m http.server 8000` va `http://localhost:8000`).
2. `surveys.html` sahifasida **5 ta alohida so‘rovnoma**, har birida **5 ta savol** bor.
3. `tour.html` — interaktiv 3D model. Internet kerak: Three.js CDN dan yuklanadi. Agar model chiqmasa, internetni, brauzerning WebGL imkoniyatini va Developer Tools Console xatolarini tekshiring. Model binoning suratlariga asoslangan taxminiy maket, haqiqiy 3D skan emas.
4. Google Sheets ulash: Google Sheets jadvalini yarating, URL dan ID ni oling, `google-apps-script.gs` faylidagi `SHEET_ID` ga joylang. Sheets -> Extensions -> Apps Script ga kodni qo‘ying. Deploy -> New deployment -> Web app; Execute as Me; Who has access Anyone. Berilgan `/exec` URL ni `app.js` dagi `SHEETS_ENDPOINT` ga qo‘ying.
5. Sinov: 5 ta so‘rovnomadan bittadan javob yuboring va Sheets dagi `Survey_1` ... `Survey_5` varaqlarini tekshiring. Ro‘yxatdan o‘tishni ham sinang (`Registrations`).
6. Google Sheets ulanmaguncha javoblar faqat brauzerning localStorage xotirasiga saqlanadi. Real foydalanuvchilardan ma’lumot yig‘ishdan avval maxfiylik siyosati, rozilik va xavfsiz backend sozlanishi zarur.

Bu ISFT ning rasmiy sayti emas, mustaqil o‘quv loyihasi.
