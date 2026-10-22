from pathlib import Path
from datetime import datetime
import shutil
import sys

root = Path(".")
gradle = root / "app/build.gradle.kts"
dialogs = root / "app/src/main/java/com/example/ui/components/Dialogs.kt"

gradle_old = '  implementation(libs.coil.compose)'
gradle_new = gradle_old + '\n  implementation("com.caverock:androidsvg-aar:1.4")'

import_old = "import android.graphics.BitmapFactory"
import_new = import_old + "\nimport com.caverock.androidsvg.SVG"

load_old = """    LaunchedEffect(file) {
        bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            BitmapFactory.decodeFile(file.absolutePath)
        }
        undoStack.value = emptyList()
        redoStack.value = emptyList()
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }"""

load_new = """    LaunchedEffect(file) {
        bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching {
                if (file.extension.equals("svg", ignoreCase = true)) {
                    val picture = SVG.getFromFile(file).renderToPicture()
                    val sourceWidth = picture.width.coerceAtLeast(1)
                    val sourceHeight = picture.height.coerceAtLeast(1)
                    val factor = minOf(
                        1f,
                        4096f / sourceWidth,
                        4096f / sourceHeight
                    )
                    val width = (sourceWidth * factor).toInt().coerceAtLeast(1)
                    val height = (sourceHeight * factor).toInt().coerceAtLeast(1)
                    Bitmap.createBitmap(
                        width,
                        height,
                        Bitmap.Config.ARGB_8888
                    ).also { output ->
                        android.graphics.Canvas(output).drawPicture(
                            picture,
                            android.graphics.Rect(0, 0, width, height)
                        )
                    }
                } else {
                    BitmapFactory.decodeFile(file.absolutePath)
                }
            }.getOrNull()
        }
        undoStack.value = emptyList()
        redoStack.value = emptyList()
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }"""

files = {
    gradle: gradle.read_text(),
    dialogs: dialogs.read_text()
}

checks = [
    (gradle, gradle_old, "Gradle dependency insertion point"),
    (dialogs, import_old, "SVG import insertion point"),
    (dialogs, load_old, "Image loading block")
]

for path, old, label in checks:
    content = files[path]
    if content.count(old) != 1:
        print("PATCH ABORTED: Expected exactly one match for " + label)
        print("No files were changed.")
        sys.exit(1)

if "com.caverock.androidsvg.SVG" in files[dialogs]:
    print("PATCH ABORTED: SVG import already exists.")
    print("No files were changed.")
    sys.exit(1)

stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
backups = []

try:
    for path in files:
        backup = path.with_name(path.name + ".before-svg-" + stamp + ".bak")
        shutil.copy2(path, backup)
        backups.append((path, backup))

    updated_gradle = files[gradle].replace(gradle_old, gradle_new, 1)
    updated_dialogs = files[dialogs].replace(import_old, import_new, 1)
    updated_dialogs = updated_dialogs.replace(load_old, load_new, 1)

    gradle.write_text(updated_gradle)
    dialogs.write_text(updated_dialogs)

except Exception as exc:
    for path, backup in backups:
        if backup.exists():
            shutil.copy2(backup, path)
    print("PATCH FAILED: " + str(exc))
    print("Original files restored where backups were available.")
    sys.exit(1)

print("PATCH SUCCESS")
for path, backup in backups:
    print("Backup: " + str(backup))
print("SVG rendering added to the image preview.")
print("Non-SVG image loading remains unchanged.")
