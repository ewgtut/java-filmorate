package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NoSuchEntityException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    static final int STARTING_FILM_ID = 1;

    private int id = STARTING_FILM_ID;
    private HashMap<Integer, Film> filmStorage = new HashMap<>();
    private final UserStorage userStorage;

    @Autowired
    public InMemoryFilmStorage(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        var id = returnId();
        film.setId(id);
        filmStorage.put(id, film);
        return film;
    }

    public Film changeFilm(Film film) {
        if (containsId(film.getId())) {
            filmStorage.put(film.getId(), film);
            return filmStorage.get(film.getId());
        } else {
            throw new NoSuchEntityException(String.format("Фильма с id %d нет!", film.getId()));
        }
    }

    public void deleteFilm(int id) {
        if (containsId(id)) {
            filmStorage.remove(id);
        } else {
            throw new NoSuchEntityException(String.format("Фильма с id %d нет!", id));
        }
    }

    @Override
    public Film getFilm(int id) {
        if (containsId(id)) {
            return filmStorage.get(id);
        } else {
            throw new NoSuchEntityException(String.format("Фильма с id %d нет!", id));
        }
    }

    private int returnId() {
        return this.id++;
    }

    public boolean containsId(int id) {
        return filmStorage.containsKey(id);
    }

    public void addLike(int filmId, int userId) {
        if (!containsId(filmId)) {
            throw new NoSuchEntityException(String.format("Фильма с id %d нет!", filmId));
        }
        if (!userStorage.containsId(userId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", userId));
        }
        filmStorage.get(filmId).addLike(userId);
    }

    public void deleteLike(int filmId, int userId) {
        if (!containsId(filmId)) {
            throw new NoSuchEntityException(String.format("Фильма с id %d нет!", filmId));
        }
        if (!userStorage.containsId(userId)) {
            throw new NoSuchEntityException(String.format("Пользователя с id %d нет!", userId));
        }
        if (!filmStorage.get(filmId).hasLikeFromUser(userId)) {
            throw new NoSuchEntityException(String.format("Лайка пользователя с id %d на фильме с id %d нет!", userId, filmId));
        }
        filmStorage.get(filmId).removeLike(userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return new ArrayList<>(filmStorage.values().stream().sorted(new Comparator<Film>() {
            @Override
            public int compare(Film o1, Film o2) {
                if (o1.getLikesCount() > o2.getLikesCount()) {
                    return 1;
                }
                if (o1.getLikesCount() < o2.getLikesCount()) {
                    return -1;
                }
                return 0;
            }
        }).limit(count).collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public Collection<Film> getFilms() {
        return filmStorage.values();
    }

    @Override
    public void deleteFilms() {
        filmStorage = new HashMap<>();
        id = STARTING_FILM_ID;
    }
}
