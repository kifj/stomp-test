const application = new Client();

function Client() {
	let protocol = 'ws://';
	if (window.location.protocol == 'https:') {
		protocol = 'wss://';
	}
	let path = '';
	if (window.location.pathname.length > 1) {
		path = '/' + window.location.pathname.split( '/' )[1];
	}
	if (path == '/index.html') {
	  path = '';
	}
	this.url = protocol + location.host + path + "/ws/stocks";
	this.debug = false;
}

//---------------------------------------------------------------------

Client.prototype.init = function() {
	let caller = this;
	$("#button_subscribe").click(function(e) {
		caller.subscribe($('#l_share').val());
	});
	$("#button_unsubscribe").click(function(e) {
		caller.unsubscribe($('#l_share').val());
	});
	$("#button_connect").click(function(e) {
		caller.connect();
	});	
	$("#button_disconnect").click(function(e) {
		caller.disconnect();
	});
	this.connect();
}

Client.prototype.connect = function() {
	this.statusOff();
	let caller = this;
	let connection = new WebSocket(this.url);
	$("#sidebar").fadeOut();
	
	connection.onopen = function() {
		caller.messageOn("Connected.");
		$('#button_connect').fadeOut({
			duration : 'fast'
		});
		$('#button_disconnect').fadeIn();
		$('#l_share').removeAttr('disabled');
  	};
  	connection.onclose = function(event) {
	  caller.disconnect(true);  	
  	};
  	connection.onmessage = function(e){
  		caller.onmessage(JSON.parse(e.data));
  	};
  	connection.onerror = function(event) {
  		caller.errorStatusOn("Operation not successful.")
  	};
  	this.connection = connection;
}

Client.prototype.disconnect = function(isClosed) {
	let caller = this;
	let connection = this.connection;
	if (connection) {
		if (!isClosed) {
			connection.close();
		}
		this.connection = null;
		$('#button_disconnect').fadeOut({
			duration : 'fast'
		});
		$('#button_connect').fadeIn();
		$('#l_share').attr('disabled', 'disabled');
		caller.messageOn("Disconnected.");
	}
}

Client.prototype.subscribe = function(key) {
	let connection = this.connection;
	if (connection && key) {
		this.messageOn('Subscribe ' + key);
		let data = {'command' : {'action': 'SUBSCRIBE', 'key': key}};
		connection.send(JSON.stringify(data));
		$('#l_share').val("");
	}
}

Client.prototype.unsubscribe = function(key) {
	let connection = this.connection;
	if (connection && key) {
		this.messageOn('Unsubscribe ' + key);
		let data = {'command' : {'action': 'UNSUBSCRIBE', 'key': key}};
		connection.send(JSON.stringify(data));
		$('#l_share').val("");
	}
}

Client.prototype.onmessage = function(message) {
	let quote = message.quote;
	let event = message.subscriptionEvent;
	let id;
	if (event) {
		id = "key_" + event.key.replace('.', '');
		if (event.action == 'UNSUBSCRIBE') {
			$("#" + id).remove();
		}
		if (this.debug) {
		    console.log('Received event ' + event.action  + ' for ' + event.key );
		}
	}
	if (quote) {
		id = "key_" + quote.share.key.replace('.', '');
		$("#" + id).remove();
		let row = "<tr id='" + id + "'>";
		row += "<td>" + quote.share.key + "</td>";
		row += "<td>" + quote.share.name + "</td>";
		row += "<td>" + quote.price + "</td>";
		row += "<td>" + quote.currency + "</td></tr>";
		$("#tbl_quotes").append(row);
		this.messageOn('Received quote for ' + quote.share.key + '.');
		if (this.debug) {
  		    console.log('Received quote ' + quote.share.key );
  		 }
	}
}

//---------------------------------------------------------------------

Client.prototype.messageOn = function(msg) {
	let message = $('#message');
	message[0].innerHTML = msg;
	message[0].className = 'messageOn';
}

Client.prototype.messageOff = function() {
	let message = $('#message');
	message[0].innerHTML = '';
	message[0].className = 'messageOff';
}

Client.prototype.statusOn = function(msg) {
	let status = $('#status');
	status[0].innerHTML = msg;
	status[0].className = 'statusOn';
}

Client.prototype.errorStatusOn = function(msg) {
	let status = $('#status');
	status[0].innerHTML = msg;
	status[0].className = 'errorStatusOn';
}

Client.prototype.statusOff = function() {
	let status = $('#status');
	status[0].innerHTML = '';
	status[0].className = 'statusOff';
}

Client.prototype.progressOn = function() {
	$('#progressbar').css('display', 'block');
}

Client.prototype.progressOff = function() {
	$('#progressbar').css('display', 'none');
}

//---------------------------------------------------------------------

