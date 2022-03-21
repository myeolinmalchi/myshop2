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

function addProduct(sellerId) { try {
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
            if(json === true) {
                alert("상품이 등록되었습니다.")
                location.href='/seller/product'
            } else alert(json.error)
        });
} catch(e) { alert(e) } }

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

class Stack {
    constructor() {
        this._arr = [];
    }
    push(item) {
        this._arr.push(item);
    }
    pop() {
        return this._arr.pop();
    }
    peek() {
        return this._arr[this._arr.length - 1];
    }

    length(){
        return this._arr.length;
    }
}
const catStack = new Stack();

function nextCategories(target) {
    const code = target.options[target.selectedIndex].value
    const newBox = document.querySelector('div.hidden-category').cloneNode(true)
    cat.classList.replace('hidden-category','category')

    while(true){
        if(catStack.length() < code.length) break;
        $(catStack.pop()).remove();
    }

    fetch("/category/children", {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Data-Type": "json"
        },
        body: JSON.stringify(code)
    })
        .then(response => response.json())
        .then(json => {
            [json].forEach((cat, index) => {
                newBox.setAttribute('id', 'code')
                newBox.querySelector('select.category')
                    .append('<option value="'.concat(cat.code,'">',cat.name,'</option>'));
            })
            document.querySelector('div.category-aria')
                .append(newBox)
        })
}