package com.example.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.core.filemanager.LocalFileManager
import com.example.data.local.UserPreferencesState
import com.example.domain.model.AppThemeMode
import com.example.domain.model.CompressionPreset
import com.example.domain.model.PaperSizePreset
import com.example.feature.TopLevelDestination
import com.example.feature.common.PdflyTopBar
import com.example.ui.motion.pdflyPressable
import com.example.ui.theme.pdflyColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferencesState,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetDefaultCompression: (CompressionPreset) -> Unit,
    onSetDefaultPaperSize: (PaperSizePreset) -> Unit,
    onSetReducedMotion: (Boolean) -> Unit,
    onCleanTempCache: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val pdflyColors = MaterialTheme.pdflyColors
    var workspaceStorage by remember { mutableLongStateOf(LocalFileManager.getWorkspaceStorageSizeBytes(context)) }
    var tempStorage by remember { mutableLongStateOf(LocalFileManager.getTempCacheSizeBytes(context)) }

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "Settings & Preferences",
            subtitle = "Theme, Motion, Defaults, Storage & Privacy",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance & Theme Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, pdflyColors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Appearance & Theme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppThemeMode.entries.forEach { mode ->
                                val selected = preferences.themeMode == mode
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSetThemeMode(mode) },
                                    label = { Text(mode.label, fontWeight = FontWeight.SemiBold) },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = pdflyColors.strongBorder,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("theme_chip_${mode.name}")
                                )
                            }
                        }
                    }
                }
            }

            // Accessibility & Motion Preferences Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, pdflyColors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Animation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Reduced Motion Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Simplifies choreographed transitions and spring animations (also automatically honors Android system animation scale).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = preferences.reducedMotionEnabled,
                            onCheckedChange = onSetReducedMotion,
                            modifier = Modifier.testTag("toggle_reduced_motion")
                        )
                    }
                }
            }

            // PDF Processing Defaults Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, pdflyColors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Default Compression Preset",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CompressionPreset.entries.forEach { preset ->
                                val selected = preferences.defaultCompression == preset
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSetDefaultCompression(preset) },
                                    label = { Text(preset.name, fontWeight = FontWeight.SemiBold) },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = pdflyColors.strongBorder,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Default Paper Size (Scanner & Image-to-PDF)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PaperSizePreset.entries.forEach { size ->
                                val selected = preferences.defaultPaperSize == size
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSetDefaultPaperSize(size) },
                                    label = { Text(size.label, fontWeight = FontWeight.SemiBold) },
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = pdflyColors.strongBorder,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Local Storage & Temp Cache Safety Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, pdflyColors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Local Workspace & Temporary File Hygiene",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Workspace PDFs: ${LocalFileManager.formatFileSize(workspaceStorage)} • Temporary Cache: ${LocalFileManager.formatFileSize(tempStorage)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                onCleanTempCache()
                                tempStorage = LocalFileManager.getTempCacheSizeBytes(context)
                                workspaceStorage = LocalFileManager.getWorkspaceStorageSizeBytes(context)
                            },
                            border = BorderStroke(1.dp, pdflyColors.strongBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("clean_temp_cache_button")
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clean Temporary Processing Cache", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Navigation Links to Support, Privacy Policy, and About
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsNavRow(
                        icon = Icons.Default.VolunteerActivism,
                        title = "Support PDFly (Voluntary Donation)",
                        subtitle = "100% optional community support • Zero feature locks",
                        tag = "settings_nav_support",
                        onClick = { onNavigate(TopLevelDestination.SUPPORT) }
                    )
                    SettingsNavRow(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy & Offline Guarantee",
                        subtitle = "Zero cloud uploads, SAF file security & local processing",
                        tag = "settings_nav_privacy",
                        onClick = { onNavigate(TopLevelDestination.PRIVACY_POLICY) }
                    )
                    SettingsNavRow(
                        icon = Icons.Default.Info,
                        title = "About PDFly & Commercial Licenses",
                        subtitle = "Version ${BuildConfig.VERSION_NAME} • Apache 2.0 & Android SDK Engine",
                        tag = "settings_nav_about",
                        onClick = { onNavigate(TopLevelDestination.ABOUT) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    val pdflyColors = MaterialTheme.pdflyColors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pdflyPressable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, pdflyColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "Privacy Policy & Terms",
            subtitle = "Strict Local-Only Document Processing & Legal Terms",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                PolicySectionCard(
                    title = "1. 100% Local On-Device PDF Processing",
                    body = "PDFly processes every PDF document strictly locally on your Android device:\n" +
                        "• Zero Cloud Uploads: Your PDF files, scans, signatures, watermarks, and extracted text never leave your device.\n" +
                        "• Offline Execution: All 16 PDF tools operate completely offline without any external server dependencies.\n" +
                        "• Private Workspace: Processed files are saved inside your app-private workspace and exported only where you choose."
                )
            }
            item {
                PolicySectionCard(
                    title = "2. Storage Access Framework (SAF) & Permissions",
                    body = "PDFly adheres to strict least-privilege Android security practices:\n" +
                        "• System File Picker (SAF): Uses Android Storage Access Framework and Photo Picker so you grant access only to individual files you select.\n" +
                        "• No Broad Storage Permissions: Never requests READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, or MANAGE_EXTERNAL_STORAGE.\n" +
                        "• Camera Permission: Requested at runtime only when you open the live CameraX Document Scanner."
                )
            }
            item {
                PolicySectionCard(
                    title = "3. Zero Telemetry, Analytics, or Tracking",
                    body = "Your reading habits and document metadata remain strictly confidential:\n" +
                        "• No Analytics SDKs: PDFly does not collect filenames, page contents, SHA-256 hashes, or usage telemetry.\n" +
                        "• On-Device Database: Recent files, starred documents, bookmarks, annotations, and processing history are stored in a local Room SQLite database.\n" +
                        "• One-Tap Cleanup: You can clear temporary processing cache and operation history at any time in Settings."
                )
            }
            item {
                PolicySectionCard(
                    title = "4. Voluntary Donations & Non-Custodial Policy",
                    body = "PDFly is 100% free with zero paywalls, subscriptions, or locked features:\n" +
                        "• Optional Community Support: Voluntary USDT (BEP-20) and USDC (BEP-20) contributions on BNB Smart Chain (BSC • Chain ID 56) are completely optional.\n" +
                        "• Non-Custodial Safety: PDFly never asks for private keys, seed phrases, wallet connections, or transaction screenshots.\n" +
                        "• Irreversible Blockchain Transfers: Cryptocurrency transfers on public blockchains are final and non-refundable."
                )
            }
            item {
                PolicySectionCard(
                    title = "5. Terms of Use & Authorized Document Access",
                    body = "By using PDFly, you agree to the following responsible-use terms:\n" +
                        "• Document Ownership: Use PDFly's editing, extraction, and security unlock tools only on documents you own or are explicitly authorized to process.\n" +
                        "• Backup Important Files: Always keep backups of critical original documents before performing destructive edits or page deletions."
                )
            }
        }
    }
}

@Composable
fun AboutScreen(
    onOpenSupport: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val pdflyColors = MaterialTheme.pdflyColors

    Column(modifier = Modifier.fillMaxSize()) {
        PdflyTopBar(
            title = "About PDFly",
            subtitle = "PDFly: PDF Editor & Toolkit • v${BuildConfig.VERSION_NAME}",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                PolicySectionCard(
                    title = "1. Product Overview & Core Vision",
                    body = "PDFly is a complete, free, offline-first PDF productivity suite engineered for Android with Kotlin, Jetpack Compose, Material 3, Room, DataStore, CameraX, and Android's native PdfRenderer & ISO 32000-1 PDF engine.\n" +
                        "• 16 Local PDF Tools: Reader, Annotation, Signature, Merge, Split, Page Organizer, Compress, JPG/PNG to PDF, PDF to JPG/PNG, Extract Text, Extract Images, Document Scanner, Watermark, Page Numbers, Security & Unlock, and Metadata Inspector.\n" +
                        "• Free Forever: Every tool is unlocked for every user with zero subscriptions, zero ads, and zero account requirements."
                )
            }
            item {
                PolicySectionCard(
                    title = "2. Commercial Licensing & Open-Source Compliance",
                    body = "PDFly is built exclusively on commercially permissive (Apache 2.0, MIT, and SIL OFL 1.1) components with zero GPL or AGPL copyleft dependencies:\n" +
                        "• Android PdfRenderer & PdfDocument (Android SDK / Apache 2.0)\n" +
                        "• PDFly ISO 32000-1 (%PDF-1.4) Local Stream Codec\n" +
                        "• AndroidX Jetpack Compose, Room, DataStore & CameraX (Apache 2.0)\n" +
                        "• Google Fonts: Space Grotesk, Plus Jakarta Sans & JetBrains Mono (SIL Open Font License 1.1)"
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onOpenPrivacy,
                        border = BorderStroke(1.dp, pdflyColors.strongBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Privacy & Terms", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onOpenSupport,
                        border = BorderStroke(1.dp, pdflyColors.strongBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Support PDFly", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicySectionCard(title: String, body: String) {
    val pdflyColors = MaterialTheme.pdflyColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, pdflyColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
