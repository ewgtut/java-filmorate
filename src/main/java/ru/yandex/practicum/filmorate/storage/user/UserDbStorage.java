package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    private static final String INSERT_USER_QUERY = "INSERT INTO USERS(email, login, name, birthday) " +
            "VALUES (?, ?, ?, ?) ";
    private static final String UPDATE_QUERY = "UPDATE USERS SET email = ?, login = ?, name = ?," +
            "birthday = ? WHERE id = ?";
    private static final String GET_USERS_QUERY = "SELECT * from USERS";
    private static final String DELETE_USERS_QUERY = "DELETE FROM USERS";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM USERS WHERE id = ?";
    private static final String INSERT_FRIEND_QUERY = "INSERT INTO FRIENDS(friend1_id, friend2_id, status) " +
            "VALUES (?,?,?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM FRIENDS WHERE friend1_id = ? and friend2_id = ?";
    private static final String GET_FRIENDS_QUERY = "SELECT * from USERS WHERE ID IN (SELECT friend2_id from FRIENDS " +
            "where friend1_id = ? )";
    private static final String GET_COMMON_FRIENDS_QUERY = "SELECT * from USERS WHERE ID IN (SELECT friend2_id from FRIENDS " +
            "where friend1_id = ?  INTERSECT SELECT friend2_id from FRIENDS where friend1_id = ?)";

    @Override
    public User addUser(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, user.getEmail());
            ps.setObject(2, user.getLogin());
            ps.setObject(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);
        Integer id = keyHolder.getKeyAs(Integer.class);

        if (id != null) {
            user.setId(id);
            return user;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    @Override
    public Optional<User> changeUser(User user) {
        int rowsUpdated = jdbc.update(UPDATE_QUERY, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday(), user.getId());
        if (rowsUpdated == 1) {
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getUsers() {
        return jdbc.query(GET_USERS_QUERY, mapper);
    }

    @Override
    public void deleteUsers() {
        jdbc.update(DELETE_USERS_QUERY);
    }

    @Override
    public Optional<User> getUser(int id) {
        try {
            User result = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(int userId, int friendId) {
        jdbc.update(INSERT_FRIEND_QUERY, userId, friendId, 1);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> getFriends(int id) {
        return jdbc.query(GET_FRIENDS_QUERY, mapper, id);
    }

    @Override
    public List<User> getCommonFriends(int userId, int friendId) {
        return jdbc.query(GET_COMMON_FRIENDS_QUERY, mapper, userId, friendId);
    }

    @Override
    public boolean containsId(int id) {
        try {
            User result = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            return true;
        } catch (EmptyResultDataAccessException ignored) {
            return false;
        }
    }

}
