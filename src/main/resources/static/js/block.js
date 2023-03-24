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

