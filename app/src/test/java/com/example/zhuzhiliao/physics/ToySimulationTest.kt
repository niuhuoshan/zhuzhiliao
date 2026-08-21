package com.example.zhuzhiliao.physics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToySimulationTest {
    @Test
    fun fixedStepMatchesAtSixtyAndOneHundredTwentyFramesPerSecond() {
        val sixty = ToySimulation()
        val oneTwenty = ToySimulation()
        val input = MotionInput(Vec3(0.8f, -0.2f, 0.35f), Vec3(0f, -1f, 0f), Vec3.ZERO)
        repeat(120) { sixty.advance(input, 1.0 / 60.0) }
        repeat(240) { oneTwenty.advance(input, 1.0 / 120.0) }
        assertVecEquals(sixty.state.position, oneTwenty.state.position, 0.0001f)
        assertVecEquals(sixty.state.velocity, oneTwenty.state.velocity, 0.0001f)
    }

    @Test
    fun webRopeKeepsEndpointsAndReferenceSag() {
        val points = WebRopeShape.points(Vec3.ZERO, Vec3(0f, -1f, 0f), ropeLength = 2f, count = 5)
        assertEquals(Vec3.ZERO, points.first())
        assertEquals(Vec3(0f, -1f, 0f), points.last())
        assertEquals(-0.98125f, points[2].y, 0.0001f)
        assertTrue(points[2].y < -0.5f)
    }

    @Test
    fun stationaryInputNeverFalseCounts() {
        val simulation = ToySimulation()
        var completed = 0
        val input = MotionInput(Vec3.ZERO, Vec3(0f, -1f, 0f), Vec3.ZERO)
        repeat(60 * 10) { completed += simulation.advance(input, 1.0 / 60.0).completedWahs }
        assertEquals(0, completed)
    }

    private fun assertVecEquals(expected: Vec3, actual: Vec3, tolerance: Float) {
        assertEquals(expected.x, actual.x, tolerance)
        assertEquals(expected.y, actual.y, tolerance)
        assertEquals(expected.z, actual.z, tolerance)
    }
}
