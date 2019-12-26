
		 $('#activeCheckId').click(function() {
		
				$.ajax({ 
						method: 'GET', 
						url: '/alarm?', 
						data: { active: this.checked }, 
						cache: false ,
						success: function(result){
						
						if (result === 'notSaved'){
							alert("Don't forget to save the alarm to activate it");
							window.location.reload();
						}						
														
						},
						fail: function(xhr, status, error){
							 var errorMessage = xhr.status + ': ' + xhr.statusText
							 alert("Problem, cannot change status");
							
						 }
					}); 
			
		});
		
		$('#alarmTypeId').change(function() {
			var valueOfSelect = $(this).val();

			if (valueOfSelect != 0){
				$('#musicDivId').show(200);
				$('#volumeDivId').show(200);
			
				$.ajax({ 
						method: 'GET', 
						url: '/alarm?', 
						data: { fetchBuzzerList: valueOfSelect }, 
						cache: false ,
						success: function(result){							
										
							$('#musicSelectedId')
								.empty()
								.append(result);									
						},
						fail: function(xhr, status, error){
							 var errorMessage = xhr.status + ': ' + xhr.statusText
							 $('#musicSelectedId').empty();
							 alert("Problem, cannot fetch");
							
						 }
					}); 	
					
					
					
					
					
			}else if(valueOfSelect == 0){
				$('#musicDivId').hide(200);
				$('#volumeDivId').hide(200);
				$('#musicSelectedId')
								.empty();
			}
			
		});		
	 