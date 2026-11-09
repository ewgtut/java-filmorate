package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Primary
public class FilmDbStorage implements FilmStorage {
    private static final String INSERT_FILM_QUERY = "INSERT INTO FILM(name, description, releaseDate, duration, rating_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE FILM SET name = ?, description = ?, releaseDate = ?," +
            "duration = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM FILM WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM FILM WHERE id = ?";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO LIKES(film_id, user_id) VALUES (?,?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM LIKES WHERE film_id = ? and user_id = ?";
    private static final String GET_POPULAR_FILMS_QUERY = "SELECT * from FILM WHERE id IN " +
            "(SELECT film_id from LIKES GROUP BY film_id ORDER BY COUNT(*) DESC LIMIT ?)";
    private static final String GET_FILMS_QUERY = "SELECT * from FILM";
    private static final String DELETE_FILMS_QUERY = "DELETE FROM FILM";
    private static final String ADD_GENRE_TO_FILM_QUERY = "INSERT INTO GENRE_FILM (film_id, genre_id) VALUES(?,?)";
    private static final String DELETE_GENRES_FROM_FILM_QUERY = "DELETE FROM GENRE_FILM WHERE film_id = ?";
    private static final String DELETE_GENRES_FROM_FILMS_QUERY = "DELETE FROM GENRE_FILM";
    private static final String GET_GENRES_OF_FILM_BY_FILM_ID_QUERY = "SELECT * FROM GENRE WHERE ID IN" +
            "(SELECT genre_id from GENRE_FILM WHERE film_id = ?)";

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;

    @Override
    public Optional<Film> addFilm(Film film) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_FILM_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, film.getName());
            ps.setObject(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setObject(4, film.getDuration());
            ps.setObject(5, film.getMpa());
            return ps;
        }, keyHolder);
        Integer id = keyHolder.getKeyAs(Integer.class);

        film.getGenres().stream().forEach(genre_id -> jdbc.update(ADD_GENRE_TO_FILM_QUERY, id, genre_id));

        if (id != null) {
            film.setId(id);
            return Optional.of(film);
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    @Override
    public Optional<Film> changeFilm(Film film) {
        int rowsUpdated = jdbc.update(UPDATE_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getId());
        jdbc.update(DELETE_GENRES_FROM_FILM_QUERY, film.getId());
        film.getGenres().stream().forEach(genre_id -> jdbc.update(ADD_GENRE_TO_FILM_QUERY, film.getId(), genre_id));

        if (rowsUpdated == 1) {
            return Optional.of(film);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void deleteFilm(int id) {
        int rowsDeleted = jdbc.update(DELETE_QUERY, id);
    }

    @Override
    public Optional<Film> getFilm(int id) {
        try {
            Film result = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            ArrayList<Genre> genreList = new ArrayList<>(jdbc.query(GET_GENRES_OF_FILM_BY_FILM_ID_QUERY,
                    new GenreRowMapper(), id));
            genreList.stream().forEach(genre -> result.getGenres().add(genre.getId()));
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public boolean containsId(int id) {
        try {
            Film result = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            return true;
        } catch (EmptyResultDataAccessException ignored) {
            return false;
        }
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbc.update(INSERT_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        int rowsDeleted = jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    public Collection<Film> getPopularFilms(int count) {

        var filmList = jdbc.query(GET_POPULAR_FILMS_QUERY, mapper, count);
        filmList.stream().forEach(film -> {
            film.setGenres(jdbc.query(GET_GENRES_OF_FILM_BY_FILM_ID_QUERY,
                            new GenreRowMapper(), film.getId()).stream()
                    .map(genre -> genre.getId())
                    .collect(Collectors.toSet()));
        });
        return filmList;
    }

    @Override
    public Collection<Film> getFilms() {
        var filmList = jdbc.query(GET_FILMS_QUERY, mapper);
        filmList.stream().forEach(film -> {
            film.setGenres(jdbc.query(GET_GENRES_OF_FILM_BY_FILM_ID_QUERY,
                            new GenreRowMapper(), film.getId()).stream()
                    .map(genre -> genre.getId())
                    .collect(Collectors.toSet()));
        });
        return filmList;
    }

    @Override
    public void deleteFilms() {
        this.deleteAllGenres();
        jdbc.update(DELETE_FILMS_QUERY);
    }

    private void deleteAllGenres() {
        jdbc.update(DELETE_GENRES_FROM_FILMS_QUERY);
    }

}

