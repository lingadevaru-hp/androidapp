package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.PortfolioTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class BulletItem(val text: String) : MarkdownBlock()
    data class NumberedItem(val number: String, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    data class Formula(val formula: String) : MarkdownBlock()
}

@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Paragraph -> {
                    InlineMarkdownText(text = block.text)
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Real indented bullet indicator (accent colored dot)
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp, end = 10.dp)
                                .size(5.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(PortfolioTheme.colors.accent)
                        )
                        InlineMarkdownText(
                            text = block.text,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = PortfolioTheme.typography.bodyMedium.copy(
                                color = PortfolioTheme.colors.accent,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.width(22.dp)
                        )
                        InlineMarkdownText(
                            text = block.text,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockCard(code = block.code, language = block.language)
                }
                is MarkdownBlock.Table -> {
                    TableCard(headers = block.headers, rows = block.rows)
                }
                is MarkdownBlock.Formula -> {
                    FormulaCard(formula = block.formula)
                }
            }
        }
    }
}

@Composable
fun InlineMarkdownText(
    text: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val colors = PortfolioTheme.colors
    val typography = PortfolioTheme.typography

    val (annotatedString, linkMap) = remember(text, colors) {
        buildInlineAnnotatedString(text, colors.text, colors.accent, colors.surface3)
    }

    ClickableText(
        text = annotatedString,
        style = typography.bodyLarge.copy(color = colors.text),
        modifier = modifier,
        onClick = { offset ->
            linkMap.firstOrNull { it.range.contains(offset) }?.let { entry ->
                runCatching { uriHandler.openUri(entry.url) }
            }
        }
    )
}

@Composable
fun CodeBlockCard(
    code: String,
    language: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PortfolioTheme.colors.surface2)
            .border(1.dp, PortfolioTheme.colors.border, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" },
                style = PortfolioTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    color = PortfolioTheme.colors.textFaint
                )
            )

            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(code))
                    isCopied = true
                    Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                    scope.launch {
                        delay(2000)
                        isCopied = false
                    }
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = if (isCopied) PortfolioTheme.colors.ok else PortfolioTheme.colors.textDim,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = code,
            style = PortfolioTheme.typography.bodySmall.copy(
                fontFamily = JetBrainsMono,
                color = PortfolioTheme.colors.text,
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            )
        )
    }
}

private data class LinkEntry(val range: IntRange, val url: String)

@Composable
fun TableCard(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scroll = androidx.compose.foundation.rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PortfolioTheme.colors.surface2)
            .border(1.dp, PortfolioTheme.colors.border, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "table",
                style = PortfolioTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    color = PortfolioTheme.colors.textFaint
                )
            )
            IconButton(
                onClick = {
                    val csv = (listOf(headers) + rows).joinToString("\n") { it.joinToString(", ") }
                    clipboardManager.setText(AnnotatedString(csv))
                    Toast.makeText(context, "Table copied as CSV", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy table",
                    tint = PortfolioTheme.colors.textDim,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
        ) {
            // Header row
            Row(modifier = Modifier.padding(vertical = 6.dp)) {
                headers.forEach { h ->
                    Text(
                        text = h.trim(),
                        style = PortfolioTheme.typography.bodySmall.copy(
                            color = PortfolioTheme.colors.accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        ),
                        modifier = Modifier
                            .width(140.dp)
                            .padding(end = 12.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PortfolioTheme.colors.border)
            )
            rows.forEach { row ->
                Row(modifier = Modifier.padding(vertical = 6.dp)) {
                    headers.indices.forEach { i ->
                        Text(
                            text = row.getOrNull(i)?.trim().orEmpty(),
                            style = PortfolioTheme.typography.bodySmall.copy(
                                color = PortfolioTheme.colors.text,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier
                                .width(140.dp)
                                .padding(end = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormulaCard(
    formula: String,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PortfolioTheme.colors.surface2)
            .border(1.dp, PortfolioTheme.colors.accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = formula,
            style = PortfolioTheme.typography.bodyMedium.copy(
                fontFamily = com.example.ui.theme.InstrumentSerif,
                fontStyle = FontStyle.Italic,
                color = PortfolioTheme.colors.accent,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = {
                clipboardManager.setText(AnnotatedString(formula))
                Toast.makeText(context, "Formula copied", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy formula",
                tint = PortfolioTheme.colors.textDim,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun buildInlineAnnotatedString(
    rawText: String,
    textColor: androidx.compose.ui.graphics.Color,
    accentColor: androidx.compose.ui.graphics.Color,
    codeBgColor: androidx.compose.ui.graphics.Color
): Pair<AnnotatedString, List<LinkEntry>> {
    val links = mutableListOf<LinkEntry>()
    val sb = StringBuilder()

    // Regex to match (ordered — bold before italic):
    // 1. Links: [label](url)
    // 2. Bold: **text**
    // 3. Inline code: `text`
    // 4. Inline formula: $text$
    // 5. Italic: *text*
    val tokenRegex = Regex("(\\[.*?\\]\\(.*?\\))|(\\*\\*.+?\\*\\*)|(`.+?`)|(\\$[^$]+?\\$)|(\\*[^*]+?\\*)")

    var lastIndex = 0
    val builder = AnnotatedString.Builder()

    val matches = tokenRegex.findAll(rawText).toList()
    for (match in matches) {
        if (match.range.first > lastIndex) {
            val plain = rawText.substring(lastIndex, match.range.first)
            builder.append(plain)
        }

        val full = match.value
        when {
            // [label](url)
            full.startsWith("[") && full.contains("](") -> {
                val label = full.substringAfter("[").substringBefore("](")
                val url = full.substringAfter("](").substringBeforeLast(")")
                val start = builder.length
                builder.pushStyle(
                    SpanStyle(
                        color = accentColor,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    )
                )
                builder.append(label)
                builder.pop()
                val end = builder.length
                links.add(LinkEntry(start until end, url))
            }
            // **bold**
            full.startsWith("**") && full.endsWith("**") -> {
                val inner = full.substring(2, full.length - 2)
                builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor))
                builder.append(inner)
                builder.pop()
            }
            // *italic*
            full.startsWith("*") && full.endsWith("*") -> {
                val inner = full.substring(1, full.length - 1)
                builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor))
                builder.append(inner)
                builder.pop()
            }
            // $formula$
            full.startsWith("$") && full.endsWith("$") && full.length > 2 -> {
                val inner = full.substring(1, full.length - 1)
                builder.pushStyle(
                    SpanStyle(
                        fontFamily = com.example.ui.theme.InstrumentSerif,
                        fontStyle = FontStyle.Italic,
                        background = codeBgColor,
                        color = accentColor
                    )
                )
                builder.append(" $inner ")
                builder.pop()
            }
            // `code`
            full.startsWith("`") && full.endsWith("`") -> {
                val inner = full.substring(1, full.length - 1)
                builder.pushStyle(
                    SpanStyle(
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp,
                        background = codeBgColor,
                        color = accentColor
                    )
                )
                builder.append(" $inner ")
                builder.pop()
            }
            else -> {
                builder.append(full)
            }
        }
        lastIndex = match.range.last + 1
    }

    if (lastIndex < rawText.length) {
        builder.append(rawText.substring(lastIndex))
    }

    return Pair(builder.toAnnotatedString(), links)
}

private fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = text.lines()
    var index = 0

    while (index < lines.size) {
        val line = lines[index]
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            // Fenced code block
            val lang = trimmed.removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            index++
            while (index < lines.size && !lines[index].trim().startsWith("```")) {
                codeLines.add(lines[index])
                index++
            }
            if (index < lines.size) index++ // skip closing ```
            blocks.add(MarkdownBlock.CodeBlock(lang, codeLines.joinToString("\n")))
        } else if (trimmed.startsWith("$$")) {
            // Display formula block: $$ ... $$ (single or multi-line)
            val formulaLines = mutableListOf<String>()
            var first = trimmed.removePrefix("$$").trim()
            if (first.endsWith("$$")) {
                first = first.removeSuffix("$$").trim()
                if (first.isNotEmpty()) formulaLines.add(first)
                index++
            } else {
                if (first.isNotEmpty()) formulaLines.add(first)
                index++
                while (index < lines.size && !lines[index].trim().endsWith("$$")) {
                    formulaLines.add(lines[index])
                    index++
                }
                if (index < lines.size) {
                    val last = lines[index].trim().removeSuffix("$$").trim()
                    if (last.isNotEmpty()) formulaLines.add(last)
                    index++
                }
            }
            val formula = formulaLines.joinToString("\n").trim()
            if (formula.isNotEmpty()) blocks.add(MarkdownBlock.Formula(formula))
        } else if (trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.contains("|", ignoreCase = false)) {
            // Pipe table: collect consecutive pipe rows
            val tableLines = mutableListOf<String>()
            while (index < lines.size) {
                val t = lines[index].trim()
                if (t.startsWith("|") && t.endsWith("|")) {
                    tableLines.add(t)
                    index++
                } else break
            }
            val parsed = parsePipeTable(tableLines)
            if (parsed != null) {
                blocks.add(MarkdownBlock.Table(parsed.first, parsed.second))
            } else {
                tableLines.forEach { blocks.add(MarkdownBlock.Paragraph(it)) }
            }
        } else if (trimmed.startsWith("- ") || trimmed.startsWith("• ") || trimmed.startsWith("* ")) {
            // Bullet list item
            val itemText = trimmed.replace(Regex("^[-•*]\\s+"), "")
            blocks.add(MarkdownBlock.BulletItem(itemText))
            index++
        } else if (trimmed.matches(Regex("^\\d+\\.\\s+.*"))) {
            // Numbered list item
            val num = trimmed.substringBefore(".")
            val itemText = trimmed.substringAfter(".").trim()
            blocks.add(MarkdownBlock.NumberedItem(num, itemText))
            index++
        } else if (trimmed.isNotEmpty()) {
            blocks.add(MarkdownBlock.Paragraph(line))
            index++
        } else {
            index++
        }
    }

    return blocks
}

private fun parsePipeTable(tableLines: List<String>): Pair<List<String>, List<List<String>>>? {
    if (tableLines.size < 2) return null
    // Second line must be a separator row: | --- | --- |
    val separator = tableLines[1].trim('|').split("|").map { it.trim() }
    if (separator.isEmpty() || separator.any { !it.matches(Regex(":?-{3,}:?")) }) return null
    val headers = tableLines[0].trim('|').split("|").map { it.trim() }
    if (headers.isEmpty()) return null
    val rows = tableLines.drop(2).map { line ->
        line.trim('|').split("|").map { it.trim() }
    }.filter { it.any(String::isNotEmpty) }
    return Pair(headers, rows)
}
