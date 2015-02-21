package com.github.ykrasik.gamedex.persistence.entity;

import com.github.ykrasik.gamedex.persistence.dao.genre.GenreDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Yevgeny Krasik
 */
@Data
@Accessors(fluent = true)
@DatabaseTable(tableName = "genres", daoClass = GenreDaoImpl.class)
public class GenreEntity {
    public static final String ID_COLUMN = "id";
    public static final String NAME_COLUMN = "name";

    @DatabaseField(columnName = ID_COLUMN, generatedId = true)
    private int id;

    @DatabaseField(columnName = NAME_COLUMN, canBeNull = false, index = true)
    private String name;
}
