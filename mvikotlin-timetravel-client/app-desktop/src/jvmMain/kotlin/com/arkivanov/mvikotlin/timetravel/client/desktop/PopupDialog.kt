package com.arkivanov.mvikotlin.timetravel.client.desktop

import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*
 * Workaround due to https://github.com/JetBrains/compose-jb/issues/345
 */

private val LocalCustomPopup = staticCompositionLocalOf<@Composable (PopupArgs) -> Unit>()

@Composable
fun PopupDialog(
    title: String,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    LocalCustomPopup.current.invoke(PopupArgs(title, onDismissRequest, content))
}

@Composable
fun WithCustomPopups(content: @Composable () -> Unit) {
    val popups = mutableStateListOf<PopupArgs>()

    CompositionLocalProvider(
        LocalCustomPopup.provides(
            value = { popupArgs ->
                DisposableEffect(popupArgs) {
                    popups += popupArgs
                    onDispose { popups -= popupArgs }
                }
            }
        )
    ) {
        Box {
            content()

            popups.forEach { popupArgs ->
                PopupUi(popupArgs)
            }
        }
    }
}

@OptIn(ExperimentalLayout::class)
@Composable
private fun PopupUi(args: PopupArgs) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionState = remember { InteractionState() },
                onClick = args.onDismissRequest
            )
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .clickable(
                    indication = null,
                    interactionState = remember { InteractionState() },
                    onClick = {}
                ),
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.width(IntrinsicSize.Min)) {
                Text(
                    text = args.title,
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.SemiBold
                )

                Divider()

                args.content()
            }
        }
    }
}

private data class PopupArgs(
    val title: String,
    val onDismissRequest: () -> Unit,
    val content: @Composable () -> Unit
)
