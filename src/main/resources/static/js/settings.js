    const showDivButton = document.getElementById("create-acc");
    const createAccDiv = document.getElementById("newAcc-div");

    showDivButton.addEventListener("click", () => {
        if (createAccDiv.style.display === "none") {
            createAccDiv.style.display = "block";
        } else {
            createAccDiv.style.display = "none";
        }
    });