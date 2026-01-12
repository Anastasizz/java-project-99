package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.UserDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import org.instancio.Instancio;
import org.instancio.Select;
import net.datafaker.Faker;

import java.util.HashMap;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@SpringBootTest
@AutoConfigureMockMvc
public class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Faker faker;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();

        testUser1 = userBuild();
        testUser2 = userBuild();

        userRepository.save(testUser1);
        userRepository.save(testUser2);
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/users"))
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        //может все-таки не нужны эти танцы с бубном?
        List<UserDTO> userDTOs = om.readValue(body, new TypeReference<>() { });
        var actual = userDTOs.stream()
                .map(userDTO -> userMapper.map(userDTO))
                .toList();
        var expected = userRepository.findAll();

        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    public void testShow() throws Exception {
        var result = mockMvc.perform(get("/api/users/" + testUser2.getId()))
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                json -> json.node("id").isEqualTo(testUser2.getId()),
                json -> json.node("firstName").isEqualTo(testUser2.getFirstName()),
                json -> json.node("lastName").isEqualTo(testUser2.getLastName()),
                json -> json.node("email").isEqualTo(testUser2.getEmail())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var userData = userBuild();
        var request = post("/api/users")
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(om.writeValueAsString(userData));


        mockMvc.perform(request)
                .andExpect(content().contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(userData.getEmail()).orElse(null);

        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(userData.getFirstName());
        assertThat(user.getLastName()).isEqualTo(userData.getLastName());

    }

    @Test
    public void testDestroy() throws Exception {
        assertTrue(userRepository.existsById(testUser1.getId()));

        mockMvc.perform(delete("/api/users/" + testUser1.getId()))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(testUser1.getId()));
    }

    @Test
    public void testUpdate() throws Exception {
        var data = new HashMap<>();
        data.put("firstName", "Lolly");

        var request = put("/api/users/" + testUser2.getId())
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(om.writeValueAsString(data));


        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser2.getId()).orElseThrow();

        assertThat(updatedUser.getFirstName()).isEqualTo("Lolly");
    }

    private User userBuild() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPasswordDigest), () -> faker.internet().password(8, 16))
                .create();
    }
}
