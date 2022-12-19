package com.example.zadanie.ui.viewmodels

import androidx.lifecycle.*
import com.example.zadanie.data.DataRepository
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.helpers.Evento
import com.example.zadanie.ui.viewmodels.data.MyLocation
import kotlinx.coroutines.launch

class BarsViewModel(private val repository: DataRepository): ViewModel() {
    private val _message = MutableLiveData<Evento<String>>()
    val message: LiveData<Evento<String>>
        get() = _message
    val loading = MutableLiveData(false)
    val myLocation = MutableLiveData<MyLocation>(null)
    val sortingNameAsc = MutableLiveData(true)
    val sortingUsersAsc = MutableLiveData(true)
    val sortingDistanceAsc = MutableLiveData(true)

    var bars: MutableLiveData<List<BarItem>?> = myLocation.switchMap {
        liveData {
            loading.postValue(true)
            it?.let {
                repository.apiBarList(it.lat,it.lon) { _message.postValue(Evento(it)) }
            }
            loading.postValue(false)
            emitSource(repository.dbBars())
        } as MutableLiveData<List<BarItem>?>
    } as MutableLiveData<List<BarItem>?>

    fun sortNameAsc() {
        viewModelScope.launch {
            sortingNameAsc.postValue(false)
            sortingNameAsc.postValue(true)
        }
    }
    fun sortUsersAsc() {
        viewModelScope.launch {
            sortingUsersAsc.postValue(false)
            sortingUsersAsc.postValue(true)
        }
    }
    fun sortDistanceAsc() {
        viewModelScope.launch {
            sortingDistanceAsc.postValue(false)
            sortingDistanceAsc.postValue(true)
        }
    }
    fun refreshData(){
        loading.postValue(true)
        this.bars = myLocation.switchMap {
            liveData {

                it?.let {
                    repository.apiBarList(it.lat,it.lon) { _message.postValue(Evento(it)) }
                }

                emitSource(repository.dbBars())
            } as MutableLiveData<List<BarItem>?>
        } as MutableLiveData<List<BarItem>?>
        loading.postValue(false)
    }

    fun show(msg: String){ _message.postValue(Evento(msg))}
}