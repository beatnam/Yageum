function sendLogoClick() {
    $.ajax({
        type: 'GET',
        url: '/member/logoClick', // 컨트롤러에서 GET 매핑 필요
        success: function(response) {
            confirm(response);
            window.location.href = '/cashbook/chart';
        },
        error: function(xhr, status, error) {
            console.error("에러:", error);
            alert("요청 중 오류가 발생했습니다.");
        }
    });
}