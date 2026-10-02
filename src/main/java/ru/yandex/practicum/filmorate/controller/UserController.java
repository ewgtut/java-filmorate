package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final int STARTING_USER_ID = 1;
    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private HashMap<Integer, User> userStorage = new HashMap<>();
    private int id = STARTING_USER_ID;

    //получение всех фильмов
    @GetMapping
    public Collection<User> getUsers() {
        log.trace("Вызван /users GET");
        return userStorage.values();
    }

    //добавление фильма
    @PostMapping
    public User postUser(@RequestBody User user) {
        validateUser(user);
        var id = returnId();
        user.setId(id);
        userStorage.put(id, user);
        return user;
    }

    //добавление/изменение фильма
    @PutMapping
    public User putUser(@RequestBody User user) {
        validateUser(user);
        if (userIsAlreadyAdded(user)) {
            return updateUser(user);
        } else {
            throw new ValidationException(String.format("Пользователя с id %d нет!",user.getId()));
        }
    }

    @DeleteMapping
    public void deleteUser() {
        userStorage = new HashMap<>();
        id = STARTING_USER_ID;
    }

    private int returnId() {
        return this.id++;
    }

    private boolean userIsAlreadyAdded(User user) {
        return userStorage.containsKey(user.getId());
    }

    private User updateUser(User user) {
        userStorage.put(user.getId(), user);
        return userStorage.get(user.getId());
    }

    private void validateUser(User user) {
        ValidationException err = null;
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            err = new ValidationException("E-mail не может быть пустым");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            err = new ValidationException("Login не может быть пустым или с пробелом");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            err = new ValidationException("Дата рождения не может быть в будущем");
        }
        if (err != null) {
            log.warn(user.toString(), err);
            throw err;
        }
    }
}
