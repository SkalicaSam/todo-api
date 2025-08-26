package com.example.todo_api;

import com.example.todo_api.model.Task;
import com.example.todo_api.model.User;
import com.example.todo_api.repository.TaskRepository;
import com.example.todo_api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    private final String TEST_USER2 = "user2";

    @BeforeEach
    void setup() throws Exception {
        // Register a user before each test
        User newUser = new User();
        newUser.setUsername(TEST_USER2);
        newUser.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());
    }

    @AfterEach
    void cleanup() {
        Optional<User> userOptional = userRepository.findByUsername(TEST_USER2);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Task> tasks = taskRepository.findByUserId(user.getId());

            taskRepository.deleteAll(tasks);
            userRepository.delete(user);
        }
    }

    @Test
    void whenGetNonExistentTask_thenIsNotFound() throws Exception {
        // Attempt to get a task with a non-existent ID
        mockMvc.perform(get("/api/tasks/999")
                        .with(user(TEST_USER2)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenCreateTask_thenIsCreated() throws Exception {
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New task description");

        mockMvc.perform(post("/api/tasks")
                        .with(user(TEST_USER2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }
}

// you must change api\src\test\resources\cleanup.sql to DELETE FROM tasks;
//DELETE FROM users;
// to succesful of this test, because without it thorows: Failed to execute SQL script statement #1 of
// class path resource [cleanup.sql]: DELETE FROM users​