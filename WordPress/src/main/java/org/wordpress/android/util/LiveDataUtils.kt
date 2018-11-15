package org.wordpress.android.util

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Transformations

/**
 * A helper function that merges sources into a single LiveData object
 * @param sources producing an item of the same type
 * @return merged results from all the sources
 */
fun <T> merge(vararg sources: LiveData<T>): LiveData<T> {
    val mediator = MediatorLiveData<T>()
    for (source in sources) {
        mediator.addSource(source) {
            mediator.value = it
        }
    }
    return mediator
}

/**
 * Merges two LiveData sources using a given function. The function returns an object of a new type.
 * @param sourceA first source
 * @param sourceB second source
 * @return new data source
 */
fun <T, U, V> merge(sourceA: LiveData<T>, sourceB: LiveData<U>, merger: (T, U) -> V): LiveData<V> {
    val mediator = MediatorLiveData<Pair<T?, U?>>()
    mediator.addSource(sourceA) {
        mediator.value = it to mediator.value?.second
    }
    mediator.addSource(sourceB) {
        mediator.value = mediator.value?.first to it
    }
    return mediator.map { (dataA, dataB) ->
        if (dataA != null && dataB != null) {
            merger(dataA, dataB)
        } else {
            null
        }
    }
}

/**
 * Combines all the LiveData values in the given Map into one LiveData with the map of values.
 * @param sources is a map of all the live data sources in a map by a given key
 * @return one livedata instance that combines all the values into one map
 */
fun <Key, Value> combineMap(sources: Map<Key, LiveData<Value>>): LiveData<Map<Key, Value>> {
    val mediator = MediatorLiveData<MutableMap<Key, Value>>()
    mediator.value = mutableMapOf()
    for (source in sources) {
        mediator.addSource(source.value) {
            val value = mediator.value ?: mutableMapOf()
            if (it != null) {
                value[source.key] = it
            } else {
                value.remove(source.key)
            }
            mediator.value = value
        }
    }
    return mediator.map { it.toMap() }
}

/**
 * Simple wrapper of the map utility method that is null safe
 */
fun <T, U> LiveData<T>.map(mapper: (T) -> U?): LiveData<U> {
    return Transformations.map(this) {
        it?.let(mapper)
    }
}