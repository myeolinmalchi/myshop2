function register() {
    const sellerDto = {
       sellerId: document.querySelector('#sellerId').value,
       sellerPw: document.querySelector("#sellerPw").value,
       name: document.querySelector("#name").value,
       email: document.querySelector("#email").value,
       phonenumber: document.querySelector("#phone").value
    }

    fetch("/seller/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Data-Type": "json"
        },
        body: JSON.stringify(sellerDto)
    })
        .then(response => response.json())
        .then(json => {
            if(json === true) location.href = "/seller"
            else alert(json.error)
        })
}

function login() {
    const sellerDto = {
        sellerId: document.querySelector('#sellerId').value,
        sellerPw: document.querySelector('#sellerPw').value,
        name: "",
        email: "",
        phonenumber: ""
    }

    fetch("/seller/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Data-Type": "json"
        },
        body: JSON.stringify(sellerDto)
    })
        .then(response => response.json())
        .then(json => {
            if(json === true) location.href = "/seller"
            else alert(json.error)
        });
}

function orThrow(element, query) {
    const value = element.querySelector(query).value
    if(value === undefined || value === "") {
        throw "빈칸이 있습니다."
    } else return value
}

function addProduct(sellerId) {
    try {
        const optionList = [...document.querySelectorAll('div.option')].map((option, index) => {
            const itemList = [...option.querySelectorAll('div.item')].map((item, index) => {
                return {
                    productOptionId: 0,
                    productOptionItemId: 0,
                    name: orThrow(item, 'input[name="name"]'),
                    surcharge: Number(orThrow(item, 'input[name="surcharge"]')),
                    stock: Number(orThrow(item, 'input[name="stock"]')),
                    itemSequence: index + 1
                }
            })

            return {
                productId: 0,
                productOptionId: 0,
                name: orThrow(option, 'input[name="name"]'),
                images: orThrow(option, 'input[name="images'),
                optionSequence: index + 1,
                itemList: itemList
            }
        })

        const product = {
            productId: 0,
            name: orThrow(document, 'input[name="name"]'),
            sellerId: sellerId,
            price: Number(orThrow(document, 'input[name="price"]')),
            categoryCode: orThrow(document, 'input[name="category"]'),
            detailInfo: orThrow(document, 'input[name="detailInfo"]'),
            thumbnail: orThrow(document, 'input[name="thumbnail"]'),
            reviewCount: 0,
            rating: 0,
            optionList: optionList
        }
        fetch("/seller/product", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Data-Type": "json"
            },
            body: JSON.stringify(product)
        })
            .then(response => response.json())
            .then(json => {
                console.log(json)
            });
    } catch(e) { alert(e) }
}

function addOption(element){
    let temp = document.querySelector("div.hidden-option").cloneNode(true)
    temp.classList.replace('hidden-option', 'option')

    let container = document.querySelector("div.container")
    container.appendChild(temp)
}

function addItem(element){
    let temp = document.querySelector("div.hidden-item").cloneNode(true)
    temp.classList.replace('hidden-item', 'item')

    element.parentNode.appendChild(temp)
}