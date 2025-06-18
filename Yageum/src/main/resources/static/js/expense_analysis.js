// expense_analysis.js
document.addEventListener("DOMContentLoaded", function () {
    let monthlyExpensesLabels;
    let monthlyExpensesValues;

    if (window.chartData) {
        try {
            monthlyExpensesLabels = JSON.parse(window.chartData.labels);
            monthlyExpensesValues = JSON.parse(window.chartData.values);
        } catch (e) {
            console.error("전역 변수에서 차트 데이터 파싱 오류:", e);
            console.log("Raw Labels String (global):", window.chartData.labels);
            console.log("Raw Values String (global):", window.chartData.values);
            monthlyExpensesLabels = [];
            monthlyExpensesValues = [];
        }
    } else {
        console.warn("window.chartData 객체를 찾을 수 없습니다. 차트 데이터를 로드하지 못했습니다.");
        monthlyExpensesLabels = [];
        monthlyExpensesValues = [];
    }

    const ctx = document.getElementById('monthlyExpenseChart').getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: monthlyExpensesLabels,
            datasets: [{
                label: '월별 총 지출 (원)',
                data: monthlyExpensesValues,
                backgroundColor: 'rgba(255, 215, 0, 0.2)',
                borderColor: '#FFD700',
                borderWidth: 2,
                fill: true,
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '₩' + value.toLocaleString();
                        }
                    }
                }
            },
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': ₩' + context.parsed.y.toLocaleString();
                        }
                    }
                }
            }
        }
    });


    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px) scale(1.02)';
        });
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });

    const feedbackArea = document.getElementById('chatGptFeedbackArea');
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    // CSRF 토큰이 없을 경우 초기 경고 메시지 출력
    if (!csrfToken || !csrfHeader) {
        if (feedbackArea) { // feedbackArea가 존재하는 경우에만 메시지 출력
            feedbackArea.innerHTML = '<p style="color: red;">오류: 보안 토큰(CSRF)을 찾을 수 없습니다. 페이지를 새로고침하거나 관리자에게 문의하세요.</p>';
        }
        console.error("CSRF token or header name is missing.", { csrfToken, csrfHeader });
    }

    function loadFeedbackAutomatically() {
        if (feedbackArea) { // feedbackArea가 존재하는 경우에만 메시지 출력
            feedbackArea.innerHTML = '<p>피드백을 로드 중입니다...</p>';
        }

        const selectedMetrics = [
            "totalExpense",
            "averageDailyExpense",
            "daysLeft",
            "remainingBudget"
        ];

        // CSRF 토큰이 없으면 fetch 요청을 보내지 않도록 조건 추가
        if (!csrfToken || !csrfHeader) {
             if (feedbackArea) { // feedbackArea가 존재하는 경우에만 메시지 출력
                 feedbackArea.innerHTML = '<p style="color: red;">CSRF 토큰 누락으로 피드백을 가져올 수 없습니다.</p>';
             }
             return; // 함수 실행 중단
        }

        fetch('/consumption/getChatGptFeedbackForEanalysis', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ selectedMetrics: selectedMetrics })
        })
        .then(response => {
            if (!response.ok) {
                if (response.status === 403) {
                     throw new Error('인증 오류: 세션이 만료되었거나 접근 권한이 없습니다. 다시 로그인해주세요.');
                }
                throw new Error('네트워크 응답이 올바르지 않습니다: ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            if (feedbackArea) { // feedbackArea가 존재하는 경우에만 처리
                if (data && data.feedback) {
                    feedbackArea.innerHTML = '<p id="chatGptFeedbackContent">' + data.feedback.replace(/\n/g, '<br>') + '</p>';
                } else if (data && data.error) {
                    feedbackArea.innerHTML = '<p style="color: red;">오류: ' + data.error + '</p>';
                } else {
                    feedbackArea.innerHTML = '<p>피드백을 가져오지 못했습니다.</p>';
                }
            }
        })
        .catch(error => {
            console.error('ChatGPT 피드백을 가져오는 중 오류 발생:', error);
            if (feedbackArea) { // feedbackArea가 존재하는 경우에만 메시지 출력
                feedbackArea.innerHTML = '<p style="color: red;">피드백 로드 중 오류가 발생했습니다: ' + error.message + '</p>';
            }
        });
    }

    loadFeedbackAutomatically();

    // 전역 함수로 분리 (HTML에서 직접 호출되므로 window 객체에 할당)
    window.move = function() {
        const btn = event.target; // event 객체는 이 위치에서는 정의되지 않을 수 있음. 함수 호출 방식에 따라 다름.
                                  // 일반적으로는 button 요소 자체를 인자로 받거나, document.getElementById로 가져옴
        if (!btn) { // btn이 없으면 함수 실행 중단
            console.error("Move function: Target button not found.");
            return;
        }

        btn.textContent = '예산 설정으로 가는 중...';
        btn.disabled = true;

        setTimeout(() => {
            btn.textContent = '새로운 예산 생성하기';
            btn.disabled = false;

            window.location.href = '/consumption/bplanner';
        }, 2000);
    }

    // 전역 함수로 분리 (HTML에서 직접 호출되므로 window 객체에 할당)
    window.logout = function() {
        if (confirm('로그아웃 하시겠습니까?')) {
           window.location.href = '/logout';
        }
    }

    async function handleMoveToCanalysis() {
        const btn = document.getElementById('moveToCanalysisBtn');
        if (!btn) { // btn이 없으면 함수 실행 중단
            console.error("handleMoveToCanalysis function: Target button not found.");
            return;
        }
        btn.textContent = '종합 분석하는 중...';
        btn.disabled = true;

        const feedbackElement = document.getElementById('chatGptFeedbackContent');
        let budFeedbackContent = "";

        if (feedbackElement) {
            budFeedbackContent = feedbackElement.innerText;
        } else {
            console.warn("chatGptFeedbackContent 요소를 찾을 수 없습니다. 피드백을 빈 문자열로 처리합니다.");
        }

        console.log("종합분석을 위해 저장할 AI 피드백 내용 (bud_feedback):", budFeedbackContent);

        let shouldProceedToSave = true;

        try {
            const checkBudFeedbackUrl = '/consumption/hasBudFeedback';
            const checkResponse = await fetch(checkBudFeedbackUrl, {
                method: 'GET',
                headers: {
                    [csrfHeader]: csrfToken
                }
            });

            if (!checkResponse.ok) {
                 if (checkResponse.status === 403) {
                     throw new Error('인증 오류: 세션이 만료되었거나 접근 권한이 없습니다. 다시 로그인해주세요.');
                 }
                throw new Error(`bud_feedback 존재 여부 확인 실패: ${checkResponse.status} ${checkResponse.statusText}`);
            }
            const checkResult = await checkResponse.json();

            if (checkResult.exists) {
                const confirmOverwrite = confirm("저장된 종합분석 피드백이 있습니다. 현재 내용으로 덮어쓰시겠습니까?\n'예'를 누르면 업데이트되고, '아니오'를 누르면 저장 없이 다음 페이지로 이동합니다.");
                if (!confirmOverwrite) {
                    shouldProceedToSave = false;
                }
            }

            if (shouldProceedToSave) {
                console.log("종합분석 피드백 저장을 시도합니다.");
                const saveResponse = await fetch('/consumption/cfeedset', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    },
                    body: JSON.stringify({
                        feedbackContent: budFeedbackContent
                    })
                });

                if (!saveResponse.ok) {
                     if (saveResponse.status === 403) {
                         throw new Error('인증 오류: 세션이 만료되었거나 접근 권한이 없습니다. 다시 로그인해주세요.');
                     }
                    const errorResult = await saveResponse.json().catch(() => {
                        return saveResponse.text().then(text => ({ message: text || '알 수 없는 서버 오류' }));
                    });
                    throw new Error(errorResult.message || `서버 오류 발생: ${saveResponse.status}`);
                }

                const saveResult = await saveResponse.json();
                console.log('종합분석 피드백 저장 결과:', saveResult);

                if (saveResult.success) {
                    console.log("종합분석 피드백 저장 성공:", saveResult.message);
                } else {
                    alert("종합분석 피드백 저장 실패: " + saveResult.message);
                }
            } else {
                console.log("종합분석 피드백 저장을 취소하고 다음 페이지로 이동합니다.");
            }

            setTimeout(() => {
                btn.textContent = '종합분석 화면으로 가기';
                btn.disabled = false;
                window.location.href = '/consumption/canalysis';
            }, 1000);

        } catch (error) {
            console.error('종합분석 피드백 처리 중 오류 발생:', error);
            alert("피드백 로드 중 오류가 발생했습니다: " + error.message); // 기존 메시지 수정
            btn.textContent = '종합분석 화면으로 가기';
            btn.disabled = false;
        }
    }

    const moveToCanalysisButton = document.getElementById('moveToCanalysisBtn');
    if (moveToCanalysisButton) {
        moveToCanalysisButton.addEventListener('click', handleMoveToCanalysis);
    }
});