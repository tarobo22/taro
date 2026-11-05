import {NativeModules,PermissionsAndroid,Platform} from 'react-native';
import type {ReminderPort,ReminderState} from '../features/reminders/types';
import {defaultReminderTime,unavailableReminder,validateReminderTime} from '../features/reminders/policy';
interface NativeState{permission:'granted'|'denied';enabled:boolean;hour:number|null;minute:number|null;nextAt:number|null;}
interface ReminderBridge{read():Promise<NativeState>;schedule(hour:number,minute:number):Promise<NativeState>;disable():Promise<NativeState>;clear():Promise<NativeState>;openSettings():Promise<void>;preview():Promise<void>;}
const bridge=NativeModules.YeounReminders as ReminderBridge|undefined;
function decode(value:NativeState):ReminderState{
 const time={hour:value.hour??defaultReminderTime.hour,minute:value.minute??defaultReminderTime.minute};validateReminderTime(time);
 if(!['granted','denied'].includes(value.permission)||typeof value.enabled!=='boolean'||(value.nextAt!==null&&(!Number.isFinite(value.nextAt)||value.nextAt<=0)))throw new Error('기기의 알림 설정을 읽지 못했어요.');
 return {...time,supported:true,permission:value.permission,enabled:value.enabled,nextAt:value.nextAt===null?null:new Date(value.nextAt).toISOString()};
}
const available=()=>Platform.OS==='android'&&!!bridge;
export const reminderPort:ReminderPort={
 async read(){return available()?decode(await bridge!.read()):unavailableReminder();},
 async requestPermission(){
  if(!available())return 'unavailable';
  if(Number(Platform.Version)>=33){const status=await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);if(status!==PermissionsAndroid.RESULTS.GRANTED)return status===PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN?'blocked':'denied';}
  return (await this.read()).permission;
 },
 async schedule(time){validateReminderTime(time);return available()?decode(await bridge!.schedule(time.hour,time.minute)):unavailableReminder();},
 async disable(){return available()?decode(await bridge!.disable()):unavailableReminder();},
 async clear(){return available()?decode(await bridge!.clear()):unavailableReminder();},
 async openSettings(){if(available())await bridge!.openSettings();},
 async preview(){if(available())await bridge!.preview();},
};
