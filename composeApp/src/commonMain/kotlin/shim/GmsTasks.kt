@file:Suppress("unused", "UNUSED_PARAMETER", "PackageDirectoryMismatch")

package com.google.android.gms.tasks

import com.example.compat.PlatformApi
import com.example.compat.Void
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.reflect.KClass

fun interface OnSuccessListener<T> { fun onSuccess(result: T) }
fun interface OnFailureListener { fun onFailure(e: Exception) }
fun interface OnCompleteListener<T> { fun onComplete(task: Task<T>) }
fun interface OnCanceledListener { fun onCanceled() }

/**
 * بديل Task في خدمات Google: عملية غير متزامنة تُنفَّذ في الخلفية،
 * وتُستدعى المستمعات على الخيط الرئيسي كما في أندرويد.
 */
open class Task<T> internal constructor() {
    internal val deferred = CompletableDeferred<Result<T>>()

    val isComplete: Boolean get() = deferred.isCompleted
    val isCanceled: Boolean get() = false
    val isSuccessful: Boolean get() = deferred.isCompleted && deferred.getCompleted().isSuccess

    val result: T
        get() {
            val r = deferred.getCompleted()
            return r.getOrElse { throw RuntimeExecutionException(it) }
        }

    fun getResult(): T = result

    fun <X : Throwable> getResult(type: KClass<X>): T {
        val r = deferred.getCompleted()
        return r.getOrElse { throw it }
    }

    val exception: Exception?
        get() = if (!deferred.isCompleted) null else deferred.getCompleted().exceptionOrNull()?.let { it as? Exception ?: Exception(it) }

    internal fun complete(value: T) { deferred.complete(Result.success(value)) }
    internal fun fail(e: Throwable) { deferred.complete(Result.failure(e)) }

    private fun onDone(block: () -> Unit) {
        deferred.invokeOnCompletion { PlatformApi.runOnMain(block) }
    }

    fun addOnSuccessListener(listener: OnSuccessListener<T>): Task<T> {
        onDone { val r = deferred.getCompleted(); if (r.isSuccess) listener.onSuccess(r.getOrThrow()) }
        return this
    }

    fun addOnSuccessListener(activity: Any?, listener: OnSuccessListener<T>): Task<T> = addOnSuccessListener(listener)

    fun addOnFailureListener(listener: OnFailureListener): Task<T> {
        onDone {
            val e = deferred.getCompleted().exceptionOrNull()
            if (e != null) listener.onFailure(e as? Exception ?: Exception(e))
        }
        return this
    }

    fun addOnFailureListener(activity: Any?, listener: OnFailureListener): Task<T> = addOnFailureListener(listener)

    fun addOnCompleteListener(listener: OnCompleteListener<T>): Task<T> {
        onDone { listener.onComplete(this) }
        return this
    }

    fun addOnCompleteListener(activity: Any?, listener: OnCompleteListener<T>): Task<T> = addOnCompleteListener(listener)

    fun addOnCanceledListener(listener: OnCanceledListener): Task<T> = this

    fun <R> continueWith(block: (Task<T>) -> R): Task<R> {
        val next = Task<R>()
        deferred.invokeOnCompletion {
            try { next.complete(block(this)) } catch (e: Throwable) { next.fail(e) }
        }
        return next
    }

    fun <R> onSuccessTask(block: (T) -> Task<R>): Task<R> {
        val next = Task<R>()
        deferred.invokeOnCompletion {
            val r = deferred.getCompleted()
            if (r.isFailure) next.fail(r.exceptionOrNull()!!)
            else {
                try {
                    val t = block(r.getOrThrow())
                    t.deferred.invokeOnCompletion {
                        val rr = t.deferred.getCompleted()
                        if (rr.isSuccess) next.complete(rr.getOrThrow()) else next.fail(rr.exceptionOrNull()!!)
                    }
                } catch (e: Throwable) { next.fail(e) }
            }
        }
        return next
    }

    suspend fun awaitResult(): T = deferred.await().getOrThrow()
}

class RuntimeExecutionException(cause: Throwable) : RuntimeException(cause.message, cause)

class TaskCompletionSource<T> {
    val task: Task<T> = Task()
    fun setResult(value: T) = task.complete(value)
    fun setException(e: Exception) = task.fail(e)
    fun trySetResult(value: T): Boolean { task.complete(value); return true }
    fun trySetException(e: Exception): Boolean { task.fail(e); return true }
}

internal val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

/** يُنشئ Task ينفّذ العمل (المتزامن/المانع) في خيط خلفي */
fun <T> backgroundTask(work: () -> T): Task<T> {
    val t = Task<T>()
    backgroundScope.launch {
        try { t.complete(work()) } catch (e: Throwable) { t.fail(e) }
    }
    return t
}

object Tasks {
    suspend fun <T> await(task: Task<T>): T = task.awaitResult()

    suspend fun <T> await(task: Task<T>, timeout: Long, unit: java.util.concurrent.TimeUnit): T =
        try {
            withTimeout(unit.toMillis(timeout)) { task.awaitResult() }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            throw java.util.concurrent.TimeoutException("Timed out waiting for Task")
        }

    fun <T> forResult(value: T): Task<T> = Task<T>().also { it.complete(value) }
    fun <T> forException(e: Exception): Task<T> = Task<T>().also { it.fail(e) }

    fun whenAllComplete(vararg tasks: Task<*>): Task<List<Task<*>>> {
        val all = Task<List<Task<*>>>()
        backgroundScope.launch {
            for (t in tasks) {
                try { t.deferred.await() } catch (_: Throwable) {}
            }
            all.complete(tasks.toList())
        }
        return all
    }

    fun whenAllComplete(tasks: Collection<Task<*>>): Task<List<Task<*>>> = whenAllComplete(*tasks.toTypedArray())

    fun whenAll(vararg tasks: Task<*>): Task<Void?> {
        val all = Task<Void?>()
        backgroundScope.launch {
            try {
                for (t in tasks) t.deferred.await().getOrThrow()
                all.complete(null)
            } catch (e: Throwable) { all.fail(e) }
        }
        return all
    }

    fun whenAll(tasks: Collection<Task<*>>): Task<Void?> = whenAll(*tasks.toTypedArray())
}
