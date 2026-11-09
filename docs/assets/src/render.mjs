// Renders the README banners and the plugin icon with headless Chrome or Edge.
// Usage: node docs/assets/src/render.mjs [path-to-chrome]
// Writes docs/assets/banner-light.png, docs/assets/banner-dark.png, .claude-plugin/icon.png.
// The banner font is Segoe UI, so render on Windows to match the committed PNGs.

import { execFileSync } from 'node:child_process';
import { existsSync, mkdtempSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const src = dirname(fileURLToPath(import.meta.url));
const root = resolve(src, '..', '..', '..');

const candidates = {
  win32: [
    'C:/Program Files/Google/Chrome/Application/chrome.exe',
    'C:/Program Files (x86)/Google/Chrome/Application/chrome.exe',
    'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
    'C:/Program Files/Microsoft/Edge/Application/msedge.exe',
  ],
  darwin: [
    '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
    '/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge',
  ],
  linux: ['/usr/bin/google-chrome', '/usr/bin/chromium', '/usr/bin/chromium-browser', '/usr/bin/microsoft-edge'],
}[process.platform] ?? [];

const browser = process.argv[2] ?? candidates.find((path) => existsSync(path));
if (!browser || !existsSync(browser)) {
  console.error('FAIL render: no Chrome or Edge found. Pass its path: node docs/assets/src/render.mjs <path>');
  process.exit(1);
}

const profile = mkdtempSync(join(tmpdir(), 'baap-render-'));
const shots = [
  { page: 'banner.html', hash: '', size: '860,260', scale: 2, out: 'docs/assets/banner-light.png' },
  { page: 'banner.html', hash: '#dark', size: '860,260', scale: 2, out: 'docs/assets/banner-dark.png' },
  { page: 'icon.html', hash: '', size: '512,512', scale: 1, out: '.claude-plugin/icon.png', transparent: true },
];

try {
  for (const shot of shots) {
    const out = join(root, shot.out);
    const args = [
      '--headless=new',
      '--disable-gpu',
      '--hide-scrollbars',
      '--no-first-run',
      `--user-data-dir=${profile}`,
      `--window-size=${shot.size}`,
      `--force-device-scale-factor=${shot.scale}`,
      `--screenshot=${out}`,
      ...(shot.transparent ? ['--default-background-color=00000000'] : []),
      pathToFileURL(join(src, shot.page)).href + shot.hash,
    ];
    execFileSync(browser, args, { stdio: 'ignore' });
    if (!existsSync(out)) throw new Error(`no file written: ${shot.out}`);
    console.log(`wrote ${shot.out}`);
  }
  console.log('PASS render: open each PNG and check it in light and dark.');
} catch (error) {
  console.error(`FAIL render: ${error.message}`);
  process.exitCode = 1;
} finally {
  rmSync(profile, { recursive: true, force: true });
}
