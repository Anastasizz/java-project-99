package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import lombok.extern.slf4j.Slf4j;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusesControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper om;

    private TaskStatus testTaskStatus;

    private User testUser;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();

        testTaskStatus = ModelGenerator.generateTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        testUser = ModelGenerator.generateUser();

        userRepository.save(testUser);

        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/task_statuses").with(token))
                            .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                            .andExpect(status().isOk())
                            .andReturn();


        var body = result.getResponse().getContentAsString();
        List<TaskStatusDTO> taskStatusDTOs = om.readValue(body, new TypeReference<>() { });

        var actual = taskStatusDTOs.stream()
                .map(t -> taskStatusMapper.map(t))
                .toList();
        var expected = taskStatusRepository.findAll();

        log.info("testIndex:expected {}", expected);
        log.info("testIndex:actual {}", actual);

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testShow() throws Exception {
        var result = mockMvc.perform(get("/api/task_statuses/" + testTaskStatus.getId()).with(token))
                        .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                        .andExpect(status().isOk())
                        .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                json -> json.node("id").isEqualTo(testTaskStatus.getId()),
                json -> json.node("name").isEqualTo(testTaskStatus.getName()),
                json -> json.node("slug").isEqualTo(testTaskStatus.getSlug())
        );
    }

    @Test
    public void testNotFoundResource() throws Exception {
        var request = get("/api/task_statuses/" + 9999).with(token);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreate() throws Exception {
        var taskStatusData = taskStatusMapper.map(ModelGenerator.generateTaskStatus());

        var request = post("/api/task_statuses").with(token)
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(om.writeValueAsString(taskStatusData));

        mockMvc.perform(request)
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isCreated());

        var taskStatus = taskStatusRepository.findByName(taskStatusData.getName()).orElse(null);

        assertNotNull(taskStatus);
        assertThat(taskStatus.getName()).isEqualTo(taskStatusData.getName());
        assertThat(taskStatus.getSlug()).isEqualTo(taskStatusData.getSlug());
    }

    @Test
    public void testDestroy() throws Exception {
        var request = delete("/api/task_statuses/" + testTaskStatus.getId()).with(token);

        assertTrue(taskStatusRepository.existsById(testTaskStatus.getId()));
        mockMvc.perform(request).andExpect(status().isNoContent());
        assertFalse(taskStatusRepository.existsById(testTaskStatus.getId()));
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("slug", "in-process");

        var taskStatusId = testTaskStatus.getId();

        var request = put("/api/task_statuses/" + taskStatusId).with(token)
                .contentType((String.valueOf(MediaType.APPLICATION_JSON)))
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk());

        var updTaskStatus = taskStatusRepository.findByName(testTaskStatus.getName()).orElseThrow();
        assertThat(updTaskStatus.getSlug()).isEqualTo("in-process");

    }

}
