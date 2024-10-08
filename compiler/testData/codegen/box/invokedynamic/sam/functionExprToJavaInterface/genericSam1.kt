// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// CHECK_BYTECODE_TEXT
// 1 java/lang/invoke/LambdaMetafactory

// FILE: genericSam1.kt

fun box(): String = Sam<String, String> { "O" + it }.get("K")

// FILE: Sam.java
public interface Sam<T, R> {
    R get(T x);
}
