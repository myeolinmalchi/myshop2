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

function cartList(){
    const init = {
        method: 'GET',
        headers: {
            'Content-Type':'application/json'
        }
    };
    fetch('/user/carts', init)
        .then(response => response.json())
        .then(data => console.log(data))
}

function search(){
    let category = document.querySelector("select.category")
    let code = category.options[category.selectedIndex].value
    let keyword = document.querySelector("input[name='keyword']").value

    const url = new URL("http://localhost:9000/search/"+code+"/"+keyword)
    location.href = url.href
}