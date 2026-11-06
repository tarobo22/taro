import * as Keychain from 'react-native-keychain';
import {runtime} from '../config/runtime';
const service='com.yeoun.app.session:'+runtime.apiBaseUrl;
export async function readSession(){const value=await Keychain.getGenericPassword({service});return value?value.password:null;}
export async function writeSession(token:string){await Keychain.setGenericPassword('guest',token,{service,accessible:Keychain.ACCESSIBLE.WHEN_UNLOCKED_THIS_DEVICE_ONLY});}
export async function clearSession(){await Keychain.resetGenericPassword({service});}
export async function readAccountSecret(name:'pending'|'guests'){const value=await Keychain.getGenericPassword({service:service+':account:'+name});return value?value.password:null;}
export async function writeAccountSecret(name:'pending'|'guests',value:string){await Keychain.setGenericPassword('account',value,{service:service+':account:'+name,accessible:Keychain.ACCESSIBLE.WHEN_UNLOCKED_THIS_DEVICE_ONLY});}
export async function clearAccountSecret(name:'pending'|'guests'){await Keychain.resetGenericPassword({service:service+':account:'+name});}
