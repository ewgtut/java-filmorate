package ru.yandex.practicum.filmorate.storage.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RatingDbStorage {
    private static final String GET_RATING_BY_ID_QUERY = "SELECT * FROM RATING WHERE ID = ?";
    private static final String GET_RATINGS_QUERY = "SELECT * FROM RATING ";

    private final JdbcTemplate jdbc;
    private final RatingRowMapper mapper;

    public Optional<Rating> getRating(int id) {
        try {
            Rating result = jdbc.queryForObject(GET_RATING_BY_ID_QUERY, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Collection<Rating> getRatings() {
        return jdbc.query(GET_RATINGS_QUERY, mapper);
    }
}