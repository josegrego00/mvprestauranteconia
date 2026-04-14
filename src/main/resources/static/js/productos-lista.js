document.addEventListener('DOMContentLoaded', function() {
    const productos = document.querySelectorAll('[id^="estimado-"]');
    productos.forEach(function(elem) {
        const productoId = elem.id.replace('estimado-', '');
        fetch('/productos/estimado/' + productoId)
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
                elem.title = 'Stock estimado: ' + valor + ' unidades';
            })
            .catch(error => {
                elem.innerHTML = '<span class="text-secondary">-</span>';
            });
    });
});