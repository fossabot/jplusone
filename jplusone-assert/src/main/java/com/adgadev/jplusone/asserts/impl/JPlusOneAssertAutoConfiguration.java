/*
 * Copyright (c) 2020 Adam Gaj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adgadev.jplusone.asserts.impl;

import com.adgadev.jplusone.asserts.api.JPlusOneAssertionContext;
import com.adgadev.jplusone.core.JPlusOneAutoConfiguration;
import com.adgadev.jplusone.core.registry.RootNodeView;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@ConditionalOnBean(RootNodeView.class)
@AutoConfigureAfter(JPlusOneAutoConfiguration.class)
class JPlusOneAssertAutoConfiguration {

    private final RootNodeView rootNode;

    @Bean
    public JPlusOneAssertionContext jPlusOneAssertionContext() {
        return new JPlusOneAssertionContextImpl(rootNode);
    }
}
