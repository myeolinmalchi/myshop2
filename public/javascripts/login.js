const idBox = document.getElementById("userId")
const pwBox = document.getElementById("userPw")
const loginButton = document.getElementById('login')
const registerButton = document.getElementById("register")


const login = () => {
    fetch("/user/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Data-Type": "json"
        },
        body: JSON.stringify({
            userId: idBox.value,
            userPw: pwBox.value,
            name: "",
            email: "",
            phonenumber: ""
        })
    }).then(response => response.json())
        .then(json => {
            if(json === true) location.href="/"
            else alert(json.error)
        })
}

loginButton.addEventListener('click' , login)
registerButton.addEventListener('click', () => {
    location.href = '/user/register'
})