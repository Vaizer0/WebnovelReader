package my.noveldoksuha.coreui.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeEditorField(
    text: String,
    onTextChange: (String) -> Unit,
    fontSize: Int,
    showLineNumbers: Boolean,
    wordWrap: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalSyntaxColors.current
    val fontSp = fontSize.sp
    val lineHeight = (fontSize * 1.4f).sp
    val scrollState = rememberScrollState()

    var value by remember { mutableStateOf(TextFieldValue(text)) }
    LaunchedEffect(text) {
        if (value.text != text) value = TextFieldValue(text)
    }

    val transformation = remember(colors) {
        SyntaxHighlightTransformation(colors)
    }

    val lineCount = remember(value.text) {
        maxOf(1, value.text.count { it == '\n' } + 1)
    }

    Row(
        modifier = modifier
            .background(colors.background)
            .verticalScroll(scrollState)
    ) {
        if (showLineNumbers) {
            Box(
                modifier = Modifier
                    .background(colors.gutter)
                    .padding(top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    text = (1..lineCount).joinToString("\n"),
                    color = colors.gutterText,
                    fontSize = fontSp,
                    lineHeight = lineHeight,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 6.dp, end = 8.dp)
                )
            }
        }
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                value = newValue
                onTextChange(newValue.text)
            },
            textStyle = TextStyle(
                color = colors.foreground,
                fontSize = fontSp,
                lineHeight = lineHeight,
                fontFamily = FontFamily.Monospace
            ),
            cursorBrush = SolidColor(colors.foreground),
            softWrap = wordWrap,
            visualTransformation = transformation,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        )
    }
}

private class SyntaxHighlightTransformation(
    private val colors: SyntaxColors,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(luaHighlight(text.text, colors), OffsetMapping.Identity)
    }
}
