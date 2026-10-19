package ru.yandex.practicum.filmorate.storage.film;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    static final int STARTING_FILM_ID = 1;

    private int id = STARTING_FILM_ID;
    private HashMap<Integer, Film> filmStorage = new HashMap<>();

    public Optional<Film> addFilm(Film film) {
        var id = returnId();
        film.setId(id);
        filmStorage.put(id, film);
        return Optional.of(film);
    }

    public Optional<Film> changeFilm(Film film) {
        if (filmStorage.containsKey(film.getId())) {
            filmStorage.put(film.getId(), film);
            return Optional.of(filmStorage.get(film.getId()));
        } else
            return Optional.empty();
    }

    public void deleteFilm(int id) {
        filmStorage.remove(id);
    }

    @Override
    public Optional<Film> getFilm(int id) {
        return Optional.ofNullable(filmStorage.get(id));
    }

    private int returnId() {
        return this.id++;
    }

    public boolean containsId(int id) {
        return filmStorage.containsKey(id);
    }

    public void addLike(int filmId, int userId) {
        filmStorage.get(filmId).addLike(userId);
    }

    public void deleteLike(int filmId, int userId) {
        filmStorage.get(filmId).removeLike(userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return new ArrayList<>(filmStorage.values().stream().sorted(new Comparator<Film>() {
            @Override
            public int compare(Film o1, Film o2) {
                if (o1.getLikesCount() > o2.getLikesCount()) {
                    return -1;
                }
                if (o1.getLikesCount() < o2.getLikesCount()) {
                    return 1;
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
