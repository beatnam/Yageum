// 현재 표시중인 년도와 월
        let currentYear = new Date().getFullYear();
        let currentMonth = new Date().getMonth(); // 0부터 시작 (0 = 1월)

		// 메인 달력에 데이터 넣기 (일별 수입, 지출)
		let transactionData = {};

		function fetchTransactionData(year, month) {
		    const url = `/cashbook/monthsum?year=${year}&month=${month + 1}`;
		    
		    fetch(url)
		        .then(res => res.json())
		        .then(data => {
					//console.log("백엔드에서 받아온 데이터:", data);
		            transactionData = {}; // 초기화

		            data.forEach(item => {
		                const dateStr = item.expense_date;
		                transactionData[dateStr] = {
		                    income: item.income || 0,
		                    expense: item.expense || 0
		                };
		            });

		            generateCalendar(year, month); // 받은 후 달력 다시 그림
		        })
		        .catch(error => {
		            console.error("데이터 가져오기 실패:", error);
		        });
		}
        
        // 달력 생성 함수
        function generateCalendar(year, month) {
            const monthNames = ['1월', '2월', '3월', '4월', '5월', '6월', 
                              '7월', '8월', '9월', '10월', '11월', '12월'];
            
            // 월 표시 업데이트
            document.getElementById('currentMonth').textContent = `${year}년 ${monthNames[month]}`;
            
            // 첫 번째 날과 마지막 날 계산
            const firstDay = new Date(year, month, 1);
            const lastDay = new Date(year, month + 1, 0);
            const startDate = new Date(firstDay);
            startDate.setDate(startDate.getDate() - firstDay.getDay());
            
            const calendarBody = document.getElementById('calendarBody');
            calendarBody.innerHTML = '';
            
            const today = new Date();
            
            // 6주 생성
            for (let week = 0; week < 6; week++) {
                const row = document.createElement('tr');
                
                for (let day = 0; day < 7; day++) {
                    const cell = document.createElement('td');
                    const currentDate = new Date(startDate);
                    currentDate.setDate(startDate.getDate() + (week * 7) + day);
                    
                    const dateDiv = document.createElement('div');
                    dateDiv.className = 'date-number';
                    dateDiv.textContent = currentDate.getDate();
                    
                    // 다른 달의 날짜 스타일 적용
                    if (currentDate.getMonth() !== month) {
                        dateDiv.classList.add('other-month');
                    }
                    
                    // 오늘 날짜 표시
                    if (currentDate.toDateString() === today.toDateString()) {
                        dateDiv.classList.add('today');
                    }
					
					// 날짜 공간 클릭하면 리스트 페이지로 이동
					cell.addEventListener('click', () => {
					    const y = currentDate.getFullYear();
					    const m = (currentDate.getMonth() + 1).toString().padStart(2, '0');
					    const d = currentDate.getDate().toString().padStart(2, '0');
					    const dateStr = `${y}-${m}-${d}`;
					    
						// 현재 월의 날짜만 이동 가능
						   if (currentDate.getMonth() === month) {
						       window.location.href = `/cashbook/list?date=${dateStr}`;
						   }
					});
                    
                    cell.appendChild(dateDiv);
                    
                    // 거래 내역 표시 (현재 월의 날짜만)
                    if (currentDate.getMonth() === month) {
                       const dateKey = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-${String(currentDate.getDate()).padStart(2, '0')}`;
                        const transactions = transactionData[dateKey];

						if (transactions) {
						    const transactionDiv = document.createElement('div');
						    transactionDiv.className = 'date-transactions';

						    if (transactions.income > 0) {
						        const incomeDiv = document.createElement('div');
						        incomeDiv.className = 'transaction-item transaction-income';
						        incomeDiv.textContent = `+${transactions.income.toLocaleString()}`;
						        transactionDiv.appendChild(incomeDiv);
						    }

						    if (transactions.expense > 0) {
						        const expenseDiv = document.createElement('div');
						        expenseDiv.className = 'transaction-item transaction-expense';
						        expenseDiv.textContent = `-${transactions.expense.toLocaleString()}`;
						        transactionDiv.appendChild(expenseDiv);
						    }

						    cell.appendChild(transactionDiv);
						}
                    }
                    
                    row.appendChild(cell);
                }
                
                calendarBody.appendChild(row);
            }
            
            // 월별 요약 업데이트
            updateMonthlySummary(year, month);
        }

        // 월별 요약 업데이트 함수
        function updateMonthlySummary(year, month) {
            let totalIncome = 0;
            let totalExpense = 0;
            
            // 해당 월의 모든 거래 합계 계산
			Object.keys(transactionData).forEach(key => {
			    const [transYear, transMonth] = key.split('-').map(Number);
			    if (transYear === year && transMonth === month + 1) {
			        const transaction = transactionData[key];
			        totalIncome += transaction.income;
			        totalExpense += transaction.expense;
			    }
			});
            
            const balance = totalIncome - totalExpense;
            
            document.getElementById('monthlyIncome').textContent = 
                totalIncome > 0 ? `+${totalIncome.toLocaleString()}원` : '0원';
            document.getElementById('monthlyExpense').textContent = 
                totalExpense > 0 ? `-${totalExpense.toLocaleString()}원` : '0원';
            document.getElementById('monthlyBalance').textContent = 
                `${balance >= 0 ? '+' : ''}${balance.toLocaleString()}원`;
        }

        // 월 변경 함수
        function changeMonth(direction) {
            currentMonth += direction;
            
            if (currentMonth > 11) {
                currentMonth = 0;
                currentYear++;
            } else if (currentMonth < 0) {
                currentMonth = 11;
                currentYear--;
            }
            
            generateCalendar(currentYear, currentMonth);
        }

		
		//쿠키 get / set 함수 
		function setCookie(name, value, expirationDate) {
	           let expires = "";
	           if (expirationDate) {
	               expires = "; expires=" + expirationDate.toUTCString();
	           }
	           document.cookie = name + "=" + (value || "") + expires + "; path=/";
	       }

	       function getCookie(name) {
	           let nameEQ = name + "=";
	           let ca = document.cookie.split(';');
	           for(let i=0; i < ca.length; i++) {
	               let c = ca[i];
	               while (c.charAt(0) === ' ') c = c.substring(1, c.length);
	               if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
	           }
	           return null;
	       }

		
		
		

         // 페이지 로드 시 현재 월 달력 생성
		 document.addEventListener('DOMContentLoaded', function() {
		     fetchTransactionData(currentYear, currentMonth); // ← 이걸로 시작
//			 alert("작동");
			 const cookieName = "hideLoginPopupToday";
			 if (getCookie(cookieName)  !== 'true') {
//				alert("조건식 통과");
 		        // 이 안에 팝업을 띄우는 로직을 넣습니다.
 		        // window.open() 방식의 팝업은 window.onload로 감싸는 게 좋습니다.
 		            const uri = "/notice/main";
					setTimeout(function(){

					window.open(uri, "popup", "height=685, width=626, scrollbars=yes, resizable=yes"); // 추가 옵션 포함 예시
					}, 100);
 		        }
			 	 		       
			 
		 });
		 
					
		 
		 
		 
		 
		 
		 
		 
		 
		 
		 