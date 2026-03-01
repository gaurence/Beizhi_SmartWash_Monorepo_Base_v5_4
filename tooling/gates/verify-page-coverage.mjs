import fs from 'node:fs';
import path from 'node:path';
import {
  ensureKnownSubsystems,
  groupPagesBySubsystem,
  loadPageInventory,
  resolveAppDirForSubsystem,
} from '../lib/page-inventory.mjs';

const repoRoot = process.cwd();
const iter = process.env.ITER || 'ITER-00';
const pages = loadPageInventory(repoRoot);
ensureKnownSubsystems(pages);

const bySubsystem = groupPagesBySubsystem(pages);
const errors = [];

for (const [subsystem, subsystemPages] of bySubsystem.entries()) {
  const appDir = resolveAppDirForSubsystem(subsystem);
  const generatedJson = path.join(repoRoot, 'frontend', appDir, 'generated', 'pages.json');
  const routerFile = path.join(repoRoot, 'frontend', appDir, 'src', 'router', 'index.ts');

  if (!fs.existsSync(generatedJson)) {
    errors.push(`${subsystem}: missing generated file frontend/${appDir}/generated/pages.json`);
    continue;
  }

  if (!fs.existsSync(routerFile)) {
    errors.push(`${subsystem}: missing router file frontend/${appDir}/src/router/index.ts`);
    continue;
  }

  const generated = JSON.parse(fs.readFileSync(generatedJson, 'utf8'));
  const generatedKeys = new Set(generated.map((item) => item.pageKey));

  for (const page of subsystemPages) {
    if (!generatedKeys.has(page.pageKey)) {
      errors.push(`${subsystem}: page ${page.pageKey} missing in generated/pages.json`);
      continue;
    }

    const row = generated.find((item) => item.pageKey === page.pageKey);
    if (!row.routePath || !row.routePath.endsWith(page.pageKey)) {
      errors.push(`${subsystem}: page ${page.pageKey} routePath is invalid`);
    }

    if (page.parentPage) {
      const parentExists = generatedKeys.has(page.parentPage);
      if (!parentExists) {
        errors.push(`${subsystem}: page ${page.pageKey} parent ${page.parentPage} missing`);
      }
    }
  }

  const routerSource = fs.readFileSync(routerFile, 'utf8');
  if (!routerSource.includes('/pages/:pageKey')) {
    errors.push(`${subsystem}: router missing /pages/:pageKey route`);
  }
}

if (errors.length > 0) {
  console.error(`verify-page-coverage failed for ${iter}:`);
  for (const error of errors.slice(0, 120)) {
    console.error(`- ${error}`);
  }
  if (errors.length > 120) {
    console.error(`... and ${errors.length - 120} more`);
  }
  process.exit(1);
}

console.log(`verify-page-coverage passed (${iter}): ${pages.length} pages across ${bySubsystem.size} subsystems`);
