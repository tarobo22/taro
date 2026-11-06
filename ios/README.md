# 여운 iOS 네이티브 앱

RN Community CLI0.85.3 공식 템플릿으로 기존 `YeounApp`을 연결한다. Android 프로젝트와 공통 화면은 재생성하지 않았다.

| 경로 | 역할 |
|---|---|
| Yeoun/AppDelegate.swift | RN 진입·계정 deep link·Hot Updater native bundle 경계 |
| Yeoun/Native/YeounAuth.swift | SecRandom32바이트·S256 PKCE |
| Yeoun/Native/YeounSpeech.swift | 명시적으로 시작한 기기 음성·권한·capture ID·백그라운드 중단 |
| Yeoun/Native/YeounFeedback.swift | 기존 효과음·BGM·선택/확정 햅틱·캡처 중 mute |
| Yeoun/Native/YeounReminders.swift | 선택적 로컬 시각 알림·OS 권한·끄기·데이터 삭제 시 clear |
| Yeoun/Native/YeounFiles.swift | 제한된 음성 파일 선택·인증 내보내기·redirect 차단·stream 용량 제한 |
| YeounCore | 순수 Swift 규칙. Linux XCTest로 RED6 → GREEN6 확인 |

## 개발 환경

Mac/Xcode에서 `npm ci`, `bundle install`, `cd ios && bundle exec pod install` 후 `npm run ios`를 실행한다. iOS SDK·앱 서명·실기기 연결은 Windows에 설치했다고 표시하지 않는다.

Simulator 개발 API는 Xcode의 `YEOUN_API_BASE_URL=http://localhost:8808`이다. 실제 배포는 운영 HTTPS 주소로 바꿔야 한다. 이 기본값은 실제 운영 서버가 아니다. Native1.4.0/code5·preview 채널이며 Hot Updater bundle resolver를 연결했다. 실제 새 OTA 다운로드·적용 확인은 별도다.

기본 효과음·BGM OFF와 기존 감각 설정을 유지한다. WAV6개는 기존 에셋 그대로 복사했고, OGG 배경음은 같은 원음을 AAC96kbps M4A로 변환했다. iOS의 사진·연락처·위치 권한을 추가하지 않았다. 마이크·음성 인식·알림 권한은 사용자가 해당 기능을 시작할 때만 요청한다.

## 검증 범위

- PKCE RFC7636 벡터·충분한 entropy·늦은 capture callback·download origin/stream cap: Swift XCTest6 통과. Apple `swift-crypto`3.12.3을 사용하며 Apple에서는 CryptoKit으로 계산한다.
- Windows에서는 전체 Swift syntax parse와 RN 타입·관련 기존 테스트를 확인한다.
- `.github/workflows/ios.yml`은 Mac에서 native rules, CocoaPods, unsigned Release simulator build·기동·초기 화면·deep link를 검사한다. 결과는 실제 CI 완료 상태로 판정한다.
- 실제 OAuth 승인·App Store 상품/구매·물리 햅틱/마이크·서명/스토어 제출은 완료로 세지 않는다.
