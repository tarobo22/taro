import {NativeEventEmitter,NativeModules,PermissionsAndroid,Platform} from 'react-native';
import {beginAudioCapture} from './feedback';
import {VoiceCaptureSession} from '../features/audio/VoiceCapture';
const module=NativeModules.YeounSpeech;
const runtimeId=Date.now().toString(36)+'-'+Math.random().toString(36).slice(2);
let sequence=0,currentCapture:string|null=null;
let cancelPrevious:(()=>void)|null=null;
export async function voiceAvailable(){return !!module&&await module.isAvailable();}
export function startVoice(onText:(v:string)=>void,onState:(v:string)=>void,onEnd:()=>void=()=>{}){
 const captureId=`device-${runtimeId}-${++sequence}`;
 const session=new VoiceCaptureSession(beginAudioCapture,onText,onState,()=>{if(cancelPrevious===cancel)cancelPrevious=null;onEnd();});
 const cancel=()=>session.cancel();
 // Acquire the new lease before cancelling the old one, so audio cannot briefly resume between sessions.
 const previous=cancelPrevious;cancelPrevious=cancel;previous?.();
 void Promise.resolve().then(async()=>{
  if(!session.active)return;
  if(!module){session.finish('이 기기에서는 음성 입력을 사용할 수 없어요. 글로 이어갈 수 있어요.');return;}
  if(Platform.OS==='android'){
   const permission=await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,{title:'음성으로 이야기하기',message:'말한 내용을 글로 옮길 때 마이크를 사용합니다.',buttonPositive:'허용',buttonNegative:'지금은 안 할래요'});
   if(!session.active)return;
   if(permission!==PermissionsAndroid.RESULTS.GRANTED){session.finish('마이크 권한 없이도 글로 이야기할 수 있어요.');return;}
  }
  const scoped=typeof module.startCapture==='function';
  const emitter=new NativeEventEmitter(module);
  const subscription=emitter.addListener('YeounSpeech',event=>{
   if(!session.active||(scoped&&event.captureId!==captureId))return;
   if(event.text)session.text(event.text);
   if(event.final||event.error)session.finish(event.message||'');else session.state(event.message||'');
  });
  currentCapture=captureId;
  session.attachStop(()=>{subscription.remove();if(currentCapture===captureId){currentCapture=null;module.stop();}});
  if(scoped)module.startCapture('ko-KR',captureId);else module.start('ko-KR');
 }).catch(()=>session.finish('음성 입력을 시작하지 못했어요. 글로 이어갈 수 있어요.'));
 return cancel;
}
