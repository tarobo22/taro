import React,{useEffect,useRef,useState} from 'react';
import {AppState,Pressable,Text,View,useWindowDimensions} from 'react-native';
import {Gesture,GestureDetector} from 'react-native-gesture-handler';
import Animated,{cancelAnimation,useAnimatedReaction,useAnimatedStyle,useSharedValue,withSpring,withTiming} from 'react-native-reanimated';
import {scheduleOnRN} from 'react-native-worklets';
import {feedback} from '../services/feedback';
import {color} from '../design/tokens';
import type {ChoiceStageProps} from './ChoiceStage.types';
export function ChoiceStage({children,onChoice,disabled=false,reduced,label='대화 선택',showButtons=true}:ChoiceStageProps){
 const width=Math.min(useWindowDimensions().width,430)-48,x=useSharedValue(0),enabled=useSharedValue(true),alive=useRef(true),lock=useRef(false),pending=useRef(false),[locked,setLocked]=useState(false),[hint,setHint]=useState(true);
 const latest=useRef({onChoice,disabled,reduced});latest.current={onChoice,disabled,reduced};
 useEffect(()=>{enabled.value=!disabled&&!locked;},[disabled,locked]);
 useEffect(()=>{alive.current=true;const sub=AppState.addEventListener('change',state=>{if(state!=='active'){cancelAnimation(x);x.value=0;enabled.value=false;if(!pending.current){lock.current=false;setLocked(false);}}else enabled.value=!latest.current.disabled&&!lock.current;});return()=>{alive.current=false;sub.remove();cancelAnimation(x);};},[]);
 function tick(){feedback('select');setHint(false);}
 useAnimatedReaction(()=>Math.abs(x.value)>width*.22,(hit,previous)=>{if(hit&&!previous)scheduleOnRN(tick);},[width]);
 async function commit(yes:boolean){if(!alive.current)return;pending.current=true;let success=true;try{success=(await latest.current.onChoice(yes))!==false;}catch{success=false;}pending.current=false;if(!alive.current)return;
  if(latest.current.reduced||AppState.currentState!=='active'){x.value=0;lock.current=false;setLocked(false);return;}
  requestAnimationFrame(()=>{if(!alive.current)return;if(success)x.value=(yes?-1:1)*width*.55;
   x.value=withSpring(0,{damping:25,stiffness:230,mass:.8},finished=>{if(finished)scheduleOnRN(unlock);});});
 }
 function unlock(){if(alive.current){lock.current=false;setLocked(false);}}
 function choose(yes:boolean){if(lock.current||latest.current.disabled||!alive.current)return;lock.current=true;setLocked(true);setHint(false);enabled.value=false;feedback('select');
  if(latest.current.reduced){void commit(yes);return;}x.value=withTiming((yes?1:-1)*width*1.1,{duration:190},finished=>{if(finished)scheduleOnRN(commit,yes);});
 }
 const gesture=Gesture.Pan().activeOffsetX([-10,10]).failOffsetY([-20,20]).enabled(!disabled&&!locked)
  .onUpdate(e=>{if(enabled.value)x.value=e.translationX*.92;})
  .onEnd(e=>{if(!enabled.value)return;if(Math.abs(e.translationX)>width*.22||Math.abs(e.velocityX)>850){enabled.value=false;scheduleOnRN(choose,e.translationX>0);}else x.value=withSpring(0,{damping:23,stiffness:260,mass:.7});})
  .onFinalize((_e,success)=>{if(!success)x.value=withSpring(0,{damping:24,stiffness:260});});
 const style=useAnimatedStyle(()=>({transform:[{translateX:x.value},{rotate:reduced?'0deg':`${x.value/width*12}deg`}]}));
 const left=useAnimatedStyle(()=>({opacity:Math.min(.65,Math.max(0,-x.value/width*1.5))})),right=useAnimatedStyle(()=>({opacity:Math.min(.65,Math.max(0,x.value/width*1.5))}));
 return <View style={{gap:10}}>
  <View style={{position:'relative'}}><Animated.View pointerEvents="none" style={[{position:'absolute',inset:0,backgroundColor:color.pink,borderRadius:24},left]}/><Animated.View pointerEvents="none" style={[{position:'absolute',inset:0,backgroundColor:color.mintDeep,borderRadius:24},right]}/>
   <GestureDetector gesture={gesture}><Animated.View testID="choice-profile" style={style}
    accessible={!showButtons&&!disabled?true:undefined} accessibilityRole={!showButtons&&!disabled?'adjustable':undefined}
    accessibilityHint={!showButtons&&!disabled?'위로 올리면 예, 아래로 내리면 아니요.':undefined}
    accessibilityActions={!showButtons&&!disabled?[{name:'decrement',label:'아니요'},{name:'increment',label:'예'}]:undefined}
    onAccessibilityAction={showButtons?undefined:event=>{
     const action=event.nativeEvent.actionName;
     if(action==='increment'||action==='decrement')choose(action==='increment');
    }}>{children}</Animated.View></GestureDetector>
  </View>
  {showButtons?<View style={{flexDirection:'row',gap:8}}>{[false,true].map(yes=><Pressable key={String(yes)} accessibilityRole="button" accessibilityLabel={yes?'예 선택':'아니요 선택'} disabled={disabled||locked} onPress={()=>choose(yes)} style={{flex:1,minHeight:48,borderRadius:17,alignItems:'center',justifyContent:'center',backgroundColor:yes?color.mint:color.pink,opacity:disabled||locked?.5:1}}><Text style={{fontSize:15,fontWeight:'600',color:color.ink}}>{yes?'예 →':'← 아니요'}</Text></Pressable>)}</View>:null}
  {(hint||!showButtons)&&!disabled?<Text style={{textAlign:'center',fontSize:showButtons?11:13,color:color.muted}}>{showButtons?label+' · 왼쪽 아니요, 오른쪽 예':'카드를 밀어 답해요. 왼쪽은 아니요, 오른쪽은 예.'}</Text>:null}
 </View>;
}
