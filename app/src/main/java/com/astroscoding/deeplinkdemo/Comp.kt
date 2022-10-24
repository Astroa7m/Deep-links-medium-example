import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.astroscoding.deeplinkdemo.SharingUtil
import com.astroscoding.deeplinkdemo.database.User

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    users: List<User>,
    onUserClicked: (id: Int) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        itemsIndexed(users) { index: Int, user: User ->
            Box(modifier = Modifier.padding(8.dp)) {
                SingleUser(user = user, onUserClicked = onUserClicked)
            }
        }
    }
}

@Composable
fun SingleUser(
    modifier: Modifier = Modifier,
    user: User,
    onUserClicked: (id: Int) -> Unit
) {
    UserItem(
        user = user,
        modifier = modifier,
        onUserClicked = onUserClicked
    )

}


@Composable
fun DetailsScreen(
    modifier: Modifier = Modifier,
    user: User?,
    onDismiss: () -> Unit,
    onSharing: () -> Unit,
    onAddNewUserClicked: () -> Unit
) {
    if (user == null)
        UserNotFoundDialog(
            onAddNewUserClicked = onAddNewUserClicked,
            onDismiss = onDismiss
        )
    else {
        Column(
            modifier = modifier.scrollable(rememberScrollState(), Orientation.Vertical),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UserItem(
                user = user
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                SharingUtil.CheckIfAppApprovedForDomain()

            IconButton(onClick = { onSharing() }) {
                Icon(imageVector = Icons.Rounded.Share, contentDescription = null)
            }

        }

    }
}

@Composable
fun UserItem(
    modifier: Modifier = Modifier,
    onUserClicked: ((id: Int) -> Unit)? = null,
    user: User
) {
    Card(
        modifier
            .clickable(enabled = onUserClicked != null)
            {
                onUserClicked?.let { it(user.id) }
            }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${user.joinedYear})",
                        style = MaterialTheme.typography.caption,
                        fontStyle = FontStyle.Italic
                    )
                }

                Icon(
                    imageVector = if (user.isElite) Icons.Rounded.Check else Icons.Rounded.Close,
                    tint = if (user.isElite) Color.Green else Color.Red,
                    contentDescription = null
                )
            }
            Text(
                text = user.description,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.SemiBold,
                maxLines = if (onUserClicked != null) 2 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun UserNotFoundDialog(
    onDismiss: () -> Unit,
    onAddNewUserClicked: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "User weren't found") },
        text = { Text(text = "Would you like to add them?") },
        confirmButton = {
            TextButton(onClick = onAddNewUserClicked) {
                Text(text = "Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }

    )
}

@Composable
fun AddUserScreen(
    userToBeAdded: User,
    onAddUser: (User) -> Unit
) {
    val (name, onNameChanged) = remember { mutableStateOf(userToBeAdded.name) }
    val (desc, onDescChanged) = remember { mutableStateOf(userToBeAdded.description) }
    val (isElite, onIsEliteChanged) = remember { mutableStateOf(userToBeAdded.isElite) }
    var joinedYear by remember { mutableStateOf(userToBeAdded.joinedYear.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TextField(
            value = name,
            onValueChange = onNameChanged,
            placeholder = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = desc,
            onValueChange = onDescChanged,
            placeholder = { Text("Description") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = joinedYear,
            onValueChange = { newYear ->
                joinedYear = newYear.filter { it.isDigit() }
            },
            placeholder = { Text("Joined Year") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Switch(
            checked = isElite,
            onCheckedChange = onIsEliteChanged
        )

        Button(onClick = {
            val user = User(0, name, desc, joinedYear.toInt(), isElite)
            onAddUser(user)
        }) {
            Text(text = "Add")
        }
    }
}
