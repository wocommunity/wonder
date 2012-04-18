/*! DatePicker v6a MIT/GPL2 @freqdec */
var datePickerController = (function datePickerController() {

        var debug               = false,
            isOpera             = Object.prototype.toString.call(window.opera) === "[object Opera]",
            describedBy         = "",
            languageInfo        = parseUILanguage(),
            nbsp                = String.fromCharCode(160),
            datePickers         = {},
            weeksInYearCache    = {},
            bespokeTitles       = {},
            uniqueId            = 0,
            finalOpacity        = 100,
            buttonTabIndex      = true,
            mouseWheel          = true,
            deriveLocale        = false,
            localeImport        = false,
            nodrag              = false,            
            returnLocaleDate    = false,              
            kbEvent             = false,
            cellFormat          = "%d %F %Y",
            titleFormat         = "%F %d, %Y",
            statusFormat        = "",
            formatParts         = isOpera ? ["%j"] : ["%j", " %F %Y"],
            dPartsRegExp        = /%([d|j])/,
            mPartsRegExp        = /%([M|F|m|n])/,
            yPartsRegExp        = /%[y|Y]/,                                    
            noSelectionRegExp   = /date-picker-unused|out-of-range|day-disabled|not-selectable/,
            formatTestRegExp    = /%([d|j|M|F|m|n|Y|y])/,
            formatSplitRegExp   = /%([d|D|l|j|N|w|S|W|M|F|m|n|t|Y|y])/,
            rangeRegExp         = /^((\d\d\d\d)(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01]))$/,
            wcDateRegExp        = /^(((\d\d\d\d)|(\*\*\*\*))((0[1-9]|1[012])|(\*\*))(0[1-9]|[12][0-9]|3[01]))$/,            
            wsCharClass         = "\u0009\u000A\u000B\u000C\u000D\u0020\u00A0\u1680\u180E\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u202F\u205F\u3000\u2028\u2029";                                      
                
        (function() {                 
                var scriptFiles = document.getElementsByTagName('script'),
                    json        = parseJSON(String(scriptFiles[scriptFiles.length - 1].innerHTML).replace(/[\n\r\s\t]+/g, " ").replace(/^\s+/, "").replace(/\s+$/, ""));                
               
                if(typeof json === "object" && !("err" in json)) {                          
                        affectJSON(json);
                };
       
                if(deriveLocale && typeof(fdLocale) != "object") {
                        var head   = document.getElementsByTagName("head")[0] || document.documentElement,
                            loc    = scriptFiles[scriptFiles.length - 1].src.substr(0, scriptFiles[scriptFiles.length - 1].src.lastIndexOf("/")) + "/lang/",
                            script,
                            i;
                        
                        for(i = 0; i < languageInfo.length; i++) {                                 
                                script          = document.createElement('script');                                               
                                script.type     = "text/javascript";                         
                                script.src      = loc + languageInfo[i] + ".js"; 
                                script.charSet  = "utf-8";
                                
                                /*@cc_on
                                /*@if(@_win32)
                                var bases = document.getElementsByTagName('base');
                                if (bases.length && bases[0].childNodes.length) {
                                        bases[0].appendChild(script);
                                } else {
                                        document.getElementsByTagName('head')[0].appendChild(script);
                                };
                                bases = null;
                                @else @*/
                                head.appendChild(script);
                                /*@end
                                @*/    
                        };
                        script = null;                      
                } else {
                        returnLocaleDate = true;
                };                              
        })();
        
        // Simple add/remove class methods - they are slow but used rarely
        function addClass(e, c) {
                if(new RegExp("(^|[" + wsCharClass + "])" + c + "([" + wsCharClass + "]|$)").test(e.className)) { 
                        return; 
                };
                e.className += ( e.className ? " " : "" ) + c;
        };
        
        function removeClass(e, c) {
                e.className = !c ? "" : e.className.replace(new RegExp("(^|[" + wsCharClass + "])" + c + "([" + wsCharClass + "]|$)"), " ").replace(new RegExp("/^[" + wsCharClass + "][" + wsCharClass + "]*/"), '').replace(new RegExp("/[" + wsCharClass + "][" + wsCharClass + "]*$/"), '');
        };
        
        // Attempts to parse the current language from the HTML element. Defaults to "en" if none given.
        function parseUILanguage() {                                 
                var languageTag = document.getElementsByTagName('html')[0].getAttribute('lang') || document.getElementsByTagName('html')[0].getAttribute('xml:lang');                
                languageTag = !languageTag ? "en" : languageTag.toLowerCase();                                                            
                return languageTag.search(/^([a-z]{2,3})-([a-z]{2})$/) != -1 ? [languageTag.match(/^([a-z]{2,3})-([a-z]{2})$/)[1], languageTag] : [languageTag];                       
        };
        
        // Cross browser split from http://blog.stevenlevithan.com/archives/cross-browser-split 
        cbSplit = function(str, separator, limit) {
                // if `separator` is not a regex, use the native `split`
                if(Object.prototype.toString.call(separator) !== "[object RegExp]") {
                        return cbSplit._nativeSplit.call(str, separator, limit);
                };

                var output = [],
                    lastLastIndex = 0,
                    flags         = "",
                    separator     = RegExp(separator.source, "g"), 
                    separator2, match, lastIndex, lastLength;

                str = str + "";
                
                if(!cbSplit._compliantExecNpcg) {
                        separator2 = RegExp("^" + separator.source + "$(?!\\s)", flags);
                };

                /* behavior for `limit`: if it's...
                - `undefined`: no limit.
                - `NaN` or zero: return an empty array.
                - a positive number: use `Math.floor(limit)`.
                - a negative number: no limit.
                - other: type-convert, then use the above rules. */
                if(limit === undefined || +limit < 0) {
                        limit = Infinity;
                } else {
                        limit = Math.floor(+limit);
                        if(!limit) {
                                return [];
                        };
                };

                while(match = separator.exec(str)) {
                        lastIndex = match.index + match[0].length; // `separator.lastIndex` is not reliable cross-browser
                        
                        if (lastIndex > lastLastIndex) {
                                output.push(str.slice(lastLastIndex, match.index));
                                
                                // fix browsers whose `exec` methods don't consistently return `undefined` for nonparticipating capturing groups
                                if(!cbSplit._compliantExecNpcg && match.length > 1) {
                                        match[0].replace(separator2, function () {
                                                for (var i = 1; i < arguments.length - 2; i++) {
                                                        if(arguments[i] === undefined) {
                                                                match[i] = undefined;
                                                        };
                                                };
                                        });
                                };
                                
                                if(match.length > 1 && match.index < str.length) {
                                        Array.prototype.push.apply(output, match.slice(1));
                                };
                                
                                lastLength = match[0].length;
                                lastLastIndex = lastIndex;
                                
                                if(output.length >= limit) {
                                        break;
                                };
                        };
                        
                        if(separator.lastIndex === match.index) {
                                // avoid an infinite loop
                                separator.lastIndex++; 
                        };
                };
                
                if(lastLastIndex === str.length) {
                        if (lastLength || !separator.test("")) {
                                output.push("");
                        };
                } else {
                        output.push(str.slice(lastLastIndex));
                };
                
                return output.length > limit ? output.slice(0, limit) : output;
        };
        // NPCG: nonparticipating capturing group
        cbSplit._compliantExecNpcg = /()??/.exec("")[1] === undefined; 
        cbSplit._nativeSplit = String.prototype.split;

        // Affects the JSON passed to the script
        function affectJSON(json) {
                if(!(typeof json === "object")) { 
                        return; 
                };
                
                var key,
                    switchObj = {
                        "debug":function(value) {
                                debug = !!value;
                                return true;
                        },
                        "lang":function(value) {
                                if(typeof value === "string" && value.search(/^[a-z]{2,3}(-([a-z]{2}))?$/i) != -1) {                                                
                                        languageInfo = [value.toLowerCase()];                                                   
                                        returnLocaleDate = true;
                                        deriveLocale = true;
                                };
                                return true;
                        },
                        "nodrag":function(value) {
                                nodrag = !!value;
                                return true;
                        },
                        "buttontabindex":function(value) {
                                buttonTabIndex = !!value;
                                return true;        
                        },
                        "derivelocale":function(value) {
                                deriveLocale = !!value;
                                return true;        
                        },
                        "mousewheel":function(value) {
                                mouseWheel = !!value;
                                return true;        
                        },
                        "cellformat":function(value) {
                                if(typeof value === "string") {
                                        parseCellFormat(value);
                                };
                                return true;
                        },
                        "titleformat":function(value) {
                                if(typeof value === "string") {
                                        titleFormat = value;
                                };
                                return true;
                        },
                        "statusformat":function(value) {
                                if(typeof value === "string") {
                                        statusFormat = value;
                                };
                                return true;
                        },
                        "describedby":function(value) {
                                if(typeof value === "string") {
                                        describedBy = value;
                                };
                                return true;
                        },
                        "finalopacity":function(value) {
                                if(typeof value === 'number' && (+value > 20 && +value <= 100)) {
                                        finalOpacity = parseInt(value, 10);
                                };
                                return true; 
                        },
                        "bespoketitles":function(value) {                               
                                if(typeof value === "object") {
                                        bespokeTitles = {};
                                        for(var dt in value) {
                                                if(value.hasOwnProperty(dt) && String(dt).match(wcDateRegExp) != -1) {
                                                        bespokeTitles[dt] = String(value[dt]);
                                                };
                                        };
                                };
                                return true;    
                        },
                        "_default":function() {
                                if(debug) {
                                        throw "Unknown key located within JSON data: " + key;
                                };
                                return true;
                        }
                };
                
                for(key in json) {
                        if(!json.hasOwnProperty(key)) {
                                continue;
                        };                                             
                        (switchObj.hasOwnProperty(String(key).toLowerCase()) && switchObj[key] || switchObj._default)(json[key]);
                };        
        };                  
        
        // Parses the cell format to use whenever the datepicker has keyboard focus
        function parseCellFormat(value) {                  
                if(isOpera) { 
                        // Don't use hidden text for opera due to the default 
                        // browser focus outline stretching outside of the viewport              
                        formatParts = ["%j"];
                        cellFormat  = "%j %F %Y";  
                        return;
                };   
                
                // If no day part stipulated then use presets
                if(value.match(/%([d|j])/) == -1) {
                        return;
                };
                
                // Basic split on the %j or %d modifiers
                formatParts = cbSplit(value, /%([d|j])/);
                cellFormat  = value;               
        };
        
        // Pads a number to "length" 
        function pad(value, length) { 
                length = Math.min(4, length || 2); 
                return "0000".substr(0,length - Math.min(String(value).length, length)) + value; 
        };
        
        // Basic event functions
        function addEvent(obj, type, fn) { 
                try {                 
                        if(obj.attachEvent) {
                                obj.attachEvent("on"+type, fn);
                        } else {
                                obj.addEventListener(type, fn, true);
                        };
                } catch(err) {};
        };
        
        function removeEvent(obj, type, fn) {
                try {
                        if(obj.detachEvent) {
                                obj.detachEvent("on"+type, fn);
                        } else {
                                obj.removeEventListener(type, fn, true);
                        };
                } catch(err) {};
        };   

        function stopEvent(e) {
                e = e || document.parentWindow.event;
                if(e.stopPropagation) {
                        e.stopPropagation();
                        e.preventDefault();
                };
                /*@cc_on
                @if(@_win32)
                e.cancelBubble = true;
                e.returnValue = false;
                @end
                @*/
                return false;
        };
        
        // Parses the JSON passed between the script tags or by using the setGlobalOptions method
        function parseJSON(str) {
                // Check we have a String
                if(!(typeof str === 'string') || str == "") { 
                        return {}; 
                };                 
                try {
                        // Does a JSON (native or not) Object exist                              
                        if(typeof JSON === "object" && JSON.parse) {                                              
                                return window.JSON.parse(str);  
                        // Genious code taken from: http://kentbrewster.com/badges/                                                      
                        } else if(/lang|buttontabindex|mousewheel|cellformat|titleformat|nodrag|describedby/.test(str.toLowerCase())) {                                               
                                var f = Function(['var document,top,self,window,parent,Number,Date,Object,Function,',
                                        'Array,String,Math,RegExp,Image,ActiveXObject;',
                                        'return (' , str.replace(/<\!--.+-->/gim,'').replace(/\bfunction\b/g,'function-') , ');'].join(''));
                                return f();                          
                        };
                } catch (e) { };
                
                if(debug) {
                        throw "Could not parse the JSON object";
                };
                
                return {"err":"Could not parse the JSON object"};                                            
        };        

        // Sets an ARIA role on an element
        function setARIARole(element, role) {
                if(element && element.tagName) {
                        element.setAttribute("role", role);
                };
        };
        
        // Sets an ARIA property on an element        
        function setARIAProperty(element, property, value) {
		if(element && element.tagName) {
                        element.setAttribute("aria-" + property, value);
                };	
	};

        // Sets a tabindex attribute on an element, bends over for IE.
        function setTabIndex(e, i) {
                e.setAttribute(!/*@cc_on!@*/false ? "tabIndex" : "tabindex", i);
                e.tabIndex = i;                        
        };
        
        function dateToYYYYMMDD(dt) {                
                return dt instanceof Date && !isNaN(dt) ? dt.getFullYear() + pad(dt.getMonth() + 1) + "" + pad(dt.getDate()) : dt;                
        };
        
        // The datePicker object itself 
        function datePicker(options) {                                      
                this.dateSet             = null;                 
                this.timerSet            = false;
                this.visible             = false;
                this.fadeTimer           = null;
                this.timer               = null;
                this.yearInc             = 0;
                this.monthInc            = 0;
                this.dayInc              = 0;
                this.mx                  = 0;
                this.my                  = 0;
                this.x                   = 0;
                this.y                   = 0; 
                this.created             = false;
                this.disabled            = false;
                this.opacity             = 0; 
                this.opacityTo           = 99;
                this.inUpdate            = false;                              
                this.kbEventsAdded       = false;
                this.fullCreate          = false;
                this.selectedTD          = null;
                this.cursorTD            = null;
                this.cursorDate          = options.cursorDate ? options.cursorDate : "",       
                this.date                = options.cursorDate ? new Date(+options.cursorDate.substr(0,4), +options.cursorDate.substr(4,2) - 1, +options.cursorDate.substr(6,2)) : new Date();
                this.defaults            = {};
                this.dynDisabledDates    = {};
                this.dateList            = [];
                this.bespokeClass        = options.bespokeClass;
                this.firstDayOfWeek      = localeImport.firstDayOfWeek; 
                this.interval            = new Date();
                this.clickActivated      = false;
                this.noFocus             = true;
                this.kbEvent             = false; 
                this.delayedUpdate       = false;  
                this.bespokeTitles       = {};             
                
                for(var thing in options) {
                        if(options.hasOwnProperty(thing) && String(thing).search(/callbacks|formElements|enabledDates|disabledDates/) != -1) { 
                                continue; 
                        };
                        this[thing] = options[thing];                 
                };                
                
                /*@cc_on
                @if(@_win32)                   
                this.iePopUp             = null;
                this.isIE7               = false;                 
                @end
                @*/
                
                /*@cc_on
                @if(@_jscript_version <= 5.7)
                this.isIE7               = document.documentElement && typeof document.documentElement.style.maxHeight != "undefined";
                @end
                @*/
                
                for(var i = 0, prop; prop = ["callbacks", "formElements"][i]; i++) { 
                        this[prop] = {};
                        if(options.hasOwnProperty(prop)) {                        
                                for(thing in options[prop]) {                                
                                        if(options[prop].hasOwnProperty(thing)) {
                                                this[prop][thing] = options[prop][thing];
                                        };                 
                                };
                        };
                };
                
                // Adjust time to stop daylight savings madness on windows
                this.date.setHours(5);

                // Called from an associated form elements onchange event
                this.changeHandler = function() {
                        if(o.disabled) {
                                return;
                        };                        
                        o.setDateFromInput();  
                        o.callback("dateset", o.createCbArgObj());                                                                                 
                };
                
                // Creates the object passed to the callback functions
                this.createCbArgObj = function() {                        
                        return this.dateSet ? {"id":this.id,"date":this.dateSet,"dd":pad(this.date.getDate()),"mm":pad(this.date.getMonth() + 1),"yyyy":this.date.getFullYear()} : {"id":this.id,"date":null,"dd":null,"mm":null,"yyyy":null};                         
                };
                
                // Attempts to grab the window scroll offsets
                this.getScrollOffsets = function() {                         
                        if(typeof(window.pageYOffset) == 'number') {
                                //Netscape compliant
                                return [window.pageXOffset, window.pageYOffset];                                
                        } else if(document.body && (document.body.scrollLeft || document.body.scrollTop)) {
                                //DOM compliant
                                return [document.body.scrollLeft, document.body.scrollTop];                                
                        } else if(document.documentElement && (document.documentElement.scrollLeft || document.documentElement.scrollTop)) {
                                //IE6 standards compliant mode
                                return [document.documentElement.scrollLeft, document.documentElement.scrollTop];
                        };
                        return [0,0];
                };
                
                // Calculates the current list of disabled & enabled dates for a specific year/month
                this.getDateExceptions = function(y, m) {
                
                        m = pad(m);                 
                        
                        var obj     = {},            
                            lower   = o.firstDateShown,
                            upper   = o.lastDateShown,
                            rLength = o.dateList.length,                            
                            rNumber, workingDt, workingY, workingM, dtLower, dtUpper, i, dt, dt1, dt2, rngLower, rngUpper, cDate;  
                        
                        if(!upper || !lower) {
                                lower = o.firstDateShown = y + pad(m) + "01";
                                upper = o.lastDateShown  = y + pad(m) + pad(daysInMonth(m, y));                        
                        };
                        
                        dtLower = Number(lower.substr(0,6));
                        dtUpper = Number(upper.substr(0,6));
                                        
                        workingDt = String(dtLower);
                        
                        while(+workingDt <= dtUpper) {
                                workingY = workingDt.substr(0,4);
                                workingM = workingDt.substr(4,2);
                                
                                for(rNumber = 0; rNumber < rLength; rNumber++) {                         
                                        dt1 = String(o.dateList[rNumber].rLow).replace(/^(\*\*\*\*)/, workingY).replace(/^(\d\d\d\d)(\*\*)/, "$1"+workingM);
                                        dt2 = String(o.dateList[rNumber].rHigh).replace(/^(\*\*\*\*)/, workingY).replace(/^(\d\d\d\d)(\*\*)/, "$1"+workingM);
                                        
                                        // Single date
                                        if(dt2 == 1) { 
                                                if(+dt1 >= +o.firstDateShown && +dt1 <= +o.lastDateShown) {                                               
                                                        obj[dt1] = o.dateList[rNumber].type;                                                              
                                                };
                                                continue; 
                                        };                       
                                        
                                        // Range
                                        if(+dt1 <= +dt2
                                           &&
                                           +workingDt >= dt1.substr(0,6)
                                           &&
                                           +workingDt <= dt2.substr(0,6)
                                           ) {
                                                rngLower = Math.max(dt1,Math.max(String(workingDt) + "01", this.firstDateShown));
                                                rngUpper = Math.min(dt2,Math.min(String(workingDt) + "31", this.lastDateShown));
                                                for(var i = rngLower; i <= rngUpper; i++) {
                                                        obj[i] = o.dateList[rNumber].type;                                                                                                
                                                };
                                        };
                                };
                                
                                workingDt = new Date(workingY, +workingM, 02);
                                workingDt = workingDt.getFullYear()+""+pad(workingDt.getMonth()+1);
                        };        
                        
                        return obj;
                };

                // Repositions the datepicker beside the button
                this.reposition = function() {
                        if(!o.created || o.staticPos) { 
                                return; 
                        };

                        o.div.style.visibility = "hidden";
                        o.div.style.left = o.div.style.top = "0px";                           
                        o.div.style.display = "block";

                        var osh         = o.div.offsetHeight,
                            osw         = o.div.offsetWidth,
                            elem        = document.getElementById('fd-but-' + o.id),
                            pos         = o.truePosition(elem),
                            trueBody    = (document.compatMode && document.compatMode!="BackCompat") ? document.documentElement : document.body,
                            sOffsets    = o.getScrollOffsets(),
                            scrollTop   = sOffsets[1], 
                            scrollLeft  = sOffsets[0],
                            tSpace      = parseInt(pos[1] - 2) - parseInt(scrollTop),
                            bSpace      = parseInt(trueBody.clientHeight + scrollTop) - parseInt(pos[1] + elem.offsetHeight + 2); 
                       
                        o.div.style.visibility = "visible";

                        o.div.style.left = Number(parseInt(trueBody.clientWidth+scrollLeft) < parseInt(osw+pos[0]) ? Math.abs(parseInt((trueBody.clientWidth+scrollLeft) - osw)) : pos[0]) + "px";
                        //o.div.style.top  = (fitsBottom || !fitsTop) ? Math.abs(parseInt(pos[1] + elem.offsetHeight + 2)) + "px" : Math.abs(parseInt(pos[1] - (osh + 2))) + "px";
                        o.div.style.top  = (bSpace > tSpace) ? Math.abs(parseInt(pos[1] + elem.offsetHeight + 2)) + "px" : Math.abs(parseInt(pos[1] - (osh + 2))) + "px";
                        /*@cc_on
                        @if(@_jscript_version <= 5.7)
                        if(o.isIE7) return;
                        o.iePopUp.style.top    = o.div.style.top;
                        o.iePopUp.style.left   = o.div.style.left;
                        o.iePopUp.style.width  = osw + "px";
                        o.iePopUp.style.height = (osh - 2) + "px";
                        @end
                        @*/
                };
                
                // Resets the tabindex of the previously focused cell
                this.removeOldFocus = function() {
                        var td = document.getElementById(o.id + "-date-picker-hover");
                        if(td) {                                        
                                try {                                        
                                        setTabIndex(td, -1);                                          
                                        removeClass(td, "date-picker-hover");
                                        td.id = ""; 
                                        td.onblur  = null; 
                                        td.onfocus = null;                                                                             
                                } catch(err) {};
                        };
                };
                
                // Sets the tabindex & focus on the currently highlighted cell
                this.setNewFocus = function() {                                                                                             
                        var td = document.getElementById(o.id + "-date-picker-hover");
                        if(td) {
                                try {                                             
                                        setTabIndex(td, 0);                                                                                                                   
                                        addClass(td, "date-picker-hover");
                                        // If opened with the keyboard then add focus & blur events to the cell
                                        if(!this.clickActivated) {                                                
                                                td.onblur    = o.onblur;  
                                                td.onfocus   = o.onfocus;                                   
                                        };
                                        
                                        // If opened with the keyboard (and not in opera) then add a screen-reader friendly date format                                                                                                                                                                                         
                                        if(!isOpera && !this.clickActivated) {  
                                                o.addAccessibleDate();
                                        };
                                        
                                        // Try to programmatically set focus on the cell
                                        if(!this.noFocus && !this.clickActivated) {                                                                                                                                                   
                                                setTimeout(function() { try { td.focus(); } catch(err) {}; }, 0);
                                        };                                         
                                } catch(err) { };
                        };
                };
                
                // Adds a screen-reader friendly date to the current cell whenever 
                // the datepicker has been opened with the keyboard 
                this.addAccessibleDate = function() {
                        var td   = document.getElementById(o.id + "-date-picker-hover");                            
                                
                        if(td && !(td.getElementsByTagName("span").length)) {                                                          
                                var ymd = td.className.match(/cd-([\d]{4})([\d]{2})([\d]{2})/),
                                    noS = td.className.search(noSelectionRegExp) != -1,
                                    spn = document.createElement('span'),
                                    spnC;                                        
                        
                                spn.className       = "fd-screen-reader";
                                
                                while(td.firstChild) {
                                        td.removeChild(td.firstChild);
                                };
                                
                                if(noS) {
                                        spnC = spn.cloneNode(false);
                                        spnC.appendChild(document.createTextNode(getTitleTranslation(13)));
                                        td.appendChild(spnC);
                                };
                                
                                for(var pt = 0, part; part = formatParts[pt]; pt++) {
                                        
                                        if(part == "%j" || part == "%d") {
                                                td.appendChild(document.createTextNode(printFormattedDate(new Date(ymd[1], +ymd[2]-1, ymd[3]), part, true)));
                                        } else {
                                                spnC = spn.cloneNode(false);
                                                spnC.appendChild(document.createTextNode(printFormattedDate(new Date(ymd[1], +ymd[2]-1, ymd[3]), part, true)));
                                                td.appendChild(spnC);
                                        };                                                
                                };
                        };
                };
                
                // Sets the current cursor to a specific date
                this.setCursorDate = function(yyyymmdd) {                        
                        if(String(yyyymmdd).search(/^([0-9]{8})$/) != -1) {
                                this.date = new Date(+yyyymmdd.substr(0,4), +yyyymmdd.substr(4,2) - 1, +yyyymmdd.substr(6,2));
                                this.cursorDate = yyyymmdd;
                                
                                if(this.staticPos) {                                         
                                        this.updateTable();
                                };                                                                                                  
                        };
                };
                
                // Updates the table used to display the datepicker                  
                this.updateTable = function(noCallback) {  
                        if(!o || o.inUpdate || !o.created) {
                                return;
                        };
                        
                        // We are currently updating (used to stop public methods from firing)
                        o.inUpdate = true;
                        
                        // Remove the focus from the currently highlighted cell                                         
                        o.removeOldFocus();
                        
                        o.div.dir = localeImport.rtl ? "rtl" : "ltr"; 
                        
                        // If the update timer initiated
                        if(o.timerSet && !o.delayedUpdate) {
                                // Are we incrementing/decrementing the month
                                if(o.monthInc) {
                                        var n = o.date.getDate(),
                                            d = new Date(o.date);                         
                       
                                        d.setDate(2);                                               
                                        d.setMonth(d.getMonth() + o.monthInc * 1);
                                        // Don't go over the days in the month
                                        d.setDate(Math.min(n, daysInMonth(d.getMonth(),d.getFullYear())));
                                        
                                        o.date = new Date(d);
                                } else {                                 
                                        o.date.setDate(Math.min(o.date.getDate()+o.dayInc, daysInMonth(o.date.getMonth()+o.monthInc,o.date.getFullYear()+o.yearInc)));
                                        o.date.setMonth(o.date.getMonth() + o.monthInc);                                        
                                        o.date.setFullYear(o.date.getFullYear() + o.yearInc);
                                };                                       
                        }; 
        
                        // Make sure the internal date is within range
                        o.outOfRange();
                        
                        // Disable/enable the today button
                        if(!o.noToday) { 
                                o.disableTodayButton(); 
                        };
                        
                        // Disable/enable the month & year buttons
                        o.showHideButtons(o.date);
                
                        var cd = o.date.getDate(),
                            cm = o.date.getMonth(),
                            cy = o.date.getFullYear(),
                            cursorDate = (String(cy) + pad(cm+1) + pad(cd)),
                            tmpDate    = new Date(cy, cm, 1);                      
                        
                        tmpDate.setHours(5);
                        
                        var dt, cName, td, i, currentDate, cellAdded, col, currentStub, abbr, bespokeRenderClass, spnC, dateSetD,
                            // Weekday of the fist of the month
                            weekDayC            = (tmpDate.getDay() + 6) % 7,
                            // The column index this weekday will occupy                
                            firstColIndex       = (((weekDayC - o.firstDayOfWeek) + 7 ) % 7) - 1,
                            // The number of days in the current month
                            dpm                 = daysInMonth(cm, cy),
                            // Today as a Date Object
                            today               = new Date(),
                            // Today as a YYYYMMDD String
                            today               = today.getFullYear() + pad(today.getMonth()+1) + pad(today.getDate()),
                            // A Sring date stub in a YYYYMM format for the current date                        
                            stub                = String(tmpDate.getFullYear()) + pad(tmpDate.getMonth()+1),
                            //
                            cellAdded           = [4,4,4,4,4,4],
                            // The first day of the previous month as a Date Object                                                                    
                            lm                  = new Date(cy, cm-1, 1),
                            // The first day of the next month as a Date Object
                            nm                  = new Date(cy, cm+1, 1),
                            // The number of days in the previous month                          
                            daySub              = daysInMonth(lm.getMonth(), lm.getFullYear()),
                            // YYYYMM String date stub for the next month                
                            stubN               = String(nm.getFullYear()) + pad(nm.getMonth()+1),
                            // YYYYMM String date stub for the previous month
                            stubP               = String(lm.getFullYear()) + pad(lm.getMonth()+1),                
                            weekDayN            = (nm.getDay() + 6) % 7,
                            weekDayP            = (lm.getDay() + 6) % 7,
                            // A SPAN node to clone when adding dates to individual cells                                       
                            spn                 = document.createElement('span');                        
                        
                        // Give the "fd-screen-reader" class to the span in order to hide them in the UI
                        // but keep them accessible to screen-readers
                        spn.className       = "fd-screen-reader";
                        
                        // The first & last dates shown on the datepicker UI - could be a date from the previous & next month respectively
                        o.firstDateShown    = !o.constrainSelection && o.fillGrid && (0 - firstColIndex < 1) ? String(stubP) + (daySub + (0 - firstColIndex)) : stub + "01";            
                        o.lastDateShown     = !o.constrainSelection && o.fillGrid ? stubN + pad(41 - firstColIndex - dpm) : stub + String(dpm);
                        
                        // Store a reference to the current YYYYMM String representation of the current month
                        o.currentYYYYMM     = stub;                    
                
                        bespokeRenderClass  = o.callback("redraw", {id:o.id, dd:pad(cd), mm:pad(cm+1), yyyy:cy, firstDateDisplayed:o.firstDateShown, lastDateDisplayed:o.lastDateShown}) || {};                                            
                        
                        // An Object of dates that have been explicitly disabled (1) or enabled (0)
                        dts                 = o.getDateExceptions(cy, cm+1);                               
                
                        // Double check current date within limits etc
                        o.checkSelectedDate();
                        
                        // 
                        dateSetD            = (o.dateSet != null) ? o.dateSet.getFullYear() + pad(o.dateSet.getMonth()+1) + pad(o.dateSet.getDate()) : false;
                        
                        // If we have selected a date then set its ARIA selected property
                        // to false. We then set the ARIA selected property to true on the 
                        // newly selected cell after redrawing the table
                        if(this.selectedTD != null) {
                                setARIAProperty(this.selectedTD, "selected", false);
                                this.selectedTD = null;
                        };
                        
                        // Redraw all of the table cells representing the date parts of the UI
                        for(var curr = 0; curr < 42; curr++) {
                                // Current row
                                row  = Math.floor(curr / 7);
                                // Current TD node                         
                                td   = o.tds[curr];
                                // Clone our SPAN node
                                spnC = spn.cloneNode(false); 
                                // Remove any previous contents from the cell
                                while(td.firstChild) {
                                        td.removeChild(td.firstChild);
                                };
                                // If the current cell contains a date       
                                if((curr > firstColIndex && curr <= (firstColIndex + dpm)) || o.fillGrid) {
                                        currentStub     = stub;
                                        weekDay         = weekDayC;                                
                                        dt              = curr - firstColIndex;
                                        cName           = [];                                         
                                        selectable      = true;                                     
                                        
                                        // Are we drawing last month
                                        if(dt < 1) {
                                                dt              = daySub + dt;
                                                currentStub     = stubP;
                                                weekDay         = weekDayP;                                        
                                                selectable      = !o.constrainSelection;
                                                cName.push("month-out");
                                        // Are we drawing next month                                                  
                                        } else if(dt > dpm) {
                                                dt -= dpm;
                                                currentStub     = stubN;
                                                weekDay         = weekDayN;                                        
                                                selectable      = !o.constrainSelection; 
                                                cName.push("month-out");                                                                                           
                                        }; 
                                        
                                        // Calcuate this cells weekday
                                        weekDay = (weekDay + dt + 6) % 7;
                                        
                                        // Push a classname representing the weekday e.g. "day-3"
                                        cName.push("day-" + weekDay + " cell-" + curr);
                                        
                                        // A YYYYMMDD String representation of this cells date
                                        currentDate = currentStub + String(dt < 10 ? "0" : "") + dt;                            
                                        
                                        // If this cells date is out of range
                                        if(o.rangeLow && +currentDate < +o.rangeLow || o.rangeHigh && +currentDate > +o.rangeHigh) {                                          
                                                // Add a classname to style the cell and stop selection
                                                td.className = "out-of-range";
                                                // Reset this TD nodes title attribute  
                                                td.title = "";
                                                // Append the cells date as a text node to the TD 
                                                td.appendChild(document.createTextNode(dt));
                                                // Jaysus, what the feck does this line do again...                                             
                                                if(o.showWeeks) { 
                                                        cellAdded[row] = Math.min(cellAdded[row], 2); 
                                                };                                                                                                                                               
                                        // This cells date is within the lower & upper ranges (or no ranges have been defined)
                                        } else {
                                                // If it's a date from last or next month and the "constrainSelection" option
                                                // is false then give the cell a CD-YYYYMMDD class  
                                                if(selectable) {                                                                                                        
                                                        td.title = titleFormat ? printFormattedDate(new Date(+String(currentStub).substr(0,4), +String(currentStub).substr(4, 2) - 1, +dt), titleFormat, true) : "";                                                                                                      
                                                        cName.push("cd-" + currentDate + " yyyymm-" + currentStub + " mmdd-" + currentStub.substr(4,2) + pad(dt));
                                                // Otherwise give a "not-selectable" class (which shouldn't be styled in any way, it's for internal use)
                                                } else {  
                                                        td.title = titleFormat ? getTitleTranslation(13) + " " + printFormattedDate(new Date(+String(currentStub).substr(0,4), +String(currentStub).substr(4, 2) - 1, +dt), titleFormat, true) : "";                                                                       
                                                        cName.push("yyyymm-" + currentStub + " mmdd-" + currentStub.substr(4,2) + pad(dt) + " not-selectable");
                                                };                                                                                                                                             
                                                
                                                // Add a classname if the current cells date is today
                                                if(currentDate == today) { 
                                                        cName.push("date-picker-today"); 
                                                };

                                                // If this cell represents the currently selected date 
                                                if(dateSetD == currentDate) {
                                                        // Add a classname (for styling purposes) 
                                                        cName.push("date-picker-selected-date");
                                                        // Set the ARIA selected property to true 
                                                        setARIAProperty(td, "selected", "true");
                                                        // And cache a reference to the current cell
                                                        this.selectedTD = td;
                                                };

                                                // If the current cell has been explicitly disabled
                                                if(((currentDate in dts) && dts[currentDate] == 1)
                                                   // or
                                                   ||
                                                   // ... the current weekday has been disabled
                                                   (o.disabledDays[weekDay]
                                                    &&
                                                   // ... and the current date has not been explicitly enabled
                                                   !((currentDate in dts) && dts[currentDate] == 0) 
                                                   )
                                                  ) {
                                                        // Add a classname to style the cell and stop selection
                                                        cName.push("day-disabled");
                                                        // Update the current cells title to say "Disabled date: ..." (or whatever the translation says) 
                                                        if(titleFormat && selectable) { td.title = getTitleTranslation(13) + " " + td.title; }; 
                                                };
                                                
                                                // Has the redraw callback given us a bespoke classname to add to this cell
                                                if(currentDate in bespokeRenderClass) { 
                                                        cName.push(bespokeRenderClass[currentDate]); 
                                                }
                                        
                                                // Do we need to highlight this cells weekday representation
                                                if(o.highlightDays[weekDay]) { 
                                                        cName.push("date-picker-highlight"); 
                                                };

                                                // Is the current onscreen cursor set to this cells date
                                                if(cursorDate == currentDate) { 
                                                        td.id = o.id + "-date-picker-hover";                                                                                                                                                                 
                                                };      
                                                
                                                // Add the date to the TD cell as a text node. Note: If the datepicker has been given keyboard
                                                // events, this textnode is replaced by a more screen-reader friendly date during the focus event                                  
                                                td.appendChild(document.createTextNode(dt));
                                                
                                                // Add the classnames to the TD node
                                                td.className = cName.join(" ");
                                               
                                                // If the UI displays week numbers then update the celladded 
                                                if(o.showWeeks) {                                                         
                                                        cellAdded[row] = Math.min(cName[0] == "month-out" ? 3 : 1, cellAdded[row]);                                                          
                                                }; 
                                        };
                                // The current TD node is empty i.e. represents no date in the UI                       
                                } else {
                                        // Add a classname to style the cell
                                        td.className = "date-picker-unused";
                                        // Add a non-breaking space to unused TD node (for IEs benefit mostly)                                                                                                                    
                                        td.appendChild(document.createTextNode(nbsp));
                                        // Reset the TD nodes title attribute
                                        td.title = "";                                                                              
                                };                                                  
                                
                                // Do we update the week number for this row
                                if(o.showWeeks && curr - (row * 7) == 6) { 
                                        while(o.wkThs[row].firstChild) {
                                                o.wkThs[row].removeChild(o.wkThs[row].firstChild);
                                        };                                         
                                        o.wkThs[row].appendChild(document.createTextNode(cellAdded[row] == 4 && !o.fillGrid ? nbsp : getWeekNumber(cy, cm, curr - firstColIndex - 6)));
                                        o.wkThs[row].className = "date-picker-week-header" + (["",""," out-of-range"," month-out",""][cellAdded[row]]);                                          
                                };                                
                        };            
                        
                        // Update the UI title bar displaying the year & month
                        var span = o.titleBar.getElementsByTagName("span");
                        while(span[0].firstChild) {
                                span[0].removeChild(span[0].firstChild);
                        };
                        while(span[1].firstChild) {
                                span[1].removeChild(span[1].firstChild);
                        };
                        span[0].appendChild(document.createTextNode(getMonthTranslation(cm, false) + nbsp));
                        span[1].appendChild(document.createTextNode(cy));
                        
                        // If we are in an animation 
                        if(o.timerSet) {
                                // Speed the timer up a little bit to make the pause between updates quicker
                                o.timerInc = 50 + Math.round(((o.timerInc - 50) / 1.8));
                                // Recall this function in a timeout
                                o.timer = window.setTimeout(o.updateTable, o.timerInc);
                        };
                        
                        // We are not currently updating the UI
                        o.inUpdate = o.delayedUpdate = false; 
                        // Focus on the correct TD node
                        o.setNewFocus();                         
                };
                
                // Removes all scaffold from the DOM & events from memory
                this.destroy = function() {
                        
                        // Remove the button if it exists
                        if(document.getElementById("fd-but-" + this.id)) {
                                document.getElementById("fd-but-" + this.id).parentNode.removeChild(document.getElementById("fd-but-" + this.id));        
                        };
                        
                        if(!this.created) { 
                                return; 
                        };
                        
                        // Event cleanup for Internet Explorers benefit
                        removeEvent(this.table, "mousedown", o.onmousedown);  
                        removeEvent(this.table, "mouseover", o.onmouseover);
                        removeEvent(this.table, "mouseout", o.onmouseout);
                        removeEvent(document, "mousedown", o.onmousedown);
                        removeEvent(document, "mouseup",   o.clearTimer);
                        
                        if (window.addEventListener && !window.devicePixelRatio) {
                                try {
                                        window.removeEventListener('DOMMouseScroll', this.onmousewheel, false);
                                } catch(err) {};                                 
                        } else {
                                removeEvent(document, "mousewheel", this.onmousewheel);
                                removeEvent(window,   "mousewheel", this.onmousewheel);
                        }; 
                        o.removeOnFocusEvents();
                        clearTimeout(o.fadeTimer);
                        clearTimeout(o.timer);

                        /*@cc_on
                        @if(@_jscript_version <= 5.7)                         
                        if(!o.staticPos && !o.isIE7) {
                                try {
                                        o.iePopUp.parentNode.removeChild(o.iePopUp);
                                        o.iePopUp = null;
                                } catch(err) {};
                        };
                        @end
                        @*/                         

                        if(this.div && this.div.parentNode) {
                                this.div.parentNode.removeChild(this.div);
                        };
                                                 
                        o = null;
                };
                this.resizeInlineDiv = function()  {                        
                        o.div.style.width = o.table.offsetWidth + "px";
                        o.div.style.height = o.table.offsetHeight + "px";
                };
                
                this.reset = function() {
                        var elemID, elem;
                        for(elemID in o.formElements) {
                                elem = document.getElementById(elemID); 
                                if(elem) {
                                        if(elem.tagName.toLowerCase() == "select") {
                                                elem.selectedIndex = o.formElements[elemID.defaultVal];
                                        } else {
                                                elem.value = o.formElements[elemID.defaultVal];
                                        };
                                };                                
                        };
                        o.changeHandler();                                             
                };
                
                // Creates the DOM scaffold
                this.create = function() {
                        
                        if(document.getElementById("fd-" + this.id)) {
                                return;
                        };
                        
                        this.noFocus = true; 
                        
                        function createTH(details) {
                                var th = document.createElement('th');
                                if(details.thClassName) {
                                        th.className = details.thClassName;
                                };
                                if(details.colspan) {
                                        /*@cc_on
                                        /*@if (@_win32)
                                        th.setAttribute('colSpan',details.colspan);
                                        @else @*/
                                        th.setAttribute('colspan',details.colspan);
                                        /*@end
                                        @*/
                                };
                                /*@cc_on
                                /*@if (@_win32)
                                th.unselectable = "on";
                                /*@end@*/
                                return th;
                        };
                        function createThAndButton(tr, obj) {
                                for(var i = 0, details; details = obj[i]; i++) {
                                        var th = createTH(details);
                                        tr.appendChild(th);
                                        var but = document.createElement('span');
                                        but.className = details.className;
                                        but.id = o.id + details.id;
                                        but.appendChild(document.createTextNode(details.text || o.nbsp));
                                        but.title = details.title || "";                                          
                                        /*@cc_on
                                        /*@if(@_win32)
                                        th.unselectable = but.unselectable = "on";
                                        /*@end@*/
                                        th.appendChild(but);
                                };
                        };  
                        
                        this.div                     = document.createElement('div');
                        this.div.id                  = "fd-" + this.id;
                        this.div.className           = "date-picker" + this.bespokeClass;  
                        
                        // Attempt to hide the div from screen readers during content creation
                        this.div.style.visibility = "hidden";
                        this.div.style.display = "none";
                                                               
                        // Set the ARIA describedby property if the required block available
                        if(this.describedBy && document.getElementById(this.describedBy)) {
                                setARIAProperty(this.div, "describedby", this.describedBy);
                        };
                        
                        // Set the ARIA labelled property if the required label available
                        if(this.labelledBy) {
                                setARIAProperty(this.div, "labelledby", this.labelledBy.id);
                        };
                              
                        var tr, row, col, tableHead, tableBody, tableFoot;

                        this.table             = document.createElement('table');
                        this.table.className   = "date-picker-table";                         
                        this.table.onmouseover = this.onmouseover;
                        this.table.onmouseout  = this.onmouseout;
                        this.table.onclick     = this.onclick;
                        
                        if(this.staticPos) {
                                this.table.onmousedown  = this.onmousedown;
                        };

                        this.div.appendChild(this.table);   
                        
                        var dragEnabledCN = !this.dragDisabled ? " drag-enabled" : "";
                                
                        if(!this.staticPos) {
                                this.div.style.visibility = "hidden";
                                this.div.className += dragEnabledCN;
                                document.getElementsByTagName('body')[0].appendChild(this.div);
                                                              
                                /*@cc_on
                                @if(@_jscript_version <= 5.7) 
                                
                                if(!this.isIE7) {                                         
                                        this.iePopUp = document.createElement('iframe');
                                        this.iePopUp.src = "javascript:'<html></html>';";
                                        this.iePopUp.setAttribute('className','iehack');
                                        // Remove iFrame from tabIndex                                        
			                this.iePopUp.setAttribute("tabIndex", -1);  			                
                                        // Hide it from ARIA aware technologies
			                setARIARole(this.iePopUp, "presentation");
                                        setARIAProperty(this.iePopUp, "hidden", "true"); 			                
                                        this.iePopUp.scrolling = "no";
                                        this.iePopUp.frameBorder = "0";
                                        this.iePopUp.name = this.iePopUp.id = this.id + "-iePopUpHack";
                                        document.body.appendChild(this.iePopUp);                                        
                                };
                                
                                @end
                                @*/
                                
                                // Aria "hidden" property for non active popup datepickers
                                setARIAProperty(this.div, "hidden", "true");
                        } else {
                                elem = document.getElementById(this.positioned ? this.positioned : this.id);
                                if(!elem) {
                                        this.div = null;
                                        if(debug) {
                                                throw this.positioned ? "Could not locate a datePickers associated parent element with an id:" + this.positioned : "Could not locate a datePickers associated input with an id:" + this.id;
                                        };
                                        return;
                                };

                                this.div.className += " static-datepicker";                          

                                if(this.positioned) {
                                        elem.appendChild(this.div);
                                } else {
                                        elem.parentNode.insertBefore(this.div, elem.nextSibling);
                                };
                                
                                if(this.hideInput) {
                                        for(var elemID in this.formElements) {
                                                elem = document.getElementById(elemID);
                                                if(elem) {
                                                        elem.className += " fd-hidden-input";
                                                };        
                                        };                                        
                                };                                                                  
                                                                          
                                setTimeout(this.resizeInlineDiv, 300);                               
                        };                          
                        
                        // ARIA Application role
                        setARIARole(this.div, "application");
                        setARIARole(this.table, "grid");
                       
                        if(this.statusFormat) {
                                tableFoot = document.createElement('tfoot');
                                this.table.appendChild(tableFoot);
                                tr = document.createElement('tr');
                                tr.className = "date-picker-tfoot";
                                tableFoot.appendChild(tr);                                
                                this.statusBar = createTH({thClassName:"date-picker-statusbar" + dragEnabledCN, colspan:this.showWeeks ? 8 : 7});
                                tr.appendChild(this.statusBar); 
                                this.updateStatus(); 
                        };

                        tableHead = document.createElement('thead');
                        tableHead.className = "date-picker-thead";
                        this.table.appendChild(tableHead);

                        tr  = document.createElement('tr');
                        setARIARole(tr, "presentation");
                        
                        tableHead.appendChild(tr);

                        // Title Bar
                        this.titleBar = createTH({thClassName:"date-picker-title" + dragEnabledCN, colspan:this.showWeeks ? 8 : 7});
                        
                        tr.appendChild(this.titleBar);
                        tr = null;

                        var span = document.createElement('span');
                        span.appendChild(document.createTextNode(nbsp));
                        span.className = "month-display" + dragEnabledCN; 
                        this.titleBar.appendChild(span);

                        span = document.createElement('span');
                        span.appendChild(document.createTextNode(nbsp));
                        span.className = "year-display" + dragEnabledCN; 
                        this.titleBar.appendChild(span);

                        span = null;

                        tr  = document.createElement('tr');
                        setARIARole(tr, "presentation");
                        tableHead.appendChild(tr);

                        createThAndButton(tr, [
                        {className:"prev-but prev-year",  id:"-prev-year-but", text:"\u00AB", title:getTitleTranslation(2) },
                        {className:"prev-but prev-month", id:"-prev-month-but", text:"\u2039", title:getTitleTranslation(0) },
                        {colspan:this.showWeeks ? 4 : 3, className:"today-but", id:"-today-but", text:getTitleTranslation(4)},
                        {className:"next-but next-month", id:"-next-month-but", text:"\u203A", title:getTitleTranslation(1)},
                        {className:"next-but next-year",  id:"-next-year-but", text:"\u00BB", title:getTitleTranslation(3) }
                        ]);

                        tableBody = document.createElement('tbody');
                        this.table.appendChild(tableBody);

                        var colspanTotal = this.showWeeks ? 8 : 7,
                            colOffset    = this.showWeeks ? 0 : -1,
                            but, abbr, formElemId, formElem;   
                
                        for(var rows = 0; rows < 7; rows++) {
                                row = document.createElement('tr');

                                if(rows != 0) {
                                        // ARIA Grid role
                                        setARIARole(row, "row");
                                        tableBody.appendChild(row);   
                                } else {
                                        tableHead.appendChild(row);
                                };

                                for(var cols = 0; cols < colspanTotal; cols++) {                                                                                
                                        if(rows === 0 || (this.showWeeks && cols === 0)) {
                                                col = document.createElement('th');                                                                                              
                                        } else {
                                                col = document.createElement('td');                                                                                           
                                                setARIAProperty(col, "describedby", this.id + "-col-" + cols + (this.showWeeks ? " " + this.id + "-row-" + rows : ""));
                                                setARIAProperty(col, "selected", "false");                                                 
                                        };
                                        
                                        /*@cc_on@*/
                                        /*@if(@_win32)
                                        col.unselectable = "on";
                                        /*@end@*/  
                                        
                                        row.appendChild(col);
                                        if((this.showWeeks && cols > 0 && rows > 0) || (!this.showWeeks && rows > 0)) {                                                
                                                setARIARole(col, "gridcell"); 
                                        } else {
                                                if(rows === 0 && cols > colOffset) {
                                                        col.className = "date-picker-day-header";
                                                        col.scope = "col";
                                                        setARIARole(col, "columnheader"); 
                                                        col.id = this.id + "-col-" + cols;                                          
                                                } else {
                                                        col.className = "date-picker-week-header";
                                                        col.scope = "row";
                                                        setARIARole(col, "rowheader");
                                                        col.id = this.id + "-row-" + rows;
                                                };
                                        };
                                };
                        };

                        col = row = null; 
                
                        this.ths = this.table.getElementsByTagName('thead')[0].getElementsByTagName('tr')[2].getElementsByTagName('th');
                        for (var y = 0; y < colspanTotal; y++) {
                                if(y == 0 && this.showWeeks) {
                                        this.ths[y].appendChild(document.createTextNode(getTitleTranslation(6)));
                                        this.ths[y].title = getTitleTranslation(8);
                                        continue;
                                };

                                if(y > (this.showWeeks ? 0 : -1)) {
                                        but = document.createElement("span");
                                        but.className = "fd-day-header";                                        
                                        /*@cc_on@*/
                                        /*@if(@_win32)
                                        but.unselectable = "on";
                                        /*@end@*/
                                        this.ths[y].appendChild(but);
                                };
                        };
                
                        but = null; 
                                        
                        this.trs             = this.table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');
                        this.tds             = this.table.getElementsByTagName('tbody')[0].getElementsByTagName('td');
                        this.butPrevYear     = document.getElementById(this.id + "-prev-year-but");
                        this.butPrevMonth    = document.getElementById(this.id + "-prev-month-but");
                        this.butToday        = document.getElementById(this.id + "-today-but");
                        this.butNextYear     = document.getElementById(this.id + "-next-year-but"); 
                        this.butNextMonth    = document.getElementById(this.id + "-next-month-but");
        
                        if(this.noToday) {
                                this.butToday.style.display = "none";        
                        };
                        
                        if(this.showWeeks) {
                                this.wkThs = this.table.getElementsByTagName('tbody')[0].getElementsByTagName('th');
                                this.div.className += " weeks-displayed";
                        };

                        tableBody = tableHead = tr = createThAndButton = createTH = null;

                        this.updateTableHeaders();
                        this.created = true;                                                                    
                        this.updateTable();                         
                        
                        if(this.staticPos) {                                 
                                this.visible = true;
                                this.opacity = this.opacityTo = this.finalOpacity;                                                                                              
                                this.div.style.visibility = "visible";                       
                                this.div.style.display = "block";
                                this.noFocus = true;                                                          
                                this.fade();
                        } else {                                     
                                this.reposition();
                                this.div.style.visibility = "visible";
                                this.fade();
                                this.noFocus = true;   
                        };   
                        
                        this.callback("domcreate", { "id":this.id });                                                   
                };                 
                this.fade = function() {
                        window.clearTimeout(o.fadeTimer);
                        o.fadeTimer = null;   
                        var diff = Math.round(o.opacity + ((o.opacityTo - o.opacity) / 4)); 
                        o.setOpacity(diff);  
                        if(Math.abs(o.opacityTo - diff) > 3 && !o.noFadeEffect) {                                 
                                o.fadeTimer = window.setTimeout(o.fade, 50);
                        } else {
                                o.setOpacity(o.opacityTo);
                                if(o.opacityTo == 0) {
                                        o.div.style.display    = "none";
                                        o.div.style.visibility = "hidden";
                                        setARIAProperty(o.div, "hidden", "true");
                                        o.visible = false;
                                } else {
                                        setARIAProperty(o.div, "hidden", "false");
                                        o.visible = true;                                        
                                };
                        };
                };                  
                this.trackDrag = function(e) {
                        e = e || window.event;
                        var diffx = (e.pageX?e.pageX:e.clientX?e.clientX:e.x) - o.mx;
                        var diffy = (e.pageY?e.pageY:e.clientY?e.clientY:e.Y) - o.my;
                        o.div.style.left = Math.round(o.x + diffx) > 0 ? Math.round(o.x + diffx) + 'px' : "0px";
                        o.div.style.top  = Math.round(o.y + diffy) > 0 ? Math.round(o.y + diffy) + 'px' : "0px";
                        /*@cc_on
                        @if(@_jscript_version <= 5.7)                         
                        if(o.staticPos || o.isIE7) {
                                return;
                        };
                        o.iePopUp.style.top    = o.div.style.top;
                        o.iePopUp.style.left   = o.div.style.left;
                        @end
                        @*/
                };
                this.stopDrag = function(e) {
                        var b = document.getElementsByTagName("body")[0];
                        removeClass(b, "fd-drag-active");
                        removeEvent(document,'mousemove',o.trackDrag, false);
                        removeEvent(document,'mouseup',o.stopDrag, false);
                        o.div.style.zIndex = 9999;
                }; 
                this.onmousedown = function(e) {
                        e = e || document.parentWindow.event;
                        var el     = e.target != null ? e.target : e.srcElement,
                            origEl = el,
                            hideDP = true,
                            reg    = new RegExp("^fd-(but-)?" + o.id + "$");
                        
                        o.mouseDownElem = null;
                       
                        // Are we within the wrapper div or the button    
                        while(el) {
                                if(el.id && el.id.length && el.id.search(reg) != -1) { 
                                        hideDP = false;
                                        break;
                                };
                                try { 
                                        el = el.parentNode; 
                                } catch(err) { 
                                        break; 
                                };
                        };
                        
                        // If not, then ...     
                        if(hideDP) {                                                        
                                hideAll();                                                            
                                return true;                                                                  
                        };
                        
                        if((o.div.className + origEl.className).search('fd-disabled') != -1) { 
                                return true; 
                        };                                                                                                            
                        
                        // We check the mousedown events on the buttons
                        if(origEl.id.search(new RegExp("^" + o.id + "(-prev-year-but|-prev-month-but|-next-month-but|-next-year-but)$")) != -1) {
                                
                                o.mouseDownElem = origEl;
                                
                                addEvent(document, "mouseup", o.clearTimer);
                                addEvent(origEl, "mouseout",  o.clearTimer); 
                                                                 
                                var incs = {
                                        "-prev-year-but":[0,-1,0],
                                        "-prev-month-but":[0,0,-1],
                                        "-next-year-but":[0,1,0],
                                        "-next-month-but":[0,0,1]
                                    },
                                    check = origEl.id.replace(o.id, ""),
                                    dateYYYYMM = Number(o.date.getFullYear() + pad(o.date.getMonth()+1));
                                
                                o.timerInc      = 800;
                                o.timerSet      = true;
                                o.dayInc        = incs[check][0];
                                o.yearInc       = incs[check][1];
                                o.monthInc      = incs[check][2]; 
                                o.accellerator  = 1;
                                
                                if(!(o.currentYYYYMM == dateYYYYMM)) {
                                        if((o.currentYYYYMM < dateYYYYMM && (o.yearInc == -1 || o.monthInc == -1)) || (o.currentYYYYMM > dateYYYYMM && (o.yearInc == 1 || o.monthInc == 1))) {
                                                o.delayedUpdate = false; 
                                                o.timerInc = 1200;                                                
                                        } else {
                                                o.delayedUpdate = true;
                                                o.timerInc = 800;                                                
                                        };  
                                };
                                
                                o.updateTable();    
                                
                                return stopEvent(e);
                                                            
                        } else if(el.className.search("drag-enabled") != -1) {                                  
                                o.mx = e.pageX ? e.pageX : e.clientX ? e.clientX : e.x;
                                o.my = e.pageY ? e.pageY : e.clientY ? e.clientY : e.Y;
                                o.x  = parseInt(o.div.style.left);
                                o.y  = parseInt(o.div.style.top);
                                addEvent(document,'mousemove',o.trackDrag, false);
                                addEvent(document,'mouseup',o.stopDrag, false);
                                addClass(document.getElementsByTagName("body")[0], "fd-drag-active");
                                o.div.style.zIndex = 10000;
                                
                                return stopEvent(e);
                        };
                        return true;                                                                      
                }; 
                this.onclick = function(e) {
                        if(o.opacity != o.opacityTo || o.disabled) {
                                return stopEvent(e);
                        };
                        
                        e = e || document.parentWindow.event;
                        var el = e.target != null ? e.target : e.srcElement;                         
                          
                        while(el.parentNode) {
                                // Are we within a valid i.e. clickable TD node  
                                if(el.tagName && el.tagName.toLowerCase() == "td") {   
                                                                        
                                        if(el.className.search(/cd-([0-9]{8})/) == -1 || el.className.search(noSelectionRegExp) != -1) {
                                                return stopEvent(e);
                                        };
                                        
                                        var cellDate = el.className.match(/cd-([0-9]{8})/)[1];                                                                                                                                                                           
                                        o.date       = new Date(cellDate.substr(0,4),cellDate.substr(4,2)-1,cellDate.substr(6,2));                                                                                
                                        o.dateSet    = new Date(o.date); 
                                        o.noFocus    = true;                                                                       
                                        o.callback("dateset", { "id":o.id, "date":o.dateSet, "dd":o.dateSet.getDate(), "mm":o.dateSet.getMonth() + 1, "yyyy":o.dateSet.getFullYear() });                                          
                                        o.returnFormattedDate();
                                        o.hide();                  
                                                
                                        o.stopTimer();
                                        
                                        break;   
                                // Today button pressed             
                                } else if(el.id && el.id == o.id + "-today-but") {                                 
                                        o.date = new Date(); 
                                        o.updateTable();
                                        o.stopTimer();
                                        break; 
                                // Day headers clicked, change the first day of the week      
                                } else if(el.className.search(/date-picker-day-header/) != -1) {
                                        var cnt = o.showWeeks ? -1 : 0,
                                        elem = el;
                                        
                                        while(elem.previousSibling) {
                                                elem = elem.previousSibling;
                                                if(elem.tagName && elem.tagName.toLowerCase() == "th") {
                                                        cnt++;
                                                };
                                        };
                                        
                                        o.firstDayOfWeek = (o.firstDayOfWeek + cnt) % 7;
                                        o.updateTableHeaders();
                                        break;     
                                };
                                try { 
                                        el = el.parentNode; 
                                } catch(err) { 
                                        break; 
                                };
                        };
                        
                        return stopEvent(e);                                                
                };
                
                this.show = function(autoFocus) {                         
                        if(this.staticPos) { 
                                return; 
                        };
                        
                        var elem, elemID;
                        for(elemID in this.formElements) {
                                elem = document.getElementById(this.id);
                                if(!elem || (elem && elem.disabled)) { 
                                        return; 
                                };   
                        };
                        
                        this.noFocus = true; 
                        
                        // If the datepicker doesn't exist in the dom  
                        if(!this.created || !document.getElementById('fd-' + this.id)) {                          
                                this.created    = false;
                                this.fullCreate = false;                                                                                             
                                this.create();                                 
                                this.fullCreate = true;                                                            
                        } else {                                                        
                                this.setDateFromInput();                                                               
                                this.reposition();                                 
                        };                      
                        
                        this.noFocus = !!!autoFocus;                          
                        
                        if(this.noFocus) { 
                                this.clickActivated = true;
                                addEvent(document, "mousedown", this.onmousedown); 
                                if(mouseWheel) {
                                        if (window.addEventListener && !window.devicePixelRatio) {
                                                window.addEventListener('DOMMouseScroll', this.onmousewheel, false);
                                        } else {
                                                addEvent(document, "mousewheel", this.onmousewheel);
                                                addEvent(window,   "mousewheel", this.onmousewheel);
                                        };
                                };     
                        } else {
                                this.clickActivated = false;
                        };    
                        
                        this.opacityTo = this.finalOpacity;
                        this.div.style.display = "block";                        
                                                        
                        /*@cc_on
                        @if(@_jscript_version <= 5.7)                          
                        if(!o.isIE7) {
                                this.iePopUp.style.width = this.div.offsetWidth + "px";
                                this.iePopUp.style.height = this.div.offsetHeight + "px";
                                this.iePopUp.style.display = "block";
                        };                                
                        @end
                        @*/                               
                        
                        this.setNewFocus(); 
                        this.fade();
                        var butt = document.getElementById('fd-but-' + this.id);
                        if(butt) { 
                                  addClass(butt, "date-picker-button-active");
                        };                                                
                };
                this.hide = function() {                        
                        if(!this.visible || !this.created || !document.getElementById('fd-' + this.id)) {
                                return;
                        };
                        
                        this.kbEvent = false;
                        
                        removeClass(o.div, "date-picker-focus");
                        
                        this.stopTimer();
                        this.removeOnFocusEvents();
                        this.clickActivated = false;
                        this.noFocus = true;
                        this.setNewFocus();
                        
                        if(this.staticPos) {                                                                 
                                return; 
                        };

                        // Update status bar                                
                        if(this.statusBar) { 
                                this.updateStatus(getTitleTranslation(9)); 
                        };    
                        
                        var butt = document.getElementById('fd-but-' + this.id);
                        if(butt) {
                                removeClass(butt, "date-picker-button-active");                                
                        };
                
                        removeEvent(document, "mousedown", this.onmousedown);
                        
                        if(mouseWheel) {
                                if (window.addEventListener && !window.devicePixelRatio) {
                                        try { 
                                                window.removeEventListener('DOMMouseScroll', this.onmousewheel, false);
                                        } catch(err) {};                                 
                                } else {
                                        removeEvent(document, "mousewheel", this.onmousewheel);
                                        removeEvent(window,   "mousewheel", this.onmousewheel);
                                }; 
                        };
                        
                        /*@cc_on
                        @if(@_jscript_version <= 5.7)
                        if(!this.isIE7) { this.iePopUp.style.display = "none"; };
                        @end
                        @*/

                        this.opacityTo = 0;
                        this.fade();                  
                };
                
                this.onblur = function(e) {                                                                                                  
                        o.hide();
                };
                // The current cursor cell gains focus
                this.onfocus = function(e) {                                               
                        o.noFocus = false;                        
                        addClass(o.div, "date-picker-focus"); 
                        if(o.statusBar) { 
                                o.updateStatus(printFormattedDate(o.date, o.statusFormat, true)); 
                        };                                                                                                     
                        o.addOnFocusEvents();                                                                        
                };   
                this.onmousewheel = function(e) {                        
                        e = e || document.parentWindow.event;
                        var delta = 0;
                        
                        if (e.wheelDelta) {
                                delta = e.wheelDelta/120;
                                if (isOpera && window.opera.version() < 9.2) {
                                        delta = -delta;
                                };
                        } else if(e.detail) {
                                delta = -e.detail/3;
                        };                          
                        
                        var n = o.date.getDate(),
                            d = new Date(o.date),
                            inc = delta > 0 ? 1 : -1;                         
                       
                        d.setDate(2);
                        d.setMonth(d.getMonth() + inc * 1);
                        d.setDate(Math.min(n, daysInMonth(d.getMonth(),d.getFullYear())));
                      
                        if(o.outOfRange(d)) { 
                                return stopEvent(e); 
                        };
                        
                        o.date = new Date(d);
                        
                        o.updateTable(); 
                        
                        if(o.statusBar) { 
                                o.updateStatus(printFormattedDate(o.date, o.statusFormat, true)); 
                        };
                        
                        return stopEvent(e);                                                       
                };                      
                this.onkeydown = function (e) {
                        o.stopTimer();
                        
                        if(!o.visible) {
                                return false;
                        };
                                
                        e = e || document.parentWindow.event;
                        
                        var kc = e.keyCode ? e.keyCode : e.charCode;
                                
                        if(kc == 13) {
                                // RETURN/ENTER: close & select the date
                                var td = document.getElementById(o.id + "-date-picker-hover");                                         
                                if(!td || td.className.search(/cd-([0-9]{8})/) == -1 || td.className.search(/out-of-range|day-disabled/) != -1) {
                                        return stopEvent(e);
                                };
                                o.dateSet = new Date(o.date);
                                o.callback("dateset", o.createCbArgObj()); 
                                o.returnFormattedDate();    
                                o.hide();
                                return stopEvent(e);
                        } else if(kc == 27) {
                                // ESC: close, no date selection 
                                if(!o.staticPos) {
                                        o.hide();
                                        return stopEvent(e);
                                };
                                return true;
                        } else if(kc == 32 || kc == 0) {
                                // SPACE: goto todays date 
                                o.date = new Date();
                                o.updateTable();
                                return stopEvent(e);
                        } else if(kc == 9) {
                                // TAB: close, no date selection & focus back to associated button - popup datepickers only                                      
                                if(!o.staticPos) {
                                        return stopEvent(e);
                                };
                                return true;                                
                        };    
                        // TODO - test the need for the IE specific stuff in IE9
                                 
                        // Internet Explorer fires the keydown event faster than the JavaScript engine can
                        // update the interface. The following attempts to fix this.
                                
                        /*@cc_on
                        @if(@_win32)                                 
                        if(new Date().getTime() - o.interval.getTime() < 50) { return stopEvent(e); }; 
                        o.interval = new Date();                                 
                        @end
                        @*/
                        
                        // A number key has been pressed so change the first day of the week
                        if((kc > 49 && kc < 56) || (kc > 97 && kc < 104)) {
                                if(kc > 96) {
                                        kc -= (96-48);
                                };
                                kc -= 49;
                                o.firstDayOfWeek = (o.firstDayOfWeek + kc) % 7;
                                o.updateTableHeaders();
                                return stopEvent(e);
                        };

                        // If outside any other tested keycodes then let the keystroke pass
                        if(kc < 33 || kc > 40) {
                                return true;
                        };

                        var d = new Date(o.date),                            
                            cursorYYYYMM = o.date.getFullYear() + pad(o.date.getMonth()+1), 
                            tmp;
                             
                        // HOME: Set date to first day of current month
                        if(kc == 36) {
                                d.setDate(1); 
                        // END: Set date to last day of current month                                 
                        } else if(kc == 35) {
                                d.setDate(daysInMonth(d.getMonth(),d.getFullYear())); 
                        // PAGE UP & DOWN                                   
                        } else if ( kc == 33 || kc == 34) {
                                var inc = (kc == 34) ? 1 : -1; 
                                
                                // CTRL + PAGE UP/DOWN: Moves to the same date in the previous/next year
                                if(e.ctrlKey) {                                                                                                               
                                        d.setFullYear(d.getFullYear() + inc * 1);
                                // PAGE UP/DOWN: Moves to the same date in the previous/next month                                            
                                } else {                                          
                                        var n = o.date.getDate();                         
                       
                                        d.setDate(2);
                                        d.setMonth(d.getMonth() + inc * 1);
                                        d.setDate(Math.min(n, daysInMonth(d.getMonth(),d.getFullYear())));                                         
                                };                                                                    
                        // LEFT ARROW                                    
                        } else if ( kc == 37 ) {                                         
                                d = new Date(o.date.getFullYear(), o.date.getMonth(), o.date.getDate() - 1);                                       
                        // RIGHT ARROW
                        } else if ( kc == 39 || kc == 34) {                                         
                                d = new Date(o.date.getFullYear(), o.date.getMonth(), o.date.getDate() + 1 ); 
                        // UP ARROW                                        
                        } else if ( kc == 38 ) {                                          
                                d = new Date(o.date.getFullYear(), o.date.getMonth(), o.date.getDate() - 7);  
                        // DOWN ARROW                                        
                        } else if ( kc == 40 ) {                                          
                                d = new Date(o.date.getFullYear(), o.date.getMonth(), o.date.getDate() + 7);                                         
                        };

                        // If the new date is out of range then disallow action
                        if(o.outOfRange(d)) { 
                                return stopEvent(e); 
                        };
                        
                        // Otherwise set the new cursor date
                        o.date = d;
                        
                        // Update the status bar if needs be
                        if(o.statusBar) { 
                                o.updateStatus(o.getBespokeTitle(o.date.getFullYear(),o.date.getMonth() + 1,o.date.getDate()) || printFormattedDate(o.date, o.statusFormat, true));                                
                        };                     
                        
                        // YYYYMMDD format String of the current cursor date
                        var t = String(o.date.getFullYear()) + pad(o.date.getMonth()+1) + pad(o.date.getDate());

                        // If we need to redraw the UI completely
                        if(e.ctrlKey || (kc == 33 || kc == 34) || t < o.firstDateShown || t > o.lastDateShown) {                                                                       
                                o.updateTable(); 
                                /*@cc_on
                                @if(@_win32)
                                o.interval = new Date();                        
                                @end
                                @*/
                        // Just highlight current cell                                                
                        } else { 
                                // Do we need to disable the today button for this date                                   
                                if(!o.noToday) { 
                                        o.disableTodayButton(); 
                                };
                                // Remove focus from the previous cell                                        
                                o.removeOldFocus();
                                // Show/hide the month & year buttons
                                o.showHideButtons(o.date);
                                
                                // Locate this TD             
                                for(var i = 0, td; td = o.tds[i]; i++) {                                                                                             
                                        if(td.className.search("cd-" + t) == -1) {                                                          
                                                continue;
                                        };                                                
                                       
                                        td.id = o.id + "-date-picker-hover";                                                
                                        o.setNewFocus();
                                        break;
                                };
                        };

                        return stopEvent(e);
                }; 
                this.onmouseout = function(e) {
                        e = e || document.parentWindow.event;
                        var p = e.toElement || e.relatedTarget;
                        while(p && p != this) {
                                try { 
                                        p = p.parentNode 
                                } catch(e) { 
                                        p = this; 
                                };
                        };
                        
                        if(p == this) {
                                return false;
                        };
                        
                        if(o.currentTR) {
                                o.currentTR.className = ""; 
                                o.currentTR = null;
                        };
                        
                        if(o.statusBar) { 
                                o.updateStatus(o.getBespokeTitle(o.date.getFullYear(),o.date.getMonth() + 1,o.date.getDate()) || printFormattedDate(o.date, o.statusFormat, true));                                
                        };                          
                };
                this.onmouseover = function(e) {
                        e = e || document.parentWindow.event;
                        var el = e.target != null ? e.target : e.srcElement;
                        while(el.nodeType != 1) { 
                                el = el.parentNode; 
                        }; 
                                
                        if(!el || ! el.tagName) { 
                                return; 
                        };                              
                        
                        o.noFocus = true;
                                
                        var statusText = getTitleTranslation(9);
                        
                        switch (el.tagName.toLowerCase()) {
                                case "td":                                            
                                        if(el.className.search(/date-picker-unused|out-of-range/) != -1) {
                                                statusText = getTitleTranslation(9);
                                        } if(el.className.search(/cd-([0-9]{8})/) != -1) {                                                                                               
                                                o.stopTimer();
                                                var cellDate = el.className.match(/cd-([0-9]{8})/)[1];                                                                                                                          
                                                
                                                o.removeOldFocus();
                                                el.id = o.id+"-date-picker-hover";
                                                o.setNewFocus();
                                                                                       
                                                o.date = new Date(+cellDate.substr(0,4),+cellDate.substr(4,2)-1,+cellDate.substr(6,2));                                                
                                                if(!o.noToday) { 
                                                        o.disableTodayButton(); 
                                                };
                                                
                                                statusText = o.getBespokeTitle(+cellDate.substr(0,4),+cellDate.substr(4,2),+cellDate.substr(6,2)) || printFormattedDate(o.date, o.statusFormat, true);                                                
                                        };
                                        break;
                                case "th":
                                        if(!o.statusBar) { 
                                                break; 
                                        };
                                        if(el.className.search(/drag-enabled/) != -1) {
                                                statusText = getTitleTranslation(10);
                                        } else if(el.className.search(/date-picker-week-header/) != -1) {
                                                var txt = el.firstChild ? el.firstChild.nodeValue : "";
                                                statusText = txt.search(/^(\d+)$/) != -1 ? getTitleTranslation(7, [txt, txt < 3 && o.date.getMonth() == 11 ? getWeeksInYear(o.date.getFullYear()) + 1 : getWeeksInYear(o.date.getFullYear())]) : getTitleTranslation(9);
                                        };
                                        break;
                                case "span":
                                        if(!o.statusBar) { 
                                                break; 
                                        };
                                        if(el.className.search(/drag-enabled/) != -1) {
                                                statusText = getTitleTranslation(10);
                                        } else if(el.className.search(/day-([0-6])/) != -1) {
                                                var day = el.className.match(/day-([0-6])/)[1];
                                                statusText = getTitleTranslation(11, [getDayTranslation(day, false)]);
                                        } else if(el.className.search(/prev-year/) != -1) {
                                                statusText = getTitleTranslation(2);
                                        } else if(el.className.search(/prev-month/) != -1) {
                                                statusText = getTitleTranslation(0);
                                        } else if(el.className.search(/next-year/) != -1) {
                                                statusText = getTitleTranslation(3);
                                        } else if(el.className.search(/next-month/) != -1) {
                                                statusText = getTitleTranslation(1);
                                        } else if(el.className.search(/today-but/) != -1 && el.className.search(/disabled/) == -1) {
                                                statusText = getTitleTranslation(12);
                                        };
                                        break;
                                default:
                                        statusText = "";
                        };
                        while(el.parentNode) {
                                el = el.parentNode;
                                if(el.nodeType == 1 && el.tagName.toLowerCase() == "tr") {                                                  
                                        if(o.currentTR) {
                                                if(el == o.currentTR) break;
                                                o.currentTR.className = ""; 
                                        };                                                 
                                        el.className = "dp-row-highlight";
                                        o.currentTR = el;
                                        break;
                                };
                        };                                                          
                        if(o.statusBar && statusText) { 
                                o.updateStatus(statusText); 
                        };                                 
                }; 
                this.clearTimer = function() {
                        o.stopTimer();
                        o.timerInc      = 800;
                        o.yearInc       = 0;
                        o.monthInc      = 0;
                        o.dayInc        = 0;
                        
                        removeEvent(document, "mouseup", o.clearTimer);
                        if(o.mouseDownElem != null) {
                                removeEvent(o.mouseDownElem, "mouseout",  o.clearTimer);
                        };
                        o.mouseDownElem = null;
                };    
                
                var o = this;                 
                
                this.setDateFromInput();
                
                if(this.staticPos) {                          
                        this.create();                                               
                } else { 
                        this.createButton();                                               
                };
                
                (function() {
                        var elemID, elem, elemCnt = 0;
                        
                        for(elemID in o.formElements) {                              
                                elem = document.getElementById(elemID);
                                if(elem && elem.tagName && elem.tagName.search(/select|input/i) != -1) {                                                                     
                                        addEvent(elem, "change", o.changeHandler); 
                                        if(elemCnt == 0 && elem.form) {
                                                addEvent(elem.form, "reset", o.reset);
                                        };
                                        elemCnt++;                               
                                };
                                
                                if(!elem || elem.disabled == true) {
                                        o.disableDatePicker();
                                };                         
                        };                                      
                })();   
                                        
                // We have fully created the datepicker...
                this.fullCreate = true;
        };
        datePicker.prototype.addButtonEvents = function(but) {
               function buttonEvent (e) {
                        e = e || window.event;                      
                        
                        var inpId     = this.id.replace('fd-but-',''),
                            dpVisible = isVisible(inpId),
                            autoFocus = false,
                            kbEvent   = datePickers[inpId].kbEvent;
                            
                        if(kbEvent) {
                                datePickers[inpId].kbEvent = false;
                                return;
                        };

                        if(e.type == "keydown") {
                                datePickers[inpId].kbEvent = true;
                                var kc = e.keyCode != null ? e.keyCode : e.charCode;
                                if(kc != 13) return true; 
                                if(dpVisible) {
                                        removeClass(this, "date-picker-button-active")                                          
                                        hideAll();
                                        return stopEvent(e);
                                };                                   
                                autoFocus = true;
                        } else {
                                datePickers[inpId].kbEvent = false;
                        };

                        if(!dpVisible) {                                 
                                addClass(this, "date-picker-button-active")
                                hideAll(inpId);                                                             
                                showDatePicker(inpId, autoFocus);
                        } else {
                                removeClass(this, "date-picker-button-active");                        
                                hideAll();
                        };
                
                        return stopEvent(e);
                };
                
                but.onkeydown = buttonEvent;
                but.onclick   = buttonEvent;
                
                if(!buttonTabIndex || this.bespokeTabIndex === false) {
                        setTabIndex(but, -1); 
                        but.onkeydown = null; 
                        removeEvent(but, "keydown", buttonEvent);
                } else {
                        setTabIndex(but, this.bespokeTabIndex);
                };                              
        };
        
        datePicker.prototype.createButton = function() {
                
                if(this.staticPos || document.getElementById("fd-but-" + this.id)) { 
                        return; 
                };

                var inp         = document.getElementById(this.id),
                    span        = document.createElement('span'),
                    but         = document.createElement('a');

                but.href        = "#" + this.id;
                but.className   = "date-picker-control";
                but.title       = getTitleTranslation(5);
                but.id          = "fd-but-" + this.id;
                                
                span.appendChild(document.createTextNode(nbsp));
                but.appendChild(span);

                span = document.createElement('span');
                span.className = "fd-screen-reader";
                span.appendChild(document.createTextNode(but.title));
                but.appendChild(span);
                
                // Set the ARIA role to be "button"
                setARIARole(but, "button");                 
                
                // Set a "haspopup" ARIA property - should this not be a list if ID's????
                setARIAProperty(but, "haspopup", true);
                                             			                	
                if(this.positioned && document.getElementById(this.positioned)) {
                        document.getElementById(this.positioned).appendChild(but);
                } else {
                        inp.parentNode.insertBefore(but, inp.nextSibling);
                };                   
                
                this.addButtonEvents(but);

                but = null;
                
                this.callback("dombuttoncreate", {id:this.id});
        };
        datePicker.prototype.setBespokeTitles = function(titles) {                
                this.bespokeTitles = {};
                this.addBespokeTitles(titles);               
        }; 
        datePicker.prototype.addBespokeTitles = function(titles) {                
                for(var dt in titles) {
                        if(titles.hasOwnProperty(dt)) {
                                this.bespokeTitles[dt] = titles[dt];
                        };
                };              
        }; 
        datePicker.prototype.getBespokeTitle = function(y,m,d) {
                var dt, dtFull, yyyymmdd = y + String(pad(m)) + pad(d);
                
                // Try the datepickers bespoke titles
                for(dt in this.bespokeTitles) {
                        if(this.bespokeTitles.hasOwnProperty(dt)) {
                                dtFull = String(dt).replace(/^(\*\*\*\*)/, y).replace(/^(\d\d\d\d)(\*\*)/, "$1"+ pad(m));        
                                if(dtFull == yyyymmdd) {
                                        return this.bespokeTitles[dt];
                                };
                        };
                };
                                
                // Try the generic bespoke titles
                for(dt in bespokeTitles) {
                        if(bespokeTitles.hasOwnProperty(dt)) {
                                dtFull = String(dt).replace(/^(\*\*\*\*)/, y).replace(/^(\d\d\d\d)(\*\*)/, "$1"+ pad(m));        
                                if(dtFull == yyyymmdd) {
                                        return bespokeTitles[dt];
                                };
                        };
                };
                
                return false;             
        };
        datePicker.prototype.returnSelectedDate = function() {                
                return this.dateSet;                
        };   
        datePicker.prototype.setRangeLow = function(range) {
                if(String(range).search(rangeRegExp) == -1) {
                        if(debug) {
                                throw "Invalid value passed to setRangeLow method: " + range;
                        };
                        return false;
                };
                this.rangeLow = range;
                if(!this.inUpdate) {
                        this.setDateFromInput();
                };                
        };
        datePicker.prototype.setRangeHigh = function(range) {
                if(String(range).search(rangeRegExp) == -1) {
                        if(debug) {
                                throw "Invalid value passed to setRangeHigh method: " + range;
                        };
                        return false;
                };
                this.rangeHigh = range;                                               
                if(!this.inUpdate) {
                        this.setDateFromInput();
                };                
        };
        datePicker.prototype.setDisabledDays = function(dayArray) {
                if(!dayArray.length || dayArray.join("").search(/^([0|1]{7})$/) == -1) {
                        if(debug) {
                                throw "Invalid values located when attempting to call setDisabledDays";
                        };
                        return false;
                };                
                this.disabledDays = dayArray;                 
                if(!this.inUpdate) {
                        this.setDateFromInput();
                };    
        };
        
        datePicker.prototype.setDisabledDates = function(dateObj) {                      
                this.filterDateList(dateObj, true);                
        }; 
        datePicker.prototype.setEnabledDates = function(dateObj) {
                this.filterDateList(dateObj, false);                               
        };         
        datePicker.prototype.addDisabledDates = function(dateObj) {                    
                this.addDatesToList(dateObj, true);                                                                
        };
        datePicker.prototype.addEnabledDates = function(dateObj) {
                this.addDatesToList(dateObj, false);
        };
        datePicker.prototype.filterDateList = function(dateObj, type) {                                                              
                var tmpDates = [];
                for(var i = 0; i < this.dateList.length; i++) {
                        if(this.dateList[i].type != type) {
                                tmpDates.push(this.dateList[i]);                        
                        };
                };
                
                this.dateList = tmpDates.concat();
                this.addDatesToList(dateObj, type);                
        };
        datePicker.prototype.addDatesToList = function(dateObj, areDisabled) {                                    
                var startD;
                for(startD in dateObj) {
                        if(String(startD).search(wcDateRegExp) != -1 && (dateObj[startD] == 1 || String(dateObj[startD]).search(wcDateRegExp) != -1)) {
                                
                                if(dateObj[startD] != 1 && Number(String(startD).replace(/^\*\*\*\*/, 2010).replace(/^(\d\d\d\d)(\*\*)/, "$1"+"22")) > Number(String(dateObj[startD]).replace(/^\*\*\*\*/, 2010).replace(/^(\d\d\d\d)(\*\*)/, "$1"+"22"))) {
                                        continue;
                                };
                                
                                this.dateList.push({
                                        type:!!(areDisabled),
                                        rLow:startD,
                                        rHigh:dateObj[startD]
                                });                                
                        };
                };
                
                if(!this.inUpdate) {
                        this.setDateFromInput();
                };                                                                
        };
        datePicker.prototype.setSelectedDate = function(yyyymmdd) {                                             
                if(String(yyyymmdd).search(wcDateRegExp) == -1) {
                        return false;
                };  
                
                var match = yyyymmdd.match(rangeRegExp),
                    dt    = new Date(+match[2],+match[3]-1,+match[4]);
                
                if(!dt || isNaN(dt) || !this.canDateBeSelected(dt)) {
                        return false;
                };
                    
                this.dateSet = new Date(dt);
                
                if(!this.inUpdate) {
                        this.updateTable();
                };
                
                this.callback("dateset", this.createCbArgObj());
                this.returnFormattedDate();                                         
        };
        datePicker.prototype.checkSelectedDate = function() {                
                if(this.dateSet && !this.canDateBeSelected(this.dateSet)) {                        
                        this.dateSet = null;
                };
                if(!this.inUpdate) {
                        this.updateTable();
                };
        };
        datePicker.prototype.addOnFocusEvents = function() {                              
                if(this.kbEventsAdded || this.noFocus) {                         
                        return;
                };
                
                addEvent(document, "keypress", this.onkeydown);
                addEvent(document, "mousedown", this.onmousedown);
                
                /*@cc_on
                @if(@_win32)
                removeEvent(document, "keypress", this.onkeydown);
                addEvent(document, "keydown", this.onkeydown);                 
                @end
                @*/
                if(window.devicePixelRatio) {
                        removeEvent(document, "keypress", this.onkeydown);
                        addEvent(document, "keydown", this.onkeydown);
                };             
                this.noFocus = false;   
                this.kbEventsAdded = true;                
        };         
        datePicker.prototype.removeOnFocusEvents = function() {
                
                if(!this.kbEventsAdded) { 
                        return; 
                };
                
                removeEvent(document, "keypress",  this.onkeydown);
                removeEvent(document, "keydown",   this.onkeydown);
                removeEvent(document, "mousedown", this.onmousedown);                 
                
                this.kbEventsAdded = false;                 
        };         
        datePicker.prototype.stopTimer = function() {
                this.timerSet = false;
                window.clearTimeout(this.timer);
        };
        datePicker.prototype.setOpacity = function(op) {
                this.div.style.opacity = op/100;
                this.div.style.filter = 'alpha(opacity=' + op + ')';
                this.opacity = op;
        };              
        datePicker.prototype.truePosition = function(element) {
                var pos = this.cumulativeOffset(element);
                if(isOpera) { 
                        return pos; 
                };
                var iebody      = (document.compatMode && document.compatMode != "BackCompat")? document.documentElement : document.body,
                    dsocleft    = document.all ? iebody.scrollLeft : window.pageXOffset,
                    dsoctop     = document.all ? iebody.scrollTop  : window.pageYOffset,
                    posReal     = this.realOffset(element);
                return [pos[0] - posReal[0] + dsocleft, pos[1] - posReal[1] + dsoctop];
        };
        datePicker.prototype.realOffset = function(element) {
                var t = 0, l = 0;
                do {
                        t += element.scrollTop  || 0;
                        l += element.scrollLeft || 0;
                        element = element.parentNode;
                } while(element);
                return [l, t];
        };
        datePicker.prototype.cumulativeOffset = function(element) {
                var t = 0, l = 0;
                do {
                        t += element.offsetTop  || 0;
                        l += element.offsetLeft || 0;
                        element = element.offsetParent;
                } while(element);
                return [l, t];
        };
        datePicker.prototype.outOfRange = function(tmpDate) {
                
                if(!this.rangeLow && !this.rangeHigh) { 
                        return false; 
                };
                
                var level = false;
                
                if(!tmpDate) {
                        level   = true;
                        tmpDate = this.date;
                };

                var d  = pad(tmpDate.getDate()),
                    m  = pad(tmpDate.getMonth() + 1),
                    y  = tmpDate.getFullYear(),
                    dt = String(y)+String(m)+String(d);

                if(this.rangeLow && +dt < +this.rangeLow) {
                        if(!level) { 
                                return true; 
                        };
                        this.date = new Date(this.rangeLow.substr(0,4), this.rangeLow.substr(4,2)-1, this.rangeLow.substr(6,2), 5, 0, 0);
                        return false;
                };
                if(this.rangeHigh && +dt > +this.rangeHigh) {
                        if(!level) { 
                                return true; 
                        };
                        this.date = new Date(this.rangeHigh.substr(0,4), this.rangeHigh.substr(4,2)-1, this.rangeHigh.substr(6,2), 5, 0, 0);
                };
                return false;
        };  
        datePicker.prototype.canDateBeSelected = function(tmpDate) {
                if(!tmpDate || isNaN(tmpDate)) {
                        return false;
                };
                                                               
                var d  = pad(tmpDate.getDate()),
                    m  = pad(tmpDate.getMonth() + 1),
                    y  = tmpDate.getFullYear(),
                    dt = y + "" + m + "" + d,
                    dd = this.getDateExceptions(y, m),                    
                    wd = tmpDate.getDay() == 0 ? 7 : tmpDate.getDay();               
                
                // If date out of range
                if((this.rangeLow && +dt < +this.rangeLow) 
                   || 
                   (this.rangeHigh && +dt > +this.rangeHigh) 
                   ||
                   // or the date has been explicitly disabled 
                   ((dt in dd) && dd[dt] == 1) 
                   ||
                   // or the date lies on a disabled weekday and it hasn't been explicitly enabled 
                   (this.disabledDays[wd-1] && (!(dt in dd) || ((dt in dd) && dd[dt] == 1)))) {
                        return false;
                };
                
                return true;
        };        
        datePicker.prototype.updateStatus = function(msg) {                                
                while(this.statusBar.firstChild) { 
                        this.statusBar.removeChild(this.statusBar.firstChild); 
                };
                // All this arseing about just for sups in the footer... nice typography and all that...
                if(msg && this.statusFormat.search(/%S/) != -1 && msg.search(/([0-9]{1,2})(st|nd|rd|th)/) != -1) {                
                        msg = cbSplit(msg.replace(/([0-9]{1,2})(st|nd|rd|th)/, "$1<sup>$2</sup>"), /<sup>|<\/sup>/);                                                 
                        var dc = document.createDocumentFragment();
                        for(var i = 0, nd; nd = msg[i]; i++) {
                                if(/^(st|nd|rd|th)$/.test(nd)) {
                                        var sup = document.createElement("sup");
                                        sup.appendChild(document.createTextNode(nd));
                                        dc.appendChild(sup);
                                } else {
                                        dc.appendChild(document.createTextNode(nd));
                                };
                        };
                        this.statusBar.appendChild(dc);                        
                } else {                        
                        this.statusBar.appendChild(document.createTextNode(msg ? msg : getTitleTranslation(9)));                                                 
                };                                    
        };
        
        /* So needs rewritten */
        datePicker.prototype.setDateFromInput = function() {
                var origDateSet = this.dateSet,
                    m           = false,
                    but         = this.staticPos ? false : document.getElementById("fd-but-" + this.id),
                    i, dt, elemID, elem, elemFmt, d, y, elemVal, dp, mp, yp, dpt;
                
                // Reset the internal dateSet variable
                this.dateSet = null;
                
                // Try and get a year, month and day from the form element values   
                for(elemID in this.formElements) {
                
                        elem = document.getElementById(elemID);
                        
                        if(!elem) {
                                return false;
                        };
                        
                        elemVal = String(elem.value);
                        elemFmt = this.formElements[elemID];
                        dt      = false;
                        
                        dp = elemFmt.search(dPartsRegExp) != -1 ? 1 : 0;
                        mp = elemFmt.search(mPartsRegExp) != -1 ? 1 : 0;
                        yp = elemFmt.search(yPartsRegExp) != -1 ? 1 : 0;
                        
                        dpt = dp + mp + yp;
                        
                        allFormats = [];
                        allFormats.push(elemFmt);
                        
                        // Try to assign some default date formats to throw at
                        // the (simple) regExp parser.
                        
                        // If year, month & day required
                        if(dp && mp && yp) {
                                // Inject some common formats, placing the easiest
                                // to spot at the beginning.
                                allFormats = allFormats.concat([
                                        "%Y%m%d",       
                                        "%Y/%m/%d",     
                                        "%Y/%n/%d",     
                                        "%Y/%n/%j",     
                                        "%d/%m/%Y",     
                                        "%j/%m/%Y",     
                                        "%j/%n/%Y",     
                                        "%d/%m/%y",
                                        "%d/%M/%Y",     
                                        "%d/%F/%Y",
                                        "%d/%M/%y",
                                        "%d/%F/%y",
                                        "%d%m%Y",
                                        "%j%m%Y",
                                        "%d%n%Y",
                                        "%j%n%Y",
                                        "%d%m%y",
                                        "%j%m%y",
                                        "%j%n%y"                                                                           
                                        ]);        
                        } else if(yp) {
                                allFormats = allFormats.concat([
                                        "%Y",
                                        "%y"
                                        ]);
                        } else if(mp) {
                                allFormats = allFormats.concat([
                                        "%M",
                                        "%F",
                                        "%m",
                                        "%n"
                                        ]);
                        } else if(dp) {
                                allFormats = allFormats.concat([
                                        "%d%",
                                        "%j"
                                        ]);
                        };
                        
                        for(i = 0; i < allFormats.length; i++) { 
                                dt = parseDateString(elemVal, allFormats[i]); 
                                
                                if(dt) {
                                        if(!d && dp && dt.d) {
                                                d = dt.d;        
                                        };
                                        if(m === false && mp && dt.m) { 
                                                m = dt.m;                                               
                                        };
                                        if(!y && yp && dt.y) {
                                                y = dt.y;        
                                        };                        
                                };
                                
                                if(((dp && d) || !dp)
                                   &&
                                   ((mp && !m === false) || !mp)
                                   &&
                                   ((yp && y) || !yp)) { 
                                        break;
                                };
                        };                                            
                };
                
                dt = false;
                
                if(d && !(m === false) && y) { 
                        if(+d > daysInMonth(+m - 1, +y)) {
                                d  = daysInMonth(+m - 1, +y);
                                dt = false;
                        } else {
                                dt = new Date(+y, +m - 1, +d);
                        };
                };
                
                if(but) {
                        removeClass(but, "date-picker-dateval");
                };
                        
                if(!dt || isNaN(dt)) {                        
                        var newDate = new Date(y || new Date().getFullYear(), !(m === false) ? m - 1 : new Date().getMonth(), 1);
                        this.date = this.cursorDate ? new Date(+this.cursorDate.substr(0,4), +this.cursorDate.substr(4,2) - 1, +this.cursorDate.substr(6,2)) : new Date(newDate.getFullYear(), newDate.getMonth(), Math.min(+d || new Date().getDate(), daysInMonth(newDate.getMonth(), newDate.getFullYear())));
                        
                        this.date.setHours(5);
                        this.outOfRange();                         
                        if(this.fullCreate) {
                                this.updateTable();
                        };                 
                        return;
                };
        
                dt.setHours(5);
                this.date = new Date(dt);                            
                this.outOfRange();                 
                
                if(dt.getTime() == this.date.getTime() && this.canDateBeSelected(this.date)) {                                              
                        this.dateSet = new Date(this.date);
                        if(but) {
                                addClass(but, "date-picker-dateval");
                        };                        
                };
                
                if(this.fullCreate) {
                        this.updateTable();
                };
                
                this.returnFormattedDate(true);
        };
        
        datePicker.prototype.setSelectIndex = function(elem, indx) {
                for(var opt = elem.options.length-1; opt >= 0; opt--) {
                        if(elem.options[opt].value == indx) {
                                elem.selectedIndex = opt;
                                return;
                        };
                };
        };
        datePicker.prototype.returnFormattedDate = function(noFocus) {     
                var but = this.staticPos ? false : document.getElementById("fd-but-" + this.id);
                
                if(!this.dateSet) {
                        if(but) {
                                removeClass(but, "date-picker-dateval");
                        };                                
                        return;
                };
                
                var d   = pad(this.dateSet.getDate()),
                    m   = pad(this.dateSet.getMonth() + 1),
                    y   = this.dateSet.getFullYear(),
                    el  = false, 
                    elemID, elem, elemFmt, fmtDate;
                
                noFocus = !!noFocus;
                 
                for(elemID in this.formElements) {
                        elem    = document.getElementById(elemID);
                        
                        if(!elem) {
                                return;
                        };
                        
                        if(!el) {
                                el = elem;
                        };
                        
                        elemFmt = this.formElements[elemID];
                        
                        fmtDate = printFormattedDate(this.dateSet, elemFmt, returnLocaleDate);                   
                        if(elem.tagName.toLowerCase() == "input") {
                                elem.value = fmtDate; 
                        } else {  
                                this.setSelectIndex(elem, fmtDate);                              
                        };
                };
                
                if(this.staticPos) { 
                        this.noFocus = true;
                        this.updateTable(); 
                        this.noFocus = false;
                } else if(but) {
                        addClass(but, "date-picker-dateval");
                };                         
                
                if(this.fullCreate) {
                        if(el.type && el.type != "hidden" && !noFocus) { 
                                el.focus(); 
                        };                                                                                                                                             
                };         
        };
        datePicker.prototype.disableDatePicker = function() {
                if(this.disabled) {
                        return;
                };
                
                if(this.staticPos) {
                        this.removeOnFocusEvents();
                        this.removeOldFocus();
                        this.noFocus = true;
                        addClass(this.div, "date-picker-disabled")  
                        this.table.onmouseover = this.table.onclick = this.table.onmouseout = this.table.onmousedown = null;                                      
                        removeEvent(document, "mousedown", this.onmousedown);                         
                        removeEvent(document, "mouseup",   this.clearTimer);                       
                } else {  
                        if(this.visible) {
                                this.hide();
                        };                        
                        var but = document.getElementById("fd-but-" + this.id);
                        if(but) {
                                addClass(but, "date-picker-control-disabled");
                                // Set a "disabled" ARIA state
                                setARIAProperty(but, "disabled", true);                               
                                but.onkeydown = but.onclick = function() { 
                                        return false; 
                                }; 
                                setTabIndex(but, -1);                
                        };                         
                };               
                                
                clearTimeout(this.timer);                
                this.disabled = true;  
        }; 
        datePicker.prototype.enableDatePicker = function() {
                if(!this.disabled) {
                        return;
                };
                
                if(this.staticPos) {
                        this.removeOldFocus();
                        
                        // Reset the cursor to the selected date
                        if(this.dateSet != null) {
                                this.date = this.dateSet;
                        };
                        this.noFocus = true;                        
                        this.updateTable();                        
                        removeClass(this.div, "date-picker-disabled");
                        this.disabled = false;                         
                        this.table.onmouseover = this.onmouseover;
                        this.table.onmouseout  = this.onmouseout;
                        this.table.onclick     = this.onclick;                         
                        this.table.onmousedown = this.onmousedown;                                                                    
                } else {                         
                        var but = document.getElementById("fd-but-" + this.id);
                        if(but) {                                
                                removeClass(but, "date-picker-control-disabled");
                                // Reset the "disabled" ARIA state
                                setARIAProperty(but, "disabled", false);
                                this.addButtonEvents(but);                                                
                        };                         
                };
                
                this.disabled = false;                
        };
        datePicker.prototype.disableTodayButton = function() {
                var today = new Date();                
                removeClass(this.butToday, "fd-disabled");
                if(this.outOfRange(today) 
                   || 
                   (this.date.getDate() == today.getDate() 
                    && 
                    this.date.getMonth() == today.getMonth() 
                    && 
                    this.date.getFullYear() == today.getFullYear())
                    ) {
                        addClass(this.butToday, "fd-disabled");                          
                };
        };
        datePicker.prototype.updateTableHeaders = function() {
                var colspanTotal = this.showWeeks ? 8 : 7,
                    colOffset    = this.showWeeks ? 1 : 0,
                    d, but;

                for(var col = colOffset; col < colspanTotal; col++ ) {
                        d = (this.firstDayOfWeek + (col - colOffset)) % 7;
                        this.ths[col].title = getDayTranslation(d, false);

                        if(col > colOffset) {
                                but = this.ths[col].getElementsByTagName("span")[0];
                                while(but.firstChild) { 
                                        but.removeChild(but.firstChild); 
                                };
                                but.appendChild(document.createTextNode(getDayTranslation(d, true)));
                                but.title = this.ths[col].title;                               
                                but = null;
                        } else {
                                while(this.ths[col].firstChild) { 
                                        this.ths[col].removeChild(this.ths[col].firstChild); 
                                };
                                this.ths[col].appendChild(document.createTextNode(getDayTranslation(d, true)));
                        };

                        removeClass(this.ths[col], "date-picker-highlight");
                        if(this.highlightDays[d]) {
                                addClass(this.ths[col], "date-picker-highlight");
                        };
                };
                
                if(this.created) { 
                        this.updateTable(); 
                };
        }; 
        datePicker.prototype.callback = function(type, args) {   
                if(!type || !(type in this.callbacks)) { 
                        return false; 
                };
                
                var ret = false,
                    func;
                                       
                for(func = 0; func < this.callbacks[type].length; func++) {                         
                        ret = this.callbacks[type][func](args || this.id);                        
                };
                                      
                return ret;
        };      
        datePicker.prototype.showHideButtons = function(tmpDate) {
                if(!this.butPrevYear) { return; };
                
                var tdm = tmpDate.getMonth(),
                    tdy = tmpDate.getFullYear();

                if(this.outOfRange(new Date((tdy - 1), tdm, daysInMonth(+tdm, tdy-1)))) {                                                    
                        addClass(this.butPrevYear, "fd-disabled");
                        if(this.yearInc == -1) {
                                this.stopTimer();
                        };
                } else {
                        removeClass(this.butPrevYear, "fd-disabled");
                };                 
                
                if(this.outOfRange(new Date(tdy, (+tdm - 1), daysInMonth(+tdm-1, tdy)))) {                           
                        addClass(this.butPrevMonth, "fd-disabled");
                        if(this.monthInc == -1) {
                                this.stopTimer();
                        };
                } else {
                        removeClass(this.butPrevMonth, "fd-disabled");
                };
         
                if(this.outOfRange(new Date((tdy + 1), +tdm, 1))) {                            
                        addClass(this.butNextYear, "fd-disabled");
                        if(this.yearInc == 1) {
                                this.stopTimer();
                        };
                } else {
                        removeClass(this.butNextYear, "fd-disabled");
                };                
                
                if(this.outOfRange(new Date(tdy, +tdm + 1, 1))) {
                        addClass(this.butNextMonth, "fd-disabled");
                        if(this.monthInc == 1) {
                                this.stopTimer();
                        };
                } else {
                        removeClass(this.butNextMonth, "fd-disabled");
                };
        };        
        var localeDefaults = {
                fullMonths:["January","February","March","April","May","June","July","August","September","October","November","December"],
                monthAbbrs:["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"],
                fullDays:  ["Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"],
                dayAbbrs:  ["Mon","Tue","Wed","Thu","Fri","Sat","Sun"],
                titles:    ["Previous month","Next month","Previous year","Next year", "Today", "Show Calendar", "wk", "Week [[%0%]] of [[%1%]]", "Week", "Select a date", "Click \u0026 Drag to move", "Display \u201C[[%0%]]\u201D first", "Go to Today\u2019s date", "Disabled date :"],
                rtl:       false,
                firstDayOfWeek:0,
                imported:  false
        };        
        var joinNodeLists = function() {
                if(!arguments.length) { 
                        return []; 
                };
                var nodeList = [];
                for (var i = 0; i < arguments.length; i++) {
                        for (var j = 0, item; item = arguments[i][j]; j++) {
                                nodeList[nodeList.length] = item;
                        };
                };
                return nodeList;
        };
        var cleanUp = function() {
                var dp, fe;
                for(dp in datePickers) {
                        for(fe in datePickers[dp].formElements) {
                                if(!document.getElementById(fe)) {
                                        datePickers[dp].destroy();
                                        datePickers[dp] = null;
                                        delete datePickers[dp];
                                        break;
                                };
                        };
                };
        };         
        var hideAll = function(exception) {
                var dp;
                for(dp in datePickers) {
                        if(!datePickers[dp].created || (exception && exception == datePickers[dp].id)) {
                                continue;
                        };
                        datePickers[dp].hide();
                };
        };
        var hideDatePicker = function(inpID) {                
                if(inpID in datePickers) {
                        if(!datePickers[inpID].created || datePickers[inpID].staticPos) {
                                return;
                        };
                        datePickers[inpID].hide();
                };
        };
        var showDatePicker = function(inpID, autoFocus) {                
                if(!(inpID in datePickers)) {
                        return false;
                };   
                
                datePickers[inpID].clickActivated = !!!autoFocus;             
                datePickers[inpID].show(autoFocus);
                return true;        
        };
        var destroy = function(e) {
                e = e || window.event;
                
                // Don't remove datepickers if it's a pagehide/pagecache event (webkit et al)
                if(e.persisted) {
                        return;
                };
                
                for(dp in datePickers) {
                        datePickers[dp].destroy();
                        datePickers[dp] = null;
                        delete datePickers[dp];
                };
                datePickers = null;
                
                removeEvent(window, 'unload', datePickerController.destroy);
        }; 
        var destroySingleDatePicker = function(id) {
                if(id && (id in datePickers)) {
                        datePickers[id].destroy();
                        datePickers[id] = null;
                        delete datePickers[id];        
                };
        };
        var getTitleTranslation = function(num, replacements) {
                replacements = replacements || [];
                if(localeImport.titles.length > num) {
                         var txt = localeImport.titles[num];
                         if(replacements && replacements.length) {
                                for(var i = 0; i < replacements.length; i++) {
                                        txt = txt.replace("[[%" + i + "%]]", replacements[i]);
                                };
                         };
                         return txt.replace(/[[%(\d)%]]/g,"");
                };
                return "";
        };
        var getDayTranslation = function(day, abbreviation) {
                var titles = localeImport[abbreviation ? "dayAbbrs" : "fullDays"];
                return titles.length && titles.length > day ? titles[day] : "";
        };
        var getMonthTranslation = function(month, abbreviation) {
                var titles = localeImport[abbreviation ? "monthAbbrs" : "fullMonths"];
                return titles.length && titles.length > month ? titles[month] : "";
        };
        var daysInMonth = function(nMonth, nYear) {
                nMonth = (nMonth + 12) % 12;
                return (((0 == (nYear%4)) && ((0 != (nYear%100)) || (0 == (nYear%400)))) && nMonth == 1) ? 29: [31,28,31,30,31,30,31,31,30,31,30,31][nMonth];
        };
        
        var getWeeksInYear = function(Y) {
                if(Y in weeksInYearCache) {
                        return weeksInYearCache[Y];
                };
                
                var X1 = new Date(Y, 0, 4),
                    X2 = new Date(Y, 11, 28);
                
                X1.setDate(X1.getDate() - (6 + X1.getDay()) % 7);
                X2.setDate(X2.getDate() + (7 - X2.getDay()) % 7);
                
                weeksInYearCache[Y] = Math.round((X2 - X1) / 604800000);
                
                return weeksInYearCache[Y];
        };

        var getWeekNumber = function(y,m,d) {
                var d   = new Date(y, m, d, 0, 0, 0),
                    DoW = d.getDay(), 
                    ms;
                    
                d.setDate(d.getDate() - (DoW + 6) % 7 + 3); 
                
                ms = d.valueOf(); 
                
                d.setMonth(0);
                d.setDate(4);
                
                return Math.round((ms - d.valueOf()) / (7 * 864e5)) + 1;
        };
        
        var printFormattedDate = function(date, fmt, useImportedLocale) {
                if(!date || isNaN(date)) { 
                        return fmt; 
                };                
                
                var d           = date.getDate(),
                    D           = date.getDay(),
                    m           = date.getMonth(),
                    y           = date.getFullYear(),
                    locale      = useImportedLocale ? localeImport : localeDefaults,
                    fmtParts    = String(fmt).split(formatSplitRegExp),
                    fmtParts    = cbSplit(fmt, formatSplitRegExp), 
                    fmtNewParts = [],
                    flags       = {
                                "d":pad(d),
                                "D":locale.dayAbbrs[D == 0 ? 6 : D - 1],
                                "l":locale.fullDays[D == 0 ? 6 : D - 1],
                                "j":d,
                                "N":D == 0 ? 7 : D,
                                "w":D,                                
                                "W":getWeekNumber(y,m,d),
                                "M":locale.monthAbbrs[m],
                                "F":locale.fullMonths[m],
                                "m":pad(m + 1),
                                "n":m + 1,
                                "t":daysInMonth(m, y),
                                "y":String(y).substr(2,2),                                
                                "Y":y,
                                "S":["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
                                },
                    len         = fmtParts.length,
                    currFlag, f;    
                
                
                for(f = 0; f < len; f++) {
                        currFlag = fmtParts[f];                                       
                        fmtNewParts.push(currFlag in flags ? flags[currFlag] : currFlag);                        
                };
                
                return fmtNewParts.join("");
        };
        var parseDateString = function(str, fmt) {
                var d     = false,
                    m     = false,
                    y     = false,
                    dp    = fmt.search(dPartsRegExp) != -1 ? 1 : 0,
                    mp    = fmt.search(mPartsRegExp) != -1 ? 1 : 0,
                    yp    = fmt.search(yPartsRegExp) != -1 ? 1 : 0,                        
                    now   = new Date(),
                    parts = cbSplit(fmt, formatSplitRegExp),
                    str   = "" + str,
                    len   = parts.length,
                    pt, part;
                
                        
                //console.log("attempting to parse " + fmt + " from string " + str)
                    
                loopLabel:
                for(pt = 0; pt < len; pt++) {
                        part = parts[pt];
                        
                        if(part === "") {
                                continue loopLabel;
                        };
                        
                        //console.log(pt + ": parsing " + part + " from string " + str)
                        
                        if(str.length == 0) { 
                                break; 
                        };
                              
                        switch(part) {
                                // Dividers - be easy on them all i.e. accept them all when parsing...                                
                                case "/":
                                case ".":
                                case " ":
                                case "-":
                                case ",":
                                case ":":       
                                                str = str.substr(1);                                     
                                                break;                                             
                                // DAY
                                case "d": // Day of the month, 2 digits with leading zeros (01 - 31)
                                                if(str.search(/^(3[01]|[12][0-9]|0[1-9])/) != -1) {
                                                        d = str.substr(0,2);
                                                        str = str.substr(2);                                                        
                                                        //console.log("d and str: " + d + " " + str)
                                                        break;
                                                } else {                                                        
                                                        return false;
                                                };
                                case "j": // Day of the month without leading zeros (1 - 31)  
                                                if(str.search(/^(3[01]|[12][0-9]|[1-9])/) != -1) {
                                                        d = +str.match(/^(3[01]|[12][0-9]|[1-9])/)[0];
                                                        str = str.substr(str.match(/^(3[01]|[12][0-9]|[1-9])/)[0].length);                                                        
                                                        break;
                                                } else {                                                        
                                                        return false;
                                                };
                                case "D": // A textual representation of a day, three letters (Mon - Sun)
                                case "l": // A full textual representation of the day of the week (Monday - Sunday)
                                          // Accept English & imported locales and both modifiers                                                  
                                                l = localeDefaults.fullDays.concat(localeDefaults.dayAbbrs);                                                  
                                                if(localeImport.imported) {
                                                        l = l.concat(localeImport.fullDays).concat(localeImport.dayAbbrs);
                                                }; 
                                                
                                                for(var i = 0; i < l.length; i++) {
                                                        if(new RegExp("^" + l[i], "i").test(str)) {                                                                
                                                                str = str.substr(l[i].length);
                                                                continue loopLabel;
                                                        };
                                                };
                                                
                                                break;                                  
                                case "N": // ISO-8601 numeric representation of the day of the week (added in PHP 5.1.0) 1 (for Monday) through 7 (for Sunday)
                                case "w": // Numeric representation of the day of the week 0 (for Sunday) through 6 (for Saturday)
                                                if(str.search(part == "N" ? /^([1-7])/ : /^([0-6])/) != -1) {
                                                        str = str.substr(1);                                                        
                                                };
                                                break;
                                case "S": // English ordinal suffix for the day of the month, 2 characters: st, nd, rd or th
                                                if(str.search(/^(st|nd|rd|th)/i) != -1) {
                                                        str = str.substr(2);                                                        
                                                };
                                                break;                                
                                // WEEK
                                case "W": // ISO-8601 week number of year, weeks starting on Monday (added in PHP 4.1.0): 1 - 53
                                                if(str.search(/^([1-9]|[1234[0-9]|5[0-3])/) != -1) {
                                                        str = str.substr(str.match(/^([1-9]|[1234[0-9]|5[0-3])/)[0].length);                                                        
                                                };
                                                break;
                                // MONTH
                                case "M": // A short textual representation of a month, three letters
                                case "F": // A full textual representation of a month, such as January or March
                                          // Accept English & imported locales and both modifiers                                                    
                                                l = localeDefaults.fullMonths.concat(localeDefaults.monthAbbrs);
                                                if(localeImport.imported) {
                                                        l = l.concat(localeImport.fullMonths).concat(localeImport.monthAbbrs);
                                                };
                                                for(var i = 0; i < l.length; i++) {                                                        
                                                        if(str.search(new RegExp("^" + l[i],"i")) != -1) {
                                                                str = str.substr(l[i].length);
                                                                m = ((i + 12) % 12) + 1;                                                                 
                                                                continue loopLabel;
                                                        };
                                                };
                                                return false;
                                case "m": // Numeric representation of a month, with leading zeros
                                                l = /^(1[012]|0[1-9])/;
                                                if(str.search(l) != -1) {
                                                        m = +str.substr(0, 2);
                                                        str = str.substr(2);
                                                        break;
                                                } else {                                                        
                                                        return false;
                                                };
                                case "n": // Numeric representation of a month, without leading zeros
                                          // Accept either when parsing
                                                l = /^(1[012]|[1-9])/;
                                                if(str.search(l) != -1) {
                                                        m = +str.match(l)[0];
                                                        str = str.substr(str.match(l)[0].length);
                                                        break;
                                                } else {                                                        
                                                        return false;
                                                };
                                case "t": // Number of days in the given month: 28 through 31
                                                if(str.search(/2[89]|3[01]/) != -1) {
                                                        str = str.substr(2);
                                                        break;
                                                } else {                                                        
                                                        return false;
                                                };
                                // YEAR
                                
                                case "Y": // A full numeric representation of a year, 4 digits
                                                if(str.search(/^(\d{4})/) != -1) {
                                                        y = str.substr(0,4);
                                                        str = str.substr(4);
                                                        break;
                                                } else {                                                        
                                                        return false;
                                                };
                                
        
                                case "y": // A two digit representation of a year                                                
                                                if(str.search(/^(0[0-9]|[1-9][0-9])/) != -1) {
                                                        y = str.substr(0,2);
                                                        y = +y < 50 ? '20' + String(y) : '19' + String(y);
                                                        str = str.substr(2);
                                                        break;
                                                } else {
                                                        return false;
                                                };
                                       
                                default:
                                                str = str.substr(part.length);
                        };
                };   
                
                //console.log("parse end, dmy: " + d + ", " + m + ", " + y)
                
                if((dp && d === false) || (mp && m === false) || (yp && y === false)) {
                        return false;
                };                
                
                if(dp && mp && yp && +d > daysInMonth(+m - 1, +y)) {
                        return false;
                };
                
                return {
                        "d":dp ? +d : false,
                        "m":mp ? +m : false,
                        "y":yp ? +y : false
                        };
        };      
                                    
        var findLabelForElement = function(element) {
                var label;
                if(element.parentNode && element.parentNode.tagName.toLowerCase() == "label") {
                        label = element.parentNode;
                } else {
                        var labelList = document.getElementsByTagName('label');
                        // loop through label array attempting to match each 'for' attribute to the id of the current element
                        for(var lbl = 0; lbl < labelList.length; lbl++) {
                                // Internet Explorer requires the htmlFor test
                                if((labelList[lbl]['htmlFor'] && labelList[lbl]['htmlFor'] == element.id) || (labelList[lbl].getAttribute('for') == element.id)) {
                                        label = labelList[lbl];
                                        break;
                                };
                        };
                };
                
                if(label && !label.id && element.id) { 
                        label.id = element.id + "_label"; 
                };
                
                return label;         
        };  
        var updateLanguage = function() {
                if(typeof(window.fdLocale) == "object" ) {                         
                        localeImport = {
                                titles          : fdLocale.titles,
                                fullMonths      : fdLocale.fullMonths,
                                monthAbbrs      : fdLocale.monthAbbrs,
                                fullDays        : fdLocale.fullDays,
                                dayAbbrs        : fdLocale.dayAbbrs,
                                firstDayOfWeek  : ("firstDayOfWeek" in fdLocale) ? fdLocale.firstDayOfWeek : 0,
                                rtl             : ("rtl" in fdLocale) ? !!(fdLocale.rtl) : false,
                                imported        : true
                        };                                               
                } else if(!localeImport) {                        
                        localeImport = localeDefaults;
                };    
        };
        var loadLanguage = function() {
                updateLanguage();
                for(dp in datePickers) {
                        if(!datePickers[dp].created) {
                                continue;
                        };
                        datePickers[dp].updateTable();
                };   
        };
        var checkElem = function(elem) {                        
                return !(!elem || !elem.tagName || !((elem.tagName.toLowerCase() == "input" && (elem.type == "text" || elem.type == "hidden")) || elem.tagName.toLowerCase() == "select"));                
        };
        var addDatePicker = function(options) {  
                updateLanguage();
                
                if(!options.formElements) {
                        if(debug) {
                                throw "No form elements stipulated within initialisation parameters";
                        };
                        return;
                };
               
                options.id            = (options.id && (options.id in options.formElements)) ? options.id : "";
                options.enabledDates  = false;
                options.disabledDates = false;
                 
                var partsFound  = {d:0,m:0,y:0},
                    cursorDate  = false,
                    myMin       = 0,
                    myMax       = 0,               
                    fmt,
                    opts,
                    dtPartStr,
                    elemID,
                    elem;
                    
                for(elemID in options.formElements) {
                        elem = document.getElementById(elemID);
                        
                        if(!checkElem(elem)) {
                                if(debug) {
                                        throw "Element '" + elemID + "' is of the wrong type or does not exist within the DOM";
                                };
                                return false;
                        };
                        
                        if(!(options.formElements[elemID].match(formatTestRegExp))) {
                                if(debug) {
                                        throw "Element '" + elemID + "' has a date format that does not contain either a day (d|j), month (m|F|n) or year (y|Y) part: " + options.formElements[elemID];
                                };
                                return false;
                        };
                        
                        if(!options.id) {
                                options.id = elemID;
                        };
                        
                        options.formElements[elemID].defaultVal = elem.tagName == "select" ? elem.selectedIndex || 0 : elem.defaultValue;
                        
                        fmt             = {
                                "value":options.formElements[elemID]
                        };
                        
                        fmt.d = fmt.value.search(dPartsRegExp) != -1;
                        fmt.m = fmt.value.search(mPartsRegExp) != -1;
                        fmt.y = fmt.value.search(yPartsRegExp) != -1;
                                         
                        if(fmt.d) { 
                                partsFound.d++; 
                        };
                        if(fmt.m) { 
                                partsFound.m++; 
                        };
                        if(fmt.y) { 
                                partsFound.y++; 
                        };
                        
                        if(elem.tagName.toLowerCase() == "select") {                                
                                // If we have a selectList, then try to parse the higher and lower limits 
                                var selOptions = elem.options;
                                
                                // Check the yyyymmdd 
                                if(fmt.d && fmt.m && fmt.y) { 
                                        cursorDate = false;
                                        
                                        // Dynamically calculate the available "enabled" dates
                                        options.enabledDates = {};
                                        options.disabledDates = {};
                                            
                                        for(i = 0; i < selOptions.length; i++) {                                                                                                
                                                dt = parseDateString(selOptions[i].value, fmt.value);
                                                
                                                if(dt && dt.y && !(dt.m === false) && dt.d) {
                                                        
                                                        dtPartStr = dt.y + "" + pad(dt.m) + pad(dt.d);
                                                        if(!cursorDate) {
                                                                cursorDate = dtPartStr;
                                                        };
                                                        
                                                        options.enabledDates[dtPartStr] = 1;
                                                        
                                                        if(!myMin || +dtPartStr < +myMin) {
                                                                myMin = dtPartStr;
                                                        }; 
                                                        
                                                        if(!myMax || +dtPartStr > +myMax) {
                                                                myMax = dtPartStr;
                                                        };
                                                };                                     
                                        };  
                        
                                        // Automatically set cursor to first available date (if no bespoke cursorDate was set);                                        
                                        if(!options.cursorDate && cursorDate) {
                                                options.cursorDate = cursorDate;
                                        };
                                        
                                        options.disabledDates[myMin] = myMax;
                                          
                                } else if(fmt.m && fmt.y) {
                                         
                                        for(i = 0; i < selOptions.length; i++) {
                                                dt = parseDateString(selOptions[i].value, fmt.value);
                                                if(dt.y && !(dt.m === false)) {
                                                        dtPartStr = dt.y + "" + pad(dt.m);
                                                
                                                        if(!myMin || +dtPartStr < +myMin) {
                                                                myMin = dtPartStr;
                                                        }; 
                                                
                                                        if(!myMax || +dtPartStr > +myMax) {
                                                                myMax = dtPartStr;
                                                        };                                                
                                                };
                                                                                     
                                        };                                           
                                        
                                        // Round the min & max values to be used as rangeLow & rangeHigh
                                        myMin += "" + "01";
                                        myMax += "" + daysInMonth(+myMax.substr(4,2) - 1, +myMax.substr(0,4));
                                                                                
                                } else if(fmt.y) {
                                        for(i = 0; i < selOptions.length; i++) {
                                                dt = parseDateString(selOptions[i].value, fmt.value);
                                                if(dt.y) {                                                           
                                                         
                                                        if(!myMin || +dt.y < +myMin) {
                                                                myMin = dt.y;
                                                        }; 
                                                
                                                        if(!myMax || +dt.y > +myMax) {
                                                                myMax = dt.y;
                                                        }; 
                                                };                         
                                        };  
                                        
                                        // Round the min & max values to be used as rangeLow & rangeHigh
                                        myMin += "" + "0101";
                                        myMax += "" + "1231";                                                                                                    
                                };                                
                        };
                };
                
                if(!(partsFound.d == 1 && partsFound.m == 1 && partsFound.y == 1)) {
                        if(debug) {
                                throw "Could not find all of the required date parts within the date format for element: " + elem.id;
                        };
                        return false;
                }; 

                if(myMin && (!options.rangeLow  || (+options.rangeLow < +myMin))) { 
                        options.rangeLow = myMin; 
                };
                if(myMax && (!options.rangeHigh || (+options.rangeHigh > +myMin))) { 
                        options.rangeHigh = myMax; 
                };                                
                
                opts = {
                        formElements:options.formElements,
                        // Form element id
                        id:options.id,                       
                        // Non popup datepicker required
                        staticPos:!!(options.staticPos),
                        // Position static datepicker or popup datepicker's button
                        positioned:options.positioned && document.getElementById(options.positioned) ? options.positioned : "",
                        // Ranges stipulated in YYYYMMDD format       
                        rangeLow:options.rangeLow && String(options.rangeLow).search(rangeRegExp) != -1 ? options.rangeLow : "",
                        rangeHigh:options.rangeHigh && String(options.rangeHigh).search(rangeRegExp) != -1 ? options.rangeHigh : "",
                        // Status bar format
                        statusFormat:options.statusFormat || statusFormat,                                                                                 
                        // No fade in/out effect
                        noFadeEffect:!!(options.staticPos) ? true : !!(options.noFadeEffect),
                        // No drag functionality
                        dragDisabled:nodrag || !!(options.staticPos) ? true : !!(options.dragDisabled),
                        // Bespoke tabindex for this datePicker (or its activation button)
                        bespokeTabIndex:options.bespokeTabindex && typeof options.bespokeTabindex == 'number' ? parseInt(options.bespokeTabindex, 10) : 0,
                        // Bespoke titles
                        bespokeTitles:options.bespokeTitles || (bespokeTitles || {}),
                        // Final opacity 
                        finalOpacity:options.finalOpacity && typeof options.finalOpacity == 'number' && (options.finalOpacity > 20 && options.finalOpacity <= 100) ? parseInt(+options.finalOpacity, 10) : (!!(options.staticPos) ? 100 : finalOpacity),
                        // Do we hide the form elements on datepicker creation
                        hideInput:!!(options.hideInput),
                        // Do we hide the "today" button
                        noToday:!!(options.noTodayButton),
                        // Do we show week numbers
                        showWeeks:!!(options.showWeeks),
                        // Do we fill the entire grid with dates                                                  
                        fillGrid:!!(options.fillGrid),
                        // Do we constrain selection of dates outside the current month
                        constrainSelection:"constrainSelection" in options ? !!(options.constrainSelection) : true,
                        // The date to set the initial cursor to
                        cursorDate:options.cursorDate && String(options.cursorDate).search(rangeRegExp) != -1 ? options.cursorDate : "",                        
                        // Locate label to set the ARIA labelled-by property
                        labelledBy:findLabelForElement(elem),
                        // Have we been passed a describedBy to set the ARIA decribed-by property...
                        describedBy:(options.describedBy && document.getElementById(options.describedBy)) ? options.describedBy : describedBy && document.getElementById(describedBy) ? describedBy : "",
                        // Callback functions
                        callbacks:options.callbackFunctions ? options.callbackFunctions : {},
                        // Days of the week to highlight (normally the weekend)
                        highlightDays:options.highlightDays && options.highlightDays.length && options.highlightDays.length == 7 ? options.highlightDays : [0,0,0,0,0,1,1],
                        // Days of the week to disable
                        disabledDays:options.disabledDays && options.disabledDays.length && options.disabledDays.length == 7 ? options.disabledDays : [0,0,0,0,0,0,0],
                        // A bespoke class to give the datepicker
                        bespokeClass:options.bespokeClass ? " " + options.bespokeClass : ""                                                                   
                };  
                
                datePickers[options.id] = new datePicker(opts);                         
                
                if("disabledDates" in options && !(options.disabledDates === false)) {
                        datePickers[options.id].setDisabledDates(options.disabledDates)
                };
                
                if("enabledDates" in options && !(options.enabledDates === false)) {
                        datePickers[options.id].setEnabledDates(options.enabledDates)
                };
                
                datePickers[options.id].callback("create", datePickers[options.id].createCbArgObj());                                  
        };

        // Used by the button to dictate whether to open or close the datePicker
        var isVisible = function(id) {
                return (!id || !(id in datePickers)) ? false : datePickers[id].visible;
        };  
        
        var updateStatic = function() {
                var dp;
                for(dp in datePickers) {
                        if(datePickers.hasOwnProperty(dp)) {
                                datePickers[dp].changeHandler();
                        };
                };
        };
        
        addEvent(window, 'unload', destroy);
        addEvent(window, "load", function() { setTimeout(updateStatic, 0); });
        
        // Add oldie class if needed for IE < 9
        /*@cc_on
        @if (@_jscript_version < 5.8)
                addClass(document.documentElement, "oldie-mhtml");
        @end
        @if (@_jscript_version < 9)
                addClass(document.documentElement, "oldie");
        @end 
        @*/
        
        return {
                // General event functions...
                addEvent:               function(obj, type, fn) { return addEvent(obj, type, fn); },
                removeEvent:            function(obj, type, fn) { return removeEvent(obj, type, fn); },
                stopEvent:              function(e) { return stopEvent(e); },
                // Show a single popup datepicker
                show:                   function(inpID) { return showDatePicker(inpID, false); },
                // Hide a popup datepicker
                hide:                   function(inpID) { return hideDatePicker(inpID); },                
                // Create a new datepicker
                createDatePicker:       function(options) { addDatePicker(options); },
                // Destroy a datepicker (remove events and DOM nodes)               
                destroyDatePicker:      function(inpID) { destroySingleDatePicker(inpID); },
                // Check datePicker form elements exist, if not, destroy the datepicker
                cleanUp:                function() { cleanUp(); },                    
                // Pretty print a date object according to the format passed in               
                printFormattedDate:     function(dt, fmt, useImportedLocale) { return printFormattedDate(dt, fmt, useImportedLocale); },
                // Update the internal date using the form element value
                setDateFromInput:       function(inpID) { if(!inpID || !(inpID in datePickers)) return false; datePickers[inpID].setDateFromInput(); },
                // Set low and high date ranges
                setRangeLow:            function(inpID, yyyymmdd) { if(!inpID || !(inpID in datePickers)) { return false; }; datePickers[inpID].setRangeLow(dateToYYYYMMDD(yyyymmdd)); },
                setRangeHigh:           function(inpID, yyyymmdd) { if(!inpID || !(inpID in datePickers)) { return false; }; datePickers[inpID].setRangeHigh(dateToYYYYMMDD(yyyymmdd)); },
                // Set bespoke titles for a datepicker instance
                setBespokeTitles:       function(inpID, titles) {if(!inpID || !(inpID in datePickers)) { return false; }; datePickers[inpID].setBespokeTitles(titles); },
                // Add bespoke titles for a datepicker instance
                addBespokeTitles:       function(inpID, titles) {if(!inpID || !(inpID in datePickers)) { return false; }; datePickers[inpID].addBespokeTitles(titles); },                
                // Attempt to parse a valid date from a date string using the passed in format
                parseDateString:        function(str, format) { return parseDateString(str, format); },
                // Change global configuration parameters
                setGlobalOptions:       function(json) { affectJSON(json); },
                // Forces the datepickers "selected" date
                setSelectedDate:        function(inpID, yyyymmdd) { if(!inpID || !(inpID in datePickers)) { return false; }; datePickers[inpID].setSelectedDate(dateToYYYYMMDD(yyyymmdd)); },
                // Is the date valid for selection i.e. not outside ranges etc
                dateValidForSelection:  function(inpID, dt) { if(!inpID || !(inpID in datePickers)) return false; return datePickers[inpID].canDateBeSelected(dt); },
                // Add disabled and enabled dates
                addDisabledDates:       function(inpID, dts) { if(!inpID || !(inpID in datePickers)) return false; datePickers[inpID].addDisabledDates(dts); },
                setDisabledDates:       function(inpID, dts) { if(!inpID || !(inpID in datePickers)) return false; datePickers[inpID].setDisabledDates(dts); },
                addEnabledDates:        function(inpID, dts) { if(!inpID || !(inpID in datePickers)) return false; datePickers[inpID].addEnabledDates(dts); },
                setEnabledDates:        function(inpID, dts) { if(!inpID || !(inpID in datePickers)) return false; datePickers[inpID].setEnabledDates(dts); },
                // Disable and enable the datepicker
                disable:                function(inpID) { if(!inpID || !(inpID in datePickers)) return false; datePickers[inpID].disableDatePicker(); },
                enable:                 function(inpID) { if(!inpID || !(inpID in datePickers)) return false; datePickers[inpID].enableDatePicker(); },
                // Set the cursor date
                setCursorDate:          function(inpID, yyyymmdd) { if(!inpID || !(inpID in datePickers)) return false; datePickers[inpID].setCursorDate(dateToYYYYMMDD(yyyymmdd)); },
                // Whats the currently selected date
                getSelectedDate:        function(inpID) { return (!inpID || !(inpID in datePickers)) ? false : datePickers[inpID].returnSelectedDate(); },
                // Attempt to update the language (causes a redraw of all datepickers on the page)
                loadLanguage:           function() { loadLanguage(); },
                // Set the debug level i.e. throw errors or fail silently
                setDebug:               function(dbg) { debug = !!(dbg); },
                // Converts Date Object to a YYYYMMDD formatted String
                dateToYYYYMMDDStr:      function(dt) { return dateToYYYYMMDD(dt); }                                                            
        }; 
})();