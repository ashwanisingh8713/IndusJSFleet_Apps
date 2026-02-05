package com.indusjs.pdfreport.handler

import androidx.compose.runtime.Composable

/**
 * Initialize PDF context.
 * Must be called at the start of any PDF handler composable.
 * On Android, this sets the context from LocalContext.current.
 */
@Composable
expect fun InitializePdfContext()
