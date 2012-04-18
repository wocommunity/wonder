/* 2009-03-13 12:38:00 MEZ created by Nils Schreiber (n dot schreiber at gmx dot de) */
var fdLocale = {
fullMonths:["Januar", "Februar", "M\u00E4rz", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"],
monthAbbrs:["Jan", "Feb", "Mrz", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"],
fullDays:["Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"],
dayAbbrs:["Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"],
titles:["vorheriger Monat", "n\u00E4chster Monat", "vorheriges Jahr", "n\u00E4chstes Jahr", "Heute", "Kalender anzeigen", "KW", "Woche [[%0%]] von [[%1%]]", "Woche", "W\u00E4hlen Sie ein Datum", "Klicken \u0026 Ziehen zum Verschieben", "Zeige [[%0%]] zuerst", "Zu Heute wechseln", "Datum deaktivieren"]};
try { 
        if("datePickerController" in window) { 
                datePickerController.loadLanguage(); 
        }; 
} catch(err) {}; 
