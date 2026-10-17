# Task Manager

API REST de gestión de tareas con autenticación JWT y un frontend ligero en HTML, CSS y JavaScript.

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 17 + Spring Boot 3.2 |
| Seguridad | Spring Security + JWT (jjwt), contraseñas con BCrypt |
| Persistencia | Spring Data JPA + MySQL |
| Frontend | HTML, CSS y JavaScript sin frameworks |

## Funcionalidades

- Registro e inicio de sesión con token JWT (validez de 10 horas).
- Crear, listar y borrar tareas desde el dashboard.
- Rutas de la API protegidas por un filtro JWT.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/register` | Crea un usuario y devuelve un token |
| POST | `/auth/login` | Devuelve un token |
| GET | `/tasks` | Lista las tareas |
| POST | `/tasks` | Crea una tarea |
| DELETE | `/tasks/{id}` | Borra una tarea |

## Ejecutar en local

Requisitos: Java 17 o superior y MySQL 8.

1. Crea la base de datos:

   ```sql
   CREATE DATABASE taskmanager_db;
   ```

2. Define la contraseña de MySQL como variable de entorno (nunca en el código):

   ```powershell
   # PowerShell
   $env:DB_PASSWORD = "tu_contraseña"
   ```

   ```bash
   # bash
   export DB_PASSWORD="tu_contraseña"
   ```

   Opcionales: `DB_URL` (por defecto `jdbc:mysql://localhost:3306/taskmanager_db`) y `DB_USERNAME` (por defecto `root`).

3. Arranca la aplicación:

   ```bash
   ./mvnw spring-boot:run
   ```

4. Abre [http://localhost:8080](http://localhost:8080).

La clave de firma de los JWT se genera al arrancar, así que los tokens dejan de valer cada vez que se reinicia la aplicación.
