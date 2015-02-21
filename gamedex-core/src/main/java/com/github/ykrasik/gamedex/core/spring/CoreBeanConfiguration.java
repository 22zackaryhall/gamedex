package com.github.ykrasik.gamedex.core.spring;

import com.github.ykrasik.gamedex.common.spring.AbstractBeanConfiguration;
import com.github.ykrasik.gamedex.core.config.GameCollectionConfig;
import com.github.ykrasik.gamedex.core.config.GameCollectionConfigImpl;
import com.github.ykrasik.gamedex.core.manager.exclude.ExcludedPathManager;
import com.github.ykrasik.gamedex.core.manager.exclude.ExcludedPathManagerImpl;
import com.github.ykrasik.gamedex.core.manager.exclude.debug.ExcludedPathDebugCommands;
import com.github.ykrasik.gamedex.core.manager.game.GameManager;
import com.github.ykrasik.gamedex.core.manager.game.GameManagerImpl;
import com.github.ykrasik.gamedex.core.manager.game.debug.GameManagerDebugCommands;
import com.github.ykrasik.gamedex.core.manager.info.GameInfoProviderManager;
import com.github.ykrasik.gamedex.core.manager.info.GameInfoProviderManagerImpl;
import com.github.ykrasik.gamedex.core.manager.library.LibraryManager;
import com.github.ykrasik.gamedex.core.manager.library.LibraryManagerImpl;
import com.github.ykrasik.gamedex.core.manager.library.debug.LibraryManagerDebugCommands;
import com.github.ykrasik.gamedex.core.service.dialog.DialogService;
import com.github.ykrasik.gamedex.persistence.PersistenceService;
import com.github.ykrasik.gamedex.provider.GameInfoProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author Yevgeny Krasik
 */
@Configuration
public class CoreBeanConfiguration extends AbstractBeanConfiguration {
    @Bean
    public GameCollectionConfig gameCollectionConfig() throws IOException {
        preloader.message("Loading config...");
        return new GameCollectionConfigImpl();
    }

    @Bean
    public GameManager gameManager(PersistenceService persistenceService) {
        preloader.message("Loading game manager...");
        return new GameManagerImpl(persistenceService);
    }

    @Bean
    public GameManagerDebugCommands gameManagerDebugCommands(GameManager gameManager) {
        return new GameManagerDebugCommands(gameManager);
    }

    @Bean
    public LibraryManager libraryManager(PersistenceService persistenceService) {
        preloader.message("Loading library manager...");
        return new LibraryManagerImpl(persistenceService);
    }

    @Bean
    public LibraryManagerDebugCommands libraryManagerDebugCommands(LibraryManager libraryManager) {
        return new LibraryManagerDebugCommands(libraryManager);
    }

    @Bean
    public ExcludedPathManager excludedPathManager(PersistenceService persistenceService) {
        preloader.message("Loading excluded path manager...");
        return new ExcludedPathManagerImpl(persistenceService);
    }

    @Bean
    public ExcludedPathDebugCommands excludedPathDebugCommands(ExcludedPathManager excludedPathManager) {
        return new ExcludedPathDebugCommands(excludedPathManager);
    }

    @Qualifier("metacriticManager")
    @Bean
    public GameInfoProviderManager metacriticManager(DialogService dialogService,
                                                     @Qualifier("metacriticGameInfoProvider") GameInfoProvider metacriticGameInfoProvider) {
        return new GameInfoProviderManagerImpl(dialogService, metacriticGameInfoProvider, false);
    }

    @Qualifier("giantBombManager")
    @Bean
    public GameInfoProviderManager giantBombManager(DialogService dialogService,
                                                    @Qualifier("giantBombGameInfoProvider") GameInfoProvider giantBombGameInfoProvider) {
        return new GameInfoProviderManagerImpl(dialogService, giantBombGameInfoProvider, true);
    }
}
