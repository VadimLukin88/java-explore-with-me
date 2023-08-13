package ru.practicum.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exceptions.DataNotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.models.User;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Получение информации о пользователях (для Admin контроллера)
    // Возвращает информацию обо всех пользователях (учитываются параметры ограничения выборки),
    // либо о конкретных (учитываются указанные идентификаторы)
    //
    //В случае, если по заданным фильтрам не найдено ни одного пользователя, возвращает пустой список
    @Override
    @Transactional
    public List<UserDto> getUsers(Long[] ids, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);

        List<Long> userIds = null;

        if (ids != null) {
            userIds = Arrays.asList(ids);
        }
        return userRepository.findUsers(userIds, pageable).stream()
                                                          .map(UserMapper::mapUserToDto)
                                                          .collect(Collectors.toList());
    }

    // Добавление нового пользователя (для Admin контроллера)
    @Override
    @Transactional
    public UserDto addUser(NewUserRequest newUserDto) {
        if (userRepository.existsByName(newUserDto.getName())) {
            throw new ValidationException("User name already exist", HttpStatus.CONFLICT);
        }
        User user = UserMapper.mapDtoToUser(newUserDto);

        return UserMapper.mapUserToDto(userRepository.save(user));
    }

    // Удаление пользователя (для Admin контроллера)
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new DataNotFoundException("User not found", HttpStatus.NOT_FOUND));

        userRepository.delete(user);
    }
}
