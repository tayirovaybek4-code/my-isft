// ISFT PREMIUM: Google Apps Script web app. Google Sheets ID oldindan kiritilgan.
// Deploy > New deployment > Web app > Execute as Me > Who has access: Anyone.
// Havolani app.js faylidagi SHEETS_ENDPOINT qiymatiga kiriting.
const SHEET_ID = '11RWlQZ4ApNYJCSXtbKM4VVM3DGc2UOTRmxFV00o-za0';
function doPost(e) {
  try {
    const d = JSON.parse(e.postData.contents || '{}');
    const ss = SpreadsheetApp.openById(SHEET_ID);
    const registration = d.type === 'registration';
    if (!registration && d.type !== 'survey') throw Error('Noto‘g‘ri forma');
    const n = Number(d.surveyId);
    if (!registration && (!Number.isInteger(n) || n < 1 || n > 5)) throw Error('So‘rovnoma raqami xato');
    const tab = registration ? 'Royxatdan_otganlar' : 'Sorovnoma_' + n;
    let sheet = ss.getSheetByName(tab);
    if (!sheet) {
      sheet = ss.insertSheet(tab);
      sheet.appendRow(registration ? ['Sana','Ism','Familiya','Telefon','Yonalish'] : ['Sana','Savol_1','Savol_2','Savol_3','Savol_4','Savol_5']);
      sheet.setFrozenRows(1);
    }
    function safe(v) {
      let s = String(v == null ? '' : v).trim().slice(0,500);
      if (/^[\s]*[=+@-]/.test(s)) s = "'" + s;
      return s;
    }
    if (registration) {
      if (!d.firstName || !d.lastName || !d.phone) throw Error('Majburiy maydonlar to‘ldirilmagan');
      sheet.appendRow([new Date(),safe(d.firstName),safe(d.lastName),safe(d.phone),safe(d.direction)]);
    } else {
      if (![d.q1,d.q2,d.q3,d.q4,d.q5].every(Boolean)) throw Error('5 ta javob talab qilinadi');
      sheet.appendRow([new Date(),safe(d.q1),safe(d.q2),safe(d.q3),safe(d.q4),safe(d.q5)]);
    }
    return ContentService.createTextOutput(JSON.stringify({ok:true})).setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ok:false,error:String(err)})).setMimeType(ContentService.MimeType.JSON);
  }
}
