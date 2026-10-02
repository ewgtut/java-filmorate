package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(classes = FilmorateApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class FilmorateApplicationTests {

    private static final int port = 8080;
    private static GsonBuilder gsonBuilder;
    private static Gson gson;
    private static HttpClient client;
    private URI filmUri = URI.create("http://localhost:" + port + "/films");
    private URI userUri = URI.create("http://localhost:" + port + "/users");

    @BeforeAll
    static void beforeAll() {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
        client = HttpClient.newBuilder()
                .build();
    }

    @BeforeEach
    void beforeEach() throws Exception {
        //очитска данных перед тестов
        HttpRequest request = HttpRequest.newBuilder()
                .uri(filmUri)
                .DELETE()
                .build();
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder()
                .uri(userUri)
                .DELETE()
                .build();
        resp = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // тесты /film
    @Test
    void getFilms_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(filmUri)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, resp.statusCode(), "Ожидается код возврата 200");
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        Assertions.assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных ");
        String body = resp.body().trim();
        Assertions.assertTrue(body.startsWith("[") && body.endsWith("]"), "Ожидается JSON-массив");
        List<Film> movieList = gson.fromJson(body, new ListOfFilmesTypeToken().getType());
        Assertions.assertEquals(0, movieList.size(), "Ожидается пустой список фильмов");
    }

    @Test
    void getFilms_whenAddedOne_returnsArrayWithOneFilm() throws Exception {

        String jsonFilm = gson.toJson(new Film(1, "Film_1", "Good film!",
                LocalDate.of(1994, 1, 1), 100));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(filmUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonFilm))
                .build();


        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, resp.statusCode(), "Ожидается код возврата 200");

        request = HttpRequest.newBuilder()
                .uri(filmUri)
                .header("Accept", "application/json")
                .GET()
                .build();
        resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        Assertions.assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных ");
        String body = resp.body().trim();
        Assertions.assertTrue(body.startsWith("[") && body.endsWith("]"), "Ожидается JSON-массив");
        List<Film> movieList = gson.fromJson(body, new ListOfFilmesTypeToken().getType());
        Assertions.assertEquals(1, movieList.size(), "Ожидается список фильмов с одним фильмом");
    }

    @Test
    void postFilms_withWrongReleaseDate_returnsErrorCode() throws Exception {

        String jsonFilm = gson.toJson(new Film(1, "Film_1", "Good film!",
                LocalDate.of(1694, 1, 1), 100));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(filmUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonFilm))
                .build();


        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(500, resp.statusCode(), "Ожидается код возврата 500 - ошибка");

    }

    @Test
    void putFilms_withDifferentDescription_returnsNewDescriptionFilm() throws Exception {

        final String newDescription = "Very good film!";

        String jsonFilm1 = gson.toJson(new Film(1, "Film_1", "Good film!",
                LocalDate.of(1994, 1, 1), 100));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(filmUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonFilm1))
                .build();


        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        String jsonFilm2 = gson.toJson(new Film(1, "Film_1", newDescription,
                LocalDate.of(1994, 1, 1), 100));

        request = HttpRequest.newBuilder()
                .uri(filmUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonFilm2))
                .build();

        resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        Film filmDeserialized = gson.fromJson(resp.body(), Film.class);

        Assertions.assertEquals(1, filmDeserialized.getId());
        Assertions.assertEquals(newDescription, filmDeserialized.getDescription());
    }

    // тесты /user
    @Test
    void getUsers_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(userUri)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, resp.statusCode(), "Ожидается код возврата 200");
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        Assertions.assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных ");
        String body = resp.body().trim();
        Assertions.assertTrue(body.startsWith("[") && body.endsWith("]"), "Ожидается JSON-массив");
        List<Film> userList = gson.fromJson(body, new ListOfUsersTypeToken().getType());
        Assertions.assertEquals(0, userList.size(), "Ожидается пустой список пользователей");
    }

    @Test
    void getUsers_whenAddedOne_returnsArrayWithOneUser() throws Exception {

        String jsonUser = gson.toJson(new User(0, "user@email.com",
                "userLogin", "userName", LocalDate.of(2000, 1, 1)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(userUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonUser))
                .build();


        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, resp.statusCode(), "Ожидается код возврата 200");

        request = HttpRequest.newBuilder()
                .uri(userUri)
                .header("Accept", "application/json")
                .GET()
                .build();
        resp = client.send(request, HttpResponse.BodyHandlers.ofString());
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        Assertions.assertEquals("application/json", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных ");
        String body = resp.body().trim();
        Assertions.assertTrue(body.startsWith("[") && body.endsWith("]"), "Ожидается JSON-массив");
        List<User> userList = gson.fromJson(body, new ListOfUsersTypeToken().getType());
        Assertions.assertEquals(1, userList.size(), "Ожидается список пользователей с одним пользователем");
    }

    @Test
    void postUsers_withWrongBirthDate_returnsErrorCode() throws Exception {

        String jsonUser = gson.toJson(new User(0, "user@email.com",
                "userLogin", "userName", LocalDate.of(4000, 1, 1)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(userUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonUser))
                .build();


        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(500, resp.statusCode(), "Ожидается код возврата 500 - ошибка");

    }

    @Test
    void putUsers_withDifferentName_returnsNewNameUser() throws Exception {

        final String newName = "User1234!";

        String jsonUser = gson.toJson(new User(1, "user@email.com",
                "userLogin", "userName", LocalDate.of(2000, 1, 1)));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(userUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonUser))
                .build();


        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        String jsonUser2 = gson.toJson(new User(1, "user@email.com",
                "userLogin", newName, LocalDate.of(2000, 1, 1)));

        request = HttpRequest.newBuilder()
                .uri(userUri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonUser2))
                .build();

        resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        User userDeserialized = gson.fromJson(resp.body(), User.class);
        Assertions.assertEquals(1, userDeserialized.getId());
        Assertions.assertEquals(newName, userDeserialized.getName());
    }
}
