package com.github.ykrasik.indexter.games.persistence.dao;

import com.github.ykrasik.indexter.games.persistence.entity.GameEntity;
import com.github.ykrasik.indexter.games.persistence.entity.GenreEntity;
import com.github.ykrasik.indexter.games.persistence.entity.GenreGameLinkEntity;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Yevgeny Krasik
 */
public interface GenreGameLinkDao extends Dao<GenreGameLinkEntity, Integer> {
    List<GenreEntity> getGenresByGame(GameEntity game) throws SQLException;

    void deleteByGameId(int gameId) throws SQLException;
}
