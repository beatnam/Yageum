document.addEventListener('DOMContentLoaded', function() {
		  const form = document.querySelector('form');

		  form.addEventListener('submit', function(e) {
		    // 기존에 name 속성 제거
		    document.querySelectorAll('.account-name-input').forEach(input => {
		      input.removeAttribute('name');
		    });

		    // 체크된 항목에만 name 붙이기
		    const checked = document.querySelectorAll('input[name="selectedAccounts"]:checked');

		    checked.forEach((checkbox, i) => {
		      const li = checkbox.closest('.account-card');
		      const input = li.querySelector('.account-name-input');
		      if (input) {
		        input.setAttribute('name', 'accountNames');
		        console.log("✔ accountNames name 추가됨: ", input.value);
		      }
		    });
		  });
		});
		
		// 로그아웃 함수
		function logout() {
		    if (confirm('로그아웃 하시겠습니까?')) {
		       window.location.href = '/logout';
		    }
		}