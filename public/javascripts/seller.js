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

function getBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
    });
}


const registerButton = document.getElementById("register-button")

registerButton.addEventListener('click', () => {
    const name = orThrow(document, 'input[name="name"]')
    addProduct("minsuk4820", name+i)
})

async function addProduct(sellerId, productName) {
    try {
        const imagesDiv = document.querySelector('div.images')
        const imageStrings = await Promise.all(
            [...imagesDiv.querySelectorAll('input[name="image"]')].map((image, index) => {
                return getBase64(image.files[0]).then(result => result.toString())
        }))

        const imageList = imageStrings.map((src, index) => {
            return {
                productId: 0,
                productImageId: 0,
                image: src,
                sequence: index + 1
            }
        })

        const optionList = [...document.querySelectorAll('div.option')].map((option, index) => {
            const itemList = [...option.querySelectorAll('div.item')].map((item, index) => {
                return {
                    productOptionId: 0,
                    productOptionItemId: 0,
                    name: orThrow(item, 'input[name="name"]'),
                    surcharge: Number(orThrow(item, 'input[name="surcharge"]')),
                    itemSequence: index+1
                }
            })
            return {
                productId: 0,
                productOptionId: 0,
                name: orThrow(option, 'input[name="name"]'),
                optionSequence: index + 1,
                itemList: itemList
            }
        })

        const categories = document.querySelector('select.last-category')
        const category = categories.options[categories.selectedIndex].value
        const product = {
            productId: 0,
            name: productName,
            sellerId: sellerId,
            price: Number(orThrow(document, 'input[name="price"]')),
            categoryCode: category,
            detailInfo: orThrow(document, 'input[name="detailInfo"]'),
            thumbnail: await getBase64(document.querySelector('input[name="thumbnail"]').files[0]),
            reviewCount: 0,
            rating: 0,
            optionList: await Promise.all(optionList),
            imageList: imageList
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
                if (json === true) {
                    console.log("상품이 등록되었습니다.")
                    location.href = '/seller/product'
                } else { alert(json.error)
                    console.log(json.error)}
            });
    } catch (e) {
        console.log(e)
    }
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

function addImage(){
    let temp = document.querySelector("div.hidden-image").cloneNode(true)
    temp.classList.replace('hidden-image', 'image')

    let images = document.querySelector("div.images")
    images.appendChild(temp)
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
    newBox.classList.replace('hidden-category','category')

    while(true){
        if(catStack.length() < code.length) break;
        catStack.pop().remove()
    }

    fetch("/category/children", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Data-Type": "json"
        },
        body: JSON.stringify(code)
    })
        .then(response => response.json())
        .then(json => {
            console.log(json)
            if(json !== null && json.length !==0){
                json.forEach((cat, index) => {
                    let option = document.createElement('option');
                    option.setAttribute('value', cat[0])
                    option.label = cat[1]
                    newBox.setAttribute('class', 'category')
                    newBox.querySelector('select.category').append(option)
                    console.log(cat)
                })
                document.querySelector('div.category-aria').append(newBox)
                catStack.push(newBox)
            }
            else target.className += ' last-category'
        })
}

function getProducts() {
    fetch("/seller/product")
        .then(response => response.json())
        .then(json => {
            console.log(json)
            json.forEach((product, index) => {

            })
        })
}