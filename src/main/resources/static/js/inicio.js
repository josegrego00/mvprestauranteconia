// Script para manejar los subdominios
document.getElementById('btnCrearCuenta').addEventListener('click', function () {
    const subdominio = document.getElementById('subdominio').value.trim();
    if (subdominio === '') {
        alert('Por favor ingresa un nombre para tu restaurante');
        return;
    }
    if (!/^[a-z0-9-]+$/.test(subdominio)) {
        alert('El subdominio solo puede contener letras minúsculas, números y guiones');
        return;
    }
    window.location.href = '/registro?subdominio=' + encodeURIComponent(subdominio);
});

// Para usuarios registrados
document.getElementById('btnIrALogin').addEventListener('click', function () {
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