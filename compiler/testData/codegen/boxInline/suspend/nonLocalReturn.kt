// WITH_STDLIB
// WITH_COROUTINES
// NO_CHECK_LAMBDA_INLINING
// FILE: a.kt

inline suspend fun runReturning(lambda: suspend () -> Unit): Unit =
    lambda()

inline suspend fun myRun(lambda: suspend () -> String): String {
    runReturning { return lambda() }
    return "fail: did not return from lambda"
}

// FILE: b.kt
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun box(): String {
    var result = "fail"
    suspend { result = myRun { "OK" } }.startCoroutine(EmptyContinuation)
    return result
}
