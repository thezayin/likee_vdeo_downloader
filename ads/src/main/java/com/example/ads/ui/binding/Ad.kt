package com.example.ads.ui.binding

import com.example.ads.databinding.LayoutAdmobNativeMaxBinding
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.CoroutineScope

fun NativeAd.show(
    coroutineScope: CoroutineScope,
    binding: LayoutAdmobNativeMaxBinding?,
    onDismiss: () -> Unit,
) {
    binding.show(coroutineScope, this, onDismiss)
}