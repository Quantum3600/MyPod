package com.trishit.mypod.ui.extras

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.trishit.mypod.ui.components.WheelEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CameraScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    if (!hasPermission) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4F8))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Camera,
                contentDescription = null,
                tint = Color(0xFF1E5BB5),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Camera Access Needed",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF222222)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Grant camera permission to take photos on your iPod screen.",
                fontSize = 10.sp,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { launcher.launch(Manifest.permission.CAMERA) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5BB5))
            ) {
                Text("Allow Access", fontSize = 11.sp, color = Color.White)
            }
        }
        return
    }

    val photosDir = remember {
        File(context.filesDir, "photos").apply { mkdirs() }
    }

    var photosList by remember {
        mutableStateOf(getSavedPhotos(photosDir))
    }

    var isGalleryMode by remember { mutableStateOf(false) }
    var galleryIndex by remember { mutableIntStateOf(0) }
    var showFlash by remember { mutableStateOf(false) }
    var captureStatusText by remember { mutableStateOf<String?>(null) }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    // CameraX Lifecycle Binding
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    Log.e("CameraScreen", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    // Flash animation dismiss
    LaunchedEffect(showFlash) {
        if (showFlash) {
            delay(150L.milliseconds)
            showFlash = false
        }
    }

    // Capture photo helper
    fun takePhoto() {
        showFlash = true
        val photoFile = File(
            photosDir,
            "iPod_Photo_${System.currentTimeMillis()}.jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    photosList = getSavedPhotos(photosDir)
                    captureStatusText = "Photo Saved!"
                }

                override fun onError(exc: ImageCaptureException) {
                    captureStatusText = "Error saving photo"
                }
            }
        )
    }

    // Clear capture status text after delay
    LaunchedEffect(captureStatusText) {
        if (captureStatusText != null) {
            delay(2000L.milliseconds)
            captureStatusText = null
        }
    }

    // Handle Wheel Events
    LaunchedEffect(isGalleryMode, photosList.size) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> {
                    if (isGalleryMode && photosList.isNotEmpty()) {
                        var next = galleryIndex + event.detents
                        while (next < 0) next += photosList.size
                        galleryIndex = next % photosList.size
                    }
                }
                is WheelEvent.SelectPress -> {
                    if (isGalleryMode) {
                        isGalleryMode = false
                    } else {
                        takePhoto()
                    }
                }
                is WheelEvent.PlayPausePress -> {
                    if (photosList.isNotEmpty()) {
                        isGalleryMode = !isGalleryMode
                    }
                }
                is WheelEvent.MenuPress -> {
                    if (isGalleryMode) {
                        isGalleryMode = false
                    } else {
                        onExit()
                    }
                }
                else -> {}
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top LCD Camera Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (isGalleryMode) Color(0xFF38BDF8) else Color(0xFFEF4444),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGalleryMode) "PHOTO GALLERY" else "CAMERA VIEW",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "${photosList.size} Photos",
                    fontSize = 9.sp,
                    color = Color(0xFFAAAAAA)
                )
            }
        }

        // Viewport Area: Live Camera Preview or Photo Viewer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (isGalleryMode && photosList.isNotEmpty()) {
                val file = photosList[galleryIndex.coerceIn(0, photosList.lastIndex)]
                Image(
                    painter = rememberAsyncImagePainter(file),
                    contentDescription = "Captured Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                // Shutter Frame Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.White.copy(alpha = 0.2f))
                )
            }

            // White Flash Overlay
            if (showFlash) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                )
            }

            // Capture status toast
            captureStatusText?.let { text ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = text,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Footer Guidance Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isGalleryMode) "Wheel: Browse | Center: Back to Camera" else "Center: SNAP PHOTO | Play/Pause: Gallery",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

private fun getSavedPhotos(dir: File): List<File> {
    return dir.listFiles { f -> f.extension.equals("jpg", ignoreCase = true) || f.extension.equals("png", ignoreCase = true) }
        ?.sortedByDescending { it.lastModified() }
        ?: emptyList()
}
