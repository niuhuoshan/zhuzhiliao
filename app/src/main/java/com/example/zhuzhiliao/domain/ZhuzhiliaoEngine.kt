package com.example.zhuzhiliao.domain

import com.example.zhuzhiliao.physics.OrbitCounter
import com.example.zhuzhiliao.physics.Vec3
import kotlin.math.PI
import kotlin.math.abs

enum class PlayState {
    IDLE,
    AUTO
}

data class ZhuzhiliaoState(
    val playState: PlayState = PlayState.IDLE,
    val elapsedSeconds: Float = 0f,
    /** Absolute angular position in radians, wrapped to [0, 2*PI). */
    val angle: Float = 0f,
    /** Angular velocity in radians per second. */
    val angularVelocity: Float = 0f,
    val revolutions: Int = 0,
    val wahCount: Int = 0
)

/** Reserved input hook for future controller or gesture integration. */
data class MotionInput(val drive: Float = 0f)

/** Small deterministic motion model for the spatial toy. */
class ZhuzhiliaoEngine {
    companion object {
        const val TARGET_RPS = 3.0f
        const val WAH_THRESHOLD_RPS = 2.2f
        private const val ACCELERATION = 15f
        private const val BRAKING = 8f
        private const val FULL_TURN = (2.0 * PI).toFloat()
    }

    private var state = ZhuzhiliaoState()
    private var revolutionProgress = 0f
    private val wahCounter = OrbitCounter()
    private var input = MotionInput()

    fun snapshot(): ZhuzhiliaoState = state

    fun startAuto() {
        state = state.copy(playState = PlayState.AUTO)
    }

    /** Requests a graceful stop; the current spin eases down over subsequent frames. */
    fun stop() {
        state = state.copy(playState = PlayState.IDLE)
    }

    fun reset() {
        state = ZhuzhiliaoState()
        revolutionProgress = 0f
        wahCounter.reset()
        input = MotionInput()
    }

    fun updateInput(input: MotionInput) {
        this.input = input
    }

    fun advance(deltaSeconds: Float): ZhuzhiliaoState {
        val delta = deltaSeconds.coerceIn(0f, 0.05f)
        val targetVelocity = if (state.playState == PlayState.AUTO) {
            // A small future input bias lets a controller drive the same model later.
            FULL_TURN * (TARGET_RPS + input.drive.coerceIn(-0.8f, 0.8f))
        } else {
            0f
        }
        val rate = if (state.playState == PlayState.AUTO) ACCELERATION else BRAKING
        val velocity = moveToward(state.angularVelocity, targetVelocity, rate * delta)
        val signedDelta = velocity * delta
        val nextAngle = wrapAngle(state.angle + signedDelta)
        revolutionProgress += abs(signedDelta)
        val completedTurns = (revolutionProgress / FULL_TURN).toInt()
        if (completedTurns > 0) revolutionProgress %= FULL_TURN
        val speedRps = abs(velocity) / FULL_TURN
        // Use the reference project's direction-stable orbit counter. The
        // automatic mode has a fixed +Y orbit axis; future controller input can
        // provide a different axis through the migrated ToySimulation.
        val completedWahs = wahCounter.update(
            angleDelta = abs(signedDelta),
            axis = Vec3(0f, 1f, 0f),
            isQualified = speedRps >= WAH_THRESHOLD_RPS,
        )
        val nextRevolutions = state.revolutions + completedTurns
        val nextWahCount = state.wahCount + completedWahs
        state = state.copy(
            elapsedSeconds = state.elapsedSeconds + if (state.playState == PlayState.AUTO) delta else 0f,
            angle = nextAngle,
            angularVelocity = velocity,
            revolutions = nextRevolutions,
            wahCount = nextWahCount
        )
        return state
    }

    private fun moveToward(current: Float, target: Float, maxDelta: Float): Float {
        val difference = target - current
        return when {
            abs(difference) <= maxDelta -> target
            difference > 0f -> current + maxDelta
            else -> current - maxDelta
        }
    }

    private fun wrapAngle(angle: Float): Float {
        var wrapped = angle % FULL_TURN
        if (wrapped < 0f) wrapped += FULL_TURN
        return wrapped
    }
}
