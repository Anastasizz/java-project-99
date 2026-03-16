package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
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
public class LabelControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private ObjectMapper om;

    private Label testLabel;

    private User testUser;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        userRepository.deleteAll();

        testLabel = ModelGenerator.generateLabel();
        labelRepository.save(testLabel);

        testUser = ModelGenerator.generateUser();

        userRepository.save(testUser);

        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/labels").with(token))
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andReturn();


        var body = result.getResponse().getContentAsString();
        List<LabelDTO> labelDTOs = om.readValue(body, new TypeReference<>() { });

        var actual = labelDTOs.stream()
                .map(l -> labelMapper.map(l))
                .toList();
        var expected = labelRepository.findAll();

        log.info("testIndex:expected {}", expected);
        log.info("testIndex:actual {}", actual);

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testShow() throws Exception {
        var result = mockMvc.perform(get("/api/labels/" + testLabel.getId()).with(token))
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                json -> json.node("id").isEqualTo(testLabel.getId()),
                json -> json.node("name").isEqualTo(testLabel.getName())
        );
    }

    @Test
    public void testNotFoundResource() throws Exception {
        var request = get("/api/labels/" + 9999).with(token);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreate() throws Exception {
        var labelData = labelMapper.map(ModelGenerator.generateLabel());

        var request = post("/api/labels").with(token)
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(om.writeValueAsString(labelData));

        mockMvc.perform(request)
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isCreated());

        var label = labelRepository.findByName(labelData.getName()).orElse(null);

        assertNotNull(label);
        assertThat(label.getName()).isEqualTo(labelData.getName());
    }

    @Test
    public void testDestroy() throws Exception {
        var request = delete("/api/labels/" + testLabel.getId()).with(token);

        assertTrue(labelRepository.existsById(testLabel.getId()));
        mockMvc.perform(request).andExpect(status().isNoContent());
        assertFalse(labelRepository.existsById(testLabel.getId()));
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("name", "Lolly");

        var labelId = testLabel.getId();

        var request = put("/api/labels/" + labelId).with(token)
                .contentType((String.valueOf(MediaType.APPLICATION_JSON)))
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk());

        var updLabel = labelRepository.findById(labelId).orElseThrow(
                () -> new ResourceNotFoundException("Label with id: " + labelId + " does not exist!")
        );
        assertThat(updLabel.getName()).isEqualTo("Lolly");

    }

}
