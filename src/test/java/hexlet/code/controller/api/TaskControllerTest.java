package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ObjectMapper om;

    private Task testTask;

    private TaskStatus testTaskStatus;

    private User testUser;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        testUser = ModelGenerator.generateUser();

        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        testTaskStatus = ModelGenerator.generateTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        testTask = ModelGenerator.generateTask();
        testTask.setTaskStatus(testTaskStatus);
        testTask.setAssignee(testUser);
        taskRepository.save(testTask);
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/tasks").with(token))
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andReturn();


        var body = result.getResponse().getContentAsString();
        List<TaskDTO> taskDTOs = om.readValue(body, new TypeReference<>() { });

        var actual = taskDTOs.stream()
                .map(t -> taskMapper.map(t))
                .toList();
        var expected = taskRepository.findAll();

        log.info("testIndex:expected {}", expected);
        log.info("testIndex:actual {}", actual);

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testShow() throws Exception {
        var result = mockMvc.perform(get("/api/tasks/" + testTask.getId()).with(token))
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        log.info("\ntestShow:testTask {}", testTask);
        log.info("\ntestShow:body {}", body);

        assertThatJson(body).and(
                json -> json.node("id").isEqualTo(testTask.getId()),
                json -> json.node("index").isEqualTo(testTask.getIndex()),
                json -> json.node("title").isEqualTo(testTask.getName()),
                json -> json.node("assignee_id").isEqualTo(testTask.getAssignee().getId()),
                json -> json.node("status").isEqualTo(testTask.getTaskStatus().getSlug())
        );
    }

    @Test
    public void testNotFoundResource() throws Exception {
        var request = get("/api/tasks/" + 9999).with(token);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreate() throws Exception {
        var taskData = ModelGenerator.generateTask();
        taskData.setTaskStatus(testTaskStatus);
        //taskData.setAssignee(testUser);

        var taskDTO = taskMapper.map(taskData);

        var request = post("/api/tasks").with(token)
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(om.writeValueAsString(taskDTO));

        log.info("\ntestCreate:taskDTO.assignee {}", taskDTO.getAssigneeId());

        var response = mockMvc.perform(request)
                            .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                            .andExpect(status().isCreated());

        log.info("\ntestCreate:response {}", response);

        var task = taskRepository.findByName(taskData.getName()).orElse(null);

        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(taskData.getName());
        assertThat(task.getIndex()).isEqualTo(taskData.getIndex());
        assertThat(task.getAssignee()).isEqualTo(taskData.getAssignee());
        assertThat(task.getTaskStatus()).isEqualTo(taskData.getTaskStatus());
    }

    @Test
    public void testCreateWithNonExistingUser() throws Exception {

        var dto = new TaskCreateDTO();
        dto.setTitle("task");
        dto.setIndex(1);
        dto.setStatus(testTaskStatus.getSlug());
        dto.setAssigneeId(99999L);

        var request = post("/api/tasks").with(token)
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(om.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDestroy() throws Exception {
        var request = delete("/api/tasks/" + testTask.getId()).with(token);

        assertTrue(taskRepository.existsById(testTask.getId()));
        mockMvc.perform(request).andExpect(status().isNoContent());
        assertFalse(taskRepository.existsById(testTask.getId()));
    }

    @Test
    public void testUpdateTitle() throws Exception {
        var data = new HashMap<>();
        data.put("title", "New Task's title");

        var taskId = testTask.getId();

        var request = put("/api/tasks/" + taskId).with(token)
                .contentType((String.valueOf(MediaType.APPLICATION_JSON)))
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk());

        var updTaskStatus = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + taskId + " does not exist!"));
        assertThat(updTaskStatus.getName()).isEqualTo("New Task's title");

    }

    @Test
    public void testUpdateAssignee() throws Exception {

        var anotherUser = ModelGenerator.generateUser();

        userRepository.save(anotherUser);

        var dto = new TaskUpdateDTO();
        dto.setAssigneeId(JsonNullable.of(anotherUser.getId()));

        mockMvc.perform(put("/api/tasks/" + testTask.getId())
                        .with(token)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(om.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk());

        var updated = taskRepository.findById(testTask.getId()).orElseThrow();

        assertThat(updated.getAssignee().getId())
                .isEqualTo(anotherUser.getId());
    }

    @Test
    public void testUpdateStatus() throws Exception {

        var newStatus = ModelGenerator.generateTaskStatus();
        taskStatusRepository.save(newStatus);

        var data = new HashMap<>();
        data.put("status", newStatus.getSlug());

        mockMvc.perform(put("/api/tasks/" + testTask.getId())
                        .with(token)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk());

        var updated = taskRepository.findById(testTask.getId()).orElseThrow();

        assertThat(updated.getTaskStatus().getSlug())
                .isEqualTo(newStatus.getSlug());
    }

    @Test
    public void testFilterByTitleContains() throws Exception {
        var matchedTask = ModelGenerator.generateTask();
        matchedTask.setName("create task");
        matchedTask.setTaskStatus(testTaskStatus);
        matchedTask.setAssignee(testUser);
        taskRepository.save(matchedTask);

        var notMatchedTask = ModelGenerator.generateTask();
        notMatchedTask.setName("another title");
        notMatchedTask.setTaskStatus(testTaskStatus);
        notMatchedTask.setAssignee(testUser);
        taskRepository.save(notMatchedTask);

        var result = mockMvc.perform(get("/api/tasks")
                                .param("titleCont", "create").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        List<TaskDTO> tasks = om.readValue(body, new TypeReference<>() { });

        assertThat(tasks)
                .extracting(TaskDTO::getId)
                .contains(matchedTask.getId())
                .doesNotContain(notMatchedTask.getId());
    }

    @Test
    public void testFilterByLabel() throws Exception {
        var label = ModelGenerator.generateLabel();

        labelRepository.save(label);

        testTask.setLabels(List.of(label));
        taskRepository.save(testTask);

        var result = mockMvc.perform(get("/api/tasks")
                                .param("labelId", String.valueOf(label.getId())).with(token))
                                .andExpect(status().isOk())
                                .andReturn();

        var body = result.getResponse().getContentAsString();
        List<TaskDTO> tasks = om.readValue(body, new TypeReference<>() { });

        assertThat(tasks)
                .extracting(TaskDTO::getId)
                .contains(testTask.getId());
    }

    @Test
    public void testFilterByStatus() throws Exception {
        var anotherStatus = ModelGenerator.generateTaskStatus();
        taskStatusRepository.save(anotherStatus);

        var taskWithAnotherStatus = ModelGenerator.generateTask();
        taskWithAnotherStatus.setTaskStatus(anotherStatus);
        taskWithAnotherStatus.setAssignee(testUser);
        taskRepository.save(taskWithAnotherStatus);

        var result = mockMvc.perform(get("/api/tasks")
                                .param("status", testTaskStatus.getSlug()).with(token))
                                .andExpect(status().isOk())
                                .andReturn();

        var body = result.getResponse().getContentAsString();
        List<TaskDTO> tasks = om.readValue(body, new TypeReference<>() { });

        assertThat(tasks)
                .extracting(TaskDTO::getStatus)
                .containsOnly(testTaskStatus.getSlug());
    }

    @Test
    public void testFilterByAssignee() throws Exception {
        var anotherUser = ModelGenerator.generateUser();

        userRepository.save(anotherUser);

        var taskForAnotherUser = ModelGenerator.generateTask();
        taskForAnotherUser.setAssignee(anotherUser);
        taskForAnotherUser.setTaskStatus(testTaskStatus);
        taskRepository.save(taskForAnotherUser);

        var result = mockMvc.perform(get("/api/tasks")
                                .param("assigneeId", String.valueOf(testUser.getId())).with(token))
                                .andExpect(status().isOk())
                                .andReturn();

        var body = result.getResponse().getContentAsString();
        List<TaskDTO> tasks = om.readValue(body, new TypeReference<>() { });

        assertThat(tasks)
                .extracting(TaskDTO::getAssigneeId)
                .containsOnly(testUser.getId());
    }

}
