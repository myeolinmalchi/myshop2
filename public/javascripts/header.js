const categoryBox = document.querySelector('div.category');
const keywordBox = document.querySelector('input[name="keyword"]');

window.onload = () => {
    fetch("/category")
        .then(response => response.json())
        .then(json => {
            json.forEach((cat, index) => {
                let option = document.createElement('option');
                option.setAttribute('value', cat[0])
                option.label = cat[1]
                categoryBox.querySelector('select.category').append(option)
            })
        })
}

keywordBox.addEventListener('keydown', (e) => {
    if(e.keyCode === 13) {
        let category = document.querySelector("select.category")
        let code = category.options[category.selectedIndex].value
        let keyword = document.querySelector("input[name='keyword']").value
        console.log(keyword)
        location.href = "/search/"+code+"/"+keyword
    }
})