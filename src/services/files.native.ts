import {NativeModules} from 'react-native';
import type {AudioFile} from './fileTypes';
interface NativeFile{uri:string;name:string;mimeType:string;sizeBytes:number;durationMs:number;}
interface FileBridge{chooseAudio(maxBytes:number,maxDurationMs:number):Promise<NativeFile|null>;downloadPrivate(url:string,bearer:string,filename:string,maxBytes:number):Promise<boolean>;}
const bridge=NativeModules.YeounFiles as FileBridge|undefined;
export function filesAvailable(){return !!bridge?.chooseAudio;}
export async function chooseAudio(maxBytes:number,maxDurationMs:number):Promise<AudioFile|null>{
 if(!bridge?.chooseAudio)throw new Error('현재 앱에서 음성 파일 선택을 지원하지 않아요. 기기 음성 입력이나 글 입력을 이용해 주세요.');
 const file=await bridge.chooseAudio(maxBytes,maxDurationMs);if(!file)return null;
 if(typeof file.uri!=='string'||typeof file.name!=='string'||typeof file.mimeType!=='string'||!Number.isFinite(file.sizeBytes)||file.sizeBytes<=0||file.sizeBytes>maxBytes||!Number.isFinite(file.durationMs)||file.durationMs<=0||file.durationMs>maxDurationMs)throw new Error('음성 파일은 60초·10MiB 이내로 선택해 주세요.');
 return {...file,payload:{uri:file.uri,name:file.name,type:file.mimeType}};
}
export async function downloadPrivate(url:string,bearer:string,filename:string,maxBytes:number):Promise<boolean>{
 if(!bridge?.downloadPrivate)throw new Error('현재 앱에서 파일 저장을 지원하지 않아요. 새 기본 앱에서 내보내기를 이용해 주세요.');
 return bridge.downloadPrivate(url,bearer,filename,maxBytes);
}
