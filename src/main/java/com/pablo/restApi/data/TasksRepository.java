package com.pablo.restApi.data;


import com.pablo.restApi.models.Task;

import java.util.List;

public interface TasksRepository {

    void insertTask(Task task);

    Task getTaskById(int id);

    void updateTask(int index, Task task);

    void deleteTask(int index);

    List<Task> getTasks();

    void cleanUp();
}
