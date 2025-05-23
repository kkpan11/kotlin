// RUN_PIPELINE_TILL: FRONTEND
// ISSUE: KT-55179
// SKIP_TXT
// RENDER_DIAGNOSTICS_FULL_TEXT

private class Foo {
    companion object {
        fun buildFoo() = Foo()

        object Nested {
            fun bar() {}
        }
    }
}

internal <!NOTHING_TO_INLINE!>inline<!> fun foo() {
    <!LESS_VISIBLE_CONTAINING_CLASS_IN_INLINE_WARNING, LESS_VISIBLE_TYPE_IN_INLINE_ACCESSED_SIGNATURE_WARNING, PRIVATE_CLASS_MEMBER_FROM_INLINE!>Foo<!>()
    Foo.<!PRIVATE_CLASS_MEMBER_FROM_INLINE!>Companion<!>
    Foo.<!LESS_VISIBLE_CONTAINING_CLASS_IN_INLINE_WARNING, LESS_VISIBLE_TYPE_IN_INLINE_ACCESSED_SIGNATURE_WARNING, PRIVATE_CLASS_MEMBER_FROM_INLINE!>buildFoo<!>()
    Foo.Companion.Nested.<!LESS_VISIBLE_CONTAINING_CLASS_IN_INLINE_WARNING!>bar<!>()
}
