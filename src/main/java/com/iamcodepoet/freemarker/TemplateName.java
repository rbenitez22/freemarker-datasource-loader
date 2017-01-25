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
package com.iamcodepoet.freemarker;

import com.iamcodepoet.freemarker.util.Experimental;
import java.util.Locale;

/**
 *A specification for a (localizable) template name.  
 * @author Roberto C. Benitez
 */
@Experimental(description = "Should this be more generic (e.g. LocalizableName/TemplateMetadata)?")
public interface TemplateName
{
    String getName();
    String getLocalizedName();
    Locale getLocale();
    
}
