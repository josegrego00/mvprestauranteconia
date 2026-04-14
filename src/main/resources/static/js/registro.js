// Validación del formulario
(function () {
    'use strict';
    var forms = document.querySelectorAll('.needs-validation');
    Array.prototype.slice.call(forms).forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
})();

// Login rápido
document.getElementById('btnLogin')?.addEventListener('click', function () {
    const subdominio = document.getElementById('subdominioLogin').value.trim();
    if (subdominio === '') {
        alert('Por favor ingresa el subdominio de tu restaurante');
        return;
    }
    if (!/^[a-z0-9-]+$/.test(subdominio)) {
        alert('El subdominio solo puede contener letras minúsculas, números y guiones');
        return;
    }
    window.location.href = 'http://' + subdominio + '.localhost:8080/login';
});

// Enter en el campo de login
document.getElementById('subdominioLogin')?.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        document.getElementById('btnLogin').click();
    }
});