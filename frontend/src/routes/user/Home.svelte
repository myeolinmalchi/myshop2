<script>

    import Header from '../../components/common/Header.svelte'
    import ProductList from '../../components/product/ProductList.svelte'
    import ProductDetail from '../../components/product/ProductDetail.svelte'

    let keyword = "test"
    let code = ""
    let page = 1
    let size = 48
    let seq = 0

    let thispage = "home"

    $: productInfo = fetch(`http://localhost:9000/api/v1/product/search/${keyword}?code=${code}&page=${page}&size=${size}&seq=${seq}`)
            .then(response => response.json())

    let productId = 0
</script>
<Header 
    bind:keyword={keyword}
    bind:code={code}
    bind:thispage={thispage} 
/>
{#if thispage === "home"}

{:else if thispage === "search"}
    <ProductList 
        bind:keyword={keyword}
        bind:page={page}
        bind:code={code}
        bind:size={size}
        bind:seq={seq}
        bind:productInfo={productInfo}
        bind:productId={productId}
        bind:thispage={thispage}
        />
{:else if thispage === "productDetail"}
    <ProductDetail
        bind:productId={productId}
        bind:thispage={thispage}
        />

{:else if thispage === "cart"}

{:else if thispage === "login"}

{:else if thispage === "register"}

{/if}
