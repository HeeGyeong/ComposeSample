package com.example.composesample

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.ui.theme.ComposeSampleTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background) {
                    ListTest(itemList)
                }
            }
        }
    }
}

val itemList = listOf(
    Message("A1",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\""),
    Message("A2",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\""),
    Message("A3",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\""),
    Message("A4", "b4"),
    Message("A5", "b5"),
    Message("A6", "b6"),
    Message("A7", "b7"),
    Message("A8", "b8"),
    Message("A9", "b9"),
    Message("A0", "b0"),
    Message("A1", "b1"),
    Message("A2",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\""),
    Message("A3", "b3"),
    Message("A4", "b4"),
    Message("A5", "b5"),
    Message("A6", "b6"),
    Message("A7", "b7"),
    Message("A8", "b8"),
    Message("A9", "b9"),
    Message("A0",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\""),
    Message("A1", "b1"),
    Message("A2", "b2"),
    Message("A3", "b3"),
    Message("A4", "b4"),
    Message("A5", "b5"),
    Message("A6", "b6"),
    Message("A7", "b7"),
    Message("A8", "b8"),
    Message("A9", "b9"),
    Message("A0", "b0"),
)

data class Message(val head: String, val body: String)

@Composable
fun TestButton(text: String) {
    Button(
        onClick = { Log.d("logggggggging", "click test Button") },
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 12.dp,
            end = 20.dp,
            bottom = 12.dp
        ),

        ) {
        Text("Like : $text")
    }
}

@Composable
fun ListTest(itemList: List<Message>) {
    // 스크롤의 position의 상태를 저장.
    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier.padding(bottom = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(itemList) { index, item ->
            if (index == 3) {
                TestButton("Sample")
            } else {
                CardView(item)
            }
        }
    }
}

@Composable
fun CardView(msg: Message) {

    val coroutineScope = rememberCoroutineScope()

    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = painterResource(R.drawable.profile_picture),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        )

        Column(modifier = Modifier.clickable {
            coroutineScope.launch {
                isExpanded = !isExpanded
                Log.d("logggggggging", "isExpanded ? $isExpanded")
            }
        }.fillMaxWidth()
        ) {
            Text(
                text = msg.head,
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.subtitle2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                elevation = 10.dp,
                color = surfaceColor,
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun DefaultPreview() {
    ComposeSampleTheme {
        ListTest(itemList)
    }
}