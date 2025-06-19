// consumption_analysis.js

let categoryChartInstance = null;

function animateNumbers() {
    document.querySelectorAll('.stat-number').forEach(element => {
        const textContent = element.textContent;
        let number = parseInt(textContent.replace('₩', '').replace(/,/g, ''));
        if (isNaN(number) || number === 0) {
            return;
        }
        let current = 0;
        const duration = 1000;
        let startTime = null;

        function easeOutQuad(t) {
            return t * (2 - t);
        }
        function animate(currentTime) {
            if (!startTime) startTime = currentTime;
            const progress = (currentTime - startTime) / duration;
            const easedProgress = Math.min(progress, 1);
            const animatedValue = Math.floor(easedProgress * number);
            element.textContent = '₩' + animatedValue.toLocaleString('ko-KR');

            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                element.textContent = textContent;
            }
        }
        requestAnimationFrame(animate);
    });
}

function createCategoryChart(categoryExpenses) {
    const labels = [];
    const data = [];
    const backgroundColors = [];
    const colorPalette = [
        '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF',
        '#FF9F40', '#E7E9ED', '#C9CBCE', '#A7AEB8', '#868B94',
        '#6B6B6B', '#FFD700', '#ADFF2F', '#8A2BE2', '#D2691E'
    ];

    categoryExpenses.forEach((item, index) => {
        labels.push(item.categoryName);
        data.push(Number(item.amount));
        backgroundColors.push(colorPalette[index % colorPalette.length]);
    });
    
    console.log("Labels for Chart:", labels);
    console.log("Data for Chart (amounts):", data);
    console.log("Data length:", data.length);
    console.log("Are all data values zero?", data.every(amount => amount === 0));

    if (data.length === 0 || data.every(amount => amount === 0)) {
        const chartContainer = document.querySelector('.chart-container');
        if (chartContainer) {
            if (categoryChartInstance) {
                categoryChartInstance.destroy();
                categoryChartInstance = null;
            }
            chartContainer.innerHTML = '<p style="text-align:center; padding: 20px; color: #555;">데이터가 없습니다. 지출을 기록해주세요!</p>';
            chartContainer.style.display = 'flex';
            chartContainer.style.alignItems = 'center';
            chartContainer.style.justifyContent = 'center';
            chartContainer.style.minHeight = '200px';
        }
    } else {
        const ctx = document.getElementById('categoryChart').getContext('2d');
		
		if (categoryChartInstance) {
		    categoryChartInstance.destroy();
		}
		
        categoryChartInstance = new Chart(ctx, { 
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: backgroundColors,
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'right',
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                let label = context.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                const value = context.parsed;
                                label += '₩' + value.toLocaleString('ko-KR');
                                return label;
                            },
                            afterLabel: function(context) {
                                const sum = context.dataset.data.reduce((a, b) => a + b, 0);
                                const currentValue = context.parsed;
                                const percentage = parseFloat((currentValue / sum * 100).toFixed(1));
                                return `(${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });
    }
}


function fetchAiFeedback() {
    console.log("fetchAiFeedback 함수 시작 - AJAX 요청 준비 중");
    const aiFeedbackOutput = document.getElementById('aiFeedbackOutput');
    const feedbackLoading = document.getElementById('feedbackLoading');
    const feedbackError = document.getElementById('feedbackError');

    const csrfHeader = 'X-CSRF-TOKEN';
    let csrfToken = document.querySelector('input[name="_csrf"]')?.value || ''; 
    
    aiFeedbackOutput.innerHTML = '<p style="color: #666; margin-top: 20px;">AI가 소비 데이터를 분석하여 맞춤형 피드백을 생성 중입니다.</p>';
    feedbackLoading.style.display = 'block'; 
    feedbackError.style.display = 'none';    

    $.ajax({
        url: "/consumption/getChatGptFeedbackForCanalysis",
        type: "GET",
        beforeSend: function(xhr) {
            if (csrfHeader && csrfHeader !== '' && csrfToken && csrfToken !== '') {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            } else {
                console.warn("CSRF token or header name is missing. Request might fail.");
            }
        },
        success: function(response) {
            console.log("AJAX 성공, 서버 응답:", response);
            feedbackLoading.style.display = 'none';
            if (response.error) {
                console.log("서버에서 에러 반환:", response.error);
                feedbackError.style.display = 'block';
                aiFeedbackOutput.innerHTML = '<p style="color: red; margin-top: 20px;">오류: ' + response.error + '</p>';
                window.currentAiFeedback = null; // 오류 발생 시 저장할 피드백 초기화
                window.hasExistingFeedback = false;
            } else if (response.feedback) {
                console.log("피드백 내용 존재, 화면에 표시");
                aiFeedbackOutput.innerHTML = marked.parse(response.feedback);
                
                // 피드백 내용을 전역 변수에 저장하여 버튼 클릭 시 접근 가능하게 함
                window.currentAiFeedback = response.feedback;
                window.hasExistingFeedback = response.hasExistingFeedback; // 기존 피드백 여부도 저장
            } else {
                console.log("피드백 없음, 에러도 아님");
                aiFeedbackOutput.innerHTML = '<p style="color: #666; margin-top: 20px;">AI 피드백을 받아오지 못했습니다.</p>';
                window.currentAiFeedback = null; // 피드백이 없으면 저장할 내용도 없음
                window.hasExistingFeedback = false;
            }
        },
        error: function(xhr, status, error) {
            console.error("AJAX Error:", status, error, xhr.responseText);
            feedbackLoading.style.display = 'none'; 
            feedbackError.style.display = 'block';   
            aiFeedbackOutput.innerHTML = '<p style="color: red; margin-top: 20px;">피드백을 가져오는 중 네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.</p>';
            window.currentAiFeedback = null; // 에러 발생 시 저장할 피드백 초기화
            window.hasExistingFeedback = false;
        }
    });
}

function saveAiFeedbackToDatabase(feedbackContent, csrfToken, isUpdate) {
    const csrfHeader = 'X-CSRF-TOKEN';
    $.ajax({
        url: "/consumption/saveAiFeedback",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({ 
            feedback: feedbackContent,
            isUpdate: isUpdate 
        }), 
        beforeSend: function(xhr) {
            if (csrfHeader && csrfHeader !== '' && csrfToken && csrfToken !== '') {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            } else {
                console.warn("CSRF token or header name is missing for save feedback request.");
            }
        },
        success: function(response) {
            console.log("AI 피드백 저장 응답:", response);
            if (response.message) {
                alert(response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error("AI 피드백 저장 중 오류 발생:", status, error, xhr.responseText);
            alert("피드백 저장 중 오류가 발생했습니다.");
        }
    });
}


document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.stat-card, .category-item').forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px) scale(1.02)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });

    const markdownTextElement = document.getElementById('categoryAnalysisInsightsHidden');
    if (markdownTextElement) {
        const markdownText = markdownTextElement.textContent;
        const htmlOutput = marked.parse(markdownText);
        const outputElement = document.getElementById('categoryAnalysisOutput');
        if (outputElement) {
            outputElement.innerHTML = htmlOutput;
        }
    }
    animateNumbers();
    createCategoryChart(categoryExpenses);
    fetchAiFeedback(); // 페이지 로드 시 AI 피드백을 가져와서 화면에 표시

    // AI 피드백 저장 버튼 클릭 이벤트 리스너 추가
    const saveAiFeedbackBtn = document.getElementById('saveAiFeedbackBtn');
    if (saveAiFeedbackBtn) {
        saveAiFeedbackBtn.addEventListener('click', function() {
            if (window.currentAiFeedback) { // 피드백 내용이 있는지 확인
                const csrfToken = document.querySelector('input[name="_csrf"]')?.value || '';
                if (window.hasExistingFeedback) {
                    if (confirm('이번 달에 이미 저장된 피드백이 있습니다. 업데이트하시겠습니까?')) {
                        saveAiFeedbackToDatabase(window.currentAiFeedback, csrfToken, true);
                    } else {
                        console.log('AI 피드백 저장을 취소했습니다.');
                        alert('AI 피드백 저장이 취소되었습니다.');
                    }
                } else {
                    if (confirm('AI 소비 분석 피드백을 저장하시겠습니까?')) {
                        saveAiFeedbackToDatabase(window.currentAiFeedback, csrfToken, false);
                    } else {
                        console.log('AI 피드백 저장을 취소했습니다.');
                        alert('AI 피드백 저장이 취소되었습니다.');
                    }
                }
            } else {
                alert('저장할 AI 피드백 내용이 없습니다. 먼저 피드백을 생성해주세요.');
            }
        });
    }
});

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
       window.location.href = '/logout';
    }
}