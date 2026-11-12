package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.storage.film.model.FilmDbExtended;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmDBExtendedRowMapper implements RowMapper<FilmDbExtended> {
    @Override
    public FilmDbExtended mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new FilmDbExtended(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getDate("releaseDate").toLocalDate(),
                resultSet.getInt("duration"),
                resultSet.getInt("rating_id"),
                resultSet.getString("rating_name"),
                resultSet.getInt("genre_id"),
                resultSet.getString("genre_name")
        );
    }
}
