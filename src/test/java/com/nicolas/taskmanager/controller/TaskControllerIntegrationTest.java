package com.nicolas.taskmanager.controller;

import com.jayway.jsonpath.JsonPath;
import com.nicolas.taskmanager.model.Task;
import com.nicolas.taskmanager.repository.TaskRepository;
import com.nicolas.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pasa por la cadena de seguridad real (filtro JWT incluido) y por H2,
// así que comprueba también las consultas del repositorio.
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private String anaToken;
    private String bobToken;

    @BeforeEach
    void setUp() throws Exception {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        anaToken = register("ana@example.com");
        bobToken = register("bob@example.com");
    }

    @Test
    void listReturnsOnlyTheCallersTasks() throws Exception {
        createTask(anaToken, "Tarea de Ana");
        createTask(bobToken, "Tarea de Bob");

        mockMvc.perform(get("/tasks").header(HttpHeaders.AUTHORIZATION, bearer(anaToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Tarea de Ana"));
    }

    @Test
    void createAssignsTheCallerAndIgnoresAUserInTheBody() throws Exception {
        Long bobId = userRepository.findByEmail("bob@example.com").orElseThrow().getId();
        String body = """
                {"title": "Tarea ajena", "user": {"id": %d}}
                """.formatted(bobId);

        mockMvc.perform(post("/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(anaToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tarea ajena"));

        assertThat(taskRepository.findByUserEmail("ana@example.com"))
                .extracting(Task::getTitle)
                .containsExactly("Tarea ajena");
        assertThat(taskRepository.findByUserEmail("bob@example.com")).isEmpty();
    }

    @Test
    void responsesDoNotExposeTheOwner() throws Exception {
        createTask(anaToken, "Tarea de Ana");

        mockMvc.perform(get("/tasks").header(HttpHeaders.AUTHORIZATION, bearer(anaToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user").doesNotExist())
                .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void deletingOwnTaskRemovesIt() throws Exception {
        Long id = createTask(anaToken, "Tarea de Ana");

        mockMvc.perform(delete("/tasks/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(anaToken)))
                .andExpect(status().isOk());

        assertThat(taskRepository.existsById(id)).isFalse();
    }

    @Test
    void deletingAnotherUsersTaskReturns404AndKeepsIt() throws Exception {
        Long bobTaskId = createTask(bobToken, "Tarea de Bob");

        mockMvc.perform(delete("/tasks/{id}", bobTaskId).header(HttpHeaders.AUTHORIZATION, bearer(anaToken)))
                .andExpect(status().isNotFound());

        assertThat(taskRepository.existsById(bobTaskId)).isTrue();
    }

    @Test
    void deletingAMissingTaskReturns404() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", 999_999L).header(HttpHeaders.AUTHORIZATION, bearer(anaToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestsWithoutTokenAreRejected() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isForbidden());
    }

    private String register(String email) throws Exception {
        String body = """
                {"email": "%s", "password": "secreto123"}
                """.formatted(email);

        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(response, "$.token");
    }

    private Long createTask(String token, String title) throws Exception {
        String body = """
                {"title": "%s", "description": "", "completed": false}
                """.formatted(title);

        String response = mockMvc.perform(post("/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.parse(response).read("$.id", Long.class);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
