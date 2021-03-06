/*
 * Copyright 2018 Google Inc. All Rights Reserved.
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

package com.google.container.tools.init

import com.google.container.tools.core.analytics.UsageTrackerManagerService
import com.google.container.tools.test.ContainerToolsRule
import com.google.container.tools.test.TestService
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.plugins.PluginNode
import com.intellij.openapi.extensions.PluginId
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [PluginInitComponent].
 */
class PluginInitComponentTest {
    @get:Rule
    val containerToolsRule = ContainerToolsRule(this)

    @MockK
    @TestService
    private lateinit var usageTrackerManagerService: UsageTrackerManagerService

    private lateinit var pluginInitComponent: PluginInitComponent

    @Before
    fun setUp() {
        pluginInitComponent = PluginInitComponent()
    }

    @Test
    fun `feature tracking is set in when all conditions are met`() {
        every { usageTrackerManagerService.isUsageTrackingAvailable() } answers { true }
        every { usageTrackerManagerService.hasUserRecordedTrackingPreference() } answers { false }

        pluginInitComponent.initComponent()

        verify { usageTrackerManagerService.setTrackingOptedIn(true) }
    }

    @Test
    fun `feature tracking is not set when the gct plugin is installed`() {
        PluginManagerCore.setPlugins(arrayOf(PluginNode(PluginId.getId("com.google.gct.core"))))
        every { usageTrackerManagerService.isUsageTrackingAvailable() } answers { true }
        every { usageTrackerManagerService.hasUserRecordedTrackingPreference() } answers { false }

        pluginInitComponent.initComponent()

        verify(exactly = 0) { usageTrackerManagerService.setTrackingOptedIn(any()) }
    }

    @Test
    fun `feature tracking is not set when the feature tracking is not available`() {
        every { usageTrackerManagerService.isUsageTrackingAvailable() } answers { false }
        every { usageTrackerManagerService.hasUserRecordedTrackingPreference() } answers { false }

        pluginInitComponent.initComponent()

        verify(exactly = 0) { usageTrackerManagerService.setTrackingOptedIn(any()) }
    }

    @Test
    fun `feature tracking is not set when the feature tracking preference has already been set`() {
        every { usageTrackerManagerService.isUsageTrackingAvailable() } answers { true }
        every { usageTrackerManagerService.hasUserRecordedTrackingPreference() } answers { true }

        pluginInitComponent.initComponent()

        verify(exactly = 0) { usageTrackerManagerService.setTrackingOptedIn(any()) }
    }
}
