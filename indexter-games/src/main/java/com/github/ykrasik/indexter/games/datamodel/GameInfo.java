package com.github.ykrasik.indexter.games.datamodel;

import com.github.ykrasik.indexter.util.UrlUtils;
import com.google.common.base.MoreObjects;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Yevgeny Krasik
 */
public class GameInfo {
    private final String name;
    private final Optional<String> description;
    private final GamePlatform gamePlatform;
    private final Optional<LocalDate> releaseDate;
    private final double criticScore;
    private final double userScore;
    private final List<String> genres;
    private final List<String> publishers;
    private final List<String> developers;
    private final Optional<String> url;
    private final Optional<byte[]> thumbnailData;
    private final Optional<Image> thumbnail;

    public GameInfo(String name,
                    Optional<String> description,
                    GamePlatform gamePlatform,
                    Optional<LocalDate> releaseDate,
                    double criticScore,
                    double userScore,
                    List<String> genres,
                    List<String> publishers,
                    List<String> developers,
                    Optional<String> url,
                    Optional<byte[]> thumbnailData) {
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.gamePlatform = Objects.requireNonNull(gamePlatform);
        this.releaseDate = Objects.requireNonNull(releaseDate);
        this.criticScore = criticScore;
        this.userScore = userScore;
        this.genres = Objects.requireNonNull(genres);
        this.publishers = Objects.requireNonNull(publishers);
        this.developers = Objects.requireNonNull(developers);
        this.url = Objects.requireNonNull(url);
        this.thumbnailData = Objects.requireNonNull(thumbnailData);

        // There is no need to close byte array input streams.
        this.thumbnail = thumbnailData.map(data -> new Image(new ByteArrayInputStream(data)));
    }

    public String getName() {
        return name;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public GamePlatform getGamePlatform() {
        return gamePlatform;
    }

    public Optional<LocalDate> getReleaseDate() {
        return releaseDate;
    }

    public double getCriticScore() {
        return criticScore;
    }

    public double getUserScore() {
        return userScore;
    }

    public List<String> getGenres() {
        return genres;
    }

    public List<String> getPublishers() {
        return publishers;
    }

    public List<String> getDevelopers() {
        return developers;
    }

    public Optional<String> getUrl() {
        return url;
    }

    public Optional<byte[]> getThumbnailData() {
        return thumbnailData;
    }

    public Optional<Image> getThumbnail() {
        return thumbnail;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("gamePlatform", gamePlatform)
            .toString();
    }

    public static GameInfo from(String name,
                                Optional<String> description,
                                GamePlatform gamePlatform,
                                Optional<LocalDate> releaseDate,
                                double criticScore,
                                double userScore,
                                List<String> genres,
                                List<String> publishers,
                                List<String> developers,
                                Optional<String> url,
                                Optional<String> thumbnailUrl) throws IOException {
        final Optional<byte[]> thumbnailData;
        if (thumbnailUrl.isPresent()) {
            thumbnailData = Optional.of(UrlUtils.fetchData(thumbnailUrl.get()));
        } else {
            thumbnailData = Optional.empty();
        }

        return new GameInfo(
            name, description, gamePlatform, releaseDate, criticScore, userScore,
            genres, publishers, developers, url, thumbnailData
        );
    }
}