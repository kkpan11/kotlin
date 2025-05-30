// FIR_IDENTICAL
// JSPECIFY_STATE: strict
// DIAGNOSTICS: -UNUSED_PARAMETER

// FILE: NullUnmarkedType.java

import org.jspecify.annotations.*;

@NullUnmarked
public interface NullUnmarkedType {

    @NullMarked
    public interface NullMarkedType {
        public String unannotatedProduce();
        public void unannotatedConsume(String arg);
    }

    public interface UnannotatedType {
        @NullMarked
        public String nullMarkedProduce();
        @NullMarked
        public void nullMarkedConsume(String arg);
    }

}

// FILE: NullUnmarkedTypeWithNullMarkedConstructor.java

import org.jspecify.annotations.*;

@NullUnmarked
public class NullUnmarkedTypeWithNullMarkedConstructor {
    @NullMarked
    public NullUnmarkedTypeWithNullMarkedConstructor(String arg) {}
}

// FILE: kotlin.kt

interface TestA: NullUnmarkedType.NullMarkedType {
    override fun unannotatedProduce(): <!RETURN_TYPE_MISMATCH_ON_OVERRIDE!>String?<!>
}

interface TestB: NullUnmarkedType.UnannotatedType {
    override fun nullMarkedProduce(): <!RETURN_TYPE_MISMATCH_ON_OVERRIDE!>String?<!>
}

fun test(
    a: NullUnmarkedType.NullMarkedType,
    b: NullUnmarkedType.UnannotatedType
) {
    a.unannotatedConsume(<!NULL_FOR_NONNULL_TYPE!>null<!>)
    b.nullMarkedConsume(<!NULL_FOR_NONNULL_TYPE!>null<!>)
    NullUnmarkedTypeWithNullMarkedConstructor(<!NULL_FOR_NONNULL_TYPE!>null<!>)
}
