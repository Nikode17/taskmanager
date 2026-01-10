// ===========================
// CONSTANTES Y CONFIGURACIÓN
// ===========================
const API_URL = 'http://localhost:8080';
let tasks = [];

// ===========================
// ELEMENTOS DEL DOM
// ===========================
const userEmail = document.getElementById('userEmail');
const logoutBtn = document.getElementById('logoutBtn');
const tasksContainer = document.getElementById('tasksContainer');
const emptyState = document.getElementById('emptyState');
const createTaskForm = document.getElementById('createTaskForm');
const saveTaskBtn = document.getElementById('saveTaskBtn');
const createTaskModal = new bootstrap.Modal(document.getElementById('createTaskModal'));

// ===========================
// VERIFICAR AUTENTICACIÓN
// ===========================
window.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    const email = localStorage.getItem('email');

    // Si no hay token, redirigir al login
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    // Mostrar email del usuario
    userEmail.textContent = email;

    // Cargar tareas
    cargarTareas();
});

// ===========================
// CARGAR TAREAS DESDE EL BACKEND
// ===========================
async function cargarTareas() {
    const token = localStorage.getItem('token');

    try {
        const response = await fetch(`${API_URL}/tasks`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            tasks = await response.json();
            renderizarTareas();
        } else if (response.status === 403 || response.status === 401) {
            // Token inválido o expirado
            alert('Sesión expirada. Por favor, inicia sesión nuevamente.');
            cerrarSesion();
        }
    } catch (error) {
        console.error('Error al cargar tareas:', error);
        mostrarError('Error al cargar las tareas');
    }
}

// ===========================
// RENDERIZAR TAREAS EN EL HTML
// ===========================
function renderizarTareas() {
    // Limpiar contenedor
    tasksContainer.innerHTML = '';

    // Si no hay tareas, mostrar mensaje
    if (tasks.length === 0) {
        emptyState.classList.remove('d-none');
        return;
    }

    // Ocultar mensaje vacío
    emptyState.classList.add('d-none');

    // Crear HTML para cada tarea
    tasks.forEach(task => {
        const taskCard = crearTarjetaTarea(task);
        tasksContainer.innerHTML += taskCard;
    });

    // Añadir event listeners a los botones de borrar
    document.querySelectorAll('.delete-task-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const taskId = e.target.dataset.id;
            borrarTarea(taskId);
        });
    });
}

// ===========================
// CREAR HTML DE UNA TAREA
// ===========================
function crearTarjetaTarea(task) {
    const completedClass = task.completed ? 'border-success' : 'border-secondary';
    const completedIcon = task.completed ? '✅' : '⭕';
    const completedText = task.completed ? 'text-decoration-line-through text-muted' : '';

    return `
        <div class="card mb-3 ${completedClass}" style="border-left: 4px solid;">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-start">
                    <div class="flex-grow-1">
                        <h5 class="card-title ${completedText}">
                            ${completedIcon} ${task.title}
                        </h5>
                        <p class="card-text text-muted ${completedText}">
                            ${task.description || 'Sin descripción'}
                        </p>
                    </div>
                    <button class="btn btn-danger btn-sm delete-task-btn" data-id="${task.id}">
                        🗑️ Borrar
                    </button>
                </div>
            </div>
        </div>
    `;
}

// ===========================
// CREAR NUEVA TAREA
// ===========================
saveTaskBtn.addEventListener('click', async () => {
    const title = document.getElementById('taskTitle').value;
    const description = document.getElementById('taskDescription').value;
    const completed = document.getElementById('taskCompleted').checked;

    // Validación
    if (!title.trim()) {
        alert('El título es obligatorio');
        return;
    }

    const token = localStorage.getItem('token');

    try {
        const response = await fetch(`${API_URL}/tasks`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                title: title.trim(),
                description: description.trim(),
                completed: completed
            })
        });

        if (response.ok) {
            // Cerrar modal
            createTaskModal.hide();

            // Limpiar formulario
            createTaskForm.reset();

            // Recargar tareas
            cargarTareas();

            // Mostrar mensaje de éxito
            mostrarExito('✅ Tarea creada correctamente');
        } else {
            alert('Error al crear la tarea');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error de conexión con el servidor');
    }
});

// ===========================
// BORRAR TAREA
// ===========================
async function borrarTarea(taskId) {
    // Confirmación
    if (!confirm('¿Estás seguro de que quieres borrar esta tarea?')) {
        return;
    }

    const token = localStorage.getItem('token');

    try {
        const response = await fetch(`${API_URL}/tasks/${taskId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            // Recargar tareas
            cargarTareas();

            // Mostrar mensaje de éxito
            mostrarExito('🗑️ Tarea eliminada correctamente');
        } else {
            alert('Error al borrar la tarea');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error de conexión con el servidor');
    }
}

// ===========================
// CERRAR SESIÓN
// ===========================
logoutBtn.addEventListener('click', () => {
    if (confirm('¿Estás seguro de que quieres cerrar sesión?')) {
        cerrarSesion();
    }
});

function cerrarSesion() {
    // Borrar token del localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('email');

    // Redirigir al login
    window.location.href = 'index.html';
}

// ===========================
// FUNCIONES AUXILIARES
// ===========================
function mostrarExito(mensaje) {
    // Crear notificación temporal
    const notification = document.createElement('div');
    notification.className = 'alert alert-success position-fixed top-0 start-50 translate-middle-x mt-3';
    notification.style.zIndex = '9999';
    notification.textContent = mensaje;

    document.body.appendChild(notification);

    // Eliminar después de 3 segundos
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

function mostrarError(mensaje) {
    const notification = document.createElement('div');
    notification.className = 'alert alert-danger position-fixed top-0 start-50 translate-middle-x mt-3';
    notification.style.zIndex = '9999';
    notification.textContent = mensaje;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 3000);
}