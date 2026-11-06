import settings from './endpoints.json';
import {NativeModules,Platform} from 'react-native';
const nativeApi=NativeModules.YeounEnvironment?.apiBaseUrl;
export const runtime = Platform.OS==='ios'&&typeof nativeApi==='string'&&nativeApi
 ? {...settings,apiBaseUrl:nativeApi.replace(/\/$/,'')}
 : settings;
