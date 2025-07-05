function handlePasswordConfirm(event) {
		event.preventDefault();
		const password = document.getElementById('password').value;

		if (!password.trim()) {
			alert('비밀번호를 입력해주세요.');
			return;
		}

		const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
		const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

		fetch('/mypage/checkPassword', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
				[header]: token
			},
			body: JSON.stringify({ password: password }),
			credentials: 'same-origin'
		})
		.then(response => response.json())
		.then(data => {
			if (data.match) {
				// 성공하면 탈퇴 페이지로 이동
				window.location.href = '/mypage/delete';
			} else {
				alert('비밀번호가 일치하지 않습니다.');
			}
		})
		.catch(error => {
			console.error('에러 발생:', error);
			alert('비밀번호 확인 중 오류가 발생했습니다.');
		});
	}
	// 로그아웃 함수
	function logout() {
	    if (confirm('로그아웃 하시겠습니까?')) {
	       window.location.href = '/logout';
	    }
	}