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

package com.adgadev.jplusone.test.domain.commerce.jpa;

import com.adgadev.jplusone.test.domain.commerce.crud.EntityManagerCrudService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class JpaDialectTest {

    @Autowired
    private EntityManagerCrudService crudService;

    @Test
    void shouldUseHibernateJpaDialect() {
        // given
        crudService.addManufacturer(100L, "Name1");

        // when
        assertThrows(DataIntegrityViolationException.class, () ->  // default JPA dialect throws JpaSystemException
            crudService.addManufacturer(100L, "Name2")
        );
    }

}