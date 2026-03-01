import { loadOpenApi } from '../lib/contracts.mjs';
import { loadPageInventory } from '../lib/page-inventory.mjs';
import { loadTraceabilityRows } from '../lib/traceability.mjs';

const repoRoot = process.cwd();
const iter = process.env.ITER || 'ITER-00';

const inventoryPages = loadPageInventory(repoRoot);
const { rows } = loadTraceabilityRows(repoRoot);
const { tags, paths } = loadOpenApi(repoRoot);

const pageKeys = new Set(inventoryPages.map((page) => page.pageKey));
const rowsByPage = new Map();
for (const row of rows) {
  if (!rowsByPage.has(row.pageKey)) {
    rowsByPage.set(row.pageKey, row);
  }
}

const openApiPaths = new Set(paths);
const errors = [];
const virtualTags = new Set(['ui-static', 'app', 'asset', 'reports']);

for (const page of inventoryPages) {
  const row = rowsByPage.get(page.pageKey);
  if (!row) {
    errors.push(`${page.pageKey}: missing row in traceability matrix`);
    continue;
  }

  if (!row.apiTag) {
    errors.push(`${page.pageKey}: api_tag is empty`);
  }

  if (!row.permissionKey) {
    errors.push(`${page.pageKey}: permission_key is empty`);
  }

  if (row.apiTag && !tags.has(row.apiTag) && !virtualTags.has(row.apiTag)) {
    errors.push(`${page.pageKey}: api_tag '${row.apiTag}' not found in openapi tags`);
  }

  const endpoints = row.apiEndpoints
    ? row.apiEndpoints
        .split(';')
        .map((item) => item.trim())
        .filter(Boolean)
    : [];

  if (row.apiTag !== 'ui-static' && endpoints.length === 0) {
    errors.push(`${page.pageKey}: api_endpoints is empty for non ui-static page`);
  }

  for (const endpoint of endpoints) {
    const match = endpoint.match(/^(GET|POST|PUT|PATCH|DELETE|OPTIONS|HEAD)\s+(.+)$/i);
    if (!match) {
      errors.push(`${page.pageKey}: invalid endpoint format '${endpoint}'`);
      continue;
    }

    const endpointPath = match[2].trim();
    if (!openApiPaths.has(endpointPath)) {
      errors.push(`${page.pageKey}: endpoint path '${endpointPath}' not found in openapi`);
    }
  }
}

for (const row of rows) {
  if (!pageKeys.has(row.pageKey)) {
    errors.push(`traceability has unknown page_key '${row.pageKey}'`);
  }
}

if (errors.length > 0) {
  console.error(`verify-traceability failed for ${iter}:`);
  for (const error of errors.slice(0, 140)) {
    console.error(`- ${error}`);
  }
  if (errors.length > 140) {
    console.error(`... and ${errors.length - 140} more`);
  }
  process.exit(1);
}

console.log(`verify-traceability passed (${iter}): ${inventoryPages.length} pages mapped`);
