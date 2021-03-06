package com.leewilson.moviebee.ui.main.newpost

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.leewilson.moviebee.model.Movie
import com.leewilson.moviebee.repository.main.SearchMovieRepository
import com.leewilson.moviebee.ui.main.newpost.state.SearchStateEvent
import com.leewilson.moviebee.ui.main.newpost.state.SearchViewState
import com.leewilson.moviebee.ui.main.newpost.state.SearchViewState.*

class SearchMovieViewModel @ViewModelInject constructor(
    private val repository: SearchMovieRepository
): ViewModel() {

    private val TAG = "AppDebug"

    private val _stateEvent: MutableLiveData<SearchStateEvent> = MutableLiveData()
    private var currentSearchQuery: String? = null
    private val results: MutableList<Movie> = ArrayList()
    var totalResults = 0
    var resultsLoaded = 0
    var page = 1

    val dataState: LiveData<SearchViewState> = Transformations.switchMap(_stateEvent) { stateEvent ->
        handleStateEvent(stateEvent)
    }

    private fun handleStateEvent(
        stateEvent: SearchStateEvent
    ): LiveData<SearchViewState> = liveData {
        when (stateEvent) {
            is SearchStateEvent.SearchMoviesStateEvent -> {
                emit(LoadingState)
                currentSearchQuery = stateEvent.searchTerm
                val result: SearchViewState = repository.searchMovies(stateEvent.searchTerm, page)
                if (result is ErrorState) {
                    emit(result)
                } else {
                    results.clear()
                    results += (result as SearchResultsState).listMovies
                    emit(result)
                }
            }
            is SearchStateEvent.GetNextPageRequest -> {
                emit(LoadingState)
                currentSearchQuery?.let {
                    val result: SearchViewState = repository.searchMovies(it, page)
                    if (result is ErrorState) {
                        emit(result)
                    } else {
                        val newResults = (result as SearchResultsState).listMovies
                        results += newResults
                        emit(SearchResultsState(
                            results,
                            result.totalResults
                        ))
                    }
                }
            }
        }
    }

    fun setStateEvent(stateEvent: SearchStateEvent) {
        _stateEvent.value = stateEvent
    }

    fun requestMoreResults() {
        if (resultsLoaded < totalResults) {
            page++
            _stateEvent.value = SearchStateEvent.GetNextPageRequest
        }
    }
}