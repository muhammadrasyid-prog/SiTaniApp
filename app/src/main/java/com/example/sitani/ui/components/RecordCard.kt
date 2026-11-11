package com.example.sitani.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sitani.ui.theme.BrandPrimary
import com.example.sitani.ui.theme.BrandStrong
import com.example.sitani.ui.theme.SuccessBg
import com.example.sitani.ui.theme.TextSecondary
import com.example.sitani.ui.theme.WarningBg
import com.example.sitani.ui.theme.WarningText

@Composable
fun RecordCard(
    title: String,
    subtitle: String? = null,
    amount: String? = null,
    amountColor: Color = BrandPrimary,
    infoLeft: String? = null,
    infoRight: String? = null,
    isSynced: Boolean = true,
    showSyncChip: Boolean = true,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = com.example.sitani.ui.theme.SurfaceWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showSyncChip) {
                        if (isSynced) {
                            StatusChip("Tersinkron", SuccessBg, BrandStrong)
                        } else {
                            StatusChip("Belum sinkron", WarningBg, WarningText)
                        }
                    }
                    if (showChevron) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (subtitle != null || amount != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text("", modifier = Modifier.weight(1f))
                    }
                    if (amount != null) {
                        Text(
                            amount,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = amountColor
                        )
                    }
                }
            }

            if (infoLeft != null || infoRight != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (infoLeft != null) {
                        Text(
                            infoLeft,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text("", modifier = Modifier.weight(1f))
                    }
                    if (infoRight != null) {
                        Text(
                            infoRight,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
