package com.asteam.appcollection.test

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runtime smoke test for the consolidated 78-in-1 QA application.
 *
 * This test intentionally opens every rebuilt MainActivity once on a real Android runtime.
 * A successful compilation alone cannot detect crashes caused during Activity.onCreate(),
 * resource inflation, manifest resolution, or first-frame initialization. Launching each
 * Activity through ActivityScenario covers those failure classes while keeping the test fast.
 *
 * The feature list uses direct class references instead of reflection. If a future source edit
 * removes or renames one of these Activities, the androidTest source itself stops compiling and
 * CI fails before an incomplete APK can be published.
 */
@RunWith(AndroidJUnit4::class)
class AllActivitiesSmokeTest {

    /**
     * One direct reference for each numbered project in repository order.
     * Keeping this list explicit also makes omissions visible during code review.
     */
    private val activities: List<Class<out Activity>> = listOf(
        com.asteam.appcollection.p01.MainActivity::class.java,
        com.asteam.appcollection.p02.MainActivity::class.java,
        com.asteam.appcollection.p03.MainActivity::class.java,
        com.asteam.appcollection.p04.MainActivity::class.java,
        com.asteam.appcollection.p05.MainActivity::class.java,
        com.asteam.appcollection.p06.MainActivity::class.java,
        com.asteam.appcollection.p07.MainActivity::class.java,
        com.asteam.appcollection.p08.MainActivity::class.java,
        com.asteam.appcollection.p09.MainActivity::class.java,
        com.asteam.appcollection.p10.MainActivity::class.java,
        com.asteam.appcollection.p11.MainActivity::class.java,
        com.asteam.appcollection.p12.MainActivity::class.java,
        com.asteam.appcollection.p13.MainActivity::class.java,
        com.asteam.appcollection.p14.MainActivity::class.java,
        com.asteam.appcollection.p15.MainActivity::class.java,
        com.asteam.appcollection.p16.MainActivity::class.java,
        com.asteam.appcollection.p17.MainActivity::class.java,
        com.asteam.appcollection.p18.MainActivity::class.java,
        com.asteam.appcollection.p19.MainActivity::class.java,
        com.asteam.appcollection.p20.MainActivity::class.java,
        com.asteam.appcollection.p21.MainActivity::class.java,
        com.asteam.appcollection.p22.MainActivity::class.java,
        com.asteam.appcollection.p23.MainActivity::class.java,
        com.asteam.appcollection.p24.MainActivity::class.java,
        com.asteam.appcollection.p25.MainActivity::class.java,
        com.asteam.appcollection.p26.MainActivity::class.java,
        com.asteam.appcollection.p27.MainActivity::class.java,
        com.asteam.appcollection.p28.MainActivity::class.java,
        com.asteam.appcollection.p29.MainActivity::class.java,
        com.asteam.appcollection.p30.MainActivity::class.java,
        com.asteam.appcollection.p31.MainActivity::class.java,
        com.asteam.appcollection.p32.MainActivity::class.java,
        com.asteam.appcollection.p33.MainActivity::class.java,
        com.asteam.appcollection.p34.MainActivity::class.java,
        com.asteam.appcollection.p35.MainActivity::class.java,
        com.asteam.appcollection.p36.MainActivity::class.java,
        com.asteam.appcollection.p37.MainActivity::class.java,
        com.asteam.appcollection.p38.MainActivity::class.java,
        com.asteam.appcollection.p39.MainActivity::class.java,
        com.asteam.appcollection.p40.MainActivity::class.java,
        com.asteam.appcollection.p41.MainActivity::class.java,
        com.asteam.appcollection.p42.MainActivity::class.java,
        com.asteam.appcollection.p43.MainActivity::class.java,
        com.asteam.appcollection.p44.MainActivity::class.java,
        com.asteam.appcollection.p45.MainActivity::class.java,
        com.asteam.appcollection.p46.MainActivity::class.java,
        com.asteam.appcollection.p47.MainActivity::class.java,
        com.asteam.appcollection.p48.MainActivity::class.java,
        com.asteam.appcollection.p49.MainActivity::class.java,
        com.asteam.appcollection.p50.MainActivity::class.java,
        com.asteam.appcollection.p51.MainActivity::class.java,
        com.asteam.appcollection.p52.MainActivity::class.java,
        com.asteam.appcollection.p53.MainActivity::class.java,
        com.asteam.appcollection.p54.MainActivity::class.java,
        com.asteam.appcollection.p55.MainActivity::class.java,
        com.asteam.appcollection.p56.MainActivity::class.java,
        com.asteam.appcollection.p57.MainActivity::class.java,
        com.asteam.appcollection.p58.MainActivity::class.java,
        com.asteam.appcollection.p59.MainActivity::class.java,
        com.asteam.appcollection.p60.MainActivity::class.java,
        com.asteam.appcollection.p61.MainActivity::class.java,
        com.asteam.appcollection.p62.MainActivity::class.java,
        com.asteam.appcollection.p63.MainActivity::class.java,
        com.asteam.appcollection.p64.MainActivity::class.java,
        com.asteam.appcollection.p65.MainActivity::class.java,
        com.asteam.appcollection.p66.MainActivity::class.java,
        com.asteam.appcollection.p67.MainActivity::class.java,
        com.asteam.appcollection.p68.MainActivity::class.java,
        com.asteam.appcollection.p69.MainActivity::class.java,
        com.asteam.appcollection.p70.MainActivity::class.java,
        com.asteam.appcollection.p71.MainActivity::class.java,
        com.asteam.appcollection.p72.MainActivity::class.java,
        com.asteam.appcollection.p73.MainActivity::class.java,
        com.asteam.appcollection.p74.MainActivity::class.java,
        com.asteam.appcollection.p75.MainActivity::class.java,
        com.asteam.appcollection.p76.MainActivity::class.java,
        com.asteam.appcollection.p77.MainActivity::class.java,
        com.asteam.appcollection.p78.MainActivity::class.java
    )

    /**
     * Starts every Activity, waits until Android reports RESUMED, then closes it cleanly.
     * ActivityScenario propagates launch/runtime failures to JUnit, so one broken sample marks
     * the CI job as failed instead of silently showing a generic in-app error message.
     */
    @Test
    fun launchAllRebuiltActivitiesWithoutCrashing() {
        // Protect the contract that this repository contains exactly 78 rebuilt examples.
        assertTrue("Smoke-test list must contain all 78 activities", activities.size == 78)

        activities.forEachIndexed { index, activityClass ->
            ActivityScenario.launch(activityClass).use { scenario ->
                // Reaching RESUMED proves the Activity completed its initial Android lifecycle.
                scenario.onActivity { activity ->
                    assertTrue(
                        "Activity ${index + 1} did not reach a usable state: ${activityClass.name}",
                        !activity.isFinishing
                    )
                }
            }
        }
    }
}
