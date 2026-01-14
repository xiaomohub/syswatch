package org.xiaomo.syswatch.service;

import org.springframework.stereotype.Service;
import org.xiaomo.syswatch.repository.UserRepository;
import org.xiaomo.syswatch.domain.entity.User;
import org.xiaomo.syswatch.security.JwtUtil;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }

        // 生成 token
        return jwtUtil.generateToken(username);
    }
}
