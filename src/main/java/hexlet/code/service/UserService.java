package hexlet.code.service;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public UserDTO create(UserCreateDTO userData) {
        var user = userMapper.map(userData);

        user.setPasswordDigest(passwordEncoder.encode(userData.getPasswordDigest()));

        userRepository.save(user);
        return userMapper.map(user);
    }
}
