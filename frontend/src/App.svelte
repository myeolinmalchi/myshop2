<script>

    import {Router, Link, Route} from "svelte-routing"

    export let url = ""

    // Routes for User Home
    import UserHome from "./routes/user/Home.svelte"

    // Routes for User Account
    import Login from "./routes/user/account/Login.svelte"
    import Register from "./routes/user/account/Register.svelte"

    // Routes for User Cart
    import CartList from "./routes/user/order/CartList.svelte"

    // Routes for User Order


    // Routes for Product Search
    import Search from "./routes/user/search/Search.svelte"
    import ProductDetail from "./routes/user/search/ProductDetail.svelte"

    // Routes for Seller Home


    // Routes for Seller Account


    // Routes for Seller Order


    // Routes for Seller Product



    import Productcard from './Productcard.svelte'
    import Header from './Header.svelte'


</script>

<Router url="{url}">

    <nav>
        <Link to="/">UserHome</Link>
        <Link to="search">Search</Link>
    </nav>

    <div>
        <Route path="/" component="{UserHome}"></Route>
        <Route path="search"><Search/></Route>
    </div>

</Router>

