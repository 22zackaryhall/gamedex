package com.github.ykrasik.gamedex.datamodel;

import com.github.ykrasik.gamedex.common.enums.EnumIdConverter;
import com.github.ykrasik.gamedex.common.enums.IdentifiableEnum;
import com.github.ykrasik.gamedex.common.util.ListUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author Yevgeny Krasik
 */
public enum GamePlatform implements IdentifiableEnum<String> {
    PC("PC"),
    XBOX_360("Xbox 360"),
    XBOX_ONE("Xbox One"),
    PS3("PlayStation 3"),
    PS4("PlayStation 4");

    private static final EnumIdConverter<String, GamePlatform> VALUES = new EnumIdConverter<>(GamePlatform.class);

    private final String key;

    GamePlatform(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    public static GamePlatform fromString(String name) {
        return VALUES.get(name);
    }

    public static List<String> getKeys() {
        return ListUtils.map(Arrays.asList(values()), GamePlatform::getKey);
    }
}