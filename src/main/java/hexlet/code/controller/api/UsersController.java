package hexlet.code.controller.api;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping(path = "/api/users")
public class UsersController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    UserService userService;


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public final List<UserDTO> index() {
        var users = userRepository.findAll();
        return users.stream()
                .map(user -> userMapper.map(user))
                .toList();
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public final UserDTO show(@PathVariable Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " Not Found"));
        return userMapper.map(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public final UserDTO create(@Valid @RequestBody UserCreateDTO userData) {
//        var user = userMapper.map(userData);
//        userRepository.save(user);
//        return userMapper.map(user);
        return userService.create(userData);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO update(@PathVariable Long id,
                          @Valid @RequestBody UserUpdateDTO userData) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " Not Found"));

        userMapper.update(userData, user);
        userRepository.save(user);
        return userMapper.map(user);
    }

}
