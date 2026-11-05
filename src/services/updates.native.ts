import {HotUpdater} from '@hot-updater/react-native';
import {runtime} from '../config/runtime';
import {GovernedUpdates} from '../features/updates/GovernedUpdates';
import type {OtaPolicy} from '../features/p0/model';

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
const updates=new GovernedUpdates({
  async check(channel){
    const update=await hotUpdater.checkForUpdate({updateStrategy:'appVersion',channel});
    return update?{download:()=>update.updateBundle()}:null;
  },
  async reload(){await hotUpdater.reload();},
},()=>hotUpdater.getAppVersion());
export function configureUpdatePolicy(policy:OtaPolicy|null){const ready=updates.ready;updates.configure(policy);if(ready&&!updates.ready)notice='업데이트 정책이 바뀌었어요. 다시 확인해 주세요.';listeners.forEach(listener=>listener());}
export const checkAndDownload=()=>updates.checkAndDownload();
export const applyUpdate=()=>updates.apply();
export const isUpdateReady=()=>updates.ready;
export function bundleId(){return hotUpdater.getBundleId();}
