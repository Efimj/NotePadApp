package com.jobik.shkiper.screens.AppLayout

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavHostController
import com.jobik.shkiper.database.models.NotePosition
import com.jobik.shkiper.navigation.Route
import com.jobik.shkiper.navigation.SetupAppScreenNavGraph
import com.jobik.shkiper.screens.AppLayout.NavigationBar.AppNavigationBarState

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun ScreenWrapper(navController: NavHostController, startDestination: String) {

    val connection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (consumed.y < -30) {
                    AppNavigationBarState.hide()
                }
                if (consumed.y > 30) {
                    AppNavigationBarState.show()
                }
                if (available.y > 0) {
                    AppNavigationBarState.show()
                }

                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(connection)
    ) {
        SetupAppScreenNavGraph(
            navController = navController,
            startDestination = if (startDestination == Route.Onboarding.route) Route.Onboarding.route else Route.NoteList.route
        )
    }

    val isInitialized = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (isInitialized.value) return@LaunchedEffect
        isInitialized.value = true
        if (startDestination != Route.NoteList.notePosition(NotePosition.MAIN.name) && startDestination != Route.Onboarding.route)
            navController.navigate(startDestination) {
                launchSingleTop
            }
    }
}