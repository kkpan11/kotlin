package foo

// CHECK_NOT_CALLED: component1
// CHECK_NOT_CALLED: component2

class A(val a: Int, val b: Int)

inline operator fun A.component1(): Int = a
inline operator fun A.component2(): Int = b

// CHECK_BREAKS_COUNT: function=box count=0
// CHECK_LABELS_COUNT: function=box name=$l$block count=0
fun box(): String {
    val (a, b) = A(1, 2)
    assertEquals(1, a)
    assertEquals(2, b)

    return "OK"
}