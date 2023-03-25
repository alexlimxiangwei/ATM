    const showDivButton = document.getElementById("create-acc");
    const createAccDiv = document.getElementById("newAcc-div");

    showDivButton.addEventListener("click", () => {
        if (createAccDiv.style.display === "none") {
            createAccDiv.style.display = "block";
            passwordDiv.style.display = "none"
            editDiv.style.display = "none";
            deleteDiv.style.display = "none";
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
            editDiv.style.display = "none";
            deleteDiv.style.display = "none";
        } else {
            passwordDiv.style.display = "none";
        }
    });

    const showDiv3Button = document.getElementById("edit-acc");
    const editDiv = document.getElementById("edit-div");

    showDiv3Button.addEventListener("click", () => {
        if (editDiv.style.display === "none") {
            editDiv.style.display = "block";
            createAccDiv.style.display = "none";
            passwordDiv.style.display = "none";
            deleteDiv.style.display = "none";
        } else {
            editDiv.style.display = "none";
        }
    });

    const showDiv4Button = document.getElementById("delete-acc");
    const deleteDiv = document.getElementById("delete-div");

    showDiv4Button.addEventListener("click", () => {
        if (deleteDiv.style.display === "none") {
            deleteDiv.style.display = "block";
            createAccDiv.style.display = "none";
            editDiv.style.display = "none";
            passwordDiv.style.display = "none";
        } else {
            deleteDiv.style.display = "none";
        }
    });