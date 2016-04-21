/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.plugin.use.internal;

import org.gradle.api.Action;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.DependencyResolutionServices;
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.VersionSelectorScheme;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.plugin.use.resolve.internal.CustomRepositoryPluginResolver;
import org.gradle.plugin.use.resolve.internal.PluginResolver;

import java.net.URI;


class DefaultMavenPluginRepository implements PluginRepositoryInternal {


    private final FileResolver fileResolver;
    private final DependencyResolutionServices dependencyResolutionServices;
    private final VersionSelectorScheme versionSelectorScheme;

    private Object url;
    private PluginResolver resolver;

    DefaultMavenPluginRepository(FileResolver fileResolver, DependencyResolutionServices dependencyResolutionServices, VersionSelectorScheme versionSelectorScheme) {
        this.fileResolver = fileResolver;
        this.dependencyResolutionServices = dependencyResolutionServices;
        this.versionSelectorScheme = versionSelectorScheme;
    }

    @Override
    public String getName() {
        return "maven";
    }

    public URI getUrl() {
        return fileResolver.resolveUri(url);
    }

    public void setUrl(Object url) {
        if (resolver != null) {
            throw new IllegalStateException("The url of a plugin repository cannot be changed after it has been used to resolve plugins.");
        }
        this.url = url;
    }

    @Override
    public PluginResolver asResolver() {
        if (resolver == null) {
            dependencyResolutionServices.getResolveRepositoryHandler().maven(new Action<MavenArtifactRepository>() {
                @Override
                public void execute(MavenArtifactRepository mavenArtifactRepository) {
                    mavenArtifactRepository.setUrl(url);
                }
            });
            resolver = new CustomRepositoryPluginResolver(dependencyResolutionServices, versionSelectorScheme);
        }
        return resolver;
    }
}
