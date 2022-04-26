<script>
    import ProductCard from './ProductCard.svelte'

    export let keyword
    export let code
    export let page
    export let size
    export let seq
    export let productInfo
    export let thispage

    export let productId
    
</script>

<div class="container p-0 mt-0">
    <div class="row mx-auto px-0">
        <div class="col-2 border border-top-0" style="height:1500px"></div>

        <div class="col-10 border-end">
            <div class="row-vh mt-4 mb-3 d-flex px-3">
                <h5 class="mb-0">'{keyword}'에 대한 검색 결과</h5>
            </div>
            <div class="row my-2 px-3">
                <div></div>
                <div class="btn-group btn-group-sm w-auto" role="group">
                    <a on:click={() => seq = 0} class="btn btn-outline-secondary"
                        aria-current="page">가격 낮은순</a>
                    <a on:click={() => seq = 1} class="btn btn-outline-secondary"
                        aria-current="page">가격 높은순</a>
                    <button type="button" class="btn btn-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
                        {size}개씩 보기
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li><a href="#" on:click={() => size=36} class="dropdown-item">36개씩 보기</a></li>
                        <li><a href="#" on:click={() => size=48} class="dropdown-item">48개씩 보기</a></li>
                        <li><a href="#" on:click={() => size=60} class="dropdown-item">60개씩 보기</a></li>
                        <li><a href="#" on:click={() => size=72} class="dropdown-item">72개씩 보기</a></li>
                    </ul>
                </div>
            </div>
            {#await productInfo}
                <h2>상품을 불러오는 중입니다...</h2>
            {:then productInfo}
                <div class="row g-3 px-3 pb-3 my-auto">
                        {#each productInfo[0] as product} 
                            <ProductCard product={product} bind:productId={productId} bind:thispage={thispage}/>
                        {/each}
                </div>
                <div class="row mt-3">
                    <nav aria-label="Page navigation example">
                        <ul class="pagination justify-content-center">
                            {#each Array(productInfo[1]+1) as _, index}
                                {#if index+1 === Number(page)}
                                    <li class="page-item active">
                                        <a class="page-link" on:click={() => page = index+1}>{index+1}</a>
                                    </li>
                                {:else} 
                                    <li class="page-item">
                                        <a class="page-link" on:click={() => page = index+1}>{index+1}</a>
                                    </li>
                                {/if}

                            {/each}
                        </ul>
                    </nav>
                </div>
            {/await}
        </div>
    </div>
</div>
