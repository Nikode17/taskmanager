// ===========================
// CONSTANTES Y CONFIGURACIÓN
// ===========================
const API_URL = 'http://localhost:8080';

// ===========================
// ELEMENTOS DEL DOM
// ===========================
const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const loginError = document.getElementById('loginError');
const registerError = document.getElementById('registerError');
const registerSuccess = document.getElementById('registerSuccess');

// ===========================
// EVENTO: LOGIN
// ===========================
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault(); // Evita que el formulario recargue la página

    // Obtener valores de los inputs
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    try {
        // Hacer petición POST al backend
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            // Login exitoso
            const data = await response.json();

            // Guardar token en localStorage
            localStorage.setItem('token', data.token);
            localStorage.setItem('email', data.email);

            // Redirigir al dashboard
            window.location.href = 'dashboard.html';
        } else {
            // Login fallido
            mostrarError(loginError, 'Email o contraseña incorrectos');
        }
    } catch (error) {
        mostrarError(loginError, 'Error de conexión con el servidor');
        console.error('Error:', error);
    }
});

// ===========================
// EVENTO: REGISTRO
// ===========================
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;

    // Validación básica
    if (password.length < 6) {
        mostrarError(registerError, 'La contraseña debe tener al menos 6 caracteres');
        return;
    }

    try {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        if (response.ok) {
            // Registro exitoso
            mostrarExito(registerSuccess, '¡Registro exitoso! Ahora puedes iniciar sesión');
            registerForm.reset(); // Limpiar formulario

            // Cambiar a la pestaña de login después de 2 segundos
            setTimeout(() => {
                document.getElementById('login-tab').click();
            }, 2000);
        } else {
            // Registro fallido
            mostrarError(registerError, 'El email ya está registrado');
        }
    } catch (error) {
        mostrarError(registerError, 'Error de conexión con el servidor');
        console.error('Error:', error);
    }
});

// ===========================
// FUNCIONES AUXILIARES
// ===========================
function mostrarError(elemento, mensaje) {
    elemento.textContent = mensaje;
    elemento.classList.remove('d-none');

    // Ocultar después de 5 segundos
    setTimeout(() => {
        elemento.classList.add('d-none');
    }, 5000);
}

function mostrarExito(elemento, mensaje) {
    elemento.textContent = mensaje;
    elemento.classList.remove('d-none');

    // Ocultar después de 5 segundos
    setTimeout(() => {
        elemento.classList.add('d-none');
    }, 5000);
}

// ===========================
// VERIFICAR SI YA ESTÁ LOGUEADO
// ===========================
window.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');

    // Si ya tiene token, redirigir al dashboard
    if (token) {
        window.location.href = 'dashboard.html';
    }
});