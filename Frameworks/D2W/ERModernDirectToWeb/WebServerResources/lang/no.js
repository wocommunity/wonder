var fdLocale = {
fullMonths:["Januar", "Februar", "Mars", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Desember"],
monthAbbrs:["Jan", "Feb", "Mar", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Des"],
fullDays:["Mandag", "Tirsdag", "Onsdag", "Torsdag", "Fredag", "L\u00F8rdag", "S\u00F8ndag"],
dayAbbrs:["Man", "Tir", "Ons", "Tor", "Fre", "L\u00F8r", "S\u00F8n"],
titles:["Forrige m\u00E5ned", "Neste m\u00E5ned", "Forrige \u00E5r", "Neste \u00E5r", "I dag", "Vis kalender", "uk", "Uke [[%0%]] av [[%1%]]", "Uke", "Velg dato", "Klikk og dra for \u00E5 flytte", "Vis [[%0%]] f\u00F8rst", "G\u00E5 til dagens dato", "Deaktivert dato"]};
try { 
        if("datePickerController" in window) { 
                datePickerController.loadLanguage(); 
        }; 
} catch(err) {};