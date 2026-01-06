package com.example.mobilcoffeebookingappmidterm2025.ui.ar

import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch
import com.google.android.filament.LightManager
import com.google.android.filament.EntityManager
import com.google.android.filament.utils.Float3

/**
 * AR Preview Screen - View coffee products in true-to-scale AR
 * Uses SceneView 2.3.1 for ARCore integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArPreviewScreen(
    productName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var sceneView by remember { mutableStateOf<ARSceneView?>(null) }
    var isPlaneDetected by remember { mutableStateOf(false) }
    var isValidPlacement by remember { mutableStateOf(false) }
    var modelPlaced by remember { mutableStateOf(false) }
    var anchorNode by remember { mutableStateOf<AnchorNode?>(null) }
    var modelNode by remember { mutableStateOf<ModelNode?>(null) }
    var currentHitResult by remember { mutableStateOf<HitResult?>(null) }
    var selectedSize by remember { mutableStateOf("Medium") }
    var isLoading by remember { mutableStateOf(false) }
    var modelConfigJson by remember { mutableStateOf<org.json.JSONObject?>(null) }
    
    // Cup sizes in meters (realistic measurements)
    val cupSizes = mapOf(
        "Small" to 0.10f,   // 10cm height
        "Medium" to 0.12f,  // 12cm height
        "Large" to 0.14f    // 14cm height
    )
    
    // Load model config JSON once (if available) so UI can display correct metric sizes.
    LaunchedEffect(Unit) {
        try {
            val configName = "models/coffee_cup.json"
            context.assets.open(configName).bufferedReader().use { reader ->
                val jsonText = reader.readText()
                modelConfigJson = org.json.JSONObject(jsonText)
            }
        } catch (_: Exception) {
            modelConfigJson = null
        }
    }

    // Function to load and place model with current size
    fun placeModelWithSize(anchor: AnchorNode, size: String) {
        val currentSceneView = sceneView ?: return
        lifecycleOwner.lifecycleScope.launch {
            isLoading = true
            try {
                // Load model instance
                currentSceneView.modelLoader.loadModelInstance(
                    fileLocation = "models/coffee_cup.glb"
                )?.let { modelInstance ->

                    // Compute original mesh height from the model bounding box (Y axis)
                    val tempNode = ModelNode(modelInstance = modelInstance)
                    val measuredHeight = tempNode.size.y // measured height in scene units (meters expected)

                    // Read model config JSON (if loaded earlier) and determine desired / original heights
                    val originalHeightFromConfig: Float? = try {
                        modelConfigJson?.optDouble("original_height_meters", Double.NaN)?.takeIf { !it.isNaN() }?.toFloat()
                    } catch (e: Exception) { null }

                    // desiredHeight: prefer config sizes if available, otherwise fallback to cupSizes map
                    val desiredHeightMeters: Float = try {
                        modelConfigJson?.getJSONObject("size")?.getDouble(size)?.toFloat()
                            ?: cupSizes[size]!!
                    } catch (e: Exception) {
                        cupSizes[size]!!
                    }

                    // Compute baked scale (how much the model is already scaled inside the GLB)
                    val bakedScale: Float? = if (originalHeightFromConfig != null && originalHeightFromConfig > 0f && measuredHeight > 0f) {
                        measuredHeight / originalHeightFromConfig
                    } else null

                    // Compute final uniform scale to apply at runtime.
                    // If measuredHeight is valid, scale so that measuredHeight * scale = desiredHeightMeters
                    // Otherwise, fall back to using originalHeightFromConfig if present.
                    val finalScale = when {
                        measuredHeight > 0f -> desiredHeightMeters / measuredHeight
                        originalHeightFromConfig != null && originalHeightFromConfig > 0f -> desiredHeightMeters / originalHeightFromConfig
                        else -> 1.0f
                    }

                    // Optional: compute actual (current) size information for logging / UI
                    val actualMeasuredSizeMeters = if (measuredHeight > 0f) measuredHeight else originalHeightFromConfig ?: 0f

                    // Create the node and apply scale/centering
                    val newModelNode = ModelNode(
                        modelInstance = modelInstance,
                        centerOrigin = Position(y = 0f)
                    ).apply {
                        // Uniform scale
                        scale = Scale(finalScale)
                    }

                    // Debug logs (useful during development)
                    try {
                        android.util.Log.d("ARPreview", "Model measuredHeight(m): $measuredHeight")
                        android.util.Log.d("ARPreview", "Model originalFromConfig(m): $originalHeightFromConfig")
                        android.util.Log.d("ARPreview", "Model bakedScale: $bakedScale")
                        android.util.Log.d("ARPreview", "Desired height(m): $desiredHeightMeters")
                        android.util.Log.d("ARPreview", "Final runtime scale applied: $finalScale")
                        android.util.Log.d("ARPreview", "Actual measured size used(m): $actualMeasuredSizeMeters")
                    } catch (_: Exception) { }

                    // Remove old model if exists
                    modelNode?.let { anchor.removeChildNode(it) }
                    // Add new model
                    anchor.addChildNode(newModelNode)
                    modelNode = newModelNode
                }
            } catch (e: Exception) {
                // Model not found - that's ok, placeholder behavior
            }
            isLoading = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            sceneView?.destroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("AR Preview", fontSize = 18.sp)
                        Text(
                            productName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, "Close AR")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // AR Scene View
            AndroidView(
                factory = { ctx ->
                    ARSceneView(ctx).apply {
                        sceneView = this
                        lifecycle = lifecycleOwner.lifecycle
                        planeRenderer.isEnabled = true
                        
                        // Configure ARCore session with environmental lighting
                        // This should prevent black/textureless models by using real-world light estimation
                        configureSession { session, config ->
                            config.depthMode = when (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                                true -> Config.DepthMode.AUTOMATIC
                                else -> Config.DepthMode.DISABLED
                            }
                            config.instantPlacementMode = Config.InstantPlacementMode.DISABLED
                            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                        }
                        
                        // Add lighting using Filament directly for PBR materials
                        // This is ESSENTIAL for textures and colors to be visible
                        val lightManager = engine.lightManager
                        
                        // Create main directional light (sun-like)
                        val sunEntity = EntityManager.get().create()
                        LightManager.Builder(LightManager.Type.SUN)
                            .color(1.0f, 1.0f, 1.0f)
                            .intensity(100000.0f)
                            .direction(0.0f, -1.0f, -0.5f)
                            .castShadows(true)
                            .build(engine, sunEntity)
                        scene.addEntity(sunEntity)
                        
                        // Create fill light from opposite side
                        val fillEntity = EntityManager.get().create()
                        LightManager.Builder(LightManager.Type.DIRECTIONAL)
                            .color(0.9f, 0.95f, 1.0f)
                            .intensity(50000.0f)
                            .direction(0.0f, -1.0f, 0.5f)
                            .castShadows(false)
                            .build(engine, fillEntity)
                        scene.addEntity(fillEntity)
                        
                        // Frame-by-frame hit testing for reticle positioning
                        onSessionUpdated = { _, frame ->
                            if (!modelPlaced) {
                                // Check if any planes are detected
                                val planes = frame.getUpdatedPlanes()
                                    .filter { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                                
                                if (planes.isNotEmpty()) {
                                    isPlaneDetected = true
                                }
                                
                                // Perform hit test from screen center
                                val centerX = width / 2f
                                val centerY = height / 2f
                                
                                val hits = frame.hitTest(centerX, centerY)
                                val validHit = hits.firstOrNull { hitResult ->
                                    val trackable = hitResult.trackable
                                    if (trackable is Plane) {
                                        // Filter planes: must be tracking and have reasonable size
                                        val isTracking = trackable.trackingState == TrackingState.TRACKING
                                        val isHorizontal = trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                                        val isPoseInPolygon = trackable.isPoseInPolygon(hitResult.hitPose)
                                        
                                        // Check plane size (extents in meters)
                                        val extentX = trackable.extentX
                                        val extentZ = trackable.extentZ
                                        val area = extentX * extentZ
                                        val isLargeEnough = area >= 0.04f // At least 20cm x 20cm
                                        
                                        isTracking && isHorizontal && isPoseInPolygon && isLargeEnough
                                    } else {
                                        false
                                    }
                                }
                                
                                currentHitResult = validHit
                                isValidPlacement = validHit != null
                            }
                        }
                        
                        // Tap handler for placing the model
                        onTouchEvent = { motionEvent, _ ->
                            if (motionEvent.action == MotionEvent.ACTION_UP && !modelPlaced) {
                                currentHitResult?.let { hitResult ->
                                    // Create anchor at hit position
                                    val anchor = hitResult.createAnchor()
                                    val newAnchorNode = AnchorNode(engine, anchor)
                                    
                                    addChildNode(newAnchorNode)
                                    anchorNode = newAnchorNode
                                    modelPlaced = true
                                    
                                    // Load model and place with selected size
                                    placeModelWithSize(newAnchorNode, selectedSize)
                                }
                                true
                            } else {
                                false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Center reticle indicator (shows before placement)
            if (!modelPlaced) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = when {
                                isValidPlacement -> Color(0xFF4CAF50).copy(alpha = 0.8f)
                                isPlaneDetected -> Color(0xFFFF9800).copy(alpha = 0.6f)
                                else -> Color(0xFF9E9E9E).copy(alpha = 0.4f)
                            },
                            modifier = Modifier.size(if (isValidPlacement) 48.dp else 40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (isValidPlacement) "✓" else "+",
                                    fontSize = 24.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // (size label removed from camera view - size shown in selector below)
                    }
                }
            }
            
            // Top size indicator removed from camera view. Size is shown in the white selector card below.
            
            // UI Overlays
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                
                // Instructions Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Status indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    modelPlaced -> Color(0xFF4CAF50)
                                    isValidPlacement -> Color(0xFF2196F3)
                                    isPlaneDetected -> Color(0xFFFF9800)
                                    else -> Color(0xFF9E9E9E)
                                }
                            ) {
                                Text(
                                    text = when {
                                        isLoading -> "⏳ Loading..."
                                        modelPlaced -> "✓ Model Placed"
                                        isValidPlacement -> "✓ Ready to Place"
                                        isPlaneDetected -> "◉ Move to Valid Surface"
                                        else -> "◯ Scanning..."
                                    },
                                    modifier = Modifier.padding(8.dp, 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Instructions
                        Text(
                            text = when {
                                isLoading -> "Loading 3D model..."
                                modelPlaced -> "Model placed! Change size or move your phone to view from different angles."
                                isValidPlacement -> "Tap anywhere on screen to place the model at the center marker."
                                isPlaneDetected -> "Point the center marker at a flat surface (table, floor). Green = good placement."
                                else -> "Move your phone slowly to detect flat surfaces."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        // Size selector with metric height display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Cup Size:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = modelConfigJson?.optJSONObject("size")
                                    ?.optDouble(selectedSize, Double.NaN)
                                    ?.takeIf { !it.isNaN() }
                                    ?.let { "${(it * 100).toInt()} cm height" }
                                    ?: "${(cupSizes[selectedSize]!! * 100).toInt()} cm height",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            cupSizes.keys.forEach { size ->
                                FilterChip(
                                    selected = selectedSize == size,
                                    onClick = { 
                                        if (selectedSize != size) {
                                            selectedSize = size
                                            // Dynamically update model with new size
                                            anchorNode?.let { anchor ->
                                                placeModelWithSize(anchor, size)
                                            }
                                        }
                                    },
                                    label = { Text(size) },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading
                                )
                            }
                        }
                        
                        // Reset button
                        if (modelPlaced) {
                            Button(
                                onClick = {
                                    anchorNode?.let { node -> 
                                        sceneView?.removeChildNode(node)
                                    }
                                    anchorNode = null
                                    modelPlaced = false
                                    isPlaneDetected = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Reset & Scan Again")
                            }
                        }
                        
                        // Info about missing model
                        if (modelPlaced && !isLoading) {
                            Text(
                                "ℹ️ Add coffee_cup.glb to assets/models/ for 3D model",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
