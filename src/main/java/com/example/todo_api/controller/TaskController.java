package com.example.todo_api.controller;

import com.example.todo_api.dto.TaskDto;
import com.example.todo_api.model.Task;
import com.example.todo_api.model.User;
import com.example.todo_api.repository.UserRepository;
import com.example.todo_api.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
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
    public ResponseEntity<Page<TaskDto>> getTasks(Principal principal, Pageable pageable) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//        return taskService.getTasksByUserId(user.getId());

        Page<Task> tasksPage = taskService.getTasksByUserId(user.getId(), pageable);

        Page<TaskDto> tasksDtoPage = tasksPage.map(task -> new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getDueDate()
        ));

        return ResponseEntity.ok(tasksDtoPage);
    }

    @GetMapping("/AllTasks") // for testing in swagger
    public ResponseEntity<List<TaskDto>> getAllUsersTasks(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Task> tasks = taskService.getAllTasksByUserId(user.getId());

        List<TaskDto> tasksDto = new ArrayList<>();
        for (Task task : tasks) {
            tasksDto.add(new TaskDto(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.isCompleted(),
                    task.getDueDate()
            ));
        }

        return ResponseEntity.ok(tasksDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<Task> taskOptional = taskService.getTasksByTaskIdAndUserId(id, user.getId());

        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            TaskDto dto = new TaskDto(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.isCompleted(),
                    task.getDueDate()
            );
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody Task task, Principal principal) {
        Task createdTask = taskService.createTask(task, principal.getName());

        TaskDto createdTaskDto = new TaskDto(
                createdTask.getId(),
                createdTask.getTitle(),
                createdTask.getDescription(),
                createdTask.isCompleted(),
                createdTask.getDueDate()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTask.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdTaskDto);
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