import fs from 'node:fs';
import path from 'node:path';
import yaml from 'js-yaml';

export function loadYamlFile(filePath) {
  const raw = fs.readFileSync(filePath, 'utf8');
  return yaml.load(raw);
}

export function loadOpenApi(repoRoot = process.cwd()) {
  const filePath = path.join(repoRoot, 'docs/contracts/openapi.yaml');
  const doc = loadYamlFile(filePath);

  if (!doc || typeof doc !== 'object') {
    throw new Error('openapi.yaml is empty or invalid');
  }
  if (!String(doc.openapi || '').startsWith('3.')) {
    throw new Error('openapi.yaml must declare openapi 3.x');
  }
  if (!doc.paths || typeof doc.paths !== 'object') {
    throw new Error('openapi.yaml missing paths section');
  }

  const tags = new Set((doc.tags || []).map((tag) => tag.name));

  return {
    filePath,
    doc,
    tags,
    paths: Object.keys(doc.paths),
  };
}

export function loadAsyncApi(repoRoot = process.cwd()) {
  const filePath = path.join(repoRoot, 'docs/contracts/asyncapi.yaml');
  const doc = loadYamlFile(filePath);

  if (!doc || typeof doc !== 'object') {
    throw new Error('asyncapi.yaml is empty or invalid');
  }

  const version = String(doc.asyncapi || '');
  if (!version.startsWith('2.')) {
    throw new Error('asyncapi.yaml must declare asyncapi 2.x');
  }

  if (!doc.channels || typeof doc.channels !== 'object') {
    throw new Error('asyncapi.yaml missing channels section');
  }

  return {
    filePath,
    doc,
    channels: Object.keys(doc.channels),
  };
}
