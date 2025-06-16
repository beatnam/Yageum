//현재 비밀번호 확인 로직
	document.getElementById('currentPassword').addEventListener('input', function () {
	    const password = this.value;
	    const resultDiv = document.getElementById('currentPasswordCheck');

	    if (password.length === 0) {
	        resultDiv.textContent = '';
	        resultDiv.className = 'password-check-message';
	        return;
	    }
		
		//db에 암호화되어 저장된 비밀번호랑 같은지 
		const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
		const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

		
	    fetch('/mypage/checkPassword', {
	        method: 'POST',
	        headers: {
	            'Content-Type': 'application/json',
				[header]: token
	        },
	        body: JSON.stringify({ password: password })
	    })
	    .then(response => response.json())
	    .then(data => {
	        if (data.match === true) {
	            resultDiv.textContent = '비밀번호가 일치합니다.';
	            resultDiv.className = 'password-check-message success'; // 초록색
	        } else {
	            resultDiv.textContent = '비밀번호가 일치하지 않습니다.';
	            resultDiv.className = 'password-check-message error'; // 빨간색
	        }
	    });
	});

	
		function showError(id, message) {
			const errorDiv = document.getElementById(id + 'Error');
			if (errorDiv) {
				errorDiv.textContent = message;
				errorDiv.style.display = 'block';
			}
		}

		function hideError(id) {
			const errorDiv = document.getElementById(id + 'Error');
			if (errorDiv) {
				errorDiv.textContent = '';
				errorDiv.style.display = 'none';
			}
		}

		function validatePassword(password) {
			if (!password.trim()) {
				showError('newPassword', '비밀번호를 입력해주세요.');
				return false;
			}
			if (password.length < 8 || password.length > 20) {
				showError('newPassword', '비밀번호는 8~20자로 입력해주세요.');
				return false;
			}
			if (!/^(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*()_+{}\[\]:;<>,.?~\\/-]).+$/.test(password)) {
				showError('newPassword', '영문 대소문자, 특수문자를 각각 1개 이상 포함해야 합니다.');
				return false;
			}
			hideError('newPassword');
			return true;
		}
	
		document.getElementById("newPassword").addEventListener("input", function () {
			    const password = this.value;
			    validatePassword(password);
			});
	
	// 새 비밀번호 확인 일치 검사
		document.getElementById("confirmPassword").addEventListener("input", function () {
		    const newPassword = document.getElementById("newPassword").value;
		    const confirmPassword = this.value;
		    const matchDiv = document.getElementById("passwordMatch");

		    if (confirmPassword.length === 0) {
		        matchDiv.textContent = '';
		        matchDiv.className = 'password-match';
		        return;
		    }

		    if (newPassword === confirmPassword) {
		        matchDiv.textContent = '비밀번호가 일치합니다.';
		        matchDiv.className = 'password-match success'; // 초록색
		    } else {
		        matchDiv.textContent = '비밀번호가 일치하지 않습니다.';
		        matchDiv.className = 'password-match error'; // 빨간색
		    }
		});
	
	//전송할 때, 현재 비밀번호 올바르게 입력 안하면 alert 경고 및 저장 안됨
		document.getElementById("editForm").addEventListener("submit", function (e) {
		    const passwordCheckDiv = document.getElementById("currentPasswordCheck");
		    const passwordStatus = passwordCheckDiv.textContent.trim();
			const newPassword = document.getElementById("newPassword").value;

		    if (passwordStatus !== "비밀번호가 일치합니다.") {
		        alert("현재 비밀번호가 일치하지 않습니다.");
		        e.preventDefault(); // 전송 막기
				return;
		    } 		
			
			if (newPassword.trim().length > 0) {
				    if (!validatePassword(newPassword)) {
				        e.preventDefault(); // 유효성 검사 실패 시 전송 막기
				        return;
				    }

				    const confirmPassword = document.getElementById("confirmPassword").value;
				    if (newPassword !== confirmPassword) {
				        alert("새 비밀번호가 일치하지 않습니다.");
				        e.preventDefault();
				        return;
				    }
				}

			alert("회원정보가 수정되었습니다.");
			
		});
		
		function toggleTerms(id) {
		   const el = document.getElementById(id);
		   el.style.display = (el.style.display === 'none') ? 'block' : 'none';
		 }