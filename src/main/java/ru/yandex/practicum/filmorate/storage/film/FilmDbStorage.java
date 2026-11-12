package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.model.FilmDbExtended;
import ru.yandex.practicum.filmorate.storage.mappers.FilmDBExtendedRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Primary
public class FilmDbStorage implements FilmStorage {
    private static final String INSERT_FILM_QUERY = "INSERT INTO FILM(name, description, releaseDate, duration, rating_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE FILM SET name = ?, description = ?, releaseDate = ?," +
            "duration = ?, rating_id = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM FILM WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT FILM.id, FILM.name, FILM.description," +
            "FILM.releaseDate, FILM.DURATION, FILM.rating_id, RATING.name as rating_name, GENRE.id as genre_id," +
            "GENRE.name as genre_name " +
            "FROM FILM LEFT JOIN RATING ON FILM.rating_id = RATING.id " +
            "LEFT JOIN GENRE_FILM ON FILM.id = GENRE_FILM.film_id " +
            "LEFT JOIN GENRE on GENRE_FILM.genre_id = GENRE.id " +
            "WHERE FILM.id = ?";

    private static final String INSERT_LIKE_QUERY = "INSERT INTO LIKES(film_id, user_id) VALUES (?,?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM LIKES WHERE film_id = ? and user_id = ?";
    private static final String GET_POPULAR_FILMS_QUERY = "SELECT FILM.id, FILM.name, FILM.description," +
            "FILM.releaseDate, FILM.DURATION, FILM.rating_id, RATING.name as rating_name, GENRE.id as genre_id," +
            "GENRE.name as genre_name " +
            "FROM FILM LEFT JOIN RATING ON FILM.rating_id = RATING.id " +
            "LEFT JOIN GENRE_FILM ON FILM.id = GENRE_FILM.film_id " +
            "LEFT JOIN GENRE on GENRE_FILM.genre_id = GENRE.id " +
            "join " +
            "(SELECT " +
            "FILM.id as id, COUNT(*) as count " +
            "from FILM JOIN LIKES ON FILM.id = LIKES.FILM_ID " +
            "GROUP BY FILM.id " +
            "LIMIT ?) as count_table on Film.id = count_table.id " +
            "ORDER BY count_table.count DESC";

    private static final String GET_FILMS_QUERY = "SELECT FILM.id, FILM.name, FILM.description," +
            "FILM.releaseDate, FILM.DURATION, FILM.rating_id, RATING.name as rating_name, GENRE.id as genre_id," +
            "GENRE.name as genre_name " +
            "FROM FILM LEFT JOIN RATING ON FILM.rating_id = RATING.id " +
            "LEFT JOIN GENRE_FILM ON FILM.id = GENRE_FILM.film_id " +
            "LEFT JOIN GENRE on GENRE_FILM.genre_id = GENRE.id";
    private static final String DELETE_FILMS_QUERY = "DELETE FROM FILM";
    private static final String ADD_GENRE_TO_FILM_QUERY = "INSERT INTO GENRE_FILM (film_id, genre_id) VALUES(?,?)";
    private static final String DELETE_GENRES_FROM_FILM_QUERY = "DELETE FROM GENRE_FILM WHERE film_id = ?";

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
            ps.setObject(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        Integer id = keyHolder.getKeyAs(Integer.class);

        var genreList = film.getGenres();
        jdbc.batchUpdate(ADD_GENRE_TO_FILM_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Genre genre = new ArrayList<>(genreList).get(i);
                ps.setObject(1, id);
                ps.setObject(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return genreList.size();
            }
        });
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
                film.getDuration(), film.getMpa().getId(), film.getId());
        jdbc.update(DELETE_GENRES_FROM_FILM_QUERY, film.getId());
        var genreList = film.getGenres();
        jdbc.batchUpdate(ADD_GENRE_TO_FILM_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Genre genre = new ArrayList<>(genreList).get(i);
                ps.setObject(1, film.getId());
                ps.setObject(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return genreList.size();
            }
        });

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
            var result = jdbc.query(FIND_BY_ID_QUERY, new FilmDBExtendedRowMapper(), id);
            return Optional.ofNullable(getFilmFromFilmDbExtended(result).get(0));
        } catch (EmptyResultDataAccessException | IndexOutOfBoundsException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public boolean containsId(int id) {
        try {
            var result = jdbc.query(FIND_BY_ID_QUERY, new FilmDBExtendedRowMapper(), id);
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
        var filmDbList = jdbc.query(GET_POPULAR_FILMS_QUERY, new FilmDBExtendedRowMapper(), count);
        return getFilmFromFilmDbExtended(filmDbList);
    }

    @Override
    public Collection<Film> getFilms() {
        var filmList = jdbc.query(GET_FILMS_QUERY, new FilmDBExtendedRowMapper());
        return getFilmFromFilmDbExtended(filmList);
    }

    @Override
    public void deleteFilms() {
        jdbc.update(DELETE_FILMS_QUERY);
    }

    private List<Film> getFilmFromFilmDbExtended(List<FilmDbExtended> filmDbExtended) {
        LinkedHashSet<Integer> idSet = new LinkedHashSet<>(filmDbExtended.stream()
                .map(filmDb -> filmDb.getId()).collect(Collectors.toUnmodifiableList()));
        var userList = new ArrayList<Film>();

        idSet.stream().forEach(id -> {
            var filteredFilmList = new ArrayList<FilmDbExtended>(
                    filmDbExtended.stream().filter(filmDb -> filmDb.getId() == id).toList());
            userList.add(new Film(id,
                    filteredFilmList.get(0).getName(),
                    filteredFilmList.get(0).getDescription(),
                    filteredFilmList.get(0).getReleaseDate(),
                    filteredFilmList.get(0).getDuration(),
                    new Rating(filteredFilmList.get(0).getMpaId(), filteredFilmList.get(0).getMpaName()),
                    new HashSet<>(
                            filteredFilmList.stream().filter(filmDb -> filmDb.getGenreId() != 0)
                                    .map(filmDb -> {
                                return new Genre(filmDb.getGenreId(), filmDb.getGenreName());
                            }).toList()
                    )
            ));

        });
        return userList;
    }

}

