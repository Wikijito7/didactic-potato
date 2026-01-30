# UI Instructions

## Compose UI Patterns

### Screen Structure
```kotlin
@Composable
fun MyScreen(
    modifier: Modifier = Modifier,
    viewModel: MyViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    MyScreenContent(modifier, state)
}

@Composable
private fun MyScreenContent(modifier: Modifier, state: MyState) {
    // UI implementation
}
```

### ViewModel Injection
- **Always use**: `koinInject<VM>()` for ViewModel dependencies
- **Never use**: `viewModel()` from Compose for parameterized ViewModels
- **Registration**: `viewModelOf(::VM)` in Koin uiModule
- **Import**: `import org.koin.compose.koinInject`

### State Management
- **ViewModel State**: `private val _state = MutableStateFlow(MyState())`
- **Public State**: `val state: StateFlow<MyState> = _state.asStateFlow()`
- **UI Collection**: `val state by viewModel.state.collectAsState()`
- **State Updates**: `_state.value = _state.value.copy(...)`

### State Data Classes
```kotlin
data class MyState(
    val data: List<MyVO> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Event Handling
- **User Actions**: Pass lambdas from ViewModel methods
- **Example**: `onClick = viewModel::onActionClicked`
- **Navigation**: Use callbacks for screen transitions

### Error Handling in UI
```kotlin
state.error?.let { error ->
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp)
    )
}
```

### Loading States

#### Skeleton Loading (Preferred)
Show placeholder shapes that mimic the actual layout for better UX:
```kotlin
if (state.isLoading) {
    SkeletonLoadingContent()
} else {
    // Real content
}

@Composable
private fun SkeletonLoadingContent() {
    val skeletonColor = MaterialTheme.colorScheme.surfaceVariant
    
    // Match the structure of your actual UI
    Box(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(skeletonColor)
    )
    // ... skeleton placeholders matching real cards
}
```

#### Spinner Loading (Legacy)
```kotlin
if (state.isLoading) {
    CircularProgressIndicator()
} else {
    // Content
}
```

### Preventing Skeleton Flash
Check cache before showing loading state:
```kotlin
// In ViewModel
val hasCache = repository.hasCachedData()
if (!hasCache) {
    _state.value = _state.value.copy(isLoading = true)
}
// Load data...
_state.value = _state.value.copy(isLoading = false, data = loadedData)
```

### Navigation Patterns
- **Conditional Screens**: Use boolean flags for auth-required sections
- **Tab Navigation**: NavigationSuiteScaffold for main app navigation
- **Overlay Screens**: Show login/auth screens over main UI

### Material Design 3
- **Theme**: Use DidacticpotatoTheme wrapper
- **Components**: Material3 components (Card, Button, TextField, etc.)
- **Colors**: Access via `MaterialTheme.colorScheme`
- **Typography**: `MaterialTheme.typography`

### Preview Functions
```kotlin
@Preview(showBackground = true)
@Composable
private fun MyScreenPreview() {
    MaterialTheme {
        MyScreen()
    }
}
```

### Responsive Design
- **Adaptive Navigation**: NavigationSuiteScaffold for phones/tablets
- **Modifier Patterns**: `fillMaxWidth()`, `padding()`, etc.
- **Arrangement**: `verticalArrangement`, `horizontalAlignment`

### Best Practices
- Keep UI stateless, logic in ViewModel
- Use descriptive modifier names
- Handle all state cases (loading, error, success)
- Preview all screen states
- Follow Material Design guidelines