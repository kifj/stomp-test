const application = new Client();

function Client() {
	let protocol = 'ws://';
	if (window.location.protocol == 'https:') {
		protocol = 'wss://';
	}
	this.url = protocol + location.hostname + ":61614/stomp";
	this.login = "guest";
	this.passcode = "guest_12345!";
	this.stocksQueue = "jms.queue.stocksQueue";
	this.quotesTopic = "jms.topic.quotesTopic";
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
	let caller = this;
	let client = Stomp.client(this.url);
	
	if (this.debug) {
		// this allows to display debug logs directly on the web page
		client.debug = function(str) {
			$("#debug").append(str + "\n");
		};
	} else {
		$("#sidebar").fadeOut();
	}
	// the client is notified when it is connected to the server.
	let onconnect = function(frame) {
    caller.messageOn("Connected.");
		$('#button_connect').fadeOut({
			duration : 'fast'
		});
		$('#button_disconnect').fadeIn();
		$('#l_share').removeAttr('disabled');

		client.subscribe(caller.quotesTopic, function(message) {
			caller.onmessage(JSON.parse(message.body));
		});	
	};
	let onerror = function(error) {
		caller.messageOn(error.headers.message + ": " + error.body);
  		aller.disconnect();
	};

	this.stompClient = client;
	client.connect(this.login, this.passcode, onconnect, onerror);
}

Client.prototype.disconnect = function() {
	let caller = this;
	let client = this.stompClient;
	if (client) {
		this.stompClient = null;
		client.disconnect(function() {
			$('#button_disconnect').fadeOut({
				duration : 'fast'
			});
			$('#button_connect').fadeIn();
			$('#l_share').attr('disabled', 'disabled');
			caller.messageOn("Disconnected.");
		});
	}
}

Client.prototype.subscribe = function(key) {
	let client = this.stompClient;
	if (client && key) {
		this.messageOn('Subscribe ' + key);
		let data = {'command' : {'action': 'SUBSCRIBE', 'key': key}};
		client.send(this.stocksQueue, {foo: 1}, JSON.stringify(data));
		$('#l_share').val("");
	}
}

Client.prototype.unsubscribe = function(key) {
	let client = this.stompClient;
	if (client && key) {
		this.messageOn('Unsubscribe ' + key);
		let data = {'command' : {'action': 'UNSUBSCRIBE', 'key': key}};
		client.send(this.stocksQueue, {foo: 1}, JSON.stringify(data));
		$('#l_share').val("");
		let id = "key_" + key.replace('.', '');
		$("#" + id).remove();
	}
}

Client.prototype.onmessage = function(message) {
	this.messageOn('Received quotes.');
	let quote = message.quote;
	let id = "key_" + quote.share.key.replace('.', '');
	$("#" + id).remove();
	let row = "<tr id='" + id + "'>";
	row += "<td>" + quote.share.key + "</td>";
	row += "<td>" + quote.share.name + "</td>";
	row += "<td>" + quote.price + "</td>";
	row += "<td>" + quote.currency + "</td></tr>";
	$("#tbl_quotes").append(row);
	this.messageOn('Received quote for ' + quote.share.key + '.');
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

