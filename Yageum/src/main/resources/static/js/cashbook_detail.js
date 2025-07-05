//수정하기 버튼 누르면 수정 페이지로 넘어감
document.querySelector(".btn-edit").addEventListener("click", function () {
    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    const date = params.get("date");
    window.location.href = `/cashbook/update?id=${id}&date=${date}`;
});

// 로그아웃 함수
function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
       window.location.href = '/logout';
    }
}