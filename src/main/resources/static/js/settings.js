    const showDivButton = document.getElementById("create-acc");
    const createAccDiv = document.getElementById("newAcc-div");

    showDivButton.addEventListener("click", () => {
        if (createAccDiv.style.display === "none") {
            createAccDiv.style.display = "block";
            passwordDiv.style.display = "none"
        } else {
            createAccDiv.style.display = "none";
        }
    });

    const showDiv2Button = document.getElementById("password-change");
    const passwordDiv = document.getElementById("password-div");

    showDiv2Button.addEventListener("click", () => {
        if (passwordDiv.style.display === "none") {
            passwordDiv.style.display = "block";
            createAccDiv.style.display = "none";
        } else {
            passwordDiv.style.display = "none";
        }
    });