package com.nicolas.taskmanager.service;

import com.nicolas.taskmanager.dto.TaskRequest;
import com.nicolas.taskmanager.dto.TaskResponse;
import com.nicolas.taskmanager.exception.TaskNotFoundException;
import com.nicolas.taskmanager.model.Task;
import com.nicolas.taskmanager.model.User;
import com.nicolas.taskmanager.repository.TaskRepository;
import com.nicolas.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final String ANA = "ana@example.com";

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getTasksOnlyQueriesTheCallersTasks() {
        Task task = new Task();
        task.setTitle("Estudiar Spring");
        when(taskRepository.findByUserEmail(ANA)).thenReturn(List.of(task));

        List<TaskResponse> tasks = taskService.getTasks(ANA);

        assertThat(tasks).extracting(TaskResponse::getTitle).containsExactly("Estudiar Spring");
        verify(taskRepository, never()).findAll();
    }

    @Test
    void createTaskAssignsTheAuthenticatedUserAsOwner() {
        User ana = new User();
        ana.setEmail(ANA);
        when(userRepository.findByEmail(ANA)).thenReturn(Optional.of(ana));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskRequest request = new TaskRequest();
        request.setTitle("Preparar entrevista");
        request.setDescription("Repasar JPA");
        request.setCompleted(true);

        TaskResponse response = taskService.createTask(request, ANA);

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(ana);
        assertThat(saved.getValue().getTitle()).isEqualTo("Preparar entrevista");
        assertThat(saved.getValue().getDescription()).isEqualTo("Repasar JPA");
        assertThat(saved.getValue().isCompleted()).isTrue();
        assertThat(response.getTitle()).isEqualTo("Preparar entrevista");
    }

    @Test
    void deleteTaskRemovesATaskOwnedByTheCaller() {
        Task task = new Task();
        when(taskRepository.findByIdAndUserEmail(1L, ANA)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L, ANA);

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTaskThrowsNotFoundForATaskTheCallerDoesNotOwn() {
        when(taskRepository.findByIdAndUserEmail(1L, ANA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(1L, ANA))
                .isInstanceOf(TaskNotFoundException.class);

        verify(taskRepository, never()).delete(any());
        verify(taskRepository, never()).deleteById(any());
    }
}
