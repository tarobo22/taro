import React,{useEffect} from 'react';
import Animated,{useSharedValue,useAnimatedStyle,withSpring} from 'react-native-reanimated';
import {color} from '../design/tokens';
export function RailHighlight({x,width,reduced}:{x:number;width:number;reduced:boolean}){
 const left=useSharedValue(x),size=useSharedValue(width);
 useEffect(()=>{left.value=reduced?x:withSpring(x,{damping:26,stiffness:280});size.value=reduced?width:withSpring(width,{damping:26,stiffness:280});},[x,width,reduced]);
 const style=useAnimatedStyle(()=>({width:size.value,transform:[{translateX:left.value}]}));
 return <Animated.View pointerEvents="none" style={[{position:'absolute',left:0,top:0,bottom:0,borderRadius:18,backgroundColor:color.mint},style]}/>;
}
