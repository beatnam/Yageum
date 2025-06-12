function execDaumPostcode() {
        new daum.Postcode({
            oncomplete: function (data) {
                document.getElementById("postcode_lbl").value = data.zonecode;
                document.getElementById("memberAddress").value = data.address;
            }
        }).open();
    }