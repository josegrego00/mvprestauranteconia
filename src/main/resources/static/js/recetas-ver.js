document.addEventListener('DOMContentLoaded', function() {
    const recetaId = window.location.pathname.split('/').pop();
    fetch('/recetas/stock/' + recetaId)
        .then(response => response.json())
        .then(data => {
            const valor = Math.floor(data);
            const elem = document.getElementById('stock-disponible');
            let clase = 'text-success';
            if (valor === 0) {
                clase = 'text-danger';
            } else if (valor < 5) {
                clase = 'text-warning';
            }
            elem.innerHTML = '<span class="' + clase + '">' + valor + '</span>';
        })
        .catch(error => {
            document.getElementById('stock-disponible').innerHTML = '<span class="text-secondary">-</span>';
        });
});