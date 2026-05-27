package com.beautystock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beautystock.utils.BeautyColors
import com.beautystock.utils.BeautySpacing

@Composable
fun AuthScaffold(
    title: String,
    subtitle: String,
    onFooterClick: () -> Unit,
    footerText: String,
    footerAction: String,
    body: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BeautyColors.PrimaryLight.copy(alpha = 0.2f),
                        BeautyColors.Background,
                        BeautyColors.SecondaryLight.copy(alpha = 0.18f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BeautySpacing.xl, vertical = BeautySpacing.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = BeautyColors.TextPrimary.copy(alpha = 0.08f)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BeautyColors.Surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = BeautyColors.Divider
                )
            ) {
                Column(
                    modifier = Modifier.padding(BeautySpacing.xl)
                ) {
                    Column(
                        modifier = Modifier.padding(bottom = BeautySpacing.xxxl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BeautyStock",
                            color = BeautyColors.Secondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.displaySmall,
                            color = BeautyColors.TextPrimary,
                            fontStyle = FontStyle.Italic
                        )
                        Text(
                            text = subtitle,
                            color = BeautyColors.TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = BeautySpacing.xs)
                        )
                    }

                    body()

                    AuthFooter(
                        footerText = footerText,
                        footerAction = footerAction,
                        onClick = onFooterClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthFooter(
    footerText: String,
    footerAction: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = footerText,
            color = BeautyColors.TextSecondary,
            fontSize = 14.sp
        )
        TextButton(onClick = onClick) {
            Text(
                text = footerAction,
                color = BeautyColors.Primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SocialDivider(text: String = "or continue with") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = BeautySpacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BeautyColors.Divider)
        )
        Text(
            text = text,
            color = BeautyColors.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier
                .background(Color.White)
                .padding(horizontal = BeautySpacing.md)
        )
    }
}

internal data class AgeGroup(
    val value: String,
    val label: String,
    val desc: String
)

@Composable
internal fun AgeGroupCard(
    ageGroup: AgeGroup,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(64.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                BeautyColors.PrimaryLight.copy(alpha = 0.2f)
            } else {
                BeautyColors.Surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BeautyColors.Primary else BeautyColors.PrimaryLight.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(BeautySpacing.sm),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = ageGroup.label,
                color = if (isSelected) BeautyColors.Primary else BeautyColors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = ageGroup.desc,
                color = BeautyColors.TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

