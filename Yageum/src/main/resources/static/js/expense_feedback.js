// expense_feedback.js

$(document).ready(function() {
    // CSRF 토큰 및 헤더 설정 (jQuery AJAX setup)
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');
    if (csrfToken && csrfHeader) {
        $(document).ajaxSend(function(e, xhr, options) {
            xhr.setRequestHeader(csrfHeader, csrfToken);
        });
    }

    // 예산 모달 관련 로직
    $.ajax({
        url: '/consumption/checkPlan',
        type: 'GET',
        success: function(response) {
            if (response === 0) {
                $('#budgetAlertModal').css('display', 'flex');
                // #analysisContent는 현재 HTML에 없는 ID이므로 주석 처리하거나 제거
                // $('#analysisContent').css('display', 'none');
            } else {
                $('#budgetAlertModal').css('display', 'none');
                // $('#analysisContent').css('display', 'block');
            }
        },
        error: function(xhr, status, error) {
            console.error("'/checkPlan' 호출 중 오류 발생:", error);
            alert("예산 설정 정보를 불러오는 데 실패했습니다. 잠시 후 다시 시도해주세요.");
            $('#budgetAlertModal').css('display', 'flex');
            // $('#analysisContent').css('display', 'none');
        }
    });

    $('.close-button').on('click', function() {
        $('#budgetAlertModal').css('display', 'none');
    });

    $(window).on('click', function(event) {
        if ($(event.target).is('#budgetAlertModal')) {
            $('#budgetAlertModal').css('display', 'none');
        }
    });

    // '전반적으로 분석하기' 버튼에 이벤트 리스너 연결
    // HTML의 해당 버튼에 id="generateNewGoalsBtn"를 추가해야 합니다.
    const generateGoalsBtn = document.getElementById('generateNewGoalsBtn');
    if (generateGoalsBtn) {
        generateGoalsBtn.addEventListener('click', generateNewGoals);
    } else {
        // ID가 없을 경우 클래스 선택자로 연결 (HTML 수정이 어렵다면 임시 방편)
        // 이 경우, 버튼이 하나만 있어야 예상대로 동작합니다.
        // $(".progress-section .btn").on('click', generateNewGoals);
        console.warn("ID 'generateNewGoalsBtn'을 가진 버튼을 찾을 수 없습니다. HTML을 확인해주세요.");
    }

    // DOM이 완전히 로드된 후 애니메이션 및 피드백 호출
    // $(document).ready()는 DOMContentLoaded와 유사하므로,
    // 이 블록 안에서 모든 초기화 작업을 수행하는 것이 일관성 있습니다.
    animateScore();
    animateProgressBars();
    createProgressChart(); // 이 함수는 현재 콘솔 로그만 출력합니다.
    animateCards();
    fetchChatGptFeedback(); // 페이지 로드 시 피드백 가져오기

});

// 예산 설정 페이지로 리다이렉트 함수 (모달 버튼용)
function redirectToBudgetSetting() {
    window.location.href = '/consumption/bplanner';
}

// createProgressChart 함수 (현재는 콘솔 로그만 출력)
function createProgressChart() {
    console.log("createProgressChart 함수가 호출되었습니다. (이 함수 내부에 차트 로직을 구현해야 합니다.)");
}

/*
// DOMContentLoaded 리스너는 $(document).ready()와 중복되므로 제거하거나,
// jQuery를 사용하지 않는 경우에만 사용합니다.
document.addEventListener("DOMContentLoaded", function () {
    animateScore();
    animateProgressBars(); // 이 부분은 $(document).ready()에서 호출되므로 중복될 수 있습니다.
    createProgressChart();
    animateCards();

    fetchChatGptFeedback();
});
*/

function animateScore() {
    const scoreElement = document.getElementById('overallScore');
    const targetScore = parseInt(scoreElement.innerText, 10);
    if (isNaN(targetScore)) {
        console.warn("overallScore가 유효한 숫자가 아닙니다. 애니메이션을 건너뜜.");
        scoreElement.textContent = "0"; // 기본값 설정
        return;
    }

    let currentScore = 0;
    const increment = targetScore / 50;

    const timer = setInterval(() => {
        currentScore += increment;
        if (currentScore >= targetScore) {
            currentScore = targetScore;
            clearInterval(timer);
        }
        scoreElement.textContent = Math.round(currentScore);
    }, 50);
}

function animateProgressBars() {
    // HTML에서 th:style로 설정된 width 값을 가져와 애니메이션
    const progressBars = document.querySelectorAll('.progress-fill');
    progressBars.forEach((bar, index) => {
        // bar.style.width는 현재 계산된 스타일 (예: "75px")을 반환할 수 있으므로,
        // CSS에서 설정된 width 값 (예: "75%")을 정확히 가져오기 위해 data 속성을 활용하거나,
        // 아니면 computed style을 사용해야 합니다.
        // 여기서는 HTML에 th:style로 %가 직접 들어가기 때문에, 그 값을 초기화하고 애니메이션하는 방식이 적합합니다.

        // 초기 너비를 0%로 설정하여 애니메이션 시작
        const initialWidth = bar.style.width; // '75%'와 같은 값 (Thymeleaf에서 설정된 값)
        bar.style.width = '0%'; // 애니메이션을 위해 0으로 초기화

        setTimeout(() => {
            bar.style.transition = 'width 2s ease-in-out'; // 애니메이션 효과 추가
            bar.style.width = initialWidth; // 원래 너비로 애니메이션
        }, 100 + (index * 200)); // 각 진행 바마다 약간의 지연을 줘서 순차적으로 보이게
    });
}

function animateCards() {
    const cards = document.querySelectorAll('.feedback-card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';

        setTimeout(() => {
            card.style.transition = 'all 0.6s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 150);
    });
}

// AI 피드백을 서버에 저장하고 종합 분석 페이지로 이동하는 함수
async function generateNewGoals(event) {
    const btn = event.target;
    btn.textContent = '전반적인 분석 중...';
    btn.disabled = true;

    const feedbackElement = document.getElementById('chatGptFeedbackContent');
    let aiFeedback = "";

    if (feedbackElement && feedbackElement.querySelector('p')) {
        aiFeedback = feedbackElement.querySelector('p').innerText;
    } else {
        console.warn("chatGptFeedbackContent 또는 그 안의 p 태그를 찾을 수 없습니다. 빈 문자열로 처리합니다.");
    }

    console.log("컨트롤러로 전송할 AI 피드백 내용:", aiFeedback);

    // CSRF 토큰과 헤더 이름을 HTML 메타 태그에서 가져옵니다.
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    // 요청 헤더를 동적으로 생성
    const headers = {
        'Content-Type': 'application/json'
    };
    // CSRF 토큰이 존재하면 헤더에 추가합니다.
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }

    try {
        const response = await fetch('/consumption/efeedset', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({
                feedbackContent: aiFeedback
            })
        });
        if (!response.ok) {
            const errorResult = await response.json().catch(() => {
                return response.text().then(text => ({ message: text || '알 수 없는 서버 오류' }));
            });
            throw new Error(errorResult.message || `서버 오류 발생: ${response.status}`);
        }

        const result = await response.json();
        console.log('피드백 저장 결과:', result);

        if (result.success) {
            alert(result.message);
            window.location.href = '/consumption/eanalysis';
        } else {
            if (result.message && result.message.includes("이미 저장된 피드백이 있습니다")) {
                if (confirm("이미 저장된 피드백이 있습니다. 저장하시겠습니까? (예를 누르면 다음 페이지로 이동합니다)")) {
                    alert("기존 피드백을 유지하고 다음 페이지로 이동합니다.");
                    window.location.href = '/consumption/eanalysis';
                } else {
                    alert("피드백 저장을 취소하고 다음 페이지로 이동합니다.");
                    window.location.href = '/consumption/eanalysis';
                }
            } else if (result.message && result.message.includes("계획이 없습니다")) {
                 if (confirm(result.message + "\n예산 설정 페이지로 이동하시겠습니까?")) {
                     window.location.href = '/consumption/bplanner';
                 } else {
                     btn.textContent = '전반적으로 분석하기';
                     btn.disabled = false;
                 }
            } else {
                alert("피드백 저장 실패: " + result.message);
                btn.textContent = '전반적으로 분석하기';
                btn.disabled = false;
            }
        }

    } catch (error) {
        console.error('AI 피드백 저장 중 오류 발생:', error);
        alert('AI 피드백 저장 중 오류가 발생했습니다: ' + error.message);
        btn.textContent = '전반적으로 분석하기';
        btn.disabled = false;
    }
}

// 화면에 알림을 띄우는 함수
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: linear-gradient(135deg, #ffd700, #ffb347);
        color: #8b4513;
        padding: 15px 20px;
        border-radius: 10px;
        box-shadow: 0 5px 15px rgba(255, 215, 0, 0.3);
        z-index: 1000;
        font-weight: bold;
        animation: slideIn 0.3s ease;
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// 30초마다 무작위 통찰 알림 표시
setInterval(() => {
    const insights = [
        "💡 팁: 포인트카드를 사용해서 절약해보세요!",
        "🎯 목표까지 달성은 성취감 뿐만 아니라 여유도 가져다 줘요!",
        "📊 야금야금을 사용하는 것만으로 절약 중!",
		" 좋은 습관은 합리적인 소비를 하는것! ",
		" 행복한 소비습관은 무조건 아끼는 것 보다 효율적이에요! "
    ];
    const randomInsight = insights[Math.floor(Math.random() * insights.length)];
    showNotification(randomInsight);
}, 30000);

// AI 피드백을 가져와 화면에 표시하는 함수
async function fetchChatGptFeedback() {
    const feedbackContentDiv = document.getElementById('chatGptFeedbackContent');
    if (!feedbackContentDiv) {
        console.error("ID가 'chatGptFeedbackContent'인 요소를 찾을 수 없습니다.");
        return;
    }
    feedbackContentDiv.innerHTML = '<p>AI가 현재 소비 패턴을 분석하여 개인 맞춤 피드백을 생성 중입니다... <span class="loading-spinner"></span></p>';

    try {
        const response = await fetch('/consumption/getChatGptFeedbackForEfeedback');
        const data = await response.json();

        if (data.feedback) {
            feedbackContentDiv.innerHTML = '<p>' + data.feedback.replace(/\n/g, '<br>') + '</p>';
            const goalStatusDiv = feedbackContentDiv.previousElementSibling.querySelector('.goal-status');
            if (goalStatusDiv) {
                 goalStatusDiv.textContent = '✔️ 분석 완료';
                 goalStatusDiv.classList.remove('status-warning');
                 goalStatusDiv.classList.add('status-success');
            }
        } else if (data.error) {
            feedbackContentDiv.innerHTML = '<p style="color: red;">오류: ' + data.error + '</p>';
            const goalStatusDiv = feedbackContentDiv.previousElementSibling.querySelector('.goal-status');
            if (goalStatusDiv) {
                 goalStatusDiv.textContent = '❌ 오류 발생';
                 goalStatusDiv.classList.remove('status-warning');
                 goalStatusDiv.classList.add('status-danger');
            }
        } else {
            feedbackContentDiv.innerHTML = '<p>피드백을 가져오는 데 실패했습니다.</p>';
             const goalStatusDiv = feedbackContentDiv.previousElementSibling.querySelector('.goal-status');
            if (goalStatusDiv) {
                 goalStatusDiv.textContent = '⚠️ 실패';
                 goalStatusDiv.classList.remove('status-warning');
                 goalStatusDiv.classList.add('status-warning');
            }
        }
    } catch (error) {
        console.error('ChatGPT 피드백을 가져오는 중 오류 발생:', error);
        feedbackContentDiv.innerHTML = '<p style="color: red;">피드백 요청 중 네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.</p>';
        const goalStatusDiv = feedbackContentDiv.previousElementSibling.querySelector('.goal-status');
        if (goalStatusDiv) {
            goalStatusDiv.textContent = '❌ 네트워크 오류';
            goalStatusDiv.classList.remove('status-warning');
            goalStatusDiv.classList.add('status-danger');
        }
    }
}

// 로그아웃 함수
function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
       window.location.href = '/logout';
    }
}