from pathlib import Path
from datetime import datetime
import shutil
import sys

p = Path("app/src/main/java/com/example/util/FileUtils.kt")
if not p.is_file():
    print("ERROR: FileUtils.kt not found.")
    sys.exit(1)

text = p.read_text()

old_loop = '''            var usbIndex = 0

            for (volume in storageManager.storageVolumes) {
                if (volume.isPrimary || !volume.isRemovable) continue

                val description =
                    volume.getDescription(context).lowercase(Locale.ROOT)'''

new_loop = '''            var usbIndex = 0
            val diagnosticLines = mutableListOf<String>()
            diagnosticLines.add("MY PHONE storage volume diagnostics")
            diagnosticLines.add("Android SDK: ${Build.VERSION.SDK_INT}")
            diagnosticLines.add("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            diagnosticLines.add("")

            for (volume in storageManager.storageVolumes) {
                val rawDescription = volume.getDescription(context).toString()
                val diagnosticDirectory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    volume.directory?.absolutePath ?: "null"
                } else {
                    volume.uuid?.let { File("/storage", it).absolutePath } ?: "null"
                }

                diagnosticLines.add(
                    "description=$rawDescription | uuid=${volume.uuid} | " +
                    "directory=$diagnosticDirectory | primary=${volume.isPrimary} | " +
                    "removable=${volume.isRemovable} | state=${volume.state}"
                )

                if (volume.isPrimary || !volume.isRemovable) continue

                val description = rawDescription.lowercase(Locale.ROOT)'''

if text.count(old_loop) != 1:
    print("ERROR: Expected storage loop was not found exactly once.")
    print("No source files modified.")
    sys.exit(1)

old_end = '''            }
        } catch (_: Exception) {
        }

        return list
    }

    private fun getRootOfVolume'''

new_end = '''            }

            val diagnosticReport = diagnosticLines.joinToString("\\\\n")
            try {
                File(
                    "/storage/emulated/0/Download/fff/storage-volume-diagnostics.txt"
                ).writeText(diagnosticReport)
            } catch (_: Exception) {
                try {
                    File(context.filesDir, "storage-volume-diagnostics.txt")
                        .writeText(diagnosticReport)
                } catch (_: Exception) {
                }
            }
        } catch (_: Exception) {
        }

        return list
    }

    private fun getRootOfVolume'''

if text.count(old_end) != 1:
    print("ERROR: Expected storage function ending was not found exactly once.")
    print("No source files modified.")
    sys.exit(1)

updated = text.replace(old_loop, new_loop, 1)
updated = updated.replace(old_end, new_end, 1)

backup = p.with_name(
    p.name + ".before-storage-diagnostics-" +
    datetime.now().strftime("%Y%m%d_%H%M%S") + ".bak"
)
shutil.copy2(p, backup)
p.write_text(updated)

print("PATCH SUCCESS")
print(f"Backup: {backup}")
print("Temporary storage diagnostics enabled.")
