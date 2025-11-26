package com.test.kmp.floor.planner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import floorplankmp.composeapp.generated.resources.Res
import floorplankmp.composeapp.generated.resources.compose_multiplatform
import logkat.LogKat

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}

@Composable
fun DrawingCanvas() {
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    // Force recomposition on drag updates
    var redrawTrigger by remember { mutableStateOf(0) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentPath = Path().apply {
                            moveTo(offset.x, offset.y)
                        }
                        currentPath?.let { paths.add(it) }
                        redrawTrigger++  // trigger redraw
                    },
                    onDrag = { change, _ ->
                        currentPath?.lineTo(change.position.x, change.position.y)
                        redrawTrigger++  // trigger redraw
                    },
                    onDragEnd = {
                        currentPath = null
                    }
                )
            }
    ) {
        // This makes sure recomposition happens
        redrawTrigger

        for (path in paths) {
            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 5f)
            )
        }
    }
}


fun generateShades(baseColor: Color): List<Color> {
    return (1..5).map { i ->
        baseColor.copy(alpha = 0.2f * i)
    }
}
/*

// Updated Shape data class with fillColor and fillImage
data class Shape(
    val points: MutableList<Offset>,
    var isClosed: Boolean = false,
    var isSelected: Boolean = false,
    val lineColors: MutableList<Color> = mutableListOf(),
    val lineWidths: MutableList<Float> = mutableListOf(),
    var selectedLineIndex: Int? = null,
    var fillColor: Color = Color.Transparent,
    var fillImageRes: String? = null // Resource name for drawable/svg
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapeEditorScreen() {
    var isDrawMode by remember { mutableStateOf(false) }
    var isDrawSquareMode by remember { mutableStateOf(false) }
    var isSelectLIneMode by remember { mutableStateOf(false) }
    var isSeparateLineMode by remember { mutableStateOf(false) }
    var showLineEditSheet by remember { mutableStateOf(false) }
    var showFillSheet by remember { mutableStateOf(false) }

    // Fill mode: "color" or "image"
    var fillMode by remember { mutableStateOf("color") }

    // currently selected line and shape
    var selectedShapeIndex by remember { mutableStateOf(-1) }
    var selectedLineIndex by remember { mutableStateOf<Int?>(null) }

    // selected color and stroke width
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var selectedStrokeWidth by remember { mutableStateOf(5f) }
    var shadeList by remember { mutableStateOf(listOf<Color>()) }

    // Key state to track shapes for updates
    val shapes = remember { mutableStateListOf<Shape>() }
    var forceUpdate by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        EditableShapeCanvas(
            isDrawMode = isDrawMode,
            isDrawSquareMode = isDrawSquareMode,
            isSelectLineMode = isSelectLIneMode,
            isSeparateLineMode = isSeparateLineMode,
            shapes = shapes,
            onShapeCompleted = {
                isDrawMode = false
            },
            onLineSelected = { shape, i ->
                selectedShapeIndex = shapes.indexOf(shape)
                selectedLineIndex = i
                selectedColor = shape.lineColors.getOrNull(i) ?: Color.Black
                selectedStrokeWidth = shape.lineWidths.getOrNull(i) ?: 5f

                showLineEditSheet = true
            },
            onShapeSelected = { shape ->
                selectedShapeIndex = shapes.indexOf(shape)
                showFillSheet = true
            }
        )

        // Toolbar at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.LightGray)
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isDrawMode = !isDrawMode },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDrawMode) Color.Green else Color.Gray
                )
            ) {
                Text(text = if (isDrawMode) "Drawing…" else "Draw")
            }

            Button(
                onClick = { isDrawSquareMode = !isDrawSquareMode },
            ) {
                Text(text = "Drawing Square")
            }

            Button(
                onClick = { isSelectLIneMode = !isSelectLIneMode },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelectLIneMode) Color.Green else Color.Gray
                )
            ) {
                Text(text = "Select Line")
            }

            Button(
                onClick = { isSeparateLineMode = !isSeparateLineMode },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSeparateLineMode) Color.Green else Color.Gray
                )
            ) {
                Text(text = "Separate Line")
            }
        }
    }

    // Line Edit Bottom Sheet
    if (showLineEditSheet) {
        ModalBottomSheet(onDismissRequest = { showLineEditSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Choose Line Color", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                // Color palette
                val colorPalette = listOf(
                    Color.Red,
                    Color.Green,
                    Color.Blue,
                    Color.Yellow,
                    Color.Cyan,
                    Color.Magenta,
                    Color.Black
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colorPalette.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColor = color
                                    shadeList = generateShades(color)

                                    // Update the line color
                                    if (selectedShapeIndex >= 0 && selectedLineIndex != null) {
                                        shapes[selectedShapeIndex].lineColors[selectedLineIndex!!] =
                                            color
                                        forceUpdate++
                                    }
                                }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Shades")
                Spacer(Modifier.height(8.dp))

                // Shades row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    shadeList.forEach { shade ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(shade)
                                .border(
                                    width = if (selectedColor == shade) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColor = shade

                                    // Update the line color with shade
                                    if (selectedShapeIndex >= 0 && selectedLineIndex != null) {
                                        shapes[selectedShapeIndex].lineColors[selectedLineIndex!!] =
                                            shade
                                        forceUpdate++
                                    }
                                }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Line Thickness: ${selectedStrokeWidth.toInt()}px")
                Slider(
                    value = selectedStrokeWidth,
                    onValueChange = { newValue ->
                        selectedStrokeWidth = newValue

                        // Update the line width
                        if (selectedShapeIndex >= 0 && selectedLineIndex != null) {
                            shapes[selectedShapeIndex].lineWidths[selectedLineIndex!!] = newValue
                            forceUpdate++
                        }
                    },
                    valueRange = 2f..12f
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { showLineEditSheet = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Done")
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Fill Bottom Sheet (Color or Image)
    if (showFillSheet) {
        ModalBottomSheet(onDismissRequest = { showFillSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Fill Shape",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Toggle between Color and Image
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { fillMode = "color" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (fillMode == "color") Color(0xFF6200EE) else Color.Gray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Color")
                    }

                    Button(
                        onClick = { fillMode = "image" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (fillMode == "image") Color(0xFF6200EE) else Color.Gray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Image")
                    }
                }

                // Show colors or images based on mode
                if (fillMode == "color") {
                    val fillColors = listOf(
                        Color.Transparent to "None",
                        Color(0xFFFFCDD2) to "Light Red",
                        Color(0xFFF8BBD0) to "Light Pink",
                        Color(0xFFE1BEE7) to "Light Purple",
                        Color(0xFFD1C4E9) to "Light Deep Purple",
                        Color(0xFFC5CAE9) to "Light Indigo",
                        Color(0xFFBBDEFB) to "Light Blue",
                        Color(0xFFB3E5FC) to "Light Cyan",
                        Color(0xFFB2DFDB) to "Light Teal",
                        Color(0xFFC8E6C9) to "Light Green",
                        Color(0xFFF0F4C3) to "Light Lime",
                        Color(0xFFFFF9C4) to "Light Yellow",
                        Color(0xFFFFECB3) to "Light Amber",
                        Color(0xFFFFE0B2) to "Light Orange",
                        Color(0xFFFFCCBC) to "Light Deep Orange"
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        items(fillColors.size) { index ->
                            val (color, name) = fillColors[index]
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        if (selectedShapeIndex >= 0) {
                                            shapes[selectedShapeIndex].fillColor = color
                                            shapes[selectedShapeIndex].fillImageRes = null
                                            forceUpdate++
                                            showFillSheet = false
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(
                                            color = color,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = Color.Gray,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Image mode
                    // List of your drawable resource names
                    val imageResources = listOf(
                        "pattern1",
                        "pattern2",
                        "pattern3",
                        "texture1",
                        "texture2",
                        "texture3",
                        "background1",
                        "background2",
                        "background3"
                        // Add more drawable names as needed
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        items(imageResources.size) { index ->
                            val imageName = imageResources[index]
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        if (selectedShapeIndex >= 0) {
                                            shapes[selectedShapeIndex].fillImageRes = imageName
                                            shapes[selectedShapeIndex].fillColor =
                                                Color.Transparent
                                            forceUpdate++
                                            showFillSheet = false
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = 2.dp,
                                            color = Color.Gray,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(imageName),
                                        contentDescription = imageName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text(
                                    text = imageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditableShapeCanvas(
    isDrawMode: Boolean,
    isDrawSquareMode: Boolean,
    isSelectLineMode: Boolean,
    isSeparateLineMode: Boolean,
    shapes: SnapshotStateList<Shape>,
    onShapeCompleted: () -> Unit,
    onLineSelected: (Shape, Int) -> Unit,
    onShapeSelected: (Shape) -> Unit
) {
    val currentDrawMode = rememberUpdatedState(isDrawMode)
    val currentDrawSquareMode = rememberUpdatedState(isDrawSquareMode)
    val currentSelectLineMode = rememberUpdatedState(isSelectLineMode)
    val currentSeparateLineMode = rememberUpdatedState(isSeparateLineMode)

    var currentShape by remember { mutableStateOf<Shape?>(null) }
    var dragEnd by remember { mutableStateOf<Offset?>(null) }
    var draggingPointIndex by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isDragModeAtStart = false

    var invalidateKey by remember { mutableStateOf(0) }

    LaunchedEffect(currentDrawSquareMode.value) {
        if (currentDrawSquareMode.value) {
            val startX = 300f
            val startY = 300f
            val size = 200f

            val squarePoints = mutableListOf(
                Offset(startX, startY),
                Offset(startX + size, startY),
                Offset(startX + size, startY + size),
                Offset(startX, startY + size)
            )

            shapes.add(
                Shape(
                    points = squarePoints,
                    isClosed = true,
                    isSelected = false,
                    lineColors = mutableListOf(Color.Black, Color.Black, Color.Black, Color.Black),
                    lineWidths = mutableListOf(5f, 5f, 5f, 5f),
                    fillColor = Color.Transparent
                )
            )

            invalidateKey++
        }
    }

    LaunchedEffect(!currentSelectLineMode.value) {
        shapes.forEach { shape ->
            shape.selectedLineIndex = null
        }
        invalidateKey++
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(isDrawMode, isSelectLineMode, isSeparateLineMode) {
                detectTapGestures(
                    onTap = { offset ->

                        // 1. SEPARATE LINE MODE
                        if (currentSeparateLineMode.value) {
                            var pointAdded = false

                            shapes.forEach { shape ->
                                if (shape.points.size > 1) {
                                    for (i in 0 until shape.points.size) {
                                        val start = shape.points[i]
                                        val end = if (shape.isClosed) {
                                            shape.points[(i + 1) % shape.points.size]
                                        } else {
                                            if (i < shape.points.size - 1) shape.points[i + 1] else return@forEach
                                        }

                                        if (isPointNearLine(offset, start, end, 30f)) {
                                            shape.points.add(i + 1, offset)
                                            val currentColor =
                                                shape.lineColors.getOrElse(i) { Color.Black }
                                            val currentWidth = shape.lineWidths.getOrElse(i) { 5f }
                                            shape.lineColors.add(i + 1, currentColor)
                                            shape.lineWidths.add(i + 1, currentWidth)
                                            shapes.forEach { it.isSelected = false }
                                            shape.isSelected = true
                                            pointAdded = true
                                            invalidateKey++
                                            return@detectTapGestures
                                        }
                                    }
                                }
                            }
                            return@detectTapGestures
                        }

                        // 2. DRAW MODE
                        if (currentDrawMode.value) {
                            if (currentShape == null || currentShape?.isClosed == true) {
                                currentShape = Shape(
                                    mutableListOf(offset),
                                    fillColor = Color.Transparent
                                )
                                shapes.add(currentShape!!)
                                invalidateKey++
                                return@detectTapGestures
                            }

                            currentShape?.let { shape ->
                                val first = shape.points.first()
                                if (shape.points.size > 2 &&
                                    hypot(offset.x - first.x, offset.y - first.y) < 50f
                                ) {
                                    shape.isClosed = true
                                    currentShape = null
                                    onShapeCompleted()
                                    invalidateKey++
                                    return@detectTapGestures
                                }

                                shape.points.add(offset)
                                shape.lineColors.add(Color.Black)
                                shape.lineWidths.add(5f)
                                invalidateKey++
                            }
                            return@detectTapGestures
                        }

                        // 3. LINE SELECT MODE
                        if (currentSelectLineMode.value) {
                            var lineSelected = false

                            shapes.forEach { shape ->
                                shape.selectedLineIndex = null

                                if (shape.points.size > 1) {
                                    for (i in 0 until shape.points.size) {
                                        val start = shape.points[i]
                                        val end = shape.points[(i + 1) % shape.points.size]

                                        if (isPointNearLine(offset, start, end, 30f)) {
                                            shape.selectedLineIndex = i
                                            lineSelected = true
                                            shape.isSelected = true
                                            invalidateKey++
                                            onLineSelected(shape, i)
                                            break
                                        }
                                    }
                                }
                                if (lineSelected) return@detectTapGestures
                            }

                            if (!lineSelected) {
                                shapes.forEach {
                                    it.selectedLineIndex = null
                                    it.isSelected = false
                                }
                                invalidateKey++
                            }
                            return@detectTapGestures
                        }

                        // 4. SHAPE SELECTION MODE - Show fill bottom sheet
                        val shapeSelectedIndex = shapes.indexOfFirst { it.isSelected }

                        if (shapeSelectedIndex >= 0) {
                            val shape = shapes[shapeSelectedIndex]
                            val pointIndex = shape.points.indexOfFirst {
                                hypot(it.x - offset.x, it.y - offset.y) < 30f
                            }
                            if (pointIndex < 0) {
                                val newSelectedShapeIndex = shapes.indexOfFirst {
                                    it.isClosed && isPointInsidePolygon(offset, it.points)
                                }
                                if (newSelectedShapeIndex >= 0) {
                                    shapes.forEach { it.isSelected = false }
                                    shapes[newSelectedShapeIndex].isSelected = true
                                    onShapeSelected(shapes[newSelectedShapeIndex])
                                    invalidateKey++
                                } else {
                                    shapes.forEach { it.isSelected = false }
                                    invalidateKey++
                                }
                            }
                        } else {
                            val selectedShapeIndex = shapes.indexOfFirst {
                                it.isClosed && isPointInsidePolygon(offset, it.points)
                            }
                            if (selectedShapeIndex >= 0) {
                                shapes.forEach { it.isSelected = false }
                                shapes[selectedShapeIndex].isSelected = true
                                onShapeSelected(shapes[selectedShapeIndex])
                                invalidateKey++
                            }
                        }
                    }
                )
            }
            .pointerInput(isDrawMode) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragModeAtStart = currentDrawMode.value
                        if (isDragModeAtStart) {
                            if (currentShape == null || currentShape?.isClosed == true) {
                                currentShape = Shape(
                                    mutableListOf(offset),
                                    fillColor = Color.Transparent
                                )
                                shapes.add(currentShape!!)
                            }
                            dragEnd = offset
                        } else {
                            val shapeSelectedIndex = shapes.indexOfFirst { it.isSelected }
                            if (shapeSelectedIndex >= 0) {
                                val shape = shapes[shapeSelectedIndex]
                                val pointIndex = shape.points.indexOfFirst {
                                    hypot(it.x - offset.x, it.y - offset.y) < 30f
                                }
                                if (pointIndex >= 0) {
                                    draggingPointIndex = shapeSelectedIndex to pointIndex
                                }
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        if (isDragModeAtStart) {
                            dragEnd = change.position
                        } else {
                            draggingPointIndex?.let { (shapeIndex, pointIndex) ->
                                shapes[shapeIndex].points[pointIndex] = change.position
                                invalidateKey++
                            }
                        }
                    },
                    onDragEnd = {
                        if (isDragModeAtStart) {
                            currentShape?.let { shape ->
                                val end = dragEnd
                                if (end != null) {
                                    if (shape.points.isNotEmpty() &&
                                        hypot(
                                            end.x - shape.points.first().x,
                                            end.y - shape.points.first().y
                                        ) < 50f
                                    ) {
                                        shape.isClosed = true
                                        currentShape = null
                                        onShapeCompleted()
                                        invalidateKey++
                                    } else {
                                        shape.points.add(end)
                                        shape.lineColors.add(Color.Black)
                                        shape.lineWidths.add(5f)
                                        invalidateKey++
                                    }
                                }
                            }
                            dragEnd = null
                        } else {
                            draggingPointIndex = null
                        }
                    }
                )
            }
    ) {
        val _validate = invalidateKey

        shapes.forEach { shape ->
            // Draw fill (color or image) first (behind lines)
            if (shape.isClosed) {
                val path = Path().apply {
                    if (shape.points.isNotEmpty()) {
                        moveTo(shape.points[0].x, shape.points[0].y)
                        for (i in 1 until shape.points.size) {
                            lineTo(shape.points[i].x, shape.points[i].y)
                        }
                        close()
                    }
                }

                // Draw image fill if available
                if (shape.fillImageRes != null) {
                    clipPath(path) {
                        // Get the bounds of the shape
                        val bounds = calculateBounds(shape.points)

                        drawIntoCanvas { canvas ->
                            // You'll need to load the image as ImageBitmap
                            // This is a placeholder - actual implementation depends on your image loading
                            try {
                                val imageBitmap = loadImageBitmap(shape.fillImageRes!!)
                                canvas.drawImageRect(
                                    image = imageBitmap,
                                    srcOffset = IntOffset.Zero,
                                    srcSize = IntSize(imageBitmap.width, imageBitmap.height),
                                    dstOffset = IntOffset(bounds.left.toInt(), bounds.top.toInt()),
                                    dstSize = IntSize(bounds.width.toInt(), bounds.height.toInt()),
                                    paint = Paint()
                                )
                            } catch (e: Exception) {
                                // Fallback to color if image fails
                                drawPath(path = path, color = Color.LightGray)
                            }
                        }
                    }
                } else if (shape.fillColor != Color.Transparent) {
                    // Draw solid color fill
                    drawPath(
                        path = path,
                        color = shape.fillColor
                    )
                }
            }

            // Draw lines
            if (shape.points.size > 1) {
                for (i in 0 until shape.points.size - 1) {
                    val isSelectedLine = shape.selectedLineIndex == i
                    drawLine(
                        color = if (isSelectedLine) Color.Red else shape.lineColors.getOrElse(
                            i
                        ) { Color.Black },
                        start = shape.points[i],
                        end = shape.points[i + 1],
                        strokeWidth = if (isSelectedLine) shape.lineWidths.getOrElse(i) { 5f } + 3f
                        else shape.lineWidths.getOrElse(i) { 5f }
                    )
                }

                if (shape.isClosed) {
                    val lastIndex = shape.points.size - 1
                    val isSelectedLine = shape.selectedLineIndex == lastIndex
                    drawLine(
                        color = if (isSelectedLine) Color.Red else shape.lineColors.getOrElse(
                            lastIndex
                        ) { Color.Black },
                        start = shape.points.last(),
                        end = shape.points.first(),
                        strokeWidth = if (isSelectedLine) shape.lineWidths.getOrElse(lastIndex) { 5f } + 3f
                        else shape.lineWidths.getOrElse(lastIndex) { 5f }
                    )
                }
            }

            // Draw selection handles
            if (shape.isSelected && shape.isClosed && !currentDrawMode.value) {
                shape.points.forEach { point ->
                    drawRoundRect(
                        color = Color.Blue,
                        topLeft = Offset(point.x - 15f, point.y - 15f),
                        size = Size(30f, 30f),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Fill
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(point.x - 15f, point.y - 15f),
                        size = Size(30f, 30f),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 2f)
                    )
                }
            }
        }

        // Draw current shape being drawn
        currentShape?.let { shape ->
            if (currentDrawMode.value && !shape.isClosed && dragEnd != null && shape.points.isNotEmpty()) {
                drawLine(
                    color = Color.Red,
                    start = shape.points.last(),
                    end = dragEnd!!,
                    strokeWidth = 3f
                )
            }
            if (currentDrawMode.value && !shape.isClosed && shape.points.isNotEmpty()) {
                drawCircle(
                    color = Color.Green,
                    radius = 10f,
                    center = shape.points.first()
                )
            }
        }
    }
}

// Helper function to calculate bounds of a shape
private fun calculateBounds(points: List<Offset>): Rect {
    val minX = points.minOfOrNull { it.x } ?: 0f
    val maxX = points.maxOfOrNull { it.x } ?: 0f
    val minY = points.minOfOrNull { it.y } ?: 0f
    val maxY = points.maxOfOrNull { it.y } ?: 0f
    return Rect(minX, minY, maxX, maxY)
}

// Helper function to load image bitmap - platform specific
@Composable
expect fun loadImageBitmap(resourceName: String): ImageBitmap
*/


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapeEditorScreen(modifier: Modifier = Modifier) {
    var isDrawMode by remember { mutableStateOf(false) }
    var isDrawSquareMode by remember { mutableStateOf(false) }
    var isSelectLIneMode by remember { mutableStateOf(false) }
    var isSeparateLineMode by remember { mutableStateOf(false) }
    var showLineEditSheet by remember { mutableStateOf(false) }

    // currently selected line and shape
    var selectedShapeIndex by remember { mutableStateOf(-1) }
    var selectedLineIndex by remember { mutableStateOf<Int?>(null) }

    // selected color and stroke width
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var selectedStrokeWidth by remember { mutableStateOf(5f) }
    var shadeList by remember { mutableStateOf(listOf<Color>()) }

    // Key state to track shapes for updates
    val shapes = remember { mutableStateListOf<Shape>() }
    var forceUpdate by remember { mutableStateOf(0) }

    Box(modifier = modifier.fillMaxSize()) {
        EditableShapeCanvas(
            isDrawMode = isDrawMode,
            isDrawSquareMode = isDrawSquareMode,
            isSelectLineMode = isSelectLIneMode,
            isSeparateLineMode = isSeparateLineMode,
            shapes = shapes,
            onShapeCompleted = {
                isDrawMode = false
            },
            onLineSelected = { shape, i ->
                selectedShapeIndex = shapes.indexOf(shape)
                selectedLineIndex = i
                selectedColor = shape.lineColors.getOrNull(i) ?: Color.Black
                selectedStrokeWidth = shape.lineWidths.getOrNull(i) ?: 5f

                showLineEditSheet = true
            }
        )

        // Toolbar at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.LightGray)
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { isDrawMode = !isDrawMode },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDrawMode) Color.Green else Color.Gray
                )
            ) {
                Text(text = if (isDrawMode) "Drawing…" else "Draw")
            }

            Button(
                onClick = { isDrawSquareMode = !isDrawSquareMode },
            ) {
                Text(text = "Drawing Square")
            }

            Button(
                onClick = { isSelectLIneMode = !isSelectLIneMode },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelectLIneMode) Color.Green else Color.Gray
                )
            ) {
                Text(text = "Select Line")
            }

            Button(
                onClick = { isSeparateLineMode = !isSeparateLineMode },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSeparateLineMode) Color.Green else Color.Gray
                )
            ) {
                Text(text = "Separate Line")
            }
        }
    }

    if (showLineEditSheet) {
        ModalBottomSheet(onDismissRequest = { showLineEditSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Choose Line Color", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                // Color palette
                val colorPalette = listOf(
                    Color.Red,
                    Color.Green,
                    Color.Blue,
                    Color.Yellow,
                    Color.Cyan,
                    Color.Magenta,
                    Color.Black
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colorPalette.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == color) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColor = color
                                    shadeList = generateShades(color)

                                    // Update the line color
                                    if (selectedShapeIndex >= 0 && selectedLineIndex != null) {
                                        shapes[selectedShapeIndex].lineColors[selectedLineIndex!!] =
                                            color
                                        forceUpdate++
                                    }
                                }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Shades")
                Spacer(Modifier.height(8.dp))

                // Shades row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    shadeList.forEach { shade ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(shade)
                                .border(
                                    width = if (selectedColor == shade) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColor = shade

                                    // Update the line color with shade
                                    if (selectedShapeIndex >= 0 && selectedLineIndex != null) {
                                        shapes[selectedShapeIndex].lineColors[selectedLineIndex!!] =
                                            shade
                                        forceUpdate++
                                    }
                                }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Line Thickness: ${selectedStrokeWidth.toInt()}px")
                Slider(
                    value = selectedStrokeWidth,
                    onValueChange = { newValue ->
                        selectedStrokeWidth = newValue

                        // Update the line width
                        if (selectedShapeIndex >= 0 && selectedLineIndex != null) {
                            shapes[selectedShapeIndex].lineWidths[selectedLineIndex!!] = newValue
                            forceUpdate++
                        }
                    },
                    valueRange = 2f..12f
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { showLineEditSheet = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Done")
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableShapeCanvas(
    isDrawMode: Boolean,
    isDrawSquareMode: Boolean,
    isSelectLineMode: Boolean,
    isSeparateLineMode: Boolean,
    shapes: SnapshotStateList<Shape>,
    onShapeCompleted: () -> Unit,
    onLineSelected: (Shape, Int) -> Unit
) {
    val currentDrawMode = rememberUpdatedState(isDrawMode)
    val currentDrawSquareMode = rememberUpdatedState(isDrawSquareMode)
    val currentSelectLineMode = rememberUpdatedState(isSelectLineMode)
    val currentSeparateLineMode = rememberUpdatedState(isSeparateLineMode)

    var currentShape by remember { mutableStateOf<Shape?>(null) }
    var dragEnd by remember { mutableStateOf<Offset?>(null) }
    var draggingPointIndex by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isDragModeAtStart = false

    var invalidateKey by remember { mutableStateOf(0) }

    // Bottom sheet state
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedShapeForColor by remember { mutableStateOf<Shape?>(null) }

    // Available colors for fill
    val fillColors = listOf(
        Color.Transparent to "None",
        Color(0xFFFFCDD2) to "Light Red",
        Color(0xFFF8BBD0) to "Light Pink",
        Color(0xFFE1BEE7) to "Light Purple",
        Color(0xFFD1C4E9) to "Light Deep Purple",
        Color(0xFFC5CAE9) to "Light Indigo",
        Color(0xFFBBDEFB) to "Light Blue",
        Color(0xFFB3E5FC) to "Light Cyan",
        Color(0xFFB2DFDB) to "Light Teal",
        Color(0xFFC8E6C9) to "Light Green",
        Color(0xFFF0F4C3) to "Light Lime",
        Color(0xFFFFF9C4) to "Light Yellow",
        Color(0xFFFFECB3) to "Light Amber",
        Color(0xFFFFE0B2) to "Light Orange",
        Color(0xFFFFCCBC) to "Light Deep Orange"
    )

    LaunchedEffect(currentDrawSquareMode.value) {
        if (currentDrawSquareMode.value) {
            val startX = 300f
            val startY = 300f
            val size = 200f

            val squarePoints = mutableListOf(
                Offset(startX, startY),
                Offset(startX + size, startY),
                Offset(startX + size, startY + size),
                Offset(startX, startY + size)
            )

            shapes.add(
                Shape(
                    points = squarePoints,
                    isClosed = true,
                    isSelected = false,
                    lineColors = mutableListOf(Color.Black, Color.Black, Color.Black, Color.Black),
                    lineWidths = mutableListOf(5f, 5f, 5f, 5f),
                    fillColor = Color.Transparent
                )
            )

            invalidateKey++
        }
    }

    LaunchedEffect(!currentSelectLineMode.value) {
        shapes.forEach { shape ->
            shape.selectedLineIndex = null
        }
        invalidateKey++
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(isDrawMode, isSelectLineMode, isSeparateLineMode) {
                    detectTapGestures(
                        onTap = { offset ->

                            // 1. SEPARATE LINE MODE
                            if (currentSeparateLineMode.value) {
                                var pointAdded = false

                                shapes.forEach { shape ->
                                    if (shape.points.size > 1) {
                                        for (i in 0 until shape.points.size) {
                                            val start = shape.points[i]
                                            val end = if (shape.isClosed) {
                                                shape.points[(i + 1) % shape.points.size]
                                            } else {
                                                if (i < shape.points.size - 1) shape.points[i + 1] else return@forEach
                                            }

                                            if (isPointNearLine(offset, start, end, 30f)) {
                                                shape.points.add(i + 1, offset)
                                                val currentColor =
                                                    shape.lineColors.getOrElse(i) { Color.Black }
                                                val currentWidth =
                                                    shape.lineWidths.getOrElse(i) { 5f }
                                                shape.lineColors.add(i + 1, currentColor)
                                                shape.lineWidths.add(i + 1, currentWidth)
                                                shapes.forEach { it.isSelected = false }
                                                shape.isSelected = true
                                                pointAdded = true
                                                invalidateKey++
                                                return@detectTapGestures
                                            }
                                        }
                                    }
                                }
                                return@detectTapGestures
                            }

                            // 2. DRAW MODE
                            if (currentDrawMode.value) {
                                if (currentShape == null || currentShape?.isClosed == true) {
                                    currentShape = Shape(
                                        mutableListOf(offset),
                                        fillColor = Color.Transparent
                                    )
                                    shapes.add(currentShape!!)
                                    invalidateKey++
                                    return@detectTapGestures
                                }

                                currentShape?.let { shape ->
                                    val first = shape.points.first()
                                    if (shape.points.size > 2 &&
                                        hypot(offset.x - first.x, offset.y - first.y) < 50f
                                    ) {
                                        shape.isClosed = true
                                        currentShape = null
                                        onShapeCompleted()
                                        invalidateKey++
                                        return@detectTapGestures
                                    }

                                    shape.points.add(offset)
                                    shape.lineColors.add(Color.Black)
                                    shape.lineWidths.add(5f)
                                    invalidateKey++
                                }
                                return@detectTapGestures
                            }

                            // 3. LINE SELECT MODE
                            if (currentSelectLineMode.value) {
                                var lineSelected = false

                                shapes.forEach { shape ->
                                    shape.selectedLineIndex = null

                                    if (shape.points.size > 1) {
                                        for (i in 0 until shape.points.size) {
                                            val start = shape.points[i]
                                            val end = shape.points[(i + 1) % shape.points.size]

                                            if (isPointNearLine(offset, start, end, 30f)) {
                                                shape.selectedLineIndex = i
                                                lineSelected = true
                                                shape.isSelected = true
                                                invalidateKey++
                                                onLineSelected(shape, i)
                                                break
                                            }
                                        }
                                    }
                                    if (lineSelected) return@detectTapGestures
                                }

                                if (!lineSelected) {
                                    shapes.forEach {
                                        it.selectedLineIndex = null
                                        it.isSelected = false
                                    }
                                    invalidateKey++
                                }
                                return@detectTapGestures
                            }

                            // 4. SHAPE SELECTION MODE - Show bottom sheet when shape selected
                            val shapeSelectedIndex = shapes.indexOfFirst { it.isSelected }

                            if (shapeSelectedIndex >= 0) {
                                val shape = shapes[shapeSelectedIndex]
                                val pointIndex = shape.points.indexOfFirst {
                                    hypot(it.x - offset.x, it.y - offset.y) < 30f
                                }
                                if (pointIndex < 0) {
                                    val newSelectedShapeIndex = shapes.indexOfFirst {
                                        it.isClosed && isPointInsidePolygon(offset, it.points)
                                    }
                                    if (newSelectedShapeIndex >= 0) {
                                        shapes.forEach { it.isSelected = false }
                                        shapes[newSelectedShapeIndex].isSelected = true
                                        selectedShapeForColor = shapes[newSelectedShapeIndex]
                                        showBottomSheet = true
                                        invalidateKey++
                                    } else {
                                        shapes.forEach { it.isSelected = false }
                                        invalidateKey++
                                    }
                                }
                            } else {
                                val selectedShapeIndex = shapes.indexOfFirst {
                                    it.isClosed && isPointInsidePolygon(offset, it.points)
                                }
                                if (selectedShapeIndex >= 0) {
                                    shapes.forEach { it.isSelected = false }
                                    shapes[selectedShapeIndex].isSelected = true
                                    selectedShapeForColor = shapes[selectedShapeIndex]
                                    showBottomSheet = true
                                    invalidateKey++
                                }
                            }
                        }
                    )
                }
                .pointerInput(isDrawMode) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragModeAtStart = currentDrawMode.value
                            if (isDragModeAtStart) {
                                if (currentShape == null || currentShape?.isClosed == true) {
                                    currentShape = Shape(
                                        mutableListOf(offset),
                                        fillColor = Color.Transparent
                                    )
                                    shapes.add(currentShape!!)
                                }
                                dragEnd = offset
                            } else {
                                val shapeSelectedIndex = shapes.indexOfFirst { it.isSelected }
                                if (shapeSelectedIndex >= 0) {
                                    val shape = shapes[shapeSelectedIndex]
                                    val pointIndex = shape.points.indexOfFirst {
                                        hypot(it.x - offset.x, it.y - offset.y) < 30f
                                    }
                                    if (pointIndex >= 0) {
                                        draggingPointIndex = shapeSelectedIndex to pointIndex
                                    }
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            if (isDragModeAtStart) {
                                dragEnd = change.position
                            } else {
                                draggingPointIndex?.let { (shapeIndex, pointIndex) ->
                                    shapes[shapeIndex].points[pointIndex] = change.position
                                    invalidateKey++
                                }
                            }
                        },
                        onDragEnd = {
                            if (isDragModeAtStart) {
                                currentShape?.let { shape ->
                                    val end = dragEnd
                                    if (end != null) {
                                        if (shape.points.isNotEmpty() &&
                                            hypot(
                                                end.x - shape.points.first().x,
                                                end.y - shape.points.first().y
                                            ) < 50f
                                        ) {
                                            shape.isClosed = true
                                            currentShape = null
                                            onShapeCompleted()
                                            invalidateKey++
                                        } else {
                                            shape.points.add(end)
                                            shape.lineColors.add(Color.Black)
                                            shape.lineWidths.add(5f)
                                            invalidateKey++
                                        }
                                    }
                                }
                                dragEnd = null
                            } else {
                                draggingPointIndex = null
                            }
                        }
                    )
                }
        ) {
            val _validate = invalidateKey

            shapes.forEach { shape ->
                // Draw fill color first (behind lines)
                if (shape.isClosed && shape.fillColor != Color.Transparent) {
                    val path = Path().apply {
                        if (shape.points.isNotEmpty()) {
                            moveTo(shape.points[0].x, shape.points[0].y)
                            for (i in 1 until shape.points.size) {
                                lineTo(shape.points[i].x, shape.points[i].y)
                            }
                            close()
                        }
                    }
                    drawPath(
                        path = path,
                        color = shape.fillColor
                    )
                }

                // Draw lines
                if (shape.points.size > 1) {
                    for (i in 0 until shape.points.size - 1) {
                        val isSelectedLine = shape.selectedLineIndex == i
                        drawLine(
                            color = if (isSelectedLine) Color.Red else shape.lineColors.getOrElse(
                                i
                            ) { Color.Black },
                            start = shape.points[i],
                            end = shape.points[i + 1],
                            strokeWidth = if (isSelectedLine) shape.lineWidths.getOrElse(i) { 5f } + 3f
                            else shape.lineWidths.getOrElse(i) { 5f }
                        )
                    }

                    if (shape.isClosed) {
                        val lastIndex = shape.points.size - 1
                        val isSelectedLine = shape.selectedLineIndex == lastIndex
                        drawLine(
                            color = if (isSelectedLine) Color.Red else shape.lineColors.getOrElse(
                                lastIndex
                            ) { Color.Black },
                            start = shape.points.last(),
                            end = shape.points.first(),
                            strokeWidth = if (isSelectedLine) shape.lineWidths.getOrElse(lastIndex) { 5f } + 3f
                            else shape.lineWidths.getOrElse(lastIndex) { 5f }
                        )
                    }
                }

                if (shape.isSelected && shape.isClosed && !currentDrawMode.value) {
                    shape.points.forEach { point ->
                        drawRoundRect(
                            color = Color.Blue,
                            topLeft = Offset(point.x - 15f, point.y - 15f),
                            size = Size(30f, 30f),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Fill
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(point.x - 15f, point.y - 15f),
                            size = Size(30f, 30f),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }

            currentShape?.let { shape ->
                if (currentDrawMode.value && !shape.isClosed && dragEnd != null && shape.points.isNotEmpty()) {
                    drawLine(
                        color = Color.Red,
                        start = shape.points.last(),
                        end = dragEnd!!,
                        strokeWidth = 3f
                    )
                }
                if (currentDrawMode.value && !shape.isClosed && shape.points.isNotEmpty()) {
                    drawCircle(
                        color = Color.Green,
                        radius = 10f,
                        center = shape.points.first()
                    )
                }
            }
        }

        // Bottom Sheet for color selection
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Select Fill Color",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        items(fillColors) { (color, name) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        selectedShapeForColor?.fillColor = color
                                        invalidateKey++
                                        showBottomSheet = false
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(
                                            color = color,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = Color.Gray,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Don't forget to update your Shape data class to include fillColor
data class Shape(
    val points: MutableList<Offset>,
    var isClosed: Boolean = false,
    var isSelected: Boolean = false,
    val lineColors: MutableList<Color> = mutableListOf(),
    val lineWidths: MutableList<Float> = mutableListOf(),
    var selectedLineIndex: Int? = null,
    var fillColor: Color = Color.Transparent,
)

fun isPointInsidePolygon(point: Offset, polygon: List<Offset>): Boolean {
    if (polygon.size < 3) return false // must be at least a triangle

    var inside = false
    var j = polygon.lastIndex
    for (i in polygon.indices) {
        val xi = polygon[i].x
        val yi = polygon[i].y
        val xj = polygon[j].x
        val yj = polygon[j].y

        val intersect = ((yi > point.y) != (yj > point.y)) &&
                (point.x < (xj - xi) * (point.y - yi) / (yj - yi) + xi)

        if (intersect) inside = !inside
        j = i
    }
    return inside
}

private fun isPointNearLine(point: Offset, start: Offset, end: Offset, threshold: Float): Boolean {
    val lineLength = hypot(end.x - start.x, end.y - start.y)
    if (lineLength == 0f) return false

    // Project point onto the line segment
    val t =
        ((point.x - start.x) * (end.x - start.x) + (point.y - start.y) * (end.y - start.y)) / (lineLength * lineLength)
    if (t < 0f || t > 1f) return false

    val projection = Offset(
        start.x + t * (end.x - start.x),
        start.y + t * (end.y - start.y)
    )

    val distance = hypot(point.x - projection.x, point.y - projection.y)
    return distance <= threshold
}




