#!/usr/bin/env python3
"""Static regression checks for Windows USB paths without requiring Windows."""

import ntpath
import unittest
from pathlib import Path


REPOSITORY = Path(__file__).resolve().parents[2]
WINDOWS_SOURCE = REPOSITORY / "portable" / "windows"


class WindowsLauncherPathTests(unittest.TestCase):
    def test_batch_files_do_not_pass_a_trailing_slash_root_argument(self):
        expected_scripts = {
            "OPEN DENTALWAVE.bat": (r"OtherInfo\app", "start-dentalwave.ps1"),
            "CLOSE DENTALWAVE.bat": (r"OtherInfo\app", "stop-dentalwave.ps1"),
            "BACKUP DATA.bat": ("app", "backup-dentalwave.ps1"),
            "OPEN DIAGNOSTICS.bat": ("app", "diagnose-dentalwave.ps1"),
        }

        for batch_name, (script_folder, script_name) in expected_scripts.items():
            with self.subTest(batch=batch_name):
                content = (WINDOWS_SOURCE / batch_name).read_text(encoding="utf-8")
                self.assertNotIn('-Root "%~dp0"', content)
                self.assertIn(
                    f'-File "%~dp0{script_folder}\\{script_name}"', content
                )

    def test_build_keeps_only_daily_launchers_at_the_usb_root(self):
        content = (REPOSITORY / "build-portable.sh").read_text(encoding="utf-8")
        self.assertIn('PORTABLE_CONTENT_DIR="$OUTPUT_DIR/OtherInfo"', content)
        self.assertIn(
            'cp "$TEMPLATE_DIR/OPEN DENTALWAVE.bat" "$OUTPUT_DIR/"', content
        )
        self.assertIn(
            'cp "$TEMPLATE_DIR/CLOSE DENTALWAVE.bat" "$OUTPUT_DIR/"', content
        )
        self.assertIn(
            'cp -R "$TEMPLATE_DIR/app/." "$PORTABLE_CONTENT_DIR/app/"', content
        )

    def test_powershell_scripts_derive_root_from_their_own_location(self):
        for script_name in (
            "start-dentalwave.ps1",
            "stop-dentalwave.ps1",
            "backup-dentalwave.ps1",
            "diagnose-dentalwave.ps1",
        ):
            with self.subTest(script=script_name):
                content = (WINDOWS_SOURCE / "app" / script_name).read_text(
                    encoding="utf-8"
                )
                self.assertIn("$PSScriptRoot", content)
                self.assertIn("Split-Path -Parent $PSScriptRoot", content)
                self.assertNotIn("GetFullPath", content)

    def test_startup_requires_the_html_and_javascript_before_opening_browser(self):
        content = (WINDOWS_SOURCE / "app" / "start-dentalwave.ps1").read_text(
            encoding="utf-8"
        )
        self.assertIn("Test-DentalWaveBrowserPage", content)
        self.assertIn("Invoke-WebRequest -UseBasicParsing", content)
        self.assertIn("/assets/", content)
        self.assertIn("validating the packaged DentalWave browser page", content)

    def test_startup_recovers_an_unresponsive_existing_instance(self):
        content = (WINDOWS_SOURCE / "app" / "start-dentalwave.ps1").read_text(
            encoding="utf-8"
        )
        self.assertIn("Stop-UnresponsiveDentalWave", content)
        self.assertIn('"$url/api/portable/shutdown"', content)
        self.assertIn('"X-DentalWave-Control" = $controlToken', content)
        self.assertIn("restarting an unresponsive DentalWave instance", content)

    def test_close_fallback_targets_only_one_verified_dentalwave_process(self):
        content = (WINDOWS_SOURCE / "app" / "stop-dentalwave.ps1").read_text(
            encoding="utf-8"
        )
        self.assertIn("Get-VerifiedDentalWaveProcess", content)
        self.assertIn('CommandLine -like "*dentalwave.jar*"', content)
        self.assertIn(
            'CommandLine -like "*--spring.profiles.active=portable*"', content
        )
        self.assertIn("$matches.Count -eq 1", content)
        self.assertIn("Stop-Process -Id $dentalWaveProcess.ProcessId -Force", content)

    def test_diagnostics_are_opened_in_notepad(self):
        content = (WINDOWS_SOURCE / "app" / "diagnose-dentalwave.ps1").read_text(
            encoding="utf-8"
        )
        self.assertIn("DENTALWAVE DIAGNOSTICS.txt", content)
        self.assertIn('Start-Process -FilePath "notepad.exe"', content)

    def test_all_portable_paths_are_valid_for_expected_space_and_drive_roots(self):
        installation_roots = (
            r"D:\DentalWave Scheduler",
            r"E:\DentalWave Scheduler",
            r"F:\Office Apps\DentalWave Scheduler",
        )
        relative_paths = (
            r"app\dentalwave.jar",
            r"runtime\bin\java.exe",
            "data",
            r"data\dentalwave",
            r"data\jwt.secret",
            r"data\control.secret",
            r"data\dentalwave.pid",
            "logs",
            "backups",
        )

        for installation_root in installation_roots:
            with self.subTest(root=installation_root):
                expected_root = ntpath.join(installation_root, "OtherInfo")
                script_directory = ntpath.join(expected_root, "app")
                derived_root = ntpath.dirname(script_directory)
                self.assertEqual(expected_root, derived_root)

                expected_drive = ntpath.splitdrive(expected_root)[0]
                for relative_path in relative_paths:
                    constructed = ntpath.join(derived_root, relative_path)
                    self.assertNotIn('"', constructed)
                    self.assertEqual(expected_drive, ntpath.splitdrive(constructed)[0])


if __name__ == "__main__":
    unittest.main(verbosity=2)
