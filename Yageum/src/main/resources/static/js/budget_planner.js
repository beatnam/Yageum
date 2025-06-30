// budget_planner.js

let incomes = [];
let expenses = [];

const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

if (!csrfToken || !csrfHeader) {
    console.error("CSRF token or header name is missing. AJAX requests might fail.");
    alert("보안 토큰을 찾을 수 없습니다. 페이지를 새로고침하거나 관리자에게 문의하세요.");
}

function addIncome() {
    const sourceSelect = document.getElementById('income-source');
    const amountInput = document.getElementById('income-amount-input');
    const name = sourceSelect.value;
    const amount = parseInt(amountInput.value);

    if (name && !isNaN(amount) && amount > 0) {
        incomes.push({ name, amount });
        updateIncomeList();
        updateTotals();
        sourceSelect.value = '';
        amountInput.value = '';
    } else {
        alert('수입원과 유효한 금액을 입력해주세요.');
    }
}

function addExpense() {
    const categorySelect = document.getElementById('expense-category');
    const amountInput = document.getElementById('expense-amount-input');
    const name = categorySelect.value;
    const amount = parseInt(amountInput.value);

    if (name && !isNaN(amount) && amount > 0) {
        expenses.push({ name, amount });
        updateExpenseList();
        updateTotals();
        categorySelect.value = '';
        amountInput.value = '';
    } else {
        alert('지출 카테고리와 유효한 금액을 입력해주세요.');
    }
}

function updateIncomeList() {
    const listContainer = document.getElementById('income-list');
    listContainer.innerHTML = '';

    if (incomes.length === 0) {
        const noData = document.createElement('div');
        noData.className = 'category-item no-data';
        noData.innerHTML = `<span class="category-name">등록된 수입이 없습니다</span><span class="category-amount">₩0</span>`;
        listContainer.appendChild(noData);
    } else {
        incomes.forEach((item, index) => {
            const div = document.createElement('div');
            div.className = 'category-item';
            div.innerHTML = `<span class="category-name">${item.name}</span><span class="category-amount">₩${item.amount.toLocaleString('ko-KR')}</span><button class="delete-btn" onclick="removeIncome(${index})">삭제</button>`;
            listContainer.appendChild(div);
        });
    }
}

function updateExpenseList() {
    const listContainer = document.getElementById('expense-list');
    listContainer.innerHTML = '';

    if (expenses.length === 0) {
        const noData = document.createElement('div');
        noData.className = 'category-item no-data';
        noData.innerHTML = `<span class="category-name">등록된 예산이 없습니다</span><span class="category-amount">₩0</span>`;
        listContainer.appendChild(noData);
    } else {
        expenses.forEach((item, index) => {
            const div = document.createElement('div');
            div.className = 'category-item';
            div.innerHTML = `<span class="category-name">${item.name}</span><span class="category-amount">₩${item.amount.toLocaleString('ko-KR')}</span><button class="delete-btn" onclick="removeExpense(${index})">삭제</button>`;
            listContainer.appendChild(div);
        });
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

function updateTotals() {
    const totalIncome = incomes.reduce((sum, item) => sum + item.amount, 0);
    const totalExpense = expenses.reduce((sum, item) => sum + item.amount, 0);
    const remainingAmount = totalIncome - totalExpense;

    document.getElementById('total-income').textContent = `₩${totalIncome.toLocaleString('ko-KR')}`;
    document.getElementById('total-expense').textContent = `₩${totalExpense.toLocaleString('ko-KR')}`;
    document.getElementById('remaining-budget').textContent = `₩${remainingAmount.toLocaleString('ko-KR')}`;

    if (remainingAmount < 0) {
        document.getElementById('remaining-budget').style.color = 'red';
    } else {
        document.getElementById('remaining-budget').style.color = '#28a745';
    }
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

    const budgetData = {
        saveName: budgetName,
        saveCreatedDate: firstDayOfMonth, 
        saveTargetDate: lastDayOfMonth,
        saveAmount: saveAmount
    };

    let shouldProceedToSave = true;

    try {
        const checkUrl = `/consumption/hasSavingsPlanForMonth?month=${parseInt(month)}&year=${parseInt(year)}`;
        
        const checkResponse = await fetch(checkUrl, {
            method: 'GET',
            headers: {
                [csrfHeader]: csrfToken
            }
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
            const saveResponse = await fetch(saveUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(budgetData)
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
            console.log("예산 저장을 취소하고 다음 페이지로 이동합니다.");
        }

        if (!shouldProceedToSave) {
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

async function loadBudgetForSelectedMonth() {
    const selectedMonth = document.getElementById('budget-month-select').value;
    if (!selectedMonth) {
        return;
    }

    const [year, month] = selectedMonth.split('-');
    const firstDayOfMonth = `${year}-${month}-01`;
    const lastDayOfMonth = `${year}-${month}-${new Date(parseInt(year), parseInt(month), 0).getDate()}`;

    const loadUrl = `/consumption/getSavingsPlanForMonth?startOfMonth=${firstDayOfMonth}&endOfMonth=${lastDayOfMonth}`;

    try {
        const response = await fetch(loadUrl, {
            method: 'GET',
            headers: {
                [csrfHeader]: csrfToken
            }
        });

        if (!response.ok) {
             if (response.status === 404) {
                 console.log("선택된 월에 저장된 예산이 없습니다.");
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
            const savingsPlan = data.data.savingsPlan;
            const incomeCategories = data.data.incomeCategories;
            const expenseCategories = data.data.expenseCategories;

            document.getElementById('budget-goal-name').value = savingsPlan.saveName || '';
            
            incomes = incomeCategories.map(item => ({ name: item.categoryName, amount: item.amount }));
            expenses = expenseCategories.map(item => ({ name: item.categoryName, amount: item.amount }));

        } else {
            // 데이터가 없거나 로드 실패 시 초기화
            console.log("저장된 예산 데이터가 없습니다: " + (data.message || "알 수 없는 응답"));
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
async function populateExpenseCategories() {
    const selectElement = document.getElementById('expense-category');
    // 초기 "카테고리를 선택하세요" 옵션 유지
    selectElement.innerHTML = '<option value="">카테고리를 선택하세요</option>'; 

    try {
        const response = await fetch('/consumption/api/expense-categories', { // 새로 만든 API 엔드포인트
            method: 'GET',
            headers: {
                // CSRF 토큰이 필요하다면 여기에 추가 (GET 요청에는 보통 필요 없음)
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


document.addEventListener('DOMContentLoaded', function() {
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