#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(YeounAuth, NSObject)
RCT_EXTERN_METHOD(createPkce:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
@end

@interface RCT_EXTERN_MODULE(YeounEnvironment, NSObject)
@end

@interface RCT_EXTERN_MODULE(YeounFeedback, NSObject)
RCT_EXTERN_METHOD(configure:(BOOL)sound haptics:(BOOL)haptics background:(BOOL)background)
RCT_EXTERN_METHOD(setCaptureMuted:(BOOL)muted)
RCT_EXTERN_METHOD(play:(NSString *)kind)
@end

@interface RCT_EXTERN_MODULE(YeounSpeech, RCTEventEmitter)
RCT_EXTERN_METHOD(isAvailable:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(startCapture:(NSString *)language captureId:(NSString *)captureId)
RCT_EXTERN_METHOD(stop)
@end

@interface RCT_EXTERN_MODULE(YeounReminders, NSObject)
RCT_EXTERN_METHOD(read:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(requestPermission:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(schedule:(double)hour minute:(double)minute resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(disable:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(clear:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(openSettings:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(preview:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
@end

@interface RCT_EXTERN_MODULE(YeounFiles, NSObject)
RCT_EXTERN_METHOD(chooseAudio:(double)maxBytes maxDurationMs:(double)maxDurationMs resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(downloadPrivate:(NSString *)url bearer:(NSString *)bearer filename:(NSString *)filename maxBytes:(double)maxBytes resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
@end
