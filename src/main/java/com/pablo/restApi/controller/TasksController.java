package com.pablo.restApi.controller;

import com.pablo.restApi.data.TasksRepository;
import com.pablo.restApi.models.Result;
import com.pablo.restApi.models.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api")
public class TasksController {

    @Autowired
    private TasksRepository tasksRepository;

    @GetMapping("/tasks")
    public ResponseEntity<Result> getTasks() {
        List<Task> tasks = new ArrayList<>();
        tasksRepository.findAll().iterator().forEachRemaining(tasks::add);
        Result result = new Result(tasks);
        return ResponseEntity.ok(result);
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
    public ResponseEntity<Result> insertTask(@Valid @RequestBody Task newTask) {
        tasksRepository.save(newTask);
        Result result = new Result(Collections.singletonList(newTask));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/task/{id}")
    public ResponseEntity<?> replaceTask(@Valid @PathVariable long id, @Valid @RequestBody Task newTask) {
        if (tasksRepository.updateTask(id, newTask.getName(), newTask.getContent()) == 1) {
            return ResponseEntity.ok(newTask);
        } else  {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/task/{id}")
    public ResponseEntity<?> updateTask(@Valid @PathVariable long id, @Valid @RequestBody String content) {
        if (tasksRepository.updateTask(id, content) == 1) {
            return ResponseEntity.ok().build();
        } else  {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/task/{id}")
    public ResponseEntity<?> updateTaskWithoutPatch(@Valid @PathVariable long id, @Valid @RequestBody String content) {
        if (tasksRepository.updateTask(id, content) == 1) {
            return ResponseEntity.ok().build();
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
