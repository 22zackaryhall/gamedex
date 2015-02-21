package com.github.ykrasik.gamedex.persistence.entity;

import com.github.ykrasik.gamedex.persistence.dao.exclude.ExcludePathDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Yevgeny Krasik
 */
@Data
@Accessors(fluent = true)
@DatabaseTable(tableName = "exclude_paths", daoClass = ExcludePathDaoImpl.class)
public class ExcludedPathEntity {
    public static final String PATH_COLUMN = "path";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = PATH_COLUMN, unique = true, canBeNull = false, index = true)
    private String path;
}
