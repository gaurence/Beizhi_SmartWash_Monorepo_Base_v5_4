import fs from 'node:fs';
import path from 'node:path';
import { loadPageInventory } from '../lib/page-inventory.mjs';

const repoRoot = process.cwd();

const requiredFiles = [
  'AGENTS.md',
  'docs/requirements/Beizhi_v5_4.md',
  'docs/design/Beizhi_DDD_Detailed_Design_v5_4.md',
  'docs/ui/page_inventory.csv',
  'docs/traceability/page_to_api.md',
  'docs/contracts/openapi.yaml',
  'docs/contracts/asyncapi.yaml',
];

const missing = [];
for (const file of requiredFiles) {
  const abs = path.join(repoRoot, file);
  if (!fs.existsSync(abs)) {
    missing.push(file);
  }
}

const pages = loadPageInventory(repoRoot);
const missingPageFiles = [];
for (const page of pages) {
  const svgAbs = path.join(repoRoot, page.svgPath);
  const mdAbs = path.join(repoRoot, page.mdPath);
  if (!fs.existsSync(svgAbs)) {
    missingPageFiles.push(`${page.pageKey}: missing svg ${page.svgPath}`);
  }
  if (!fs.existsSync(mdAbs)) {
    missingPageFiles.push(`${page.pageKey}: missing md ${page.mdPath}`);
  }
}

if (missing.length > 0 || missingPageFiles.length > 0) {
  if (missing.length > 0) {
    console.error('Missing required docs:');
    for (const file of missing) {
      console.error(`- ${file}`);
    }
  }

  if (missingPageFiles.length > 0) {
    console.error('Missing UI reference files:');
    for (const item of missingPageFiles.slice(0, 50)) {
      console.error(`- ${item}`);
    }
    if (missingPageFiles.length > 50) {
      console.error(`... and ${missingPageFiles.length - 50} more`);
    }
  }

  process.exit(1);
}

console.log(`verify-docs passed: ${requiredFiles.length} core files + ${pages.length} page SVG/MD references`);
