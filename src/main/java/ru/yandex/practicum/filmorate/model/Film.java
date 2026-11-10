package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Rating mpa;
    private Set<Integer> likeSet = new HashSet<>();
    private Set<Genre> genres = new HashSet<>();

    public Film(int id, String name, String description, LocalDate releaseDate, int duration,
                Rating mpa, Collection<Genre> genreList) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        if (genreList != null) {
            this.genres = new HashSet<>(genreList);
        }
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
