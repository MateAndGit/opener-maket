package mateandgit.opener_maket.service;

import lombok.RequiredArgsConstructor;
import mateandgit.opener_maket.domain.User;
import mateandgit.opener_maket.dto.SingUpRequest;
import mateandgit.opener_maket.repsoitory.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public Long join(SingUpRequest request) {

        validateUserExist(request);

        User user = User.createUser(request);
        userRepository.save(user);

        return user.getId();
        
    }

    public List<User> findUsers() {
        return userRepository.findAll();
    }

    private void validateUserExist(SingUpRequest request) {
        userRepository.findByEmail(request.email())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("user already exist");
                });
    }
}
