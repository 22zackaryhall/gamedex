package com.github.ykrasik.indexter.games.persistence.entity;

import com.github.ykrasik.indexter.games.datamodel.GamePlatform;
import com.github.ykrasik.indexter.games.persistence.dao.LibraryDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

/**
 * @author Yevgeny Krasik
 */
@Data
@DatabaseTable(tableName = "libraries", daoClass = LibraryDaoImpl.class)
public class LibraryEntity {
    public static final String PATH_COLUMN = "path";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(columnName = PATH_COLUMN, unique = true, canBeNull = false, index = true)
    private String path;

    @DatabaseField(canBeNull = false)
    private GamePlatform platform;
}
