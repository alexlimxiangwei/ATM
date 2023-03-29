const cancelLink = document.querySelector('a[href="http://localhost:8090/signOut"]');
cancelLink.addEventListener("click", function(event) {
  const result = confirm("Are you sure you want to sign out ?");
  if (result === false) {
     event.preventDefault();
  }
});