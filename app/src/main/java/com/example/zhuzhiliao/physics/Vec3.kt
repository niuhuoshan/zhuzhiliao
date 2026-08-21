package com.example.zhuzhiliao.physics

import kotlin.math.sqrt

/** Small value type used by the migrated motion/rope algorithms. */
data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun unaryMinus() = Vec3(-x, -y, -z)
    operator fun times(value: Float) = Vec3(x * value, y * value, z * value)
    operator fun div(value: Float) = Vec3(x / value, y / value, z / value)
    val lengthSquared: Float get() = dot(this)
    val length: Float get() = sqrt(lengthSquared)
    fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z
    fun cross(other: Vec3) = Vec3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x,
    )
    fun normalized(fallback: Vec3 = ZERO): Vec3 {
        val magnitude = length
        return if (magnitude > EPSILON) this / magnitude else fallback
    }
    fun distance(to: Vec3): Float = (this - to).length

    companion object {
        const val EPSILON = 0.000001f
        val ZERO = Vec3(0f, 0f, 0f)
        val UP = Vec3(0f, 1f, 0f)
    }
}

operator fun Float.times(vector: Vec3): Vec3 = vector * this
