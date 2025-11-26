package com.test.kmp.floor.planner

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.refTo
import org.jetbrains.skia.Image
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.getBytes

//@Composable
//actual fun loadImageBitmap(resourceName: String): ImageBitmap {
//    val path = NSBundle.mainBundle.pathForResource(resourceName, "png")
//    val data = NSData.dataWithContentsOfFile(path!!)!!
//    val bytes = ByteArray(data.length.toInt())
//    data.getBytes(bytes.refTo(0), data.length)
//    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
//}