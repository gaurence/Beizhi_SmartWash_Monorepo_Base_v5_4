import fs from 'node:fs';
import path from 'node:path';
import { parseCsv } from './csv.mjs';

export const SUBSYSTEM_TO_APP = {
  Agent_Admin_Portal: 'agent-admin',
  Cabinet_Screen: 'cabinet-screen',
  Customer_App: 'customer-app',
  Device_Investor_Portal: 'device-investor',
  Platform_Admin_Portal: 'platform-admin',
  Platform_CS_Portal: 'platform-cs',
  Platform_Finance_Portal: 'platform-finance',
  Platform_IoTOps_Portal: 'platform-iotops',
  Platform_Ops_Portal: 'platform-ops',
  Property_Portal: 'property-portal',
  Store_Admin_Portal: 'store-admin',
  Technician_App: 'technician-app',
};

function normalizeHeader(value) {
  return value.trim().replace(/^\uFEFF/, '');
}

export function loadPageInventory(repoRoot = process.cwd()) {
  const csvPath = path.join(repoRoot, 'docs/ui/page_inventory.csv');
  const raw = fs.readFileSync(csvPath, 'utf8');
  const rows = parseCsv(raw);

  if (rows.length < 2) {
    throw new Error('page_inventory.csv is empty');
  }

  const header = rows[0].map(normalizeHeader);
  const idx = Object.fromEntries(header.map((name, i) => [name, i]));

  const requiredCols = [
    'page_key',
    '中文名',
    '角色',
    '模块',
    '父页面',
    '入口',
    'subsystem',
    'svg_path',
    'md_path',
    'impl_status',
  ];

  for (const col of requiredCols) {
    if (idx[col] === undefined) {
      throw new Error(`page_inventory.csv missing required column: ${col}`);
    }
  }

  const pages = rows
    .slice(1)
    .filter((row) => row[idx.page_key] && row[idx.subsystem])
    .map((row) => ({
      pageKey: (row[idx.page_key] || '').trim(),
      titleZh: readByAlias(row, idx, ['中文名', 'titleZh']),
      role: readByAlias(row, idx, ['角色', 'role']),
      module: readByAlias(row, idx, ['模块', 'module']),
      parentPage: readByAlias(row, idx, ['父页面', 'parentPage']),
      entry: readByAlias(row, idx, ['入口', 'entry']),
      subsystem: (row[idx.subsystem] || '').trim(),
      svgPath: (row[idx.svg_path] || '').trim(),
      mdPath: (row[idx.md_path] || '').trim(),
      implStatus: (row[idx.impl_status] || '').trim(),
    }))
    .filter((page) => page.subsystem !== '_Common');

  return pages;
}

function readByAlias(row, idxMap, aliases) {
  for (const alias of aliases) {
    if (idxMap[alias] !== undefined) {
      return (row[idxMap[alias]] || '').trim();
    }
  }
  return '';
}

export function groupPagesBySubsystem(pages) {
  const map = new Map();

  for (const page of pages) {
    if (!map.has(page.subsystem)) {
      map.set(page.subsystem, []);
    }
    map.get(page.subsystem).push(page);
  }

  for (const list of map.values()) {
    list.sort((a, b) => a.pageKey.localeCompare(b.pageKey));
  }

  return map;
}

export function resolveAppDirForSubsystem(subsystem) {
  return SUBSYSTEM_TO_APP[subsystem] || null;
}

export function ensureKnownSubsystems(pages) {
  const unknown = [...new Set(
    pages
      .map((page) => page.subsystem)
      .filter((subsystem) => !SUBSYSTEM_TO_APP[subsystem]),
  )];

  if (unknown.length > 0) {
    throw new Error(`No frontend app mapping for subsystem(s): ${unknown.join(', ')}`);
  }
}
