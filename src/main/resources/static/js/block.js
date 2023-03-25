    const showDivButton = document.getElementById("show-deposit");
    const depositDiv = document.getElementById("deposit-div");

    showDivButton.addEventListener("click", () => {
        if (depositDiv.style.display === "none") {
            depositDiv.style.display = "block";
            withdrawDiv.style.display = "none";
            transferDiv.style.display = "none";
        } else {
            depositDiv.style.display = "none";
        }
    });

    const showDiv2Button = document.getElementById("show-withdraw");
    const withdrawDiv = document.getElementById("withdraw-div");

    showDiv2Button.addEventListener("click", () => {
        if (withdrawDiv.style.display === "none") {
            withdrawDiv.style.display = "block";
            depositDiv.style.display = "none";
            transferDiv.style.display = "none";
        } else {
            withdrawDiv.style.display = "none";
        }
    });

    const showDiv3Button = document.getElementById("show-transfer");
    const transferDiv = document.getElementById("transfer-div");

    showDiv3Button.addEventListener("click", () => {
        if (transferDiv.style.display === "none") {
            transferDiv.style.display = "block";
            depositDiv.style.display = "none";
            withdrawDiv.style.display = "none";
        } else {
            transferDiv.style.display = "none";
        }
    });

    const showInternal = document.getElementById("type-transfer");
    const internalDiv = document.getElementById("internal-div");

    showInternal.addEventListener("change", () => {
        if (showInternal.value === "1") {
            internalDiv.style.display = "block";
            externalDiv.style.display = "none";

        } else {
            internalDiv.style.display = "none";
        }
    });

    const showExternal = document.getElementById("type-transfer");
    const externalDiv = document.getElementById("external-div");
    const inputVal = document.getElementById("accId-transfer-to-external");

    showExternal.addEventListener("change", () => {
        if (showExternal.value === "2" || showExternal.value === "3") {
            externalDiv.style.display = "block";
            inputVal.value = "";
            internalDiv.style.display = "none";

        } else {
            externalDiv.style.display = "none";
            inputVal.value = "-1";
        }
    });