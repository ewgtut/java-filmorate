package ru.yandex.practicum.filmorate.storage.rating;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

public interface RatingStorage {
    public Optional<Rating> getRating(int id);
    public Collection<Rating> getRatings();
}
