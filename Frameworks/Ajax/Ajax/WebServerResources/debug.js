var debugWindow = null;
function debug(text, reverse) {
	if (debugWindow == null)
 		return;

	time = "-"; //new Date();
	if (reverse) {
		$('debug').innerHTML = time + " " + text + "<br>"+ 	$('debug').innerHTML;
		debugWindow.getContent().scrollTop=0;
	}
	else {
		$('debug').innerHTML +=  time + " " + text + "<br>";
		debugWindow.getContent().scrollTop=10000; // Far away 
	}
}

function hideDebug() {
	debugWindow.destroy();
	debugWindow = null;
}

function showDebug() {
	if (debugWindow == null) {
		debugWindow = new Window('debug_window', {className: 'dialog',width:250, height:100, right:4, bottom:42, zIndex:1000, opacity:1, showEffect: Element.show, resizable: true, title: "Debug"})
		debugWindow.getContent().innerHTML = "<style>#debug_window .dialog_content {background:#000;}</style> <div font='monaco' id='debug' style='padding:3px;color:#0F0;font-family:monaco'></div>";
	}
	debugWindow.show()
}

function clearDebug() {
	if (debugWindow == null)
 		return;
	debugWindow.innerHTML = "";
}

