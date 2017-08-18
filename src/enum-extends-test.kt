

class Transition<out T: State>(val name: String, val toState: T)

interface State {
    val name: String
    val transitions: List<Transition<*>>
}

fun <S: State> S.doTransition(transitionName: String): S {
    @SuppressWarnings("UNCHECKED_CAST")
    val trans = transitions as List<Transition<S>>
    trans.forEach { t: Transition<S> ->
        if (t.name == transitionName) {
            return t.toState
        }
    }
    throw IllegalArgumentException("No transition named $transitionName is available from ${this.name}")
}


enum class MyState(override val transitions: List<Transition<MyState>> = emptyList()) : State {
    END(),
    START(listOf(Transition("end", END)))
    ;
}

enum class MyOtherState(override val transitions: List<Transition<MyOtherState>>) : State {

    END(listOf())
    ,
    START(listOf(Transition("end", END)))
    ;
}

fun main(args: Array<String>) {
    val s1: MyState = MyState.START
    val s2: MyState = s1.doTransition("end")
    println("From $s1 to ${s2}")
}
