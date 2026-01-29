package mateandgit.opener_maket.service;

import mateandgit.opener_maket.domain.User;
import mateandgit.opener_maket.dto.SingUpRequest;
import mateandgit.opener_maket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("Sign Up")
    void signUp() {
        // given
        SingUpRequest request = new SingUpRequest("test@test.com", "password1234");

        // when
        Long saveId = userService.join(request);

        // then
        User findUser = userRepository.findById(saveId).get();
        assertThat(findUser.getEmail()).isEqualTo(request.email());
    }

    @Test
    @DisplayName("Should throw exception when signing up with duplicate email")
    void duplicate_signup_exception() {

        SingUpRequest request = new SingUpRequest("duplicate@test.com", "1234");
        
        userService.join(request);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.join(request);
        });
    }
}