import test from 'node:test';
import assert from 'node:assert/strict';
import {UpdateManager} from '../src/features/updates/UpdateManager';

test('repeated update checks share one download and cannot apply an unfinished bundle',async()=>{
  let downloads=0,reloads=0,finish!:(ok:boolean)=>void;
  const download=new Promise<boolean>(resolve=>{finish=resolve;});
  const manager=new UpdateManager({check:async()=>({download:()=>{downloads++;return download;}}),
    reload:async()=>{reloads++;}});
  const first=manager.checkAndDownload();const second=manager.checkAndDownload();
  assert.equal(first,second);
  await assert.rejects(manager.apply());
  await Promise.resolve();assert.equal(downloads,1);assert.equal(reloads,0);
  finish(true);assert.equal(await first,true);assert.equal(manager.ready,true);
  await manager.apply();assert.equal(reloads,1);
});

test('a failed download is reported as failure and leaves the running bundle alone',async()=>{
  let reloads=0;
  const manager=new UpdateManager({check:async()=>({download:async()=>false}),reload:async()=>{reloads++;}});
  await assert.rejects(manager.checkAndDownload(),/업데이트/);
  assert.equal(manager.ready,false);
  await assert.rejects(manager.apply());
  assert.equal(reloads,0);
});

test('a reload failure retains the prepared update so applying can be retried',async()=>{
  let calls=0;
  const manager=new UpdateManager({check:async()=>({download:async()=>true}),reload:async()=>{
    if(++calls===1)throw new Error('reload unavailable');
  }});
  await manager.checkAndDownload();
  await assert.rejects(manager.apply(),/reload unavailable/);
  assert.equal(manager.ready,true);
  await manager.apply();assert.equal(calls,2);
});
