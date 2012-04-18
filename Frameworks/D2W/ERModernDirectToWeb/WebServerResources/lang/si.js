var fdLocale = {
fullMonths:["Januar", "Februar", "Marec", "April", "Maj", "Junij", "Julij", "Avgust", "September", "Oktober", "November", "December"],
monthAbbrs:["Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Avg", "Sep", "Okt", "Nov", "Dec"],
fullDays:["Ponedeljek", "Torek", "Sreda", "\u010Cetrtek", "Petek", "Sobota", "Nedelja"],
dayAbbrs:["Pon", "Tor", "Sre", "\u010Cet", "Pet", "Sob", "Ned"],
titles:["Prej\u0161nji mesec", "Naslednji mesec", "Prej\u0161nje leto", "Naslednje leto", "Danes", "Poka\u017Ei koledar", "td", "Teden [[%0%]] od [[%1%]]", "Teden", "Izberi datum", "Vleci in spusti za premik", "Prika\u017Ei najprej \u201C[[%0%]]\u201D", "Pojdi na dana\u0161nji datum", "Neveljaven datum"]};
try { 
        if("datePickerController" in window) { 
                datePickerController.loadLanguage(); 
        }; 
} catch(err) {};