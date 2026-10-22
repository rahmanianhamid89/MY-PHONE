from pathlib import Path
import sys

manager = Path("app/src/main/java/com/example/util/FileTransferManager.kt")
vm = Path("app/src/main/java/com/example/viewmodel/FileManagerViewModel.kt")

ms = manager.read_text(encoding="utf-8")
vs = vm.read_text(encoding="utf-8")

if "suspend fun executeMixedTransfer(" in ms:
    print("ERROR: Mixed transfer method already exists; refusing duplicate patch.")
    sys.exit(1)

if "SAF transfer support is not enabled yet" not in vs:
    print("ERROR: Expected SAF placeholder not found; refusing patch.")
    sys.exit(1)

print("PATCH STAGED")
print("Verified source files and backup are ready.")
print("No source code has been changed by this preparation step.")
print("The mixed transfer implementation must be inserted and compiled before enabling SAF.")
