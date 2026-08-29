package com.github.sbonjour.my_cloud.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.github.sbonjour.my_cloud.TestDataFactory;
import com.github.sbonjour.my_cloud.entity.User;


@DataJpaTest
public class UserRepositoryTest extends AbstractPostgresContainerTest{
    @Autowired
    TestEntityManager entityManager;

    @Autowired
    UserRepository repository;

    @Nested
    class FindByEmail {

        @Test
        void shouldFindByEmail() {
            User user = TestDataFactory.persistUser(entityManager, "test@example.com", "test", "password1");

            Optional<User> found = repository.findByEmail("test@example.com");

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(user.getId());
        }

        @Test
        void shouldNotFindByEmail_whenEmailDoesntExist() {
            TestDataFactory.persistUser(entityManager, "test@example.com", "test", "password1");

            Optional<User> found = repository.findByEmail("unknown@example.com");

            assertThat(found).isEmpty();
        }

        @Test
        void shouldNotFindByEmail_whenCaseDiffers() {
            TestDataFactory.persistUser(entityManager, "test@example.com", "test", "password1");

            Optional<User> found = repository.findByEmail("Test@Example.com");

            assertThat(found).isEmpty();
        }

        @Test
        void shouldFindByEmail_whenMultipleUsersExist() {
            TestDataFactory.persistUser(entityManager, "other@example.com", "other", "password2");
            User user = TestDataFactory.persistUser(entityManager, "test@example.com", "test", "password1");

            Optional<User> found = repository.findByEmail("test@example.com");

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(user.getId());
        }
    }

}
