package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreStorage {
    public Optional<Genre> getGenre(int id);
    public Collection<Genre> getGenres();
}
