#!/usr/bin/env node
// Creates a new Android app from the template next to this script. Runs the same
// on Windows, macOS, and Linux.
// Usage: node new-app.mjs <app-name> [parent-folder] [--title "Daily Log"]
//          [--package com.acme.dailylog] [--scanner] [--no-build]
//   node new-app.mjs daily-log ~/dev --title "Daily Log"
// Creates <parent-folder>/<app-name>. If that folder exists, it must not have a
// settings.gradle.kts yet; existing files (like docs/BRIEF.md) are kept.
// app-name is lowercase letters, digits, and dashes.
import { execSync } from "node:child_process";
import {
  chmodSync, cpSync, existsSync, mkdirSync, readdirSync, readFileSync, renameSync,
  rmSync, statSync, writeFileSync,
} from "node:fs";
import { homedir } from "node:os";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const args = process.argv.slice(2);
const flag = (name) => {
  const index = args.indexOf(name);
  if (index === -1) return undefined;
  const [, value] = args.splice(index, 2);
  return value;
};
const bool = (name) => {
  const index = args.indexOf(name);
  if (index === -1) return false;
  args.splice(index, 1);
  return true;
};
const build = !bool("--no-build");
const scanner = bool("--scanner");
const titleArg = flag("--title");
const packageArg = flag("--package");
const [name, parent = process.cwd()] = args;

const usage = () => {
  console.error('usage: node new-app.mjs <app-name> [parent-folder] [--title "Daily Log"] [--package com.acme.dailylog] [--scanner] [--no-build]');
  console.error("app-name is lowercase letters, digits, and dashes, like tasks or daily-log");
  process.exit(1);
};
if (!name || !/^[a-z][a-z0-9-]*$/.test(name)) usage();

const title = titleArg ?? name.split("-").map((word) => word[0].toUpperCase() + word.slice(1)).join(" ");
const appPackage = packageArg ?? `com.example.${name.replaceAll("-", "")}`;
if (!/^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$/.test(appPackage)) {
  console.error(`"${appPackage}" is not a valid package name (lowercase segments joined by dots, like com.acme.dailylog)`);
  process.exit(1);
}

const skillDir = join(dirname(fileURLToPath(import.meta.url)), "..");
const template = join(skillDir, "template");
const scannerTemplate = join(skillDir, "template-scanner");
const dest = resolve(parent, name);

if (existsSync(join(dest, "settings.gradle.kts"))) {
  console.error(`${dest} already has a settings.gradle.kts`);
  process.exit(1);
}
if (scanner && !existsSync(scannerTemplate)) {
  console.error(`--scanner needs ${scannerTemplate}, which is missing`);
  process.exit(1);
}

// Copy the template and keep files that are already there.
const skip = new Set(["build", ".gradle", "local.properties", "shots", ".idea", "node_modules", ".git"]);
const copyTree = (from, to) =>
  cpSync(from, to, {
    recursive: true,
    force: false,
    errorOnExist: false,
    filter: (source) => !skip.has(source.split(/[\\/]/).pop()),
  });
mkdirSync(dest, { recursive: true });
copyTree(template, dest);

// A kept .gitignore gets the template lines it lacks.
const ignorePath = join(dest, ".gitignore");
const ours = readFileSync(join(template, ".gitignore"), "utf8").split(/\r?\n/).filter(Boolean);
const theirs = readFileSync(ignorePath, "utf8").split(/\r?\n/);
const missing = ours.filter((line) => !theirs.includes(line));
if (missing.length) writeFileSync(ignorePath, theirs.join("\n").replace(/\n*$/, "\n") + missing.join("\n") + "\n");

const read = (path) => readFileSync(path, "utf8");
const marker = /^.*baap:scanner-[\w-]+.*\r?\n/gm;
const appKt = join(dest, "app", "src", "main", "kotlin", "__APP_PACKAGE__", "App.kt");

if (scanner) {
  // The overlay adds a :scanner module, a scan test, and the lines that start the scanner.
  copyTree(join(scannerTemplate, "scanner"), join(dest, "scanner"));
  mkdirSync(join(dest, "app", "src", "test", "kotlin", "__APP_PACKAGE__"), { recursive: true });
  copyTree(join(scannerTemplate, "tests"), join(dest, "app", "src", "test", "kotlin", "__APP_PACKAGE__"));

  const settingsPath = join(dest, "settings.gradle.kts");
  writeFileSync(settingsPath, read(settingsPath).replace(/\s*$/, "\n") + 'include(":scanner")\n');

  const buildPath = join(dest, "app", "build.gradle.kts");
  writeFileSync(buildPath, read(buildPath).replace("dependencies {\n", 'dependencies {\n    implementation(project(":scanner"))\n'));

  const snippet = read(join(scannerTemplate, "App.start.snippet")).replace(/\s*$/, "\n");
  writeFileSync(appKt, read(appKt).replace(/^.*baap:scanner-start.*\r?\n/m, snippet));

  const claudePath = join(dest, "CLAUDE.md");
  writeFileSync(claudePath, read(claudePath).replace(/\s*$/, "\n") + "\n" + read(join(scannerTemplate, "CLAUDE.scanner.md")));
}

// Plain apps carry no scanner code and no overlay markers.
writeFileSync(appKt, read(appKt).replace(marker, ""));

// Move every __APP_PACKAGE__ folder to its nested package path.
const packagePath = appPackage.split(".");
(function moveFolders(dir) {
  for (const entry of readdirSync(dir)) {
    const path = join(dir, entry);
    if (!statSync(path).isDirectory() || skip.has(entry)) continue;
    if (entry === "__APP_PACKAGE__") {
      const target = join(dir, ...packagePath);
      const staging = join(dir, "__moving__");
      renameSync(path, staging);
      mkdirSync(dirname(target), { recursive: true });
      renameSync(staging, target);
    } else {
      moveFolders(path);
    }
  }
})(dest);

// Replace the placeholders in every text file.
(function walk(dir) {
  for (const entry of readdirSync(dir)) {
    const path = join(dir, entry);
    if (skip.has(entry)) continue;
    if (statSync(path).isDirectory()) walk(path);
    else if (/\.(kts|kt|xml|toml|properties|md|pro|json)$|^\.gitignore$/.test(entry)) {
      const text = read(path);
      const next = text
        .replaceAll("__APP_NAME__", name)
        .replaceAll("__APP_TITLE__", title)
        .replaceAll("__APP_PACKAGE__", appPackage);
      if (next !== text) writeFileSync(path, next);
    }
  }
})(dest);

// Point Gradle at the Android SDK when the environment does not.
const sdkCandidates = [
  process.env.ANDROID_HOME,
  process.env.ANDROID_SDK_ROOT,
  process.platform === "win32" && process.env.LOCALAPPDATA && join(process.env.LOCALAPPDATA, "Android", "Sdk"),
  process.platform === "darwin" && join(homedir(), "Library", "Android", "sdk"),
  process.platform === "linux" && join(homedir(), "Android", "Sdk"),
].filter(Boolean);
const sdk = sdkCandidates.find((path) => existsSync(path));
if (sdk && !process.env.ANDROID_HOME && !process.env.ANDROID_SDK_ROOT) {
  writeFileSync(join(dest, "local.properties"), `sdk.dir=${sdk.replaceAll("\\", "/")}\n`);
} else if (!sdk) {
  console.warn("No Android SDK found. See skills/start/new-android-app/setup-toolchain.md, then run scripts/doctor.mjs.");
}

if (process.platform !== "win32") chmodSync(join(dest, "gradlew"), 0o755);

const run = (command) => execSync(command, { cwd: dest, stdio: "inherit" });
// Downloads fail now and then on a slow network. One retry fixes most.
const runWithRetry = (command) => {
  try {
    run(command);
  } catch {
    console.log(`retrying: ${command}`);
    run(command);
  }
};
const gradlew = process.platform === "win32" ? ".\\gradlew.bat" : "./gradlew";
if (build) runWithRetry(`${gradlew} assembleDebug`);
if (!existsSync(join(dest, ".git"))) run("git init -q");
run("git add -A");
run(`git commit -qm "Create ${title} from the Android app template"`);
console.log(`created ${dest}`);
console.log(`next: cd "${dest}" && ${gradlew} check`);
