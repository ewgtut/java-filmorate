package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoSuchEntityException;
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
        return userStorage.changeUser(user)
                .orElseThrow(() -> new NoSuchEntityException(String.format("Пользователя с id %d нет!", user.getId())));
    }

    public void deleteUsers() {
        userStorage.deleteUsers();
    }

    public User getUser(int userId) {
        return userStorage.getUser(userId)
                .orElseThrow(() -> new NoSuchEntityException(String.format("Пользователя с id %d нет!", userId)));
    }

    public void addFriend(int userId, int friendId) {
        if (!containsId(userId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", userId));
        }
        if (!containsId(friendId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", friendId));
        }
        if (userId == friendId) {
            throw new ValidationException(
                    String.format("Нельзя добавить пользователя с id %d самого к себе в друзья!", friendId));
        }
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        if (!containsId(userId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", userId));
        }
        if (!containsId(friendId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", friendId));
        }
        if (userId == friendId) {
            throw new ValidationException(
                    String.format("Нельзя удалить пользователя с id %d самого у себя из друзей!", friendId));
        }
        userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getCommonFriends(int userId, int friendId) {
        if (userId == friendId) {
            throw new ValidationException(
                    String.format("Нельзя запрашивать общих друзей у пользователя с id %d с самим собой!", friendId));
        }
        return userStorage.getCommonFriends(userId, friendId);
    }

    public List<User> getFriendsList(int userId) {
        if (!containsId(userId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", userId));
        }
        return userStorage.getFriends(userId);
    }

    public boolean containsId(int id) {
        return userStorage.containsId(id);
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
            throw err;
        }
    }


}
