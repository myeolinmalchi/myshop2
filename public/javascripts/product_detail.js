const optionCount = Number(document.getElementById("option-count").value)
const userId = document.getElementById("user_id").value

const token = document.cookie.split('=')[1]
const uuid = document.cookie.split('=')[1].split('-')[2]

const price = Number(document.querySelector('input[name="price"]').value)
const totalPriceBox = document.querySelector('#totPrice')
const totalPrice = totalPriceBox.previousElementSibling
const itemInputs = document.querySelectorAll('input[name^="option"]')

const quantityInput = document.getElementById("quantity")
const addQuantityButton = document.getElementById("add-quantity")
const subQuantityButton = document.getElementById("sub-quantity")

const addCartButton = document.getElementById("add-cart-button")

const updateTotalPrice = () => {
    const checkedItems = document.querySelectorAll('input[name^="option"]:checked')
    const totalSurcharge = [...checkedItems].map((item, index) => {
        return item.nextElementSibling.nextElementSibling.value
    }).reduce((a, b) => Number(a)+Number(b))
    const result = price + Number(totalSurcharge)
    setTotalPrice(result)
}

const setTotalPrice = (result) => {
    const quantity = quantityInput.value
    totalPriceBox.innerHTML = result*quantity + " 원"
    totalPrice.value = result
}

[...itemInputs].forEach((item) =>
    item.addEventListener('change', updateTotalPrice))

const addQuantity = () => {
    quantityInput.value = Number(quantityInput.value) + 1
    setTotalPrice(totalPrice.value)
}

const subQuantity = () => {
    if(quantityInput.value > 1) {
        quantityInput.value = Number(quantityInput.value) - 1
        setTotalPrice(totalPrice.value)
    }
}

addQuantityButton.addEventListener('click', addQuantity)
subQuantityButton.addEventListener('click', subQuantity)

const readItems = () => {
    const checkedItems = document.querySelectorAll('input[name^="option"]:checked')
    return [...checkedItems].map((item, index) => {
        return {
            productOptionId: 0,
            productOptionItemId: Number(item.previousElementSibling.value),
            name: item.previousElementSibling.previousElementSibling.value,
            itemSequence: 0,
            surcharge: 0
        }
    })
}
const readCart = (itemList) => {
    const itemNames = itemList.map((item, index) => item.name)
    const productName = document.getElementById("name").value
    const combinedItemName = "("+itemNames.reduce((a, b) => a+", "+b)+")"
    const finalProductName = productName+combinedItemName

    return {
        userId: userId,
        cartId: 0,
        name: finalProductName,
        productId: Number(document.getElementById("product_id").value),
        price: Number(totalPrice.value),
        quantity: Number(quantityInput.value),
        addedDate: new Date(),
        itemList: itemList
    }
}
const addCart = (cartDto) => {
    fetch("/user/carts", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Data-Type": "json"
        },
        redirect: 'follow',
        body: JSON.stringify(cartDto)
    }).then(response => response.json())
        .then(json => {
            if(json === true) location.href="/user/carts"
            else alert(json.error)
        })
}

addCartButton.addEventListener('click', (e) => {
    const itemList = readItems()
    console.log(itemList.length)
    if(itemList.length === 0) {
        alert("모든 옵션을 지정해주세요.")
    } else if(userId === "") {
        let cartDto = readCart(itemList)
        cartDto.userId = token
        addCart(cartDto)
    } else addCart(readCart(itemList))
})