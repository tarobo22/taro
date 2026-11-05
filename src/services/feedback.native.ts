import {NativeModules} from 'react-native';
export interface SensoryPreferences{sound:boolean;haptics:boolean;music:boolean;}
export type FeedbackKind='tap'|'select'|'confirm'|'back'|'shuffle'|'flip'|'tick';
const bridge=NativeModules.YeounFeedback;
let previous=0;
export function configureFeedback(prefs:SensoryPreferences){bridge?.configure(prefs.sound,prefs.haptics,prefs.music);}
export function feedback(kind:FeedbackKind='tap'){
  const now=Date.now();if(now-previous<55)return;previous=now;bridge?.play(kind);
}
