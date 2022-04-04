const idBox = document.getElementById("userId")
const pwBox = document.getElementById("userPw")
const nameBox = document.getElementById("name")
const emailBox = document.getElementById("email")
const phoneBox = document.getElementById("phone")
const registerButton = document.getElementById("register")
const backButton = document.getElementById("back")

registerButton.addEventListener('click', () => {
    fetch("/user/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Data-Type": "json"
        },
        body: JSON.stringify({
            userId: idBox.value,
            userPw: pwBox.value,
            name: nameBox.value,
            email: emailBox.value,
            phonenumber: phoneBox.value
        })
    })
        .then(response => response.json())
        .then(json => {
            if(json === true) location.href = "/"
            else alert(json.error)
        })
})

backButton.addEventListener('click', () => {
    location.href = "/"
})
