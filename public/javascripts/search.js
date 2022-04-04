function searchPage(page) {

    let url = new URL(location.href);
    let params = url.searchParams
    params.set('page', page);

    location.href=url.href

}

function setSeq(element) {
    let seq = element.getAttribute('name')
    element.getClass += 'active'
    let url = new URL(location.href);
    let params = url.searchParams
    params.set('seq', seq)
    location.href = url.href
}