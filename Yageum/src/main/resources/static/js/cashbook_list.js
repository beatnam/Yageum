
//url에서 쿼리 파라미터 값 들고옴
		function getDateFromURL() {
	        const params = new URLSearchParams(window.location.search);
	        return params.get("date");
	    }

		//YYYY-MM-DD → "2025년 6월 15일"로 변환
	    function formatDateToKorean(dateStr) {
	        const [year, month, day] = dateStr.split("-");
	        return `${year}년 ${parseInt(month)}월 ${parseInt(day)}일`;
	    }

		//날짜 영역에 url에서 가지고 온 값을 날짜 형식으로 변환한대로 출력
	    document.addEventListener("DOMContentLoaded", function () {
	        const dateStr = getDateFromURL();
	        if (dateStr) {
	            const formatted = formatDateToKorean(dateStr);
	            document.getElementById("currentDate").textContent = formatted;
	        }
			
			document.querySelectorAll('.transaction-item').forEach(item => {
			    item.addEventListener('click', () => {
			        const dateStr = getDateFromURL();  // 현재 날짜 쿼리
					const expenseId = item.dataset.id;
					window.location.href = `/cashbook/detail?id=${expenseId}&date=${dateStr}`;
			    });
			});
			
	    });   
  
	
		function previousDay() {
		    const currentDate = getDateFromURL();
		    if (currentDate) {
		        const date = new Date(currentDate);
		        date.setDate(date.getDate() - 1);
		        const newDateStr = formatDateForURL(date);
		        window.location.href = `/cashbook/list?date=${newDateStr}`;
		    }
		}

		function nextDay() {
		    const currentDate = getDateFromURL();
		    if (currentDate) {
		        const date = new Date(currentDate);
		        date.setDate(date.getDate() + 1);
		        const newDateStr = formatDateForURL(date);
		        window.location.href = `/cashbook/list?date=${newDateStr}`;
		    }
		}

		// YYYY-MM-DD 형태로 포맷
		function formatDateForURL(date) {
		    const year = date.getFullYear();
		    const month = (`0${date.getMonth() + 1}`).slice(-2);  // 1~12
		    const day = (`0${date.getDate()}`).slice(-2);         // 1~31
		    return `${year}-${month}-${day}`;
		}
		
		
			
        function logout() {
            if(confirm('로그아웃 하시겠습니까?')) {
                window.location.href = '/logout';
            }
        }