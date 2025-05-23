// WITH_STDLIB

open class BaseWrapper<T>(val response: T)
class Wrapper(result: Result<String>) : BaseWrapper<Result<String>>(result)

fun box(): String {
    return Wrapper(Result.success("OK")).response.getOrThrow()
}
