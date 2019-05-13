$(document).ready(function(){
	var checkVar;
	$('#connectBtnId').click(function(){
		var canSubmit = true;
		
		var wifiN = $('#wifiNameId').val();
		var pass = $('#wifiPassId').val();
		
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
			$('#connectingId').show(200);
			
			$.ajax({
				method: 'GET',
				url: '/wifi?',
				data: {wifiName : wifiN, wifiPassword : pass },
				cache: false,
				success: function(result){
					if (result === 'ping'){
						$('#connectingId').html('Verifying');
						$('#connectBtnId').attr('disabled', true);
						checkVar = setInterval(checkIntervals, 2000);
					}else if(result === 'connected'){
						$('#connectedSpinnerId').hide();
						$('#connectingId').html('<div class="alert alert-success" > Success, Connected </div>');
					}else{
						$('#connectSpinnerId').hide();
						$('#connectingId').html('<div class="alert alert-danger" > Error , please verify password </div>');
					}
				}
			});
		}		
		
	});
	
	function checkIntervals(){
		$.ajax({
				method: 'GET',
				url: '/wifi?',
				data: {check : 'check' },
				cache: false,
				success: function(result){
					if (result === 'ping'){
						$('#connectingId').html('Verifying');
					}else if(result === 'connected'){
						clearTimeout(checkVar);
						$('#connectedSpinnerId').hide();
						$('#connectingId').html('<div class="alert alert-success" > Success, Connected </div>');
						$('#connectBtnIt').removeAttr("disabled");
					}else{
						$('#connectSpinnerId').hide();
						$('#connectingId').html('<div class="alert alert-danger" > Error , please verify password </div>');
						clearTimeout(checkVar);
						$('#connectBtnIt').removeAttr("disabled");
					}
				}
			});
	}


});