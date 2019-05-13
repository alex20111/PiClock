

$(document).ready(function(){
	 
		var lastval = 0; 
		var check = false;
		 
		$('.selChkbox').change(function(){ 
			if($('input.selChkbox').filter(':checked').length == 1) {  
				lastval=$('input.selChkbox').filter(':checked').val();
				 check = true;
				$('input.selChkbox:not(:checked)').attr('disabled', 'disabled');  
			}else{ 
				$('input.selChkbox').removeAttr('disabled'); 
				 check = false; 
			}
			 
			$.ajax({
				method: 'GET',
				url: '/radio?',
				data: { selrad: lastval, selradChecked: check },
				cache: false
			});
		}); 
 
});