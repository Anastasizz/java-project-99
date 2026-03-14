package hexlet.code.service.interfaces;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import java.util.List;

public interface UserService {

    UserDTO createUser(UserCreateDTO userData);
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    void deleteUser(Long id);
    UserDTO updateUser(UserUpdateDTO userData, Long id);

}
