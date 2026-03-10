package com.example.demoapp.features.users.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.demoapp.domain.model.User

@Composable
fun UserListScreen(
    onNavigateToUserDetail: (String) -> Unit, // Función para navegar a la pantalla de detalles del usuario (recibe el ID del usuario)
    usersViewModel: UserListViewModel = viewModel()
){
    // Obtener la lista de usuarios desde el ViewModel
    val users by usersViewModel.users.collectAsState(initial = emptyList())

    // Se usa LazyColumn para mostrar la lista de usuarios.
    // LazyColumn solo renderiza los elementos visibles en pantalla, mejorando el rendimiento, además integra scrolling automáticamente.
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Iterar sobre la lista de usuarios y crear un ItemUser para cada uno
        items(users) {
            ItemUser(
                onNavigateToUserDetail = onNavigateToUserDetail,
                user = it
            )
        }
    }
}

@Composable
fun ItemUser(
    onNavigateToUserDetail: (String) -> Unit, // Función para navegar a la pantalla de detalles del usuario (recibe el ID del usuario)
    user: User
){
    // ListItem es un composable que muestra un elemento de una lista con un diseño predefinido.
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable {
                // Sabemos que al hacer clic en el ListItem, se navega a la pantalla de detalles del usuario, pasando el ID del usuario seleccionado, pero no se imlementa la navegación aquí.
                onNavigateToUserDetail(user.id)
            },
        headlineContent = {
            Text(text = user.name)
        },
        supportingContent = {
            // Mostrar el email del usuario como contenido secundario (puede ajustarse según se desee)
            Text(text = user.email)
        },
        leadingContent = {
            // Mostrar la imagen de perfil del usuario
            AsyncImage(
                contentScale = ContentScale.Crop,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profilePictureUrl) // URL de la imagen
                    .crossfade(true) // Efecto de desvanecimiento al cargar
                    .build(),
                contentDescription = "Imagen de perfil",
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .size(80.dp)
            )
        }
    )
}