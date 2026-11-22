package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.elearning.ui.theme.ElearningTheme
import com.example.platformaELearningowa.R

@Composable
internal fun OfflineBanner() {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(android.R.drawable.ic_dialog_alert), stringResource(R.string.offline_icon),
                modifier = Modifier.size(18.dp))
            Text(stringResource(R.string.offline_banner), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OfflineBannerPreview() {
    ElearningTheme { OfflineBanner() }
}
