import { loadOpenApi } from '../lib/contracts.mjs';

const { doc, tags, paths } = loadOpenApi(process.cwd());

const errors = [];

if (!doc.info?.title) {
  errors.push('info.title is required');
}
if (!doc.info?.version) {
  errors.push('info.version is required');
}

for (const [apiPath, pathItem] of Object.entries(doc.paths)) {
  for (const [method, operation] of Object.entries(pathItem || {})) {
    if (!['get', 'post', 'put', 'patch', 'delete', 'options', 'head'].includes(method)) {
      continue;
    }
    if (!operation || typeof operation !== 'object') {
      errors.push(`${method.toUpperCase()} ${apiPath}: operation is empty`);
      continue;
    }
    if (!operation.operationId) {
      errors.push(`${method.toUpperCase()} ${apiPath}: missing operationId`);
    }
    if (!Array.isArray(operation.tags) || operation.tags.length === 0) {
      errors.push(`${method.toUpperCase()} ${apiPath}: missing tags`);
    } else {
      for (const tag of operation.tags) {
        if (!tags.has(tag)) {
          errors.push(`${method.toUpperCase()} ${apiPath}: unknown tag '${tag}'`);
        }
      }
    }
    if (!operation.responses || Object.keys(operation.responses).length === 0) {
      errors.push(`${method.toUpperCase()} ${apiPath}: missing responses`);
    }
  }
}

if (errors.length > 0) {
  console.error('OpenAPI validation failed:');
  for (const error of errors.slice(0, 80)) {
    console.error(`- ${error}`);
  }
  if (errors.length > 80) {
    console.error(`... and ${errors.length - 80} more`);
  }
  process.exit(1);
}

console.log(`verify-openapi passed: ${paths.length} paths, ${(doc.tags || []).length} tags`);
