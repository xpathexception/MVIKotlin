package com.arkivanov.mvikotlin.sample.todo.common.internal.store.list

import com.arkivanov.mvikotlin.core.store.*
import com.arkivanov.mvikotlin.core.utils.JvmSerializable
import com.arkivanov.mvikotlin.sample.todo.common.database.TodoItem
import com.arkivanov.mvikotlin.sample.todo.common.database.update
import com.arkivanov.mvikotlin.sample.todo.common.internal.store.list.TodoListStore.*

/**
 * Abstract factories are normally not needed. Just create a normal factory as described [here][Store].
 * In MVIKotlin samples abstract factories are used because each `Store` has two implementations: using Reaktive and coroutines.
 */
abstract class TodoListStoreAbstractFactory(
    private val storeFactory: StoreFactory
) {

    fun create(): TodoListStore =
        object : TodoListStore, Store<Intent, State, Label> by storeFactory.create(
            name = "ListStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Action.NotifyCreated),
            executorFactory = ::createExecutor,
            reducer = ReducerImpl
        ) {
        }

    protected sealed class Result : JvmSerializable {
        data class Loaded(val items: List<TodoItem>) : Result()
        data class Deleted(val id: String) : Result()
        data class DoneToggled(val id: String) : Result()
        data class Added(val item: TodoItem) : Result()
        data class Changed(val id: String, val data: TodoItem.Data) : Result()
    }

    protected abstract fun createExecutor(): Executor<Intent, Action, State, Result, Label>

    private object ReducerImpl : Reducer<State, Result> {
        override fun State.reduce(result: Result): State =
            when (result) {
                is Result.Loaded -> copy(items = result.items)
                is Result.Deleted -> copy(items = items.filterNot { it.id == result.id })
                is Result.DoneToggled -> copy(items = items.update(result.id) { copy(isDone = !isDone) })
                is Result.Added -> copy(items = items + result.item)
                is Result.Changed -> copy(items = items.update(result.id) { result.data })
            }
    }

    protected sealed class Action {
        object NotifyCreated : Action()
    }
}
