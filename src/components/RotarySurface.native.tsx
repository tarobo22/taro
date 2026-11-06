import React,{useEffect,useMemo,useRef,useState} from 'react';
import {AppState,Pressable,Text,View,useWindowDimensions} from 'react-native';
import {Canvas,Circle,Group,Path,Rect,RoundedRect,Skia} from '@shopify/react-native-skia';
import {Gesture,GestureDetector} from 'react-native-gesture-handler';
import Animated,{cancelAnimation,useAnimatedReaction,useAnimatedStyle,useDerivedValue,useSharedValue,withSequence,withSpring,withTiming,type SharedValue} from 'react-native-reanimated';
import {scheduleOnRN} from 'react-native-worklets';
import {color} from '../design/tokens';
import {feedback} from '../services/feedback';
import type {RotaryProps} from './RotarySurface.types';

const STEP=Math.PI/8;
function bound(value:number,count:number,circular:boolean){'worklet';return circular?value:Math.max(0,Math.min(count-1,value));}
function ordinal(value:number,count:number){'worklet';return ((Math.round(value)%count)+count)%count;}
function FanCard({i,count,p,shuffle,width,chosen,reduced}:{i:number;count:number;p:SharedValue<number>;shuffle:SharedValue<number>;width:number;chosen:boolean;reduced:boolean}){
 const lift=useSharedValue(chosen?9:0);
 useEffect(()=>{lift.value=reduced?(chosen?9:0):withSpring(chosen?9:0,{damping:20,stiffness:260});},[chosen,reduced]);
 const transform=useDerivedValue(()=>{let d=i-p.value;d-=Math.round(d/count)*count;const spread=1-shuffle.value;return [{translateX:width/2-43+d*70*spread},{translateY:22+Math.abs(d)*12*spread-lift.value+shuffle.value*14},{rotate:d*.11*spread+Math.sin(i*1.7)*shuffle.value*.17},{scale:1-Math.min(3,Math.abs(d))*.065}];});
 const opacity=useDerivedValue(()=>{let d=i-p.value;d-=Math.round(d/count)*count;const base=Math.max(0,Math.min(1,1.9-Math.abs(d)));return base+(1-base)*shuffle.value;});
 return <Group transform={transform} opacity={opacity} origin={{x:43,y:65}}>
  <RoundedRect x={0} y={0} width={86} height={134} r={12} color={chosen?color.action:color.mintDeep}/>
  <RoundedRect x={6} y={6} width={74} height={122} r={8} color={color.paper}/>
  <RoundedRect x={10} y={10} width={66} height={114} r={6} color={chosen?color.mint:color.sage}/>
  <Group transform={[{translateX:43},{translateY:67},{rotate:Math.PI/4}]}><Rect x={-18} y={-18} width={36} height={36} color={color.mintDeep}/><Rect x={-13} y={-13} width={26} height={26} color={color.sage}/></Group>
  <Circle cx={43} cy={67} r={7} color={color.action}/>
  {chosen?<Circle cx={68} cy={19} r={5} color={color.action}/>:null}
 </Group>;
}

/** UI-thread rotation is also the source of truth for the card fan. React only receives detents. */
export function RotarySurface({options,index,onChange,label,circular=false,disabled=false,reduced,cards,selected=[],version}:RotaryProps){
 const count=Math.max(1,options.length),p=useSharedValue(index),dragging=useSharedValue(false),start=useSharedValue(0),lastAngle=useSharedValue(0),angleTotal=useSharedValue(0),shuffle=useSharedValue(0);
 const latest=useRef({onChange,disabled,index});latest.current={onChange,disabled,index};const previousVersion=useRef(version);
 const [width,setWidth]=useState(300),[hint,setHint]=useState(true);const large=useWindowDimensions().fontScale>1.3;
 function detent(next:number,notify:boolean){if(latest.current.disabled)return;feedback('tick');setHint(false);if(notify)latest.current.onChange(next);}
 useAnimatedReaction(()=>ordinal(p.value,count),(next,old)=>{if(old!==null&&next!==old)scheduleOnRN(detent,next,dragging.value);},[count]);
 useEffect(()=>{if(dragging.value)return;let target=index;if(circular)target=p.value+((index-ordinal(p.value,count)+count*1.5)%count-count*.5);p.value=reduced?target:withSpring(target,{damping:24,stiffness:240,mass:.7});},[index,circular,count,reduced]);
 useEffect(()=>{if(previousVersion.current!==undefined&&version!==previousVersion.current){shuffle.value=reduced?0:withSequence(withTiming(1,{duration:210}),withTiming(.82,{duration:200}),withTiming(0,{duration:330}));}previousVersion.current=version;},[version,reduced]);
 useEffect(()=>{const sub=AppState.addEventListener('change',state=>{if(state!=='active'){cancelAnimation(p);cancelAnimation(shuffle);dragging.value=false;p.value=latest.current.index;shuffle.value=0;}});return()=>{sub.remove();cancelAnimation(p);cancelAnimation(shuffle);};},[]);
 const ticks=useMemo(()=>{let path='';for(let i=0;i<32;i++){const a=i*Math.PI/16,r=i%4===0?33:37;path+=`M${48+Math.sin(a)*r} ${48-Math.cos(a)*r}L${48+Math.sin(a)*42} ${48-Math.cos(a)*42}`;}return Skia.Path.MakeFromSVGString(path)!;},[]);
 const turn=useDerivedValue(()=>[{rotate:-p.value*STEP}]);
 const roll=useAnimatedStyle(()=>({transform:[{translateY:-(circular?((p.value%count)+count)%count+count:bound(p.value,count,false))*32}]}));
 const gesture=Gesture.Pan().enabled(!disabled&&count>1).minDistance(3).manualActivation(true)
  .onTouchesDown((event,manager)=>{const touch=event.allTouches[0];if(!touch||Math.hypot(touch.x-48,touch.y-48)<28)manager.fail();})
  .onTouchesMove((_event,manager)=>manager.activate())
  .onBegin(e=>{cancelAnimation(p);start.value=p.value;lastAngle.value=Math.atan2(e.x-48,48-e.y);angleTotal.value=0;dragging.value=true;})
  .onUpdate(e=>{const angle=Math.atan2(e.x-48,48-e.y);let delta=angle-lastAngle.value;if(delta>Math.PI)delta-=Math.PI*2;if(delta<-Math.PI)delta+=Math.PI*2;angleTotal.value+=delta;lastAngle.value=angle;p.value=bound(start.value+angleTotal.value/STEP,count,circular);})
  .onEnd(e=>{const tangent=(e.velocityX*Math.cos(lastAngle.value)+e.velocityY*Math.sin(lastAngle.value))/(42*STEP);const projected=bound(p.value+Math.max(-1.25,Math.min(1.25,tangent*.075)),count,circular),target=Math.round(projected);if(reduced){p.value=target;dragging.value=false;scheduleOnRN(detent,ordinal(target,count),true);}else p.value=withSpring(target,{damping:23,stiffness:230,mass:.75},finished=>{if(finished){dragging.value=false;scheduleOnRN(detent,ordinal(target,count),true);}});})
  .onFinalize((_e,success)=>{if(!success){dragging.value=false;p.value=withSpring(Math.round(p.value),{damping:24,stiffness:240});scheduleOnRN(detent,ordinal(p.value,count),true);}});
 const keyIndex=(next:number)=>{if(disabled||!options.length)return;if(!circular&&(next<0||next>=count)){feedback('boundary');return;}setHint(false);onChange((next+count)%count);};
 return <View style={{gap:8}} onLayout={e=>setWidth(e.nativeEvent.layout.width)}>
  {cards?<Canvas style={{width,height:192}} accessible={false}>{cards.map((id,i)=>({id,i,d:Math.abs(((i-index+count*1.5)%count)-count/2)})).sort((a,b)=>b.d-a.d).map(({id,i})=><FanCard key={id} i={i} count={count} p={p} shuffle={shuffle} width={width} chosen={selected.includes(id)} reduced={reduced}/>)}</Canvas>:null}
  <View style={{flexDirection:'row',alignItems:'center',gap:12}}>
   <GestureDetector gesture={gesture}><View accessible focusable accessibilityRole="adjustable" accessibilityLabel={label} accessibilityValue={{min:0,max:count-1,now:index,text:options[index]}} accessibilityActions={[{name:'increment'},{name:'decrement'}]} onAccessibilityAction={e=>keyIndex(index+(e.nativeEvent.actionName==='decrement'?-1:1))} style={{width:96,height:96}}>
    <Canvas style={{width:96,height:96}}><Circle cx={48} cy={48} r={42} color={color.sage}/><Group origin={{x:48,y:48}} transform={turn}><Path path={ticks} color={color.mintDeep} style="stroke" strokeWidth={2}/></Group><Circle cx={48} cy={48} r={29} color={color.paper}/><Circle cx={48} cy={6} r={4} color={color.action}/></Canvas>
    <View pointerEvents="none" style={{position:'absolute',inset:0,alignItems:'center',justifyContent:'center'}}><Text style={{color:color.action,fontSize:14,fontWeight:'600'}}>{index+1}<Text style={{color:color.muted,fontWeight:'400'}}> / {count}</Text></Text></View>
   </View></GestureDetector>
   <View style={{flex:1,gap:6}}><Text style={{fontSize:12,color:color.muted}}>{label}</Text>
    {large?<Text numberOfLines={1} adjustsFontSizeToFit minimumFontScale={.8} style={{fontSize:19,lineHeight:32,fontWeight:'600',color:color.ink}}>{options[index]}</Text>:<View accessible accessibilityLabel={options[index]} style={{height:32,overflow:'hidden'}}><Animated.View accessible={false} importantForAccessibility="no-hide-descendants" style={roll}>{(circular?[...options,...options,...options]:options).map((option,i)=><Text key={i} numberOfLines={1} adjustsFontSizeToFit minimumFontScale={.85} style={{fontSize:19,lineHeight:32,height:32,fontWeight:'600',color:color.ink}}>{option}</Text>)}</Animated.View></View>}
    <Text style={{fontSize:11,color:color.muted}}>{hint?'테두리를 돌려 골라요':'천천히, 내 마음이 가는 쪽으로'}</Text>
   </View>
   <View style={{gap:2}}>{[-1,1].map(step=><Pressable key={step} accessibilityRole="button" accessibilityLabel={label+(step<0?' 이전':' 다음')} disabled={disabled} onPress={()=>keyIndex(index+step)} style={{width:48,height:48,alignItems:'center',justifyContent:'center',borderRadius:16,backgroundColor:color.sage,opacity:disabled?.4:1}}><Text style={{fontSize:22,color:color.action}}>{step<0?'−':'+'}</Text></Pressable>)}</View>
  </View>
 </View>;
}
