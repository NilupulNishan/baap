#!/usr/bin/env node
// Checks that this machine can build an Android app. Prints PASS, or one fix per problem.
// Usage: node doctor.mjs
import { spawnSync } from "node:child_process";
import { existsSync, readdirSync } from "node:fs";
import { homedir } from "node:os";
import { join } from "node:path";

const help = "see skills/start/new-android-app/setup-toolchain.md";
const problems = [];
const ok = (message) => console.log(`ok    ${message}`);
const bad = (message, fix) => {
  problems.push(message);
  console.log(`FAIL  ${message}\n      fix: ${fix}`);
};

// 1. A JDK, version 17 or later.
const javaHome = process.env.JAVA_HOME;
const javaBin = javaHome ? join(javaHome, "bin", process.platform === "win32" ? "java.exe" : "java") : "java";
const java = spawnSync(javaBin, ["-version"], { encoding: "utf8" });
if (java.error || java.status !== 0) {
  bad("java is not available", `install JDK 17 or later and set JAVA_HOME (${help})`);
} else {
  const major = Number((`${java.stderr}${java.stdout}`.match(/version "(\d+)(?:\.(\d+))?/) ?? [])[1]);
  const version = major === 1 ? 8 : major;
  if (version >= 17) ok(`java ${version}`);
  else bad(`java ${version} is too old`, `install JDK 17 or later and point JAVA_HOME at it (${help})`);
}

// 2. The Android SDK.
const candidates = [
  process.env.ANDROID_HOME,
  process.env.ANDROID_SDK_ROOT,
  process.platform === "win32" && process.env.LOCALAPPDATA && join(process.env.LOCALAPPDATA, "Android", "Sdk"),
  process.platform === "darwin" && join(homedir(), "Library", "Android", "sdk"),
  process.platform === "linux" && join(homedir(), "Android", "Sdk"),
].filter(Boolean);
const sdk = candidates.find((path) => existsSync(path));
if (!sdk) {
  bad("no Android SDK found", `install it and set ANDROID_HOME (${help})`);
} else {
  ok(`Android SDK at ${sdk}`);
  const adb = join(sdk, "platform-tools", process.platform === "win32" ? "adb.exe" : "adb");
  if (existsSync(adb)) ok("platform-tools");
  else bad("platform-tools is missing", 'run: sdkmanager "platform-tools"');

  const folder = (name) => (existsSync(join(sdk, name)) ? readdirSync(join(sdk, name)) : []);
  const platforms = folder("platforms").filter((entry) => entry.startsWith("android-"));
  if (platforms.length) ok(`platform ${platforms.sort().at(-1)}`);
  else bad("no Android platform is installed", 'run: sdkmanager "platforms;android-37.0"');

  const tools = folder("build-tools");
  if (tools.length) ok(`build-tools ${tools.sort().at(-1)}`);
  else bad("build-tools is missing", 'run: sdkmanager "build-tools;37.0.0"');
}

// 3. Node and git, for the scaffold script.
const nodeMajor = Number(process.versions.node.split(".")[0]);
if (nodeMajor >= 20) ok(`node ${process.versions.node}`);
else bad(`node ${process.versions.node} is too old`, "install Node 20 or later");
const git = spawnSync("git", ["--version"], { encoding: "utf8" });
if (git.status === 0) ok(git.stdout.trim());
else bad("git is not available", "install git");

if (problems.length) {
  console.error(`${problems.length} problem(s)`);
  process.exit(1);
}
console.log("PASS doctor");
