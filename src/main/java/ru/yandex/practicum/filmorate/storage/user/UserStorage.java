package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User addUser(User user);

    User changeUser(User user);

    void deleteUsers();

    User getUser(int id);

    List<User> getUsers();

    boolean containsId(int id);

    void addFriend(int userID, int friendId);

    void deleteFriend(int userID, int friendId);

    List<User> getFriends(int id);

    List<User> getCommonFriends(int userID, int friendId);
}
