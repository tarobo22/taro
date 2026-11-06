import {Linking,NativeModules,Platform} from 'react-native';
import {validatePkce} from '../features/account/contracts';
export const social={platform:Platform.OS==='ios'?'ios' as const:'android' as const,returnUri:()=> 'yeoun://auth/complete',ready:()=>typeof NativeModules.YeounAuth?.createPkce==='function',async pkce(){if(!social.ready())throw new Error('안전한 계정 연결을 사용하려면 기본 앱을 업데이트해 주세요.');return validatePkce(await NativeModules.YeounAuth.createPkce());},async open(url:string){await Linking.openURL(url);}};
export function listenSocial(receive:(url:string)=>void){const subscription=Linking.addEventListener('url',event=>receive(event.url));void Linking.getInitialURL().then(url=>{if(url)receive(url);}).catch(()=>{});return()=>subscription.remove();}
