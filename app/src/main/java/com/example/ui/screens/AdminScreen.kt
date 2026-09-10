package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.RemedyEntity
import com.example.ui.components.AdminGate
import com.example.ui.components.ButtonVariant
import com.example.ui.components.LargeButton
import com.example.ui.components.RemedyForm
import com.example.ui.components.SourceCardItem
import com.example.ui.theme.DestructiveRed
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onNavigateHome: () -> Unit
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val remedies by viewModel.remedies.collectAsState()
    val allSources by viewModel.allSources.collectAsState()
    val showAddForm by viewModel.showAddForm.collectAsState()
    val editingRemedy by viewModel.editingRemedy.collectAsState()
    val expandedRemedyId by viewModel.expandedRemedyId.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var remedyToDelete by remember { mutableStateOf<RemedyEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        if (!isAuthenticated) {
            AdminGate(
                onAuthenticate = { passkey ->
                    viewModel.authenticatePasskey(passkey)
                }
            )
        } else {
            // Authenticated Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Clinical Admin Portal",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Remedies (${remedies.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Sources Catalog (${allSources.size})", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: Remedies Management
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LargeButton(
                            text = "Add Remedy",
                            onClick = { viewModel.toggleAddForm(true) },
                            modifier = Modifier.weight(1f),
                            minHeight = 48.dp,
                            icon = { Icon(Icons.Default.Add, null) },
                            testTag = "admin_add_remedy_btn"
                        )

                        LargeButton(
                            text = "Export (.doc)",
                            onClick = { viewModel.exportWordDoc() },
                            variant = ButtonVariant.Secondary,
                            modifier = Modifier.weight(1f),
                            minHeight = 48.dp,
                            icon = { Icon(Icons.Default.Description, null) },
                            testTag = "admin_export_doc_btn"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (showAddForm) {
                        RemedyForm(
                            onSave = { remedy -> viewModel.saveRemedy(remedy) },
                            onCancel = { viewModel.toggleAddForm(false) }
                        )
                    } else if (editingRemedy != null) {
                        RemedyForm(
                            initialRemedy = editingRemedy,
                            onSave = { remedy -> viewModel.saveRemedy(remedy) },
                            onCancel = { viewModel.setEditingRemedy(null) }
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(remedies) { remedy ->
                                RemedyCardItem(
                                    remedy = remedy,
                                    isExpanded = expandedRemedyId == remedy.id,
                                    onToggleExpand = { viewModel.toggleExpandRemedy(remedy.id) },
                                    onEdit = { viewModel.setEditingRemedy(remedy) },
                                    onDelete = { remedyToDelete = remedy }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Verified Sources Catalog
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allSources) { source ->
                            SourceCardItem(
                                source = source,
                                onDelete = { viewModel.deleteSource(source.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (remedyToDelete != null) {
        AlertDialog(
            onDismissRequest = { remedyToDelete = null },
            title = { Text("Delete Remedy", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${remedyToDelete?.name}' from the knowledge base?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        remedyToDelete?.let { viewModel.deleteRemedy(it.id) }
                        remedyToDelete = null
                    }
                ) {
                    Text("Delete", color = DestructiveRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { remedyToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RemedyCardItem(
    remedy: RemedyEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("remedy_item_${remedy.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = remedy.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!remedy.source_material.isNull_or_blank()) {
                        Text(
                            text = remedy.source_material!!,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!remedy.source_reference.isNull_or_blank()) {
                        Text(
                            text = "Ref: ${remedy.source_reference}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = DestructiveRed
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("Full Profile:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(remedy.full_profile, fontSize = 14.sp, lineHeight = 20.sp)

                        if (!remedy.indications.isNull_or_blank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Indications:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(remedy.indications!!, fontSize = 14.sp)
                        }

                        if (!remedy.reasoning.isNull_or_blank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Reasoning:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(remedy.reasoning!!, fontSize = 14.sp)
                        }

                        if (!remedy.benefits.isNull_or_blank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Benefits:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(remedy.benefits!!, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
