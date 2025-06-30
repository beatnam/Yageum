// budget_planner.js

let incomes = [];
let expenses = [];

const csrfToken = document.querySelector('meta[name=\'_csrf\']')?.content;
const csrfHeader = document.querySelector('meta[name=\'_csrf_header\']')?.content;

if (!csrfToken || !csrfHeader) {
    console.error('CSRF token or header name is missing. AJAX requests might fail.');
    alert('보안 토큰을 찾을 수 없습니다. 페이지를 새로고침하거나 관리자에게 문의하세요.');
}

// ⭐ 수정: addIncome 함수 - 수입 카테고리 select에서 값을 가져오도록 변경
function addIncome() {
    const categorySelect = document.getElementById('income-category-select'); // HTML에서 변경된 ID 사용
    const selectedCategoryValue = categorySelect.value; // 선택된 카테고리의 ID (csIn)
    const selectedCategoryName = categorySelect.options[categorySelect.selectedIndex].text; // 선택된 카테고리의 이름 (표시 텍스트)

    const amountInput = document.getElementById('income-amount-input');
    const amount = parseInt(amountInput.value);

    // 카테고리 선택 및 유효한 금액 검사
    if (selectedCategoryValue && !isNaN(amount) && amount > 0) {
        incomes.push({ 
            name: selectedCategoryName, // 카테고리 이름 사용
            amount: amount,
            categoryId: selectedCategoryValue // 백엔드 전송을 위해 카테고리 ID 저장
            // 새로운 수입 항목에는 'memo' 필드가 추가되지 않음 (select로 대체되었기 때문)
        });
        updateIncomeList();
        updateTotals();
        categorySelect.value = ''; // select 박스 초기화
        amountInput.value = '';
    } else {
        alert('수입 카테고리와 유효한 금액을 선택/입력해주세요.'); // 알림 메시지 수정
    }
}

function addExpense() {
    const categorySelect = document.getElementById('expense-category');
    // ⭐ 삭제된 부분 (memoInput 관련)
    const amountInput = document.getElementById('expense-amount-input');
    
    const name = categorySelect.options[categorySelect.selectedIndex].textContent;
    // ⭐ 삭제된 부분 (memo 관련)
    const amount = parseInt(amountInput.value);

    // 지출 카테고리가 선택되었는지 확인
    if (!name) { 
        alert('지출 카테고리를 선택해주세요.');
        return;
    }

    if (!isNaN(amount) && amount > 0) {
        // ⭐ memo 필드 제거: expenses.push({ name, amount, memo }); -> expenses.push({ name, amount });
        expenses.push({ name, amount }); 
        updateExpenseList();
        updateTotals();
        categorySelect.value = ''; // 선택된 카테고리 초기화
        // ⭐ 삭제된 부분 (memoInput 관련)
        amountInput.value = '';
    } else {
        alert('유효한 금액을 입력해주세요.'); // 알림 메시지 수정
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

            // ⭐ 수입 메모를 표시하도록 수정 (기존과 동일하게 유지)
            // 백엔드에서 로드된 수입에 memo 필드가 있다면 memo를 표시, 없으면 name (카테고리 이름) 표시
            const displayText = item.memo && item.memo.trim() !== '' ? `${item.memo}` : item.name; 

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

            // ⭐ 지출에서는 item.memo를 사용하지 않습니다.
            const displayText = item.name; // 이제 지출은 메모 없이 카테고리 이름만 표시

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
    } else {
        alert('유효한 수입 금액을 입력해주세요.');
    }
}

function updateExpenseAmount(index, newValue) {
    const amount = parseInt(newValue);
    if (!isNaN(amount) && amount >= 0) { // 0이상의 유효한 숫자인지 확인
        expenses[index].amount = amount;
        updateTotals(); // 총액 업데이트
    } else {
        alert('유효한 지출 금액을 입력해주세요.');
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
    const amountDisplayElement = parentDiv.querySelector('.category-amount'); 

    if (amountDisplayElement.tagName === 'INPUT') {
        amountDisplayElement.blur(); 
        return;
    }

    const originalAmount = parseInt(amountDisplayElement.dataset.originalValue);

    const input = document.createElement('input');
    input.type = 'number';
    input.className = 'category-amount-input'; 
    input.value = originalAmount; 

    parentDiv.replaceChild(input, amountDisplayElement);
    input.focus(); 

    input.onblur = () => {
        let newAmount = parseInt(input.value);

        if (isNaN(newAmount) || newAmount < 0) {
            alert('유효한 금액(0 이상)을 입력해주세요.');
            newAmount = originalAmount; 
        }

        if (type === 'income') {
            incomes[index].amount = newAmount;
        } else {
            expenses[index].amount = newAmount;
        }

        updateTotals(); 

        const newAmountSpan = document.createElement('span');
        newAmountSpan.className = 'category-amount';
        newAmountSpan.textContent = `₩${newAmount.toLocaleString('ko-KR')}`;
        newAmountSpan.dataset.originalValue = newAmount; 
        parentDiv.replaceChild(newAmountSpan, input);
    };

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

    const startYear = currentYear - 2; 
    const endYear = currentYear + 2;   

    for (let year = startYear; year <= endYear; year++) {
        for (let month = 0; month < 12; month++) {
            const optionValue = `${year}-${String(month + 1).padStart(2, '0')}`;
            const optionText = `${year}년 ${String(month + 1).padStart(2, '0')}월`;
            
            const option = document.createElement('option');
            option.value = optionValue;
            option.textContent = optionText;

            if (year === currentYear && month === currentMonth) {
                option.selected = true; 
            }
            select.appendChild(option);
        }
    }
}

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
    const saveAmount = totalIncome; 

    if (!budgetName || incomes.length === 0 || expenses.length === 0) {
        alert('모든 필수 항목(예산 목표명, 수입/지출 목록)을 입력해주세요.');
        return;
    }

    if (saveAmount <= 0) {
        alert('총 수입이 0이거나 유효하지 않습니다. 수입을 입력해주세요.');
        return;
    }

    const payload = {
        saveName: budgetName,
        saveCreatedDate: firstDayOfMonth, 
        saveTargetDate: lastDayOfMonth,
        saveAmount: saveAmount,
        expenseDetails: expenses 
    };

    let shouldProceedToSave = true;

    try {
        const checkUrl = `/consumption/hasSavingsPlanForMonth?month=${parseInt(month)}&year=${parseInt(year)}`;
        const checkResponse = await fetchWithCsrf(checkUrl, {
            method: 'GET'
        });

        if (!checkResponse.ok) {
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
                body: JSON.stringify(payload) 
            });

            if (!saveResponse.ok) {
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

// ⭐ 수정: loadBudgetForSelectedMonth 함수 - 두 번째 (더 완전한) 버전을 사용하고 fetchWithCsrf 적용
async function loadBudgetForSelectedMonth() {
    console.log("loadBudgetForSelectedMonth 함수 호출됨"); 

    const selectedMonth = document.getElementById('budget-month-select').value;
    if (!selectedMonth) {
        console.log('예산 월이 선택되지 않았습니다.');
        return;
    }

    const [year, month] = selectedMonth.split('-');
    const firstDayOfMonth = `${year}-${month}-01`;
    const lastDayOfMonth = `${year}-${month}-${new Date(parseInt(year), parseInt(month), 0).getDate()}`;

    const loadUrl = `/consumption/getSavingsPlanForMonth?startOfMonth=${firstDayOfMonth}&endOfMonth=${lastDayOfMonth}`; 

    try {
        const response = await fetchWithCsrf(loadUrl, { // ⭐ fetchWithCsrf 사용
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
            const savingsPlan = data.data.savingsPlan || {}; 
            const incomeCategories = data.data.incomeCategories || []; 
            const expenseCategories = data.data.expenseCategories || []; 

            document.getElementById('budget-goal-name').value = savingsPlan.saveName || '';
            
            // 수입 항목 맵핑: memo 필드 포함 (백엔드에서 memo를 반환할 경우)
            incomes = incomeCategories.map(item => ({ 
                name: item.categoryName, 
                amount: item.amount,
                memo: item.memo // 백엔드에서 'memo'라는 키로 반환된다고 가정
            }));
            
            // 지출 항목 맵핑: memo 필드를 포함하지 않습니다.
            expenses = expenseCategories.map(item => ({ 
                name: item.categoryName, 
                amount: item.amount
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

/**
 * 백엔드에서 지출 카테고리 목록을 가져와서 <select> 엘리먼트를 채웁니다.
 * 카테고리 번호를 value로, 카테고리 이름을 텍스트로 사용합니다.
 */
// ⭐ 수정: populateExpenseCategories 함수 - fetchWithCsrf 적용
async function populateExpenseCategories() {
    const selectElement = document.getElementById('expense-category');
    selectElement.innerHTML = '<option value="">카테고리를 선택하세요</option>'; 

    try {
        const response = await fetchWithCsrf('/consumption/api/expense-categories', { // ⭐ fetchWithCsrf 사용
            method: 'GET'
        });

        if (!response.ok) {
            throw new Error(`카테고리 로드 실패: ${response.status} ${response.statusText}`);
        }

        const categories = await response.json();

        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.cmIn; 
            option.textContent = category.cmName; 
            selectElement.appendChild(option);
        });
    } catch (error) {
        console.error('지출 카테고리를 불러오는 중 오류 발생:', error);
        alert('지출 카테고리를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
    }
}

// ⭐ 새로 추가: populateIncomeCategories 함수 - 백엔드에서 수입 카테고리를 가져와 select 엘리먼트를 채웁니다.
async function populateIncomeCategories() {
    const incomeCategorySelect = document.getElementById('income-category-select');
    // 기존 옵션들을 모두 지우고 '카테고리 선택' 옵션을 다시 추가
    incomeCategorySelect.innerHTML = '<option value="">카테고리 선택</option>';

    try {
        const response = await fetchWithCsrf('/consumption/api/income-categories', { // ⭐ CSRF 토큰을 포함한 fetchWithCsrf 사용
            method: 'GET'
        });
        const data = await response.json();

        if (response.ok && data) {
            data.forEach(category => {
                const option = document.createElement('option');
                option.value = category.csIn; // CategorySubDTO의 csIn 필드 사용
                option.textContent = category.csName; // CategorySubDTO의 csName 필드 사용
                incomeCategorySelect.appendChild(option);
            });
        } else {
            console.error('수입 카테고리 로드 실패:', data.message || '알 수 없는 응답');
        }
    } catch (error) {
        console.error('수입 카테고리 로드 중 오류 발생:', error);
        alert('수입 카테고리 로드 중 오류가 발생했습니다.');
    }
}


// ⭐ 수정: DOMContentLoaded 이벤트 리스너
document.addEventListener('DOMContentLoaded', function() {
    console.log("DOMContentLoaded 이벤트 발생");
    populateMonthSelect();
    loadBudgetForSelectedMonth(); 
    populateExpenseCategories(); 
    populateIncomeCategories(); // ⭐ 수입 카테고리 로드 함수 호출 추가
    
    // ⭐ 수입 추가 버튼 이벤트 리스너 추가 (HTML에 'add-income-button' ID가 있다고 가정)
    const addIncomeButton = document.getElementById('add-income-button');
    if (addIncomeButton) {
        addIncomeButton.addEventListener('click', addIncome);
    } else {
        console.error("Error: 'add-income-button' element not found. Please check your HTML.");
    }

    const addExpenseButton = document.getElementById('add-expense-button');
    if (addExpenseButton) { 
        addExpenseButton.addEventListener('click', addExpense);
    } else {
        console.error("Error: 'add-expense-button' element not found. Please check your HTML.");
    }
	
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