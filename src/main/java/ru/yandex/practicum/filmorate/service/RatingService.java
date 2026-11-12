package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoSuchEntityException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.rating.RatingStorage;

import java.util.Collection;

@Service
public class RatingService {
    private final RatingStorage ratingStorage;

    @Autowired
    public RatingService(RatingStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    public Rating getRating(int id) {
        return ratingStorage.getRating(id)
                .orElseThrow(() -> new NoSuchEntityException(String.format("Рейтинга с id %d нет!", id)));
    }

    public Collection<Rating> getRatings() {
        return ratingStorage.getRatings();
    }

}
