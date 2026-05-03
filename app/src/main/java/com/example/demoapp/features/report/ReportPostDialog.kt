package com.example.demoapp.features.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.demoapp.R
import com.example.demoapp.domain.model.ReportReason

@Composable
fun ReportPostDialog(
    postId: String,
    onDismiss: () -> Unit,
    onReported: () -> Unit,
    viewModel: ReportPostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(postId) {
        viewModel.reset()
    }

    AlertDialog(
        onDismissRequest = { if (!state.isSubmitting) onDismiss() },
        title = {
            Text(
                text = stringResource(R.string.report_dialog_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.report_dialog_subtitle),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))

                ReportReason.values().forEach { reason ->
                    val selected = state.selectedReason == reason
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected,
                                onClick = { viewModel.selectReason(reason) }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { viewModel.selectReason(reason) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(reasonLabel(reason)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.details,
                    onValueChange = viewModel::updateDetails,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(stringResource(R.string.report_dialog_details_placeholder))
                    },
                    label = { Text(stringResource(R.string.report_dialog_details_label)) },
                    minLines = 2,
                    maxLines = 4,
                    enabled = !state.isSubmitting
                )

                state.errorMessageRes?.let { resId ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(resId),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.submit(postId) { onReported() } },
                enabled = !state.isSubmitting
            ) {
                Text(stringResource(R.string.report_dialog_submit))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isSubmitting
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

private fun reasonLabel(reason: ReportReason): Int = when (reason) {
    ReportReason.INAPPROPRIATE_CONTENT -> R.string.report_reason_inappropriate
    ReportReason.FALSE_OR_MISLEADING_INFORMATION -> R.string.report_reason_false_info
    ReportReason.SPAM -> R.string.report_reason_spam
    ReportReason.HARASSMENT -> R.string.report_reason_harassment
    ReportReason.OTHER -> R.string.report_reason_other
}
