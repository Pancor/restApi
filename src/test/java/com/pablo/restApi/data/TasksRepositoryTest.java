package com.pablo.restApi.data;

import com.pablo.restApi.models.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class TasksRepositoryTest {

    @Autowired
    private TasksRepository tasksRepository;

    @Test
    public void saveNewTaskThenMakeChangesToItThenVerifyThatChangesAreSaved() {
        Task task = new Task("New task", "Some content");

        tasksRepository.save(task);
        task.setContent("Updated content");
        Optional<Task> updatedTask = tasksRepository.findByName("New task");

        assertEquals("", task.getContent(), updatedTask.get().getContent());
    }
}
