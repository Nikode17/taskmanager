package com.nicolas.taskmanager.controller;

import com.nicolas.taskmanager.model.Task;
import com.nicolas.taskmanager.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.saveTask(task);  //
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);  //
    }
}