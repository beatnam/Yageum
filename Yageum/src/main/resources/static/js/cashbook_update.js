// 페이지 로드되자마자 기존 expenseType 값에 따라 버튼 색상 지정
		document.addEventListener("DOMContentLoaded", () => {
		    const expenseType = document.getElementById("expenseType").value;
		    const incomeBtn = document.querySelector(".type-btn.income");
		    const expenseBtn = document.querySelector(".type-btn.expense");

		    if (expenseType === "0") {
		       incomeBtn.classList.add("active");
		    } else if (expenseType === "1") {
		        expenseBtn.classList.add("active");
		    }
		});
		
		// 유형 수정
		function selectType(type) {
		    const incomeBtn = document.querySelector(".type-btn.income");
		    const expenseBtn = document.querySelector(".type-btn.expense");
		    const expenseTypeInput = document.getElementById("expenseType");

		    if (type === 'income') {
		        incomeBtn.classList.add("active");
		        expenseBtn.classList.remove("active");
		        expenseTypeInput.value = 0;
		    } else if (type === 'expense') {
		        incomeBtn.classList.remove("active");
		        expenseBtn.classList.add("active");
		        expenseTypeInput.value = 1;
		    }
		}
		
		//수단 관련
		document.addEventListener("DOMContentLoaded", function () {
		    const method1 = document.getElementById("method1").value;
			console.log("method1 값:", method1);
		    const method2 = document.getElementById("method2");
		    const selectedCardIn = document.getElementById("cardIn").value;
		    const selectedAccountIn = document.getElementById("accountIn").value;

		    if (method1 === "1" || method1 === "2") {
		        fetch(`/cashbook/cards/byMethod/${method1}`)
		            .then(res => res.json())
		            .then(data => {
						console.log("카드 데이터:", data);
						console.log("선택된 카드 ID:", selectedCardIn);
						
		                data.forEach(card => {
		                    const option = document.createElement("option");
		                    option.value = card.cardIn;
		                    option.textContent = card.cardName;
		                    if (String(card.cardIn) === selectedCardIn) {
		                        option.selected = true;
		                    }
		                    method2.appendChild(option);
		                });
		                method2.disabled = false;
		            });
		    } else if (method1 === "4") {
		        fetch(`/cashbook/accounts`)
		            .then(res => res.json())
		            .then(data => {
						console.log("계좌 데이터:", data);
						console.log("선택된 계좌 ID:", selectedAccountIn);
						
		                data.forEach(account => {
		                    const option = document.createElement("option");
		                    option.value = account.accountIn;
		                    option.textContent = account.bankName + " " + account.accountNum;
							console.log("계좌 option ID 비교: ", account.accountIn, selectedAccountIn);
		                    if (String(account.accountIn) === selectedAccountIn) {
		                        option.selected = true;
		                    }
		                    method2.appendChild(option);
		                });
		                method2.disabled = false;
		            });
		    }
		});
		
		
		// 수단 method2 나오게함
		function updateMethod2() {
		    const method1 = document.getElementById('method1').value;
		    const method2 = document.getElementById('method2');
		    const selectedCard = /*[[${expense.cardIn}]]*/ 0;
		    const selectedAccount = /*[[${expense.accountIn}]]*/ 0;

		    method2.innerHTML = '<option value="">상세 선택</option>';
		    method2.disabled = true;
		    method2.removeAttribute("required");

		    if (method1 === "1" || method1 === "2") {
		        fetch(`/cashbook/cards/byMethod/${method1}`)
		            .then(res => res.json())
		            .then(data => {
		                data.forEach(card => {
		                    const option = document.createElement("option");
		                    option.value = card.cardIn;
		                    option.textContent = card.cardName;
		                    if (card.cardIn === selectedCard) {
		                        option.selected = true;
		                    }
		                    method2.appendChild(option);
		                });
		                method2.disabled = false;
		            });
		    } else if (method1 === "4") {
		        fetch(`/cashbook/accounts`)
		            .then(res => res.json())
		            .then(data => {
		                data.forEach(account => {
		                    const option = document.createElement("option");
		                    option.value = account.accountIn;
		                    option.textContent = account.bankName + " " + account.accountNum;
		                    if (account.accountIn === selectedAccount) {
		                        option.selected = true;
		                    }
		                    method2.appendChild(option);
		                });
		                method2.disabled = false;
		            });
		    }
		}
		
		//카테고리 모달
			function openCategoryModal() {
			  document.getElementById("categoryModal").style.display = "flex";
			}

			function closeCategoryModal() {
			  document.getElementById("categoryModal").style.display = "none";
			}

			// 대분류 클릭 시 → Ajax로 소분류 로딩
			function loadSubCategories(element) {
				const cmIn = element.getAttribute("data-cmin");
				
				console.log("cmIn 확인:", cmIn);
				fetch(`/cashbook/category/${cmIn}`)
			    .then(res => res.json())
			    .then(subs => {
			      const subList = document.getElementById("subCategoryList");
			      subList.innerHTML = "";
				  subs.forEach(sub => {
				    const li = document.createElement("li");
				    li.textContent = sub.csName;
				    li.onclick = () => selectCategory(sub.csIn, element.textContent, sub.csName);
				    subList.appendChild(li);
				  });
			    });
			}

			// 소분류 선택 시 처리
			function selectCategory(csIn, mainText, subText) {
			  document.getElementById("cs_in").value = csIn;
			  document.getElementById("categoryDisplay").textContent = `${mainText} > ${subText}`;
			  document.getElementById("categoryDisplay").style.color = "#333";
			  closeCategoryModal();
			}
			
			// 수단 저장
			document.getElementById("btn").addEventListener("click", function () {
			    const method1 = document.getElementById("method1").value;
			    const method2 = document.getElementById("method2").value;

			    if (method1 === "1" || method1 === "2") {
			        document.getElementById("cardIn").value = method2;
			        document.getElementById("accountIn").value = 0;
			    } else if (method1 === "4") {
			        document.getElementById("accountIn").value = method2;
			        document.getElementById("cardIn").value = 0;
			    }
			});
		   
			// 로그아웃 함수
			function logout() {
			    if (confirm('로그아웃 하시겠습니까?')) {
			       window.location.href = '/logout';
			    }
			}