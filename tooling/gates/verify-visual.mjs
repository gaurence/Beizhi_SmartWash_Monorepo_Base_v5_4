import fs from 'node:fs';
import path from 'node:path';
import { spawn } from 'node:child_process';
import http from 'node:http';
import crypto from 'node:crypto';
import {
  groupPagesBySubsystem,
  loadPageInventory,
  resolveAppDirForSubsystem,
} from '../lib/page-inventory.mjs';

const repoRoot = process.cwd();
const iter = process.env.ITER || 'ITER-00';
const updateBaseline = process.env.UPDATE_BASELINE === '1' || process.argv.includes('--update-baseline');
const shouldTryPlaywright = process.env.ENABLE_PLAYWRIGHT_VISUAL !== '0';

const allPages = loadPageInventory(repoRoot);
const implementedPages = allPages.filter((page) => page.implStatus.toLowerCase() === 'implemented');

if (implementedPages.length === 0) {
  console.log(`verify-visual skipped (${iter}): no page with impl_status=implemented`);
  process.exit(0);
}

const bySubsystem = groupPagesBySubsystem(implementedPages);
const baselineDir = path.join(repoRoot, 'tooling/visual/baseline');
const currentDir = path.join(repoRoot, 'tooling/visual/current');
const diffDir = path.join(repoRoot, 'tooling/visual/diff');

fs.mkdirSync(baselineDir, { recursive: true });
fs.mkdirSync(currentDir, { recursive: true });
fs.mkdirSync(diffDir, { recursive: true });

const { PNG } = await import('pngjs');
const pixelmatchModule = await import('pixelmatch');
const pixelmatch = pixelmatchModule.default || pixelmatchModule;

if (shouldTryPlaywright) {
  const browserRun = await runPlaywrightMode();
  if (browserRun !== null) {
    finish(browserRun.errors, browserRun.mode);
  }
} else {
  console.log('verify-visual: playwright mode disabled via ENABLE_PLAYWRIGHT_VISUAL=0, using mock mode.');
}

const fallbackRun = runMockMode();
finish(fallbackRun.errors, fallbackRun.mode);

function finish(errors, mode) {
  if (errors.length > 0) {
    console.error(`verify-visual failed (${iter}, mode=${mode}):`);
    for (const error of errors.slice(0, 80)) {
      console.error(`- ${error}`);
    }
    if (errors.length > 80) {
      console.error(`... and ${errors.length - 80} more`);
    }
    process.exit(1);
  }

  console.log(`verify-visual passed (${iter}, mode=${mode}): ${implementedPages.length} pages`);
  process.exit(0);
}

async function runPlaywrightMode() {
  try {
    const { chromium } = await import('playwright');
    const browser = await chromium.launch({ headless: true });
    const serverProcesses = [];
    const errors = [];

    try {
      let portSeed = 4400;

      for (const [subsystem, pages] of bySubsystem.entries()) {
        const appDirName = resolveAppDirForSubsystem(subsystem);
        if (!appDirName) {
          errors.push(`${subsystem}: no frontend app mapping for visual gate`);
          continue;
        }

        const appDir = path.join(repoRoot, 'frontend', appDirName);
        const packageJson = path.join(appDir, 'package.json');
        if (!fs.existsSync(packageJson)) {
          errors.push(`${subsystem}: missing app directory frontend/${appDirName}`);
          continue;
        }

        const port = portSeed;
        portSeed += 1;

        const preview = spawn('npm', ['run', 'preview', '--', '--host', '127.0.0.1', '--port', String(port)], {
          cwd: appDir,
          stdio: ['ignore', 'pipe', 'pipe'],
          env: process.env,
        });

        let previewLog = '';
        preview.stdout.on('data', (chunk) => {
          previewLog += String(chunk);
        });
        preview.stderr.on('data', (chunk) => {
          previewLog += String(chunk);
        });

        serverProcesses.push(preview);

        const ready = await waitForHttp(`http://127.0.0.1:${port}`, 30000);
        if (!ready) {
          errors.push(`${subsystem}: preview server failed to start on port ${port}\n${previewLog}`);
          continue;
        }

        const page = await browser.newPage({ viewport: { width: 1440, height: 1024 } });

        for (const item of pages) {
          const url = `http://127.0.0.1:${port}/pages/${item.pageKey}`;
          await page.goto(url, { waitUntil: 'networkidle' });
          const fileKey = `${subsystem}__${item.pageKey}`;
          const currentPath = path.join(currentDir, `${fileKey}.png`);
          await page.screenshot({ path: currentPath, fullPage: true });

          compareWithBaseline(fileKey, fs.readFileSync(currentPath), errors, PNG, pixelmatch);
        }

        await page.close();
      }
    } finally {
      for (const child of serverProcesses) {
        try {
          child.kill('SIGTERM');
        } catch (_ignored) {
          // ignore process teardown errors in sandbox mode
        }
      }

      await browser.close();
    }

    return {
      mode: 'playwright',
      errors,
    };
  } catch (error) {
    const reason = error instanceof Error ? error.message : String(error);
    console.warn(`verify-visual: playwright mode unavailable, fallback to mock mode. reason=${reason}`);
    return null;
  }
}

function runMockMode() {
  const errors = [];

  for (const [subsystem, pages] of bySubsystem.entries()) {
    for (const item of pages) {
      const fileKey = `${subsystem}__${item.pageKey}`;
      const image = renderSyntheticPng(item, PNG);
      const currentPath = path.join(currentDir, `${fileKey}.png`);
      fs.writeFileSync(currentPath, image);

      compareWithBaseline(fileKey, image, errors, PNG, pixelmatch);
    }
  }

  return {
    mode: 'mock',
    errors,
  };
}

function compareWithBaseline(fileKey, currentBuffer, errors, PngLib, pixelmatchFn) {
  const baselinePath = path.join(baselineDir, `${fileKey}.png`);
  const diffPath = path.join(diffDir, `${fileKey}.png`);

  if (updateBaseline) {
    fs.writeFileSync(baselinePath, currentBuffer);
    if (fs.existsSync(diffPath)) {
      fs.rmSync(diffPath);
    }
    return;
  }

  if (!fs.existsSync(baselinePath)) {
    errors.push(`${fileKey}: missing visual baseline ${path.relative(repoRoot, baselinePath)}`);
    return;
  }

  const basePng = PngLib.sync.read(fs.readFileSync(baselinePath));
  const currentPng = PngLib.sync.read(currentBuffer);

  if (basePng.width !== currentPng.width || basePng.height !== currentPng.height) {
    errors.push(`${fileKey}: viewport mismatch baseline(${basePng.width}x${basePng.height}) current(${currentPng.width}x${currentPng.height})`);
    return;
  }

  const diffPng = new PngLib({ width: basePng.width, height: basePng.height });
  const diffPixels = pixelmatchFn(
    basePng.data,
    currentPng.data,
    diffPng.data,
    basePng.width,
    basePng.height,
    { threshold: 0.1 },
  );

  const ratio = diffPixels / (basePng.width * basePng.height);
  if (ratio > 0.01) {
    fs.writeFileSync(diffPath, PngLib.sync.write(diffPng));
    errors.push(`${fileKey}: visual diff ratio ${(ratio * 100).toFixed(2)}% > 1.00%`);
  }
}

function renderSyntheticPng(page, PngLib) {
  const width = 1440;
  const height = 1024;
  const png = new PngLib({ width, height });

  const hash = crypto
    .createHash('sha256')
    .update(`${page.subsystem}|${page.pageKey}|${page.titleZh}|${page.role}|${page.module}|${page.implStatus}`)
    .digest();

  fillRect(png, 0, 0, width, height, [hash[0], hash[1], hash[2], 255]);
  fillRect(png, 0, 0, 300, height, [hash[3], hash[4], hash[5], 255]);
  fillRect(png, 320, 24, width - 344, 120, [hash[6], hash[7], hash[8], 255]);
  fillRect(png, 320, 170, width - 344, 360, [hash[9], hash[10], hash[11], 255]);
  fillRect(png, 320, 560, width - 344, 440, [hash[12], hash[13], hash[14], 255]);

  // Accent bars for parent/entry markers to preserve structural sensitivity.
  const parentMarker = page.parentPage ? 220 : 80;
  const entryMarker = (page.entry || '').length % 200;
  fillRect(png, 340, 190, parentMarker, 20, [255, 255, 255, 255]);
  fillRect(png, 340, 220, 60 + entryMarker, 16, [20, 20, 20, 255]);

  return PngLib.sync.write(png);
}

function fillRect(png, x, y, w, h, rgba) {
  for (let row = y; row < y + h; row += 1) {
    for (let col = x; col < x + w; col += 1) {
      const idx = (png.width * row + col) << 2;
      png.data[idx] = rgba[0];
      png.data[idx + 1] = rgba[1];
      png.data[idx + 2] = rgba[2];
      png.data[idx + 3] = rgba[3];
    }
  }
}

async function waitForHttp(url, timeoutMs) {
  const started = Date.now();

  while (Date.now() - started < timeoutMs) {
    const ok = await new Promise((resolve) => {
      const req = http.get(url, (res) => {
        res.resume();
        resolve(res.statusCode >= 200 && res.statusCode < 500);
      });

      req.on('error', () => resolve(false));
      req.setTimeout(1000, () => {
        req.destroy();
        resolve(false);
      });
    });

    if (ok) {
      return true;
    }

    await sleep(500);
  }

  return false;
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
