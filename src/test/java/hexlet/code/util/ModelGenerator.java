package hexlet.code.util;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.stereotype.Component;

@Component
public class ModelGenerator {
    private static final Faker FAKER = new Faker();

    public static User generateUser() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getEmail),
                        () -> FAKER.internet().emailAddress())
                .supply(Select.field(User::getPasswordDigest),
                        () -> FAKER.internet().password(8, 16))
                .create();
    }

    public static TaskStatus generateTaskStatus() {
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getName), () -> FAKER.lorem().word())
                .supply(Select.field(TaskStatus::getSlug), () -> FAKER.lorem().word() + "-" + FAKER.number().digits(6))
                .create();
    }

    public static Task generateTask() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .ignore(Select.field(Task::getAssignee))
                .supply(Select.field(Task::getIndex), () -> FAKER.number().numberBetween(1, 100))
                .supply(Select.field(Task::getName), () -> String.join(" ", FAKER.lorem().words(2)))
                .supply(Select.field(Task::getDescription), () -> String.join(" ", FAKER.lorem().words(4)))
                .ignore(Select.field(Task::getLabels))
                .create();
    }

    public static Label generateLabel() {
        return Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getCreatedAt))
                .supply(Select.field(Label::getName), () -> {
                    String word;
                    do {
                        word = FAKER.lorem().word();
                    } while (word.length() < 3);
                    return word;
                })
                .create();
    }

}
