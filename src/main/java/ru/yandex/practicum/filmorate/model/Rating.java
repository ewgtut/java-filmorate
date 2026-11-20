package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.util.Objects;

@Data
public class Rating {
    private int id;
    private String name;

    public Rating(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Rating(int id) {
        this.id = id;
        this.name = "";
    }

    public Rating() {

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return id == rating.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
