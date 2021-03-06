package com.leewilson.moviebee.ui.main.newpost

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.leewilson.moviebee.repository.main.CreateMovieNightRepository
import com.leewilson.moviebee.ui.main.newpost.state.CreateMovieNightStateEvent
import com.leewilson.moviebee.ui.main.newpost.state.CreateMovieNightViewState
import com.leewilson.moviebee.util.DataState
import kotlinx.coroutines.Dispatchers

class CreateMovieNightViewModel @ViewModelInject constructor(
    private val repository: CreateMovieNightRepository
) : ViewModel() {

    private val _stateEvent = MutableLiveData<CreateMovieNightStateEvent>()

    val dataState: LiveData<DataState<CreateMovieNightViewState>>
            = Transformations.switchMap(_stateEvent) { event ->
        handleStateEvent(event)
    }

    private fun handleStateEvent(
        event: CreateMovieNightStateEvent
    ) : LiveData<DataState<CreateMovieNightViewState>> {
        when (event) {
            is CreateMovieNightStateEvent.LoadMovie -> {
                return liveData(Dispatchers.IO) {
                    emit(DataState.loading(true))
                    val result = repository.getMovieDetailById(event.id)
                    emit(result)
                }
            }
            is CreateMovieNightStateEvent.SaveMovieNight -> {
                return liveData(Dispatchers.IO) {
                    emit(DataState.loading(true))
                    val resultState = repository.saveMovieNight(event.movieNight)
                    emit(resultState)
                }
            }
        }
    }

    fun setStateEvent(stateEvent: CreateMovieNightStateEvent) {
        _stateEvent.value = stateEvent
    }
}