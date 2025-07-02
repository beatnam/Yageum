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

$(function() {
	// 이메일 중복 체크
	$('#memberEmail').blur(function() {
		let emailVal = $('#memberEmail').val();
		
		$.ajax({
			type: "GET",
			url: '/member/emailCheck',
			data: { 'email': emailVal },
			success: function(result) {
				if (result === 'emaildup') {
					$('#memberEmailError').text('이미 사용중인 이메일입니다.').css('color', 'red');
				} else {
					$('#memberEmailError').text('사용 가능한 이메일입니다.').css('color', 'blue');
				}
			}
		});
	});
})

$(function() {
	// 전화번호 중복 체크
	$('#memberPhone').blur(function() {
		let phoneVal = $('#memberPhone').val();
		
		$.ajax({
			type: "GET",
			url: '/member/phoneCheck',
			data: { 'phone': phoneVal },
			success: function(result) {
				if (result === 'phonedup') {
					$('#memberPhoneError').text('이미 사용중인 번호입니다.').css('color', 'red');
				} else {
					$('#memberPhoneError').text('사용 가능한 번호입니다.').css('color', 'blue');
				}
			}
		});
	});
})