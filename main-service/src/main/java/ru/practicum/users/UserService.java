package ru.practicum.users;

import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(Long[] ids, int from, int size);

    UserDto addUser(NewUserRequest newUserDto);

    void deleteUser(Long userId);
}
