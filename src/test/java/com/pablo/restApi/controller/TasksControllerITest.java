package com.pablo.restApi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pablo.restApi.data.TasksRepository;
import com.pablo.restApi.models.Task;

import com.pablo.restApi.testUtils.matchers.TasksMatchers;
import com.pablo.restApi.testUtils.users.Admin;
import com.pablo.restApi.testUtils.users.BobUser;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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
@Transactional
@BobUser
@ActiveProfiles("test")
public class TasksControllerITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TasksRepository tasksRepository;

    private MockMvc mvc;
    private String baseUri = "/api";

    private Long TASK_ID = 1L;
    private Long TO_HIGH_ID = 4L;
    private String WRONG_ID_TYPE = "wrongIdType";
    private String WRONG_BODY = "{wrong json body}";
    private ObjectMapper objectMapper = new ObjectMapper();
    private Task TASK = new Task("New Task", "New content");


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
                .andExpect(TasksMatchers.equalsTo(Lists.newArrayList(tasksRepository.findAll())));
    }

    @Test
    public void getAllTasksWithWrongRequestMethodThenExpectForbidden() throws Exception {
        String uri = baseUri + "/tasks";

        mvc.perform(delete(uri))
                .andExpect(status().isForbidden())
                .andExpect(status().reason("Forbidden"));
    }

    @Test
    public void getTaskWithSuccess() throws Exception {
        String uri = baseUri + "/task/" + TASK_ID;

        mvc.perform(get(uri))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(TasksMatchers.equalsTo(tasksRepository.findById(TASK_ID).get()));
    }

    @Test
    public void getTaskWithWrongIdType() throws Exception {
        String uri = baseUri + "/task/" + WRONG_ID_TYPE;

        mvc.perform(get(uri))
            .andExpect(status().is4xxClientError())
            .andExpect(status().reason("Given arguments are wrong."));
    }

    @Test
    public void getTaskWithIdThatDoesNotExistInDataRepository() throws Exception {
        String uri = baseUri + "/task/" + TO_HIGH_ID;

        mvc.perform(get(uri))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Admin
    public void insertTaskWithSuccess() throws Exception {
        String uri = baseUri + "/task";
        String inputJson = objectMapper.writeValueAsString(TASK);

        mvc.perform(post(uri).content(inputJson))
                .andExpect(status().isOk());

        assertEquals("There should be added new task", 4, tasksRepository.count());
    }

    @Test
    public void insertTaskWithoutAdminPrivileges() throws Exception {
        String uri = baseUri + "/task";
        String inputJson = objectMapper.writeValueAsString(TASK);

        mvc.perform(post(uri).content(inputJson))
                .andExpect(status().isForbidden());

        assertEquals("There should not be added new task", 3, tasksRepository.count());
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

        mvc.perform(post(uri).content(WRONG_BODY))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Admin
    public void replaceTaskWithSuccess() throws Exception {
        String uri = baseUri + "/task/" + TASK_ID;
        String inputJson = objectMapper.writeValueAsString(TASK);

        mvc.perform(put(uri).content(inputJson))
                .andExpect(status().isOk());

        assertTrue("Task: " + TASK + " should be in list, but it's not", tasksRepository.findByName(TASK.getName()).isPresent());
    }

    @Test
    public void replaceTaskWithoutAdminPrivilegesThenReturnForbidden() throws Exception {
        String uri = baseUri + "/task/" + TASK_ID;
        String inputJson = objectMapper.writeValueAsString(TASK);

        mvc.perform(put(uri).content(inputJson))
                .andExpect(status().isForbidden())
                .andExpect(status().reason("Forbidden"));

        assertFalse("Task: " + TASK + " should not be in list, but it is", tasksRepository.findByName(TASK.getName()).isPresent());
    }

    @Test
    @Admin
    public void replaceTaskWithEmptyIdThenReturnMethodNotAllowed() throws Exception {
        String uri = baseUri + "/task/";
        String inputJson = objectMapper.writeValueAsString(TASK);

        mvc.perform(put(uri).content(inputJson))
                .andExpect(status().isMethodNotAllowed());

        assertFalse("Task: " + TASK + " should not be in list, but it is", tasksRepository.findByName(TASK.getName()).isPresent());
    }

    @Test
    @Admin
    public void replaceTaskWithTaskIdWhichIsNotInDatabaseThenThrowBadRequest() throws Exception {
        String uri = baseUri + "/task/" + TO_HIGH_ID;
        String inputJson = objectMapper.writeValueAsString(TASK);

        mvc.perform(put(uri).content(inputJson))
                .andExpect(status().isBadRequest());

        assertFalse("Task: " + TASK + " should not be in list, but it is", tasksRepository.findByName(TASK.getName()).isPresent());
    }

    @Test
    @Admin
    public void replaceTaskWithEmptyBodyThenThrowBadRequest() throws Exception {
        String uri = baseUri + "/task/" + TASK_ID;

        mvc.perform(put(uri))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Admin
    public void replaceTaskWithWrongBodyThenThrowBadRequest() throws Exception {
        String uri = baseUri + "/task/" + TO_HIGH_ID;

        mvc.perform(put(uri).content(WRONG_BODY))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Admin
    public void deleteTaskWithSuccess() throws Exception {
        String uri = baseUri + "/task/" + TASK_ID;

        mvc.perform(delete(uri))
                .andExpect(status().isOk());

        assertEquals("There should be only two tasks", 2, tasksRepository.count());
    }

    @Test
    public void deleteTaskWithoutAdminPrivilegesThenReturnForbidden() throws Exception {
        String uri = baseUri + "/task/" + TASK_ID;

        mvc.perform(delete(uri))
                .andExpect(status().isForbidden());

        assertEquals("There should be three tasks", 3, tasksRepository.count());
    }

    @Test
    @Admin
    public void deleteTaskWithWrongIdTypeThenReturnBadRequest() throws Exception {
        String uri = baseUri + "/task/" + WRONG_ID_TYPE;

        mvc.perform(delete(uri))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Given arguments are wrong."));
    }

    @Test
    @Admin
    public void deleteTaskWithTaskIdThatDoesNotBelongToDatabaseThenReturnBadRequest() throws Exception {
        String uri = baseUri + "/task/" + TO_HIGH_ID;

        mvc.perform(delete(uri))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Tasks repository does not contain data with given inputs."));
    }

    @Test
    public void callWrongUriThenReturnError() throws Exception {
        String uri = baseUri + "/wrong/uri";

        mvc.perform(get(uri))
                .andExpect(status().is4xxClientError());
    }

    @After
    public void cehck() {
        System.out.println(tasksRepository.findAll().toString());
    }
}