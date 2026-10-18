package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NoSuchEntityException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    static final int STARTING_USER_ID = 1;

    private HashMap<Integer, User> userStorage = new HashMap<>();
    private int id = STARTING_USER_ID;

    @Override
    public User addUser(User user) {
        var id = returnId();
        user.setId(id);
        userStorage.put(id, user);
        return user;
    }

    @Override
    public User changeUser(User user) {
        if (containsId(user.getId())) {
            userStorage.put(user.getId(), user);
            return userStorage.get(user.getId());
        } else {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", user.getId()));
        }
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(userStorage.values());
    }

    @Override
    public void deleteUsers() {
        userStorage = new HashMap<>();
        id = STARTING_USER_ID;
    }

    @Override
    public User getUser(int id) {
        if (containsId(id)) {
            return userStorage.get(id);
        } else {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", id));
        }
    }

    @Override
    public void addFriend(int userId, int friendId) {
        if (!containsId(userId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", userId));
        }
        if (!containsId(friendId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", friendId));
        }
        userStorage.get(userId).addFriend(friendId);
        userStorage.get(friendId).addFriend(userId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        if (!containsId(userId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", userId));
        }
        if (!containsId(friendId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", friendId));
        }
        userStorage.get(userId).removeFriend(friendId);
        userStorage.get(friendId).removeFriend(userId);
    }

    @Override
    public List<User> getFriends(int id) {
        if (!containsId(id)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", id));
        }
        return userStorage.values().stream()
                .filter((user) -> user.hasFriend(id)).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<User> getCommonFriends(int userId, int friendId) {
        if (!containsId(userId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", userId));
        }
        if (!containsId(friendId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", friendId));
        }
        return userStorage.get(userId).getFriends().stream().filter(id -> userStorage.get(friendId)
                        .hasFriend(id)).map(id -> userStorage.get(id))
                .collect(Collectors.toUnmodifiableList());
    }

    private int returnId() {
        return this.id++;
    }

    @Override
    public boolean containsId(int id) {
        return userStorage.containsKey(id);
    }

}
