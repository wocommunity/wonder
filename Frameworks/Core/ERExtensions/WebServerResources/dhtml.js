var DECMD_BOLD =                      5000;
var DECMD_COPY =                      5002;
var DECMD_CUT =                       5003;
var DECMD_DELETE =                    5004;
var DECMD_DELETECELLS =               5005;
var DECMD_DELETECOLS =                5006;
var DECMD_DELETEROWS =                5007;
var DECMD_FINDTEXT =                  5008;
var DECMD_FONT =                      5009;
var DECMD_GETBACKCOLOR =              5010;
var DECMD_GETBLOCKFMT =               5011;
var DECMD_GETBLOCKFMTNAMES =          5012;
var DECMD_GETFONTNAME =               5013;
var DECMD_GETFONTSIZE =               5014;
var DECMD_GETFORECOLOR =              5015;
var DECMD_HYPERLINK =                 5016;
var DECMD_IMAGE =                     5017;
var DECMD_INDENT =                    5018;
var DECMD_INSERTCELL =                5019;
var DECMD_INSERTCOL =                 5020;
var DECMD_INSERTROW =                 5021;
var DECMD_INSERTTABLE =               5022;
var DECMD_ITALIC =                    5023;
var DECMD_JUSTIFYCENTER =             5024;
var DECMD_JUSTIFYLEFT =               5025;
var DECMD_JUSTIFYRIGHT =              5026;
var DECMD_LOCK_ELEMENT =              5027;
var DECMD_MAKE_ABSOLUTE =             5028;
var DECMD_MERGECELLS =                5029;
var DECMD_ORDERLIST =                 5030;
var DECMD_OUTDENT =                   5031;
var DECMD_PASTE =                     5032;
var DECMD_REDO =                      5033;
var DECMD_REMOVEFORMAT =              5034;
var DECMD_SELECTALL =                 5035;
var DECMD_SEND_BACKWARD =             5036;
var DECMD_BRING_FORWARD =             5037;
var DECMD_SEND_BELOW_TEXT =           5038;
var DECMD_BRING_ABOVE_TEXT =          5039;
var DECMD_SEND_TO_BACK =              5040;
var DECMD_BRING_TO_FRONT =            5041;
var DECMD_SETBACKCOLOR =              5042;
var DECMD_SETBLOCKFMT =               5043;
var DECMD_SETFONTNAME =               5044;
var DECMD_SETFONTSIZE =               5045;
var DECMD_SETFORECOLOR =              5046;
var DECMD_SPLITCELL =                 5047;
var DECMD_UNDERLINE =                 5048;
var DECMD_UNDO =                      5049;
var DECMD_UNLINK =                    5050;
var DECMD_UNORDERLIST =               5051;
var DECMD_PROPERTIES =                5052;

var OLECMDEXECOPT_DODEFAULT =         0;
var OLECMDEXECOPT_PROMPTUSER =        1;
var OLECMDEXECOPT_DONTPROMPTUSER =    2;

function dhtmlGetFormByName(formName) {return document.forms[formName];}
function dhtmlGetDHTMLByName(formName,dhtmlName) {return document.all[dhtmlName+"_dhtml"];}
function dhtmlGetTextVarByName(formName,varName) {return dhtmlGetFormByName(formName).elements[varName];}

function dhtmlCmd(formName,varName,cmd)
{
    dhtmlGetDHTMLByName(formName,varName).execCommand(cmd,false);
}

function dhtmlReplaceGlobalWithParam(text,regEx,replacement)
{
    var re=new RegExp(regEx,"i");
    while(re.test(text)) {
	text=text.replace(re,eval(replacement));
    }
    return text;
}

function dhtmlRemoveGlobal(text,regEx)
{
    var re=new RegExp(regEx,"gi");
    var tempText=text;

    text=tempText.replace(re,"");
    return text;
}

function dhtmlCheckReplace(currtext)
{
    currtext=dhtmlReplaceGlobalWithParam(currtext,"<(\\/?)strong>","'<'+RegExp.$1+'b>'");
    currtext=dhtmlReplaceGlobalWithParam(currtext,"<(\\/?)em>","'<'+RegExp.$1+'i>'");
    currtext=currtext.replace(new RegExp("<tr[^>]*>","gi"),"<tr>");
    currtext=currtext.replace(new RegExp("<td[^>]*>","gi"),"<td>");
    currtext=currtext.replace(new RegExp("<table[^>]*>","gi"),"<table border='0'>");
    currtext=dhtmlRemoveGlobal(currtext,"<\\/?span>");
    currtext=dhtmlRemoveGlobal(currtext,"<colgroup[^>]*>");
    currtext=dhtmlRemoveGlobal(currtext,"<col[^>]*>");
    currtext=dhtmlRemoveGlobal(currtext,"<\\/?font[^>]*>");
    currtext=dhtmlRemoveGlobal(currtext,"<\\/?o[^>]*>");
    currtext=dhtmlRemoveGlobal(currtext,"bgcolor=\"[^\"]*\"");
    currtext=dhtmlRemoveGlobal(currtext,"width=\"[^\"]*\"");
    currtext=dhtmlRemoveGlobal(currtext,"style=\"[^\"]*\"");
    currtext=dhtmlRemoveGlobal(currtext,"class=\"[^\"]*\"");
    currtext=dhtmlRemoveGlobal(currtext,"designtimesp = [0-9]+");
    return currtext;
}

function dhtmlSave(formName,varName)
{
    var range=dhtmlGetDHTMLByName(formName,varName).DOM.body.createTextRange();
    var aText=range.htmlText;
    aText=dhtmlCheckReplace(aText);
    dhtmlGetTextVarByName(formName,varName).value=aText;
}

function dhtmlLoad(formName,varName,mytext)
{
    dhtmlGetDHTMLByName(formName,varName).DocumentHTML='<html><head><style>body,td{color:#000000;background-color:#ffffff;font-family:Helvetica,Arial,sans-serif;font-size:11px;font-weight:normal}</style></head><body>'+mytext+'</body></html>';
}

function dhtmlSubmit(inform)
{
    var dhtmlRefs = inform.dhtmlControls;
    if(dhtmlRefs) {
	for (var control in dhtmlRefs) {
	    dhtmlSave(inform.name,control);
	}
    }
    inform.submit();
}

function dhtmlCommandCell(formName,varName,picPrefix,cmdPrefix,cmdPic,cmdValue)
{
    return '<td><a href="javascript:void(0);" onClick="dhtmlCmd('+"'"+formName+"'"+','+"'"+varName+"'"+','+cmdPrefix+cmdValue+');">'+
    '<img alt="'+picPrefix+'_'+cmdPic+'.gif" src="'+picPrefix+'_'+cmdPic+'.gif" border="0" height="20" width="21"></a></td>';
}

function dhtmlCmdBar(formName,varName,picPrefix,cmdPrefix,cmdPicArray,cmdValueArray, isLast)
{
    var out = '<table cellspacing="0" cellpadding="0" border="0" '+(isLast?"":'align="left"')+'><tr>';
    for(var i = 0; !(i >= cmdPicArray.length); i++) {
	out += dhtmlCommandCell(formName,varName,picPrefix,cmdPrefix,cmdPicArray[i],cmdValueArray[i]);
    }
    return  out += "</tr></table>";
}

function s(what)
{
    var output = '';
    var curr;
    for (var i in what) {
	output += i+ ": '" + what[i] + "', ";
    }
    return output;
}

function dhtmlGetPos(obj)
{
    var pos = new Object(), save = obj;
    pos.left = 0;
    pos.top = 0;
    pos.width = obj.offsetWidth;
    pos.height = obj.offsetHeight;

    while(obj.offsetParent) {
	if(obj.offsetParent) {
	    pos.left += obj.offsetLeft;
	    pos.top += obj.offsetTop;
	}
	obj = obj.offsetParent;
    }

    if(navigator.appVersion.indexOf("Mac")!=-1 && 0) {
	pos.left -= document.body.leftMargin - 7;
	pos.top -= document.body.topMargin - 9;
	pos.width += 4;
	pos.height += 4;
    }
    return pos;
}

function dhtmlCreate(formName,varName,baseUrl,hasToolbar)
{
    if((navigator.appName.indexOf("MSIE") >= 0 || navigator.appName.indexOf("Explorer") >= 0)
       && navigator.appVersion.indexOf("Mac") == -1 || 0)
    {
	var dhtmlName = varName + "_dhtml";
	var inform = dhtmlGetFormByName(formName);
	var out = "";
	var textVar = dhtmlGetTextVarByName(formName,varName);
	var pos = dhtmlGetPos(textVar);
	// alert(varName +"," + textVar + "," + pos.left + "," + pos.top+ "," + pos.width + "," + pos.height + ":" + textVar.left);
	var width = pos.width, height = pos.height;
	var left = pos.left, top = pos.top;
	if(baseUrl == "") {
	    baseUrl = "DHTMLcontrol";
	}
	var last = baseUrl.lastIndexOf("s.gif");
	if(last>0) {
	    baseUrl = baseUrl.substring(0,last);
	}
	//alert(baseUrl);
 // out += '<div id="'+varName+'_div'+'" style="background-color:white;position:absolute;top:'+top+'px;left:'+left+'px;width:'+width+'px;height:'+height+'px;">';
	out += '<div id="'+varName+'_div'+'" style="background-color:white;position:absolute;top:'+top+'px;left:'+left+'px;width:'+width+'px;height:'+height+'px;">';
	out += '<table cellspacing="0" cellpadding="0" border="0" width="100%">';
	if(hasToolbar != 0)
	{
	    out += '<tr ><td bgcolor="#f0f0f0">';
	    out += dhtmlCmdBar(formName,varName, baseUrl+"align","DECMD_JUSTIFY", new Array("left", "center", "right"), new Array("LEFT", "CENTER", "RIGHT"),0);
	    out += dhtmlCmdBar(formName,varName, baseUrl+"format","DECMD_", new Array("bold", "italic", "underline"), new Array("BOLD", "ITALIC", "UNDERLINE"),1);
	    out += '</td></tr>';
	}
	out += '<tr><td>';
	out += '<object classid="clsid:2D360201-FFF5-11D1-8D03-00A0C959BC0A" id="'+dhtmlName+'" width="'+width+'" height="'+(height-20)+'">';
	out += '<param name="ActivateApplets" value="0">';
	out += '<param name="ActivateActiveXControls" value="0">';
	out += '<param name="ActivateDTCs" value="-1">';
	out += '<param name="ShowDetails" value="0">';
	out += '<param name="ShowBorders" value="0">';
	out += '<param name="Appearance" value="1">';
	out += '<param name="Scrollbars" value="-1">';
	out += '<param name="ScrollbarAppearance" value="1">';
	out += '<param name="SourceCodePreservation" value="-1">';
	out += '<param name="AbsoluteDropMode" value="0">';
	out += '<param name="SnapToGrid" value="0">';
	out += '<param name="SnapToGridX" value="50">';
	out += '<param name="SnapToGridY" value="50">';
	out += '<param name="UseDivOnCarriageReturn" value="0">';
	out += '</object>';
	out += '</td></tr></table>';
	out += '</div>';

	var div = document.all['span_' + varName];
	var text = dhtmlGetTextVarByName(formName,varName).value;
	div.innerHTML += out;

	// document.writeln(out);
	if(navigator.appVersion.indexOf("Mac") == -1){
	    if(!inform.dhtmlControls) {
		inform.dhtmlControls = new Object();
	    }
	    inform.dhtmlControls[varName] = dhtmlGetDHTMLByName(formName,varName);
	    dhtmlLoad(formName,varName,text);
	    inform.onSubmit = inform.onsubmit = new Function("{dhtmlSubmit(this);};");
	}
    }
}

function dhtmlMake(formName,varName,baseUrl,hasToolbar)
{
    if(!document.body)
	return;
    if(! document.dhtmlControls) {
	document.dhtmlControls = new Object();
	if(document.body.onload) {
	    document.oldonload = document.body.onload;
	}
	document.body.onload = document.body.onLoad = new Function("{dhtmlAfterBodyLoad();};");
    }
    var params = new Object();
    params.formName = formName;
    params.varName = varName;
    params.baseUrl = baseUrl;
    params.hasToolbar = hasToolbar;
    document.dhtmlControls[varName] = params;
}

function dhtmlAfterBodyLoad(inform)
{
    if(document.oldonload) {
	document.oldonload();
    }
    var dhtmlRefs = document.dhtmlControls;
    if(dhtmlRefs) {
	for (var key in dhtmlRefs) {
	    dhtmlCreate(dhtmlRefs[key].formName,dhtmlRefs[key].varName,dhtmlRefs[key].baseUrl,dhtmlRefs[key].hasToolbar);
	}
    }
}
