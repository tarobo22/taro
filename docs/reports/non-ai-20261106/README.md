# 여운 비AI 구현·디자인 보고서

## 최신 타로 화면 반영

[설정 한 화면·직접 시작·모션·검증·사진](compact-setup/README.md)을 추가했다. 신규1+기존 관련4개 통과는 이전 전체39와 분리한다. 최신 Web JS773.65kB/gzip217.15kB이며 아래의 기존 로그·통계는 이전 스냅샷으로 보존한다.

확인일: 2026-11-06. 현재 Android·웹 비AI 구현과 로컬/원격 검증 결과다. 실제 OAuth·실과금·iOS native·운영 배포는 별도다. AI 처리 로직·모델·프롬프트 추가 개발과 실제 모델 호출은 이번 범위에서 제외했다.

[Notion 비공개 인계 초안](https://app.notion.com/p/410b37c6fe7281249a4dfa62b85a3c46?pvs=204) · [RN 보고서](https://github.com/tarobo22/taro/tree/codex/compact-tarot-setup-20261106/docs/reports/non-ai-20261106) · [Backend 계약](https://github.com/tarobo22/taro-back/tree/codex/complete-non-ai-20261106/docs/quality/non-ai-20261106) · [Web QA](https://github.com/tarobo22/taro-raect/tree/codex/compact-tarot-setup-20261106/docs/quality/non-ai-20261106)

이전 전체 검증: Back532 / RN133 / Web130, 브라우저39, Android JVM6 통과. 세 저장소 GitHub CI도 성공했다. 기존 baseline 테스트 수정·삭제·skip0. 같은 공유 규칙 검증을 품질 점수나 사용자 성능으로 합산하지 않는다.

## 읽는 순서

1. [검수한 18쪽 PDF](report.pdf): 기능 결과·실제 연속 화면·디자인 시스템·아키텍처·API·데이터·TDD·성능·남은 출시 작업.
2. [개발자 해설](implementation.md): 실제 코드와 계약·의도·한계.
3. [비AI 46개 추적표](non-ai-requirement-evidence.csv) / [전체 68개 분류](requirements-68-classification.csv).
4. [설계값 대조](design-values-reconciliation.csv), [전체 실제 변경 경로](all-changed-files.csv).
5. screens/의 앱 흐름과 18초 영상, native/의 에뮬레이터·OS 글자2배 증거.

원본 기획/상세/이전 구현 보고서는 ../../planning/에 보존한다. SDK·네이티브 변경은 [호환성 근거](ota-native-compatibility.md) 참조. 1.4.0 preview 번들은 해시 검증했지만 이번 로컬 서버 시작이 자동 승인 검토에서 거절되어 실제 다운로드·적용은 미실행이다. APK는 Git에 넣지 않았으며 별도 로컬 에뮬레이터 배포 산출물로 제공한다.

제품 검증 source cut은 RN d8576628 / Web00ab7343 / Backb152d564다. 이후 CI와 생성 계약, 이 보고서가 추가됐다. all-changed-files.csv는 문서 추가까지 포함하며, 실제 최종 원격 SHA는 PR 및 로컬 delivery.json에서 확인한다.

현재 PDF는18쪽이며 SHA256 `465d1246da6a57274c5606fc701d1eb534a880a674e82746d0537f3a4704bb39`다. compact-setup/의 실제 화면과 버전 메타데이터가 최신 UI 근거이며, 기존 final-evidence.json·all-changed-files.csv는 이전 구현 스냅샷으로 보존한다. 문서 갱신 이후 Git SHA는 PR#4의 최신 docs 커밋을 따른다.
