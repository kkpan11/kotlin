class A {
    var i = 23
}

fun test(a: A): String {
    var x = ++a.i
    if (x != 24) return "fail1: $x"

    a.i++
    if (a.i != 25) return "fail2: $a.i"

    // a.i++, used as expression, requires temporary variable

    return "OK"
}

fun box(): String = test(A())