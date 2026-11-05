package ru.yandex.practicum.filmorate.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NoSuchEntityException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Map;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //получение пользователя по ид
    @GetMapping("/{userId}")
    public User getUserById(@PathVariable int userId) {
        return userService.getUser(userId);
    }

    //получение всех пользователей
    @GetMapping
    public Collection<User> getUsers() {
        return userService.getUsers();
    }

    //добавление пользователя
    @PostMapping
    public User postUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    //добавление/изменение фильма
    @PutMapping
    public User putUser(@RequestBody User user) {
        return userService.changeUser(user);
    }

    //добавление в друзья
    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable int userId, @PathVariable int friendId) {
        userService.addFriend(userId, friendId);
    }

    //удление из друзей
    @DeleteMapping("/{userId}/friends/{friendId}")
    public void deleteFriend(@PathVariable int userId, @PathVariable int friendId) {
        userService.deleteFriend(userId, friendId);
    }

    //получение списка друзей
    @GetMapping("/{userId}/friends")
    public Collection<User> getFriendsById(@PathVariable int userId) {
        return userService.getFriendsList(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable int userId, @PathVariable int otherId) {
        return userService.getCommonFriends(userId, otherId);
    }

    @DeleteMapping
    public void deleteUser() {
        userService.deleteUsers();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(final ValidationException e) {
        return Map.of(
                "error", "Переданный объект не прошёл валидацию.",
                "errorMessage", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNoSuchEntityException(final NoSuchEntityException e) {
        return Map.of(
                "error", "Объекта не существует.",
                "errorMessage", e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleRuntimeException(final RuntimeException e) {
        return Map.of(
                "error", "Объекта не существует.",
                "errorMessage", e.getMessage()
        );
    }

}
