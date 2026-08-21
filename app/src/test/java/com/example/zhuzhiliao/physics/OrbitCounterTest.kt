package com.example.zhuzhiliao.physics

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class OrbitCounterTest {
    @Test
    fun continuousQualifiedOrbitCompletesOneWah() {
        val counter = OrbitCounter()
        var completed = 0
        repeat(4) { completed += counter.update((PI / 2).toFloat(), Vec3(0f, 0f, 1f), true) }
        assertEquals(1, completed)
    }

    @Test
    fun directionReversalBreaksOrbitContinuity() {
        val counter = OrbitCounter()
        var completed = 0
        repeat(2) { completed += counter.update((PI / 2).toFloat(), Vec3(0f, 0f, 1f), true) }
        repeat(2) { completed += counter.update((PI / 2).toFloat(), Vec3(0f, 0f, -1f), true) }
        assertEquals(0, completed)
    }

    @Test
    fun unqualifiedJitterResetsPartialOrbit() {
        val counter = OrbitCounter()
        counter.update(PI.toFloat(), Vec3(0f, 0f, 1f), true)
        counter.update(0f, Vec3(0f, 0f, 1f), false)
        assertEquals(0, counter.update(PI.toFloat(), Vec3(0f, 0f, 1f), true))
    }
}
