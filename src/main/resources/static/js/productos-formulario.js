// Bootstrap validation
(function () {
    'use strict'
    var forms = document.querySelectorAll('.needs-validation')
    Array.prototype.slice.call(forms).forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!form.checkValidity()) {
                event.preventDefault()
                event.stopPropagation()
            }
            form.classList.add('was-validated')
        }, false)
    })
})()

function toggleStock() {
    const checkbox = document.getElementById('tieneReceta');
    const stockContainer = document.getElementById('stockContainer');
    const recetaContainer = document.getElementById('recetaContainer');

    if (checkbox.checked) {
        stockContainer.style.display = 'none';
        recetaContainer.style.display = 'block';
    } else {
        stockContainer.style.display = 'block';
        recetaContainer.style.display = 'none';
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const checkbox = document.getElementById('tieneReceta');

    toggleStock(); // inicial

    checkbox.addEventListener('change', toggleStock);
});