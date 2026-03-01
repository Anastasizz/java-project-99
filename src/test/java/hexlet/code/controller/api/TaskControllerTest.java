package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
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
    private Faker faker;

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

        testUser = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPasswordDigest), () -> faker.internet().password(8, 16))
                .create();

        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        testTaskStatus = taskStatusBuilder();
        taskStatusRepository.save(testTaskStatus);

        testTask = taskBuilder();
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
        var taskData = taskBuilder();
        taskData.setTaskStatus(testTaskStatus);
        taskData.setAssignee(testUser);

        var taskDTO = taskMapper.map(taskData);

        var request = post("/api/tasks").with(token)
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(om.writeValueAsString(taskDTO));

        mockMvc.perform(request)
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isCreated());

        var task = taskRepository.findByName(taskData.getName()).orElse(null);

        assertNotNull(task);
        assertThat(task.getName()).isEqualTo(taskData.getName());
        assertThat(task.getIndex()).isEqualTo(taskData.getIndex());
        assertThat(task.getAssignee()).isEqualTo(taskData.getAssignee());
        assertThat(task.getTaskStatus()).isEqualTo(taskData.getTaskStatus());
    }

    @Test
    public void testDestroy() throws Exception {
        var request = delete("/api/tasks/" + testTask.getId()).with(token);

        assertTrue(taskRepository.existsById(testTask.getId()));
        mockMvc.perform(request).andExpect(status().isNoContent());
        assertFalse(taskRepository.existsById(testTask.getId()));
    }

    @Test
    public void testUpdate() throws Exception {
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

    private TaskStatus taskStatusBuilder() {
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getName), () -> faker.lorem().word())
                .supply(Select.field(TaskStatus::getSlug), () -> faker.lorem().word())
                .create();
    }

    private Task taskBuilder() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .supply(Select.field(Task::getIndex), () -> faker.number().numberBetween(1, 100))
                .supply(Select.field(Task::getName), () -> String.join(" ", faker.lorem().words(2)))
                .supply(Select.field(Task::getDescription), () -> String.join(" ", faker.lorem().words(4)))
                .create();
    }
}
