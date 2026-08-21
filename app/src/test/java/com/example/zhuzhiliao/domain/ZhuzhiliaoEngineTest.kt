package com.example.zhuzhiliao.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZhuzhiliaoEngineTest {
    @Test
    fun autoModeAcceleratesAndCountsQualifiedTurns() {
        val engine = ZhuzhiliaoEngine()
        engine.startAuto()
        repeat(240) { engine.advance(1f / 120f) }

        val state = engine.snapshot()
        assertEquals(PlayState.AUTO, state.playState)
        assertTrue(state.angularVelocity > 0f)
        assertTrue(state.revolutions >= 1)
        assertTrue(state.wahCount > 0)
        assertTrue(state.wahCount <= state.revolutions)
    }

    @Test
    fun stopBrakesWithoutChangingTheAccumulatedScore() {
        val engine = ZhuzhiliaoEngine()
        engine.startAuto()
        repeat(180) { engine.advance(1f / 120f) }
        val beforeStop = engine.snapshot()
        engine.stop()
        engine.advance(1f / 60f)
        val braking = engine.snapshot()

        assertEquals(PlayState.IDLE, braking.playState)
        assertTrue(braking.angularVelocity < beforeStop.angularVelocity)
        assertEquals(beforeStop.revolutions, braking.revolutions)
        assertEquals(beforeStop.wahCount, braking.wahCount)
    }

    @Test
    fun resetRestoresTheInitialState() {
        val engine = ZhuzhiliaoEngine()
        engine.startAuto()
        repeat(120) { engine.advance(1f / 60f) }
        engine.reset()

        assertEquals(ZhuzhiliaoState(), engine.snapshot())
    }
}
