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

// 필터링 요청 함수
function applyFilters() {
  const category = document.getElementById("categoryFilter").value;
  const type = document.getElementById("typeFilter").value;
  const method = document.getElementById("paymentFilter").value;
  const keyword = document.getElementById("searchInput").value;
  const { startDate, endDate } = getDateRange();

  // 타이틀 날짜 출력
  const formattedTitle = startDate.replaceAll("-", ".") + " ~ " + endDate.replaceAll("-", ".");
  const titleEl = document.querySelector(".transaction-title");
  if (titleEl) titleEl.textContent = formattedTitle;

  // 쿼리 파라미터 구성
  const params = new URLSearchParams();
  if (category) params.append("category", category);
  if (type) params.append("type", type);
  if (method) params.append("method", method);
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

  periodSelect.addEventListener("change", function () {
    customDateRange.style.display = (this.value === "custom") ? "block" : "none";
  });

  applyFilters(); // 초기 실행
});


// ======================== 내역 출력 영역 ========================

function renderTransactions(data) {
  const listContainer = document.querySelector(".transaction-list");

  // 기존 항목 제거
  const items = listContainer.querySelectorAll(".transaction-item");
  items.forEach(item => item.remove());

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