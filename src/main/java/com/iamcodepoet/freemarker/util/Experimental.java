/*
 * Copyright 2017 Roberto C. Benitez.
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
package com.iamcodepoet.freemarker.util;

import java.lang.annotation.Documented;

/**
 *An annotation (in itself experimental) to annotate experimental code.
 * The idea is to hint at an artifact that should not make it to a release candidate without further though/consideration.
 * @author Roberto C. Benitez
 */

@Documented
public @interface Experimental
{
    String description() default "";
}
