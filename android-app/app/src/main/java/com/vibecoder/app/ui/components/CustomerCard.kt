package com.vibecoder.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vibecoder.app.R
import com.vibecoder.app.models.CustomerWithFollowUps
import com.vibecoder.app.models.FollowUpStatus
import com.vibecoder.app.ui.theme.*
import com.vibecoder.app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCard(
    customerWithFollowUps: CustomerWithFollowUps,
    onCallClick: (CustomerWithFollowUps) -> Unit,
    onEditClick: (CustomerWithFollowUps) -> Unit,
    onDeleteClick: (CustomerWithFollowUps) -> Unit,
    modifier: Modifier = Modifier
) {
    val customer = customerWithFollowUps.customer
    val followUpCount = customerWithFollowUps.followUpCount
    val statusColor = customerWithFollowUps.statusColor
    
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(getStatusBackgroundColor(statusColor))
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = customer.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Actions Menu
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_call)) },
                            onClick = {
                                showMenu = false
                                onCallClick(customerWithFollowUps)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Call, contentDescription = null)
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_edit)) },
                            onClick = {
                                showMenu = false
                                onEditClick(customerWithFollowUps)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete)) },
                            onClick = {
                                showMenu = false
                                onDeleteClick(customerWithFollowUps)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Amount and Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Amount
                Text(
                    text = "$${customer.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Promise Date
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = DateUtils.formatDateForDisplay(customer.promiseDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = DateUtils.getRelativeDateString(customer.promiseDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Follow-up Status
            if (followUpCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = getStatusColor(statusColor),
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = stringResource(R.string.followup_count, followUpCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = getStatusColor(statusColor),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Notes (if present)
            if (customer.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = customer.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Quick Call Button
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { onCallClick(customerWithFollowUps) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(stringResource(R.string.action_call))
            }
        }
    }
}

@Composable
private fun getStatusBackgroundColor(status: FollowUpStatus): Color {
    return when (status) {
        FollowUpStatus.LOW -> FollowUpLow.copy(alpha = 0.1f)
        FollowUpStatus.MEDIUM -> FollowUpMedium.copy(alpha = 0.1f)
        FollowUpStatus.HIGH -> FollowUpHigh.copy(alpha = 0.1f)
        FollowUpStatus.CRITICAL -> FollowUpCritical.copy(alpha = 0.1f)
    }
}

@Composable
private fun getStatusColor(status: FollowUpStatus): Color {
    return when (status) {
        FollowUpStatus.LOW -> MaterialTheme.colorScheme.primary
        FollowUpStatus.MEDIUM -> MaterialTheme.colorScheme.secondary
        FollowUpStatus.HIGH -> MaterialTheme.colorScheme.tertiary
        FollowUpStatus.CRITICAL -> MaterialTheme.colorScheme.error
    }
}