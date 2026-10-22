package com.example.ui.icons

import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object LucideIcons {

    val Folder: ImageVector
        get() = ImageVector.Builder(
            name = "Folder",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeAlpha = 1f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(20f, 20f)
                horizontalLineTo(4f)
                curveTo(2.9f, 20f, 2f, 19.1f, 2f, 18f)
                verticalLineTo(5f)
                curveTo(2f, 3.9f, 2.9f, 3f, 4f, 3f)
                horizontalLineTo(7.93f)
                curveTo(8.61f, 3f, 9.25f, 3.35f, 9.62f, 3.93f)
                lineTo(10.41f, 5.1f)
                curveTo(10.79f, 5.67f, 11.42f, 6f, 12.1f, 6f)
                horizontalLineTo(20f)
                curveTo(21.1f, 6f, 22f, 6.9f, 22f, 8f)
                verticalLineTo(18f)
                curveTo(22f, 19.1f, 21.1f, 20f, 20f, 20f)
                close()
            }
        }.build()

    val HardDrive: ImageVector
        get() = ImageVector.Builder(
            name = "HardDrive",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeAlpha = 1f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(22f, 12f)
                horizontalLineTo(2f)
                lineTo(5.45f, 5.1f)
                curveTo(5.79f, 4.42f, 6.48f, 4f, 7.24f, 4f)
                horizontalLineTo(16.76f)
                curveTo(17.52f, 4f, 18.21f, 4.42f, 18.55f, 5.1f)
                lineTo(22f, 12f)
                close()
                moveTo(2f, 12f)
                verticalLineTo(19f)
                curveTo(2f, 20.1f, 2.9f, 21f, 4f, 21f)
                horizontalLineTo(20f)
                curveTo(21.1f, 21f, 22f, 20.1f, 22f, 19f)
                verticalLineTo(12f)
                close()
                moveTo(6f, 16f)
                horizontalLineTo(6.01f)
                moveTo(10f, 16f)
                horizontalLineTo(10.01f)
            }
        }.build()

    val Trash2: ImageVector
        get() = ImageVector.Builder(
            name = "Trash2",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeAlpha = 1f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(10f, 11f)
                verticalLineTo(17f)
                moveTo(14f, 11f)
                verticalLineTo(17f)
                moveTo(19f, 6f)
                verticalLineTo(20f)
                curveTo(19f, 21.1f, 18.1f, 22f, 17f, 22f)
                horizontalLineTo(7f)
                curveTo(5.9f, 22f, 5f, 21.1f, 5f, 20f)
                verticalLineTo(6f)
                moveTo(3f, 6f)
                horizontalLineTo(21f)
                moveTo(8f, 6f)
                verticalLineTo(4f)
                curveTo(8f, 2.9f, 8.9f, 2f, 10f, 2f)
                horizontalLineTo(14f)
                curveTo(15.1f, 2f, 16f, 2.9f, 16f, 4f)
                verticalLineTo(6f)
            }
        }.build()


    val FolderOpen: ImageVector
        get() = ImageVector.Builder(
            name = "FolderOpen",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeAlpha = 1f,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(6f, 14f)
                lineTo(7.5f, 11.1f)
                curveTo(7.84f, 10.42f, 8.52f, 10f, 9.24f, 10f)
                horizontalLineTo(20f)
                curveTo(21.27f, 10f, 22.2f, 11.17f, 21.94f, 12.5f)
                lineTo(20.4f, 18.5f)
                curveTo(20.18f, 19.37f, 19.4f, 20f, 18.45f, 20f)
                horizontalLineTo(4f)
                curveTo(2.9f, 20f, 2f, 19.1f, 2f, 18f)
                verticalLineTo(5f)
                curveTo(2f, 3.9f, 2.9f, 3f, 4f, 3f)
                horizontalLineTo(7.9f)
                curveTo(8.58f, 3f, 9.21f, 3.33f, 9.6f, 3.9f)
                lineTo(10.41f, 5.1f)
                curveTo(10.79f, 5.67f, 11.42f, 6f, 12.09f, 6f)
                horizontalLineTo(18f)
                curveTo(19.1f, 6f, 20f, 6.9f, 20f, 8f)
                verticalLineTo(10f)
            }
        }.build()



    val ArrowLeft: ImageVector
        get() = ImageVector.Builder(
            name = "ArrowLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 19f)
                lineTo(5f, 12f)
                lineTo(12f, 5f)
                moveTo(19f, 12f)
                lineTo(5f, 12f)
            }
        }.build()

val Usb: ImageVector
    get() = ImageVector.Builder(
        name = "Usb",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(11f, 7f)
            curveTo(11f, 7.552f, 10.552f, 8f, 10f, 8f)
            curveTo(9.448f, 8f, 9f, 7.552f, 9f, 7f)
            curveTo(9f, 6.448f, 9.448f, 6f, 10f, 6f)
            curveTo(10.552f, 6f, 11f, 6.448f, 11f, 7f)
            moveTo(5f, 20f)
            curveTo(5f, 20.552f, 4.552f, 21f, 4f, 21f)
            curveTo(3.448f, 21f, 3f, 20.552f, 3f, 20f)
            curveTo(3f, 19.448f, 3.448f, 19f, 4f, 19f)
            curveTo(4.552f, 19f, 5f, 19.448f, 5f, 20f)
            moveTo(4.7f, 19.3f)
            lineTo(19f, 5f)
            moveTo(21f, 3f)
            lineTo(18f, 4f)
            lineTo(20f, 6f)
            close()
            moveTo(9.26f, 7.68f)
            lineTo(5f, 12f)
            lineTo(7f, 17f)
            moveTo(10f, 14f)
            lineTo(15f, 16f)
            lineTo(18.5f, 12.5f)
            moveTo(18f, 12f)
            lineTo(19f, 11f)
            lineTo(20f, 12f)
            lineTo(19f, 13f)
            close()
        }
    }.build()


val Wrench: ImageVector
    get() = ImageVector.Builder(
        name = "Wrench",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(14.7f, 6.3f)
            curveTo(14.7f, 6.3f, 14.7f, 7.3f, 15.4f, 8.0f)
            lineTo(17.0f, 9.6f)
            curveTo(17.7f, 10.3f, 18.7f, 10.3f, 19.4f, 9.6f)
            lineTo(22.506f, 6.495f)
            curveTo(22.826f, 6.173f, 23.369f, 6.275f, 23.489f, 6.713f)
            curveTo(24.489f, 10.713f, 22.489f, 14.713f, 18.489f, 15.713f)
            lineTo(10.579f, 23.623f)
            curveTo(9.408f, 24.794f, 7.508f, 24.794f, 6.337f, 23.623f)
            curveTo(5.166f, 22.452f, 5.166f, 20.552f, 6.337f, 19.381f)
            lineTo(14.247f, 11.471f)
            curveTo(13.247f, 7.471f, 15.247f, 3.471f, 19.247f, 2.471f)
            curveTo(19.685f, 2.351f, 20.227f, 2.453f, 20.548f, 2.775f)
            lineTo(17.442f, 5.88f)
            curveTo(16.742f, 6.58f, 15.742f, 6.58f, 15.042f, 5.88f)
            close()
        }
    }.build()


val FolderPlus: ImageVector
    get() = ImageVector.Builder(
        name = "FolderPlus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 10f)
            lineTo(12f, 16f)

            moveTo(9f, 13f)
            lineTo(15f, 13f)

            moveTo(20f, 20f)
            curveTo(21.105f, 20f, 22f, 19.105f, 22f, 18f)
            lineTo(22f, 8f)
            curveTo(22f, 6.895f, 21.105f, 6f, 20f, 6f)
            lineTo(12.1f, 6f)
            curveTo(11.402f, 6f, 10.749f, 5.636f, 10.41f, 5.027f)
            lineTo(9.6f, 3.9f)
            curveTo(9.224f, 3.338f, 8.598f, 3f, 7.93f, 3f)
            lineTo(4f, 3f)
            curveTo(2.895f, 3f, 2f, 3.895f, 2f, 5f)
            lineTo(2f, 18f)
            curveTo(2f, 19.105f, 2.895f, 20f, 4f, 20f)
            close()
        }
    }.build()


val FilePen: ImageVector
    get() = ImageVector.Builder(
        name = "FilePen",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12.5f, 22f)
            lineTo(18f, 22f)
            curveTo(19.105f, 22f, 20f, 21.105f, 20f, 20f)
            lineTo(20f, 7f)
            lineTo(15f, 2f)
            lineTo(6f, 2f)
            curveTo(4.895f, 2f, 4f, 2.895f, 4f, 4f)
            lineTo(4f, 13.5f)

            moveTo(14f, 2f)
            lineTo(14f, 6f)
            curveTo(14f, 7.105f, 14.895f, 8f, 16f, 8f)
            lineTo(20f, 8f)

            moveTo(13.378f, 15.626f)
            curveTo(14.55f, 14.454f, 14.55f, 12.55f, 13.378f, 11.378f)
            curveTo(12.206f, 10.206f, 10.302f, 10.206f, 9.13f, 11.378f)
            lineTo(4.12f, 16.39f)
            curveTo(3.72f, 16.79f, 3.43f, 17.29f, 3.284f, 17.84f)
            lineTo(2.447f, 20.71f)
            curveTo(2.333f, 21.1f, 2.69f, 21.457f, 3.08f, 21.343f)
            lineTo(5.95f, 20.506f)
            curveTo(6.5f, 20.36f, 7f, 20.07f, 7.4f, 19.67f)
            close()
        }
    }.build()


val Fingerprint: ImageVector
    get() = ImageVector.Builder(
        name = "Fingerprint",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 10f)
            curveTo(10.895f, 10f, 10f, 10.895f, 10f, 12f)
            curveTo(10f, 13.02f, 9.9f, 14.51f, 9.74f, 16f)

            moveTo(14f, 13.12f)
            curveTo(14f, 15.5f, 14f, 19.5f, 13f, 22f)

            moveTo(17.29f, 21.02f)
            curveTo(17.41f, 20.42f, 17.72f, 18.12f, 17.79f, 17.4f)

            moveTo(2f, 12f)
            curveTo(2f, 6.477f, 6.477f, 2f, 12f, 2f)
            curveTo(15.69f, 2f, 18.88f, 4f, 20f, 6f)

            moveTo(2f, 16f)
            lineTo(2.01f, 16f)

            moveTo(21.8f, 16f)
            curveTo(22f, 14f, 21.931f, 10.646f, 21.8f, 10f)

            moveTo(5f, 19.5f)
            curveTo(5.5f, 18f, 6f, 15f, 6f, 12f)
            curveTo(6f, 10.895f, 6.122f, 9.844f, 6.34f, 10f)

            moveTo(8.65f, 22f)
            curveTo(8.86f, 21.34f, 9.1f, 20.68f, 9.22f, 20f)

            moveTo(9f, 6.8f)
            curveTo(10.79f, 5.39f, 13.27f, 5.24f, 15.21f, 6.42f)
            curveTo(16.96f, 7.49f, 18f, 9.42f, 18f, 11.52f)
            lineTo(18f, 14f)
        }
    }.build()


val TriangleAlert: ImageVector
    get() = ImageVector.Builder(
        name = "TriangleAlert",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21.73f, 18f)
            lineTo(13.73f, 4f)
            curveTo(12.96f, 2.67f, 11.04f, 2.67f, 10.27f, 4f)
            lineTo(2.27f, 18f)
            curveTo(1.5f, 19.33f, 2.46f, 21f, 4f, 21f)
            lineTo(20f, 21f)
            curveTo(21.54f, 21f, 22.5f, 19.33f, 21.73f, 18f)
            close()

            moveTo(12f, 9f)
            lineTo(12f, 13f)

            moveTo(12f, 17f)
            lineTo(12.01f, 17f)
        }
    }.build()


val CheckCircle: ImageVector
    get() = ImageVector.Builder(
        name = "CheckCircle",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(21.801f, 10f)
            curveTo(22.355f, 13.09f, 21.5f, 16.27f, 19.5f, 18.5f)
            curveTo(17.5f, 20.73f, 14.43f, 22f, 11.3f, 21.98f)
            curveTo(8.17f, 21.96f, 5.12f, 20.65f, 3.15f, 18.39f)
            curveTo(1.18f, 16.13f, 0.36f, 12.94f, 0.95f, 9.86f)
            curveTo(1.54f, 6.78f, 3.48f, 4.1f, 6.27f, 2.48f)
            curveTo(9.06f, 0.86f, 12.42f, 0.44f, 15.52f, 1.34f)
            curveTo(18.62f, 2.24f, 21.05f, 4.35f, 22.31f, 7.2f)

            moveTo(9f, 11f)
            lineTo(12f, 14f)
            lineTo(22f, 4f)
        }
    }.build()


val FilePlus: ImageVector
    get() = ImageVector.Builder(
        name = "FilePlus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(15f, 2f)
            lineTo(6f, 2f)
            curveTo(4.895f, 2f, 4f, 2.895f, 4f, 4f)
            lineTo(4f, 20f)
            curveTo(4f, 21.105f, 4.895f, 22f, 6f, 22f)
            lineTo(18f, 22f)
            curveTo(19.105f, 22f, 20f, 21.105f, 20f, 20f)
            lineTo(20f, 7f)
            close()

            moveTo(14f, 2f)
            lineTo(14f, 6f)
            curveTo(14f, 7.105f, 14.895f, 8f, 16f, 8f)
            lineTo(20f, 8f)

            moveTo(9f, 15f)
            lineTo(15f, 15f)

            moveTo(12f, 18f)
            lineTo(12f, 12f)
        }
    }.build()


val ListChecks: ImageVector
    get() = ImageVector.Builder(
        name = "ListChecks",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(13f, 5f)
            lineTo(21f, 5f)

            moveTo(13f, 12f)
            lineTo(21f, 12f)

            moveTo(13f, 19f)
            lineTo(21f, 19f)

            moveTo(3f, 17f)
            lineTo(5f, 19f)
            lineTo(9f, 15f)

            moveTo(3f, 7f)
            lineTo(5f, 9f)
            lineTo(9f, 5f)
        }
    }.build()


val Images: ImageVector
    get() = ImageVector.Builder(
        name = "Images",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(22f, 11f)
            lineTo(20.704f, 9.704f)
            curveTo(19.765f, 8.765f, 18.243f, 8.765f, 17.304f, 9.704f)
            lineTo(11f, 16f)

            moveTo(4f, 8f)
            curveTo(2.895f, 8f, 2f, 8.895f, 2f, 10f)
            lineTo(2f, 20f)
            curveTo(2f, 21.105f, 2.895f, 22f, 4f, 22f)
            lineTo(14f, 22f)
            curveTo(15.105f, 22f, 16f, 21.105f, 16f, 20f)

            moveTo(14f, 7f)
            curveTo(14f, 7.552f, 13.552f, 8f, 13f, 8f)
            curveTo(12.448f, 8f, 12f, 7.552f, 12f, 7f)
            curveTo(12f, 6.448f, 12.448f, 6f, 13f, 6f)
            curveTo(13.552f, 6f, 14f, 6.448f, 14f, 7f)

            moveTo(10f, 2f)
            lineTo(20f, 2f)
            curveTo(21.105f, 2f, 22f, 2.895f, 22f, 4f)
            lineTo(22f, 14f)
            curveTo(22f, 15.105f, 21.105f, 16f, 20f, 16f)
            lineTo(10f, 16f)
            curveTo(8.895f, 16f, 8f, 15.105f, 8f, 14f)
            lineTo(8f, 4f)
            curveTo(8f, 2.895f, 8.895f, 2f, 10f, 2f)
            close()
        }
    }.build()

val FileType: ImageVector
    get() = ImageVector.Builder(
        name = "FileType",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(15f, 2f)
            lineTo(6f, 2f)
            curveTo(4.895f, 2f, 4f, 2.895f, 4f, 4f)
            lineTo(4f, 20f)
            curveTo(4f, 21.105f, 4.895f, 22f, 6f, 22f)
            lineTo(18f, 22f)
            curveTo(19.105f, 22f, 20f, 21.105f, 20f, 20f)
            lineTo(20f, 7f)

            moveTo(14f, 2f)
            lineTo(14f, 6f)
            curveTo(14f, 7.105f, 14.895f, 8f, 16f, 8f)
            lineTo(20f, 8f)

            moveTo(9f, 13f)
            lineTo(9f, 12f)
            lineTo(15f, 12f)
            lineTo(15f, 13f)

            moveTo(12f, 12f)
            lineTo(12f, 18f)

            moveTo(11f, 18f)
            lineTo(13f, 18f)
        }
    }.build()

val ListFilter: ImageVector
    get() = ImageVector.Builder(
        name = "ListFilter",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(2f, 5f)
            lineTo(22f, 5f)

            moveTo(6f, 12f)
            lineTo(18f, 12f)

            moveTo(9f, 19f)
            lineTo(15f, 19f)
        }
    }.build()

val FileCode: ImageVector
    get() = ImageVector.Builder(
        name = "FileCode",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color.Transparent),
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(10f, 12.5f)
            lineTo(8f, 15f)
            lineTo(10f, 17.5f)

            moveTo(14f, 12.5f)
            lineTo(16f, 15f)
            lineTo(14f, 17.5f)

            moveTo(14f, 2f)
            lineTo(14f, 6f)
            curveTo(14f, 7.105f, 14.895f, 8f, 16f, 8f)
            lineTo(20f, 8f)

            moveTo(15f, 2f)
            lineTo(6f, 2f)
            curveTo(4.895f, 2f, 4f, 2.895f, 4f, 4f)
            lineTo(4f, 20f)
            curveTo(4f, 21.105f, 4.895f, 22f, 6f, 22f)
            lineTo(18f, 22f)
            curveTo(19.105f, 22f, 20f, 21.105f, 20f, 20f)
            lineTo(20f, 7f)
        }
    }.build()


    val ArrowRight: ImageVector
        get() = ImageVector.Builder(name = "ArrowRight", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(5f, 12f); lineTo(19f, 12f); moveTo(12f, 5f); lineTo(19f, 12f); lineTo(12f, 19f)
            }
        }.build()

    val Bookmark: ImageVector
        get() = ImageVector.Builder(name = "Bookmark", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(19f, 21f); lineTo(12f, 17f); lineTo(5f, 21f); verticalLineTo(5f)
                curveTo(5f, 3.895f, 5.895f, 3f, 7f, 3f); horizontalLineTo(17f)
                curveTo(18.105f, 3f, 19f, 3.895f, 19f, 5f); close()
            }
        }.build()

    val BookmarkPlus: ImageVector
        get() = ImageVector.Builder(name = "BookmarkPlus", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(19f, 21f); lineTo(12f, 17f); lineTo(5f, 21f); verticalLineTo(5f)
                curveTo(5f, 3.895f, 5.895f, 3f, 7f, 3f); horizontalLineTo(17f)
                curveTo(18.105f, 3f, 19f, 3.895f, 19f, 5f); close()
                moveTo(12f, 7f); verticalLineTo(13f); moveTo(15f, 10f); horizontalLineTo(9f)
            }
        }.build()

    val Check: ImageVector
        get() = ImageVector.Builder(name = "Check", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(20f, 6f); lineTo(9f, 17f); lineTo(4f, 12f)
            }
        }.build()

    val ChevronDown: ImageVector
        get() = ImageVector.Builder(name = "ChevronDown", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(6f, 9f); lineTo(12f, 15f); lineTo(18f, 9f)
            }
        }.build()

    val ChevronRight: ImageVector
        get() = ImageVector.Builder(name = "ChevronRight", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(9f, 18f); lineTo(15f, 12f); lineTo(9f, 6f)
            }
        }.build()

    val ChevronUp: ImageVector
        get() = ImageVector.Builder(name = "ChevronUp", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(18f, 15f); lineTo(12f, 9f); lineTo(6f, 15f)
            }
        }.build()

    val ClipboardPaste: ImageVector
        get() = ImageVector.Builder(name = "ClipboardPaste", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(11f, 14f); horizontalLineTo(21f)
                moveTo(16f, 4f); horizontalLineTo(18f); curveTo(19.105f, 4f, 20f, 4.895f, 20f, 6f); verticalLineTo(7.344f)
                moveTo(17f, 18f); lineTo(21f, 14f); lineTo(17f, 10f)
                moveTo(8f, 4f); horizontalLineTo(6f); curveTo(4.895f, 4f, 4f, 4.895f, 4f, 6f); verticalLineTo(20f)
                curveTo(4f, 21.105f, 4.895f, 22f, 6f, 22f); horizontalLineTo(18f)
                curveTo(19.105f, 22f, 20f, 21.105f, 20f, 20f); verticalLineTo(19f)
                moveTo(8f, 2f); horizontalLineTo(16f); verticalLineTo(6f); horizontalLineTo(8f); close()
            }
        }.build()

    val Cloud: ImageVector
        get() = ImageVector.Builder(name = "Cloud", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(17.5f, 19f); horizontalLineTo(9f); curveTo(5.134f, 19f, 2f, 15.866f, 2f, 12f)
                curveTo(2f, 8.134f, 5.134f, 5f, 9f, 5f); curveTo(12.1f, 5f, 14.74f, 7.01f, 15.71f, 9.79f)
                horizontalLineTo(17.5f); curveTo(19.985f, 9.79f, 22f, 11.805f, 22f, 14.29f)
                curveTo(22f, 16.775f, 19.985f, 19f, 17.5f, 19f); close()
            }
        }.build()

    val Cloudy: ImageVector
        get() = ImageVector.Builder(name = "Cloudy", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(17.5f, 21f); horizontalLineTo(9f); curveTo(5.134f, 21f, 2f, 17.866f, 2f, 14f)
                curveTo(2f, 10.134f, 5.134f, 7f, 9f, 7f); curveTo(12.1f, 7f, 14.74f, 9.01f, 15.71f, 11.79f)
                horizontalLineTo(17.5f); curveTo(19.985f, 11.79f, 22f, 13.805f, 22f, 16.29f)
                curveTo(22f, 18.775f, 19.985f, 21f, 17.5f, 21f)
                moveTo(22f, 10f); curveTo(22f, 8.343f, 20.657f, 7f, 19f, 7f); horizontalLineTo(16.793f)
                curveTo(16.1f, 4.0f, 13.4f, 2f, 10.3f, 2f)
            }
        }.build()

    val Copy: ImageVector
        get() = ImageVector.Builder(name = "Copy", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(8f, 8f); horizontalLineTo(20f); curveTo(21.105f, 8f, 22f, 8.895f, 22f, 10f); verticalLineTo(20f)
                curveTo(22f, 21.105f, 21.105f, 22f, 20f, 22f); horizontalLineTo(10f)
                curveTo(8.895f, 22f, 8f, 21.105f, 8f, 20f); close()
                moveTo(16f, 8f); verticalLineTo(4f); curveTo(16f, 2.895f, 15.105f, 2f, 14f, 2f); horizontalLineTo(4f)
                curveTo(2.895f, 2f, 2f, 2.895f, 2f, 4f); verticalLineTo(14f)
                curveTo(2f, 15.105f, 2.895f, 16f, 4f, 16f); horizontalLineTo(8f)
            }
        }.build()

    val Download: ImageVector
        get() = ImageVector.Builder(name = "Download", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(12f, 15f); verticalLineTo(3f); moveTo(21f, 15f); verticalLineTo(19f)
                curveTo(21f, 20.105f, 20.105f, 21f, 19f, 21f); horizontalLineTo(5f)
                curveTo(3.895f, 21f, 3f, 20.105f, 3f, 19f); verticalLineTo(15f)
                moveTo(7f, 10f); lineTo(12f, 15f); lineTo(17f, 10f)
            }
        }.build()

    val Edit3: ImageVector
        get() = ImageVector.Builder(name = "Edit3", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(13f, 21f); horizontalLineTo(21f)
                moveTo(21.174f, 6.812f); curveTo(22.151f, 5.835f, 22.151f, 4.251f, 21.174f, 3.274f)
                curveTo(20.197f, 2.297f, 18.613f, 2.297f, 17.636f, 3.274f)
                lineTo(3.842f, 16.174f); curveTo(3.547f, 16.449f, 3.319f, 16.79f, 3.181f, 17.174f)
                lineTo(1.861f, 21.526f); curveTo(1.74f, 21.925f, 2.115f, 22.3f, 2.514f, 22.179f)
                lineTo(6.867f, 20.859f); curveTo(7.25f, 20.721f, 7.591f, 20.493f, 7.867f, 20.198f)
                close()
            }
        }.build()

    val EllipsisVertical: ImageVector
        get() = ImageVector.Builder(name = "EllipsisVertical", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(12f, 12f); moveTo(12f, 5f); moveTo(12f, 19f)
                moveTo(12f, 12f)
            }
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f) {
                moveTo(13f, 12f); curveTo(13f, 12.552f, 12.552f, 13f, 12f, 13f)
                curveTo(11.448f, 13f, 11f, 12.552f, 11f, 12f)
                curveTo(11f, 11.448f, 11.448f, 11f, 12f, 11f)
                curveTo(12.552f, 11f, 13f, 11.448f, 13f, 12f)
            }
        }.build()

    val ExternalLink: ImageVector
        get() = ImageVector.Builder(name = "ExternalLink", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(15f, 3f); horizontalLineTo(21f); verticalLineTo(9f)
                moveTo(10f, 14f); lineTo(21f, 3f)
                moveTo(18f, 13f); verticalLineTo(19f); curveTo(18f, 20.105f, 17.105f, 21f, 16f, 21f)
                horizontalLineTo(5f); curveTo(3.895f, 21f, 3f, 20.105f, 3f, 19f); verticalLineTo(8f)
                curveTo(3f, 6.895f, 3.895f, 6f, 5f, 6f); horizontalLineTo(11f)
            }
        }.build()

    val Eye: ImageVector
        get() = ImageVector.Builder(name = "Eye", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                curveTo(2.062f, 12.348f, 2.062f, 11.652f, 2.062f, 11.652f)
                curveTo(4.7f, 5.2f, 8.1f, 3f, 12f, 3f); curveTo(16f, 3f, 19.3f, 5.2f, 21.938f, 11.652f)
                curveTo(21.938f, 11.652f, 21.938f, 12.348f, 21.938f, 12.348f)
                curveTo(19.3f, 18.8f, 16f, 21f, 12f, 21f); curveTo(8.1f, 21f, 4.7f, 18.8f, 2.062f, 12.348f)
                moveTo(15f, 12f); curveTo(15f, 13.657f, 13.657f, 15f, 12f, 15f); curveTo(10.343f, 15f, 9f, 13.657f, 9f, 12f)
                curveTo(9f, 10.343f, 10.343f, 9f, 12f, 9f); curveTo(13.657f, 9f, 15f, 10.343f, 15f, 12f)
            }
        }.build()

    val EyeOff: ImageVector
        get() = ImageVector.Builder(name = "EyeOff", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(10.733f, 5.076f); curveTo(14.8f, 4.3f, 19.1f, 6.4f, 21.938f, 11.652f)
                curveTo(22.1f, 11.9f, 22.1f, 12.1f, 21.938f, 12.348f); curveTo(21.4f, 13.5f, 20.8f, 14.5f, 20f, 15.5f)
                moveTo(14.084f, 14.158f); curveTo(12.916f, 15.326f, 11.024f, 15.326f, 9.856f, 14.158f)
                curveTo(8.688f, 12.99f, 8.688f, 11.098f, 9.856f, 9.93f)
                moveTo(17.479f, 17.499f); curveTo(13.2f, 21.1f, 7f, 20.5f, 2.062f, 12.348f)
                curveTo(1.9f, 12.1f, 1.9f, 11.9f, 2.062f, 11.652f); curveTo(3.2f, 9.5f, 4.7f, 7.7f, 6.508f, 6.357f)
                moveTo(2f, 2f); lineTo(22f, 22f)
            }
        }.build()

    val File: ImageVector
        get() = ImageVector.Builder(name = "File", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(15f, 2f); horizontalLineTo(6f); curveTo(4.895f, 2f, 4f, 2.895f, 4f, 4f); verticalLineTo(20f)
                curveTo(4f, 21.105f, 4.895f, 22f, 6f, 22f); horizontalLineTo(18f)
                curveTo(19.105f, 22f, 20f, 21.105f, 20f, 20f); verticalLineTo(7f)
                lineTo(15f, 2f); close()
                moveTo(14f, 2f); verticalLineTo(6f); curveTo(14f, 7.105f, 14.895f, 8f, 16f, 8f); horizontalLineTo(20f)
            }
        }.build()

    val FileAudio: ImageVector
        get() = ImageVector.Builder(name = "FileAudio", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(17.5f, 22f); horizontalLineTo(18f); curveTo(19.105f, 22f, 20f, 21.105f, 20f, 20f); verticalLineTo(7f); lineTo(15f, 2f)
                horizontalLineTo(6f); curveTo(4.895f, 2f, 4f, 2.895f, 4f, 4f); verticalLineTo(9f)
                moveTo(14f, 2f); verticalLineTo(6f); curveTo(14f, 7.105f, 14.895f, 8f, 16f, 8f); horizontalLineTo(20f)
                moveTo(2f, 19f); curveTo(2f, 17.895f, 2.895f, 17f, 4f, 17f); curveTo(5.105f, 17f, 6f, 17.895f, 6f, 19f); verticalLineTo(20f)
                curveTo(6f, 21.105f, 5.105f, 22f, 4f, 22f); curveTo(2.895f, 22f, 2f, 21.105f, 2f, 20f); close()
                moveTo(6f, 19f); curveTo(6f, 15.686f, 8.686f, 13f, 12f, 13f); curveTo(15.314f, 13f, 18f, 15.686f, 18f, 19f); verticalLineTo(20f)
            }
        }.build()

    val FileText: ImageVector
        get() = ImageVector.Builder(name = "FileText", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(15f, 2f); horizontalLineTo(6f); curveTo(4.895f, 2f, 4f, 2.895f, 4f, 4f); verticalLineTo(20f)
                curveTo(4f, 21.105f, 4.895f, 22f, 6f, 22f); horizontalLineTo(18f); curveTo(19.105f, 22f, 20f, 21.105f, 20f, 20f); verticalLineTo(7f); close()
                moveTo(14f, 2f); verticalLineTo(6f); curveTo(14f, 7.105f, 14.895f, 8f, 16f, 8f); horizontalLineTo(20f)
                moveTo(10f, 9f); horizontalLineTo(8f); moveTo(16f, 13f); horizontalLineTo(8f); moveTo(16f, 17f); horizontalLineTo(8f)
            }
        }.build()

    val FileVideo: ImageVector
        get() = ImageVector.Builder(name = "FileVideo", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(14f, 2f); verticalLineTo(6f); curveTo(14f, 7.105f, 14.895f, 8f, 16f, 8f); horizontalLineTo(20f)
                moveTo(15f, 2f); horizontalLineTo(6f); curveTo(4.895f, 2f, 4f, 2.895f, 4f, 4f); verticalLineTo(20f)
                curveTo(4f, 21.105f, 4.895f, 22f, 6f, 22f); horizontalLineTo(18f); curveTo(19.105f, 22f, 20f, 21.105f, 20f, 20f); verticalLineTo(7f)
                moveTo(15.033f, 13.44f); curveTo(15.55f, 13.739f, 15.55f, 14.261f, 15.033f, 14.56f); lineTo(10.968f, 16.912f)
                curveTo(10.451f, 17.211f, 10f, 16.962f, 10f, 16.365f); verticalLineTo(11.661f)
                curveTo(10f, 11.064f, 10.451f, 10.815f, 10.968f, 11.114f); close()
            }
        }.build()

    val FolderArchive: ImageVector
        get() = ImageVector.Builder(name = "FolderArchive", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(15f, 19f); moveTo(13f, 19f)
                moveTo(20.9f, 19.8f); curveTo(21.58f, 19.4f, 22f, 18.73f, 22f, 18f); verticalLineTo(8f)
                curveTo(22f, 6.895f, 21.105f, 6f, 20f, 6f); horizontalLineTo(12.1f)
                curveTo(11.42f, 6f, 10.79f, 5.66f, 10.41f, 5.1f); lineTo(9.6f, 3.9f)
                curveTo(9.23f, 3.34f, 8.6f, 3f, 7.93f, 3f); horizontalLineTo(4f)
                curveTo(2.895f, 3f, 2f, 3.895f, 2f, 5f); verticalLineTo(18f)
                curveTo(2f, 19.105f, 2.895f, 20f, 4f, 20f); horizontalLineTo(9.1f)
                moveTo(15f, 11f); verticalLineTo(10f); moveTo(15f, 17f); verticalLineTo(15f)
            }
        }.build()

    val Gauge: ImageVector
        get() = ImageVector.Builder(name = "Gauge", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(12f, 14f); lineTo(16f, 10f); moveTo(3.34f, 19f); curveTo(5.12f, 15.95f, 8.37f, 14f, 12f, 14f)
                curveTo(15.63f, 14f, 18.88f, 15.95f, 20.66f, 19f)
            }
        }.build()

    val Grid2x2: ImageVector
        get() = ImageVector.Builder(name = "Grid2x2", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                verticalLineTo(3f); verticalLineTo(21f); moveTo(3f, 12f); horizontalLineTo(21f)
                moveTo(3f, 3f); horizontalLineTo(21f); verticalLineTo(21f); horizontalLineTo(3f); close()
            }
        }.build()

    val House: ImageVector
        get() = ImageVector.Builder(name = "House", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(15f, 21f); verticalLineTo(13f); curveTo(15f, 12.448f, 14.552f, 12f, 14f, 12f); horizontalLineTo(10f)
                curveTo(9.448f, 12f, 9f, 12.448f, 9f, 13f); verticalLineTo(21f)
                moveTo(3f, 10f); curveTo(3f, 9.43f, 3.25f, 8.89f, 3.709f, 8.472f); lineTo(10.709f, 2.472f)
                curveTo(11.45f, 1.837f, 12.55f, 1.837f, 13.291f, 2.472f); lineTo(20.291f, 8.472f)
                curveTo(20.75f, 8.89f, 21f, 9.43f, 21f, 10f); verticalLineTo(19f)
                curveTo(21f, 20.105f, 20.105f, 21f, 19f, 21f); horizontalLineTo(5f)
                curveTo(3.895f, 21f, 3f, 20.105f, 3f, 19f); close()
            }
        }.build()

    val Image: ImageVector
        get() = ImageVector.Builder(name = "Image", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(21f, 15f); lineTo(17.914f, 11.914f); curveTo(17.133f, 11.133f, 15.867f, 11.133f, 15.086f, 11.914f); lineTo(6f, 21f)
                moveTo(3f, 3f); horizontalLineTo(21f); verticalLineTo(21f); horizontalLineTo(3f); close()
                moveTo(9f, 9f); curveTo(10.105f, 9f, 11f, 9.895f, 11f, 11f); curveTo(11f, 12.105f, 10.105f, 13f, 9f, 13f)
            }
        }.build()

    val Info: ImageVector
        get() = ImageVector.Builder(name = "Info", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(12f, 12f); verticalLineTo(16f); moveTo(12f, 8f); horizontalLineTo(12.01f)
                moveTo(22f, 12f); curveTo(22f, 17.523f, 17.523f, 22f, 12f, 22f)
                curveTo(6.477f, 22f, 2f, 17.523f, 2f, 12f); curveTo(2f, 6.477f, 6.477f, 2f, 12f, 2f)
                curveTo(17.523f, 2f, 22f, 6.477f, 22f, 12f)
            }
        }.build()

    val List: ImageVector
        get() = ImageVector.Builder(name = "List", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(3f, 5f); horizontalLineTo(3.01f); moveTo(3f, 12f); horizontalLineTo(3.01f); moveTo(3f, 19f); horizontalLineTo(3.01f)
                moveTo(8f, 5f); horizontalLineTo(21f); moveTo(8f, 12f); horizontalLineTo(21f); moveTo(8f, 19f); horizontalLineTo(21f)
            }
        }.build()

    val Network: ImageVector
        get() = ImageVector.Builder(name = "Network", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(16f, 16f); horizontalLineTo(22f); verticalLineTo(22f); horizontalLineTo(16f); close()
                moveTo(2f, 16f); horizontalLineTo(8f); verticalLineTo(22f); horizontalLineTo(2f); close()
                moveTo(9f, 2f); horizontalLineTo(15f); verticalLineTo(8f); horizontalLineTo(9f); close()
                moveTo(5f, 16f); verticalLineTo(13f); curveTo(5f, 12.448f, 5.448f, 12f, 6f, 12f); horizontalLineTo(18f)
                curveTo(18.552f, 12f, 19f, 12.448f, 19f, 13f); verticalLineTo(16f); moveTo(12f, 12f); verticalLineTo(8f)
            }
        }.build()

    val Pause: ImageVector
        get() = ImageVector.Builder(name = "Pause", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f) {
                moveTo(14f, 3f); horizontalLineTo(19f); verticalLineTo(21f); horizontalLineTo(14f); close()
                moveTo(5f, 3f); horizontalLineTo(10f); verticalLineTo(21f); horizontalLineTo(5f); close()
            }
        }.build()

    val Play: ImageVector
        get() = ImageVector.Builder(name = "Play", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(5f, 5f); curveTo(5f, 3.46f, 6.67f, 2.49f, 8.008f, 3.272f)
                lineTo(20.005f, 10.27f); curveTo(21.337f, 11.047f, 21.34f, 12.953f, 20.005f, 13.73f)
                lineTo(8.005f, 20.73f); curveTo(6.67f, 21.51f, 5f, 20.54f, 5f, 19f); close()
            }
        }.build()

    val Plus: ImageVector
        get() = ImageVector.Builder(name = "Plus", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(5f, 12f); horizontalLineTo(19f); moveTo(12f, 5f); verticalLineTo(19f)
            }
        }.build()

    val RefreshCw: ImageVector
        get() = ImageVector.Builder(name = "RefreshCw", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(3f, 12f); curveTo(3f, 7.03f, 7.03f, 3f, 12f, 3f); curveTo(15.4f, 3f, 18.5f, 4.74f, 20.24f, 7.74f); lineTo(21f, 8f)
                moveTo(21f, 3f); verticalLineTo(8f); horizontalLineTo(16f)
                moveTo(21f, 12f); curveTo(21f, 16.97f, 16.97f, 21f, 12f, 21f); curveTo(8.6f, 21f, 5.5f, 19.26f, 3.76f, 16.26f); lineTo(3f, 16f)
                moveTo(3f, 21f); verticalLineTo(16f); horizontalLineTo(8f)
            }
        }.build()

    val Save: ImageVector
        get() = ImageVector.Builder(name = "Save", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(15.2f, 3f); curveTo(15.73f, 3f, 16.24f, 3.21f, 16.6f, 3.6f); lineTo(20.4f, 7.4f)
                curveTo(20.79f, 7.76f, 21f, 8.27f, 21f, 8.8f); verticalLineTo(19f)
                curveTo(21f, 20.105f, 20.105f, 21f, 19f, 21f); horizontalLineTo(5f)
                curveTo(3.895f, 21f, 3f, 20.105f, 3f, 19f); verticalLineTo(5f)
                curveTo(3f, 3.895f, 3.895f, 3f, 5f, 3f); close()
                moveTo(17f, 21f); verticalLineTo(14f); curveTo(17f, 13.448f, 16.552f, 13f, 16f, 13f); horizontalLineTo(8f)
                curveTo(7.448f, 13f, 7f, 13.448f, 7f, 14f); verticalLineTo(21f)
                moveTo(7f, 3f); verticalLineTo(7f); curveTo(7f, 7.552f, 7.448f, 8f, 8f, 8f); horizontalLineTo(15f)
            }
        }.build()

    val Scissors: ImageVector
        get() = ImageVector.Builder(name = "Scissors", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(6f, 9f); curveTo(7.657f, 9f, 9f, 7.657f, 9f, 6f); curveTo(9f, 4.343f, 7.657f, 3f, 6f, 3f); curveTo(4.343f, 3f, 3f, 4.343f, 3f, 6f); curveTo(3f, 7.657f, 4.343f, 9f, 6f, 9f)
                moveTo(8.12f, 8.12f); lineTo(12f, 12f); moveTo(20f, 4f); lineTo(8.12f, 15.88f)
                moveTo(6f, 21f); curveTo(7.657f, 21f, 9f, 19.657f, 9f, 18f); curveTo(9f, 16.343f, 7.657f, 15f, 6f, 15f); curveTo(4.343f, 15f, 3f, 16.343f, 3f, 18f); curveTo(3f, 19.657f, 4.343f, 21f, 6f, 21f)
                moveTo(14.8f, 14.8f); lineTo(20f, 20f)
            }
        }.build()

    val Search: ImageVector
        get() = ImageVector.Builder(name = "Search", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(21f, 21f); lineTo(16.66f, 16.66f)
                moveTo(19f, 11f); curveTo(19f, 15.418f, 15.418f, 19f, 11f, 19f); curveTo(6.582f, 19f, 3f, 15.418f, 3f, 11f)
                curveTo(3f, 6.582f, 6.582f, 3f, 11f, 3f); curveTo(15.418f, 3f, 19f, 6.582f, 19f, 11f)
            }
        }.build()

    val Share2: ImageVector
        get() = ImageVector.Builder(name = "Share2", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(18f, 8f); curveTo(19.657f, 8f, 21f, 6.657f, 21f, 5f); curveTo(21f, 3.343f, 19.657f, 2f, 18f, 2f); curveTo(16.343f, 2f, 15f, 3.343f, 15f, 5f); curveTo(15f, 6.657f, 16.343f, 8f, 18f, 8f)
                moveTo(6f, 15f); curveTo(7.657f, 15f, 9f, 13.657f, 9f, 12f); curveTo(9f, 10.343f, 7.657f, 9f, 6f, 9f); curveTo(4.343f, 9f, 3f, 10.343f, 3f, 12f); curveTo(3f, 13.657f, 4.343f, 15f, 6f, 15f)
                moveTo(18f, 22f); curveTo(19.657f, 22f, 21f, 20.657f, 21f, 19f); curveTo(21f, 17.343f, 19.657f, 16f, 18f, 16f); curveTo(16.343f, 16f, 15f, 17.343f, 15f, 19f); curveTo(15f, 20.657f, 16.343f, 22f, 18f, 22f)
                moveTo(8.59f, 13.51f); lineTo(15.42f, 17.49f); moveTo(15.41f, 6.51f); lineTo(8.59f, 10.49f)
            }
        }.build()

    val X: ImageVector
        get() = ImageVector.Builder(name = "X", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = null, stroke = SolidColor(androidx.compose.ui.graphics.Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(18f, 6f); lineTo(6f, 18f); moveTo(6f, 6f); lineTo(18f, 18f)
            }
        }.build()


    val Settings: ImageVector
        get() = ImageVector.Builder(
            name = "Settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 15f)
                arcToRelative(3f, 3f, 0f, true, true, 0f, -6f)
                arcToRelative(3f, 3f, 0f, true, true, 0f, 6f)

                moveTo(19.4f, 15f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, 0.33f, 1.82f)
                lineToRelative(0.06f, 0.06f)
                arcToRelative(2f, 2f, 0f, true, true, -2.83f, 2.83f)
                lineToRelative(-0.06f, -0.06f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, -1.82f, -0.33f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, -1f, 1.51f)
                verticalLineTo(21f)
                arcToRelative(2f, 2f, 0f, true, true, -4f, 0f)
                verticalLineToRelative(-0.09f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, -1f, -1.51f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, -1.82f, 0.33f)
                lineToRelative(-0.06f, 0.06f)
                arcToRelative(2f, 2f, 0f, true, true, -2.83f, -2.83f)
                lineToRelative(0.06f, -0.06f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, 0.33f, -1.82f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, -1.51f, -1f)
                horizontalLineTo(3f)
                arcToRelative(2f, 2f, 0f, true, true, 0f, -4f)
                horizontalLineToRelative(0.09f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, 1.51f, -1f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, -0.33f, -1.82f)
                lineToRelative(-0.06f, -0.06f)
                arcToRelative(2f, 2f, 0f, true, true, 2.83f, -2.83f)
                lineToRelative(0.06f, 0.06f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, 1.82f, 0.33f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, 1f, -1.51f)
                verticalLineTo(3f)
                arcToRelative(2f, 2f, 0f, true, true, 4f, 0f)
                verticalLineToRelative(0.09f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, 1f, 1.51f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, 1.82f, -0.33f)
                lineToRelative(0.06f, -0.06f)
                arcToRelative(2f, 2f, 0f, true, true, 2.83f, 2.83f)
                lineToRelative(-0.06f, 0.06f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, -0.33f, 1.82f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, 1.51f, 1f)
                horizontalLineTo(21f)
                arcToRelative(2f, 2f, 0f, true, true, 0f, 4f)
                horizontalLineToRelative(-0.09f)
                arcToRelative(1.65f, 1.65f, 0f, false, false, -1.51f, 1f)
            }
        }.build()


}
