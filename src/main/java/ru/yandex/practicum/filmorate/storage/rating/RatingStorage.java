package ru.yandex.practicum.filmorate.storage.rating;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

public interface RatingStorage {
    Optional<Rating> getRating(int id);

    Collection<Rating> getRatings();
}
