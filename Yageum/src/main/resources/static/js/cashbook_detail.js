


//수정하기 버튼 누르면 수정 페이지로 넘어감
document.querySelector(".btn-edit").addEventListener("click", function () {
    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    const date = params.get("date");
    window.location.href = `/cashbook/update?id=${id}&date=${date}`;
});
