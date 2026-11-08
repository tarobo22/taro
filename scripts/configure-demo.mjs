import {appendFileSync, readFileSync, writeFileSync} from 'node:fs';

const publicUrl = URL.parse(process.env.YEOUN_PUBLIC_URL?.trim() ?? '');
if (!publicUrl || publicUrl.protocol !== 'https:' || publicUrl.pathname !== '/'
    || publicUrl.username || publicUrl.password || publicUrl.search || publicUrl.hash
    || !publicUrl.hostname.includes('.') || /^(127\.|0\.)|\.(localhost|local)$/.test(publicUrl.hostname)) {
  throw new Error('Set YEOUN_PUBLIC_URL to the public HTTPS origin.');
}
const apiBaseUrl = publicUrl.origin + '/api';

// Applied only to the disposable CI workspace, before native bundling.
const path = new URL('../src/config/endpoints.json', import.meta.url);
const config = JSON.parse(readFileSync(path, 'utf8'));
config.apiBaseUrl = apiBaseUrl;
writeFileSync(path, `${JSON.stringify(config, null, 2)}\n`);
if (process.env.GITHUB_ENV) appendFileSync(process.env.GITHUB_ENV, `YEOUN_API_BASE_URL=${apiBaseUrl}\n`);
console.log(`Demo API: ${apiBaseUrl}`);
