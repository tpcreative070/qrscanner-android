/*
 * Copyright (C) 2019 The Android Open Source Project
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
package tpcreative.co.qrscanner.helper

import androidx.appcompat.app.AppCompatDelegate
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.EnumThemeMode

object ThemeHelper {
    fun applyTheme(themeMode: EnumThemeMode) {
        when (themeMode) {
            EnumThemeMode.LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Utils.Log(ThemeHelper::class.java, "Call light")
            }
            EnumThemeMode.DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Utils.Log(ThemeHelper::class.java, "Call dark")
            }
            else -> Utils.Log("TAG","Nothing")
        }
    }
}