package com.github.ykrasik.gamedex.core.config;

import com.github.ykrasik.gamedex.common.service.AbstractService;
import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.impl.factory.Maps;
import com.thoughtworks.xstream.XStream;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.boon.IO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Yevgeny Krasik
 */
@Slf4j
@Accessors(fluent = true)
@SuppressWarnings("unchecked")
public class ConfigServiceImpl extends AbstractService implements ConfigService {
    private static final String NAME = "config.xml";
    private static final XStream XSTREAM = new XStream();

    private Path file;

    private ImmutableMap<ConfigType, ObjectProperty<Object>> propertyMap = Maps.immutable.empty();

    @Override
    protected void doStart() throws Exception {
        this.file = getFile();
        final String fileContent = IO.read(file);
        if (!fileContent.isEmpty()) {
            final Map<ConfigType, Object> valueMap = (Map<ConfigType, Object>) XSTREAM.fromXML(fileContent);
            propertyMap = fromValueMap(valueMap);
        } else {
            for (ConfigType type : ConfigType.values()) {
                propertyMap = propertyMap.newWithKeyValue(type, new SimpleObjectProperty<>(type.getDefaultValue()));
            }
        }
    }

    private Path getFile() throws IOException {
        Path path = Paths.get(NAME);
        if (!Files.exists(path)) {
            path = Files.createFile(path);
        }
        return path;
    }

    @Override
    protected void doStop() throws Exception {
        IO.write(file, XSTREAM.toXML(toValueMap()));
    }

    @Override
    public <T> ObjectProperty<T> property(ConfigType type) {
        return (ObjectProperty<T>) Objects.requireNonNull(propertyMap.get(type));
    }

    private ImmutableMap<ConfigType, ObjectProperty<Object>> fromValueMap(Map<ConfigType, Object> valueMap) {
        return Maps.immutable.ofMap(valueMap).collectValues((key, value) -> new SimpleObjectProperty<>(value));
    }

    private Map<ConfigType, Object> toValueMap() {
        // MutableMap doesn't deserialize without exceptions.
        return new HashMap<>(propertyMap.collectValues((key, value) -> value.get()).toMap());
    }
}
