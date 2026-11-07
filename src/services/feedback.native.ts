import {AppState,NativeModules} from 'react-native';
import {AudioGate} from '../features/audio/AudioGate';
import {CharacterCue} from '../features/skins/CharacterCue';
import {defaultSensory,SensoryPreferences} from '../features/audio/preferences';
export type {SensoryPreferences} from '../features/audio/preferences';
export type FeedbackKind='tap'|'select'|'confirm'|'back'|'shuffle'|'flip'|'tick'|'boundary'|'meow';
const bridge=NativeModules.YeounFeedback;
const audioGate=new AudioGate();
const characterCue=new CharacterCue();
let prefs:SensoryPreferences={...defaultSensory};
let previousTick=0,previousAction=0;
function applyFeedback(){
 const muted=audioGate.blocked;
 // Older binaries can block future playback through configure; stopping active streams requires the new bridge.
 bridge?.setCaptureMuted?.(muted);
 bridge?.configure(prefs.sound&&!muted,prefs.haptics&&!muted,prefs.music&&!muted);
}
audioGate.setForeground(AppState.currentState==='active');
AppState.addEventListener('change',state=>audioGate.setForeground(state==='active'));
audioGate.subscribe(applyFeedback);
export function beginAudioCapture(){return audioGate.acquire();}
export function configureFeedback(value:SensoryPreferences){prefs={...value};applyFeedback();}
export function feedback(kind:FeedbackKind='tap'){
  if(audioGate.blocked)return;
  const now=Date.now();if(kind==='meow'){if(!characterCue.request(now,prefs.sound,audioGate.blocked))return;}else if(kind==='tick'){if(now-previousTick<35)return;previousTick=now;}else{if(now-previousAction<45)return;previousAction=now;}bridge?.play(kind);
}
