package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage{
    private static final String GET_GENRE_BY_ID_QUERY = "SELECT * FROM GENRE WHERE ID = ?";
    private static final String GET_GENRES_QUERY = "SELECT * FROM GENRE ";

    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    public Optional<Genre> getGenre(int id) {
        try {
            Genre result = jdbc.queryForObject(GET_GENRE_BY_ID_QUERY, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Collection<Genre> getGenres() {
        return jdbc.query(GET_GENRES_QUERY, mapper);
    }
}