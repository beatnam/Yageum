document.addEventListener('DOMContentLoaded', function () {
  const form = document.querySelector('form');

  form.addEventListener('submit', async function (e) {
    e.preventDefault(); // 기본 제출 막기

    // 모든 name 속성 제거
    document.querySelectorAll('.account-name-input').forEach(input => {
      input.removeAttribute('name');
    });

    const checked = document.querySelectorAll('input[name="selectedAccounts"]:checked');
    const promises = [];
    let duplicateAccounts = [];

    for (const checkbox of checked) {
      const li = checkbox.closest('.account-card');
      const input = li.querySelector('.account-name-input');
      const accountValue = checkbox.value; // 예: 58251884***|국민은행
      const accountNum = accountValue.split('|')[0]; // 계좌번호만 추출

      // 중복 여부 AJAX 확인
      const p = fetch(`/openbanking/checkAccount?accountNum=${accountNum}`)
        .then(res => res.json())
        .then(data => {
          if (data.duplicate) {
            duplicateAccounts.push(accountNum);
          } else {
            if (input) {
              input.setAttribute('name', 'accountNames');
              console.log("✔ accountNames name 추가됨:", input.value);
            }
          }
        });

      promises.push(p);
    }

    // 모든 AJAX 요청이 끝날 때까지 기다림
    await Promise.all(promises);

    // 중복 계좌가 하나라도 있으면 저장하지 않음
    if (duplicateAccounts.length > 0) {
      alert(`이미 등록된 계좌입니다:\n${duplicateAccounts.join('\n')}`);
      return;
    }

    // 중복 없으면 저장 진행
    form.submit();
  });
});

// 로그아웃 함수
function logout() {
  if (confirm('로그아웃 하시겠습니까?')) {
    window.location.href = '/logout';
  }
}