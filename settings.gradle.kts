/*
 * Copyright 2019-2022 the original author or authors.
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

rootProject.name = "Hartshorn"

gradle.startParameter.isContinueOnFailure = true

includeBuild("gradle/plugins")

includeAll(rootDir, "")
configureChildren(rootProject)

fun includeAll(dir: File, prefix: String) {
    dir.listFiles()?.forEach {
        if (it.isDirectory && File(it, "${it.name}.gradle.kts").exists()) {
            include("${prefix}:${it.name}")
            // Include all nested projects
            includeAll(it, ":${it.name}")
        }
    }
}

fun configureChildren(project: ProjectDescriptor) {
    if (project.children.isNotEmpty()) {
        project.children.forEach {
            it.buildFileName = "${it.name}.gradle.kts"
            configureChildren(it)
        }
    }
}
