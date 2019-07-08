package com.pablo.restApi.data;

import com.pablo.restApi.models.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TasksRepositoryImpl implements TasksRepository {

    private ArrayList<Task> tasks;

    public TasksRepositoryImpl() {
        tasks = new ArrayList<>(Arrays.asList(
                new Task("Task 1", "Content 1"),
                new Task("Task 2", "Content 2"),
                new Task("Task 3", "Content 3")));
    }

    @Override
    public void insertTask(Task task) {
        tasks.add(task);
    }

    @Override
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    @Override
    public void updateTask(int index, Task task) {
        tasks.set(index, task);
    }

    @Override
    public void deleteTask(int index) {
        tasks.remove(index);
    }

    @Override
    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public void cleanUp() {
        tasks = new ArrayList<>(Arrays.asList(
                new Task("Task 1", "Content 1"),
                new Task("Task 2", "Content 2"),
                new Task("Task 3", "Content 3")));
    }
}
