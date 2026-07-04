package com.example.tvtimeneverdie.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

@Composable
inline fun <reified VM : ViewModel> rememberViewModel(crossinline creator: () -> VM): VM =
    viewModel(factory = viewModelFactory { initializer { creator() } })
