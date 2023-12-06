import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.time.Duration
import kotlin.time.measureTime

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)


fun testSolution(input: Int, expectedOutput: Int) {
 check(input == expectedOutput) {
  println("${input} did not match ${expectedOutput}")
 }
}

fun testSolution(input: Long, expectedOutput: Long) {
 check(input == expectedOutput) {
  println("${input} did not match ${expectedOutput}")
 }
}

class Benchmark {
    var enabled = true

    val mapTimesAccumulator = mutableMapOf<String, Duration>()
    val mapTimesCount = mutableMapOf<String, Double>()

    inline fun <T> measure(name: String, crossinline block: () -> T): T {
        if (!enabled) return block()

        var result: T
        val time = measureTime {
            result = block()
        }

        val timeAcc = mapTimesAccumulator.getOrPut(name) { Duration.ZERO }
        val timeCount = mapTimesCount.getOrPut(name) { Double.MIN_VALUE }

        mapTimesAccumulator[name] = timeAcc + time
        mapTimesCount[name] = timeCount + 1

        return result
    }

    fun average(name: String): Duration {
        val timeAcc = mapTimesAccumulator.getOrDefault(name, Duration.ZERO)
        val timeCount = mapTimesCount.getOrDefault(name, Double.MIN_VALUE)

        return timeAcc / timeCount
    }

    fun printResults() {
        fun formatTiming(name: String): String {
            val d = bm.average(name)
            return "${name}: ${d.inWholeSeconds}secs / ${d.inWholeMilliseconds}millis / ${d.inWholeMicroseconds}micros / ${d.inWholeNanoseconds}nanos"
        }

        val keys = mapTimesAccumulator.keys.sorted()
        for(key in keys) {
            println(formatTiming(key))
        }
    }
}