import { loadAsyncApi } from '../lib/contracts.mjs';

const { doc, channels } = loadAsyncApi(process.cwd());

const errors = [];
if (!doc.info?.title) {
  errors.push('info.title is required');
}
if (!doc.info?.version) {
  errors.push('info.version is required');
}

for (const [channelName, channelItem] of Object.entries(doc.channels || {})) {
  const hasPublish = !!channelItem?.publish;
  const hasSubscribe = !!channelItem?.subscribe;

  if (!hasPublish && !hasSubscribe) {
    errors.push(`channel ${channelName}: must contain publish or subscribe`);
    continue;
  }

  if (hasPublish && !channelItem.publish.message) {
    errors.push(`channel ${channelName}: publish.message is required`);
  }
  if (hasSubscribe && !channelItem.subscribe.message) {
    errors.push(`channel ${channelName}: subscribe.message is required`);
  }
}

if (errors.length > 0) {
  console.error('AsyncAPI validation failed:');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log(`verify-asyncapi passed: ${channels.length} channels`);
