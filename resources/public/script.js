window.addEventListener('load', () => {
    let form = document.getElementById('form');
    let edit_dist = document.getElementById('edit_dist');
    let diff = document.getElementById('diff');

    async function dodiff () {
        let res = await (await fetch('/diff/' + entry_id,
                              {'method': 'POST',
                               'body': new FormData(form)})).json();
        diff.innerHTML = res.diff;
        edit_dist.innerText = res.edit_dist;
    }
    let el = document.getElementById('content');
    el.addEventListener('input', async function (event) {
        dodiff();
    });
    dodiff();
});
