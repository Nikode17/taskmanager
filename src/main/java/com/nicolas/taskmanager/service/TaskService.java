package com.nicolas.taskmanager.service;

import com.nicolas.taskmanager.dto.TaskRequest;
import com.nicolas.taskmanager.dto.TaskResponse;
import com.nicolas.taskmanager.exception.TaskNotFoundException;
import com.nicolas.taskmanager.model.Task;
import com.nicolas.taskmanager.model.User;
import com.nicolas.taskmanager.repository.TaskRepository;
import com.nicolas.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // Solo las tareas del usuario autenticado
    public List<TaskResponse> getTasks(String email) {
        return taskRepository.findByUserEmail(email).stream()
                .map(TaskResponse::from)
                .toList();
    }

    // El dueño sale siempre del token, nunca del cuerpo de la petición
    public TaskResponse createTask(TaskRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado: " + email));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(request.isCompleted());
        task.setUser(user);

        return TaskResponse.from(taskRepository.save(task));
    }

    // Solo se borran tareas propias; las de otros usuarios se tratan como inexistentes
    public void deleteTask(Long id, String email) {
        Task task = taskRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskRepository.delete(task);
    }
}
