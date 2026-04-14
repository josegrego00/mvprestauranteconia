document.addEventListener('DOMContentLoaded', function() {
    // Aplicar precio sugerido al hacer clic en el botón
    document.querySelectorAll('.aplicar-sugerido').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const costo = parseFloat(this.getAttribute('data-costo'));
            const porcentaje = parseFloat(this.getAttribute('data-porcentaje'));
            const sugerido = costo * (1 + porcentaje / 100);
            
            // Encontrar el input de precio de venta en la misma fila
            const row = this.closest('tr');
            const input = row.querySelector('.precio-venta-input');
            input.value = sugerido.toFixed(2);
            
            // Actualizar el margen visualmente
            const precioVenta = parseFloat(input.value);
            if (precioVenta > 0 && costo > 0) {
                const margen = ((precioVenta - costo) / costo) * 100;
                const margenCell = row.querySelector('td:nth-child(6)');
                let clase = 'text-secondary';
                if (margen < 0) clase = 'text-danger';
                else if (margen <= 5) clase = 'text-warning';
                else clase = 'text-success';
                margenCell.innerHTML = '<span class="' + clase + '">' + margen.toFixed(2) + '%</span>';
            }
        });
    });

    // Guardar precio individual con AJAX
    document.querySelectorAll('.btn-guardar-precio').forEach(function(btn) {
        btn.addEventListener('click', function() {
            const productoId = this.getAttribute('data-producto-id');
            const row = this.closest('tr');
            const input = row.querySelector('.precio-venta-input');
            const nuevoPrecio = parseFloat(input.value);
            const costo = parseFloat(input.getAttribute('data-costo'));
            const btnGuardar = this;

            // Deshabilitar botón durante la solicitud
            btnGuardar.disabled = true;
            btnGuardar.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

            fetch('/ajuste-precios/actualizar-precio/' + productoId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: csrfParam + '=' + csrfToken + '&precio=' + nuevoPrecio
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Mostrar check de éxito
                    btnGuardar.classList.remove('btn-outline-success');
                    btnGuardar.classList.add('btn-success');
                    btnGuardar.innerHTML = '<i class="bi bi-check-lg"></i>';
                    
                    // Actualizar margen visualmente
                    if (nuevoPrecio > 0 && costo > 0) {
                        const margen = ((nuevoPrecio - costo) / costo) * 100;
                        const margenCell = row.querySelector('td:nth-child(6)');
                        let clase = 'text-secondary';
                        if (margen < 0) clase = 'text-danger';
                        else if (margen <= 5) clase = 'text-warning';
                        else clase = 'text-success';
                        margenCell.innerHTML = '<span class="' + clase + '">' + margen.toFixed(2) + '%</span>';
                    }
                    
                    // Restaurar después de 2 segundos
                    setTimeout(function() {
                        btnGuardar.classList.remove('btn-success');
                        btnGuardar.classList.add('btn-outline-success');
                        btnGuardar.innerHTML = '<i class="bi bi-check-lg"></i>';
                        btnGuardar.disabled = false;
                    }, 2000);
                } else {
                    alert('Error: ' + data.error);
                    btnGuardar.disabled = false;
                    btnGuardar.innerHTML = '<i class="bi bi-check-lg"></i>';
                }
            })
            .catch(function(error) {
                alert('Error al guardar el precio');
                btnGuardar.disabled = false;
                btnGuardar.innerHTML = '<i class="bi bi-check-lg"></i>';
            });
        });
    });
});