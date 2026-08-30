#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$SCRIPT_DIR/DentalWave-frontend"
BACKEND_DIR="$SCRIPT_DIR/DentalWave"
TEMPLATE_DIR="$SCRIPT_DIR/portable/windows"
OUTPUT_DIR="$SCRIPT_DIR/dist/DentalWave-Portable"
PORTABLE_CONTENT_DIR="$OUTPUT_DIR/OtherInfo"
CACHE_DIR="$SCRIPT_DIR/.portable-cache"
RUNTIME_ZIP="$CACHE_DIR/OpenJDK21U-jre_x64_windows_hotspot.zip"
RUNTIME_EXTRACT="$CACHE_DIR/windows-jre-21"
RUNTIME_URL="https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk"

echo "Checking Windows portable path handling..."
python3 "$SCRIPT_DIR/portable/tests/test_windows_launcher_paths.py"

echo "Building DentalWave frontend..."
(cd "$FRONTEND_DIR" && npm run build)

echo "Packaging DentalWave application..."
(cd "$BACKEND_DIR" && ./mvnw clean package -DskipTests)

mkdir -p "$CACHE_DIR"
if [[ ! -f "$RUNTIME_ZIP" ]]; then
    echo "Downloading the Eclipse Temurin Java 21 Windows runtime..."
    curl --fail --location "$RUNTIME_URL" --output "$RUNTIME_ZIP"
fi

if [[ ! -d "$RUNTIME_EXTRACT" ]]; then
    echo "Extracting the Windows Java runtime..."
    TEMP_RUNTIME_DIR="$(mktemp -d)"
    trap 'rm -rf "$TEMP_RUNTIME_DIR"' EXIT
    unzip -q "$RUNTIME_ZIP" -d "$TEMP_RUNTIME_DIR"
    RUNTIME_SOURCE="$(find "$TEMP_RUNTIME_DIR" -mindepth 1 -maxdepth 1 -type d | head -n 1)"
    if [[ -z "$RUNTIME_SOURCE" || ! -f "$RUNTIME_SOURCE/bin/java.exe" ]]; then
        echo "Downloaded Java archive did not contain bin/java.exe." >&2
        exit 1
    fi
    mv "$RUNTIME_SOURCE" "$RUNTIME_EXTRACT"
    rm -rf "$TEMP_RUNTIME_DIR"
    trap - EXIT
fi

if [[ "$OUTPUT_DIR" != "$SCRIPT_DIR/dist/DentalWave-Portable" ]]; then
    echo "Refusing to replace an unexpected output directory." >&2
    exit 1
fi

rm -rf "$OUTPUT_DIR"
mkdir -p "$PORTABLE_CONTENT_DIR/app" "$PORTABLE_CONTENT_DIR/data" \
    "$PORTABLE_CONTENT_DIR/backups" "$PORTABLE_CONTENT_DIR/logs"

# Keep the USB root intentionally simple for office users. The two daily-use
# launchers stay visible; technical files and maintenance tools live in OtherInfo.
cp "$TEMPLATE_DIR/OPEN DENTALWAVE.bat" "$OUTPUT_DIR/"
cp "$TEMPLATE_DIR/CLOSE DENTALWAVE.bat" "$OUTPUT_DIR/"
cp "$TEMPLATE_DIR/BACKUP DATA.bat" "$PORTABLE_CONTENT_DIR/"
cp "$TEMPLATE_DIR/OPEN DIAGNOSTICS.bat" "$PORTABLE_CONTENT_DIR/"
cp "$TEMPLATE_DIR/README - START HERE.txt" "$PORTABLE_CONTENT_DIR/"
cp -R "$TEMPLATE_DIR/app/." "$PORTABLE_CONTENT_DIR/app/"
cp "$BACKEND_DIR/target/assistant-scheduler-0.0.1-SNAPSHOT.jar" \
    "$PORTABLE_CONTENT_DIR/app/dentalwave.jar"
cp -R "$RUNTIME_EXTRACT" "$PORTABLE_CONTENT_DIR/runtime"

echo "Portable distribution created at:"
echo "$OUTPUT_DIR"
