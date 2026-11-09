#!/usr/bin/env node
// Lints the skills in this repo. Exits 1 and lists every problem.
// Usage: node scripts/check-skills.mjs [--strict]
//   --strict also fails when plugin.json lists a skill folder that does not exist yet.
// No dependencies. Runs on Windows, macOS, and Linux.
import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";
import { dirname, join, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const strict = process.argv.includes("--strict");
const repo = join(dirname(fileURLToPath(import.meta.url)), "..");
const skillsRoot = join(repo, "skills");
const problems = [];
const notes = [];
const fail = (message) => problems.push(message);

const isDir = (path) => existsSync(path) && statSync(path).isDirectory();
const read = (path) => readFileSync(path, "utf8");
const rel = (path) => relative(repo, path).replaceAll("\\", "/");

// Collect skills: skills/<stage>/<name>/
const skills = [];
if (isDir(skillsRoot)) {
  for (const stage of readdirSync(skillsRoot)) {
    if (!isDir(join(skillsRoot, stage))) continue;
    for (const name of readdirSync(join(skillsRoot, stage))) {
      const dir = join(skillsRoot, stage, name);
      if (isDir(dir)) skills.push({ stage, name, dir });
    }
  }
}

function frontmatter(text) {
  const match = text.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n?([\s\S]*)$/);
  if (!match) return null;
  const fields = {};
  for (const line of match[1].split(/\r?\n/)) {
    const pair = line.match(/^([A-Za-z][\w-]*):\s*(.*)$/);
    if (pair) fields[pair[1]] = pair[2].replace(/^"(.*)"$/, "$1");
  }
  return { fields, body: match[2] };
}

function markdownFiles(dir) {
  const out = [];
  for (const entry of readdirSync(dir)) {
    const path = join(dir, entry);
    if (entry === "node_modules" || entry === "template" || entry === "template-scanner") continue;
    if (statSync(path).isDirectory()) out.push(...markdownFiles(path));
    else if (entry.endsWith(".md")) out.push(path);
  }
  return out;
}

const router = join(skillsRoot, "start", "baap", "SKILL.md");
const routerText = existsSync(router) ? read(router) : null;
const readmePath = join(repo, "README.md");
const readmeText = existsSync(readmePath) ? read(readmePath) : null;

for (const skill of skills) {
  const where = `skills/${skill.stage}/${skill.name}`;
  const skillFile = join(skill.dir, "SKILL.md");
  if (!existsSync(skillFile)) {
    fail(`${where}: SKILL.md is missing`);
    continue;
  }
  if (!existsSync(join(skill.dir, "agents", "openai.yaml"))) fail(`${where}: agents/openai.yaml is missing`);

  const parsed = frontmatter(read(skillFile));
  if (!parsed) {
    fail(`${where}: SKILL.md has no frontmatter`);
    continue;
  }
  const { fields, body } = parsed;
  if (fields.name !== skill.name) fail(`${where}: frontmatter name "${fields.name}" must equal the folder name "${skill.name}"`);
  if (!fields.description) fail(`${where}: description is missing`);
  else if (skill.name !== "baap" && !fields.description.includes("Use when")) fail(`${where}: description needs "Use when"`);
  if (!/Done when/.test(body)) fail(`${where}: body needs a "Done when" line`);
  if (skill.stage === "enterprise" && !/^## Sources/m.test(body)) fail(`${where}: enterprise skills end with "## Sources"`);

  // Every skill is listed in the router and the README table.
  if (routerText !== null && skill.name !== "baap" && !routerText.includes(`\`${skill.name}\``)) fail(`${where}: not in the router (skills/start/baap/SKILL.md)`);
  if (readmeText !== null && !readmeText.includes(`skills/${skill.stage}/${skill.name}/SKILL.md`)) fail(`${where}: not in the README skills table`);
}

// No em dashes or en dashes, and every relative .md link resolves.
if (isDir(skillsRoot)) {
  for (const file of markdownFiles(skillsRoot)) {
    const text = read(file);
    if (/[–—]/.test(text)) fail(`${rel(file)}: has an em dash or en dash`);
    for (const link of text.matchAll(/\]\(([^)#\s]+\.md)(#[^)]*)?\)/g)) {
      const target = link[1];
      if (/^[a-z]+:/i.test(target)) continue;
      if (!existsSync(resolve(dirname(file), target))) fail(`${rel(file)}: broken link to ${target}`);
    }
  }
}

// plugin.json lists every skill, and every listed path exists.
const pluginPath = join(repo, ".claude-plugin", "plugin.json");
if (!existsSync(pluginPath)) fail(".claude-plugin/plugin.json is missing");
else {
  const plugin = JSON.parse(read(pluginPath));
  const listed = new Set((plugin.skills ?? []).map((path) => path.replace(/^\.\//, "")));
  for (const skill of skills) {
    if (!listed.has(`skills/${skill.stage}/${skill.name}`)) fail(`skills/${skill.stage}/${skill.name}: not in plugin.json "skills"`);
  }
  for (const path of listed) {
    if (!isDir(join(repo, path))) (strict ? fail : (m) => notes.push(m))(`plugin.json lists ${path}, which does not exist yet`);
  }
  if (strict && skills.length !== listed.size) fail(`${skills.length} skill folders but ${listed.size} entries in plugin.json`);
}

// AGENTS.md is a copy of .claude/CLAUDE.md.
const claudeMd = join(repo, ".claude", "CLAUDE.md");
const agentsMd = join(repo, "AGENTS.md");
if (existsSync(claudeMd) && existsSync(agentsMd) && read(claudeMd) !== read(agentsMd)) fail("AGENTS.md and .claude/CLAUDE.md differ; keep them identical");

for (const note of notes) console.log(`note: ${note}`);
if (problems.length) {
  for (const problem of problems) console.error(`FAIL ${problem}`);
  console.error(`${problems.length} problem(s)`);
  process.exit(1);
}
console.log(`PASS check-skills: ${skills.length} skills${strict ? " (strict)" : ""}`);
