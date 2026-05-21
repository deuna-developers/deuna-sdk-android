package com.deuna.explore.presentation.screens.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ExploreColors {
    val screenBackground = Color(0xFFF2F2F7)
    val cardBackground = Color(0xFFE5E5EA)
    val brandBlue = Color(0xFF147AE8)
    val labelGray = Color(0xFF6E7480)
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black.copy(alpha = 0.78f),
    )
}

@Composable
fun FieldTitle(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = ExploreColors.labelGray,
    )
}

@Composable
fun DrawerCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ExploreColors.cardBackground, RoundedCornerShape(16.dp)),
        content = content,
    )
}

@Composable
fun DrawerCardSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        DrawerCard {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
        }
    }
}

@Composable
fun ClearableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray, fontSize = 14.sp) },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
        ),
        shape = RoundedCornerShape(10.dp),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
    )
}

@Composable
fun <T> SegmentedPillSelector(
    items: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ExploreColors.cardBackground, RoundedCornerShape(16.dp))
            .padding(4.dp),
    ) {
        items.forEach { item ->
            val isSelected = item == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) Color.White else Color.Transparent,
                        RoundedCornerShape(12.dp),
                    )
                    .clickable { onSelect(item) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = labelOf(item),
                    color = if (isSelected) ExploreColors.brandBlue else ExploreColors.labelGray,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                )
            }
        }
    }
}
