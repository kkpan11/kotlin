// LOOK_UP_FOR_ELEMENT_OF_TYPE: KtAnnotationEntry

@Target(AnnotationTarget.TYPE)
annotation class Anno(val s: String)

interface One
interface Two

class Foo <T>(t: T) where T : One, T : <expr>@Anno("str")</expr> Two {

}