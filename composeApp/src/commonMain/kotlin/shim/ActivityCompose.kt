@file:Suppress("unused", "PackageDirectoryMismatch")
@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package androidx.activity.compose

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    androidx.compose.ui.backhandler.BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
fun <I, O> rememberLauncherForActivityResult(
    contract: ActivityResultContract<I, O>,
    onResult: (O) -> Unit
): ActivityResultLauncher<I> {
    val current by rememberUpdatedState(onResult)
    return remember(contract) {
        object : ActivityResultLauncher<I>() {
            override fun launch(input: I) {
                contract.launch(input) { output -> current(output) }
            }
        }
    }
}
