package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film addFilm(Film film);

    Film getFilm(int id);

    Film changeFilm(Film film);

    void deleteFilm(int id);

    void deleteFilms();

    void addLike(int filmId, int userId);

    void deleteLike(int filmId, int userId);

    boolean containsId(int id);

    Collection<Film> getPopularFilms(int size);

    Collection<Film> getFilms();
}
