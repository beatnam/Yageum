// 기간 관련 
document.addEventListener("DOMContentLoaded", function () {
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth() + 1;

    // 날짜 범위 설정
    const startDate = new Date(year, month - 1, 1);
    const endDate = new Date(year, month, 0);

    const format = (date) => {
        const yyyy = date.getFullYear();
        const mm = String(date.getMonth() + 1).padStart(2, '0');
        const dd = String(date.getDate()).padStart(2, '0');
        return `${yyyy}.${mm}.${dd}`;
    };

    // 제목 날짜 출력
    const formattedRange = `${format(startDate)} ~ ${format(endDate)}`;
    const titleEl = document.querySelector(".transaction-title");
    if (titleEl) titleEl.textContent = formattedRange;

    // Ajax로 월별 내역 조회
    fetch(`/cashbook/monthList?year=${year}&month=${month}`)
        .then(res => res.json())
        .then(data => renderTransactions(data))
        .catch(err => console.error("월별 내역 조회 실패", err));
});

//내역 리스트 부분
function renderTransactions(data) {
    const listContainer = document.querySelector(".transaction-list");

    // 기존 거래 아이템 제거
    const items = listContainer.querySelectorAll(".transaction-item");
    items.forEach(item => item.remove());

    let totalIncome = 0;
    let totalExpense = 0;

	// html 각 영역에 어떤거 출력할지
    data.forEach(item => {
        const isIncome = Number(item.expenseType) === 0;
        const amount = item.expenseSum || 0;
        const amountText = (isIncome ? "+" : "-") + amount.toLocaleString() + "원";
        const amountClass = isIncome ? "income" : "expense";
        const methodText = item.accountName || item.cardName || "현금";
        const content = item.expenseContent || "내용 없음";
		const memo = item.expenseMemo || "메모 없음";
		const emoji = isIncome ? "💰" : "💸";

        if (isIncome) {
            totalIncome += amount;
        } else {
            totalExpense += amount;
        }

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

    // 총 수입/지출 반영 (클래스 기반으로 찾아서 반영)
    const incomeEl = document.querySelector(".transaction-summary .income");
    const expenseEl = document.querySelector(".transaction-summary .expense");

    if (incomeEl) incomeEl.textContent = "+" + totalIncome.toLocaleString() + "원";
    if (expenseEl) expenseEl.textContent = "-" + totalExpense.toLocaleString() + "원";
}