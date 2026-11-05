import {HotUpdater} from '@hot-updater/react-native';
import {runtime} from '../config/runtime';
import {UpdateManager} from '../features/updates/UpdateManager';

let notice='';
const listeners=new Set<()=>void>();
export function getUpdateNotice(){return notice;}
export function subscribeUpdateNotice(listener:()=>void){listeners.add(listener);return()=>{listeners.delete(listener);};}
export const hotUpdater=HotUpdater.init({baseURL:runtime.otaBaseUrl,requestTimeout:runtime.timeoutMs,
  onNotifyAppReady:result=>{
    notice=result.status==='RECOVERED'?'안정적으로 사용할 수 있는 이전 내용으로 돌아왔어요.':
      result.status==='UPDATE_APPLIED'?'새로운 내용이 적용됐어요.':'';
    listeners.forEach(listener=>listener());
  },
});
const updates=new UpdateManager({
  async check(){
    const update=await hotUpdater.checkForUpdate({updateStrategy:'appVersion',channel:runtime.otaChannel});
    return update?{download:()=>update.updateBundle()}:null;
  },
  async reload(){await hotUpdater.reload();},
});
export const checkAndDownload=()=>updates.checkAndDownload();
export const applyUpdate=()=>updates.apply();
export const isUpdateReady=()=>updates.ready;
export function bundleId(){return hotUpdater.getBundleId();}
