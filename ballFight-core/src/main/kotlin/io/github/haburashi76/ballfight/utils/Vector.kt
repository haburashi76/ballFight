package io.github.haburashi76.ballfight.utils

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector(private val v0: Double, private val v1: Double) {
    constructor(direction: Double) : this(cos(direction), sin(direction))

    var x = v0
        private set
    var y = v1
        private set

    val length: Double
        get() { return sqrt(x * x + y * y) }

    fun setLength(length: Double) {
        normalize().multiply(length)
    }

    fun normalize(): Vector {
        return if(this.length != 0.0) divide(this.length) else this
    }

    fun clone(): Vector {
        return Vector(x, y)
    }

    fun add(x: Double, y: Double): Vector {
        this.x += x
        this.y += y
        return this
    }

    fun add(vector: Vector): Vector {
        add(vector.x, vector.y)
        return this
    }

    fun multiply(cof: Double): Vector {
        this.x *= cof
        this.y *= cof
        return this
    }

    fun multiply(vector: Vector): Vector {
        this.x *= vector.x
        this.y *= vector.y
        return this
    }

    fun divide(divisor: Double): Vector {
        return multiply(1/divisor)
    }

    fun distance(vector: Vector): Double {
        return abs((this - vector).length)
    }

    fun dot(other: Vector): Double {
        return x * other.x + y * other.y
    }

    fun angle(vector: Vector): Double {
        return acos(dot(vector) / (length * vector.length))
    }


    fun rotate(angle: Double): Vector {
        val cos = cos(angle)
        val sin = sin(angle)

        val newX = x * cos - y * sin
        val newY = x * sin + y * cos

        x = newX
        y = newY

        return this
    }

}

fun Vector.toAngle(): Double {
    return Math.toDegrees(
        kotlin.math.atan2(-y, x)
    )
}

operator fun Vector.plus(vector: Vector): Vector {
    val clone = this.clone().add(vector)
    return clone
}
operator fun Vector.times(v0: Number): Vector {
    val clone = this.clone().multiply(v0.toDouble())
    return clone
}
operator fun Vector.times(v0: Vector): Vector {
    val clone = this.clone().multiply(v0)
    return clone
}
operator fun Vector.minus(vector: Vector): Vector {
    return this + vector * -1.0
}
operator fun Vector.div(v0: Number): Vector {
    return this * (1.0/v0.toDouble())
}
