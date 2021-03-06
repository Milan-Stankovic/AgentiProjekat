var app = angular.module('app', []);

app.controller('klijentController', function($scope, $http, $timeout, $interval) {

	
	$scope.aclPoruke = [];
	$scope.aktivanWs = false;
	
	if ("WebSocket" in window) {
		$scope.haveWS = true;
	} else {
		$scope.haveWS = false;
	} 
	
	
	
	var getPerformative = function(){
		
		if ($scope.aktiviranWs) {
			
			var dolazniDTO = {
				"tip": "PERFORMATIVE",
				"object" : null,
				"naziv" : "",
				"tipAgenta" : ""
			}
			
			console.log("Getuje performative preko WS");
			$scope.ws.send(dolazniDTO);
		

		} else {
			console.log("Getuje performative preko RESTA");
			$http({
			  method: 'GET',
			  url: 'http://localhost:8096/AgentiProjekat/rest/agentskiCentar/message',
			}).then(function successCallback(response) {
				
				$scope.performatives = response.data;
				
			  }, function errorCallback(response) {
				  
			    console.log('Greska kod performativa');
			    
			  });
		}
	}
	
	
	
	
	var getActive = function(){
		
		if ($scope.aktivanWs) {
			
			console.log("Getuje aktivne preko WS");
			
			var dolazniDTO = {
					"tip": "TYPE",
					"object" : null,
					"naziv" : "",
					"tipAgenta" : ""
				}
				
				
				$scope.ws.send(dolazniDTO);
		} else {
			
			console.log("Getuje aktivne preko RESTA");
			
			$http({
			  method: 'GET',
			  url: 'http://localhost:8096/AgentiProjekat/rest/agentskiCentar/agents/running'
			}).then(function successCallback(response) {
				
				$scope.activeAgents = response.data;
				refresh();
				
				
				console.log("EVO DOBIO JE NESTO U RESTU : " + response.data);
				setSender(response.data);
				
				setReceivers(response.data);
				
			  }, function errorCallback(response) {
				  
				console.log('Greska kod aktivnih');
				
			  });
		}
	}
	var getTypes = function() {
		
		if ($scope.aktivanWs) {
			
			var dolazniDTO = {
					"tip": "TYPE",
					"object" : null,
					"naziv" : "",
					"tipAgenta" : ""
				}
				
				
				$scope.ws.send(dolazniDTO);
			
		} else {
			
			$http({
			  method: 'GET',
			  url: 'http://localhost:8096/AgentiProjekat/rest/agentskiCentar/agents/types'
			}).then(function successCallback(response) {
				
				$scope.tipovi = response.data;
				
				refresh();
				
			  }, function errorCallback(response) {
				  
				console.log('Greska prilikom getovanja tipova!');
			  
			  });
		}
	}
	
	// inicijalizuj podatke i startuj socket ako moze
	if ("WebSocket" in window) {
		var host = "ws://localhost:8096/AgentiProjekat/ws";
		try {
			
			$scope.ws = new WebSocket(host);
			
			
	
			$scope.ws.onopen = function() {
				
				console.log("Napravljen ws");
				
				getPerformative();
				
				getActive();
				
				getTypes();
			}
	
			$scope.ws.onmessage = function(msg) {
				
				console.log("DOBIO PORUKU");
				
				var odlazni = JSON.parse(msg.data);
				
				
				console.log(msg);
				console.log("EVO GA KAO JSON");
				console.log(JSON.stringify(odlazni));
				
				
				console.log("EVO GA KAO OBJECT KEYS");
				console.log(Object.keys(odlazni));
				
				console.log("IME : " + odlazni.ime);
				
				console.log("TIP :");
				console.log(odlazni.tip);

				console.log(odlazni.objekti);
				console.log(odlazni);
				
				switch(odlazni.tip) {
				
				case 'PERFORMATIVE':
					
					$scope.performatives = odlazni.objekti;
					
					refresh();
					
					break;
				case 'PORUKA':
					
					$scope.aclPoruke.push(odlazni.ime);
					if(!$scope.$$phase) 
						$scope.$apply($scope.aclPoruke);
					
					break;
				case 'TYPE':
					
					$scope.tipovi = odlazni.objekti;
					if(!$scope.$$phase) 
						$scope.$apply($scope.tipovi);
					
					break;
				case 'ACTIVE':
					
					console.log("ODLAZNI OBJEKTI U ACTIVE-U : ");
					console.log(odlazni.objekti);
					
					
					
					for(var i =0; i<odlazni.objekti.length; i++ ){
						var temp = {};
						temp =odlazni.objekti[i];
						odlazni.objekti[i].aid=temp;
					}
					
					$scope.activeAgents = odlazni.objekti;
					
					
					
					// $scope.$apply($scope.activeAgents);
					
					if(!$scope.$$phase) {
		            	  $scope.$apply($scope.activeAgents);
		            	 }
					
					refresh();
					
					setSender(odlazni.objekti);
					
					setReceivers(odlazni.objekti);
					
					break;
				}					
			}
	
			$scope.ws.onclose = function() {
				
				console.log("ZATVARA WS");
				
				$scope.ws = null;
			}
	
		} catch (exception) {
			console.log('Error' + exception + "\n");
		}
	} else {
		
		getPerformative();
		
		getActive();
		
		getTypes();
	}
	
	var aktiviraj = function(tip, naziv) {
		
		var dolazniDTO = {
				"tip": "UKLJUCI",
				"object" : null,
				"naziv" : naziv,
				"tipAgenta" : tip
			}
			
			
		$scope.ws.send(dolazniDTO);
		
	}
	var deaktiviraj = function(name, alias) {
		
		var dolazniDTO = {
				"tip": "ISKLJUCI",
				"object" : null,
				"naziv" : naziv,
				"tipAgenta" : alias
			}
			
			
		$scope.ws.send(dolazniDTO);
	}
    
	var sendACLMessage = function(poruka) {
		
		
		var dolazniDTO = {
				"tip": "PORUKA",
				"object" : poruka,
				"naziv" : "",
				"tipAgenta" : alias
			}
			
			
		$scope.ws.send(dolazniDTO);
		
	
	}
	

    var refresh = function() {
    	if ($scope.aktivanWs)
    		$scope.$apply();
    }
    
    $scope.activate = function(tip) {
    	
    	if (!tip.agentName || tip.agentName.length==0) {
    		
    		alert("Input agent name");
    		return;
    	}
    	
    	var name = tip.agentName;
    	
    	var obj = tip.module;
    	console.log(obj);
    	
    	if ($scope.aktivanWs) {	
    		
    		aktiviraj(tip.name, name);
    		
    		getActive();
    		
    	} else {
	    	$http({
			  method: 'PUT',
			  url: 'http://localhost:8096/AgentiProjekat/rest/agentskiCentar/agents/running/'+tip.name+'/'+name
			}).then(function successCallback(response) {

				$http({
					  method: 'GET',
					  url: 'http://localhost:8096/AgentiProjekat/rest/agentskiCentar/agents/running'
					}).then(function successCallback(response) {
						
						
						$scope.activeAgents =response.data;
						refresh();
						
						setSender(response.data);
						
						setReceivers(response.data);
						
					  }, function errorCallback(response) {
						  
						console.log('Greska kod aktivnih agenata');
						
					  });
			  }, function errorCallback(response) {
				  
				console.log('Greska kod aktiviranja agenta');
				
			  });    		    		
    	}
    };
    
    // posalji zahtev da se deaktivira odredjen agent
    $scope.deactivate = function(agent) { 
    	
    	if ($scope.aktivanWs) {
    		
    		deaktiviraj(agent.id.name, agent.id.host.alias);
    		
    		getActive();
    		
    	} else {
    		
    		$http({
  	  		  method: 'DELETE',
  	  		  url: 'http://localhost:8096/AgentiProjekat/rest/agentskiCentar/agents/running/'+agent.id.name+'/'+agent.id.host.alias
  	  		}).then(function successCallback(response) {

  	  			$http({
  					  method: 'GET',
  					  url: 'http://localhost:8096/AgentiProjekat/rest/agentskiCentar/agents/running'
  					}).then(function successCallback(response) {
  						
  						$scope.activeAgents = response.data;
  						
  						refresh();
  						
  						setSender(response.data);
  						
  					  }, function errorCallback(response) {
  						  
  						console.log('Greska kod aktivnih');
  						
  					  });
  	  		  }, function errorCallback(response) {
  	  			  
  	  			console.log('Greska kod deaktiviranja');
  	  			
  	  		});
    	}
    };
    
    $scope.send = function(selectedPerform, selectedSender, selectedReceivers, replyTo, content, language, encoding, ontology, protocol, convId, replyWith, replyBy){
    	
    	if(!selectedPerform)
    		selectedPerform="";
    	
    	if(!replyTo)
    		replyTo=null;
    	
    	
    	if(!content)
    		content="";
    	
    	if(!language)
    		language="";
    	
    	if(!encoding)
    		encoding="";
    	
    	if(!ontology)
    		ontology="";
    	
    	if(!convId)
    		convId="";
    	
    	if(!replyWith)
    		replyWith="";
    	
    	if(!replyBy)
    		replyBy="";
    	
    	if(!protocol)
    		protocol="";
    	
    	var list = [];
    	
    	console.log(selectedReceivers);

    	console.log(selectedReceivers[0]);
    	
    	console.log(selectedReceivers[0].aid);
    	
    	console.log(selectedReceivers.length)
    	
    	for(var i=0; i<selectedReceivers.length; i++){
    		console.log("TU SAM !");
    		list.push(selectedReceivers[i].aid);
    	}
    	
    	
		var poruka = {
			"performative": selectedPerform,
			"sender": selectedSender.aid,
			"receivers":  list,
			"replyTo":  replyTo,
			"content":  content,
			"language":  language,
			"encoding":  encoding,
			"ontology":  ontology,
			"protocol":  protocol,
			"conversationID":  convId,
			"replyWith":  replyWith,
			"replyBy":  replyBy
		}
		
		console.log(poruka);

		
    		
			$http({
			  method: 'POST',
			  url: 'http://localhost:8096/AgentiProjekat/rest/agentskiCentar/messages',
			  data: poruka
			}).then(function successCallback(response) {
				
				console.log("POSLAO");
				
			  }, function errorCallback(response) {
				console.log('Greska kod slanja poruke');
			  });
    	
	}
    
    $scope.startAI = function(){
    	
		$http({
		  method: 'GET',
		  url: 'http://localhost:8096/AgentiProjekat/rest/agentskiCentar/messagesAI'
		}).then(function successCallback(response) {
			
			console.log("POSLAO AI");
			
		  }, function errorCallback(response) {
			console.log('Greska kod slanja AI');
		  });
    	
    }
    
    $scope.reset = function(selectedPerform, selectedSender, selectedReceivers, replyTo, content, language, encoding, ontology, protocol, convId, replyWith, replyBy){
    	$scope.selectedPerform = undefined;
    	$scope.selectedSender = undefined;
    	$scope.selectedReceivers = undefined;
    	$scope.replyTo = undefined;
    	$scope.content = undefined;
    	$scope.language = undefined;
    	$scope.encoding = undefined;
    	$scope.ontology = undefined;
    	$scope.protocol = undefined;
    	$scope.convId = undefined;
    	$scope.replyWith = undefined;
    	$scope.replyBy = undefined;
    }



	var setReceivers = function(data) {
		
		console.log("U set receiver");
		console.log(data);
		var previous = $scope.selectedReceivers;
		var current = [];
		var temp = 0;
		$scope.receivers = data;
		
		if (previous){
			for (var i = 0; i < data.length; i++){
				for (var j = 0; j < previous.length; j++){
					if (previous[j].name == data[i].name){
						current.push(data[i]);
						temp = 1;
					}
				}
			}
			if (temp == 0){
				$scope.selectedReceivers = undefined;
			} else {
				$scope.selectedReceivers = current;
			}
		}
		
		if(!$scope.$$phase) 
      	  $scope.$apply($scope.receivers);
      	
		
		refresh();
	}
	var setSender = function(data) {
		
		console.log("U set sender");
		console.log(data);
		
		
		var temp = $scope.selectedSender;
		var t = 0;
		$scope.sender = data;
		
		if (temp){
			for (var i = 0; i < data.length; i++){
				if (temp.name == data[i].name){
					$scope.selectedSender = data[i];
					t = 1;
				}
			}
			if (t == 0){
				$scope.selectedSender = undefined;
			}
		}
		
		if(!$scope.$$phase) 
	      	  $scope.$apply($scope.sender);
	      
		
		refresh();
	}

    
  
});