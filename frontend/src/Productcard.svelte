<script>
    export let product
    export let productId
    export let thispage

    const renderDetailPage = (e) => {
        thipage = "productDetail"
        productId = product.productId 
    }
</script>

<div class="col-3">
    <div class="card rounded-0" on:click={renderDetailPage}>
        <img src="{product.thumbnail}" alt="">
        <div class="card-body">
            <h5 class="card-title">{product.name}</h5>
            <p class="card-text mb-1">{product.price}</p>
            {#if product.rating!=0}
                <p class="card-text">
                    {#each Array(product.rating/2) as _, index}
                        <i class="bi bi-star-fill text-warning"></i>
                    {/each}
                    {#if product.rating%2==1}
                        <i class="bi bi-star-half text-warning"></i> 
                    {/if}
                    {#each Array(5-product.rating/2 - product.rating%2) as _, index}
                        <i class="bi bi-star text-warning"></i> }
                    {/each}
                    <span class="text-muted">{product.reviewCount}</span>
                </p>
            {:else}
                <p class="card-text text-muted">리뷰 없음</p>
            {/if}
        </div>
    </div>
</div>
