$(document).ready(function(){
	var timer = 0;

	$('#connectBtnId').click(function(){
		var canSubmit = true;
		
		var wifiN = $('#wifiNameId').val();
		var pass = $('#wifiPassId').val();
		
		timer = 0;
		
		$('#errorNameId').hide();
		$('#errorPassId').hide();
		
		//validate errors before calling
		if(wifiN === 'Select'){
			$('#errorNameId').show(200).text('Please select a Wifi');
			canSubmit = false;
		}
		if (pass.length == 0){
			$('#errorPassId').show(200).text('Please enter a password');
			canSubmit = false;
		}
		
		if (canSubmit){
			$('#connectSpinnerId').show(200);
			$('#connectingMessage').html('');//clear message field
			$('#connectingMessage').show(200);			
			$('#connectBtnId').attr('disabled', true);
			$('#disconnectBtnId').attr('disabled', true);
			
			$.ajax({
				method: 'GET',
				url: '/wifi?',
				data: {wifiName : wifiN, wifiPassword : pass },
				cache: false		
			});
			
			setTimeout(function(){
				checkIntervals();
				}, 2000 );
				
			$('#connectingMessage').html('<div class="alert alert-warning" > Attempting to connect </div>');//add connecting text
		}		
		
	});
	
	function checkIntervals(){
	timer = timer + 1;
	console.log('checkIntervals, called');
	console.log(timer);
		$.ajax({
				method: 'GET',
				url: '/wifi?',
				data: {check : 'check' },
				cache: false,
				timeout: 2000,
				success: function(result){
				console.log('in result');
				console.log(result);
						
					if(result === 'ping'){	//recall				
						setTimeout(function(){
							checkIntervals();
							}, 1000 );
					}else{
						 $('#connectingMessage').html('');//clear
						 
						 if(result === 'connected'){						 				
							$('#connectSpinnerId').hide();
							$('#connectingMessage').html('<div class="alert alert-success" > Success, Connected </div>');
						}else{
							$('#connectSpinnerId').hide();
							$('#connectingMessage').html('<div class="alert alert-danger" > Error: ' + result + ' </div>');								
						}
						$('#connectBtnId').removeAttr("disabled");
						$('#disconnectBtnId').removeAttr("disabled");
					}
					
				},
				error: function(XMLHttpRequest, textStatus, errorThrown){
					console.log('failure');
					if(timer < 5){
					console.log('calling back again');
						setTimeout(function(){
							checkIntervals();
							}, 2000 );
					}else{
						$('#connectingMessage').html('');//clear
						$('#connectingMessage').show(200).html('<div class="alert alert-danger" > Error , cannot connect. Wrong password? </div>');
						$('#connectBtnId').removeAttr("disabled");
						$('#disconnectBtnId').removeAttr("disabled");
					}
				}
			});
	}


});