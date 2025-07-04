document.addEventListener('DOMContentLoaded', function() {
    const feedbackListContainer = document.getElementById('feedbackListContainer');

    if (typeof feedbackData !== 'undefined' && Array.isArray(feedbackData) && feedbackData.length > 0) {
        feedbackData.forEach((data, index) => {
            let conMonthDate;
            if (data.conMonth) {
                conMonthDate = new Date(data.conMonth);
                if (isNaN(conMonthDate.getTime())) {
                    console.warn("Invalid conMonth date received:", data.conMonth);
                    conMonthDate = new Date();
                }
            } else {
                conMonthDate = new Date();
                console.warn("conMonth field is missing for feedback data:", data);
            }

            const year = conMonthDate.getFullYear();
            const month = conMonthDate.getMonth() + 1;
            const formattedMonth = `${year}년 ${month}월`;

            const lastDay = new Date(year, month, 0).getDate();
            const formattedDateRange = `${year}.${String(month).padStart(2, '0')}.01 ~ ${year}.${String(month).padStart(2, '0')}.${String(lastDay).padStart(2, '0')}`;

            const conTotalValue = parseFloat(data.conTotal) || 0;
            const conTotalFormatted = new Intl.NumberFormat('ko-KR').format(conTotalValue);

            const feedbackItem = document.createElement('div');
            feedbackItem.classList.add('feedback-item');

            feedbackItem.innerHTML = `
                <div class="feedback-header">
                    <div class="feedback-info">
                        <div class="feedback-title">${formattedMonth} 지출 분석 피드백</div>
                        <div class="feedback-meta">
                            <span>기간: ${formattedDateRange}</span>
                            <span>총 지출 금액: ₩${conTotalFormatted}</span>
                        </div>
                    </div>
                    <div class="feedback-actions">
                        <button class="view-button" data-con-in="${data.conIn}" data-month="${month}" data-year="${year}">종합분석 피드백 보기</button>
                        <button class="delete-button" data-con-in="${data.conIn}">삭제</button>
                    </div>
                </div>
                <div class="feedback-content" id="feedbackContent-${data.conIn}">
                    <canvas id="chart-${data.conIn}"></canvas>
                    <p>${data.conResult || '피드백 내용이 없습니다.'}</p>
                </div>
            `;
            feedbackListContainer.appendChild(feedbackItem);
        });

        // 이벤트 리스너를 한 번에 추가
        feedbackListContainer.addEventListener('click', function(event) {
            const target = event.target;
            if (target.classList.contains('view-button')) {
                const conIn = target.dataset.conIn;
                const month = target.dataset.month;
                const year = target.dataset.year;
                toggleFeedback(target, `feedbackContent-${conIn}`, month, year);
            } else if (target.classList.contains('delete-button')) {
                const conIn = target.dataset.conIn;
                deleteFeedbackItem(target, conIn);
            }
        });

    } else {
        console.warn("feedbackList 데이터를 찾을 수 없거나 유효한 배열이 아닙니다. (JS)");
        feedbackListContainer.innerHTML = '<p style="text-align: center; color: #7f8c8d; padding: 50px;">아직 등록된 소비 분석 피드백이 없습니다.</p>';
    }
});

// 토글 피드백 함수
function toggleFeedback(button, contentId, month, year) {
    const content = document.getElementById(contentId);
    const chartCanvas = document.getElementById(`chart-${contentId.split('-')[1]}`);
    const feedbackConIn = contentId.split('-')[1];

    content.classList.toggle('show');

    if (content.classList.contains('show')) {
        button.textContent = '피드백 숨기기';
        if (!chartCanvas.dataset.chartLoaded) {
            fetchCategoryExpenses(feedbackConIn, month, year);
            chartCanvas.dataset.chartLoaded = true;
        }
    } else {
        button.textContent = '종합분석 피드백 보기';
    }
}

// 카테고리별 지출 데이터를 가져와 차트 그리는 함수
function fetchCategoryExpenses(conIn, month, year) {
    const chartCanvas = document.getElementById(`chart-${conIn}`);
    const noDataMessageId = `no-chart-data-${conIn}`;
    let noDataMessageElement = document.getElementById(noDataMessageId);

    if (chartCanvas && chartCanvas.chart) {
        chartCanvas.chart.destroy();
        chartCanvas.chart = null;
    }
    if (noDataMessageElement) {
        noDataMessageElement.remove();
    }
    chartCanvas.style.display = 'block';

    const token = $("meta[name='_csrf']").attr("content");
    const header = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
        url: `/consumption/categoryExpenses?month=${month}&year=${year}`,
        type: 'GET',
        dataType: 'json',
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(categoriesData) {
            if (Object.keys(categoriesData).length > 0) {
                const ctx = chartCanvas.getContext('2d');
                chartCanvas.chart = new Chart(ctx, {
                    type: 'pie',
                    data: {
                        labels: Object.keys(categoriesData),
                        datasets: [{
                            data: Object.values(categoriesData),
                            backgroundColor: [
                                '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40', '#C9CBCF', '#E7E9ED', '#8AC926', '#FFCA3A'
                            ],
                            hoverOffset: 8
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        aspectRatio: 1,
                        plugins: {
                            legend: {
                                position: 'right',
                                maintainAspectRatio: false,
                                labels: {
                                    boxWidth: 20,
                                }
                            },
                            title: {
                                display: true,
                                text: `${year}년 ${month}월 카테고리별 지출`,
                                font: {
                                    size: 16,
                                    weight: 'bold'
                                },
                                color: '#34495e'
                            }
                        },
                        tooltips: {
                            callbacks: {
                                label: function(context) {
                                    let label = context.label || '';
                                    if (label) {
                                        label += ': ';
                                    }
                                    if (context.parsed !== null) {
                                        label += new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(context.parsed);
                                    }
                                    return label;
                                }
                            }
                        }
                    }
                });
            } else {
                console.warn(`No category data available for conIn: ${conIn} (month: ${month}, year: ${year}). Chart will not be displayed.`);
                chartCanvas.style.display = 'none';
                const noDataMessage = document.createElement('p');
                noDataMessage.id = noDataMessageId;
                noDataMessage.textContent = '카테고리별 지출 데이터가 없습니다.';
                noDataMessage.style.textAlign = 'center';
                noDataMessage.style.color = '#7f8c8d';
                chartCanvas.parentNode.insertBefore(noDataMessage, chartCanvas.nextSibling);
            }
        },
        error: function(xhr, status, error) {
            console.error("Failed to fetch category expenses:", error);
            chartCanvas.style.display = 'none';
            const noDataMessage = document.createElement('p');
            noDataMessage.id = noDataMessageId;
            noDataMessage.textContent = '카테고리 데이터를 불러오는데 실패했습니다.';
            noDataMessage.style.textAlign = 'center';
            noDataMessage.style.color = '#e74c3c';
            chartCanvas.parentNode.insertBefore(noDataMessage, chartCanvas.nextSibling);
        }
    });
}

// 피드백 삭제 함수
function deleteFeedbackItem(button, conInId) {
    if (confirm('이 피드백을 삭제하시겠습니까?')) {
        const token = $("meta[name='_csrf']").attr("content");
        const header = $("meta[name='_csrf_header']").attr("content");

        $.ajax({
            url: '/consumption/feedback/' + conInId,
            type: 'DELETE',
            beforeSend: function(xhr) {
                xhr.setRequestHeader(header, token);
            },
            success: function(result) {
                const itemToRemove = button.closest('.feedback-item');
                itemToRemove.remove();
                alert('피드백이 성공적으로 삭제되었습니다.');
            },
            error: function(xhr, status, error) {
                alert('피드백 삭제에 실패했습니다: ' + error);
                console.error("삭제 실패:", xhr.responseText);
            }
        });
    }
}

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
       window.location.href = '/logout';
    }
}
