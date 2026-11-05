import {Pool} from 'pg';
import {fileURLToPath} from 'node:url';
import path from 'node:path';
const root=path.dirname(path.dirname(fileURLToPath(import.meta.url)));
process.loadEnvFile(path.join(root,'.env.local'));
const pool=new Pool({connectionString:process.env.DATABASE_URL});
try{const rows=await pool.query("SELECT 1 FROM pg_database WHERE datname='yeoun_updates'");if(!rows.rowCount)await pool.query('CREATE DATABASE yeoun_updates');console.log('Dedicated OTA metadata database is ready.');}finally{await pool.end();}
