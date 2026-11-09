#!/usr/bin/env node
// Links every skill in this repo into ~/.claude/skills and ~/.agents/skills,
// so edits in the repo show up in Claude Code and Codex at once. On Windows
// it makes directory junctions, which need no admin rights.
// Usage: node scripts/link-skills.mjs
import { lstatSync, mkdirSync, readdirSync, rmSync, symlinkSync } from "node:fs";
import { homedir } from "node:os";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const repo = join(dirname(fileURLToPath(import.meta.url)), "..");
const stages = join(repo, "skills");

for (const target of [join(homedir(), ".claude", "skills"), join(homedir(), ".agents", "skills")]) {
  mkdirSync(target, { recursive: true });
  for (const stage of readdirSync(stages)) {
    for (const name of readdirSync(join(stages, stage))) {
      const link = join(target, name);
      const existing = lstatSync(link, { throwIfNoEntry: false });
      if (existing) {
        if (!existing.isSymbolicLink()) {
          console.log(`skipped ${link}: a real folder is there`);
          continue;
        }
        rmSync(link);
      }
      symlinkSync(join(stages, stage, name), link, "junction");
      console.log(link);
    }
  }
}
