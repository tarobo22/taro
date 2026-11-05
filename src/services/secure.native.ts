import * as Keychain from 'react-native-keychain';
import {runtime} from '../config/runtime';
const service='com.yeoun.app.session:'+runtime.apiBaseUrl;
export async function readSession(){const value=await Keychain.getGenericPassword({service});return value?value.password:null;}
export async function writeSession(token:string){await Keychain.setGenericPassword('guest',token,{service,accessible:Keychain.ACCESSIBLE.WHEN_UNLOCKED_THIS_DEVICE_ONLY});}
