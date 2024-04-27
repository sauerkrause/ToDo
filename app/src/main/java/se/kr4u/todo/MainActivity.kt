package se.kr4u.todo

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
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

    private val todoViewModel: IToDoViewModel by viewModels {
        ToDoViewModelFactory((application as ToDoApplication).repository)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3AdaptiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appName = resources.getString(R.string.app_name)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(activity = this)
            ToDoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ToDoApp(todoViewModel, windowSizeClass.widthSizeClass)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ToDoApp(toDoViewModel: IToDoViewModel, widthClass: WindowWidthSizeClass) {
    val fabVisible = rememberSaveable { mutableIntStateOf(View.VISIBLE) }
    val addItem = rememberSaveable { mutableStateOf(false) }
    val title = stringResource(id = R.string.app_name)
    val useNavRail = when (widthClass) {
        WindowWidthSizeClass.Compact -> false
        else -> true
    }
    val fullScreenDialog = when (widthClass) {
        WindowWidthSizeClass.Compact -> true
        else -> false
    }

    Scaffold(
        topBar = {
            if (!useNavRail) {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    title = {
                        Text(text = title)
                    }
                )
            }
        },
        floatingActionButton = {
            if (!useNavRail && fabVisible.intValue == View.VISIBLE && !addItem.value) {
                FloatingActionButton(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = { addItem.value = true },
                    shape = CircleShape) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        }
    ) { innerPadding ->
        // Inner scaffold should be reactive to size
        if (fullScreenDialog) {
            ToDoCreateCompact(addItem, toDoViewModel)
        } else {
            ToDoCreateNotCompact(addItem, toDoViewModel)
        }
        Row () {
            if (useNavRail) {
                NavigationRail (modifier = Modifier
                    .fillMaxHeight()
                    .wrapContentWidth()
                    .padding(innerPadding)) {
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = { addItem.value = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }
            Column() {
                // Panes handles layout size
                ToDoPanes(Modifier.padding(innerPadding), fabVisible, toDoViewModel)
            }
        }
    }
}

@Composable
fun ToDoCreateCompact(addItem: MutableState<Boolean>, toDoViewModel: IToDoViewModel) {
    if (addItem.value) {
        ToDoCreateCompactDialog(true, toDoViewModel) {
            addItem.value = false
        }
    }
}


@Composable
fun ToDoCreateNotCompact(addItem: MutableState<Boolean>, toDoViewModel: IToDoViewModel) {
    if (addItem.value) {
        ToDoCreateDialog(false, toDoViewModel) {
            addItem.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoCreateCompactDialog(
    fullscreen: Boolean = true,
    toDoViewModel: IToDoViewModel,
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val content = @Composable {
        ToDoCompose(
            fullscreen = fullscreen,
            onAdd = { title, details ->
                run {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (title.isNotEmpty()) {
                            toDoViewModel.insert(ToDo(0, title, details))
                        }
                        onDismissRequest()
                    }
                }
            },
            onCancel = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    onDismissRequest()
                }
            })
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight()
    ) {
            content()
    }
}

@Composable
fun ToDoCreateDialog(
    fullscreen: Boolean,
    toDoViewModel: IToDoViewModel,
    onDismissRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val content = @Composable {
        ToDoCompose(fullscreen = fullscreen,
            onAdd = {title, details -> run {
                    scope.launch { onDismissRequest() }.invokeOnCompletion {
                        if (title.isNotEmpty()) {
                            toDoViewModel.insert(ToDo(0, title, details))
                        }
                    }
                }
            },
            onCancel = onDismissRequest)
    }
    if (fullscreen) {
        // TODO: Implement a navigation host to allow multiple screen navigation
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                content()
            }
        }
    } else {
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ToDoCompose(fullscreen: Boolean, onAdd: (String, String) -> Unit, onCancel: () -> Unit) {
    var titleText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var detailsText by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    val modifier = Modifier.fillMaxWidth(1f)
    ConstraintLayout (modifier = when (fullscreen) {
        true -> modifier.padding(bottom = 16.dp)
        false -> modifier.fillMaxWidth(1f)
    }) {
        val (header, cancel, body, add) = createRefs()
        Text(
            modifier = Modifier.constrainAs(header) {
                centerHorizontallyTo(parent)
                top.linkTo(parent.top, margin = 16.dp)
            },
            text = "New ToDo Item",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary)
        if (fullscreen) {
            IconButton(onClick = onCancel,
                modifier = Modifier.constrainAs(cancel) {
                    top.linkTo(parent.top, margin = 5.dp)
                    start.linkTo(parent.start, margin = 5.dp)
                }) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
            TextButton(
                onClick = {
                    onAdd(titleText.text, detailsText.text)
                },
                modifier = Modifier.constrainAs(add) {
                    top.linkTo(parent.top, margin = 5.dp)
                    end.linkTo(parent.end, margin = 5.dp)
                }) {
                Text(text = "Add")
            }
        }
        Column(
            modifier = Modifier
                .padding(16.dp)
                .constrainAs(body) {
                    top.linkTo(header.bottom, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    centerHorizontallyTo(parent)
                }
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("Title") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = detailsText,
                onValueChange = { detailsText = it },
                label = { Text("Details") },
                textStyle = MaterialTheme.typography.bodySmall,
                minLines = 3,
                maxLines = 10)
            if (!fullscreen) {
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(
                        onClick = onCancel
                    ) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        onAdd(titleText.text, detailsText.text)
                    }) {
                        Text("Add")
                    }
                }
            }
        }
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
fun ToDoPanes(modifier: Modifier = Modifier, visibleActionButton: MutableIntState = mutableIntStateOf(View.VISIBLE), toDoViewModel: IToDoViewModel) {
    var selectedItem: ToDoItem? by rememberSaveable(stateSaver = ToDoItem.saver) {
        mutableStateOf(null)
    }

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()

    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
        selectedItem = null
        visibleActionButton.intValue = View.VISIBLE
    }

    val toDoItems by toDoViewModel.allToDos.collectAsStateWithLifecycle(initialValue = toDoViewModel.initialToDos)

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
    ToDoCompose(fullscreen = false, onAdd = {_, _ -> }, onCancel = {})
}

@Preview
@Composable
fun ToDoItemAddPreviewFullscreen() {
    ToDoCompose(fullscreen = true, onAdd = {_, _ -> }, onCancel = {})
}

@Preview
@Composable
fun ToDoCompactDialog() {
    ToDoCreateCompactDialog(
        fullscreen = true,
        toDoViewModel = PreviewToDoViewModel(),
        onDismissRequest = {})
}

@Preview
@Composable
fun ToDoAppCompact() {
    ToDoApp(
        toDoViewModel = PreviewToDoViewModel(),
        widthClass = WindowWidthSizeClass.Compact)
}

@Preview(device = "spec:width=1200dp,height=600dp,dpi=120,orientation=landscape")
@Composable
fun ToDoAppExpand() {
    ToDoApp(
        toDoViewModel = PreviewToDoViewModel(),
        widthClass = WindowWidthSizeClass.Medium)
}
