// 카드번호 자동 이동
  const cardInputs = document.querySelectorAll('.card-number-input');
  cardInputs.forEach((input, index) => {
    input.addEventListener('input', function () {
      this.value = this.value.replace(/[^0-9]/g, '');
      if (this.value.length === 4 && index < cardInputs.length - 1) {
        cardInputs[index + 1].focus();
      }
    });
    input.addEventListener('keydown', function (e) {
      if (e.key === 'Backspace' && this.value.length === 0 && index > 0) {
        cardInputs[index - 1].focus();
      }
    });
  });

  // 유효기간 입력 자동 이동
  const expiryInputs = document.querySelectorAll('.expiry-input');
  expiryInputs.forEach((input, index) => {
    input.addEventListener('input', function () {
      this.value = this.value.replace(/[^0-9]/g, '');
      if (this.value.length === 2 && index === 0) {
        expiryInputs[1].focus();
      }
    });
    input.addEventListener('keydown', function (e) {
      if (e.key === 'Backspace' && this.value.length === 0 && index === 1) {
        expiryInputs[0].focus();
      }
    });
  });

  // CVC 숫자만 입력
//  document.getElementById('cvc').addEventListener('input', function () {
//    this.value = this.value.replace(/[^0-9]/g, '');
//  });

  // 폼 유효성 검사 함수
  function validateForm() {
    let isValid = true;

    const expiryMM = document.querySelector('input[name="expiryMM"]');
    const expiryYY = document.querySelector('input[name="expiryYY"]');
    const cardHolder = document.querySelector('input[name="cardHolder"]');
    //const cvc = document.querySelector('input[name="cvc"]');
    const cardType = document.querySelector('select[name="cardType"]');
    const cardName = document.querySelector('input[name="cardName"]');
    const cardInputs = document.querySelectorAll('.card-number-input');

    const cardNumber = Array.from(cardInputs).map(input => input.value).join('');
    if (cardNumber.length !== 16) {
      showError('cardNumberError', '카드번호 16자리를 모두 입력해주세요.');
      isValid = false;
    } else {
      hideError('cardNumberError');
    }

    if (!expiryMM.value || !expiryYY.value || expiryMM.value < 1 || expiryMM.value > 12) {
      showError('expiryError', '올바른 유효기간을 입력해주세요.');
      isValid = false;
    } else {
      hideError('expiryError');
    }

    if (!cardHolder.value.trim()) {
      showError('cardHolderError', '소유자명을 입력해주세요.');
      isValid = false;
    } else {
      hideError('cardHolderError');
    }

//    if (cvc.value.length !== 3) {
//      showError('cvcError', 'CVC 3자리를 입력해주세요.');
//      isValid = false;
//    } else {
//      hideError('cvcError');
//    }

    if (!cardType.value) {
      showError('cardTypeError', '카드 유형을 선택해주세요.');
      isValid = false;
    } else {
      hideError('cardTypeError');
    }

    if (!cardName.value.trim()) {
      showError('cardNameError', '카드이름을 입력해주세요.');
      isValid = false;
    } else {
      hideError('cardNameError');
    }

    return isValid;
  }

  // 에러 메시지 출력/제거 함수
  function showError(id, message) {
    document.getElementById(id).textContent = message;
  }
  function hideError(id) {
    document.getElementById(id).textContent = '';
  }
  
  const binToCcInMap = {
	 "457973": "27", // 국민카드
	 "539923": "27", // 국민카드
	 "545763": "28", // 신한카드
	 "403160": "28", // 신한카드
	 "552260": "30", // 현대카드
	 "941012": "39", // 토스카드
	 "515594": "30", // 현대카드
	 "438676": "34", // NH농협카드
	 "356317": "28", // 신한카드
	 "530957": "29", // 삼성카드
	 "545231": "29", // 삼성카드
	 "538189": "31", // 롯데카드
	 "524853": "31", // 롯데카드
	 "518055": "32", // 하나카드
	 "521324": "32", // 하나카드
	 "438033": "33", // 우리카드
	 "434631": "33", // 우리카드
	 "543394": "35", // 씨티카드
	 "516856": "35", // 씨티카드
	 "356085": "36", // IBK기업은행카드
	 "524107": "36", // IBK기업은행카드
	 "516129": "37", // SC제일은행카드
	 "515672": "37", // SC제일은행카드
	 "522865": "38", // 카카오뱅크카드
	 "531278": "38", // 카카오뱅크카드
	 "517805": "40", // 수협카드
	 "516219": "41", // 케이뱅크카드
	 "485462": "41", // 케이뱅크카드
	 "408295": "34", // NH농협카드
	 "402870": "27", // 국민카드
	 "356879": "28", // 신한카드
	 "558644": "30", // 현대카드
	 "532664": "31", // 롯데카드
	 "516841": "32"  // 하나카드
  };

  const cardCorporationSelect = document.getElementById('cardCorporation');

  cardInputs.forEach(input => {
    input.addEventListener('input', () => {
      const fullNumber = Array.from(cardInputs).map(i => i.value).join('');
      if (fullNumber.length >= 6) {
        const bin = fullNumber.substring(0, 6);
        const ccIn = binToCcInMap[bin];
        if (ccIn) {
          cardCorporationSelect.value = ccIn;
          console.log("자동 선택된 카드사 cc_in:", ccIn);
        } else {
          console.warn("BIN 매칭 실패:", bin);
        }
      }
    });
  });
  
  
  /*
  카드사 자동 매핑 api사용
  : 외부 API의 불안정성과 호출 제한 문제로 인해 프론트엔드 자체 BIN 매핑 방식으로 전환
  const bankNameMap = {
	  "Kb Kookmin Card Co., Ltd.": "27",
      "KB Kookmin Card": "27",
      "Kookmin Card": "27",
      "Shinhan Card": "28",
      "Samsung Card": "29",
      "Hyundai Card": "30",
      "Lotte Card": "31",
      "Hana Card": "32",
      "Woori Card": "33",
      "NH Nonghyup Card": "34",
      "Citi": "35",
      "Citibank": "35",
      "IBK": "36",
      "SC First Bank": "37",
      "SC Bank": "37",
      "KakaoBank": "38",
      "Toss Bank": "39",
      "Suhyup Bank": "40",
      "KBank": "41"
    };

    const cardCorporationSelect = document.getElementById('cardCorporation');

	
    cardInputs.forEach(input => {
      input.addEventListener('input', async () => {
        const fullNumber = Array.from(cardInputs).map(i => i.value).join('');
        if (fullNumber.length >= 6) {
          const bin = fullNumber.substring(0, 6);
          try {
            const res = await fetch(`/mypage/binlookup/${bin}`);
            if (!res.ok) throw new Error("BIN 조회 실패");

            const data = await res.json();
            const bankName = data.bank?.name;
            console.log("조회된 카드사명:", bankName);

            const matchedValue = bankNameMap[bankName];
            if (matchedValue) {
              cardCorporationSelect.value = matchedValue;
              console.log("자동 선택된 cc_in 값:", matchedValue);
            } else {
              console.warn("매칭 실패:", bankName);
            }
          } catch (e) {
            console.error("BIN API 오류:", e);
          }
        }
      });
    });
	*/
    

    // DOM 로드 후 실행
    //document.addEventListener('DOMContentLoaded', autoDetectcardCorporation);
	
	// 로그아웃 함수
	function logout() {
	    if (confirm('로그아웃 하시겠습니까?')) {
	       window.location.href = '/logout';
	    }
	}