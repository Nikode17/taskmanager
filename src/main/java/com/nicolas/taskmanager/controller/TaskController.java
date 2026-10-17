package com.nicolas.taskmanager.controller;

import com.nicolas.taskmanager.dto.TaskRequest;
import com.nicolas.taskmanager.dto.TaskResponse;
import com.nicolas.taskmanager.exception.TaskNotFoundException;
import com.nicolas.taskmanager.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// authentication.getName() es el email que JwtAuthenticationFilter saca del token
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> getTasks(Authentication authentication) {
        return taskService.getTasks(authentication.getName());
    }

    @PostMapping
    public TaskResponse createTask(@RequestBody TaskRequest request, Authentication authentication) {
        return taskService.createTask(request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id, Authentication authentication) {
        taskService.deleteTask(id, authentication.getName());
    }

    // El 404 se escribe aquí directamente: con @ResponseStatus Spring reenviaría a /error,
    // que está protegido por SecurityConfig, y el cliente recibiría un 403
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<Void> handleTaskNotFound() {
        return ResponseEntity.notFound().build();
    }
}
