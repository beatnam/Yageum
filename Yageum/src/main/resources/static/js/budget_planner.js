// budget_planner.js

let incomes = [];
let expenses = [];

const csrfToken = document.querySelector('meta[name=\'_csrf\']')?.content;
const csrfHeader = document.querySelector('meta[name=\'_csrf_header\']')?.content;

if (!csrfToken || !csrfHeader) {
    console.error('CSRF token or header name is missing. AJAX requests might fail.');
    alert('보안 토큰을 찾을 수 없습니다. 페이지를 새로고침하거나 관리자에게 문의하세요.');
}

function addIncome() {
    const sourceNameInput = document.getElementById('income-source-name'); // 변경된 id 사용
    const amountInput = document.getElementById('income-amount-input');
    const name = sourceNameInput.value.trim(); // .trim()을 사용하여 앞뒤 공백 제거
    const amount = parseInt(amountInput.value);

    if (name && !isNaN(amount) && amount > 0) {
        incomes.push({ name, amount });
        updateIncomeList();
        updateTotals();
        sourceNameInput.value = ''; // 입력 필드 초기화
        amountInput.value = '';
    } else {
        alert('수입원(메모)과 유효한 금액을 입력해주세요.'); // 알림 메시지 수정
    }
}

function addExpense() {
    const categorySelect = document.getElementById('expense-category');
    const memoInput = document.getElementById('expense-memo-input'); // ⭐ 추가된 부분
    const amountInput = document.getElementById('expense-amount-input');
    const name = categorySelect.value;
    const memo = memoInput.value; // ⭐ 추가된 부분
    const amount = parseInt(amountInput.value);

    if (name && !isNaN(amount) && amount > 0) {
        expenses.push({ name, amount, memo }); // ⭐ memo 추가
        updateExpenseList();
        updateTotals();
        categorySelect.value = '';
        memoInput.value = ''; // ⭐ 메모 입력 필드 초기화
        amountInput.value = '';
    } else {
        alert('지출 카테고리와 유효한 금액을 입력해주세요.');
    }
}

function updateIncomeList() {
    const listContainer = document.getElementById('income-list');
    listContainer.innerHTML = ''; // 현재 목록 비우기

    if (incomes.length === 0) {
        const noData = document.createElement('div');
        noData.className = 'category-item no-data';
        noData.innerHTML = `<span class="category-name">등록된 수입이 없습니다</span><span class="category-amount">₩0</span>`;
        listContainer.appendChild(noData);
    } else {
        incomes.forEach((item, index) => {
            const div = document.createElement('div');
            div.className = 'category-item';

            // ⭐ 수입 메모를 표시하도록 수정
            const displayText = item.memo && item.memo.trim() !== '' ? `${item.memo}` : item.name; 
            // 만약 '카테고리 이름 (메모)' 형식으로 원하면 `item.name && item.memo && item.memo.trim() !== '' ? `${item.name} (${item.memo})` : item.name` 사용

            div.innerHTML = `
                <span class="category-name">${displayText}</span>
                <span class="category-amount" data-original-value="${item.amount}">₩${item.amount.toLocaleString('ko-KR')}</span>
                <button class="edit-btn" onclick="toggleEditMode(this, ${index}, 'income')">수정</button>
                <button class="delete-btn" onclick="removeIncome(${index})">삭제</button>
            `;
            listContainer.appendChild(div);
        });
    }
}

function updateExpenseList() {
    const listContainer = document.getElementById('expense-list');
    listContainer.innerHTML = ''; // 현재 목록 비우기

    if (expenses.length === 0) {
        const noData = document.createElement('div');
        noData.className = 'category-item no-data';
        noData.innerHTML = `<span class="category-name">등록된 예산이 없습니다</span><span class="category-amount">₩0</span>`;
        listContainer.appendChild(noData);
    } else {
        expenses.forEach((item, index) => {
            const div = document.createElement('div');
            div.className = 'category-item';

            // ⭐ 이 부분이 추가/수정되었습니다. item.memo가 있으면 함께 표시합니다.
            const displayText = item.memo && item.memo.trim() !== '' ? `${item.name} (${item.memo})` : item.name;

            div.innerHTML = `
                <span class="category-name">${displayText}</span>
                <span class="category-amount" data-original-value="${item.amount}">₩${item.amount.toLocaleString('ko-KR')}</span>
                <button class="edit-btn" onclick="toggleEditMode(this, ${index}, 'expense')">수정</button>
                <button class="delete-btn" onclick="removeExpense(${index})">삭제</button>
            `;
            listContainer.appendChild(div);
        });
    }
}


function updateIncomeAmount(index, newValue) {
    const amount = parseInt(newValue);
    if (!isNaN(amount) && amount >= 0) { // 0이상의 유효한 숫자인지 확인
        incomes[index].amount = amount;
        updateTotals(); // 총액 업데이트
        // 리스트를 다시 그릴 필요는 없지만, 금액 형식을 다시 적용하려면 호출 가능
        // updateIncomeList(); // 이 함수를 호출하면 입력 포커스가 사라지므로 주의
    } else {
        alert('유효한 수입 금액을 입력해주세요.');
        // 잘못된 값이 입력되면 이전 값으로 되돌리는 로직을 추가할 수도 있습니다.
        // 예: event.target.value = incomes[index].amount;
    }
}

function updateExpenseAmount(index, newValue) {
    const amount = parseInt(newValue);
    if (!isNaN(amount) && amount >= 0) { // 0이상의 유효한 숫자인지 확인
        expenses[index].amount = amount;
        updateTotals(); // 총액 업데이트
        // updateExpenseList(); // 이 함수를 호출하면 입력 포커스가 사라지므로 주의
    } else {
        alert('유효한 지출 금액을 입력해주세요.');
        // 예: event.target.value = expenses[index].amount;
    }
}


function updateTotals() {
    const totalIncome = incomes.reduce((sum, item) => sum + item.amount, 0);
    const totalExpense = expenses.reduce((sum, item) => sum + item.amount, 0);
    const remainingAmount = totalIncome - totalExpense;

    document.getElementById('total-income').textContent = `₩${totalIncome.toLocaleString('ko-KR')}`;
    document.getElementById('total-expense').textContent = `₩${totalExpense.toLocaleString('ko-KR')}`;
    document.getElementById('remaining-budget').textContent = `₩${remainingAmount.toLocaleString('ko-KR')}`;

    // 잔여 예산에 따라 색상 변경
    if (remainingAmount < 0) {
        document.getElementById('remaining-budget').style.color = 'red';
    } else {
        document.getElementById('remaining-budget').style.color = '#28a745'; // 초록색 또는 기본 색상
    }
}

function removeIncome(index) {
    incomes.splice(index, 1);
    updateIncomeList();
    updateTotals();
}

function removeExpense(index) {
    expenses.splice(index, 1);
    updateExpenseList();
    updateTotals();
}

function toggleEditMode(buttonElement, index, type) {
    const parentDiv = buttonElement.closest('.category-item');
    const amountDisplayElement = parentDiv.querySelector('.category-amount'); // 현재 금액을 표시하는 span 또는 input

    // input 필드가 이미 존재하면 (즉, 이미 편집 모드라면)
    if (amountDisplayElement.tagName === 'INPUT') {
        amountDisplayElement.blur(); // 강제로 blur 이벤트를 발생시켜 저장 로직 실행
        return;
    }

    const originalAmount = parseInt(amountDisplayElement.dataset.originalValue);

    // input 요소 생성
    const input = document.createElement('input');
    input.type = 'number';
    input.className = 'category-amount-input'; // CSS 스타일링을 위함
    input.value = originalAmount; // 현재 금액을 초기값으로 설정

    // 기존 <span>을 <input>으로 교체
    parentDiv.replaceChild(input, amountDisplayElement);
    input.focus(); // 생성된 input 필드에 포커스

    // input 필드에서 포커스를 잃었을 때 (편집 완료 시)
    input.onblur = () => {
        let newAmount = parseInt(input.value);

        // 유효성 검사: 숫자가 아니거나 음수이면 알림 후 원본 값으로 되돌림
        if (isNaN(newAmount) || newAmount < 0) {
            alert('유효한 금액(0 이상)을 입력해주세요.');
            newAmount = originalAmount; // 원본 값으로 되돌림
        }

        // 해당 배열 업데이트
        if (type === 'income') {
            incomes[index].amount = newAmount;
        } else {
            expenses[index].amount = newAmount;
        }

        updateTotals(); // 총액 업데이트

        // <input>을 다시 <span>으로 교체
        const newAmountSpan = document.createElement('span');
        newAmountSpan.className = 'category-amount';
        newAmountSpan.textContent = `₩${newAmount.toLocaleString('ko-KR')}`;
        newAmountSpan.dataset.originalValue = newAmount; // 업데이트된 값을 data 속성에 저장
        parentDiv.replaceChild(newAmountSpan, input);
    };

    // Enter 키를 눌렀을 때 blur 이벤트 발생 (편집 완료)
    input.onkeydown = (event) => {
        if (event.key === 'Enter') {
            input.blur();
        }
    };
}

function populateMonthSelect() {
    const select = document.getElementById('budget-month-select');
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth();

    const startYear = currentYear - 2; // 현재 연도 기준 2년 전
    const endYear = currentYear + 2;   // 현재 연도 기준 2년 후

    for (let year = startYear; year <= endYear; year++) {
        for (let month = 0; month < 12; month++) {
            const optionValue = `${year}-${String(month + 1).padStart(2, '0')}`;
            const optionText = `${year}년 ${String(month + 1).padStart(2, '0')}월`;
            
            const option = document.createElement('option');
            option.value = optionValue;
            option.textContent = optionText;

            if (year === currentYear && month === currentMonth) {
                option.selected = true; // 현재 월을 기본 선택
            }
            select.appendChild(option);
        }
    }
}

// CSRF 토큰을 포함한 AJAX 요청 헬퍼 함수
async function fetchWithCsrf(url, options) {
    options.headers = {
        ...options.headers,
        [csrfHeader]: csrfToken
    };
    return fetch(url, options);
}

async function saveBudget() {
    const budgetNameInput = document.getElementById('budget-goal-name');
    const budgetName = budgetNameInput.value;
    
    const selectedMonth = document.getElementById('budget-month-select').value;
    if (!selectedMonth) {
        alert('예산 월을 선택해주세요.');
        return;
    }

    const [year, month] = selectedMonth.split('-');
    const firstDayOfMonth = `${year}-${month}-01`;
    const lastDayOfMonth = `${year}-${month}-${new Date(parseInt(year), parseInt(month), 0).getDate()}`;

    const totalIncome = incomes.reduce((sum, item) => sum + item.amount, 0);
    const saveAmount = totalIncome; // 총 수입으로 저장

    if (!budgetName || incomes.length === 0 || expenses.length === 0) {
        alert('모든 필수 항목(예산 목표명, 수입/지출 목록)을 입력해주세요.');
        return;
    }

    if (saveAmount <= 0) {
        alert('총 수입이 0이거나 유효하지 않습니다. 수입을 입력해주세요.');
        return;
    }

    // ⭐ 변경된 부분: SavingsPlan 데이터와 expenses 배열을 하나의 객체로 묶어서 전송
    const payload = {
        saveName: budgetName,
        saveCreatedDate: firstDayOfMonth, 
        saveTargetDate: lastDayOfMonth,
        saveAmount: saveAmount,
        expenseDetails: expenses // 현재 expenses 배열을 그대로 전송
    };

    let shouldProceedToSave = true;

    try {
        const checkUrl = `/consumption/hasSavingsPlanForMonth?month=${parseInt(month)}&year=${parseInt(year)}`;
        const checkResponse = await fetchWithCsrf(checkUrl, {
            method: 'GET'
        });

        if (!checkResponse.ok) {
            // ... (기존 에러 처리 로직)
            if (checkResponse.status === 403) {
                throw new Error('인증 오류: 세션이 만료되었거나 접근 권한이 없습니다. 다시 로그인해주세요.');
            }
            const errorResult = await checkResponse.json().catch(() => {
                return checkResponse.text().then(text => ({ message: text || '알 수 없는 서버 오류' }));
            });
            throw new Error(errorResult.message || `예산 존재 여부 확인 실패: ${checkResponse.status} ${checkResponse.statusText}`);
        }
        const checkResult = await checkResponse.json();

        if (checkResult.exists) {
            const confirmOverwrite = confirm("선택하신 월에 저장된 예산이 있습니다. 현재 내용으로 업데이트 하시겠습니까?\n'예'를 누르면 업데이트되고, '아니오'를 누르면 저장 없이 다음 페이지로 이동합니다.");
            if (!confirmOverwrite) {
                shouldProceedToSave = false;
            }
        }

        if (shouldProceedToSave) {
            const saveUrl = '/consumption/bplannerPro';
            const saveResponse = await fetchWithCsrf(saveUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload) // ⭐ payload 객체를 JSON 문자열로 변환하여 전송
            });

            if (!saveResponse.ok) {
                // ... (기존 에러 처리 로직)
                if (saveResponse.status === 403) {
                    throw new Error('인증 오류: 세션이 만료되었거나 접근 권한이 없습니다. 다시 로그인해주세요.');
                }
                const errorResult = await saveResponse.json().catch(() => {
                    return saveResponse.text().then(text => ({ message: text || '알 수 없는 서버 오류' }));
                });
                throw new Error(errorResult.message || `예산 저장/업데이트 실패: ${saveResponse.status}`);
            }

            const result = await saveResponse.json();
            if (result.success) {
                alert(result.message + ' 💾');
                // 저장 성공 후, 다음 페이지로 이동할지 묻는 로직
                if (confirm('소비분석 페이지로 가시겠습니까?')) {
                    window.location.href = '/consumption/efeedback';
                }
            } else {
                alert('저장 실패: ' + result.message);
            }
        } else {
            console.log('예산 저장을 취소하고 다음 페이지로 이동합니다.');
            if (confirm('저장 없이 소비분석 페이지로 가시겠습니까?')) {
                window.location.href = '/consumption/efeedback';
            }
        }

    } catch (error) {
        console.error('예산 처리 중 오류 발생:', error);
        alert('예산 처리 중 오류가 발생했습니다: ' + error.message);
    }
}

function resetBudget() {
    if (confirm('모든 예산 데이터를 초기화하시겠습니까? (현재 페이지의 데이터만 초기화됩니다.)')) {
        incomes = [];
        expenses = [];
        document.getElementById('budget-goal-name').value = '';
        updateIncomeList();
        updateExpenseList();
        updateTotals();
        alert('예산 설정이 초기화되었습니다. 🔄');
    }
}

async function loadBudgetForSelectedMonth() {
	console.log("loadBudgetForSelectedMonth 함수 호출됨");
    const selectedMonthValue = document.getElementById('budget-month-select').value;
    const [year, month] = selectedMonthValue.split('-').map(Number);

    try {
        const response = await fetch(`/consumption/loadBudgetPlan?year=${year}&month=${month}`, {
            method: 'GET',
            headers: {
                [csrfHeader]: csrfToken
            }
        });
        const data = await response.json();
        
		if (data.success && data.data) {
		    const savingsPlan = data.data.savingsPlan;
		    const incomeCategories = data.data.incomeCategories;
		    const expenseCategories = data.data.expenseCategories;

		    document.getElementById('budget-goal-name').value = savingsPlan.saveName || '';

		    // ⭐ incomes 맵핑 시 memo 필드 추가
		    incomes = incomeCategories.map(item => ({ 
		        name: item.categoryName, 
		        amount: item.amount,
		        memo: item.memo // 백엔드에서 'memo'라는 키로 반환된다고 가정
		    }));

		    // expenses는 이미 memo를 포함하고 있으므로 변경 없음
		    expenses = expenseCategories.map(item => ({ 
		        name: item.categoryName, 
		        amount: item.amount, 
		        memo: item.memo // 백엔드에서 'memo'라는 키로 반환된다고 가정
		    }));

        } else {
            // 데이터가 없거나 로드 실패 시 초기화
            console.log("저장된 예산 데이터가 없습니다: " + (data.message || "알 수 없는 응답"));
            incomes = [];
            expenses = [];
            document.getElementById('budget-goal-name').value = '';
        }
        updateIncomeList();
        updateExpenseList(); // expenses가 비어있으므로 지출 목록은 비어있게 표시됩니다.
        updateTotals();
        
    } catch (error) {
        console.error('예산 로드 중 오류 발생:', error);
        alert('예산 로드 중 오류가 발생했습니다: ' + error.message);
    }
}

/**
 * 백엔드에서 지출 카테고리 목록을 가져와서 <select> 엘리먼트를 채웁니다.
 * 카테고리 번호를 value로, 카테고리 이름을 텍스트로 사용합니다.
 */
async function populateExpenseCategories() {
    const selectElement = document.getElementById('expense-category');
    // 초기 "카테고리를 선택하세요" 옵션 유지
    selectElement.innerHTML = '<option value="">카테고리를 선택하세요</option>'; 

    try {
        const response = await fetch('/consumption/api/expense-categories', { // 기존 API 엔드포인트 사용
            method: 'GET',
            headers: {
                // CSRF 토큰은 GET 요청에 항상 필요하지 않을 수 있지만, 백엔드 설정에 따라 추가 가능
                // [csrfHeader]: csrfToken
            }
        });

        if (!response.ok) {
            throw new Error(`카테고리 로드 실패: ${response.status} ${response.statusText}`);
        }

        const categories = await response.json();

        // 받아온 카테고리 데이터를 기반으로 <option> 태그 동적 생성
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.cmIn; // 카테고리 번호를 value로 설정
            option.textContent = category.cmName; // 카테고리 이름을 텍스트로 설정
            selectElement.appendChild(option);
        });
    } catch (error) {
        console.error('지출 카테고리를 불러오는 중 오류 발생:', error);
        alert('지출 카테고리를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
    }
}

async function loadBudgetForSelectedMonth() {
    console.log("loadBudgetForSelectedMonth 함수 호출됨"); // 호출 확인용 로그

    const selectedMonth = document.getElementById('budget-month-select').value;
    if (!selectedMonth) {
        console.log('예산 월이 선택되지 않았습니다.');
        return;
    }

    const [year, month] = selectedMonth.split('-');
    const firstDayOfMonth = `${year}-${month}-01`;
    const lastDayOfMonth = `${year}-${month}-${new Date(parseInt(year), parseInt(month), 0).getDate()}`;

    // 이전에 "떴다"고 하셨던 getSavingsPlanForMonth 엔드포인트 사용
    const loadUrl = `/consumption/getSavingsPlanForMonth?startOfMonth=${firstDayOfMonth}&endOfMonth=${lastDayOfMonth}`; 

    try {
        const response = await fetchWithCsrf(loadUrl, { // fetchWithCsrf 사용
            method: 'GET'
        });

        if (!response.ok) {
             if (response.status === 404) {
                 console.log('선택된 월에 저장된 예산이 없습니다.');
                 incomes = [];
                 expenses = [];
                 document.getElementById('budget-goal-name').value = '';
                 updateIncomeList();
                 updateExpenseList();
                 updateTotals();
                 return;
             }
            if (response.status === 403) {
                throw new Error('인증 오류: 세션이 만료되었거나 접근 권한이 없습니다.');
            }
            const errorResult = await response.json().catch(() => {
                return response.text().then(text => ({ message: text || '알 수 없는 서버 오류' }));
            });
            throw new Error(errorResult.message || `예산 로드 실패: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        
        if (data.success && data.data) {
            const savingsPlan = data.data.savingsPlan || {}; // null 방지
            const incomeCategories = data.data.incomeCategories || []; // null 방지
            const expenseCategories = data.data.expenseCategories || []; // null 방지

            document.getElementById('budget-goal-name').value = savingsPlan.saveName || '';
            
            // 수입 항목 맵핑: memo 필드 포함
            incomes = incomeCategories.map(item => ({ 
                name: item.categoryName, 
                amount: item.amount,
                memo: item.memo // 백엔드에서 'memo'라는 키로 반환된다고 가정
            }));
            
            // 지출 항목 맵핑: memo 필드 포함
            expenses = expenseCategories.map(item => ({ 
                name: item.categoryName, 
                amount: item.amount,
                memo: item.memo // 백엔드에서 'memo'라는 키로 반환된다고 가정
            }));

        } else {
            console.log('저장된 예산 데이터가 없습니다: ' + (data.message || '알 수 없는 응답'));
            incomes = [];
            expenses = [];
            document.getElementById('budget-goal-name').value = '';
        }
        updateIncomeList();
        updateExpenseList();
        updateTotals();
        
    } catch (error) {
        console.error('예산 로드 중 오류 발생:', error);
        alert('예산 로드 중 오류가 발생했습니다: ' + error.message);
    }
}


document.addEventListener('DOMContentLoaded', function() {
	console.log("DOMContentLoaded 이벤트 발생");
    populateMonthSelect();
    loadBudgetForSelectedMonth(); 
    populateExpenseCategories(); // 추가된 함수 호출
    
    document.getElementById('budget-month-select').addEventListener('change', loadBudgetForSelectedMonth);

    updateIncomeList();
    updateExpenseList();
    updateTotals();
});

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
       window.location.href = '/logout';
    }
}