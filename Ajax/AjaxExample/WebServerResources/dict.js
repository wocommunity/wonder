
var status_interval = 300000;
var status_timeout = null;
var server_status = null;

function dictOnLoad()
{
    autoNode = document.getElementById("auto");
    resultNode = document.getElementById("result");
    wordNode = document.getElementById("word");
    databaseNode = document.getElementById("database");
    databaseNode.onclick = clickDatabase;
    databaseDescNode = document.getElementById("database_desc");
    strategyNode = document.getElementById("strategy");
    strategyNode.onclick = clickStrategy;
    strategyDescNode = document.getElementById("strategy_desc");
    matchesNode = document.getElementById("matches");
    matchesNode.onclick = clickMatch;
    matchesNode.options.length = 0;
    definitionsNode = document.getElementById("definitions");
    document.onkeyup = keyupEvent;

    try {
		/*	jsonrpc = new JSONRpcClient(jsonurl); */
		jsonrpc.dict.getDatabases(gotDatabases);
		jsonrpc.dict.getStrategies(gotStrategies);
    } catch(e) {
		displayError(e);
		return;
    }

    // Keep the session alive while the page is open
    serverStatus();
	return;
}

function displayError(e)
{
    matchesNode.options.length = 0;
    var newNode = document.createElement("div");
    definitionsNode.removeChild(definitionsNode.childNodes[0]);
    definitionsNode.appendChild(newNode);
    newNode.className = "dict_define_div";
    newNode.innerHTML = "<h2>Server error</h2><p><code>" + e + "</code>" +
	"</p><p>Try reloading the page or connecting again later.</p>"
}

function statusResponse(r, e)
{
    if(e != null) {
	server_status = null;
	displayError(e);
	return;
    }
    server_status = r;
}

function serverStatus()
{
    jsonrpc.dict.checkConnection(statusResponse);
    status_timeout = setTimeout("serverStatus()", status_interval);
}

function gotDatabases(result, e)
{
    if(e != null) return displayError(e);
    databases = result;
    databaseNode.options.length = databases.list.length + 1;
    databaseNode.options[0] = new Option("all", "*", true, true);
    for(var i=0; i < databases.list.length; i++)
	databaseNode.options[i+1] =
	    new Option(databases.list[i].database,
		       databases.list[i].database, false, false);
}

function gotStrategies(result, e)
{
    if(e != null) return displayError(e);
    strategies = result;
    strategyNode.options.length = strategies.list.length + 1;
    strategyNode.options[0] = new Option("default", ".", true, true);
    for(var i=0; i < strategies.list.length; i++)
	strategyNode.options[i+1] =
	    new Option(strategies.list[i].strategy,
		       strategies.list[i].strategy, false, false);
}

function keyupEvent(evt)
{
    evt = (evt) ? evt : event;
    var target_id;
    if(evt.target && evt.target.id)
	target_id = evt.target.id;
    else if(evt.srcElement && evt.srcElement.id)
	target_id = evt.srcElement.id; // IE

    if(evt.keyCode == 13 || evt.charCode == 13) {
	if(target_id == "lookup" || target_id == "word") {
	    matchWord();
	} else if(target_id == "matches") {
	    wordNode.value =
		matchesNode.options[matchesNode.selectedIndex].value;
	    defineWord();
	}
	return false;
    }
    else if(autoNode.checked && target_id == "word" &&
	    wordNode.value.length >= 3) {
	matchWord();
	return false;
    }

    return true;
}

function gotMatches(matches, e)
{
    if(e != null) return displayError(e);
    matchesNode.options.length = matches.list.length;
    var selected = false;
    var currentWord = wordNode.value.toLowerCase();
    for(var i=0; i < matches.list.length; i++) {
	matchesNode.options[i] =
	    new Option(matches.list[i].word,matches.list[i].word,false,false);
	if(matches.list[i].word.toLowerCase() == currentWord && !selected) {
	    selected = matchesNode.options[i].selected = true;
	}
    }
}

function gotDefinitions(definitions, e)
{
    if(e != null) return displayError(e);

    var newNode = document.createElement("div");
    if(definitions.list.length == 0) {
	definitionsNode.removeChild(definitionsNode.childNodes[0]);
	definitionsNode.appendChild(newNode);
	return;
    }

    newNode.className = "dict_define_div";
    newNode.appendChild(document.createElement("hr"));

    for(var i=0; i < definitions.list.length; i++) {

	var db_desc;
	for(var j=0; j < databases.list.length; j++) {
	    if(databases.list[j].database == definitions.list[i].database) {
		db_desc = databases.list[j].description;
	    }
	}
	var defText = definitions.list[i].definition;
	defText = defText.replace(/\n/g, "<br>");
	defText = defText.replace(/\{([^\}]+)\}/g,
	    "<a href=\"javascript:wordLink('$1')\">$1</a>");

	var h2 = document.createElement("h2");
	h2.appendChild(document.createTextNode(definitions.list[i].word));
	var p1 = document.createElement("p");
	p1.innerHTML = defText;
	var p2 = document.createElement("p");
	p2.innerHTML = "<strong>Source: </strong><em>" + db_desc + "</em>";
	newNode.appendChild(h2);
	newNode.appendChild(p1);
	newNode.appendChild(p2);
	newNode.appendChild(document.createElement("hr"));
    }

    definitionsNode.removeChild(definitionsNode.childNodes[0]);
    definitionsNode.appendChild(newNode);
}

function matchWord(word)
{
    var database = databaseNode.options[databaseNode.selectedIndex].value;
    var strategy = strategyNode.options[strategyNode.selectedIndex].value;
    if(!word) word = wordNode.value;
    if(word != "") {
	jsonrpc.dict.matchWord(gotMatches, database, strategy, word);
	defineWord(word);
    }
}

function defineWord(word)
{
    var database = databaseNode.options[databaseNode.selectedIndex].value;
    if(!word) word = wordNode.value;
    if(word != "") {
	jsonrpc.dict.defineWord(gotDefinitions, database, word);
    }
}

function clickDatabase()
{
    var code = databaseNode.options[databaseNode.selectedIndex].value;
    var new_desc = "All";
    for(var i=0; i < databases.list.length; i++) {
	if(databases.list[i].database == code) {
	    new_desc = databases.list[i].description;
	}
    }
    databaseDescNode.removeChild(databaseDescNode.childNodes[0]);
    databaseDescNode.appendChild(document.createTextNode(new_desc));
}

function clickStrategy()
{
    var code = strategyNode.options[strategyNode.selectedIndex].value;
    var new_desc = "Default";
    for(var i=0; i < strategies.list.length; i++) {
	if(strategies.list[i].strategy == code) {
	    new_desc = strategies.list[i].description;
	}
    }
    strategyDescNode.removeChild(strategyDescNode.childNodes[0]);
    strategyDescNode.appendChild(document.createTextNode(new_desc));
}

function clickMatch()
{
    wordNode.value = matchesNode.options[matchesNode.selectedIndex].value;
    defineWord();
}

function wordLink(a)
{
    wordNode.value = a;
    defineWord();
}
