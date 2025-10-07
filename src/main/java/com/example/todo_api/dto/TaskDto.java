package com.example.todo_api.dto;

import java.time.LocalDate;

public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDate dueDate;


    public TaskDto(Long id, String title, String description, boolean completed, LocalDate dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.dueDate = dueDate;

    }



    // getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public LocalDate getDueDate() { return dueDate; }
}
