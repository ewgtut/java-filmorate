package ru.yandex.practicum.filmorate.storage.film.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class FilmDbExtended {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private int mpaId;
    private String mpaName;
    private int genreId;
    private String genreName;
}
