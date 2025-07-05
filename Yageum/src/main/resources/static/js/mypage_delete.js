function confirmWithdraw() {
		const agreement = document.getElementById('agreement');
		
		if (!agreement.checked) {
			alert('동의하셔야 탈퇴가 가능합니다.');
			return false;
		}
		
		return confirm('정말로 탈퇴하시겠습니까? 모든 데이터가 삭제됩니다.');
	}

	function goBack() {
		if (confirm('탈퇴를 취소하고 이전 페이지로 돌아가시겠습니까?')) {
			window.history.back();
		}
	}
	
	// 로그아웃 함수
	function logout() {
	    if (confirm('로그아웃 하시겠습니까?')) {
	       window.location.href = '/logout';
	    }
	}