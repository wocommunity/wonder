var weekend = [0,6];
var weekendColor = "#e0e0e0";
var fontface = "Verdana";
var fontsize = 2;

var gNow = new Date();
var gSelected;
var ggWinCal;
isNav = (navigator.appName.indexOf("Netscape") != -1) ? true : false;
isIE = (navigator.appName.indexOf("Microsoft") != -1) ? true : false;

// items to localize
Calendar.Months = ["January", "February", "March", "April", "May", "June",
"July", "August", "September", "October", "November", "December"];
Calendar.TodayString = "Today";
Calendar.CancelString = "Cancel";
Calendar.ClearString = "Clear";
Calendar.SundayString = "Sun";
Calendar.MondayString = "Mon";
Calendar.TuesdayString = "Tue";
Calendar.WednesdayString = "Wed";
Calendar.ThursdayString = "Thu";
Calendar.FridayString = "Fri";
Calendar.SaturdayString = "Sat";
// end of items to localize

// Non-Leap year Month days..
Calendar.DOMonth = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
// Leap year Month days..
Calendar.lDOMonth = [31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

function Calendar(p_item, p_WinCal, p_month, p_year, p_format) {
	if ((p_month == null) && (p_year == null))	return;

	if (p_WinCal == null)
		gWinCal = ggWinCal;
	else
		gWinCal = p_WinCal;
	
	if (p_month == null) {
		gMonthName = null;
		gMonth = null;
		gYearly = true;
	} else {
		gMonthName = Calendar.get_month(p_month);
		gMonth = new Number(p_month);
		gYearly = false;
	}

	gYear = p_year;
	gFormat = p_format;
	gBGColor = "white";
	gFGColor = "black";
	gTextColor = "black";
	gHeaderColor = "black";
	gReturnItem = p_item;
}

Calendar.get_month = Calendar_get_month;
Calendar.get_daysofmonth = Calendar_get_daysofmonth;
Calendar.calc_month_year = Calendar_calc_month_year;
Calendar.print = Calendar_print;

function Calendar_get_month(monthNo) {
	return Calendar.Months[monthNo];
}

function Calendar_get_daysofmonth(monthNo, p_year) {
	/* 
	Check for leap year ..
	1.Years evenly divisible by four are normally leap years, except for... 
	2.Years also evenly divisible by 100 are not leap years, except for... 
	3.Years also evenly divisible by 400 are leap years. 
	*/
	if ((p_year % 4) == 0) {
		if ((p_year % 100) == 0 && (p_year % 400) != 0)
			return Calendar.DOMonth[monthNo];
	
		return Calendar.lDOMonth[monthNo];
	} else
		return Calendar.DOMonth[monthNo];
}

function Calendar_calc_month_year(p_Month, p_Year, incr) {
	/* 
	Will return an 1-D array with 1st element being the calculated month 
	and second being the calculated year 
	after applying the month increment/decrement as specified by 'incr' parameter.
	'incr' will normally have 1/-1 to navigate thru the months.
	*/
	var ret_arr = new Array();
	
	if (incr == -1) {
		// B A C K W A R D
		if (p_Month == 0) {
			ret_arr[0] = 11;
			ret_arr[1] = parseInt(p_Year) - 1;
		}
		else {
			ret_arr[0] = parseInt(p_Month) - 1;
			ret_arr[1] = parseInt(p_Year);
		}
	} else if (incr == 1) {
		// F O R W A R D
		if (p_Month == 11) {
			ret_arr[0] = 0;
			ret_arr[1] = parseInt(p_Year) + 1;
		}
		else {
			ret_arr[0] = parseInt(p_Month) + 1;
			ret_arr[1] = parseInt(p_Year);
		}
	}
	
	return ret_arr;
}

function Calendar_print() {
	ggWinCal.print();
}

function Calendar_calc_month_year(p_Month, p_Year, incr) {
	/* 
	Will return an 1-D array with 1st element being the calculated month 
	and second being the calculated year 
	after applying the month increment/decrement as specified by 'incr' parameter.
	'incr' will normally have 1/-1 to navigate thru the months.
	*/
	var ret_arr = new Array();
	
	if (incr == -1) {
		// B A C K W A R D
		if (p_Month == 0) {
			ret_arr[0] = 11;
			ret_arr[1] = parseInt(p_Year) - 1;
		}
		else {
			ret_arr[0] = parseInt(p_Month) - 1;
			ret_arr[1] = parseInt(p_Year);
		}
	} else if (incr == 1) {
		// F O R W A R D
		if (p_Month == 11) {
			ret_arr[0] = 0;
			ret_arr[1] = parseInt(p_Year) + 1;
		}
		else {
			ret_arr[0] = parseInt(p_Month) + 1;
			ret_arr[1] = parseInt(p_Year);
		}
	}
	
	return ret_arr;
}

// This is for compatibility with Navigator 3, we have to create and discard one object before the prototype object exists.
new Calendar();

Calendar.prototype.getMonthlyCalendarCode = function() {
	var vCode = "";
	var vHeader_Code = "";
	var vData_Code = "";
	
	// Begin Table Drawing code here..
	vCode = vCode + "<table class=\"calendar\">";
	
	vHeader_Code = this.cal_header();
	vData_Code = this.cal_data();
	vCode = vCode + vHeader_Code + vData_Code;
	
	vCode = vCode + "</table>";
	return vCode;
}




Calendar.prototype.format_absolute_data = function(p_day, v_month, v_year) {
	var vData;
	var vMonth = 1 + v_month;
	vMonth = (vMonth.toString().length < 2) ? "0" + vMonth : vMonth;
	var vMon = Calendar.get_month(v_month).substr(0,3);
	var vFMon = Calendar.get_month(v_month);
	var vY4 = new String(v_year);
	var vY2 = new String(v_year.substr(2,2));
	var vDD = (p_day.toString().length < 2) ? "0" + p_day : p_day;

        vData = gFormat;
        vData = vData.replace(new RegExp("MONTH","gi"), vFMon);
        vData = vData.replace(new RegExp("MON","gi"), vMon);
        vData = vData.replace(new RegExp("MM","gi"), vMonth);
        vData = vData.replace(new RegExp("YYYY","gi"), vY4);
        vData = vData.replace(new RegExp("YY","gi"), vY2);
        vData = vData.replace(new RegExp("DD","gi"), vDD);

	return vData;
}


Calendar.prototype.show = function() {
	var vCode = "";
	
	gWinCal.document.open();
	// Setup the page...
	this.wwrite("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
	this.wwrite("<html>");
	this.wwrite("<head><title>Calendar</title>");
        this.wwrite("<STYLE type=\"text/css\">");
        this.wwrite("body, text, select, input, option, table, td { font-size: 12px; font-family: Verdana, Helvetica, Arial, sans-serif; }");
        this.wwrite("a { color: #036; text-decoration:none}");
        this.wwrite("a:hover { font-weight: bold}");
        this.wwrite("td, th { text-align: center; background-color:#fff}");
        this.wwrite("table {width:100%;border-collapse:collapse;border-spacing:0;}")
        this.wwrite("table.calendar {background-color: #e0e0e0}")
        this.wwrite("table.calendar td, table.calendar th {border: 1px solid #e0e0e0}")
		this.wwrite("th {height: 1.5em;}")
		this.wwrite(".today, .selected {font-weight:bold;}")
		this.wwrite(".today {color:red;}")
		this.wwrite(".selected {color:orange;}")
        this.wwrite(".current-date { font-weight:bold; }");
        this.wwrite(".right {text-align:right;}")
        this.wwrite(".left {text-align:left;}")
        this.wwrite(".days td, .days th { font-weight:bold; font-size:11px; width:14%;}");
        this.wwrite(".end { background-color: #e0e0e0}");
        this.wwrite(".disabled { color: #999}");
        this.wwrite("</style>");
	this.wwrite("</head>");

	this.wwrite("<body bgcolor=#ffffff>");

	// Show navigation buttons
	var prevMMYYYY = Calendar.calc_month_year(gMonth, gYear, -1);
	var prevMM = prevMMYYYY[0];
	var prevYYYY = prevMMYYYY[1];

	var nextMMYYYY = Calendar.calc_month_year(gMonth, gYear, 1);
	var nextMM = nextMMYYYY[0];
	var nextYYYY = nextMMYYYY[1];
	
	this.wwrite("<table><tr><td>");
	this.wwrite("<a class=\"nav-link\" href=\"" +
		"javascript:window.opener.CalendarBuild(" + 
		"'" + gReturnItem + "', '" + gMonth + "', '" + (parseInt(gYear)-1) + "', '" + gFormat + "'" +
		");" +
		"\">&lt;&lt;</a></td><td>");
	this.wwrite("<a class=\"nav-link\" href=\"" +
		"javascript:window.opener.CalendarBuild(" + 
		"'" + gReturnItem + "', '" + prevMM + "', '" + prevYYYY + "', '" + gFormat + "'" +
		");" +
		"\">&lt;<\/a></td><td>");
	this.wwriteA("<span class=\"current-date\">");
	this.wwriteA(gMonthName + " " + gYear);
	this.wwriteA("</span>");
	this.wwrite("</td><td>");
	//this.wwrite("<A HREF=\"javascript:window.print();\">Print</A></TD><TD ALIGN=center>");
	this.wwrite("<a class=\"nav-link\" href=\"" +
		"javascript:window.opener.CalendarBuild(" + 
		"'" + gReturnItem + "', '" + nextMM + "', '" + nextYYYY + "', '" + gFormat + "'" +
		");" +
		"\">&gt;<\/a></td><td>");
	this.wwrite("<a class=\"nav-link\" href=\"" +
		"javascript:window.opener.CalendarBuild(" + 
		"'" + gReturnItem + "', '" + gMonth + "', '" + (parseInt(gYear)+1) + "', '" + gFormat + "'" +
		");" +
		"\">&gt;&gt;<\/a></td></tr></table><br />");

	// Get the complete calendar code for the month..
 	vCode = this.getMonthlyCalendarCode();
	this.wwrite(vCode);
	this.wwrite("</font><BR>");

	this.wwrite("<table><tr><td class=\"left\"><a href=\"#\" onClick=\"self.opener.document." + gReturnItem + ".value='"+this.format_absolute_data(gNow.getDate(), gNow.getMonth(), ""+gNow.getFullYear())+"'; window.close();\">"+Calendar.TodayString+"</a></td><td><a href=\"#\" onClick=\"self.opener.document." + gReturnItem + ".value='';window.close();\" >"+Calendar.ClearString+"<\/a></td><td class=\"right\"><A HREF=\"#\" onClick=\"window.close();\" >"+Calendar.CancelString+"<\/a></td></tr></table>");
	this.wwrite("</body></html>");
	gWinCal.document.close();
}

Calendar.prototype.showY = function() {
	var vCode = "";
	var i;
	var vr, vc, vx, vy;		// Row, Column, X-coord, Y-coord
	var vxf = 285;			// X-Factor
	var vyf = 200;			// Y-Factor
	var vxm = 10;			// X-margin
	var vym;				// Y-margin
	if (isIE)	vym = 75;
	else if (isNav)	vym = 25;
	
	gWinCal.document.open();

	this.wwrite("<html>");
	this.wwrite("<head><title>Calendar</title>");
	this.wwrite("<style type='text/css'>\n<!--");
	for (i=0; i<12; i++) {
		vc = i % 3;
		if (i>=0 && i<= 2)	vr = 0;
		if (i>=3 && i<= 5)	vr = 1;
		if (i>=6 && i<= 8)	vr = 2;
		if (i>=9 && i<= 11)	vr = 3;
		
		vx = parseInt(vxf * vc) + vxm;
		vy = parseInt(vyf * vr) + vym;

		this.wwrite(".lclass" + i + " {position:absolute;top:" + vy + ";left:" + vx + ";}");
	}
	this.wwrite("-->\n</style>");
	this.wwrite("</head>");

	this.wwrite("<body " + 
		"link=\"" + gLinkColor + "\" " + 
		"vlink=\"" + gLinkColor + "\" " +
		"alink=\"" + gLinkColor + "\" " +
		"text=\"" + gTextColor + "\">");
	this.wwrite("<FONT FACE='" + fontface + "' SIZE=2><B>");
	this.wwrite("Year : " + gYear);
	this.wwrite("</B><BR>");

	// Show navigation buttons
	var prevYYYY = parseInt(gYear) - 1;
	var nextYYYY = parseInt(gYear) + 1;
	
	this.wwrite("<TABLE WIDTH='100%' BORDER=1 CELLSPACING=0 CELLPADDING=0 BGCOLOR='#e0e0e0'><TR><TD ALIGN=center>");
	this.wwrite("[<A HREF=\"" +
		"javascript:window.opener.CalendarBuild(" + 
		"'" + gReturnItem + "', null, '" + prevYYYY + "', '" + gFormat + "'" +
		");" +
		"\" alt='Prev Year'><<<\/A>]</TD><TD ALIGN=center>");
	this.wwrite("[<A HREF=\"javascript:window.print();\">Print</A>]</TD><TD ALIGN=center>");
	this.wwrite("[<A HREF=\"" +
		"javascript:window.opener.CalendarBuild(" + 
		"'" + gReturnItem + "', null, '" + nextYYYY + "', '" + gFormat + "'" +
		");" +
		"\">>><\/A>]</TD></TR></TABLE><BR>");

	// Get the complete calendar code for each month..
	var j;
	for (i=11; i>=0; i--) {
		if (isIE)
			this.wwrite("<DIV ID=\"layer" + i + "\" CLASS=\"lclass" + i + "\">");
		else if (isNav)
			this.wwrite("<LAYER ID=\"layer" + i + "\" CLASS=\"lclass" + i + "\">");

		gMonth = i;
		gMonthName = Calendar.get_month(gMonth);
		vCode = this.getMonthlyCalendarCode();
		this.wwrite(gMonthName + "/" + gYear + "<BR>");
		this.wwrite(vCode);

		if (isIE)
			this.wwrite("</DIV>");
		else if (isNav)
			this.wwrite("</LAYER>");
	}

	this.wwrite("</font><BR></body></html>");
	gWinCal.document.close();
}

Calendar.prototype.wwrite = function(wtext) {
	gWinCal.document.writeln(wtext);
}

Calendar.prototype.wwriteA = function(wtext) {
	gWinCal.document.write(wtext);
}

Calendar.prototype.cal_header = function() {
	var vCode = "";
	
	vCode = vCode + "<tr class=\"days\">";
	vCode = vCode + "<th class=\"end\" >"+Calendar.SundayString+"</th>";
	vCode = vCode + "<th>"+Calendar.MondayString+"</th>";
	vCode = vCode + "<th>"+Calendar.TuesdayString+"</th>";
	vCode = vCode + "<th>"+Calendar.WednesdayString+"</th>";
	vCode = vCode + "<th>"+Calendar.ThursdayString+"</th>";
	vCode = vCode + "<th>"+Calendar.FridayString+"</th>";
	vCode = vCode + "<th class=\"end\" >"+Calendar.SaturdayString +"</th>";
	vCode = vCode + "</tr>";
	
	return vCode;
}

Calendar.prototype.cal_data = function() {
	var vDate = new Date();
	vDate.setDate(1);
	vDate.setMonth(gMonth);
	vDate.setFullYear(gYear);

	var vFirstDay=vDate.getDay();
	var vDay=1;
	var vLastDay=Calendar.get_daysofmonth(gMonth, gYear);
	var vOnLastDay=0;
	var vCode = "";

	/*
	Get day for the 1st of the requested month/year..
	Place as many blank cells before the 1st day of the month as necessary. 
	*/

	vCode = vCode + "<tr class=\"days\">";
	for (i=0; i<vFirstDay; i++) {
		vCode = vCode + "<td"+ this.write_td_class_for_day(i) + "></td>";
	}

	// Write rest of the 1st week
	for (j=vFirstDay; j<7; j++) {
		vCode = vCode + "<td"+ this.write_td_class_for_day(i) + ">" + 
			"<a href='#' " + 
				"onClick=\"self.opener.document." + gReturnItem + ".value='" + 
				this.format_data(vDay) + 
				"';window.close();\">" + 
				this.format_day(vDay) + 
			"</a>" + 
			"</td>";
		vDay=vDay + 1;
	}
	vCode = vCode + "</tr>";

	// Write the rest of the weeks
	for (k=2; k<7; k++) {
		vCode = vCode + "<tr>";

		for (j=0; j<7; j++) {
			vCode = vCode + "<td"+ this.write_td_class_for_day(j) + ">" + 
				"<a href='#' " + 
					"onClick=\"self.opener.document." + gReturnItem + ".value='" + 
					this.format_data(vDay) + 
					"';window.close();\">" + 
				this.format_day(vDay) + 
				"</a>" + 
				"</td>";
			vDay=vDay + 1;

			if (vDay > vLastDay) {
				vOnLastDay = 1;
				break;
			}
		}

		if (j == 6)
			vCode = vCode + "</tr>";
		if (vOnLastDay == 1)
			break;
	}
	
	// Fill up the rest of last week with proper blanks, so that we get proper square blocks
	for (m=1; m<(7-j); m++) {
		if (gYearly)
			vCode = vCode + "<td"+ this.write_td_class_for_day(j+m) + "></td>";
		else
			vCode = vCode + "<td"+ this.write_td_class_for_day(j+m) + "><span class=\"disabled\">" + m + "</span></td>";
	}
	return vCode;
}

Calendar.prototype.format_day = function(vday) {
	var vNowDay = gNow.getDate();
	var vNowMonth = gNow.getMonth();
	var vNowYear = gNow.getFullYear();
	var vSelectedDay;
	var vSelectedMonth;
	var vSelectedYear;
	if(gSelected!=null){
		vSelectedDay = gSelected.getDate();
		vSelectedMonth = gSelected.getMonth();
		vSelectedYear = gSelected.getFullYear();
	}

	if (vday == vNowDay && gMonth == vNowMonth && gYear == vNowYear)
		return ("<span class=\"today\">" + vday + "</span>");
	else if(vday == vSelectedDay && gMonth == vSelectedMonth && gYear == vSelectedYear )
		return ("<span class=\"selected\">"+ vday + "</span>");
	else
		return (vday);
}

Calendar.prototype.write_weekend_string = function(vday) {
	var i;

	// Return special formatting for the weekend day.
	for (i=0; i<weekend.length; i++) {
		if (vday == weekend[i])
			return (" BGCOLOR=\"" + weekendColor + "\"");
	}
	
	return "";
}

Calendar.prototype.write_td_class_for_day = function(vday) {
	var i;

	// Return special formatting for the weekend day.
	for (i=0; i<weekend.length; i++) {
		if (vday == weekend[i])
			return (" class=\"end\"");
	}
	
	return "";
}




Calendar.prototype.format_data = function(p_day) {
    return this.format_absolute_data(p_day, gMonth, gYear); 
}


function CalendarBuild(p_item, p_month, p_year, p_format) {
	var p_WinCal = ggWinCal;
	gCal = new Calendar(p_item, p_WinCal, p_month, p_year, p_format);
	// Customize your Calendar here..
	gCal.gBGColor="white";
	gCal.gLinkColor="black";
	gCal.gTextColor="black";
	gCal.gHeaderColor="darkgreen";

	// Choose appropriate show function
	if (gCal.gYearly)	gCal.showY();
	else	gCal.show();
}

function show_calendar() {
	/* 
		p_month : 0-11 for Jan-Dec; 12 for All Months.
		p_year	: 4-digit year
		p_format: Date format (mm/dd/yyyy, dd/mm/yy, ...)
		p_item	: Return Item.
	*/
            gSelected = null;
	p_item = arguments[0];
	indexOfPoint = p_item.indexOf(".");
	formName = p_item.substring(0, indexOfPoint);
	form = document.forms[formName];
	elementName  = p_item.substring(indexOfPoint+1, p_item.length);
	element = form.elements[elementName];
	selectedDate  = element.value;
        if(!selectedDate || selectedDate.length==0){
            selectedDate = new Date();
        }
	temporaryDate = new Date(selectedDate);
	if(!isNaN(temporaryDate)){
		selectedYear = temporaryDate.getYear();
		if(selectedYear < 30)
			temporaryDate.setYear(selectedYear + 2000);
		gSelected = temporaryDate;
	}
	if (arguments[1] == null)
		if(gSelected == null)
			p_month = new String(gNow.getMonth());
		else
			p_month = new String(gSelected.getMonth());
	else
		p_month = arguments[1];
	if (arguments[2] == "" || arguments[2] == null)
		if(gSelected == null)
			p_year = new String(gNow.getFullYear().toString());
		else
			p_year = new String(gSelected.getFullYear().toString());
	else
		p_year = arguments[2];
	if (arguments[3] == null)
		p_format = "MM/DD/YYYY";
	else
		p_format = arguments[3].toUpperCase();
	vWinCal = window.open("text/html", "Calendar", 
		"width=235,height=210,status=no,resizable=no,location=no,top=200,left=200");
	vWinCal.opener = self;
	ggWinCal = vWinCal;

	CalendarBuild(p_item, p_month, p_year, p_format);
	vWinCal.focus();
}
/*
Yearly Calendar Code Starts here
*/
function show_yearly_calendar(p_item, p_year, p_format) {
	// Load the defaults..
	if (p_year == null || p_year == "")
		p_year = new String(gNow.getFullYear().toString());
	if (p_format == null || p_format == "")
		p_format = "MM/DD/YYYY";

	var vWinCal = window.open("", "Calendar", "scrollbars=yes");
	vWinCal.opener = self;
	ggWinCal = vWinCal;

	CalendarBuild(p_item, null, p_year, p_format);
}
