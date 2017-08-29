package state_engine_1


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
class Transition<S: State>(val name: String,
                           val fromState: S,
                           val toState: S,
                           val preConditions: Iterable<StatefulEntity<S>.() -> Boolean>,
                           val triggeredActions: Iterable<StatefulEntity<S>.() -> Unit>
                 ) {
    fun isValid(entity: StatefulEntity<S>) : Boolean {
        return entity.state == fromState && preConditions.all { predicate -> entity.predicate() }
    }


}

/**
 * Abstract Representation of state engine in an entity
 *
 */
abstract class StatefulEntity<S: State>() : AbstractAuditableEntity() {

    lateinit var state : S

    fun listValidTransitions() : Iterable<String> {
        return  transitions.filter { transition -> transition.isValid(this) }.map { transition -> transition.name }
    }

    fun findTransitionByName(name : String) : Transition<S> {
        return transitions.first { it.name == name }
    }

    fun performTransition(name: String) {
        val  t = findTransitionByName(name)
        check(t.isValid(this), { "Transition $name cannot currently be performed, pre-conditions failed" })
        state = t.toState
        t.triggeredActions.forEach { action -> this.action() }
    }

    abstract val transitions : Iterable<Transition<S>>
}
