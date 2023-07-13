package app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import app.ui.widgets.TopAppBar

@Composable
fun AddDevice(navigation: NavHostController) {
    Scaffold(
        topBar = { TopAppBar("Add device", navigation) },
    ) { contentPadding ->
        Column(Modifier
            .padding(top = contentPadding.calculateTopPadding())
            .fillMaxSize()) {
            Text(text = "Add device")
        }
    }
}