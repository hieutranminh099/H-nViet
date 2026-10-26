package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SlateBorderStrong
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SlateSurfaceElevated
import com.example.ui.theme.SoftIndigo
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private data class OnboardingStepItem(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val highlightTip: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun OnboardingGuideModal(
    onDismiss: () -> Unit
) {
    var activeStepIndex by remember { mutableIntStateOf(0) }

    val steps = listOf(
        OnboardingStepItem(
            stepNumber = 1,
            title = "Bước 1: Tải video cần dịch",
            description = "Chọn 1 hoặc nhiều video từ máy của bạn, hoặc trải nghiệm ngay với bộ 4 tập phim ngắn mẫu có sẵn.",
            highlightTip = "Hỗ trợ MP4, MKV, MOV & tự động bảo vệ RAM điện thoại",
            icon = Icons.Default.VideoFile,
            accentColor = PrimaryBlue
        ),
        OnboardingStepItem(
            stepNumber = 2,
            title = "Bước 2: Bấm Dịch AI (Tự động che chữ gốc)",
            description = "Chọn quét phụ đề hình ảnh (OCR) hoặc âm thanh (ASR). AI tự động tạo lớp nền đặc 90% che kín chữ tiếng Trung và thay bằng tiếng Việt.",
            highlightTip = "Cam kết 100% không bao giờ bị lỗi chữ Việt đè lên chữ Trung",
            icon = Icons.Default.AutoAwesome,
            accentColor = SoftIndigo
        ),
        OnboardingStepItem(
            stepNumber = 3,
            title = "Bước 3: Kéo thả chữ & Xuất Video / Lưu Cloud",
            description = "Dùng một ngón tay kéo khung chữ đến vị trí vừa ý, chỉnh thanh trượt 'Độ mờ nền chữ' và bấm Xuất Lossless hoặc Lưu vào Kho Cloud.",
            highlightTip = "Giữ nguyên 100% chất lượng gốc & đồng bộ tức thì PC ⇄ Điện thoại",
            icon = Icons.Default.OpenWith,
            accentColor = EmeraldSuccess
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .border(1.dp, SlateBorderStrong, RoundedCornerShape(24.dp))
                .testTag("onboarding_guide_modal")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryBlue.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Hướng dẫn sử dụng nhanh",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "3 bước dịch video & màn hình không lỗi đè chữ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_close_onboarding")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng hướng dẫn",
                            tint = TextSecondary
                        )
                    }
                }

                // 3 Step Cards
                steps.forEachIndexed { index, step ->
                    val isSelected = activeStepIndex == index
                    Surface(
                        color = if (isSelected) SlateSurfaceElevated else SlateSurface,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) step.accentColor else SlateBorderStrong,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { activeStepIndex = index }
                            .testTag("onboarding_step_${step.stepNumber}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(step.accentColor.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = step.icon,
                                    contentDescription = null,
                                    tint = step.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = step.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = step.accentColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = step.highlightTip,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = step.accentColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Primary CTA Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_onboarding_start")
                ) {
                    Text(
                        text = "Đã hiểu, bắt đầu sử dụng ngay",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
