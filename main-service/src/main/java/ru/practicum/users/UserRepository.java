package ru.practicum.users;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.users.models.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT u FROM User as u WHERE "
                   + ":ids IS NULL OR u.id IN :ids ")
    List<User> findUsers(List<Long> ids, Pageable pageable);
}
