package com.nicolas.taskmanager.exception;

// La tarea no existe o pertenece a otro usuario. Ambos casos se tratan igual
// para no revelar qué ids existen.
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Tarea no encontrada: " + id);
    }
}
