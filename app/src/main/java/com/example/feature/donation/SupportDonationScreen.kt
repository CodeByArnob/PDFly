package com.example.feature.donation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.qrcode.QrCodeGenerator
import com.example.domain.model.CryptoTokenConfig
import com.example.domain.model.InternalDistributionConfig
import com.example.domain.model.OfficialDonationCatalog
import com.example.feature.common.PdflyTopBar
import com.example.ui.motion.pdflyPressable
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.pdflyColors
import kotlinx.coroutines.delay

@Composable
fun SupportDonationScreen(
    onCopyMessage: (String) -> Unit,
    onBack: () -> Unit,
    isCryptoDonationEnabled: Boolean = InternalDistributionConfig.isCryptoDonationEnabled
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val pdflyColors = MaterialTheme.pdflyColors
    val tokens = OfficialDonationCatalog.supportedTokens
    var selectedTokenIdx by remember { mutableIntStateOf(0) }
    val selectedToken: CryptoTokenConfig = tokens[selectedTokenIdx.coerceIn(0, tokens.lastIndex)]

    var copiedAddressConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(copiedAddressConfirmation) {
        if (copiedAddressConfirmation) {
            delay(2000L)
            copiedAddressConfirmation = false
        }
    }

    val qrMatrix = remember(selectedToken.receivingWalletAddress, selectedToken.symbol) {
        QrCodeGenerator.generateMatrix(selectedToken.receivingWalletAddress, size = 29)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "Support PDFly",
            subtitle = "Free PDF tools, powered by your support.",
            onBack = onBack
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .testTag("support_donation_lazy_column"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Information Card: "PDFly is Free Forever"
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("free_forever_info_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, pdflyColors.cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(pdflyColors.successContainer)
                                        .border(1.dp, pdflyColors.successBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = pdflyColors.successText,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "PDFly is Free Forever",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "All PDF tools included • Zero paywalls",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = pdflyColors.successText
                                    )
                                }
                            }
                            Text(
                                text = "All core PDF features in PDFly are completely free and work offline on your device. Donations are entirely optional and never unlock or restrict any feature.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Donation section rendered ONLY when enabled by internal compile-time build configuration
                if (isCryptoDonationEnabled) {
                    // Token Selection Section (USDT & USDC on BSC BEP-20)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Choose a Donation Token",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                tokens.forEachIndexed { idx, token ->
                                    val isSelected = idx == selectedTokenIdx
                                    val brandColor = if (token.symbol == "USDT") {
                                        pdflyColors.usdtBrand
                                    } else {
                                        pdflyColors.usdcBrand
                                    }
                                    val activeContainer = if (token.symbol == "USDT") {
                                        pdflyColors.usdtContainer
                                    } else {
                                        pdflyColors.usdcContainer
                                    }
                                    val containerColor by animateColorAsState(
                                        targetValue = if (isSelected) activeContainer else MaterialTheme.colorScheme.surface,
                                        label = "token_card_bg"
                                    )

                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .pdflyPressable(onClick = { selectedTokenIdx = idx })
                                            .testTag("token_selector_${token.symbol}"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = containerColor),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) brandColor else pdflyColors.cardBorder
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = brandColor,
                                                    modifier = Modifier.size(34.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = if (token.symbol == "USDT") "₮" else "$",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = pdflyColors.warningContainer,
                                                    border = BorderStroke(1.dp, pdflyColors.warningBorder)
                                                ) {
                                                    Text(
                                                        text = "BSC (BEP-20)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = pdflyColors.warningText,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = token.symbol,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = token.fullName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Network Info & Properly Sized QR Code Card
                    item {
                        val accentColor by animateColorAsState(
                            targetValue = if (selectedToken.symbol == "USDT") {
                                pdflyColors.usdtBrand
                            } else {
                                pdflyColors.usdcBrand
                            },
                            label = "selected_token_accent"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("crypto_donation_details_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, pdflyColors.cardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Network Information Pill
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = pdflyColors.subtleSurface,
                                    border = BorderStroke(1.dp, pdflyColors.cardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(accentColor)
                                        )
                                        Text(
                                            text = "Network: ${selectedToken.networkName} • ${selectedToken.tokenStandard}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Token Heading
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Donate ${selectedToken.symbol} (${selectedToken.tokenStandard})",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Scan the QR code or copy the receiving wallet address below.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Properly Sized QR Code Container
                                AnimatedContent(
                                    targetState = qrMatrix,
                                    transitionSpec = {
                                        fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                                    },
                                    label = "qr_code_transition"
                                ) { matrix ->
                                    Box(
                                        modifier = Modifier
                                            .size(188.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color.White)
                                            .border(2.dp, accentColor, RoundedCornerShape(18.dp))
                                            .padding(14.dp)
                                            .testTag("donation_qr_code_box"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val gridCount = matrix.size
                                            val cellSize = size.width / gridCount.toFloat()
                                            for (r in 0 until gridCount) {
                                                for (c in 0 until gridCount) {
                                                    if (matrix[r][c]) {
                                                        drawRect(
                                                            color = Color(0xFF0F172A),
                                                            topLeft = Offset(c * cellSize, r * cellSize),
                                                            size = Size(cellSize + 0.6f, cellSize + 0.6f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Receiving Wallet Address, Copy Address Button & Brief Safety Instruction Card
                    item {
                        val accentColor by animateColorAsState(
                            targetValue = if (selectedToken.symbol == "USDT") {
                                pdflyColors.usdtBrand
                            } else {
                                pdflyColors.usdcBrand
                            },
                            label = "wallet_card_accent"
                        )
                        val copyButtonColor by animateColorAsState(
                            targetValue = if (copiedAddressConfirmation) EmeraldSuccess else accentColor,
                            label = "copy_button_color"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wallet_and_contract_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, pdflyColors.cardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Receiving Wallet Address Container
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = pdflyColors.subtleSurface,
                                    border = BorderStroke(1.dp, pdflyColors.cardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Receiving Wallet Address (${selectedToken.symbol} • BSC BEP-20)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = selectedToken.receivingWalletAddress,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.testTag("receiving_wallet_address_text")
                                        )
                                    }
                                }

                                // Copy Address Action Button
                                Button(
                                    onClick = {
                                        copyToClipboard(
                                            context = context,
                                            label = "PDFly ${selectedToken.symbol} (BEP-20) Address",
                                            value = selectedToken.receivingWalletAddress
                                        )
                                        copiedAddressConfirmation = true
                                        onCopyMessage("Copied ${selectedToken.symbol} (BEP-20) wallet address")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = copyButtonColor,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("copy_wallet_address_button")
                                ) {
                                    Icon(
                                        imageVector = if (copiedAddressConfirmation) {
                                            Icons.Default.CheckCircle
                                        } else {
                                            Icons.Default.ContentCopy
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (copiedAddressConfirmation) "Address Copied" else "Copy Address",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Brief Safety Instruction
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = pdflyColors.subtleSurface,
                                    border = BorderStroke(1.dp, pdflyColors.cardBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("donation_safety_notice_card")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Send only ${selectedToken.symbol} on the BNB Smart Chain (BEP-20) network. Blockchain transfers are final and cannot be reversed.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Shown automatically when crypto donations are disabled by compile-time build configuration
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("community_support_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, pdflyColors.cardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Thank You for Using PDFly",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "You can support PDFly by sharing the app with friends, students, and colleagues who need a private, offline PDF toolkit.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, value))
}
