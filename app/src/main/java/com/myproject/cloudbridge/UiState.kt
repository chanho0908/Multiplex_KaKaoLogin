package com.myproject.cloudbridge

sealed class UiState {
    data object Loading: UiState()
    class Success<T>(item: T): UiState()
    class Error(throwable: Throwable?): UiState()
}