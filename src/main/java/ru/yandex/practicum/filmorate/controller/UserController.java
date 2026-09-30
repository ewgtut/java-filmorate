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
@RequestMapping("/user")
public class UserController {
    private final static Logger log = LoggerFactory.getLogger(UserController.class);
    private HashMap<Integer, User> userStorage = new HashMap<>();
    private int id = 0;

    //получение всех фильмов
    @GetMapping
    public Collection<User> getUsers() {
        return userStorage.values();
    }

    //добавление фильма
    @PostMapping
    public User postUser(@RequestBody User user) {
        validateUser(user);
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
        var id = returnId();
        user.setId(id);
        userStorage.put(id, user);
        return user;
    }

    //добавление/изменение фильма
    @PutMapping
    public User putFilm(@RequestBody User user) {
        validateUser(user);
        if (userIsAlreadyAdded(user)) {
            return updateUser(user);
        } else {
            return addUser(user);
        }
    }

    @DeleteMapping
    public void deleteFilm() {
        userStorage = new HashMap<>();
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

    private User addUser(User user) {
        var id = returnId();
        userStorage.put(id, user);
        return userStorage.get(id);
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
