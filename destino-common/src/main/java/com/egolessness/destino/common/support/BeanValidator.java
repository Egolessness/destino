/*
 * Copyright (c) 2023 by Kang Wang. All rights reserved.
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

package com.egolessness.destino.common.support;

import com.egolessness.destino.common.exception.BeanInvalidException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * validator for bean
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class BeanValidator {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    public static <T> void validateWithException(final T t) throws BeanInvalidException {
        try {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<T>> constraintViolations = validator.validate(t);

            for (ConstraintViolation<T> violation : constraintViolations) {
                throw new BeanInvalidException(violation.getMessage());
            }
        } catch (Exception e) {
            throw new BeanInvalidException(e.getMessage());
        }
    }

    public static <T> boolean validate(final T t) {

        if (Objects.isNull(t)) {
            return false;
        }

        try {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<T>> constraintViolations = validator.validate(t);
            return constraintViolations.isEmpty();
        } catch (Exception e) {
            return false;
        }

    }

    public static <T> void validate(final T t, final Consumer<T> consumer) {
        if (validate(t)) {
            consumer.accept(t);
        }
    }

}
