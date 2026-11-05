import {fileURLToPath} from 'node:url';
import path from 'node:path';
import {Pool} from 'pg';
import {Kysely,PostgresDialect} from 'kysely';
import {kyselyAdapter} from '@hot-updater/server/adapters/kysely';
import {localStorage} from './storage';

export const dataRoot=process.env.YEOUN_DATA_ROOT||path.dirname(path.dirname(fileURLToPath(import.meta.url)));
process.loadEnvFile(path.join(dataRoot,'.env.local'));
const connection=new URL(process.env.DATABASE_URL!);
connection.pathname='/yeoun_updates';
export const database=kyselyAdapter({db:new Kysely({dialect:new PostgresDialect({pool:new Pool({connectionString:connection.toString(),max:4})})}),provider:'postgresql'});
export const otaPort=Number(process.env.OTA_PORT||8806);
export const storage=localStorage(path.join(dataRoot,'.local','ota-files'),process.env.OTA_PUBLIC_URL||`http://127.0.0.1:${otaPort}`);
