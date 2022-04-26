<script>
    export let productId
    export let thispage

    let product = fetch(`http://localhost:9000/api/v1/product/${productId}`)
        .then(response => response.json())

    $: selectedItems = []

    $: surcharge = (selectedItems.length===0)?(0):(selectedItems.reduce((acc, x) => Number(acc)+Number(x[1]),0))
    let hiddenPrice
    $: price = (hiddenPrice===undefined)?(0):(hiddenPrice.value)
    $: totPrice = Number(price) + Number(surcharge)


</script>

<input type="hidden" id="option-count" value="@product.optionList.size">
<input type="hidden" id="product_id" value="@product.productId">
<input type="hidden" id="name" value="@product.name">

{#await product}
{:then product}
<div class="container mt-3 px-0">
    <div class="row mx-auto">
        <div class="col-1 b-1 px-2">
            <div class="row g-1 my-auto">
                <img class="col-12 border" style="height:70px" src={product.thumbnail} alt="{product.name}">
                {#each product.imageList as image}
                    <img class="col-12 border" style="height:70px" src={image.image} alt="{product.name}"> 
                {/each}
            </div>
        </div>
        <div class="col-5">
            <div class="row w-100 mx-auto my-1 border">
                <img src={product.thumbnail} alt="">
            </div>
        </div>
        <div class="col-6">
            <div class="row border-bottom">
                <p class="ms-0 mt-2 mb-1 px-1 lh-1 fs-4">{product.name}</p>
                {#if product.rating !== 0}
                    <p class="card-text">
                        {#each Array(product.rating/2) as _}
                            <i class="bi bi-star-fill text-warning"></i>
                        {/each}
                        {#if product.rating%2 === 1}
                            <i class="bi bi-star-half text-warning"></i> 
                        {/if}
                        {#each Array(5-product.rating/2-product.rating%2) as _}
                            <i class="bi bi-star-fill text-warning"></i>
                        {/each}
                    </p>
                {:else}
                    <p class="card-text text-muted px-1">리뷰 없음</p> 
                {/if}
            </div>
            <div class="row border-bottom">
                <input type="hidden" value="@product.price" name="price">
                <p id="price" class="ms-0 my-3 px-1 lh-1 fs-5">{product.price} 원</p>
            </div>

            <!------------ 옵션 -------------->
            {#each product.optionList as option, index}
                <div class="row border-bottom p-0">
                    <p class="mx-auto mb-0 mt-2 px-1 lh-1 fs-6"></p>
                    <div id="{option.productOptionId}" class="row mx-auto px-1">
                        <div id="radio-wrapper {option.productOptionId}"
                            class="d-grid gap-1 d-flex p-0 mx-auto my-2 flex-wrap">
                            {option.name}
                            {#each option.itemList as item}
                                <div>
                                    <input type="radio" class="btn-check" autocomplete="off"
                                        id="item{item.productOptionItemId}"
                                        bind:group={selectedItems[index]}
                                        value={[item.productOptionItemId, item.surcharge]}>
                                    <label for="item{item.productOptionItemId}"
                                        class="btn btn-sm btn-outline-secondary w-auto">{item.name}</label>
                                </div>
                            {/each}
                        </div>
                    </div>
                </div>
            {/each}
            <!------------ 옵션 -------------->

            <!------------ 수량조절, 가격 -------------->
            <div class="row border-bottom py-2">
                <div class="input-group col px-0">
                    <span class="input-group-text bg-white" id="sub-quantity">-</span>
                    <input name="quantity" id="quantity" type="number" class="form-control text-center" value="1">
                    <span class="input-group-text bg-white" id="add-quantity">+</span>
                </div>
                <input type="hidden" bind:this={hiddenPrice} value={product.price}>
                <p class="mb-0 mt-2 px-1 lh-1 fs-4 col-9 text-end" id="totPrice">
                    {totPrice} 원
                </p>
            </div>
            <!------------ 수량조절, 가격 -------------->

            <!------------ 구매, 장바구니 버튼 -------------->
            <div class="row py-2 d-flex gap-1 float-end">
                <button class="w-auto btn btn-primary" type="button"
                                                       id="add-cart-button" value="cart" on:click={() => console.log(price)}>장바구니담기</button>
                <button class="w-auto btn btn-primary" type="button" value="order">구매하기</button>
            </div>
            <!------------ 구매, 장바구니 버튼 -------------->
        </div>
    </div>

    <div class="row mx-auto my-3">
        <ul class="nav nav-tabs fs-5">
            <li class="nav-item w-25">
                <a class="nav-link text-center text-dark active" aria-current="page" href="#">상세정보</a>
            </li>
            <li class="nav-item w-25">
                <a class="nav-link text-center text-secondary" href="#">상품평</a>
            </li>
            <li class="nav-item w-25">
                <a class="nav-link text-center text-secondary">상품문의</a>
            </li>
            <li class="nav-item w-25">
                <a class="nav-link text-center text-secondary"></a>
            </li>
        </ul>
    </div>

    <input type="button" class="button" value="리뷰 작성" id="review-write-button">

</div>
{/await}
