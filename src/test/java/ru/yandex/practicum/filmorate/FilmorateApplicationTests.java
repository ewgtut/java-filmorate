package ru.yandex.practicum.filmorate;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@AutoConfigureTestDatabase
@SpringBootTest(classes = FilmorateApplication.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    @BeforeEach
    public void beforeEach() {
        userStorage.deleteUsers();
        filmStorage.deleteFilms();
    }

    @Test
    public void testFindUserById() {
        var newUser = userStorage.addUser(new User(1, "email@email.ru", "login",
                "Name", LocalDate.of(1999, 1, 1)));
        int newUserId = newUser.getId();

        Optional<User> userOptional = userStorage.getUser(newUserId);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", newUserId)
                );
    }

    @Test
    public void testChangeUser() {

        var newUser = userStorage.addUser(new User(1, "email@email.ru", "login",
                "Name", LocalDate.of(1999, 1, 1)));
        int newUserId = newUser.getId();

        Optional<User> userOptional = userStorage.getUser(newUserId);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", newUserId)
                );
        userStorage.changeUser(new User(newUserId, "email@email.ru", "login",
                "Name2", LocalDate.of(1999, 2, 2)));

        userOptional = userStorage.getUser(newUserId);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", newUserId))
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Name2"))
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("birthday",
                                LocalDate.of(1999, 2, 2)));
    }

    @Test
    public void testGetUsers() {

        var newUser1 = userStorage.addUser(new User(1, "email1@email.ru", "login1",
                "Name1", LocalDate.of(1999, 1, 1)));
        int newUser1Id = newUser1.getId();

        var newUser2 = userStorage.addUser(new User(1, "email2@email.ru", "login2",
                "Name2", LocalDate.of(1999, 2, 2)));
        int newUser2Id = newUser2.getId();

        var userList = new ArrayList<>(userStorage.getUsers());
        Assertions.assertEquals(2, userList.size());
    }

    @Test
    public void testDeleteUsers() {

        var newUser1 = userStorage.addUser(new User(1, "email1@email.ru", "login1",
                "Name1", LocalDate.of(1999, 1, 1)));
        var newUser2 = userStorage.addUser(new User(1, "email2@email.ru", "login2",
                "Name2", LocalDate.of(1999, 2, 2)));
        var userList = new ArrayList<>(userStorage.getUsers());
        Assertions.assertEquals(2, userList.size());
        userStorage.deleteUsers();
        userList = new ArrayList<>(userStorage.getUsers());
        Assertions.assertEquals(0, userList.size());
    }

    @Test
    public void testAddDeleteFriends() {
        var newUser1 = userStorage.addUser(new User(1, "email1@email.ru", "login1",
                "Name1", LocalDate.of(1999, 1, 1)));
        int newUser1Id = newUser1.getId();

        var newUser2 = userStorage.addUser(new User(1, "email2@email.ru", "login2",
                "Name2", LocalDate.of(1999, 2, 2)));
        int newUser2Id = newUser2.getId();
        var newUser3 = userStorage.addUser(new User(1, "email3@email.ru", "login3",
                "Name3", LocalDate.of(1999, 3, 3)));
        int newUser3Id = newUser3.getId();

        userStorage.addFriend(newUser1Id, newUser2Id);
        userStorage.addFriend(newUser1Id, newUser3Id);

        var userList = userStorage.getFriends(newUser1Id);
        Assertions.assertEquals(2, userList.size());
        Assertions.assertTrue(userList.get(0).getId() == newUser2Id ||
                userList.get(0).getId() == newUser3Id);

        userStorage.deleteFriend(newUser1Id, newUser2Id);
        userStorage.deleteFriend(newUser1Id, newUser3Id);
        userList = userStorage.getFriends(newUser1Id);
        Assertions.assertEquals(0, userList.size());

    }

    @Test
    public void testGetCommonFriends() {
        var newUser1 = userStorage.addUser(new User(1, "email1@email.ru", "login1",
                "Name1", LocalDate.of(1999, 1, 1)));
        int newUser1Id = newUser1.getId();

        var newUser2 = userStorage.addUser(new User(1, "email2@email.ru", "login2",
                "Name2", LocalDate.of(1999, 2, 2)));
        int newUser2Id = newUser2.getId();
        var newUser3 = userStorage.addUser(new User(1, "email3@email.ru", "login3",
                "Name3", LocalDate.of(1999, 3, 3)));
        int newUser3Id = newUser3.getId();

        userStorage.addFriend(newUser1Id, newUser2Id);
        userStorage.addFriend(newUser3Id, newUser2Id);

        var userList = userStorage.getCommonFriends(newUser1Id, newUser3Id);
        Assertions.assertEquals(1, userList.size());
        Assertions.assertEquals(newUser2Id, userList.getFirst().getId());

        userStorage.deleteFriend(newUser1Id, newUser2Id);
        userStorage.deleteFriend(newUser3Id, newUser2Id);
    }

    @Test
    public void testGetFilmById() {
        ArrayList<Integer> genreList = new ArrayList<>();
        genreList.add(1);
        genreList.add(2);

        var newFilm = filmStorage.addFilm(new Film(1, "FilmName", "Description",
                LocalDate.of(1999, 1, 1), 100, 1, genreList));
        int newFilmId = newFilm.get().getId();

        Optional<Film> filmOptional = filmStorage.getFilm(newFilmId);
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", newFilmId)
                );

    }

    @Test
    public void testChangeFilm() {
        ArrayList<Integer> genreList = new ArrayList<>();
        genreList.add(1);
        genreList.add(2);

        var newFilm = filmStorage.addFilm(new Film(1, "FilmName", "Description",
                LocalDate.of(1999, 1, 1), 100, 1, genreList));
        int newFilmId = newFilm.get().getId();
        Optional<Film> filmOptional = filmStorage.getFilm(newFilmId);
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", newFilmId))
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "FilmName"));

        var newFilmRenewed = filmStorage.changeFilm(new Film(newFilmId, "FilmName_new", "Description",
                LocalDate.of(1999, 1, 1), 100, 1, genreList));
        filmOptional = filmStorage.getFilm(newFilmId);
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", newFilmId))
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "FilmName_new"));
    }

    @Test
    public void testDeleteFilm() {
        ArrayList<Integer> genreList = new ArrayList<>();
        genreList.add(1);
        genreList.add(2);

        var newFilm = filmStorage.addFilm(new Film(1, "FilmName", "Description",
                LocalDate.of(1999, 1, 1), 100, 1, genreList));
        int newFilmId = newFilm.get().getId();
        Optional<Film> filmOptional = filmStorage.getFilm(newFilmId);
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", newFilmId))
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "FilmName"));

        filmStorage.deleteFilm(newFilmId);
        filmOptional = filmStorage.getFilm(newFilmId);
        assertThat(filmOptional)
                .isEmpty();
    }

    @Test
    public void testAddLikeGetPopular() {
        ArrayList<Integer> genreList = new ArrayList<>();
        genreList.add(1);
        genreList.add(2);

        var newFilm = filmStorage.addFilm(new Film(1, "FilmName", "Description",
                LocalDate.of(1999, 1, 1), 100, 1, genreList));
        int newFilmId = newFilm.get().getId();

        var newUser = userStorage.addUser(new User(1, "email@email.ru", "login",
                "Name", LocalDate.of(1999, 1, 1)));
        int newUserId = newUser.getId();

        filmStorage.addLike(newFilmId, newUserId);

        var filmList = filmStorage.getPopularFilms(1);
        Assertions.assertTrue(filmList.contains(newFilm.get()));
    }

}
