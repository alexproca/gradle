/*
 * Copyright 2014 the original author or authors.
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
import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.file.FileLookup;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.plugins.PluginRegistry;
import org.gradle.internal.Factory;
import org.gradle.plugin.use.resolve.internal.*;
import org.gradle.plugin.use.resolve.service.internal.InjectedClasspathPluginResolver;
import org.gradle.plugin.use.resolve.service.internal.PluginResolutionServiceResolver;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class PluginResolverFactory implements Factory<PluginResolver> {

    private final PluginRegistry pluginRegistry;
    private final DocumentationRegistry documentationRegistry;
    private final PluginResolutionServiceResolver pluginResolutionServiceResolver;
    private final DefaultPluginRepositoryHandler pluginRepositoryHandler;
    private final InjectedClasspathPluginResolver injectedClasspathPluginResolver;
    private final FileLookup fileLookup;

    public PluginResolverFactory(
            PluginRegistry pluginRegistry,
            DocumentationRegistry documentationRegistry,
            PluginResolutionServiceResolver pluginResolutionServiceResolver,
            DefaultPluginRepositoryHandler pluginRepositoryHandler,
            InjectedClasspathPluginResolver injectedClasspathPluginResolver,
            FileLookup fileLookup
    ) {
        this.pluginRegistry = pluginRegistry;
        this.documentationRegistry = documentationRegistry;
        this.pluginResolutionServiceResolver = pluginResolutionServiceResolver;
        this.pluginRepositoryHandler = pluginRepositoryHandler;
        this.injectedClasspathPluginResolver = injectedClasspathPluginResolver;
        this.fileLookup = fileLookup;
    }

    public PluginResolver create() {
        List<PluginResolver> resolvers = new LinkedList<PluginResolver>();
        addDefaultResolvers(resolvers);
        return new CompositePluginResolver(resolvers);
    }

    /**
     * Returns the default PluginResolvers used by Gradle.
     * <p>
     * The plugins will be searched in a chain from the first to the last until a plugin is found.
     * So, order matters.
     * <p>
     * <ol>
     *     <li>{@link NoopPluginResolver} - Only used in tests.</li>
     *     <li>{@link CorePluginResolver} - distributed with Gradle</li>
     *     <li>{@link InjectedClasspathPluginResolver} - from a TestKit test's ClassPath</li>
     *     <li>{@link CustomRepositoryPluginResolver}s - from custom Maven/Ivy repositories</li>
     *     <li>{@link PluginResolutionServiceResolver} - from Gradle Plugin Portal</li>
     * </ol>
     * <p>
     * This order is optimized for both performance and to allow resolvers earlier in the order
     * to mask plugins which would have been found later in the order.
     */
    private void addDefaultResolvers(List<PluginResolver> resolvers) {
        resolvers.add(new NoopPluginResolver(pluginRegistry));
        resolvers.add(new CorePluginResolver(documentationRegistry, pluginRegistry));

        if (!injectedClasspathPluginResolver.isClasspathEmpty()) {
            resolvers.add(injectedClasspathPluginResolver);
        }

        addPluginRepositoryResolvers(resolvers);
        resolvers.add(pluginResolutionServiceResolver);
    }

    private void addPluginRepositoryResolvers(List<PluginResolver> resolvers) {
        //use a system property until we have the `pluginRepositores` block
        final String customRepoUrl = System.getProperty("org.gradle.plugin.repoUrl");
        if (customRepoUrl != null) {
            /*
             * this is a workaround for the fact that this code runs in a
             * context that does not have a base dir, so the identity file resolver is used.
             * That resolver cannot deal with relative paths. We use the current working dir
             * as a workaround. In the final implementation, the `pluginRepositories` block
             * will be executed in a context that has a base dir.
             */
            FileResolver urlResolver = fileLookup.getFileResolver(new File("").getAbsoluteFile());
            final String normalizedUrl = urlResolver.resolveUri(customRepoUrl).toString();
            PluginRepositoryInternal mavenPluginRepository = pluginRepositoryHandler.maven(new Action<DefaultMavenPluginRepository>() {
                @Override
                public void execute(DefaultMavenPluginRepository mavenPluginRepository) {
                    mavenPluginRepository.setUrl(normalizedUrl);
                }
            });
            resolvers.add(mavenPluginRepository.asResolver());
        }
    }
}
