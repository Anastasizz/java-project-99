package hexlet.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class UserUpdateDTO {
    @NotBlank
    private JsonNullable<String> firstName;
    @NotBlank
    private JsonNullable<String> lastName;
    @NotBlank
    @Email
    private JsonNullable<String> email;
    @NotNull
    @Size(min = 8)
    private JsonNullable<String> passwordDigest;
}
