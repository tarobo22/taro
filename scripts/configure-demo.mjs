import {readFileSync, writeFileSync} from 'node:fs';

// Applied only to the disposable CI workspace, before native bundling.
const path = new URL('../src/config/endpoints.json', import.meta.url);
const config = JSON.parse(readFileSync(path, 'utf8'));
config.apiBaseUrl = 'https://yeoun-43-201-209-249.sslip.io/api';
writeFileSync(path, `${JSON.stringify(config, null, 2)}\n`);
console.log(`Demo API: ${config.apiBaseUrl}`);
