const docQuery = (query) => document.querySelector(query)
const docQueryAll = (query) => document.querySelectorAll(query)

const deleteButtons = docQueryAll("button[name='deleteButton']")
const updateQuantityButtons = docQueryAll("button[name='updateQuantityButton']")

const orderButton = document.getElementById("orderButton")

const deleteCart = (e) => {
    const target = e.currentTarget
    const cartId = target.previousElementSibling.value
    fetch('/user/carts', {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            "Data-Type": "json"
        },
        body: JSON.stringify({ cartId: Number(cartId) })
    }).then(response => response.json())
        .then(json => {
            if(json === true) target.parentNode.parentNode.remove()
            else alert(json.error)
        })
}

const newOrder = (e) => {
    const checkedBoxes = document.querySelectorAll('input[name="cartCheck"]:checked')
    if(checkedBoxes.length === 0) alert("선택된 상품이 없습니다.")
    else {
        const cartIdList = [...checkedBoxes].map((box, index) => {
            return Number(box.parentNode.parentNode.querySelector('input[name="cartId"]').value) })

        fetch("/user/order", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Data-Type": "json"
            },
            body: JSON.stringify({cartIdList: cartIdList})
        }).then(response => response.json())
            .then(json => {
                if(json === true) {
                    alert("주문이 완료되었습니다.")
                    location.href="/"
                } else alert(json.error)
            })
    }
}

orderButton.addEventListener('click', newOrder)


const updateCartQuantity = (e) => {
    const target = e.currentTarget
    const cartId = target.previousElementSibling.previousElementSibling.value
    const quantity = target.previousElementSibling.value
    fetch('/user/carts', {
        method: "PUT",
        headers: {
            "Content-Type": "application/json",
            "Data-Type": "json"
        },
        body: JSON.stringify({
            cartId: Number(cartId),
            quantity: Number(quantity)
        })
    }).then(response => response.json())
        .then(json => {
            if(json === true){
                const price = target.parentNode.parentNode.nextElementSibling.value
                const totPriceBox = target.parentNode.parentNode.nextElementSibling.nextElementSibling
                const totPrice = quantity * price
                totPriceBox.innerHTML = totPrice + " 원"
            }
            else alert(json.error)
        })
}

[...deleteButtons].forEach((button, index) => button.addEventListener('click', deleteCart));
[...updateQuantityButtons].forEach((button, index) => button.addEventListener('click', updateCartQuantity));
