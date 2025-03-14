package a

class Outer {
    inner class B(x: String)

    @Suppress("TOPLEVEL_TYPEALIASES_ONLY", "UNSUPPORTED_FEATURE")
    typealias A1 = B

    @Suppress("TOPLEVEL_TYPEALIASES_ONLY", "UNSUPPORTED_FEATURE")
    private typealias A2 = B

    fun A1(x: Any) = x
    fun A2(x: Any) = x
}
