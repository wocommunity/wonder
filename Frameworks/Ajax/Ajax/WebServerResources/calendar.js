/*
  Lightweight JavaScript calendar control v1.1.0
  
  USAGE:
    Call calendar_open(input_element,options)) from text input element onfocus
    and onclick events (see calendar_example.html for example usage).

      <input type="text" value="%d %B %Y"
        onfocus="this.select();calendar_open(this, {format:'%d %B %Y'})"
        onclick="event.cancelBubble=true;this.select(); calendar_open(this,{format:'%d %B %Y'})"
      />

    Optional options argument object has format and images_dir properties:

      format: specifies date display format using  %d, %e, %b, %m, %B, %y, %Y
              date format specifiers (default is '%d %b %Y').

      images_dir: calendar prev, next images directory (default is '.').

      month_names: array of 12 month names (defaults to english).

      day_names: array of day names (defaults to english).

  REQUIRES:
    calendar.css (calendar stylesheet)
    calendar_prev.png, calendar_next.png (next/prev month button images)

  WEBPAGE:
    http://www.methods.co.nz/rails_date_kit/rails_date_kit.html

  AUTHOR:  
    Stuart Rackham <srackham@methods.co.nz>
    Chuck Hill: minor extensions and Wonder compatability
    Fredrik Lindren: added start day

  LICENSE: 
    This source code is released under the MIT license.

  HISTORY
    See CHANGELOG

    Inspired by Swazz Javascript Calendar v 1.0 by Oliver Bryant
    (http://calendar.swazz.org). The look and feel is a blatant copy of the
    Swazz Calendar but the underlying code has been completely rewritten by
    Stuart Rackham to make it play nicely with Rails along with some additional
    features (see calendar.README file).

*/

// Hide functions inside anonymous namespace.
(function() {

var PREV_MONTH_IMAGE = 'calendar_prev.png';
var NEXT_MONTH_IMAGE = 'calendar_next.png';
var PREV_YEAR_IMAGE = 'calendar_prev.png';
var NEXT_YEAR_IMAGE = 'calendar_next.png';

function get_element(id)
{
  return document.getElementById(id);
}

function is_child(el, ancestor) {
  while(el) {
    if (el == ancestor) 
      return true;
    el = el.parentNode;
  }
  return false;
}

// Because IE does not support DOM addEventListener.
function add_event(evt,fn) {
  document.all ? document.attachEvent('on'+evt, fn)
               : document.addEventListener(evt, fn, false);
}

// Global click event handler.
// If we've clicked outside the calendar then hide it.
function calendar_hide_check(evt) {
  if (calendar.element && !is_child(event_target(evt), calendar.element)) {
    calendar_hide();
  }
}

// CH: new method start
/* Fire the onChange event of the text input that we are setting so that it plays nice with
   AjaxObserveField etc. that use this notification.  */
function fireOnChangeEvent() 
{
	var evt;
	var el = calendar.input_element;
	if (document.createEvent) {
		evt = document.createEvent("HTMLEvents");
		if (evt.initEvent) {
			evt.initEvent("change", false, false);
		}
		else {
			evt = false;
		}
		el.dispatchEvent(evt);
	}
	else {
		if(document.createEventObject) {
			var evt=document.createEventObject();
			el.fireEvent("onChange", evt);
		}
	}
}
// CH: new method done

// Input control keypress handler.
function input_keypress(evt) {
  calendar_hide();
  return true;
}

// Gets the [x,y] position on the page of the element.
function get_xy(el) {
  var result = [document.viewport.getScrollOffsets().left - $(el).cumulativeScrollOffset().left, document.viewport.getScrollOffsets().top - $(el).cumulativeScrollOffset().top]; 
  while (el) {
    result[0] += el.offsetLeft;
	result[1] += el.offsetTop;

    // CH: If you use position:relative on a span around an input, Safari copies the offset values
    //     to the contained input resulting in the calendar popup being offset to the right and bottom 
    //     To work around this, detect this situation and skip the containing span.
	if (Prototype.Browser.WebKit && el.tagName == "INPUT" && 
	    el.offsetParent && el.offsetParent.tagName == "SPAN" && 
	    el.offsetParent.getStyle('position').toLowerCase() == 'relative') {
      el = el.offsetParent;
    }

    el = el.offsetParent;
  }
  return result;
}

// Return X coord of element.
function left(el) {
  return get_xy(el)[0];
}

// Return Y coord of element.
function top(el) {
  return get_xy(el)[1];
}
  
// Pad number or string to the left with pad character to a width of width characters.
function pad_left(value, pad_char, width) {
  var result = value + '';
  if (result.length >= width)
    return result.substr(0,width); // Return string truncated to width.
  for (var i = result.length; result.length < width; i++) {
    result = pad_char + result;
  }
  return result;
}

// Convert date to output display format.
// See date(1) man page for formatting specs.
function date_to_string(date, format) {
  if (!format) format = '%d %b %Y';  // Set default format.
  var result = format;
  
  
  result = result.replace('%e',date.getDate());
  result = result.replace('%d',pad_left(date.getDate(),'0',2));
  result = result.replace('%m',pad_left(date.getMonth()+1,'0',2));
  result = result.replace('%B',calendar.month_names[date.getMonth()]);
  result = result.replace('%b',calendar.month_names[date.getMonth()].substr(0,3));
  result = result.replace('%Y',date.getFullYear());
  result = result.replace('%y',(date.getFullYear()+'').substr(2));
  result = result.replace('%A',calendar.day_names[date.getDay()]);
  return result;
}


// CH this method is a total rewrite for Wonder
/*
  Convert input date string to date (or undefined if not a date string).
  All variations of format are supported.
*/
function string_to_date(s) {
  var dateOrder = calendar.format;
  if (!dateOrder) dateOrder = '%d %b %Y';  // Set default format.
  dateOrder = dateOrder.replace(/%A/,'');
  dateOrder = dateOrder.replace(/%[ed]/,'d');
  dateOrder = dateOrder.replace(/%[mbB]/,'M');
  dateOrder = dateOrder.replace(/%[yY]/,'yyyy');
  
  var result = Date.parseExact(s, [dateOrder]);
  if (result == 'Invalid Date') {
  	result = undefined;
  }
  return result;
}

// Set day number cell styles: 'normal', 'hover', 'selected', 'today'.
function set_day_styles(day, styles) {
  day.className = 'day_number ' + styles;
  if (styles != 'hover') day._day_styles = styles;
}

// Because IE does not pass event to event handler, get it from global event.
function event_object(evt) {
  return evt ? evt : window.event;
}

// Return the element that triggered the event.
// Handle event model idiosyncrasies
// (see also http://www.quirksmode.org/js/events_properties.html)
function event_target(evt) {
  var result;
  evt = event_object(evt);
  if (evt.target)
    result = evt.target;
  else if (evt.srcElement)      // IE
    result = evt.srcElement;
  if (result.nodeType == 3)
    result = result.parentNode; // Safari
  return result;
}

// Calendar mouseover handler.
function calendar_over(evt) {
  set_day_styles(event_target(evt), 'hover');
}

function calendar_out(evt) {
  var el = event_target(evt);
  set_day_styles(el, el._day_styles);
}

function calendar_click(evt) {
  var date = calendar.dates[event_target(evt).id.substr('calendar_day_'.length)];
  calendar.input_element.value = date_to_string(date, calendar.format);
  
  //CH: Start add eval of onDateSelect
  if(calendar.onDateSelect) {
    eval(calendar.onDateSelect);
  }
  //CH: Done add eval of onDateSelect
    
  //CH: Start add JS firing of onChange for text input
  if (calendar.fireEvent) {
    fireOnChangeEvent();
  }
  //CH: Done add JS firing of onChange for text input
    
  calendar_hide();
}

function is_leap_year(year) { 
  return (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) ? true : false;
}

function last_day_of_month(year,month) {
  var month_days = new Array(31,28,31,30,31,30,31,31,30,31,30,31);
  var result = month_days[month]
  if (is_leap_year(year) && month == 1)
    result++;
  return result
}

// Return td element for cell number n (1..42).
function day_cell(n) {
  return get_element('calendar_day_'+n);
}

// Return true if dates d1 and d2 are the same.
function dates_equal(d1, d2) {
  if (!d1 || !d2)
    return false;
  else
    return d1.getDate() == d2.getDate() &&
           d1.getMonth() == d2.getMonth() &&
           d1.getFullYear() == d2.getFullYear();
}

// Advance calendar by n months (n: -11..11).
function calendar_increment(n) {
  var m = calendar.month_date.getMonth();
  var y = calendar.month_date.getFullYear();
  m += n;
  if (n<0 ? (m < 0) : (m > 11)) {
    m += n<0 ? 12 : -12;
    y += n<0 ? -1 : 1;
  }
  calendar.month_date.setFullYear(y, m, 1);
  calendar_update();
}

function calendar_next_month() {
  calendar_increment(1);
}

function calendar_prev_month() {
  calendar_increment(-1);
}

function calendar_next_year() {
  calendar_increment(12);
}

function calendar_prev_year() {
  calendar_increment(-12);
}


function calendar_show() {
  calendar.element.style.display = '';
}

function calendar_hide() {
  calendar.element.style.display = 'none';
  
  // Hide the ieShim iframe if defined
  if ($('ieShim'))
  {
    $('ieShim').style.display = 'none';  
  }
}

// Refresh the calendar table dates to month of calendar.month_date and
// highlight, if necessary, calendar.input_date and today's date. Link mouse
// handlers to each cell and update calendar.dates[].
//
function calendar_update() {
  var m = calendar.month_date.getMonth();
  var y = calendar.month_date.getFullYear();
  var day1 = calendar.month_date.getDay(); // Day of week: 0..6 of first day of the month.
  if (day1 - calendar.start_day < 0) // FL Adjust for start_day
  	day1 = 7 + day1 - calendar.start_day;
  else
  	day1 = day1 - calendar.start_day;	

  get_element('calendar_header').innerHTML = calendar.month_names[m].substr(0,3).toUpperCase()+ ' ' + y;
  get_element('calendar_prev_month').onclick = calendar_prev_month;
  get_element('calendar_next_month').onclick = calendar_next_month;
  if (calendar.showYearControls) {
  	get_element('calendar_prev_year').childNodes[0].style.visibility = 'visible';
  	get_element('calendar_prev_year').onclick = calendar_prev_year;
  	get_element('calendar_next_year').childNodes[0].style.visibility = 'visible';
  	get_element('calendar_next_year').onclick = calendar_next_year;
  }
  else {
  	get_element('calendar_prev_year').childNodes[0].style.visibility = 'hidden';
  	get_element('calendar_prev_year').onclick = undefined;
  	get_element('calendar_next_year').childNodes[0].style.visibility = 'hidden';
  	get_element('calendar_next_year').onclick = undefined;
  }

// Iterate through the 42 calendar date boxes.
  var hide_last_row = false;
  for(var i=1; i<=42; i++) {
    var el = day_cell(i);   // Calendar day cell.
    var styles = 'normal';
    if (i >= day1+1 && i <= day1+last_day_of_month(y,m)) {
      var d = i-day1;  // Day of month.
      var date = new Date(y,m,d);
      if (dates_equal(date, calendar.input_date))
        styles = 'selected';
      if (dates_equal(date, new Date()))
        styles += ' today';
      var dayIndex=(((i-1) % 7)+calendar.start_day) % 7; // FL Adjust for start_day
      if ( dayIndex == 0 || dayIndex == 6)
        styles += ' weekend ';
      if ( dayIndex == 0 ) // FL Added class holiday to sundays
        styles += 'holiday ';
      el.onmouseover = calendar_over;
      el.onmouseout = calendar_out;
      el.onclick = calendar_click;
      el.innerHTML = d;
      // Date value for setting input element.
      calendar.dates[i] = date;
    }
    else {
      styles = 'blank';
      el.innerHTML = '&#160;';
      el.onmouseover = null;
      el.onmouseout = null;
      el.onclick = null;
      el.style.cursor = 'default';
      // If the first cell of the last row has no date then hide the whole row.
      if (i == 36)
        hide_last_row = true;
    }
    set_day_styles(el, styles);
  }
  var rows = calendar.element.getElementsByTagName('tr');
  rows[7].style.display = hide_last_row ? 'none' : '';
}

/*
  This single public function does all the work.
  Optional options object has format and images_dir properties.
  Call it from text input element onfocus and onclick events.
  See calendar_example.html for example usage.
*/
function calendar_open(input_element, options) {
  build_calendar(input_element);  // CH: added this, see method below
  calendar.input_element = input_element;
  options = options || {};
  calendar.format = options.format;
  if (options.month_names) {
    calendar.month_names = options.month_names;
  }
  
  // CH: Start add init of new options
  if(options.onDateSelect) {
    calendar.onDateSelect = options.onDateSelect;
  }

  if(options.fireEvent == false) {
    calendar.fireEvent = false;
  }
  
  // FL Added to support diffrent start days. Defaults to 0 (Sunday).
  if (options.start_day) {
  	calendar.start_day = options.start_day;
  }

  if (options.showYearControls == false) {
  	calendar.showYearControls = false;
  }
  // CH: Done add init of new options
  
  if (options.day_names) {
    calendar.day_names = options.day_names;
  }
  // Built month numbers lookup.
  for (var i=0; i < calendar.month_names.length; i++) {
    calendar.month_numbers[calendar.month_names[i].substr(0,3).toLowerCase()] = i;
  }
  // Set day name cells.
  var day_names = calendar.element.getElementsByTagName('tr')[1].childNodes;
  for (var i=0; i < day_names.length; i++) {
  	var dayIndex=(i+calendar.start_day) % 7; // FL Adjust for start_day
    day_names[i].innerHTML = calendar.day_names[dayIndex].substr(0,3);
    if ( dayIndex == 0 || dayIndex == 6)
      day_names[i].className += ' weekend';
  }
  var images_dir = options.images_dir || '.';
  // Set up previous and next images.
  var images = calendar.element.getElementsByTagName('img');
  if (images.length == 0) {
    get_element('calendar_prev_year').appendChild(document.createElement('img'));
    get_element('calendar_prev_month').appendChild(document.createElement('img'));
    get_element('calendar_next_month').appendChild(document.createElement('img'));
    get_element('calendar_next_year').appendChild(document.createElement('img'));
    images = calendar.element.getElementsByTagName('img');
  }
  images[0].src = images_dir + '/' + PREV_YEAR_IMAGE;
  images[1].src = images_dir + '/' + PREV_MONTH_IMAGE;
  images[2].src = images_dir + '/' + NEXT_MONTH_IMAGE;
  images[3].src = images_dir + '/' + NEXT_YEAR_IMAGE;
  add_event('click', calendar_hide_check);
  input_element.onkeydown = input_keypress;  // CH use onkeydown instead of onkeypress to trap Tab key too
  
  // If a ieShim has been written to the page, determine the dimensions before display
  if ($('ieShim'))
  {
    var calDim = $('calendar_control').getDimensions();
    $('ieShim').style.left = left(input_element) + 'px';
    $('ieShim').style.top = (top(input_element) + input_element.offsetHeight) + 'px';
    $('ieShim').style.height = (calDim.height) + 'px';
    $('ieShim').style.width = (calDim.width) + 'px';
    $('ieShim').style.display = 'block';
  }
  
  // Position calendar by input element.
  calendar.element.style.left = left(input_element) + 'px';
  calendar.element.style.top = (top(input_element) + input_element.offsetHeight) + 'px';
  calendar_show();
  // Parse input date.
  var date = string_to_date(input_element.value);
  if (date) {
    calendar.input_date = date;
    calendar.month_date = new Date(date);
  }
  else {
    calendar.input_date = undefined;
    calendar.month_date = new Date;
  }
  calendar.month_date.setDate(1);
  calendar_update();
}


/*
 * Adds the calendar table after the BODY when the calendar is first opened.
 * This is a total re-write / conversion of static block to function to better support using this inside an AjaxModalDialog.
 */
function build_calendar(input_element) {
	if (get_element('calendar_control') == null) {  // CH only do this once per page or FireFox gets confused
		var firstRow;
		var tbody;
		
		// create shim to stop select boxes breaking into calendar display in IE6
		var ieShimControl = new Element("iframe", {id: "ieShim", style: "position:absolute; top:0; left:0; z-index:1; width:0px; height:0px; filter:mask();"});
				
		//create a table tag containing a tbody for IE
		var calendarControl = new Element("table",
				{id: "calendar_control", style: "position: absolute; display:none; z-index: 10001;"}
			).update(tbody = new Element("tbody", {}));
			
	
		firstRow = new Element("tr", {});
		firstRow.appendChild(new Element("td", {id: "calendar_prev_year", title: "Previous year"}));
		firstRow.appendChild(new Element("td", {id: "calendar_prev_month", title: "Previous month"}));
		firstRow.appendChild(new Element("td", {id: "calendar_header", colspan: "3"}));
		firstRow.appendChild(new Element("td", {id: "calendar_next_month", title: "Next month"}));
		firstRow.appendChild(new Element("td", {id: "calendar_next_year", title: "Next year"}));
		
		tbody.appendChild(firstRow);
		
		var secondRow = new Element("tr", {});
		tbody.appendChild(secondRow);
		for (var i=0; i < 7; i++) {
	  	  secondRow.appendChild(new Element("td", {'class': "day_letter"}));
        }
        
        for(var n=1, i=0; i<6 ;i++) {
		  var dayRow = new Element("tr", {});
		  tbody.appendChild(dayRow);
		  for(var j=0; j<7; j++,n++) {
	  		dayRow.appendChild(new Element("td", {id: "calendar_day_" + n, 'class': "day_number normal"}));
	  	  }
	  	}
	  	
	  	// Stick in a shim behind the calendar for IE6
	  	if (!(window.XMLHttpRequest))
	  	{  
	  	  $(document.body).insert({'top': ieShimControl});
	  	}
	  	
   		$(document.body).insert({'top': calendarControl});
		calendar.element = get_element('calendar_control');
		
		
	}
}


/*
  Namespace globals.
*/
calendar = {                        // Calendar properties.
  dates: new Array(6*7),            // Date values for each calendar day.
  element: undefined,				// The calendar table.  CH: lazy init
  input_element: undefined,         // Calendar input element, set by calendar_show().
  onDateSelect: undefined,          // CH: add function called when user selects a date
  fireEvent: true,					// CH: add should event listener for text field be fired upon date select?
  start_day: 0,						// FL: Start day, defaults to 0 Sunday.
  showYearControls: true,			// SP: Display the year prev and next arrows.
  input_date: undefined,            // Date value of input element, set by calendar_show().
  month_date: undefined,            // First day of calendar month.
  format: undefined,                // The date display format, set by calendar_show().
  month_names: new Array('January','February','March','April','May','June',
                         'July','August','September','October','November','December'),
  day_names: new Array('Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'),
  month_numbers: {}                 // Reverse lookup for month numbers.
}
window['calendar_open'] = calendar_open;  // The only publicly accessible function.

// Close anonymous namespace.
})();
