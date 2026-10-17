package com.nicolas.taskmanager.dto;

import com.nicolas.taskmanager.model.Task;

// Lo que la API devuelve de una tarea. Nunca expone la entidad User (ni su contraseña).
public class TaskResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final boolean completed;

    public TaskResponse(Long id, String title, String description, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
    }

    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.isCompleted());
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }
}
