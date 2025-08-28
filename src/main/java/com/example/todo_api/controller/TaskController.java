package com.example.todo_api.controller;

import com.example.todo_api.model.Task;
import com.example.todo_api.model.User;
import com.example.todo_api.repository.UserRepository;
import com.example.todo_api.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Task> getTasks(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return taskService.getTasksByUserId(user.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return taskService.getTasksByTaskIdAndUserId(id, user.getId())
                .map(ResponseEntity::ok) // Ak sa úloha nájde, vráti 200 OK s úlohou
                .orElse(ResponseEntity.notFound().build()); // Inak vráti 404 Not Found

//        Optional<Task> task = taskService.getTasksByTaskIdAndUserId(user.getId(), id);
//        return task.orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task, Principal principal) {
        Task createdTask = taskService.createTask(task, principal.getName());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTask.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails, Principal principal) {
        // Security check: Ensure the task belongs to the logged-in user
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<Task> taskOptional = taskService.updateTask(id, taskDetails);

        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOptional.get();
        if (!task.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Principal principal) {
        // Security check: Ensure the task belongs to the logged-in user
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (taskService.deleteTask(id)) {
            return ResponseEntity.noContent().build();   // 204 No Content
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}