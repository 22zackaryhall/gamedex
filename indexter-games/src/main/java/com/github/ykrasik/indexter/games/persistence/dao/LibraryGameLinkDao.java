package com.github.ykrasik.indexter.games.persistence.dao;

import com.github.ykrasik.indexter.games.persistence.entity.LibraryGameLinkEntity;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

/**
 * @author Yevgeny Krasik
 */
public interface LibraryGameLinkDao extends Dao<LibraryGameLinkEntity, Integer> {
    void deleteByGameId(int gameId) throws SQLException;

    void deleteByLibraryId(int libraryId) throws SQLException;
}
