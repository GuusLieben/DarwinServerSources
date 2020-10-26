/*
 *  Copyright (C) 2020 Guus Lieben
 *
 *  This framework is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 2.1 of the
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 *  the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package org.dockbox.selene.test.util;

import org.dockbox.selene.core.command.CommandBus;
import org.dockbox.selene.core.i18n.common.ResourceService;
import org.dockbox.selene.core.impl.i18n.common.SimpleResourceService;
import org.dockbox.selene.core.impl.server.config.DefaultGlobalConfig;
import org.dockbox.selene.core.impl.util.events.SimpleEventBus;
import org.dockbox.selene.core.impl.util.exceptions.SimpleExceptionHelper;
import org.dockbox.selene.core.impl.util.extension.SimpleExtensionManager;
import org.dockbox.selene.core.impl.util.text.SimpleBroadcastService;
import org.dockbox.selene.core.server.Selene;
import org.dockbox.selene.core.server.config.GlobalConfig;
import org.dockbox.selene.core.util.construct.ConstructionUtil;
import org.dockbox.selene.core.util.discord.DiscordUtils;
import org.dockbox.selene.core.util.events.EventBus;
import org.dockbox.selene.core.util.exceptions.ExceptionHelper;
import org.dockbox.selene.core.util.extension.ExtensionManager;
import org.dockbox.selene.core.util.files.ConfigurateManager;
import org.dockbox.selene.core.util.inject.SeleneInjectModule;
import org.dockbox.selene.core.util.player.PlayerStorageService;
import org.dockbox.selene.core.util.text.BroadcastService;
import org.dockbox.selene.core.util.threads.ThreadUtils;
import org.dockbox.selene.core.util.world.WorldStorageService;
import org.dockbox.selene.test.extension.IntegratedTestExtension;

public class TestInjector extends SeleneInjectModule {

    @Override
    protected void configureExceptionInject() {
        super.bind(ExceptionHelper.class).to(SimpleExceptionHelper.class);
    }

    @Override
    protected void configureExtensionInject() {
        super.bind(ExtensionManager.class).to(SimpleExtensionManager.class);
    }

    @Override
    protected void configureUtilInject() {
        super.bind(BroadcastService.class).to(SimpleBroadcastService.class);
        super.bind(CommandBus.class).to(TestCommandBus.class);
        super.bind(ConfigurateManager.class).to(TestConfigurateManager.class);
        super.bind(DiscordUtils.class).to(TestDiscordUtils.class);
        super.bind(EventBus.class).to(SimpleEventBus.class);
        super.bind(GlobalConfig.class).to(DefaultGlobalConfig.class);
        super.bind(Selene.IntegratedExtension.class).to(IntegratedTestExtension.class);
        super.bind(ResourceService.class).to(SimpleResourceService.class);
        super.bind(ConstructionUtil.class).to(TestConstructionUtil.class);
        super.bind(PlayerStorageService.class).to(TestPlayerStorageService.class);
        super.bind(ThreadUtils.class).to(TestThreadUtils.class);
        super.bind(WorldStorageService.class).to(TestWorldStorageService.class);
    }
}
