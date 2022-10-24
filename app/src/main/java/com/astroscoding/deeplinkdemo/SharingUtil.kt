package com.astroscoding.deeplinkdemo

import android.content.Context
import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.astroscoding.deeplinkdemo.database.User

object SharingUtil {

    private fun Context.createUserSharingUrl(id: Int): String {
        val scheme = getString(R.string.scheme)
        val host = getString(R.string.host)
        val path = getString(R.string.path_to_existed_user)
        return "$scheme://$host$path?userId=$id"
    }

    fun shareUserUrl(context: Context, user: User) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            //extra text -> text to be shared
            putExtra(Intent.EXTRA_TEXT, context.createUserSharingUrl(user.id))
            //extra title -> title to be previewed while sharing
            putExtra(Intent.EXTRA_TITLE, "${user.name} | ${user.description}")
            type = "text/plain"
        }
        val share = Intent.createChooser(shareIntent, null)
        context.startActivity(share)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun CheckIfAppApprovedForDomain() {
        val context = LocalContext.current
        val domain = context.getString(R.string.host)
        val lifecycleOwner = LocalLifecycleOwner.current
        var launchDialog by remember { mutableStateOf(false) }
        val manager = remember { context.getSystemService(DomainVerificationManager::class.java) }
        DisposableEffect(key1 = lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    val userState = manager.getDomainVerificationUserState(context.packageName)
                    val verifiedDomain =
                        userState?.hostToStateMap?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_SELECTED }
                    val selectedDomains = userState?.hostToStateMap
                        ?.filterValues { it == DomainVerificationUserState.DOMAIN_STATE_SELECTED }
                    launchDialog =
                        (verifiedDomain?.keys?.contains(domain) != true || selectedDomains?.keys?.contains(domain) != true)
                                || userState.isLinkHandlingAllowed == false
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        if (launchDialog) {
            VerifyDomainDialog(
                onDismissed = {
                    launchDialog = false
                },
                onConfirm = {
                    val intent = Intent(
                        Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            )
        }
    }

    @Composable
    private fun VerifyDomainDialog(
        modifier: Modifier = Modifier,
        onDismissed: () -> Unit,
        onConfirm: () -> Unit
    ) {

        AlertDialog(
            onDismissRequest = { /*TODO*/ },
            modifier = modifier,
            title = {
                Text(
                    text = "Extra Configurations Needed!"
                )
            },
            text = {
                Text(text = "To provide the best user experience with this feature, please do the following:\n" +
                        "1 - Click on \"Open Settings\" below.\n" +
                        "2 - Make sure \"Open supported links\" is switched ON.\n" +
                        "3 - Make sure you checked all the links in the dialog after clicking on \"+ Add link\".\n\n" +
                        "Note: You could simply ignore setting the app as the default links handler. Though you won't be able to navigate to the app by clicking on external app-related links")
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(text = "Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissed) {
                    Text(text = "Ignore")
                }
            }
        )
    }

}