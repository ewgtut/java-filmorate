package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User addUser(User user);

    Optional<User> changeUser(User user);

    void deleteUsers();

    Optional<User> getUser(int id);

    List<User> getUsers();

    boolean containsId(int id);

    void addFriend(int userID, int friendId);

    void deleteFriend(int userID, int friendId);

    List<User> getFriends(int id);

    List<User> getCommonFriends(int userID, int friendId);
}
