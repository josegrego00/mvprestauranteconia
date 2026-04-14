document.addEventListener('DOMContentLoaded', function() {
    const recetas = document.querySelectorAll('[id^="stock-"]');
    recetas.forEach(function(elem) {
        const recetaId = elem.id.replace('stock-', '');
        fetch('/recetas/stock/' + recetaId)
            .then(response => response.json())
            .then(data => {
                const valor = Math.floor(data);
                let clase = 'text-success';
                let icono = 'bi-box-seam';
                if (valor === 0) {
                    clase = 'text-danger';
                    icono = 'bi-exclamation-triangle';
                } else if (valor < 5) {
                    clase = 'text-warning';
                    icono = 'bi-exclamation-circle';
                }
                elem.innerHTML = '<i class="bi ' + icono + ' ' + clase + ' fw-bold"></i> ' + valor;
                elem.title = 'Stock disponible: ' + valor + ' unidades';
            })
            .catch(error => {
                elem.innerHTML = '<span class="text-secondary">-</span>';
            });
    });
});