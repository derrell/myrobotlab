// FIXME - must be an easy way to include full gui & debug 

//-------WebGUIGUI begin---------

function WebGUIGUI(name) {
	ServiceGUI.call(this, name); // call super constructor.
}

WebGUIGUI.prototype = Object.create(ServiceGUI.prototype);
WebGUIGUI.prototype.constructor = WebGUIGUI;

// --- callbacks begin ---
WebGUIGUI.prototype.getState = function(data) {
	n = this.name;
	$("#"+n+"-httpPort").val(data[0].httpPort);
	$("#"+n+"-wsPort").val(data[0].wsPort);
};
//--- callbacks end ---

// --- overrides begin ---
WebGUIGUI.prototype.attachGUI = function() {
	this.subscribe("publishState", "getState");
	// broadcast the initial state
	this.send("broadcastState");
};

WebGUIGUI.prototype.detachGUI = function() {
	this.unsubscribe("publishState", "getState");
};

WebGUIGUI.prototype.init = function() {	
	$("#"+this.name+"-setPorts").button().click(WebGUIGUI.prototype.setPorts);

};
// --- overrides end ---

// --- gui events begin ---
WebGUIGUI.prototype.setPorts = function(event) {

	var gui = guiMap[this.name];
	var httpPort = $("#"+this.name+"-httpPort").val();
	var wsPort	 = $("#"+this.name+"-wsPort").val();
	alert(httpPort);
	// FIXME - implement
	//gui.send()
	

	gui.send("broadcastState");
}

//--- gui events end ---


WebGUIGUI.prototype.getPanel = function() {
	return "<div>"
			+ "	http port       <input class='text ui-widget-content ui-corner-all' id='"+this.name+"-httpPort' type='text' value=''/>"
			+ "	web socket port <input class='text ui-widget-content ui-corner-all' id='"+this.name+"-wsPort' type='text' value=''/>"
			+ "	<input id='"+this.name+"-setPorts' type='button' value='set'/>"
			+ "</div>";
}
