package com.pablo.restApi.controller;

import com.pablo.restApi.data.TasksRepository;
import com.pablo.restApi.models.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TasksController {

    @Autowired
    private TasksRepository tasksRepository;

    @GetMapping("/tasks")
    public Iterable<Task> getTasks() {
        return tasksRepository.findAll();
    }

    @GetMapping("/task/{id}")
    public ResponseEntity<?> getTask(@Valid @PathVariable long id) {
        Optional<Task> task = tasksRepository.findById(id);
        if (task.isPresent()) {
            return ResponseEntity.ok(task.get());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/task")
    public ResponseEntity<?> insertTask(@Valid @RequestBody Task newTask) {
        tasksRepository.save(newTask);
        return ResponseEntity.ok(newTask);
    }

    @PutMapping("/task/{id}")
    public ResponseEntity<?> replaceTask(@Valid @PathVariable long id, @Valid @RequestBody Task newTask) {
        if (tasksRepository.updateTask(id, newTask.getName(), newTask.getContent()) == 1) {
            return ResponseEntity.ok(newTask);
        } else  {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/task/{id}")
    public ResponseEntity<?> deleteTask(@Valid @PathVariable long id) {
        tasksRepository.deleteById(id);
        return ResponseEntity.ok(id);
    }
}
