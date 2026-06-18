package com.example.logistic_captain.ui.pod

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.logistic_captain.ui.theme.LogisticsBlue
import com.example.logistic_captain.ui.theme.LogisticsOrange
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodScreen(
    stopId: String,
    viewModel: PodViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.photoUri = tempUri
        }
    }

    LaunchedEffect(viewModel.uploadSuccess) {
        if (viewModel.uploadSuccess) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "PROOF OF DELIVERY",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = LogisticsBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DELIVERY CONFIRMATION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LogisticsBlue.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Stop ID: $stopId",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LogisticsBlue
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Photo Section
                    Text("Capture Photo", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F3F5))
                            .border(
                                width = 1.dp, 
                                color = if (viewModel.photoUri != null) LogisticsOrange else Color(0xFFDEE2E6),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.photoUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(viewModel.photoUri),
                                contentDescription = "POD Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFFADB5BD))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val file = File(context.cacheDir, "temp_camera_photo.jpg")
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            tempUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LogisticsOrange)
                    ) {
                        Text(if (viewModel.photoUri != null) "RETAKE PHOTO" else "TAKE PHOTO")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Signature Section
                    Text("Recipient Signature", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(8.dp))
                    SignaturePad(
                        onSignatureCaptured = { viewModel.signatureBitmap = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .border(1.dp, Color(0xFFDEE2E6), RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.errorMessage != null) {
                Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
            }

            Button(
                onClick = { viewModel.uploadPod(context, stopId) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = viewModel.photoUri != null && viewModel.signatureBitmap != null && !viewModel.isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (viewModel.isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("COMPLETE DELIVERY", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SignaturePad(
    onSignatureCaptured: (Bitmap?) -> Unit,
    modifier: Modifier = Modifier
) {
    var paths by remember { mutableStateOf(listOf<Path>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = modifier.onGloballyPositioned { size = it.size }) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newPath = Path().apply { moveTo(offset.x, offset.y) }
                            currentPath = newPath
                            paths = paths + newPath
                        },
                        onDrag = { change, _ ->
                            currentPath?.lineTo(change.position.x, change.position.y)
                            // Trigger recomposition
                            val p = currentPath
                            if (p != null) {
                                paths = paths.toMutableList().also { it[it.size - 1] = p }
                            }
                        },
                        onDragEnd = {
                            currentPath = null
                            if (size.width > 0 && size.height > 0) {
                                val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
                                val canvas = AndroidCanvas(bitmap)
                                canvas.drawColor(android.graphics.Color.WHITE)
                                val paint = Paint().apply {
                                    color = android.graphics.Color.BLACK
                                    style = Paint.Style.STROKE
                                    strokeWidth = 10f
                                    strokeCap = Paint.Cap.ROUND
                                    strokeJoin = Paint.Join.ROUND
                                    isAntiAlias = true
                                }
                                paths.forEach { path ->
                                    canvas.drawPath(path.asAndroidPath(), paint)
                                }
                                onSignatureCaptured(bitmap)
                            }
                        }
                    )
                }
        ) {
            paths.forEach { path ->
                drawPath(
                    path = path,
                    color = Color.Black,
                    style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        IconButton(
            onClick = { paths = emptyList(); onSignatureCaptured(null) },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.Clear, contentDescription = "Clear Signature")
        }
    }
}
