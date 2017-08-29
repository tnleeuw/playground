package state_engine_2


/**
 * Abstract base class standing in for the actual base class
 */
abstract class AbstractAuditableEntity {
    val id: Long = -1
}

interface State

interface Stateful<S: State> {
    var state: S
}

open class Transition<S: State>(val name: String,
                                val fromState: S,
                                val toState: S,
                                val preConditions: Iterable<Stateful<S>.() -> Boolean>,
                                val triggeredActions: Iterable<Stateful<S>.() -> Unit>
) {
    fun isValid(entity: Stateful<S>) : Boolean {
        return entity.state == fromState && preConditions.all { predicate -> entity.predicate() }
    }


}

/**
 * Abstract Representation of state engine in an entity
 *
 */
abstract class StatefulEntity<S: State>() : AbstractAuditableEntity(), Stateful<S> {

    override lateinit var state: S

}


class StateEngine<S: State, in E: Stateful<S>>(
        val initialState: S,
        val transitions: Iterable<Transition<S>>
) {

    fun listValidTransitions(stateful: E) : Iterable<String> {
        return  transitions.filter { transition -> transition.isValid(stateful) }.map { transition -> transition.name }
    }

    fun findTransitionByName(name : String) : Transition<S> {
        return transitions.first { it.name == name }
    }

    fun performTransition(statefull: E, name: String) {
        val  t = findTransitionByName(name)
        check(t.isValid(statefull), { "Transition $name cannot currently be performed, pre-conditions failed" })
        statefull.state = t.toState
        t.triggeredActions.forEach { action -> statefull.action() }
    }

}
