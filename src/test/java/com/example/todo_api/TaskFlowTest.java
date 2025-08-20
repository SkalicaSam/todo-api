package com.example.todo_api;

import com.example.todo_api.model.Task;
import com.example.todo_api.model.User;
import com.example.todo_api.repository.TaskRepository;
import com.example.todo_api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/cleanup.sql")
class TaskFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void userRegisters_logsIn_andCreatesTask() throws Exception {
        // 1. Register a new user
        String username = "flowUser";
        String password = "password123";
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());

        // 2. Create a new task as the registered user
        Task newTask = new Task();
        newTask.setTitle("My First Task");
        newTask.setDescription("This is a test task.");
        newTask.setDueDate(LocalDate.now().plusDays(7));

        mockMvc.perform(post("/api/tasks")
                        .with(user(username)) // Simulate the request as the user "flowUser"
                // only in test for simulation. Not in real app life .
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isOk());

        // 3. Verify the task was created and associated with the user
        assertThat(taskRepository.count()).isEqualTo(1);
        Task createdTask = taskRepository.findAll().get(0);
        assertThat(createdTask.getTitle()).isEqualTo("My First Task");
        assertThat(createdTask.getUser().getUsername()).isEqualTo(username);
    }
}