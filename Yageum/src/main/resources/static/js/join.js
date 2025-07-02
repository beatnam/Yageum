const validation = {
	showError(fieldId, message) {
		const errorElement = document.getElementById(fieldId + 'Error');
		const inputElement = document.getElementById(fieldId);
		errorElement.textContent = message;
		errorElement.classList.add('show');
		inputElement.classList.add('error');
		inputElement.classList.remove('success');
	},
	hideError(fieldId) {
		const errorElement = document.getElementById(fieldId + 'Error');
		const inputElement = document.getElementById(fieldId);
		errorElement.classList.remove('show');
		inputElement.classList.remove('error');
		inputElement.classList.add('success');
	},
	validateId(id) {
		if (!id.trim()) {
			this.showError('memberId', '아이디를 입력해주세요.');
			return false;
		}
		if (id.length < 4 || id.length > 20) {
			this.showError('memberId', '아이디는 4~20자로 입력해주세요.');
			return false;
		}
		if (!/^[a-zA-Z0-9_]+$/.test(id)) {
			this.showError('memberId', '아이디는 영문, 숫자, 언더바만 사용 가능합니다.');
			return false;
		}
		this.hideError('memberId');
		return true;
	},
	validateName(name) {
		if (!name.trim()) {
			this.showError('memberName', '이름을 입력해주세요.');
			return false;
		}
		if (name.length < 2 || name.length > 10) {
			this.showError('memberName', '이름은 2~10자로 입력해주세요.');
			return false;
		}
		this.hideError('memberName');
		return true;
	},
	validatePassword(password) {
		if (!password.trim()) {
			this.showError('memberPasswd', '비밀번호를 입력해주세요.');
			return false;
		}
		if (password.length < 8 || password.length > 20) {
			this.showError('memberPasswd', '비밀번호는 8~20자로 입력해주세요.');
			return false;
		}
		if (!/^(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*()_+{}\[\]:;<>,.?~\\/-]).+$/.test(password)) {
			this.showError('memberPasswd', '비밀번호는 영문 대소문자, 특수문자를 각각 1개 이상 포함해야 합니다.');
			return false;
		}
		this.hideError('memberPasswd');
		return true;
	},
	validateEmail(email) {
		if (!email.trim()) {
			this.showError('memberEmail', '이메일을 입력해주세요.');
			return false;
		}
		const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		if (!emailRegex.test(email)) {
			this.showError('memberEmail', '올바른 이메일 형식을 입력해주세요.');
			return false;
		}
		this.hideError('memberEmail');
		return true;
	},
	validatePhone(phone) {
		if (phone && !/^\d{3}-\d{4}-\d{4}$/.test(phone)) {
			this.showError('memberPhone', '올바른 휴대폰 번호 형식을 입력해주세요. (예: 010-1234-5678)');
			return false;
		}
		this.hideError('memberPhone');
		return true;
	}
};

function checkPasswordMatch() {
	const password = document.getElementById('memberPasswd').value;
	const confirmPassword = document.getElementById('memberPassWd2').value;
	const matchElement = document.getElementById('passwordMatch');

	if (!confirmPassword) {
		matchElement.textContent = '';
		matchElement.className = 'password-match';
		return;
	}

	if (password === confirmPassword) {
		matchElement.textContent = '✓ 비밀번호가 일치합니다';
		matchElement.className = 'password-match match';
		validation.hideError('memberPassWd2');
	} else {
		matchElement.textContent = '✗ 비밀번호가 일치하지 않습니다';
		matchElement.className = 'password-match no-match';
	}
}

// 이벤트 등록
document.getElementById('memberId').addEventListener('blur', e => validation.validateId(e.target.value));
document.getElementById('memberName').addEventListener('blur', e => validation.validateName(e.target.value));
document.getElementById('memberPasswd').addEventListener('blur', e => {
	validation.validatePassword(e.target.value);
	checkPasswordMatch();
});
document.getElementById('memberPasswd').addEventListener('input', checkPasswordMatch);
document.getElementById('memberPassWd2').addEventListener('input', checkPasswordMatch);
document.getElementById('memberEmail').addEventListener('blur', e => validation.validateEmail(e.target.value));
document.getElementById('memberPhone').addEventListener('blur', e => validation.validatePhone(e.target.value));

// 전화번호 자동 하이픈
document.getElementById('memberPhone').addEventListener('input', function (e) {
	let value = e.target.value.replace(/[^0-9]/g, '');
	if (value.length >= 4 && value.length <= 7) {
		value = value.replace(/(\d{3})(\d+)/, '$1-$2');
	} else if (value.length >= 8) {
		value = value.replace(/(\d{3})(\d{4})(\d+)/, '$1-$2-$3');
	}
	e.target.value = value;
});

// jQuery 중복 검사
$(function () {
	$('#memberId').blur(function () {
		let idVal = $(this).val();
		if (!/^[a-zA-Z0-9_\-]{5,20}$/.test(idVal)) {
			$('#memberIdError').text('잘못된 형식입니다.').css('color', 'red');
			return;
		}
		$.ajax({
			type: 'GET',
			url: '/member/idCheck',
			data: { id: idVal },
			success: function (result) {
				if (result === 'iddup') {
					$('#memberIdError').text('이미 사용중인 아이디입니다.').css('color', 'red');
				} else {
					$('#memberIdError').text('사용 가능한 아이디입니다.').css('color', 'blue');
				}
			}
		});
	});

	$('#memberEmail').blur(function () {
		let emailVal = $(this).val();
		$.ajax({
			type: 'GET',
			url: '/member/emailCheck',
			data: { email: emailVal },
			success: function (result) {
				if (result === 'emaildup') {
					$('#memberEmailError').text('이미 사용중인 이메일입니다.').css('color', 'red');
				} else {
					$('#memberEmailError').text('사용 가능한 이메일입니다.').css('color', 'blue');
				}
			}
		});
	});

	$('#memberPhone').blur(function () {
		let phoneVal = $(this).val();
		$.ajax({
			type: 'GET',
			url: '/member/phoneCheck',
			data: { phone: phoneVal },
			success: function (result) {
				if (result === 'phonedup') {
					$('#memberPhoneError').text('이미 사용중인 번호입니다.').css('color', 'red');
				} else {
					$('#memberPhoneError').text('사용 가능한 번호입니다.').css('color', 'blue');
				}
			}
		});
	});
});

// 최종 제출
document.getElementById('signupForm').addEventListener('submit', function (e) {
	e.preventDefault();

	const memberId = document.getElementById('memberId').value;
	const memberName = document.getElementById('memberName').value;
	const memberPasswd = document.getElementById('memberPasswd').value;
	const memberPassWd2 = document.getElementById('memberPassWd2').value;
	const memberEmail = document.getElementById('memberEmail').value;
	const memberPhone = document.getElementById('memberPhone').value;

	let isValid = true;
	if (!validation.validateId(memberId)) isValid = false;
	if (!validation.validateName(memberName)) isValid = false;
	if (!validation.validatePassword(memberPasswd)) isValid = false;
	if (!validation.validateEmail(memberEmail)) isValid = false;
	if (!validation.validatePhone(memberPhone)) isValid = false;
	if (memberPasswd !== memberPassWd2) {
		validation.showError('memberPassWd2', '비밀번호가 일치하지 않습니다.');
		isValid = false;
	}

	if (isValid) {
		alert('회원가입이 완료되었습니다!');
		this.submit();
	} else {
		document.getElementById('signupError').textContent = '입력 정보를 다시 확인해주세요.';
		document.getElementById('signupError').classList.add('show');
	}
});
