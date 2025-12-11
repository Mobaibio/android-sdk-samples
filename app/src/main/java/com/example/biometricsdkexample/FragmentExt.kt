package com.example.biometricsdkexample

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController

fun Fragment.safeNavigation(direction: NavDirections) {
    val navController = findNavController()
    val currentDestination =
        (navController.currentDestination as? FragmentNavigator.Destination)?.className
            ?: (navController.currentDestination as? DialogFragmentNavigator.Destination)?.className

    if (currentDestination == this.javaClass.name) {
        navController.navigate(direction)
    }
}