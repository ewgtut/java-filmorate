package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    Set<Integer> frinedSet = new HashSet<>();

    public User(int id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

    public void addFriend(int id) {
        frinedSet.add(id);
    }

    public void removeFriend(int id) {
        frinedSet.remove(id);
    }

    public boolean hasFriend(int id) {
        return frinedSet.contains(id);
    }

    public Collection<Integer> getFriends() {
        return new ArrayList<>(frinedSet);
    }

}
