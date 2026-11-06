# 코드 변화 2
사용자 동작에서 파일·커밋까지
상태: 2안 · 실제 변경 해설

## 사용자 동작으로 코드를 읽는다.
모션 개편의 제품 변경은 RN28파일·웹23파일이다. 이번 문서·영상 제작은 제품을 변경하지 않는다.

| 사용자 동작 | 주요 파일 | 역할 |
| --- | --- | --- |
| 다이얼 회전 | SelectionDial / RotarySurface.native·web | 연속 위치·Canvas·rolling |
| 카드 탐색·선택 | CardSlotDial / P0Tarot / StepProgress | 실제 슬롯 ID·선택 순서 연결 |
| 한 줄 선택 | OptionRail / RailHighlight | 라벨 폭 유지·선택 배경 이동 |
| 좌우 대화 | ChoiceStage.native·web / Talk / Story | 직접 추적·복귀·퇴장·다음 장면 |
| 감정 확인 | MoodDial / BottomSheet / Journal | 미리보기·확정·취소 분리 |
| 손끝 반응 | feedback.native·web / Android·iOS bridge | 설정·녹음 gate·상황별 출력 |

프레임별 좌표는 표현 계층에서 관리하고, 실제 저장·카드 확정·상담 전송은 기존 유스케이스에 위임한다. UI 정리로 데이터 계약까지 바뀐 것으로 표시하지 않는다.

## 프레임과 선택 확정을 분리한다.
아래 코드는 실제 구현의 핵심 구문을 읽기 좋게 줄바꿈한 것이다.

```
const gesture = Gesture.Pan()
  .activeOffsetX([-10, 10])
  .failOffsetY([-20, 20])
  .onUpdate(e => {
    if (enabled.value) x.value = e.translationX * .92;
  });
```

| 구간 | 처리 |
| --- | --- |
| 드래그 중 | RN UI thread / 웹 transform으로 좌표 반영 |
| 미선택 release | withSpring(0)으로 복귀 |
| 선택 release | 퇴장 → onChoice → 새 장면 진입 |
| API 실패 | 기존 스토리 장면 복귀·오류 유지 |
| background/unmount | 진행 애니메이션·RAF 정리 |

RotarySurface의 p를 카드 팬과 회전 눈금이 함께 사용한다. React에는 눈금 경계 변경과 선택·결과 상태만 전달한다. 반복 rolling 라벨은 보조기술 탐색에서 숨기고 현재 값을 제공한다.

## 변경된 것과 보존한 것.
기존 사용자 작업·테스트·원본 에셋을 함께 보호한다.

| 영역 | 모션 개편 결과 |
| --- | --- |
| 제품 파일 | RN28·웹23파일, 소스 manifest로 추적 |
| 원본 에셋 | 타로 앞면·캐릭터 몸/손 파일 보존 |
| 테스트 | 새 디자인 테스트0, 기존 수정0 |
| 기존 작업 | 미완료 결제 관련13파일 SHA 불변 확인 |
| 검증 | 타입·웹 build·Android release·실제 조작·기존 CI |
| 한계 | 모든 OS·실기기·촉각 품질·운영 OTA 완료 아님 |

현재 상담의 기본 가이드는 legacy 전송 경로, GPT는 별도의 v2 대화·작업 경로를 사용한다. 다음 입력창 개선에서 화면의 주 진입과 전송 책임을 먼저 정리해야 한다.

타로3장은 신규 경로,1·5장은 기존 경로다. AI v2의 card_insights와 position은 현재3장 범위이므로,5장 지원은 계약·검증·화면을 함께 대조해야 한다.

## 기능 단위 커밋과 다음 분할.
아래는 모션 개편 당시 실제 원격 확인 커밋이다. 과거 모든 개발이 TDD였다고 표시하지 않는다.

| 단위 | RN | 웹 |
| --- | --- | --- |
| 모션 기반 | 11965184c3e4 | f36b4d208eda |
| 화면 연결 | ecdfd8b7bdb8 | 61efd866fa82 |
| 접근성 | a27cad0df3ae | 4bc1cb26efed |
| 보고서 | 11051923a22e | 2a16d561b666 |

입력 동선 → 계약 정리 → AI 연결 → 관련 검증 → 문서·영상 갱신

다음 기능도 이 크기로 분할한다. UI 구현·서버 계약·외부 AI 경계·미디어를 한 커밋에 섞지 않는다. 각 push 후 원격 SHA와 관련 CI를 확인한다.

현재 문서·영상 기준 코드와 원격 최신 상태는 동봉한 source-baseline·delivery 증거에서 확인할 수 있다.
