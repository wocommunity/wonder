/**
 * @author Joey Mazzarelli
 * @website http://joey.mazzarelli.com/projects/
 * @website http://joey.mazzarelli.com/2008/11/25/easy-date-parsing-with-javascript/
 * @copyright Joey Mazzarelli
 * @license BSD license
 */

Date.fromString = (function () {

  var defaults = {
    order : 'MDY',
    strict : false
  };

  var months = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG",
      "SEP", "OCT", "NOV", "DEC"];

  var abs = ["AM", "PM", "AFTERNOON", "MORNING"];

  var mark = function (str, val) {
    var lval = val.toLowerCase();
    var regex = new RegExp('^' + lval + '|(.*[^:alpha:])' + lval, 'g');
    return str.replace(regex, '$1' + val);
  };

  var normalize = function (str) {
    str = str.toLowerCase();
    str = str.replace(/[^:a-z0-9]/g, '-');
    for (var i=0; i<months.length; i++) str = mark(str, months[i]);
    for (var i=0; i<abs.length; i++) str = mark(str, abs[i]);
    str = str.replace(/[a-z]/g, '');
    str = str.replace(/([0-9])([A-Z])/g, '$1-$2');
    str = ('-' + str + '-').replace(/-+/g, '-');
    return str;
  };

  var find_time = function (norm) {
    var obj = {date:norm, time:''};
    obj.time = norm.replace(/^.*-(\d\d?(:\d\d){1,2}(-(AM|PM))?)-.*$/, '$1');
    if (obj.time == obj.date)
      obj.time = norm.replace(/^.*-(\d\d?-(AM|PM))-.*$/, '$1');
    if (obj.time == obj.date) obj.time = '';
    obj.date = norm.replace(obj.time, '');
    obj.time = ('-' + obj.time + '-').replace(/-+/g, '-');
    obj.date = ('-' + obj.date + '-').replace(/-+/g, '-');
    return obj;
  };

  var find_year = function (norm) {
    var year = null;
    
    // Check for a 4-digit year
    year = norm.replace(/^.*-(\d\d\d\d)-.*$/, '$1');
    if (year != norm) return year; else year = null;

    // Check for a 2-digit year, over 32.
    year = norm.replace(/^.*-((3[2-9])|([4-9][0-9]))-.*$/, '$1');
    if (year != norm) return year; else year = null;

    // Check for a single 2-digit number with a leading 0
    var matches = norm.match(/-0\d-/g);
    if (matches && matches.length == 1) return matches[0].substring(1,3);
    
    // Day is always by month, so check for explicit months in 
    // first or third spot
    year = norm.replace(/^.*-[A-Z]{3}-\d\d?-(\d\d?)-.*$/, '$1');
    if (year != norm) return year; else year = null;
    year = norm.replace(/^.*-(\d\d?)-\d\d?-[A-Z]{3}-.*$/, '$1');
    if (year != norm) return year; else year = null;

    // If all else fails, use the setting for the position of the year.
    var pos = '$3';
    if (defaults.opts.order.charAt(0) == 'Y') pos = '$1';
    else if (defaults.opts.order.charAt(1) == 'Y') pos = '$2';
    year = norm.replace(/^.*-(\d\d?)-([A-Z]{3}|\d{1,2})-(\d\d?)-.*$/, pos);
    if (year != norm) return year; else year = null;

    return year;
  };

  var find_month = function (norm, year) {
    // Check for an explicity month
    var matches = norm.match(/[A-Z]{3}/);
    if (matches && matches.length) return matches[0];

    // Remove the year, and unless obviously wrong, use order
    // to chose which one to use for month.
    var parts = norm.replace(year + '-', '').split('-');
    if (parts.length != 4) return null;
    var order = defaults.opts.order;
    var md = order.indexOf('M') < order.indexOf('D')? 1: 2;
    return (parseInt(parts[md], 10) <= 12)? parts[md]: parts[md==1? 2: 1];
  };

  var find_day  = function (norm, year, month) {
    return norm.replace(year, '').replace(month, '').replace(/-/g, '');
  };

  var create_absolute = function (obj) {
    
    var time = obj.time.replace(/[-APM]/g, '');
    var parts = time.split(':');
    parts[1] = parts[1] || 0;
    parts[2] = parts[2] || 0;
    var ihr = parseInt(parts[0], 10);
    if (obj.time.match(/-AM-/) && ihr == 12) parts[0] = 0;
    else if (obj.time.match(/-PM-/) && ihr < 12) parts[0] = ihr + 12;
    parts[0] = ("0" + parts[0]).substring(("0" + parts[0]).length - 2);
    parts[1] = ("0" + parts[1]).substring(("0" + parts[1]).length - 2);
    parts[2] = ("0" + parts[2]).substring(("0" + parts[2]).length - 2);
    time = parts.join(':');

    var strict = defaults.opts.strict;
    if (!obj.year && !strict) obj.year = (new Date()).getFullYear();
    var year = parseInt(obj.year, 10);
    if (year < 100) {
      year += (year<70? 2000: 1900);
    }

    if (!obj.month && !strict) obj.month = (new Date()).getMonth() + 1;
    var month = String(obj.month);
    if (month.match(/[A-Z]{3}/)) {
      month = "JAN-FEB-MAR-APR-MAY-JUN-JUL-AUG-SEP-OCT-NOV-DEC-"
          .indexOf(month) / 4 + 1;
    }
    month = ("0" + month).substring(("0" + month).length - 2);
    if (!obj.day && !strict) obj.day = (new Date()).getDate();
    var day = ("0" + obj.day).substring(("0" + obj.day).length - 2);

    var date = new Date();
    date.setTime(Date.parse(year + '/' + month + '/' + day + ' ' + time));
    return date;
  };

  var parse = function (norm) {
    return absolute(norm);
  };

  var absolute = function (norm) {
    var obj = find_time(norm);
    obj.norm = norm;
    obj.year = find_year(obj.date);
    obj.month = find_month(obj.date, obj.year);
    obj.day = find_day(obj.date, obj.year, obj.month);
    return create_absolute(obj);
  };

  return function (fuzz, opts) {
    defaults.opts = { order: defaults.order, strict: defaults.strict };
    if (opts && opts.order) defaults.opts.order = opts.order;
    if (opts && opts.strict != undefined) defaults.opts.strict = opts.strict;
    var date = parse(normalize(fuzz));
    return date;
  };

})();

