// ======================== 필터 / 검색 영역 ========================

// 기간 계산 함수
function getDateRange() {
  const period = document.getElementById("periodFilter").value;

  if (period === "custom") {
    const start = document.getElementById("startDateInput").value;
    const end = document.getElementById("endDateInput").value;
    return { startDate: start, endDate: end };
  }

  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth(); // 0~11

  let start, end;
  if (period === "lastMonth") {
    start = new Date(year, month - 1, 1);
    end = new Date(year, month, 0);
  } else {
    start = new Date(year, month, 1);
    end = new Date(year, month + 1, 0);
  }

  const format = (d) => {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  };

  return {
    startDate: format(start),
    endDate: format(end)
  };
}

// 카드 불러오기 (신용 + 체크)
function fetchCards() {
  const methodIds = [1, 2]; // 1: 신용, 2: 체크

  methodIds.forEach(methodId => {
    fetch(`/cashbook/cards/byMethod/${methodId}`)
      .then(res => res.json())
      .then(data => {
		data.forEach(card => {
		  const option = document.createElement('option');
		  option.value = `card-${card.cardIn}`;
		  option.textContent = card.cardName;
		  option.dataset.method = methodId; // 여기 중요! 1 또는 2
		  document.getElementById("paymentFilter").appendChild(option);
		});
      });
  });
}

// 계좌 불러오기
function fetchAccounts() {
  fetch('/cashbook/accounts')
    .then(res => res.json())
    .then(data => {
      data.forEach(account => {
        const option = document.createElement('option');
        option.value = `account-${account.accountIn}`;
        option.textContent = account.accountName;
        document.getElementById("paymentFilter").appendChild(option);
      });
    });
}



// 필터링 요청 함수
function applyFilters() {
  const category = document.getElementById("categoryFilter").value;
  const type = document.getElementById("typeFilter").value;
  const rawMethod  = document.getElementById("paymentFilter").value;
  const keyword = document.getElementById("searchInput").value;
  const { startDate, endDate } = getDateRange();
  
  // 결제수단 파싱
  const selectedOption = document.getElementById("paymentFilter").selectedOptions[0];
  let method = "";
  let cardIn = "";
  let accountIn = "";

  if (rawMethod.startsWith("card-")) {
    method = selectedOption.dataset.method;
    cardIn = rawMethod.split("-")[1];
  } else if (rawMethod.startsWith("account-")) {
    method = 4;
    accountIn = rawMethod.split("-")[1];
  } else if (rawMethod === "cash") {
    method = 3;
  }

  // 타이틀 날짜 출력
  const formattedTitle = startDate.replaceAll("-", ".") + " ~ " + endDate.replaceAll("-", ".");
  const titleEl = document.querySelector(".transaction-title");
  if (titleEl) titleEl.textContent = formattedTitle;

  // 쿼리 파라미터 구성
  const params = new URLSearchParams();
  if (category) params.append("category", category);
  if (type) params.append("type", type);
  if (method) params.append("method", method);
  if (cardIn) params.append("cardIn", cardIn);
  if (accountIn) params.append("accountIn", accountIn);
  if (keyword) params.append("keyword", keyword);
  params.append("startDate", startDate);
  params.append("endDate", endDate);

  // Ajax 요청
  fetch(`/cashbook/filterSearch?${params.toString()}`)
    .then(res => res.json())
    .then(data => renderTransactions(data))
    .catch(err => console.error("필터 검색 실패", err));
}

// 페이지 로딩 시: 날짜 input 보이기 + 초기 조회
document.addEventListener("DOMContentLoaded", function () {
  const periodSelect = document.getElementById("periodFilter");
  const customDateRange = document.getElementById("customDateRange");
  
  document.getElementById("categoryFilter").addEventListener("change", applyFilters);
  document.getElementById("typeFilter").addEventListener("change", applyFilters);
  document.getElementById("paymentFilter").addEventListener("change", applyFilters);
  document.getElementById("periodFilter").addEventListener("change", applyFilters);

  periodSelect.addEventListener("change", function () {
    customDateRange.style.display = (this.value === "custom") ? "block" : "none";
  });

  fetchCards();    // 카드 불러오기
  fetchAccounts(); // 계좌 불러오기
  applyFilters(); // 초기 실행
});

// 초기화 버튼
function resetFilters() {
  document.getElementById("categoryFilter").value = "";
  document.getElementById("typeFilter").value = "";
  document.getElementById("paymentFilter").value = "";
  document.getElementById("searchInput").value = "";
  document.getElementById("periodFilter").value = "thisMonth";

  // 사용자 정의 날짜 입력창 숨기고 초기화
  document.getElementById("customDateRange").style.display = "none";
  document.getElementById("startDateInput").value = "";
  document.getElementById("endDateInput").value = "";

  // 다시 필터 적용
  applyFilters();
}

// ======================== 내역 출력 영역 ========================

function renderTransactions(data) {
  const listContainer = document.querySelector(".transaction-list");

  // 기존 항목 제거
  const items = listContainer.querySelectorAll(".transaction-item");
  items.forEach(item => item.remove());
  
  // 데이터가 없을 경우 안내 문구 출력
  if (!data || data.length === 0) {
    const emptyHTML = `
      <div class="transaction-item empty">
        <div class="transaction-details">
          <div class="transaction-desc">내역이 존재하지 않습니다</div>
        </div>
      </div>
    `;
    listContainer.insertAdjacentHTML("beforeend", emptyHTML);

    // 총합도 0으로 초기화
    document.querySelector(".transaction-summary .income").textContent = "+0원";
    document.querySelector(".transaction-summary .expense").textContent = "-0원";
    return;
  }

  let totalIncome = 0;
  let totalExpense = 0;

  data.forEach(item => {
    const isIncome = Number(item.expenseType) === 0;
    const amount = item.expenseSum || 0;
    const amountText = (isIncome ? "+" : "-") + amount.toLocaleString() + "원";
    const amountClass = isIncome ? "income" : "expense";
    const methodText = item.accountName || item.cardName || "현금";
    const content = item.expenseContent || "내용 없음";
    const memo = item.expenseMemo || "메모 없음";
    const emoji = isIncome ? "💰" : "💸";

    if (isIncome) totalIncome += amount;
    else totalExpense += amount;

    const itemHTML = `
      <div class="transaction-item">
        <div class="transaction-icon">${emoji}</div>
        <div class="transaction-details">
          <div class="transaction-desc">${content}</div>
          <div class="transaction-meta"><span>${methodText}·${memo}</span></div>
        </div>
        <div>
          <div class="transaction-amount ${amountClass}">${amountText}</div>
          <div class="transaction-date">${item.expenseDate}</div>
        </div>
      </div>
    `;

    listContainer.insertAdjacentHTML("beforeend", itemHTML);
  });

  // 수입/지출 총합 출력
  const incomeEl = document.querySelector(".transaction-summary .income");
  const expenseEl = document.querySelector(".transaction-summary .expense");

  if (incomeEl) incomeEl.textContent = "+" + totalIncome.toLocaleString() + "원";
  if (expenseEl) expenseEl.textContent = "-" + totalExpense.toLocaleString() + "원";
}

// 로그아웃 함수
function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
       window.location.href = '/logout';
    }
}