function sendMessage() {
     const inputField = document.getElementById("user-input");
     const input = inputField.value.trim();
     if (input === "") return;

     appendUserMessage(input);
     const response = getBotResponse(input);
     appendBotMessage(response);
     inputField.value = "";
   }

   function getBotResponse(input) {
     const msg = input.toLowerCase();

     if (msg.includes("계좌")) {
       return "📌 계좌는 오픈뱅킹 인증 후 추가하실 수 있어요!";
     } else if (msg.includes("카드")) {
       return "💳 카드 추가는 마이페이지 > 결제수단 관리에서 가능합니다.";
     } else if (msg.includes("비밀번호")) {
       return "🔐 비밀번호 변경은 마이페이지 > 회원정보 수정에서 가능합니다.";
     } else if (msg.includes("탈퇴") || msg.includes("탈퇴")) {
       return "⚠️ 회원 탈퇴는 마이페이지 > 회원탈퇴 메뉴에서 진행하실 수 있어요.";
     } else if (msg.includes("삭제")) {
	   return "가계부 삭제는 가계부 상세보기 페이지에서 삭제 버튼을 누르면 됩니다!";
     }
	 
	 
	 else {
       return `😢 정확한 답변을 찾지 못했어요.<br>
               👉 <button onclick="location.href='/chat'">실시간 상담 시작하기</button>`;
     }
   }

   function appendUserMessage(text) {
     const chatLog = document.getElementById("chat-log");
     chatLog.innerHTML += `<div class="chat-message user"><strong>나:</strong> ${text}</div>`;
     chatLog.scrollTop = chatLog.scrollHeight;
   }

   function appendBotMessage(text) {
     const chatLog = document.getElementById("chat-log");
     chatLog.innerHTML += `<div class="chat-message bot"><strong>챗봇:</strong> ${text}</div>`;
     chatLog.scrollTop = chatLog.scrollHeight;
   }