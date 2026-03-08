package hexlet.code.component;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.LabelService;
import hexlet.code.service.TaskStatusService;
import hexlet.code.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskStatusRepository taskStatusRepositoryRepository;

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private LabelService labelService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var email = "hexlet@example.com";
        var userData = new UserCreateDTO();
        userData.setEmail(email);
        userData.setPassword("qwerty");
        userService.createUser(userData);

        userData = new UserCreateDTO();
        userData.setEmail("anastasia_sviridenkova@mail.ru");
        userData.setFirstName("Lolly");
        userData.setLastName("Mir");
        userData.setPassword("kittycat");
        userService.createUser(userData);

        var statuses = Map.of(
                "draft", "Draft",
                "to_review", "Under review",
                "to_be_fixed", "Needs fixing",
                "to_publish", "Ready to publish",
                "published", "Published"
        );

        statuses.forEach((slug, name) -> {
            var dto = new TaskStatusCreateDTO();
            dto.setSlug(slug);
            dto.setName(name);
            taskStatusService.createTaskStatus(dto);
        });

        List<String> defaultLabels = List.of("feature", "bug");
        defaultLabels.forEach(name -> {
            var label = new LabelCreateDTO();
            label.setName(name);
            labelService.createLabel(label);
        });
    }
}
