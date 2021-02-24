package com.arkivanov.mvikotlin.sample.todo.coroutines.store

import com.arkivanov.mvikotlin.core.store.Executor
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.sample.todo.common.database.TodoDatabase
import com.arkivanov.mvikotlin.sample.todo.common.internal.store.list.TodoListStore.*
import com.arkivanov.mvikotlin.sample.todo.common.internal.store.list.TodoListStoreAbstractFactory
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class TodoListStoreFactory(
    storeFactory: StoreFactory,
    private val database: TodoDatabase,
    private val mainContext: CoroutineContext,
    private val ioContext: CoroutineContext
) : TodoListStoreAbstractFactory(
    storeFactory = storeFactory
) {

    override fun createExecutor(): Executor<Intent, Action, State, Result, Label> = ExecutorImpl()

    private inner class ExecutorImpl : SuspendExecutor<Intent, Action, State, Result, Label>(mainContext = mainContext) {
        override suspend fun executeAction(action: Action, getState: () -> State) {
            when (action) {
                Action.NotifyCreated -> publish(Label.MakeToast("Everything is set up (Coroutines)!"))
            }

            withContext(ioContext) { database.getAll() }
                .let(Result::Loaded)
                .also(::dispatch)
        }

        override suspend fun executeIntent(intent: Intent, getState: () -> State) {
            when (intent) {
                is Intent.Delete -> delete(intent.id)
                is Intent.ToggleDone -> toggleDone(intent.id, getState)
                is Intent.AddToState -> {
                    publish(Label.MakeToast("Create new item: ${intent.item.data.text}"))
                    dispatch(Result.Added(intent.item))
                }
                is Intent.DeleteFromState -> dispatch(Result.Deleted(intent.id))
                is Intent.UpdateInState -> dispatch(Result.Changed(intent.id, intent.data))
            }.let {}
        }

        private suspend fun delete(id: String) {
            dispatch(Result.Deleted(id))

            withContext(ioContext) {
                database.delete(id)
            }
        }

        private suspend fun toggleDone(id: String, state: () -> State) {
            dispatch(Result.DoneToggled(id))

            val item = state().items.find { it.id == id } ?: return

            withContext(ioContext) {
                database.save(id, item.data)
            }
        }
    }
}
