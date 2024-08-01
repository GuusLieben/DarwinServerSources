/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.org.dockbox.hartshorn.i18n;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.i18n.Message;
import org.dockbox.hartshorn.i18n.annotations.InjectTranslation;

@Service(id = "resource")
public interface ITestResources {

    @InjectTranslation(defaultValue = "Hello world!")
    Message testEntry();

    @InjectTranslation(defaultValue = "Hello {0}!")
    Message parameterTestEntry(String name);

}