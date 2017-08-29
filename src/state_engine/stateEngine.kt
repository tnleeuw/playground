package state_engine


/**
 * Abstract base class standing in for the actual base class
 */
abstract class AbstractAuditableEntity {
    val id: Long = -1
}

interface State

/**
 * TODO: Indicate for a transition if it's automatic (system), or manual (user).
 */
class Transition<T: State>(val name: String,
                 val fromState: T,
                 val toState: T,
                 val preConditions: Iterable<StatefullEntity<T>.() -> Boolean>,
                 val triggeredActions: Iterable<StatefullEntity<T>.() -> Unit>
                 ) {
    fun isValid(entity: StatefullEntity<T>) : Boolean {
        return entity.state == fromState && preConditions.all { predicate -> entity.predicate() }
    }


}

/**
 * Abstract Representation of state engine in an entity
 *
 */
abstract class StatefullEntity<T: State>() : AbstractAuditableEntity() {

    lateinit var state : T

    fun listValidTransitions() : Iterable<String> {
        return  transitions.filter { transition -> transition.isValid(this) }.map { transition -> transition.name }
    }

    fun findTransitionByName(name : String) : Transition<T> {
        return transitions.first { it.name == name }
    }

    fun performTransition(name: String) {
        val  t = findTransitionByName(name)
        check(t.isValid(this), { "Transition $name cannot currently be performed, pre-conditions failed" })
        state = t.toState
        t.triggeredActions.forEach { action -> this.action() }
    }

    abstract val transitions : Iterable<Transition<T>>
}
