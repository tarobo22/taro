import {NativeModules,Platform} from 'react-native';
import type {StoreBilling} from '../features/billing/model';
const bridge=NativeModules.YeounBilling;
const unavailable=async():Promise<never>=>{throw new Error('이 앱 버전에서 스토어 구매를 지원하지 않아요. 새 Android 앱으로 설치해 주세요.');};
export const storeBilling:StoreBilling={supported:Platform.OS==='android'&&!!bridge,products:items=>bridge?bridge.products(items):unavailable(),purchase:(id,kind,account,offer)=>bridge?bridge.purchase(id,kind,account,offer):unavailable(),restore:()=>bridge?bridge.restore():unavailable()};
