import {defineConfig} from 'hot-updater';
import {bare} from '@hot-updater/bare';
import {database,storage} from './ota/config';
export default defineConfig({updateStrategy:'appVersion',build:bare({enableHermes:true}),database,storage});
