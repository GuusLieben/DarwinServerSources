/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.core.util.inject

import com.google.inject.AbstractModule
import org.dockbox.selene.core.i18n.common.ResourceService
import org.dockbox.selene.core.server.config.GlobalConfig
import org.dockbox.selene.core.DiscordUtils
import org.dockbox.selene.core.events.EventBus
import org.dockbox.selene.core.ExceptionHelper
import org.dockbox.selene.core.extension.ExtensionManager
import org.dockbox.selene.core.files.ConfigurateManager
import org.dockbox.selene.core.util.player.PlayerStorageService
import org.dockbox.selene.core.BroadcastService

abstract class SeleneInjectModule : AbstractModule() {

    override fun configure() {
        this.configureExceptionInject()
        this.configureExtensionInject()
        this.configureUtilInject()
        this.configurePlatformInject()
        this.configureDefaultInject()
    }

    protected abstract fun configureExceptionInject()
    protected abstract fun configureExtensionInject()
    protected abstract fun configureUtilInject()
    protected abstract fun configurePlatformInject()
    protected abstract fun configureDefaultInject()

    companion object {
        val requiredBindings: Array<Class<*>> = arrayOf(
                ExceptionHelper::class.java,
                ExtensionManager::class.java,
                ConfigurateManager::class.java,
                EventBus::class.java,
                DiscordUtils::class.java,
                BroadcastService::class.java,
                PlayerStorageService::class.java,
                ResourceService::class.java,
                GlobalConfig::class.java
        )
    }
}
