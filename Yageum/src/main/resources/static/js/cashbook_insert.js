let selectedType = '';
       let selectedCategory = '';

       // 유형 선택 함수
	function selectType(type) {
	    selectedType = type;
	    const typeValue = type === 'income' ? 0 : 1;
	    document.getElementById('expenseType').value = typeValue;

	    // 버튼 스타일 처리
	    document.querySelectorAll('.type-btn').forEach(btn => btn.classList.remove('active'));
	    event.target.classList.add('active');
	    removeError('type');
	}

	function updateMethod2() {
	    const method1 = document.getElementById('method1').value;
	    const method2 = document.getElementById('method2');

	    method2.innerHTML = '<option value="">상세 선택</option>';
	    method2.disabled = true;
		method2.removeAttribute("required"); 

	    if (method1 === "1" || method1 === "2") {
	        fetch(`/cashbook/cards/byMethod/${method1}`)
	            .then(res => res.json())
	            .then(data => {
					console.log("카드 응답 데이터:", data);
	                data.forEach(card => {
	                    const option = document.createElement("option");
	                    option.value = card.cardIn;
	                    option.textContent = card.cardName;
	                    method2.appendChild(option);
	                });
	                method2.disabled = false;
					method2.setAttribute("required", "required");
	            });
	    }				else if (method1 === "4") { 
			        fetch(`/cashbook/accounts`)
			            .then(res => res.json())
			            .then(data => {
			                console.log("계좌 응답 데이터:", data);
			                data.forEach(account => {
			                    const option = document.createElement("option");
			                    option.value = account.accountIn;  
			                    option.textContent = account.bankName + " " + account.accountNum;
			                    method2.appendChild(option);
			                });
			                method2.disabled = false;
							method2.setAttribute("required", "required");
			            });
			    }					else if (method1 === "3") {
					        // 현금: 선택 박스 숨기거나 그대로 비활성화 + required 제거
					        method2.disabled = true;
					        method2.removeAttribute("required");
					    }
	}

       // 에러 상태 제거
       function removeError(fieldName) {
           const formGroup = document.querySelector(`[name="${fieldName}"]`).closest('.form-group');
         if (formGroup) {
               formGroup.classList.remove('error');
           }
       }

       // 폼 검증 및 제출
       function handleSubmit(event) {
           event.preventDefault();
           
           let isValid = true;
           
		   const method1 = document.getElementById("method1").value;
		   
           // 필수 필드 검증
           const requiredFields = ['expenseSum', 'expenseType', 'expenseContent', 'method1', 'cs_in'];
           
		   // 카드, 계좌일때만 method2 검증함
		   if (method1 === "1" || method1 === "2" || method1 === "4") {
		          requiredFields.push('method2');
		      }
		   
           requiredFields.forEach(fieldName => {
               const field = document.getElementById(fieldName);
               const formGroup = field.closest('.form-group');
               
               if (!field.value.trim()) {
                   formGroup.classList.add('error');
                   isValid = false;
               } else {
                   formGroup.classList.remove('error');
               }
           });
           
           // 유형 선택 특별 검증
           if (!selectedType) {
               const typeGroup = document.querySelector('.type-buttons').closest('.form-group');
               typeGroup.classList.add('error');
               isValid = false;
           }
           
           if (isValid) {
               // 폼 데이터 수집
               const formData = new FormData(event.target);
                formData.set('expenseSum', formData.get('expenseSum').replace(/,/g, '')); //쉼표 제거
               const data = Object.fromEntries(formData);
               
               console.log('제출된 데이터:', data);
               alert('데이터가 성공적으로 저장되었습니다!');
               
              event.target.submit();
           }
       }

       // 금액 입력 시 숫자만 허용하고 포맷팅
       document.getElementById('expenseSum').addEventListener('input', function(e) {
           let value = e.target.value.replace(/[^\d]/g, '');
           if (value) {
               e.target.value = parseInt(value).toLocaleString();
           }
           removeError('expenseSum');
       });

       // 다른 필드들도 입력 시 에러 상태 제거
       document.querySelectorAll('.form-input, .form-select').forEach(element => {
           element.addEventListener('input', function() {
               removeError(this.name);
           });
       });
	
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
   
	// 로그아웃 함수
	function logout() {
	    if (confirm('로그아웃 하시겠습니까?')) {
	       window.location.href = '/logout';
	    }
	}