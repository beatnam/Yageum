$(function() {
	    // ID 유효성 및 중복 체크
	    $('#memberId').blur(function() {
	        let idCheck = /^[a-zA-Z0-9_\-]{5,20}$/;
	        let idVal = $('#memberId').val();
	        if (!idCheck.test(idVal)) {
	            $('#memberIdError').text('잘못된 형식입니다.').css('color', 'red');
	            return;
	        }
	        $.ajax({
	            type: "GET",
	            url: '/member/idCheck',
	            data: { 'id': idVal },
	            success: function(result) {
	                if (result === 'iddup') {
	                    $('#memberIdError').text('이미 사용중인 아이디입니다.').css('color', 'red');
	                } else {
	                    $('#memberIdError').text('사용 가능한 아이디입니다.').css('color', 'blue');
	                }
	            }
	        });
	    });
	})