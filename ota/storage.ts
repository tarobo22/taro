import {mkdir,readFile,writeFile,rename,access} from 'node:fs/promises';
import path from 'node:path';
import type {StorageAdapter} from '@hot-updater/plugin-core';

export function localStorage(root:string,downloadBaseUrl:string):StorageAdapter {
  const absolute=path.resolve(root);
  function file(key:string){const value=path.resolve(absolute,key);if(!value.startsWith(absolute+path.sep))throw new Error('Invalid bundle path');return value;}
  function keyOf(uri:string){const parsed=new URL(uri);if(parsed.protocol!=='yeoun-local:'||parsed.hostname!=='preview')throw new Error('Invalid bundle URI');return parsed.pathname.slice(1).split('/').map(decodeURIComponent).join('/');}
  function encoded(key:string){return key.split('/').map(encodeURIComponent).join('/');}
  return {name:'yeoun-loopback-files',protocol:'yeoun-local',
    async put({key,body}){const target=file(key);await mkdir(path.dirname(target),{recursive:true});await writeFile(target,new Uint8Array(await new Response(body).arrayBuffer()));return {storageUri:`yeoun-local://preview/${encoded(key)}`};},
    async get({storageUri}){try {return {response:new Response(await readFile(file(keyOf(storageUri))))};}catch{return {response:null};}},
    async getDownloadUrl({storageUri}){return {url:`${downloadBaseUrl.replace(/\/$/,'')}/ota-files/${encoded(keyOf(storageUri))}`};},
    async exists({storageUri}){try{await access(file(keyOf(storageUri)));return {exists:true};}catch{return {exists:false};}},
    async delete({storageUri}){const source=file(keyOf(storageUri));try{await access(source);}catch{return {deleted:true};}const archive=path.join(absolute,'.retained',String(Date.now()),keyOf(storageUri));if(!archive.startsWith(absolute+path.sep))throw new Error('Invalid archive path');await mkdir(path.dirname(archive),{recursive:true});await rename(source,archive);return {deleted:true};}
  };
}
