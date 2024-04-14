package se.kr4u.todo

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import se.kr4u.todo.ui.theme.ToDoTheme

class ToDoItem (val id: Int) {
    companion object {
        val saver: Saver<ToDoItem?, Int> = Saver(
            {it?.id},
            ::ToDoItem,
        )
    }
}

class MainActivity : ComponentActivity() {

    private var floatingButtonVisible = mutableIntStateOf(View.VISIBLE)

    private val todoViewModel: ToDoViewModel by viewModels {
        ToDoViewModelFactory((application as ToDoApplication).repository)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appName = resources.getString(R.string.app_name)
        setContent {
            ToDoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ToDoApp(todoViewModel)
                }
            }
        }
    }
}

@Composable
fun ToDoCompose(onClick: (String, String) -> Unit) {
    var titleText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var detailsText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    ConstraintLayout (modifier = Modifier.fillMaxWidth(1f)) {
        val (title, details, add) = createRefs()
        OutlinedTextField(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
                end.linkTo(parent.end, margin = 16.dp)
                centerHorizontallyTo(parent)
            },
            value = titleText,
            onValueChange = { titleText = it },
            label = { Text("Title") },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            modifier = Modifier.constrainAs(details) {
                top.linkTo(title.bottom, margin = 16.dp)
                start.linkTo(parent.start, margin = 16.dp)
                end.linkTo(parent.end, margin = 16.dp)
                centerHorizontallyTo(parent)
            },
            value = detailsText,
            onValueChange = { detailsText = it },
            label = { Text("Details") },
            textStyle = MaterialTheme.typography.bodySmall,
            minLines = 5)
        Button(
            onClick = {
                onClick(titleText.text, detailsText.text)
            },
            modifier = Modifier.constrainAs(add) {
                top.linkTo(details.bottom, margin = 16.dp)
                bottom.linkTo(parent.bottom, margin = 10.dp)
                centerHorizontallyTo(parent)
            }
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(Modifier.padding(5.dp))
            Text("Add TODO Item")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ToDoApp(toDoViewModel: ToDoViewModel) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val fabVisible = rememberSaveable { mutableIntStateOf(View.VISIBLE) }
    val bottomSheet = rememberSaveable { mutableStateOf(false) }
    val title = stringResource(id = R.string.app_name)
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Text(text = title)
                }
            )
        },
        floatingActionButton = {
            if (fabVisible.intValue == View.VISIBLE && !bottomSheet.value) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = { bottomSheet.value = true },
                    shape = CircleShape) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        if (bottomSheet.value) {
            ModalBottomSheet(
                onDismissRequest = {
                    bottomSheet.value = false
                },
                sheetState = sheetState,
                modifier = Modifier
                    .padding(bottom = 20.dp),
            ) {
                ToDoCompose(onClick = { title, details ->
                    scope.launch() { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            bottomSheet.value = false
                        }
                        toDoViewModel.insert(ToDo(0, title, details))
                    }
                })
                Spacer(
                    Modifier.windowInsetsBottomHeight(
                        WindowInsets.navigationBarsIgnoringVisibility
                    )
                )
            }
        }
        ToDoPanes(Modifier.padding(innerPadding), fabVisible, toDoViewModel)
    }
}

val sampleTodos = listOf(
    ToDo(0, "FOO", "BAR"),
    ToDo(1, "BAZ", "QUUX")
)

@Composable
fun ToDoList(modifier: Modifier,
             onItemClick: (ToDoItem) -> Unit,
             toDoItems: List<ToDo>) {
    LazyColumn {
        toDoItems.forEachIndexed { id, todo ->
            item {
                ListItem(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            onItemClick(ToDoItem(id))
                        },
                    headlineContent = {
                        Text(
                            text = todo.title,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                )
                if ( id < toDoItems.lastIndex)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            }
        }
    }
}

@Composable
fun ToDoItemNotSelected() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .fillMaxSize()
            .padding(16.dp),

    ) {
        Text(
            text = stringResource(R.string.select_an_item_in_the_list_to_see_details),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun ToDoItemDetails(todo: ToDo) {
    val title = todo.title
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.details_page_for, title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = todo.details,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ToDoPanes(modifier: Modifier = Modifier, visibleActionButton: MutableIntState = mutableIntStateOf(View.VISIBLE), toDoViewModel: ToDoViewModel) {
    var selectedItem: ToDoItem? by rememberSaveable(stateSaver = ToDoItem.saver) {
        mutableStateOf(null)
    }

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()

    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
        selectedItem = null
        visibleActionButton.intValue = View.VISIBLE
    }

    val toDoItems by toDoViewModel.allToDos.collectAsStateWithLifecycle(initialValue = listOf<ToDo>())

    ToDoTheme {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane(Modifier) {
                    ToDoList(
                        modifier = Modifier,
                        onItemClick = { id ->
                            // Set current item
                            selectedItem = id
                            // Display detail pane
                            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                        },
                        toDoItems = toDoItems,
                    )
                }
            },
            detailPane = {
                AnimatedPane(Modifier) {
                    selectedItem?.let {
                        if (toDoItems.size > it.id) {
                            ToDoItemDetails(toDoItems.get(it.id))
                            if (navigator.canNavigateBack()) {
                                visibleActionButton.intValue = View.INVISIBLE
                            } else {
                                visibleActionButton.intValue = View.VISIBLE
                            }
                        }
                    } ?: let {
                        ToDoItemNotSelected()
                    }
                }
            },
            modifier = modifier
        )
    }
}

@Preview
@Composable
fun ToDoItemDetailPreview() {
    val selectedItem = 1
    ToDoItemDetails(sampleTodos[selectedItem])
}

@Preview
@Composable
fun ToDoItemAddPreview() {
    ToDoCompose() {_, _ -> }
}