document.addEventListener('DOMContentLoaded', function() {
	    const sections = document.querySelectorAll('.payment-section');
	    const editBtn = document.querySelector('.btn-edit');
	    const deleteBtn = document.querySelector('.btn-delete');
	    const addBtn = document.querySelector('.btn-add');
	    const cancelBtn = document.querySelector('.btn-cancel');

	    const allCheckboxes = document.querySelectorAll('.select-checkbox');

	    // 편집 모드 ON
	    editBtn.addEventListener('click', function () {
	        sections.forEach(section => section.classList.add('edit-mode'));
	        editBtn.style.display = 'none';
	        addBtn.style.display = 'none';
	        deleteBtn.style.display = 'inline-block';
	        cancelBtn.style.display = 'inline-block';
	        allCheckboxes.forEach(cb => cb.checked = false);
	    });

	    // 편집 모드 OFF
	    cancelBtn.addEventListener('click', function () {
	        sections.forEach(section => section.classList.remove('edit-mode'));
	        editBtn.style.display = 'inline-block';
	        addBtn.style.display = 'inline-block';
	        deleteBtn.style.display = 'none';
	        cancelBtn.style.display = 'none';
	        allCheckboxes.forEach(cb => cb.checked = false);
	    });

	    // 추가 버튼 → 기존처럼 이동
	    addBtn.addEventListener('click', function () {
	        window.location.href = '/mypage/minsert';
	    });
	});
	
	document.getElementById('globalDeleteBtn').addEventListener('click', function () {
	    const checked = document.querySelectorAll('.select-checkbox:checked');
	    if (checked.length === 0) {
	        alert("삭제할 항목을 선택해주세요.");
	        return;
	    }

	    if (!confirm("선택한 항목을 삭제하시겠습니까?")) return;

	    const form = document.getElementById('deleteForm');

	    // 기존 input 정리
	    while (form.children.length > 1) {
	        form.removeChild(form.lastChild);
	    }

	    // 선택 항목의 id/type 추가
	    checked.forEach(cb => {
	        const item = cb.closest('.payment-item');
	        const id = item.getAttribute('data-id');
	        const type = item.getAttribute('data-type');

	        const idInput = document.createElement("input");
	        idInput.type = "hidden";
	        idInput.name = "ids";
	        idInput.value = id;

	        const typeInput = document.createElement("input");
	        typeInput.type = "hidden";
	        typeInput.name = "types";
	        typeInput.value = type;

	        form.appendChild(idInput);
	        form.appendChild(typeInput);
	    });

	    form.submit();
	});
	
	// 로그아웃 함수
	function logout() {
	    if (confirm('로그아웃 하시겠습니까?')) {
	       window.location.href = '/logout';
	    }
	}