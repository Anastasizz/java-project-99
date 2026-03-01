package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.EntityInUseException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public UserDTO createUser(UserCreateDTO userData) {
        var user = userMapper.map(userData);

        user.setPasswordDigest(passwordEncoder.encode(userData.getPassword()));

        userRepository.save(user);
        return userMapper.map(user);
    }

    public List<UserDTO> getAllUsers() {
        var users = userRepository.findAll();
        return users.stream()
                .map(user -> userMapper.map(user))
                .toList();
    }

    public UserDTO getUserById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " Not Found"));
        return userMapper.map(user);
    }

    public void deleteUser(Long id) {
        try {
            userRepository.deleteById(id);
        } catch (RuntimeException ex) {
            throw new EntityInUseException("User is used in tasks");
        }
    }

    public UserDTO updateUser(UserUpdateDTO userData, Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id + " Not Found"));

        userMapper.update(userData, user);
        userRepository.save(user);
        return userMapper.map(user);
    }
}
