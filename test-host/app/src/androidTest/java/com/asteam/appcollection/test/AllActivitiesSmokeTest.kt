package com.asteam.appcollection.test

import android.app.Activity
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runtime smoke test for the consolidated 78-in-1 QA application.
 *
 * This test intentionally opens every rebuilt MainActivity once on an Android runtime. Successful
 * compilation alone cannot detect failures thrown during Activity.onCreate(), resource inflation,
 * manifest resolution or first-frame lifecycle work. ActivityScenario exercises those paths while
 * launching the same classes that the consolidated test host exposes to the user.
 *
 * Direct class references are intentional: if a future edit removes or renames any numbered
 * MainActivity, androidTest compilation itself fails instead of quietly skipping that application.
 *
 * The whole sweep has a bounded JUnit timeout. A runtime/environment regression therefore cannot
 * occupy a CI runner indefinitely. Before and after every Activity launch we write a stable logcat
 * marker containing its number and class; if the timeout ever fires, the final START marker reveals
 * exactly which numbered sample was being exercised.
 */
@RunWith(AndroidJUnit4::class)
class AllActivitiesSmokeTest {

    /** Structured metadata keeps the human number beside the direct Activity class reference. */
    private data class ActivityUnderTest(
        val number: Int,
        val activityClass: Class<out Activity>
    )

    /**
     * Complete numbered set of rebuilt applications. This explicit list makes omissions visible in
     * code review and keeps the test independent from reflection/package scanning.
     */
    private val activities = listOf(
        ActivityUnderTest(1, com.asteam.appcollection.p01.MainActivity::class.java),
        ActivityUnderTest(2, com.asteam.appcollection.p02.MainActivity::class.java),
        ActivityUnderTest(3, com.asteam.appcollection.p03.MainActivity::class.java),
        ActivityUnderTest(4, com.asteam.appcollection.p04.MainActivity::class.java),
        ActivityUnderTest(5, com.asteam.appcollection.p05.MainActivity::class.java),
        ActivityUnderTest(6, com.asteam.appcollection.p06.MainActivity::class.java),
        ActivityUnderTest(7, com.asteam.appcollection.p07.MainActivity::class.java),
        ActivityUnderTest(8, com.asteam.appcollection.p08.MainActivity::class.java),
        ActivityUnderTest(9, com.asteam.appcollection.p09.MainActivity::class.java),
        ActivityUnderTest(10, com.asteam.appcollection.p10.MainActivity::class.java),
        ActivityUnderTest(11, com.asteam.appcollection.p11.MainActivity::class.java),
        ActivityUnderTest(12, com.asteam.appcollection.p12.MainActivity::class.java),
        ActivityUnderTest(13, com.asteam.appcollection.p13.MainActivity::class.java),
        ActivityUnderTest(14, com.asteam.appcollection.p14.MainActivity::class.java),
        ActivityUnderTest(15, com.asteam.appcollection.p15.MainActivity::class.java),
        ActivityUnderTest(16, com.asteam.appcollection.p16.MainActivity::class.java),
        ActivityUnderTest(17, com.asteam.appcollection.p17.MainActivity::class.java),
        ActivityUnderTest(18, com.asteam.appcollection.p18.MainActivity::class.java),
        ActivityUnderTest(19, com.asteam.appcollection.p19.MainActivity::class.java),
        ActivityUnderTest(20, com.asteam.appcollection.p20.MainActivity::class.java),
        ActivityUnderTest(21, com.asteam.appcollection.p21.MainActivity::class.java),
        ActivityUnderTest(22, com.asteam.appcollection.p22.MainActivity::class.java),
        ActivityUnderTest(23, com.asteam.appcollection.p23.MainActivity::class.java),
        ActivityUnderTest(24, com.asteam.appcollection.p24.MainActivity::class.java),
        ActivityUnderTest(25, com.asteam.appcollection.p25.MainActivity::class.java),
        ActivityUnderTest(26, com.asteam.appcollection.p26.MainActivity::class.java),
        ActivityUnderTest(27, com.asteam.appcollection.p27.MainActivity::class.java),
        ActivityUnderTest(28, com.asteam.appcollection.p28.MainActivity::class.java),
        ActivityUnderTest(29, com.asteam.appcollection.p29.MainActivity::class.java),
        ActivityUnderTest(30, com.asteam.appcollection.p30.MainActivity::class.java),
        ActivityUnderTest(31, com.asteam.appcollection.p31.MainActivity::class.java),
        ActivityUnderTest(32, com.asteam.appcollection.p32.MainActivity::class.java),
        ActivityUnderTest(33, com.asteam.appcollection.p33.MainActivity::class.java),
        ActivityUnderTest(34, com.asteam.appcollection.p34.MainActivity::class.java),
        ActivityUnderTest(35, com.asteam.appcollection.p35.MainActivity::class.java),
        ActivityUnderTest(36, com.asteam.appcollection.p36.MainActivity::class.java),
        ActivityUnderTest(37, com.asteam.appcollection.p37.MainActivity::class.java),
        ActivityUnderTest(38, com.asteam.appcollection.p38.MainActivity::class.java),
        ActivityUnderTest(39, com.asteam.appcollection.p39.MainActivity::class.java),
        ActivityUnderTest(40, com.asteam.appcollection.p40.MainActivity::class.java),
        ActivityUnderTest(41, com.asteam.appcollection.p41.MainActivity::class.java),
        ActivityUnderTest(42, com.asteam.appcollection.p42.MainActivity::class.java),
        ActivityUnderTest(43, com.asteam.appcollection.p43.MainActivity::class.java),
        ActivityUnderTest(44, com.asteam.appcollection.p44.MainActivity::class.java),
        ActivityUnderTest(45, com.asteam.appcollection.p45.MainActivity::class.java),
        ActivityUnderTest(46, com.asteam.appcollection.p46.MainActivity::class.java),
        ActivityUnderTest(47, com.asteam.appcollection.p47.MainActivity::class.java),
        ActivityUnderTest(48, com.asteam.appcollection.p48.MainActivity::class.java),
        ActivityUnderTest(49, com.asteam.appcollection.p49.MainActivity::class.java),
        ActivityUnderTest(50, com.asteam.appcollection.p50.MainActivity::class.java),
        ActivityUnderTest(51, com.asteam.appcollection.p51.MainActivity::class.java),
        ActivityUnderTest(52, com.asteam.appcollection.p52.MainActivity::class.java),
        ActivityUnderTest(53, com.asteam.appcollection.p53.MainActivity::class.java),
        ActivityUnderTest(54, com.asteam.appcollection.p54.MainActivity::class.java),
        ActivityUnderTest(55, com.asteam.appcollection.p55.MainActivity::class.java),
        ActivityUnderTest(56, com.asteam.appcollection.p56.MainActivity::class.java),
        ActivityUnderTest(57, com.asteam.appcollection.p57.MainActivity::class.java),
        ActivityUnderTest(58, com.asteam.appcollection.p58.MainActivity::class.java),
        ActivityUnderTest(59, com.asteam.appcollection.p59.MainActivity::class.java),
        ActivityUnderTest(60, com.asteam.appcollection.p60.MainActivity::class.java),
        ActivityUnderTest(61, com.asteam.appcollection.p61.MainActivity::class.java),
        ActivityUnderTest(62, com.asteam.appcollection.p62.MainActivity::class.java),
        ActivityUnderTest(63, com.asteam.appcollection.p63.MainActivity::class.java),
        ActivityUnderTest(64, com.asteam.appcollection.p64.MainActivity::class.java),
        ActivityUnderTest(65, com.asteam.appcollection.p65.MainActivity::class.java),
        ActivityUnderTest(66, com.asteam.appcollection.p66.MainActivity::class.java),
        ActivityUnderTest(67, com.asteam.appcollection.p67.MainActivity::class.java),
        ActivityUnderTest(68, com.asteam.appcollection.p68.MainActivity::class.java),
        ActivityUnderTest(69, com.asteam.appcollection.p69.MainActivity::class.java),
        ActivityUnderTest(70, com.asteam.appcollection.p70.MainActivity::class.java),
        ActivityUnderTest(71, com.asteam.appcollection.p71.MainActivity::class.java),
        ActivityUnderTest(72, com.asteam.appcollection.p72.MainActivity::class.java),
        ActivityUnderTest(73, com.asteam.appcollection.p73.MainActivity::class.java),
        ActivityUnderTest(74, com.asteam.appcollection.p74.MainActivity::class.java),
        ActivityUnderTest(75, com.asteam.appcollection.p75.MainActivity::class.java),
        ActivityUnderTest(76, com.asteam.appcollection.p76.MainActivity::class.java),
        ActivityUnderTest(77, com.asteam.appcollection.p77.MainActivity::class.java),
        ActivityUnderTest(78, com.asteam.appcollection.p78.MainActivity::class.java)
    )

    /**
     * Launches all 78 Activities and requires each one to reach a usable resumed state.
     *
     * 180 seconds is intentionally far above normal execution time but far below the workflow's
     * overall timeout. If Android/device infrastructure or one Activity stalls, JUnit terminates
     * this test and the START markers in logcat identify the last attempted numbered application.
     */
    @Test(timeout = 180_000L)
    fun launchAllRebuiltActivitiesWithoutCrashing() {
        // Protect the repository contract before touching the device lifecycle.
        assertTrue("Smoke-test list must contain all 78 activities", activities.size == 78)
        assertTrue(
            "Smoke-test numbering must remain exactly 1..78",
            activities.map { it.number } == (1..78).toList()
        )

        activities.forEach { item ->
            // This marker remains in logcat even if launch/resume later throws or times out.
            Log.i(TAG, "START p%02d %s".format(item.number, item.activityClass.name))

            ActivityScenario.launch(item.activityClass).use { scenario ->
                // onActivity executes only after ActivityScenario has a live Activity instance.
                scenario.onActivity { activity ->
                    assertTrue(
                        "Activity ${item.number} entered finishing state: ${item.activityClass.name}",
                        !activity.isFinishing
                    )
                }
            }

            // Reaching DONE proves launch, initial lifecycle callback and clean Scenario close.
            Log.i(TAG, "DONE p%02d %s".format(item.number, item.activityClass.name))
        }
    }

    /** Stable logcat tag used by CI diagnostics and future runtime troubleshooting. */
    private companion object {
        const val TAG = "AppCollectionSmoke"
    }
}
