package com.pablo.restApi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablo.restApi.data.TasksRepository;
import com.pablo.restApi.models.Task;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class TasksControllerITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TasksRepository tasksRepository;

    private MockMvc mvc;
    private String baseUri = "/api";

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .defaultRequest(post("").contentType(MediaType.APPLICATION_JSON))
                .alwaysDo(print())
                .build();
    }

    @Test
    public void getAllTasksWithSuccess() throws Exception {
        String uri = baseUri + "/tasks";

        MvcResult result = mvc.perform(get(uri))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        List<Task> tasks = Arrays.asList(new ObjectMapper().readValue(result.getResponse().getContentAsString(), Task[].class));

        assertEquals("Lists of Tasks should be equal", tasksRepository.getTasks(), tasks);
    }

    @Test
    public void getTaskWithSuccess() throws Exception {
        int TASK_ID = 1;
        String uri = baseUri + "/task/" + TASK_ID;

        MvcResult result = mvc.perform(get(uri))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Task task = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Task.class);
        Task expectedTask = tasksRepository.getTaskById(TASK_ID);

        assertEquals("Tasks should be the same", expectedTask, task);
    }

    @Test
    public void insertTaskWithSuccess() throws Exception {
        String uri = baseUri + "/task";
        Task newTask = new Task("New Task", "New content");
        String inputJson = new ObjectMapper().writeValueAsString(newTask);

        mvc.perform(post(uri).content(inputJson))
                .andExpect(status().isOk());

        assertEquals("There should be added new task", 4, tasksRepository.getTasks().size());
    }

    @Test
    public void replaceTaskWithSuccess() throws Exception {
        int TASK_ID = 1;
        String uri = baseUri + "/task/" + TASK_ID;
        Task updatedTask = new Task("New Task 2", "Updated content");
        String jsonInput = new ObjectMapper().writeValueAsString(updatedTask);

        mvc.perform(put(uri).content(jsonInput))
                .andExpect(status().isOk());

        assertTrue("Task: " + updatedTask.toString() + "should be in list", tasksRepository.getTasks().contains(updatedTask));
    }

    @Test
    public void deleteTaskWithSuccess() throws Exception {
        int TASK_ID = 1;
        String uri = baseUri + "/task/" + TASK_ID;

        mvc.perform(delete(uri))
                .andExpect(status().isOk());

        assertEquals("There sould be only two tasks", 2, tasksRepository.getTasks().size());
    }

    @After
    public void restoreData() {
        tasksRepository.cleanUp();
    }
}
