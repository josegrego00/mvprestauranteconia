// register.js - Validaciones y funcionalidades para el registro de usuarios

document.addEventListener('DOMContentLoaded', function() {
    
    // Elementos del formulario
    const registerForm = document.getElementById('registerForm');
    const nombreInput = document.getElementById('nombre');
    const nombreUsuarioInput = document.getElementById('nombreUsuario');
    const contrasennaInput = document.getElementById('contrasenna');
    const confirmarContrasennaInput = document.getElementById('confirmarContrasenna');
    const rolSelect = document.getElementById('rol');
    const estaActivoCheck = document.getElementById('estaActivo');
    const submitBtn = document.getElementById('submitBtn');
    const spinner = document.querySelector('.spinner');

    // Validación en tiempo real
    if (nombreInput) {
        nombreInput.addEventListener('blur', function() {
            validarNombre(this);
        });
    }

    if (nombreUsuarioInput) {
        nombreUsuarioInput.addEventListener('blur', function() {
            validarNombreUsuario(this);
        });
    }

    if (contrasennaInput) {
        contrasennaInput.addEventListener('input', function() {
            validarContrasenna(this);
            if (confirmarContrasennaInput.value) {
                validarConfirmacionContrasenna(confirmarContrasennaInput);
            }
        });
    }

    if (confirmarContrasennaInput) {
        confirmarContrasennaInput.addEventListener('input', function() {
            validarConfirmacionContrasenna(this);
        });
    }

    if (rolSelect) {
        rolSelect.addEventListener('change', function() {
            mostrarBadgeRol(this.value);
        });
    }

    // Validación al enviar el formulario
    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            if (validarFormulario()) {
                await enviarFormulario();
            }
        });
    }

    /**
     * Función para validar nombre completo
     */
    function validarNombre(input) {
        const valor = input.value.trim();
        const errorDiv = document.getElementById('nombreError');
        
        if (valor.length < 3) {
            mostrarError(input, errorDiv, 'El nombre debe tener al menos 3 caracteres');
            return false;
        } else if (valor.length > 100) {
            mostrarError(input, errorDiv, 'El nombre no puede exceder los 100 caracteres');
            return false;
        } else if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/.test(valor)) {
            mostrarError(input, errorDiv, 'El nombre solo puede contener letras y espacios');
            return false;
        } else {
            mostrarExito(input, errorDiv);
            return true;
        }
    }

    /**
     * Función para validar nombre de usuario
     */
    function validarNombreUsuario(input) {
        const valor = input.value.trim();
        const errorDiv = document.getElementById('nombreUsuarioError');
        
        if (valor.length < 4) {
            mostrarError(input, errorDiv, 'El nombre de usuario debe tener al menos 4 caracteres');
            return false;
        } else if (valor.length > 50) {
            mostrarError(input, errorDiv, 'El nombre de usuario no puede exceder los 50 caracteres');
            return false;
        } else if (!/^[a-zA-Z0-9_]+$/.test(valor)) {
            mostrarError(input, errorDiv, 'Solo se permiten letras, números y guión bajo');
            return false;
        } else {
            mostrarExito(input, errorDiv);
            return true;
        }
    }

    /**
     * Función para validar contraseña
     */
    function validarContrasenna(input) {
        const valor = input.value;
        const errorDiv = document.getElementById('contrasennaError');
        const requisitos = {
            length: valor.length >= 6,
            uppercase: /[A-Z]/.test(valor),
            lowercase: /[a-z]/.test(valor),
            number: /\d/.test(valor),
            special: /[!@#$%^&*(),.?":{}|<>]/.test(valor)
        };
        
        // Actualizar indicadores visuales de requisitos
        actualizarRequisitosContrasenna(requisitos);
        
        if (Object.values(requisitos).every(Boolean)) {
            mostrarExito(input, errorDiv);
            return true;
        } else {
            mostrarError(input, errorDiv, 'La contraseña debe cumplir todos los requisitos');
            return false;
        }
    }

    /**
     * Función para validar confirmación de contraseña
     */
    function validarConfirmacionContrasenna(input) {
        const valor = input.value;
        const contrasenna = contrasennaInput.value;
        const errorDiv = document.getElementById('confirmarContrasennaError');
        
        if (valor !== contrasenna) {
            mostrarError(input, errorDiv, 'Las contraseñas no coinciden');
            return false;
        } else if (valor === '') {
            mostrarError(input, errorDiv, 'Debe confirmar su contraseña');
            return false;
        } else {
            mostrarExito(input, errorDiv);
            return true;
        }
    }

    /**
     * Función para mostrar error
     */
    function mostrarError(input, errorDiv, mensaje) {
        input.classList.add('is-invalid');
        input.classList.remove('is-valid');
        if (errorDiv) {
            errorDiv.innerHTML = `<i class="bi bi-exclamation-circle"></i> ${mensaje}`;
            errorDiv.style.display = 'flex';
        }
    }

    /**
     * Función para mostrar éxito
     */
    function mostrarExito(input, errorDiv) {
        input.classList.remove('is-invalid');
        input.classList.add('is-valid');
        if (errorDiv) {
            errorDiv.style.display = 'none';
        }
    }

    /**
     * Función para actualizar indicadores de requisitos de contraseña
     */
    function actualizarRequisitosContrasenna(requisitos) {
        const elementos = {
            length: document.getElementById('req-length'),
            uppercase: document.getElementById('req-uppercase'),
            lowercase: document.getElementById('req-lowercase'),
            number: document.getElementById('req-number'),
            special: document.getElementById('req-special')
        };
        
        Object.keys(requisitos).forEach(key => {
            if (elementos[key]) {
                if (requisitos[key]) {
                    elementos[key].innerHTML = `<i class="bi bi-check-circle-fill text-success"></i> ${obtenerTextoRequisito(key)}`;
                } else {
                    elementos[key].innerHTML = `<i class="bi bi-circle text-secondary"></i> ${obtenerTextoRequisito(key)}`;
                }
            }
        });
    }

    /**
     * Función para obtener texto de requisitos
     */
    function obtenerTextoRequisito(key) {
        const textos = {
            length: 'Mínimo 6 caracteres',
            uppercase: 'Una mayúscula',
            lowercase: 'Una minúscula',
            number: 'Un número',
            special: 'Un carácter especial'
        };
        return textos[key] || key;
    }

    /**
     * Función para mostrar badge del rol
     */
    function mostrarBadgeRol(rol) {
        const badgeContainer = document.getElementById('rolBadge');
        if (!badgeContainer) return;
        
        const badges = {
            'ADMIN': '<span class="role-badge admin">Administrador</span>',
            'CAJERO': '<span class="role-badge cajero">Cajero</span>',
            'DESARROLLADOR': '<span class="role-badge desarrollador">Desarrollador</span>'
        };
        
        badgeContainer.innerHTML = badges[rol] || '';
    }

    /**
     * Función para validar todo el formulario
     */
    function validarFormulario() {
        const nombreValido = validarNombre(nombreInput);
        const usuarioValido = validarNombreUsuario(nombreUsuarioInput);
        const contrasennaValida = validarContrasenna(contrasennaInput);
        const confirmacionValida = validarConfirmacionContrasenna(confirmarContrasennaInput);
        const rolValido = rolSelect.value !== '';
        
        if (!rolValido) {
            mostrarError(rolSelect, document.getElementById('rolError'), 'Debe seleccionar un rol');
        }
        
        return nombreValido && usuarioValido && contrasennaValida && confirmacionValida && rolValido;
    }

    /**
     * Función para enviar el formulario
     */
    async function enviarFormulario() {
        // Mostrar spinner y deshabilitar botón
        spinner.style.display = 'block';
        submitBtn.disabled = true;
        
        // Preparar datos del formulario
        const formData = {
            nombre: nombreInput.value.trim(),
            nombreUsuario: nombreUsuarioInput.value.trim(),
            contrasenna: contrasennaInput.value,
            rol: rolSelect.value,
            estaActivo: estaActivoCheck.checked
        };
        
        try {
            // Simular envío - Reemplazar con tu endpoint real
            const response = await fetch('/api/usuarios/registro', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
            
            if (response.ok) {
                mostrarMensajeExito('Usuario registrado exitosamente');
                setTimeout(() => {
                    window.location.href = '/login';
                }, 2000);
            } else {
                const error = await response.json();
                mostrarMensajeError(error.message || 'Error al registrar usuario');
            }
        } catch (error) {
            mostrarMensajeError('Error de conexión. Intente nuevamente.');
        } finally {
            // Ocultar spinner y habilitar botón
            spinner.style.display = 'none';
            submitBtn.disabled = false;
        }
    }

    /**
     * Función para mostrar mensaje de éxito
     */
    function mostrarMensajeExito(mensaje) {
        const alerta = document.createElement('div');
        alerta.className = 'success-message';
        alerta.innerHTML = `<i class="bi bi-check-circle-fill"></i> ${mensaje}`;
        
        const container = document.querySelector('.register-body');
        container.insertBefore(alerta, container.firstChild);
        
        setTimeout(() => {
            alerta.remove();
        }, 3000);
    }

    /**
     * Función para mostrar mensaje de error
     */
    function mostrarMensajeError(mensaje) {
        const alerta = document.createElement('div');
        alerta.className = 'alert alert-danger';
        alerta.innerHTML = `<i class="bi bi-exclamation-triangle-fill"></i> ${mensaje}`;
        alerta.style.cssText = 'border-radius: 10px; margin-bottom: 20px;';
        
        const container = document.querySelector('.register-body');
        container.insertBefore(alerta, container.firstChild);
        
        setTimeout(() => {
            alerta.remove();
        }, 3000);
    }

    // Tooltips personalizados
    const tooltips = document.querySelectorAll('[data-tooltip]');
    tooltips.forEach(element => {
        element.addEventListener('mouseenter', (e) => {
            const tooltip = document.createElement('div');
            tooltip.className = 'custom-tooltip';
            tooltip.textContent = e.target.dataset.tooltip;
            tooltip.style.cssText = `
                position: absolute;
                background: #2c3e50;
                color: white;
                padding: 5px 10px;
                border-radius: 5px;
                font-size: 0.8rem;
                z-index: 1000;
                pointer-events: none;
            `;
            document.body.appendChild(tooltip);
            
            const rect = e.target.getBoundingClientRect();
            tooltip.style.top = rect.top - tooltip.offsetHeight - 5 + 'px';
            tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
            
            element.addEventListener('mouseleave', () => {
                tooltip.remove();
            });
        });
    });
});