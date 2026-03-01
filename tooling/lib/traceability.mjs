import fs from 'node:fs';
import path from 'node:path';

function isSeparatorLine(line) {
  return /^\|(?:\s*:?-+:?\s*\|)+$/.test(line.trim());
}

function splitPipeLine(line) {
  const trimmed = line.trim();
  if (!trimmed.startsWith('|')) {
    return null;
  }
  return trimmed
    .split('|')
    .slice(1, -1)
    .map((cell) => cell.trim());
}

export function loadTraceabilityRows(repoRoot = process.cwd()) {
  const filePath = path.join(repoRoot, 'docs/traceability/page_to_api.md');
  const text = fs.readFileSync(filePath, 'utf8');
  const lines = text.split(/\r?\n/);

  const rows = [];
  let header = null;
  let headerIndex = null;

  for (const line of lines) {
    const cells = splitPipeLine(line);
    if (!cells) {
      header = null;
      headerIndex = null;
      continue;
    }

    if (isSeparatorLine(line)) {
      continue;
    }

    if (!header) {
      header = cells;
      headerIndex = Object.fromEntries(header.map((name, idx) => [name, idx]));
      continue;
    }

    if (headerIndex.page_key === undefined) {
      continue;
    }

    const pageKey = (cells[headerIndex.page_key] || '').trim();
    if (!pageKey || !/^[A-Z]{2}_[A-Za-z0-9_]+$/.test(pageKey)) {
      continue;
    }

    rows.push({
      pageKey,
      pageTitle: readByName(cells, headerIndex, ['页面中文名']),
      role: readByName(cells, headerIndex, ['角色']),
      module: readByName(cells, headerIndex, ['模块']),
      entry: readByName(cells, headerIndex, ['入口']),
      parentPage: readByName(cells, headerIndex, ['父页面']),
      apiTag: readByName(cells, headerIndex, ['api_tag']),
      permissionKey: readByName(cells, headerIndex, ['permission_key']),
      apiEndpoints: readByName(cells, headerIndex, ['api_endpoints']),
    });
  }

  return {
    filePath,
    rows,
  };
}

function readByName(cells, indexMap, names) {
  for (const name of names) {
    if (indexMap[name] !== undefined) {
      return (cells[indexMap[name]] || '').trim();
    }
  }
  return '';
}
