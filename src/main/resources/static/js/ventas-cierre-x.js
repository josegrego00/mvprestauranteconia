function actualizarHora() {
    const ahora = new Date();
    const hora = String(ahora.getHours()).padStart(2, '0');
    const minuto = String(ahora.getMinutes()).padStart(2, '0');
    const segundo = String(ahora.getSeconds()).padStart(2, '0');
    const horaCompleta = hora + ':' + minuto + ':' + segundo;
    document.getElementById('horaImpresion').textContent = horaCompleta;
    document.getElementById('horaFooter').textContent = horaCompleta;
}
actualizarHora();
setInterval(actualizarHora, 1000);