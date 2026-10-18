package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
public class FilmService {

    static final int MAX_DESCRIPTION_LENGTH = 200;
    static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film getFilm(int id) {
        return filmStorage.getFilm(id);
    }

    public void addLike(int filmId, int userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        filmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> getPopular(int count) {
        if (count <= 0) {
            throw new ValidationException(String.format("Количество фильмов не может быть меньше 1! Передано - %d", count));
        }
        return filmStorage.getPopularFilms(count);
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        return filmStorage.addFilm(film);
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film changeFilm(Film film) {
        validateFilm(film);
        return filmStorage.changeFilm(film);
    }

    public void deleteFilms() {
        filmStorage.deleteFilms();
    }

    private void validateFilm(Film film) {
        ValidationException err = null;
        if (film.getName() == null || film.getName().isBlank()) {
            err = new ValidationException("Имя не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            err = new ValidationException("Описание не может быть длинным");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата выпуска не может быть меньше " + MIN_RELEASE_DATE);
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Длительность не может быть меньше 0");
        }
        if (err != null) {
            throw err;
        }
    }

}
