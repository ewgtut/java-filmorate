package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
    public Optional<User> changeUser(User user) {
        if (userStorage.containsKey(user.getId())) {
            userStorage.put(user.getId(), user);
            return Optional.of(userStorage.get(user.getId()));
        } else
            return Optional.empty();
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
    public Optional<User> getUser(int id) {
        return Optional.ofNullable(userStorage.get(id));
    }

    @Override
    public void addFriend(int userId, int friendId) {
        userStorage.get(userId).addFriend(friendId);
        userStorage.get(friendId).addFriend(userId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        userStorage.get(userId).removeFriend(friendId);
        userStorage.get(friendId).removeFriend(userId);
    }

    @Override
    public List<User> getFriends(int id) {
        return userStorage.get(id).getFriends().stream()
                .map((userId) -> userStorage.get(userId)).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<User> getCommonFriends(int userId, int friendId) {
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
