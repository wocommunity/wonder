var myHeight = 200;
var isResizable = true;

function createTarget(form, targetString) {
_target = targetString;
_colon = _target.indexOf(":");
if(_colon != -1) {
targetName = _target.substring(0,_colon);
targetArgs = _target.substring(_colon+1);
}else {
	targetName = _target;
}
if(targetName !="_self"){
  	form.target = targetName ;
	form.args = targetArgs ;
	if(form.args.indexOf("{")!=-1) {
	_args = form.args.split("{");
	form.args = _args[0];
	for(var i = 1; i < _args.length;i++) {
	_args[i] = _args[i].split("}");
	form.args += eval(_args[i][0]) + _args[i][1];
	   }
	}
	form.args = form.args.replace(/ /g,"");
	_win = window.open('',form.target,form.args);
	_win.focus();

}else{
	form.target = "_self";
	form.args = "";
}
return true;
}

function myBlurFunc() {
	alert("pipo");
	form.target = "_self";
	return true;
}