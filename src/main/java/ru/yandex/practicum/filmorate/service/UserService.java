package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addUser(User user) {
        validateUser(user);
        return userStorage.addUser(user);
    }

    public User changeUser(User user) {
        validateUser(user);
        return userStorage.changeUser(user);
    }

    public void deleteUsers() {
        userStorage.deleteUsers();
    }

    public User getUser(int userId) {
        return userStorage.getUser(userId);
    }

    public void addFriend(int userId, int friendId) {
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getCommonFriends(int userId, int friendId) {
        return userStorage.getCommonFriends(userId, friendId);
    }

    public List<User> getFriendsList(int userId) {
        return userStorage.getFriends(userId);
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
            //log.warn(user.toString(), err);
            throw err;
        }
    }


}
