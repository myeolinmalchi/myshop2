<script>
    export let keyword
    export let code

    const categories = fetch("http://localhost:9000/category")
            .then(response => response.json())
</script>

<div class="container pt-4 p-0">
    <div class="d-flex flex-row justify-content-center align-items-center gap-2">
        <div class="text-center px-1">
            <span class="h1">Myshop</span>
        </div>
        {#await categories}
        <div class="p-0">
            <select form="search" name="category"
                    class="form-select border-dark" style="height:40px">
                <option selected value="">전체</option>
            </select>
        </div>
        {:then categories}
        <div class="p-0">
            <select bind:value={code} form="search" name="category"
                    class="form-select border-dark" style="height:40px">
                <option selected value="">전체</option>
                {#each categories as category}
                    <option value="{category[0]}">{category[1]}</option>
                {/each}
            </select>
        </div>
        {/await}
        <div class="p-0" style="width:400px;">
            <input name="name" class="form-control border-dark" type="text" 
             placeholder="상품명으로 검색" aria-label="Search" style="height:40px" bind:value={keyword}>
        </div>
        <div class="p-0">
            <button type="button" class="btn btn-outline-success position-relative btn-sm w-100" style="height:40px"
                    onclick="location.href='/cart'">
                장바구니
            </button>
        </div>
        <div class="p-0">
            <button type="button" class="btn btn-outline-dark position-relative btn-sm w-100" style="height:40px"
                    onclick="location.href='../mypage">
                마이페이지
            </button>
        </div>
    </div>
    <div class="d-flex flex-row flex-nowrap mt-3 bg-primary mx-auto align-items-center text-white text-center px-4" style="height:50px;">
        <div class="text-center text-white" style="width:100px">카테고리9</div>
    </div>
</div>
