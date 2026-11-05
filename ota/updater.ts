import {createHotUpdater} from '@hot-updater/server';
import {database,storage} from './config';
export const hotUpdater=createHotUpdater({database,storage,clientAccess:'public'});
