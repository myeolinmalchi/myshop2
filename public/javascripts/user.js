function register() {
    const userDto = {
        userId: document.querySelector('#userId').value,
        userPw: document.querySelector("#userPw").value,
        name: document.querySelector("#name").value,
        email: document.querySelector("#email").value,
        phonenumber: document.querySelector("#phone").value
    }

    fetch("/user/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Data-Type": "json"
        },
        body: JSON.stringify(userDto)
    })
        .then(response => response.json())
        .then(json => {
            if(json === true) location.href = "/"
            else alert(json.error)
        })
}

function login() {
    const userDto = {
        userId: document.querySelector('#userId').value,
        userPw: document.querySelector('#userPw').value,
        name: "",
        email: "",
        phonenumber: ""
    }

    fetch("/user/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Data-Type": "json"
        },
        body: JSON.stringify(userDto)
    })
        .then(response => response.json())
        .then(json => {
            if(json === true) location.href = "/user"
            else alert(json.error)
        });
}

function cartList(){
    const init = {
        method: 'GET',
        headers: {
            'Content-Type':'application/json'
        },
    };
    fetch('/user/carts', init)
        .then(response => response.json())
        .then(data => console.log(data))
}