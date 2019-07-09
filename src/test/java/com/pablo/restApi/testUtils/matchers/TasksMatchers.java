package com.pablo.restApi.testUtils.matchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablo.restApi.models.Task;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TasksMatchers {

    public static ResultMatcher hasSize(int expectedSize) {
        return result -> {
            List<Task> tasks = Arrays.asList(new ObjectMapper().readValue(result.getResponse().getContentAsString(), Task[].class));
            assertEquals("List: " + tasks.toString() + " has size: " + tasks.size() + ", but expected was: " + expectedSize, expectedSize, tasks.size());
        };
    }

    public static ResultMatcher equalsTo(Task expectedTask) {
        return result -> {
            Task task = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Task.class);
            assertEquals("Task from request: " + task.toString() + "is not equal to expected: " + expectedTask.toString(), expectedTask, task);
        };
    }

    public static ResultMatcher equalsTo(List<Task> expectedTasks) {
        return result -> {
            List<Task> tasks = Arrays.asList(new ObjectMapper().readValue(result.getResponse().getContentAsString(), Task[].class));
            assertEquals("List from request: " + tasks.toString() + "is not equal to expected: " + expectedTasks.toString(), expectedTasks, tasks);
        };
    }
}
