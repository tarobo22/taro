import {HotUpdater} from '@hot-updater/react-native';
import {runtime} from '../config/runtime';
export const hotUpdater=HotUpdater.init({baseURL:runtime.otaBaseUrl,requestTimeout:runtime.timeoutMs});
export async function checkAndDownload(){const update=await hotUpdater.checkForUpdate({updateStrategy:'appVersion',channel:'preview'});if(!update)return false;return update.updateBundle();}
export async function applyUpdate(){await hotUpdater.reload();}
export function bundleId(){return hotUpdater.getBundleId();}
