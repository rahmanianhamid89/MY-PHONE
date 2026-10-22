from pathlib import Path
from datetime import datetime
import shutil
import sys

root = Path.cwd()
fu = root / "app/src/main/java/com/example/util/FileUtils.kt"
sc = root / "app/src/main/java/com/example/ui/components/StorageVolumeCard.kt"
icon = root / "app/src/main/res/drawable-nodpi/ic_sd_card.png"

for p in (fu, sc, icon):
    if not p.is_file():
        print(f"ERROR: Missing file: {p}")
        sys.exit(1)

a = fu.read_text()
b = sc.read_text()

old_detection = '''                    if (isSdCard) continue

                    usbIndex++
                    list.add(
                        StorageVolumeInfo(
                            name = "USB OTG Drive $usbIndex",'''

new_detection = '''                    if (isSdCard) {
                        list.add(
                            StorageVolumeInfo(
                                name = "SD Card",
                                path = path,
                                file = directory,
                                isRemovable = true,
                                isUsbOtg = false,
                                stats = getStorageStats(directory),
                                isMounted = true
                            )
                        )
                        continue
                    }

                    usbIndex++
                    list.add(
                        StorageVolumeInfo(
                            name = "USB OTG Drive $usbIndex",'''

old_vars = '''    val icon = if (volume.isUsbOtg) LucideIcons.Usb else LucideIcons.HardDrive
    val accentColor = if (volume.isUsbOtg) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary'''

new_vars = '''    val icon = if (volume.isUsbOtg) LucideIcons.Usb else LucideIcons.HardDrive
    val accentColor = if (volume.isUsbOtg) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary
    val isSdCard = volume.isRemovable && !volume.isUsbOtg &&
        volume.name.equals("SD Card", ignoreCase = true)'''

old_ui = '''                Icon(
                    imageVector = icon,
                    contentDescription = volume.name,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )'''

new_ui = '''                if (isSdCard) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(
                            id = com.example.R.drawable.ic_sd_card
                        ),
                        contentDescription = "SD Card",
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(accentColor),
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = volume.name,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }'''

checks = [
    (a.count(old_detection) == 1, "SD-card detection block"),
    (b.count(old_vars) == 1, "storage icon declarations"),
    (b.count(old_ui) == 1, "storage icon UI block"),
]

for ok, label in checks:
    if not ok:
        print(f"ERROR: Expected exactly one match for {label}.")
        print("No source files modified.")
        sys.exit(1)

new_a = a.replace(old_detection, new_detection, 1)
new_b = b.replace(old_vars, new_vars, 1).replace(old_ui, new_ui, 1)

stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
backup = root / "backups" / f"sd-card-source-{stamp}"
backup.mkdir(parents=True, exist_ok=False)
shutil.copy2(fu, backup / "FileUtils.kt")
shutil.copy2(sc, backup / "StorageVolumeCard.kt")

try:
    fu.write_text(new_a)
    sc.write_text(new_b)
except Exception as e:
    shutil.copy2(backup / "FileUtils.kt", fu)
    shutil.copy2(backup / "StorageVolumeCard.kt", sc)
    print(f"ERROR: Source files restored from backup: {e}")
    sys.exit(1)

print("PATCH SUCCESS")
print(f"Backup: {backup.relative_to(root)}")
print("SD-card detection and icon updated.")
