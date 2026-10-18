package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;

    private Set<Integer> likeSet = new HashSet<>();

    public Film(int id, String name, String description, LocalDate releaseDate, int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

    public void addLike(int userId) {
        likeSet.add(userId);
    }

    public void removeLike(int userId) {
        likeSet.remove(userId);
    }

    public int getLikesCount() {
        return likeSet.size();
    }

    public boolean hasLikeFromUser(int userId) {
        return likeSet.contains(userId);
    }
}
