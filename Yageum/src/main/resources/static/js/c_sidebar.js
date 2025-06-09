// 메뉴 클릭 이벤트
document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', function(e) {
        e.preventDefault();
        
        // 모든 링크에서 active 클래스 제거
        document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
        
        // 클릭된 링크에 active 클래스 추가
        this.classList.add('active');
    });
});