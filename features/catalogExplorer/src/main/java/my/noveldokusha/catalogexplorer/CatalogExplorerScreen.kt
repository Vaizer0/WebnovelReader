package my.noveldokusha.catalogexplorer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import my.noveldoksuha.coreui.components.ChipOption
import my.noveldoksuha.coreui.components.CollapsibleDivider
import my.noveldoksuha.coreui.components.LanguageFilterChips
import my.noveldokusha.extensions.ExtensionsManagerViewModel
import my.noveldokusha.extensions.ExtensionsScreen
import my.noveldokusha.extensions.ExtensionsScreenEvent
import my.noveldokusha.navigation.NavigationRouteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogExplorerScreen(
    navigationRouteViewModel: NavigationRouteViewModel = viewModel()
) {
    val viewModel: CatalogExplorerViewModel = viewModel()

    val context by rememberUpdatedState(newValue = LocalContext.current)
    var languagesOptionsExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        snapAnimationSpec = null,
        flingAnimationSpec = null
    )

    val extensionsViewModel = viewModel<ExtensionsManagerViewModel>()
    val extensionsState by extensionsViewModel.state.collectAsState()
    var extensionsChipsVisible by rememberSaveable { mutableStateOf(false) }

    val importLuaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        context.contentResolver.openInputStream(uri)?.use { input ->
            val code = input.bufferedReader().readText()
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "local.lua"
            extensionsViewModel.importLuaFromText(fileName, code)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    title = {
                        Text(
                            text = stringResource(id = R.string.title_finder),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    actions = {
                        when (selectedTabIndex) {
                            0 -> {
                                IconButton(onClick = {
                                    navigationRouteViewModel.globalSearch(
                                        context,
                                        text = ""
                                    ).let(context::startActivity)
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_search_24),
                                        contentDescription = stringResource(R.string.search_for_title)
                                    )
                                }
                                IconButton(onClick = {
                                    languagesOptionsExpanded = !languagesOptionsExpanded
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_languages_24),
                                        contentDescription = stringResource(R.string.open_for_more_options)
                                    )
                                    LanguagesDropDown(
                                        expanded = languagesOptionsExpanded,
                                        languageItemList = viewModel.languagesList,
                                        onDismiss = { languagesOptionsExpanded = false },
                                        onSourceLanguageItemToggle = { viewModel.toggleSourceLanguage(it.language) }
                                    )
                                }
                            }
                            1 -> ExtensionsTabActions(
                                onRefresh = { extensionsViewModel.onEvent(ExtensionsScreenEvent.OnRefresh) },
                                onShowRepositoryDialog = { extensionsViewModel.onEvent(ExtensionsScreenEvent.OnShowRepositoryDialog) },
                                onToggleLanguageChips = { extensionsChipsVisible = !extensionsChipsVisible },
                                onImportLua = { importLuaLauncher.launch(arrayOf("text/*", "application/octet-stream", "application/x-lua")) },
                            )
                        }
                    }
                )

                val tabTitles = listOf(
                    R.string.title_browse to Icons.Outlined.Explore,
                    R.string.title_extensions to Icons.Outlined.Extension,
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                ) {
                    tabTitles.forEachIndexed { index, (titleRes, icon) ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                        ) {
                            Row(
                                modifier = Modifier
                                    .height(48.dp)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    icon, null, Modifier.size(18.dp),
                                    tint = if (selectedTabIndex == index) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    stringResource(titleRes),
                                    color = if (selectedTabIndex == index) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (selectedTabIndex == 1) {
                    LanguageFilterChips(
                        selected = extensionsState.selectedLanguages,
                        all = extensionsState.availableLanguages.map { ChipOption(id = it.code, label = it.name, count = it.count) },
                        onToggle = { code -> extensionsViewModel.onEvent(ExtensionsScreenEvent.OnLanguageFilterToggle(code)) },
                        onClearAll = { extensionsViewModel.onEvent(ExtensionsScreenEvent.OnLanguageFilterClear(null)) },
                        visible = extensionsChipsVisible,
                    )
                }

                CollapsibleDivider(scrollBehavior.state)
            }
        },
        content = { innerPadding ->
            when (selectedTabIndex) {
                0 -> CatalogList(
                    innerPadding = innerPadding,
                    databasesList = viewModel.databaseList,
                    sourcesList = viewModel.sourcesList,
                    onDatabaseClick = {
                        navigationRouteViewModel.databaseSearch(
                            context,
                            databaseBaseUrl = it.baseUrl
                        ).let(context::startActivity)
                    },
                    onSourceClick = {
                        navigationRouteViewModel.sourceCatalog(
                            context,
                            sourceBaseUrl = it.baseUrl
                        ).let(context::startActivity)
                    },
                    onSourceSetPinned = viewModel::onSourceSetPinned
                )
                1 -> ExtensionsScreen(
                    innerPadding = innerPadding,
                    onImportLua = { importLuaLauncher.launch(arrayOf("text/*", "application/octet-stream", "application/x-lua")) }
                )
            }
        }
    )
}

@Composable
private fun ExtensionsTabActions(
    onRefresh: () -> Unit,
    onShowRepositoryDialog: () -> Unit,
    onToggleLanguageChips: () -> Unit,
    onImportLua: () -> Unit,
) {
    Row {
        IconButton(onClick = onImportLua) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.import_lua),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.refresh),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onShowRepositoryDialog) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.repository_settings),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onToggleLanguageChips) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_languages_24),
                contentDescription = stringResource(R.string.languages),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
