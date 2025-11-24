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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
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

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapeEditorScreen() {
    var isDrawMode by remember { mutableStateOf(false) }
    var isDrawSquareMode by remember { mutableStateOf(false) }
    var isSelectLIneMode by remember { mutableStateOf(false) }
    var showLineEditSheet by remember { mutableStateOf(false) }
// currently selected line and shape
    var selectedShapeIndex by remember { mutableStateOf(-1) }
    var selectedLineIndex by remember { mutableStateOf<Int?>(null) }

// selected color and stroke width
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var selectedStrokeWidth by remember { mutableStateOf(5f) }
    var shadeList by remember { mutableStateOf(listOf<Color>()) }


    Box(modifier = Modifier.fillMaxSize()) {
        EditableShapeCanvas(
            isDrawMode = isDrawMode,
            isDrawSquareMode = isDrawSquareMode,
            isSelectLineMode = isSelectLIneMode,
            onShapeCompleted = {
                // Turn off draw mode when shape is completed
                isDrawMode = false
            },
            onLineSelected = { shapes, shape, i ->
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
            // Later: add more buttons here (Delete, Color, Move, etc.)
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

                // 🎨 First row - color palette
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
                                .clickable {
                                    selectedColor = color
                                    shadeList = generateShades(color)
                                }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Shades")
                Spacer(Modifier.height(8.dp))

                // 🌈 Second row - shades
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
                                .clickable {
                                    selectedColor = shade
//                                    updateSelectedLine(shapes, selectedShapeIndex, selectedLineIndex, color = shade)
                                }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Line Thickness")
                Slider(
                    value = selectedStrokeWidth,
                    onValueChange = { newValue ->
                        selectedStrokeWidth = newValue
//                        updateSelectedLine(shapes, selectedShapeIndex, selectedLineIndex, stroke = newValue)
                    },
                    valueRange = 2f..12f
                )
            }
        }
    }

}
*/

fun generateShades(baseColor: Color): List<Color> {
    return (1..5).map { i ->
        baseColor.copy(alpha = 0.2f * i)
    }
}


data class Shape(
    val points: MutableList<Offset>,
    var isClosed: Boolean = false,
    var isSelected: Boolean = false,
    var selectedLineIndex: Int? = null,
    val lineColors: MutableList<Color> = mutableListOf(),
    val lineWidths: MutableList<Float> = mutableListOf()
)

@Composable
fun EditableShapeCanvas(
    isDrawMode: Boolean,
    isDrawSquareMode: Boolean,
    isSelectLineMode: Boolean,
    onShapeCompleted: () -> Unit,
    onLineSelected: (SnapshotStateList<Shape>, Shape, Int) -> Unit
) {


    val currentDrawMode = rememberUpdatedState(isDrawMode)
    val currentDrawSquareMode = rememberUpdatedState(isDrawSquareMode)
    val currentSelectLineMode = rememberUpdatedState(isSelectLineMode)
    val shapes = remember { mutableStateListOf<Shape>() }
    var currentShape by remember { mutableStateOf<Shape?>(null) }
    var dragEnd by remember { mutableStateOf<Offset?>(null) }
    var draggingPointIndex by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isDragModeAtStart = false
    // bottom sheet visibility


    var invalidateKey by remember { mutableStateOf(0) }
    LaunchedEffect(currentDrawSquareMode.value) {
        if (currentDrawSquareMode.value) {
            // Define square size and position
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
                Shape(points = squarePoints, isClosed = true, isSelected = false)
            )

            invalidateKey++  // trigger redraw

        }
    }
    LaunchedEffect(!currentSelectLineMode.value) {
        shapes.forEach { shape ->
            shape.selectedLineIndex = null // reset previous selection
        }
        LogKat.d("select_line", "Select line false triggered")
        invalidateKey++ // force redraw
    }
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(isDrawMode) {
                // Handle clicks when not in draw mode
                if (!isDrawMode) {
                    detectTapGestures(
                        onTap = { offset ->
                            if (currentSelectLineMode.value) {
                                var lineSelected = false

                                shapes.forEach { shape ->
                                    shape.selectedLineIndex = null // reset previous selection

                                    if (shape.points.size > 1) {
                                        for (i in 0 until shape.points.size) {
                                            val start = shape.points[i]
                                            val end =
                                                shape.points[(i + 1) % shape.points.size] // wrap for closed shape

                                            if (isPointNearLine(
                                                    offset,
                                                    start,
                                                    end,
                                                    threshold = 30f
                                                )
                                            ) {
                                                shape.selectedLineIndex = i
                                                lineSelected = true
                                                shape.isSelected = true
                                                invalidateKey++ // force redraw
                                                LogKat.d(
                                                    "line_select",
                                                    "Line $i selected in shape: ${
                                                        shapes.indexOf(shape)
                                                    }"
                                                )
                                                onLineSelected.invoke(shapes, shape, i)
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

                                return@detectTapGestures // stop further processing when in line-select mode


                            } else {

                                val shapeSelectedIndex = shapes.indexOfFirst { shape ->
                                    shape.isSelected
                                }
                                LogKat.d(
                                    "points_custom_log",
                                    "shapeSelectedIndex = $shapeSelectedIndex"
                                )

                                if (shapeSelectedIndex >= 0) {
                                    LogKat.d("points_custom_log", "shape already selected found")
                                    val shape = shapes[shapeSelectedIndex]
                                    val pointIndex = shape.points.indexOfFirst {
                                        hypot(it.x - offset.x, it.y - offset.y) < 30f
                                    }
                                    if (pointIndex >= 0) {
                                        // User clicked on a point - could add point editing here
                                        LogKat.d("points_custom_log", "Point clicked: $pointIndex")
                                    } else {
                                        // User clicked elsewhere - check if they want to select another shape
                                        val newSelectedShapeIndex = shapes.indexOfFirst { shape ->
                                            shape.isClosed && isPointInsidePolygon(
                                                offset,
                                                shape.points
                                            )
                                        }

                                        if (newSelectedShapeIndex >= 0) {
                                            // Select the new shape
                                            shapes.forEach { it.isSelected = false }
                                            shapes[newSelectedShapeIndex].isSelected = true
                                            invalidateKey++ // Force redraw when selection changes
                                        } else {
                                            // Clicked outside all shapes - deselect
                                            shapes.forEach { it.isSelected = false }
                                            invalidateKey++ // Force redraw when deselecting
                                        }
                                    }
                                } else {
                                    val selectedShapeIndex = shapes.indexOfFirst { shape ->
                                        LogKat.d(
                                            "points_custom_log",
                                            "shape status = ${shape.isClosed}"
                                        )
                                        LogKat.d(
                                            "points_custom_log",
                                            "shape inside status = ${
                                                isPointInsidePolygon(
                                                    offset,
                                                    shape.points
                                                )
                                            }"
                                        )
                                        shape.isClosed && isPointInsidePolygon(offset, shape.points)
                                    }
                                    LogKat.d(
                                        "points_custom_log",
                                        "selectedShapeIndex = $selectedShapeIndex"
                                    )
                                    if (selectedShapeIndex >= 0) {
                                        shapes.forEach { it.isSelected = false }
                                        shapes[selectedShapeIndex].isSelected = true
                                        invalidateKey++ // Force redraw when selection changes
                                    } else {
                                        shapes.forEach { it.isSelected = false }
                                        invalidateKey++ // Force redraw when deselecting
                                    }
                                }
                            }
                        }
                    )
                }
            }
            .pointerInput(isDrawMode) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragModeAtStart = currentDrawMode.value
                        if (isDragModeAtStart) {
                            if (currentShape == null || currentShape?.isClosed == true) {
                                currentShape = Shape(mutableListOf(offset))
                                shapes.add(currentShape!!)
                            }
                            dragEnd = offset
                        } else {
                            // In edit mode, only allow dragging points
                            val shapeSelectedIndex = shapes.indexOfFirst { shape ->
                                shape.isSelected
                            }
                            if (shapeSelectedIndex >= 0) {
                                val shape = shapes[shapeSelectedIndex]
                                val pointIndex = shape.points.indexOfFirst {
                                    hypot(it.x - offset.x, it.y - offset.y) < 30f
                                }
                                if (pointIndex >= 0) {
                                    // User started dragging a point
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
                                invalidateKey++ // CRITICAL: Force redraw when point moves
                            }
                        }
                    },
                    onDragEnd = {
                        if (isDragModeAtStart) {
                            currentShape?.let { shape ->
                                val end = dragEnd
                                if (end != null) {
                                    if (
                                        shape.points.isNotEmpty() &&
                                        hypot(
                                            end.x - shape.points.first().x,
                                            end.y - shape.points.first().y
                                        ) < 50f
                                    ) {
                                        shape.isClosed = true
                                        currentShape = null
                                        onShapeCompleted()
                                        invalidateKey++ // Force redraw when shape closes
                                    } else {
                                        shape.points.add(end)
                                        shape.lineColors.add(Color.Black)
                                        shape.lineWidths.add(5f)
                                        invalidateKey++ // Force redraw when point added
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
    )
    {
        val _validate = invalidateKey

        shapes.forEach { shape ->
            if (shape.points.size > 1) {
                // ✅ Draw lines of the shape, highlighting the selected line
                for (i in 0 until shape.points.size - 1) {
                    val isSelectedLine = shape.selectedLineIndex == i
                    drawLine(
                        color = if (isSelectedLine) Color.Red else shape.lineColors.getOrElse(i) { Color.Black },
                        start = shape.points[i],
                        end = shape.points[i + 1],
                        strokeWidth = if (isSelectedLine) 8f else 5f
                    )
                }

                // ✅ Draw closing line for closed shape
                if (shape.isClosed) {
                    val isSelectedLine = shape.selectedLineIndex == shape.points.size - 1
                    drawLine(
                        color = if (isSelectedLine) Color.Red else Color.Black,
                        start = shape.points.last(),
                        end = shape.points.first(),
                        strokeWidth = if (isSelectedLine) 8f else 5f
                    )
                }
            }

            // 🔍 Debug logs for development
            LogKat.d("round_track", "isSelected = ${shape.isSelected}")
            LogKat.d("round_track", "isClosed = ${shape.isClosed}")
            LogKat.d("round_track", "selectedLineIndex = ${shape.selectedLineIndex}")
            LogKat.d("round_track", "--------------------------------\n")

            // ✅ Draw selection handles when shape is selected (edit mode)
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

// ✅ Preview for current drawing (your existing logic)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapeEditorScreen() {
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

// Updated EditableShapeCanvas function signature
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
                    lineWidths = mutableListOf(5f, 5f, 5f, 5f)
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

    /*    Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(isDrawMode, isSelectLineMode) {
                    detectTapGestures(
                        onTap = { offset ->

                            // -----------------------------
                            // -----------------------------
    // 1. DRAW MODE → Tap = add first point or next points
    // -----------------------------
                            if (currentDrawMode.value) {

                                // First point OR new shape
                                if (currentShape == null || currentShape?.isClosed == true) {
                                    currentShape = Shape(mutableListOf(offset))
                                    shapes.add(currentShape!!)
                                    invalidateKey++
                                    return@detectTapGestures
                                }

                                currentShape?.let { shape ->

                                    // Check if tap is near starting point → close shape
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

                                    // Add new vertex — THIS WAS MISSING
                                    shape.points.add(offset)
                                    shape.lineColors.add(Color.Black)
                                    shape.lineWidths.add(5f)
                                    invalidateKey++
                                }

                                return@detectTapGestures
                            }

                            // -----------------------------
                            // 2. LINE SELECT MODE (original)
                            // -----------------------------
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

                            // -----------------------------
                            // 3. SHAPE / POINT SELECTION MODE (original)
                            // -----------------------------
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
                                    currentShape = Shape(mutableListOf(offset))
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
        )
        {
            val _validate = invalidateKey

            shapes.forEach { shape ->
                if (shape.points.size > 1) {
                    for (i in 0 until shape.points.size - 1) {
                        val isSelectedLine = shape.selectedLineIndex == i
                        drawLine(
                            color = if (isSelectedLine) Color.Red else shape.lineColors.getOrElse(i) { Color.Black },
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
        }*/

    /*    Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(isDrawMode, isSelectLineMode, isSeparateLineMode) {
                    detectTapGestures(
                        onTap = { offset ->

                            // -----------------------------
                            // 1. SEPARATE LINE MODE → Add point on existing line
                            // -----------------------------
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

                                            // Check if tap is near this line segment
                                            if (isPointNearLine(offset, start, end, 30f)) {
                                                // Insert the new point at position i+1
                                                shape.points.add(i + 1, offset)

                                                // Also add corresponding line properties
                                                val currentColor = shape.lineColors.getOrElse(i) { Color.Black }
                                                val currentWidth = shape.lineWidths.getOrElse(i) { 5f }

                                                shape.lineColors.add(i + 1, currentColor)
                                                shape.lineWidths.add(i + 1, currentWidth)

                                                pointAdded = true
                                                invalidateKey++
                                                return@detectTapGestures
                                            }
                                        }
                                    }
                                }

                                if (!pointAdded) {
                                    // Optionally show a message that no line was found
                                }

                                return@detectTapGestures
                            }

                            // -----------------------------
                            // 2. DRAW MODE → Tap = add first point or next points
                            // -----------------------------
                            if (currentDrawMode.value) {

                                // First point OR new shape
                                if (currentShape == null || currentShape?.isClosed == true) {
                                    currentShape = Shape(mutableListOf(offset))
                                    shapes.add(currentShape!!)
                                    invalidateKey++
                                    return@detectTapGestures
                                }

                                currentShape?.let { shape ->

                                    // Check if tap is near starting point → close shape
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

                                    // Add new vertex
                                    shape.points.add(offset)
                                    shape.lineColors.add(Color.Black)
                                    shape.lineWidths.add(5f)
                                    invalidateKey++
                                }

                                return@detectTapGestures
                            }

                            // -----------------------------
                            // 3. LINE SELECT MODE
                            // -----------------------------
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

                            // -----------------------------
                            // 4. SHAPE / POINT SELECTION MODE
                            // -----------------------------
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
                                    currentShape = Shape(mutableListOf(offset))
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
        )
        {
            val _validate = invalidateKey

            shapes.forEach { shape ->
                if (shape.points.size > 1) {
                    for (i in 0 until shape.points.size - 1) {
                        val isSelectedLine = shape.selectedLineIndex == i
                        drawLine(
                            color = if (isSelectedLine) Color.Red else shape.lineColors.getOrElse(i) { Color.Black },
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
        }*/

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(isDrawMode, isSelectLineMode, isSeparateLineMode) {
                detectTapGestures(
                    onTap = { offset ->

                        // -----------------------------
                        // 1. SEPARATE LINE MODE → Add point on existing line
                        // -----------------------------
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

                                        // Check if tap is near this line segment
                                        if (isPointNearLine(offset, start, end, 30f)) {
                                            // Insert the new point at position i+1
                                            shape.points.add(i + 1, offset)

                                            // Also add corresponding line properties
                                            val currentColor =
                                                shape.lineColors.getOrElse(i) { Color.Black }
                                            val currentWidth = shape.lineWidths.getOrElse(i) { 5f }

                                            shape.lineColors.add(i + 1, currentColor)
                                            shape.lineWidths.add(i + 1, currentWidth)

                                            // Select the shape after adding point
                                            shapes.forEach { it.isSelected = false }
                                            shape.isSelected = true

                                            pointAdded = true
                                            invalidateKey++
                                            return@detectTapGestures
                                        }
                                    }
                                }
                            }

                            if (!pointAdded) {
                                // Optionally show a message that no line was found
                            }

                            return@detectTapGestures
                        }

                        // -----------------------------
                        // 2. DRAW MODE → Tap = add first point or next points
                        // -----------------------------
                        if (currentDrawMode.value) {

                            // First point OR new shape
                            if (currentShape == null || currentShape?.isClosed == true) {
                                currentShape = Shape(mutableListOf(offset))
                                shapes.add(currentShape!!)
                                invalidateKey++
                                return@detectTapGestures
                            }

                            currentShape?.let { shape ->

                                // Check if tap is near starting point → close shape
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

                                // Add new vertex
                                shape.points.add(offset)
                                shape.lineColors.add(Color.Black)
                                shape.lineWidths.add(5f)
                                invalidateKey++
                            }

                            return@detectTapGestures
                        }

                        // -----------------------------
                        // 3. LINE SELECT MODE
                        // -----------------------------
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

                        // -----------------------------
                        // 4. SHAPE / POINT SELECTION MODE
                        // -----------------------------
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
                                currentShape = Shape(mutableListOf(offset))
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
    )
    {
        val _validate = invalidateKey

        shapes.forEach { shape ->
            if (shape.points.size > 1) {
                for (i in 0 until shape.points.size - 1) {
                    val isSelectedLine = shape.selectedLineIndex == i
                    drawLine(
                        color = if (isSelectedLine) Color.Red else shape.lineColors.getOrElse(i) { Color.Black },
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
}


fun updateSelectedLine(
    shapes: SnapshotStateList<Shape>,
    shapeIndex: Int?,
    lineIndex: Int?,
    color: Color? = null,
    stroke: Float? = null
) {
    if (shapeIndex == null || lineIndex == null) return
    if (shapeIndex !in shapes.indices) return

    val shape = shapes[shapeIndex]

    // Update color
    color?.let {
        if (lineIndex < shape.lineColors.size) {
            shape.lineColors[lineIndex] = it
        }
    }

    // Update stroke width
    stroke?.let {
        if (lineIndex < shape.lineWidths.size) {
            shape.lineWidths[lineIndex] = it
        }
    }
}

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




