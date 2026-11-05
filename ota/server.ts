import {serve} from '@hono/node-server';
import {storage,otaPort} from './config';
import {hotUpdater as updater} from './updater';
serve({hostname:'127.0.0.1',port:otaPort,fetch:async request=>{
  const url=new URL(request.url);
  const begin=Date.now();let response:Response;
  if(url.pathname==='/health') response=Response.json({ok:true,service:'yeoun-ota',adminRoutesMounted:false});
  else if(url.pathname.startsWith('/hot-updater/')){url.pathname=url.pathname.slice('/hot-updater'.length);response=await updater.handlers.client(new Request(url,request));}
  else if(url.pathname.startsWith('/ota-files/')){const key=url.pathname.slice('/ota-files/'.length);const result=await storage.get!({storageUri:`yeoun-local://preview/${key}`});response=result.response||new Response('Not found',{status:404});}
  else response=new Response('Not found',{status:404});
  console.log(JSON.stringify({at:new Date().toISOString(),method:request.method,path:new URL(request.url).pathname,status:response.status,ms:Date.now()-begin}));
  return response;
}});
console.log(`OTA client routes listening on loopback ${otaPort}; admin routes are not exposed.`);
