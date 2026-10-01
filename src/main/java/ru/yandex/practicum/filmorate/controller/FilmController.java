package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@RestController
@RequestMapping("/films")
public class FilmController {
    private  static Logger log = LoggerFactory.getLogger(FilmController.class);

    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private HashMap<Integer, Film> filmStorage = new HashMap<>();
    private int id = 1;

    //получение всех фильмов
    @GetMapping
    public Collection<Film> getFilms() {
        return filmStorage.values();
    }

    //добавление фильма
    @PostMapping
    public Film postFilm(@RequestBody Film film) {

        validateFilm(film);
        var id = returnId();
        film.setId(id);
        filmStorage.put(id, film);
        return film;
    }

    //добавление/изменение фильма
    @PutMapping
    public Film putFilm(@RequestBody Film film) {
        validateFilm(film);
        if (filmIsAlreadyAdded(film)) {
            return updateFilm(film);
        } else {
            throw new ValidationException("Такого фильма нет!");
        }
    }

    @DeleteMapping
    public void deleteFilm() {
        filmStorage = new HashMap<>();
    }

    private int returnId() {
        return this.id++;
    }

    private boolean filmIsAlreadyAdded(Film film) {
        return filmStorage.containsKey(film.getId());
    }

    private Film updateFilm(Film film) {
        filmStorage.put(film.getId(), film);
        return filmStorage.get(film.getId());
    }

    private Film addFilm(Film film) {
        var id = returnId();
        filmStorage.put(id, film);
        return filmStorage.get(id);
    }

    private void validateFilm(Film film) {
        ValidationException err = null;
        if (film.getName() == null || film.getName().isBlank()) {
            err = new ValidationException("Имя не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            err = new ValidationException("Описание не может быть длинным");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата выпуска не может быть меньше " + MIN_RELEASE_DATE);
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("Длительность не может быть меньше 0");
        }
        if (err != null) {
            log.warn(film.toString(), err);
            throw err;
        }
    }
}
