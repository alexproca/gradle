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

package org.gradle.jvm;

import org.gradle.api.Incubating;
import org.gradle.jvm.platform.JavaPlatform;
import org.gradle.jvm.toolchain.JavaToolChain;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.internal.toolchain.ToolResolver;

import java.io.File;

/**
 * Represents a binary artifact that is the result of building a jvm component.
 */
@Incubating
public interface JvmBinarySpec extends BinarySpec {
    /**
     * The set of tasks associated with this binary.
     */
    JvmBinaryTasks getTasks();

    /**
     * The target platform for this binary.
     */
    JavaPlatform getTargetPlatform();

    /**
     * Sets the target platform for this binary.
     */
    void setTargetPlatform(JavaPlatform platform);

    /**
     * Returns the {@link org.gradle.jvm.toolchain.JavaToolChain} that will be used to build this binary.
     */
    JavaToolChain getToolChain();

    /**
     * Sets the {@link org.gradle.jvm.toolchain.JavaToolChain} that will be used to build this binary.
     */
    void setToolChain(JavaToolChain toolChain);

    /**
     * Returns the {@link org.gradle.jvm.toolchain.JavaToolChain} that will be used to build this binary.
     */
    ToolResolver getToolResolver();

    void setToolResolver(ToolResolver toolResolver);

    /**
     * The classes directory for this binary.
     */
    File getClassesDir();

    /**
     * Sets the classes directory for this binary.
     */
    void setClassesDir(File classesDir);

    /**
     * The resources directory for this binary.
     */
    File getResourcesDir();

    /**
     * Sets the resources directory for this binary.
     */
    void setResourcesDir(File dir);
}
