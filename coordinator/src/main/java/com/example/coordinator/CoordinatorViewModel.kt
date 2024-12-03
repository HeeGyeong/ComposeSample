package com.example.coordinator

import android.app.Application
import com.example.core.BaseViewModel
import com.example.core.navigation.Navigation

class CoordinatorViewModel(
    private val navigation: Navigation,
    application: Application
) : BaseViewModel(navigation, application)