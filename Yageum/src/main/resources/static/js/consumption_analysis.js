// consumption_analysis.js

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
            const easedProgress = easeOutQuad(Math.min(progress, 1));
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
            chartContainer.innerHTML = '<p style="text-align:center; padding: 20px; color: #555;">데이터가 없습니다. 지출을 기록해주세요!</p>';
            chartContainer.style.display = 'flex';
            chartContainer.style.alignItems = 'center';
            chartContainer.style.justifyContent = 'center';
            chartContainer.style.minHeight = '200px';
        }
    } else {
        const ctx = document.getElementById('categoryChart').getContext('2d');
        const categoryChart = new Chart(ctx, {
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
            feedbackLoading.style.display = 'none';
            if (response.error) {
                feedbackError.style.display = 'block'; 
                aiFeedbackOutput.innerHTML = '<p style="color: red; margin-top: 20px;">오류: ' + response.error + '</p>';
            } else if (response.feedback) {
                aiFeedbackOutput.innerHTML = marked.parse(response.feedback);
            } else {
                aiFeedbackOutput.innerHTML = '<p style="color: #666; margin-top: 20px;">AI 피드백을 받아오지 못했습니다.</p>';
            }
        },
        error: function(xhr, status, error) {
            feedbackLoading.style.display = 'none'; 
            feedbackError.style.display = 'block';   
            console.error("AJAX Error:", status, error, xhr.responseText);
            aiFeedbackOutput.innerHTML = '<p style="color: red; margin-top: 20px;">피드백을 가져오는 중 네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.</p>';
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
    fetchAiFeedback();
});

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
       window.location.href = '/logout';
    }
}