package com.pablo.restApi.controller;

import com.pablo.restApi.data.TasksRepository;
import com.pablo.restApi.models.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TasksController {

    @Autowired
    private TasksRepository tasksRepository;

    @GetMapping("/tasks")
    public List<Task> getTasks() {
        return tasksRepository.getTasks();
    }

    @GetMapping("/task/{id}")
    public Task getTask(@Valid @PathVariable int id) {
        return tasksRepository.getTaskById(id);
    }

    @PostMapping("/task")
    public ResponseEntity<?> insertTask(@RequestBody Task newTask) {
        tasksRepository.insertTask(newTask);
        return ResponseEntity.ok(newTask);
    }

    @PutMapping("/task/{id}")
    public ResponseEntity<?> replaceTask(@RequestBody Task newTask, @PathVariable int id) {
        tasksRepository.updateTask(id, newTask);
        return ResponseEntity.ok(newTask);
    }

    @DeleteMapping("/task/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable int id) {
        tasksRepository.deleteTask(id);
        return ResponseEntity.ok(id);
    }
}
