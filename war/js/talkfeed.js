var $j = jQuery.noConflict();

var pDisp = Array();

$j(document).ready(function() {

});


function unsubscribe(id){
	$j.ajax({
			url : '/ajax/unsubscribe.htm?id=' + id,
			success : function close() {
				$j("#blog_" + id).fadeOut(600);
			}
	}
	);
	
	
}


function loadPosts(id){
	
	if (pDisp[id] == "1"){
		//close post window
		$j("#posts_" + id).slideUp(500, function(){
			pDisp[id] = "2";
		});
		
	} else {
		//post window is not open
		if (pDisp[id] == "2") {
			//post already loaded : just slide posts
			$j("#posts_" + id).slideDown(500);
			pDisp[id] = "1";
		} else {
			$j("#posts_" + id).load("/ajax/posts.htm?blogId=" + id , function(){
				pDisp[id] = "1";
				$j("#posts_" + id).slideDown(500);
			});
		}
		
	}
	
	
}


function invite(){
	$j("#invitation").load("/ajax/invite.htm");
}