const primaryHeader = document.getElementById("primary-header");
primaryHeader.addEventListener("click", () => {
    primaryHeader.style.color = "green";
    primaryHeader.style.fontSize = "100px";
});
primaryHeader.textContent = "Welcome to Spring Boot App";   