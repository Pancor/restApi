package com.pablo.restApi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablo.restApi.data.TasksRepository;
import com.pablo.restApi.models.Task;

import com.pablo.restApi.utils.matchers.TasksMatchers;
import com.pablo.restApi.utils.users.Admin;
import com.pablo.restApi.utils.users.BobUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@BobUser
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
                .apply(springSecurity())
                .defaultRequest(post("").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .alwaysDo(print())
                .alwaysExpect(header().string("X-XSS-Protection", "1; mode=block"))
                .alwaysExpect(header().string("X-Content-Type-Options", "nosniff"))
                .alwaysExpect(header().string("X-Frame-Options", "DENY"))
                .build();
    }

    @Test
    public void getAllTasksWithSuccess() throws Exception {
        String uri = baseUri + "/tasks";

        mvc.perform(get(uri))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(TasksMatchers.hasSize(3))
                .andExpect(TasksMatchers.equalsTo(tasksRepository.getTasks()));
    }

    @Test
    public void getTaskWithSuccess() throws Exception {
        int TASK_ID = 1;
        String uri = baseUri + "/task/" + TASK_ID;

        mvc.perform(get(uri))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(TasksMatchers.equalsTo(tasksRepository.getTaskById(TASK_ID)));
    }

    @Test
    public void getTaskWithWrongIdType() throws Exception {
        String TASK_ID = "task_ID";
        String uri = baseUri + "/task/" + TASK_ID;

        mvc.perform(get(uri))
            .andExpect(status().is4xxClientError())
            .andExpect(status().reason("Given arguments are wrong."));
    }

    @Test
    public void getTaskWithIdThatDoesNotExistInDataRepository() throws Exception {
        int TASK_ID = 4;
        String uri = baseUri + "/task/" + TASK_ID;

        mvc.perform(get(uri))
                .andExpect(status().is4xxClientError())
                .andExpect(status().reason("Tasks repository does not contain data with given inputs."));
    }

    @Test
    public void callWrongUriThenReturnError() throws Exception {
        String uri = baseUri + "/wrong/uri";

        mvc.perform(get(uri))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Admin
    public void insertTaskWithSuccess() throws Exception {
        String uri = baseUri + "/task";
        Task newTask = new Task("New Task", "New content");
        String inputJson = new ObjectMapper().writeValueAsString(newTask);

        mvc.perform(post(uri).content(inputJson))
                .andExpect(status().isOk());

        assertEquals("There should be added new task", 4, tasksRepository.getTasks().size());
    }

    @Test
    public void insertTaskWithoutAdminPrivileges() throws Exception {
        String uri = baseUri + "/task";
        Task newTask = new Task("New Task", "New content");
        String inputJson = new ObjectMapper().writeValueAsString(newTask);

        mvc.perform(post(uri).content(inputJson))
                .andExpect(status().isForbidden());

        assertEquals("There should be added new task", 3, tasksRepository.getTasks().size());
    }

    @Test
    public void insertTaskWithEmptyBody() throws Exception {
        String uri = baseUri + "/task";

        mvc.perform(post(uri))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void insertTaskWithWrongBody() throws Exception {
        String uri = baseUri + "/task";
        String inputJson = "{wrong json}";

        mvc.perform(post(uri).content(inputJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Admin
    public void replaceTaskWithSuccess() throws Exception {
        int TASK_ID = 1;
        String uri = baseUri + "/task/" + TASK_ID;
        Task updatedTask = new Task("New Task 2", "Updated content");
        String jsonInput = new ObjectMapper().writeValueAsString(updatedTask);

        mvc.perform(put(uri).content(jsonInput))
                .andExpect(status().isOk());

        assertTrue("Task: " + updatedTask + " should be in list, but it's not", tasksRepository.getTasks().contains(updatedTask));
    }

    @Test
    @Admin
    public void deleteTaskWithSuccess() throws Exception {
        int TASK_ID = 1;
        String uri = baseUri + "/task/" + TASK_ID;

        mvc.perform(delete(uri))
                .andExpect(status().isOk());

        assertEquals("There should be only two tasks", 2, tasksRepository.getTasks().size());
    }

    @After
    public void restoreData() {
        tasksRepository.cleanUp();
    }
}
