# CyberRaces — local build without GitHub Actions

**Development branch:** `development-no-actions`. Work is NOT being merged into
`main` while GitHub Actions minutes are unavailable.

## Windows setup (first time)

1. Install **Git for Windows** and **Eclipse Temurin JDK 17**. In PowerShell:
   ```powershell
   winget install --id Git.Git --exact
   winget install --id EclipseAdoptium.Temurin.17.JDK --exact
   ```
2. Close and reopen PowerShell. Check: `git --version`,
   `java -version`, and `javac -version` (Java compiler must say 17).
3. Clone from a folder you own, e.g. `Documents\CyberMods`:
   ```powershell
   git clone --branch development-no-actions https://github.com/Cyberspectraa/CyberRaces.git
   ```
4. Open the cloned `CyberRaces` directory. Double-click `build-local.bat` or
   run `.\build-local.bat` from PowerShell.

**You do not need to install Gradle yourself.** The launcher automatically
downloads exactly **Gradle 8.1.1** from the official Gradle distribution
service, verifies its official SHA-256 checksum, and caches it under
`%LOCALAPPDATA%\CyberMods\Gradle`. The initial build also downloads Forge
dependencies and requires an internet connection.

The launcher runs `gradle clean build --no-daemon --stacktrace`, keeping the
compilation log at `build-local.log`. Only after success it copies the
versioned mod JAR from `build\libs` to `CyberRaces.jar` at the repository root.

On future changes, open PowerShell in the repository directory:
```powershell
git switch development-no-actions
git pull --ff-only
.\build-local.bat
```

Do not upload/make public the `build-local.log` file without checking it for
personal paths. The script does not push, merge, trigger GitHub Actions, or
modify files in your Minecraft `mods` folder.

## Safety and status

- A commit alone is NOT a build. Keep stable JARs until local compilation
  succeeds and a backup world test passes.
- GitHub's existing workflows trigger on pushes/PRs to main. **Do not merge or
  create PRs targeting main** until Actions are available again.
- Java 17 is mandatory; later Java releases may not work with this Forge
  project. Use `javac -version` to check your current JDK.
- If local compilation fails, send the relevant lines from `build-local.log`
  and keep using the stable JAR.
- Linux/macOS: these Windows helpers do not apply. Use JDK 17 and Gradle
  8.1.1 locally and run `gradle clean build --no-daemon --stacktrace`.

## Current version

Development version **0.3.2-alpha.74**: new player choices exclude Dragonborn, Fairy, Birdfolk, Goblin and Dwarf; wild NPC choices retain Goblin and Dwarf. Existing players' removed-race abilities and safe migration are still pending. This is not yet validated by a local build.

## 0.3.2-alpha.75 race retirement

See [RACE_RETIREMENT.md](RACE_RETIREMENT.md). The final five retired
player races and automatic innate flight are now disabled in source.
Run a fresh local build and Minecraft test; this commit did NOT invoke Actions.
