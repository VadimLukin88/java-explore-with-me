package ru.practicum.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@Validated
public class AdminUsersController {

    private final UserService userService;

    @Autowired
    public AdminUsersController(UserService userService) {
        this.userService = userService;
    }

    // Получение информации о пользователях
    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) Long[] ids,
                                  @RequestParam(defaultValue = "0") int from,
                                  @RequestParam(defaultValue = "10") int size) {
        log.info(">>>> HTTP_GET: Получение данных о пользователях. Параметры запроса: User IDs = {}, from = {}, size = {}",
                 ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    // Добавление нового пользователя
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody NewUserRequest newUserDto) {
        log.info(">>>> HTTP_POST: Добавление нового пользователя. New UserDTO = {}", newUserDto);
        return userService.addUser(newUserDto);
    }

    // Удаление пользователя
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info(">>>> HTTP_DELETE: Удаление пользователя. User ID = {}", userId);
        userService.deleteUser(userId);
    }
}
