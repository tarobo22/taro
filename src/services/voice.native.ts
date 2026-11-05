import {NativeEventEmitter,NativeModules,PermissionsAndroid,Platform} from 'react-native';
const module=NativeModules.YeounSpeech;
export async function voiceAvailable(){return !!module&&await module.isAvailable();}
export async function startVoice(onText:(v:string)=>void,onState:(v:string)=>void){
 if(!module){onState('이 기기에서는 음성 입력을 사용할 수 없어요. 글로 이어갈 수 있어요.');return ()=>{};}
 const permission=await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,{title:'음성으로 이야기하기',message:'말한 내용을 글로 옮길 때 마이크를 사용합니다.',buttonPositive:'허용',buttonNegative:'지금은 안 할래요'});
 if(permission!==PermissionsAndroid.RESULTS.GRANTED){onState('마이크 권한 없이도 글로 이야기할 수 있어요.');return ()=>{};}
 const emitter=new NativeEventEmitter(module);
 const subscription=emitter.addListener('YeounSpeech',event=>{if(event.text)onText(event.text);onState(event.message||'');if(event.final||event.error)subscription.remove();});
 await module.start('ko-KR');return ()=>{module.stop();subscription.remove();onState('');};
}
