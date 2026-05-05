# 1년 학습 플랜 — WHOOP + Big Tech 준비 (4년차 → 5년차)

> **목표**: 1년 후 Google L4/L5, Meta E4/E5, Amazon SDE II/III 기술 면접 자신 있게 통과. 단기적으로 WHOOP 다음 라운드 통과.
>
> **원칙**: 매일 1.5~2시간, 주말 한 번 깊이. **꾸준함 > 강도**. 한 주 빠지면 회복 가능, 한 달 빠지면 리셋.

---

## 🎯 핵심 원칙 5가지

1. **하루 LeetCode 1문제** — 절대 안 빠뜨림. 컨디션 안 좋으면 easy 라도.
2. **영어 매일 30분** — speaking 위주. 듣기는 쉬우니 입 푸는 게 핵심.
3. **주 1회 deep work** — 토요일 2~3시간, system design 또는 side project.
4. **월말 자가평가** — 진도 체크 + 다음 달 조정.
5. **번아웃 방지** — 일요일은 쉬는 날. 휴가/명절은 강제 휴식.

---

## 📅 주간 리듬 — 매주 반복

| 요일 | 시간 | 내용 |
|---|---|---|
| **월** | 30분 + 60분 | LeetCode 1 (new) / **Track 1: 알고리즘** 패턴 학습 |
| **화** | 30분 + 60분 | LeetCode 1 (review) / **Track 2: Android 깊이** |
| **수** | 30분 + 60분 | LeetCode 1 (new) / **Track 3: CS 기초** |
| **목** | 30분 + 60분 | LeetCode 1 (review) / **Track 4: System Design** |
| **금** | 30분 + 60분 | LeetCode 1 (new) / **Behavioral / 영어 mock** |
| **토** | 2~3시간 | **Deep work** — side project, 블로그, system design 풀 문제 |
| **일** | — | 휴식 또는 catch-up (밀린 거 만회) |

**매일 기본**:
- 출근길/점심: 영어 팟캐스트 또는 듣기 (passive)
- 저녁 30분: LeetCode + 영어 풀이 설명 (active speaking)
- 저녁 60분: 그날의 트랙 학습

---

## 🗺️ 12개월 분기별 로드맵

### Q1 (1~3개월차) — **Foundation + WHOOP 라운드**

**LeetCode**: 60문제 (easy 20 + medium 40)
- Two pointers, sliding window, hash map
- Binary search, recursion 기초
- BFS / DFS 입문
- → **NeetCode 150 의 첫 50개**

**CS 기초** — OS:
- Process vs Thread, context switch
- Lock / Mutex / Semaphore
- Memory: stack/heap, virtual memory, paging
- Scheduling: round-robin, CFS
- → 자료: **Operating Systems: Three Easy Pieces** (무료 PDF) Part 1

**Android**:
- Kotlin Coroutines 깊이 — structured concurrency, exception handling, supervisorScope
- Flow internals — cold/hot, channelFlow, callbackFlow, sharedIn/stateIn
- → 자료: **Roman Elizarov 의 KotlinConf 토크** + 공식 docs

**System Design**: 입문만
- HTTP, REST 복습
- SQL vs NoSQL 언제 뭘?
- Caching 기초

**Behavioral**:
- STAR 스토리 5개 outline (영어로 bullet point)
- 자기소개 30초 → 1분 → 2분 버전 3개

**WHOOP 관련**:
- 다음 라운드 진행 (live coding / system design / behavioral)
- 매주 mock interview 1회

**월말 마일스톤**:
- M1: NeetCode 50개 풀음, OS 1회독 절반, Coroutines 자유롭게 설명
- M2: NeetCode 80개, OS 끝, Flow internals 마스터
- M3: NeetCode 120개, System design 5개 시도, WHOOP 결과

---

### Q2 (4~6개월차) — **Build**

**LeetCode**: 누적 150문제 (medium 70%)
- DP 1D / 2D 입문
- Graph (BFS / DFS / Dijkstra 기초)
- Heap / Priority Queue
- → NeetCode 150 완주

**CS 기초** — Networking:
- TCP 3-way / 4-way
- HTTP/1.1 → 2 → 3 차이
- TLS handshake
- DNS, CDN
- WebSocket vs SSE vs Long polling
- → 자료: **High Performance Browser Networking** (Ilya Grigorik, 무료 온라인)

**Android**:
- Compose deep dive — recomposition, `remember`, `derivedStateOf`, stable types
- Hilt advanced — `@Binds`, multibinding, custom scopes
- Testing — Turbine, MockK, Compose UI test, screenshot test
- → 자료: **droidcon 영상**, Cash App engineering blog

**System Design**:
- 첫 클래식 문제 5개 (URL shortener, Twitter feed, chat app, Uber, Netflix)
- → 자료: **System Design Primer** (GitHub) + **ByteByteGo Volume 1**

**Behavioral**:
- STAR 스토리 7개 완성 (영어 fluent 버전)
- Amazon LP 16개 외우기 + 매핑

**Side**:
- 기술 블로그 시작 (Medium / 본인 블로그) — 월 1편
- 첫 글 추천: BLE GATT lifecycle 트러블슈팅 스토리

**월말 마일스톤**:
- M4: NeetCode 130개, Networking 1회독, Compose recomposition 자유롭게 설명
- M5: System design 5개 풀어봄, 블로그 첫 글 발행
- M6: NeetCode 150 완주, behavioral 7개 영어 fluent

---

### Q3 (7~9개월차) — **Depth**

**LeetCode**: 누적 220문제
- Medium 마스터, Hard 입문 (월 5문제)
- Backtracking, Trie, Union-Find
- DP 어려운 패턴 (LCS, edit distance, knapsack)

**CS 기초** — Database:
- B-tree vs LSM-tree
- ACID, transaction isolation levels
- Indexing 종류
- Replication / Sharding
- → 자료: **Database Internals** (Alex Petrov)

**Android**:
- Internals — ART, Binder, Looper, GC
- Performance — startup time, baseline profiles, frame timing
- Modularization — feature module, build speed
- Security — Keystore, certificate pinning, ProGuard/R8
- → 자료: **Now in Android** 매주, Android Developer docs deep dive

**System Design** — 분산 시스템:
- CAP theorem
- Consistent hashing
- Message queue (Kafka, SQS)
- Microservices 패턴
- → 자료: **Designing Data-Intensive Applications (DDIA)** Part 1 정독

**Mobile system design**:
- Offline-first sync
- Push notification at scale
- Real-time chat
- File upload (chunked, resumable)

**Side**:
- Open source PR 1~2개 (AndroidX, Square 라이브러리, OkHttp 등 작은 docs/test PR 부터)
- 블로그 월 1편 유지

**월말 마일스톤**:
- M7: NeetCode 180, DB B-tree/LSM 자유롭게 설명, 첫 OSS PR
- M8: DDIA Part 1 1회독, system design 10개 누적
- M9: NeetCode 220, mobile system design 5개

---

### Q4 (10~12개월차) — **Polish + Apply**

**LeetCode**: 누적 300문제
- Google tag 50문제 (LeetCode Premium 한 달 구독)
- Hard 30문제 누적
- 회사별 frequency top 50 review

**CS 기초** — Distributed Systems:
- DDIA Part 2 (Replication, Partitioning, Transactions)
- DDIA Part 3 (Stream processing)
- → 보너스: **MIT 6.824** 강의 (선택)

**Android**:
- 본인 도메인 expertise 정리 — 블로그 / talk
- droidcon talk proposal 도전 (떨어져도 OK, 정리 자체가 자산)

**System Design**:
- Mock interview 주 1회
- 회사별 자주 나오는 문제 review
  - Google: GFS, Chubby, Spanner 영감 받은 design
  - Amazon: DynamoDB, S3, CloudFront 패턴
  - Meta: News feed, messaging at scale

**Behavioral**:
- 영어 mock interview 주 2회 (Cambly, ChatGPT, friend)
- 회사별 LP / values 매핑 완성

**Apply**:
- LinkedIn 프로필 업데이트, 영어 resume 다듬기
- Recruiter outreach 시작 (Meta, Amazon 부터)
- Referral 요청 — 한국인 미국 개발자 커뮤니티

**월말 마일스톤**:
- M10: NeetCode 250, DDIA Part 2 절반, mock interview 시작
- M11: NeetCode 280, Hard 20문제, 첫 recruiter screen
- M12: NeetCode 300+, **첫 onsite 라운드 도전** (Meta/Amazon)

---

## 📋 하루 템플릿 (출근일 기준)

```
[ ] 06:30~07:00  영어 듣기 (출근길 팟캐스트)
[ ] 12:30~13:00  LeetCode 1문제 (점심 30분)
                  - 영어로 풀이 설명 녹음
                  - 못 풀면 30분 후 답 보기 → 다음날 다시
[ ] 19:00~20:00  그날의 트랙 학습 (1시간)
                  - 월: 알고리즘 패턴
                  - 화: Android 깊이
                  - 수: CS 기초
                  - 목: System design
                  - 금: Behavioral / 영어 speaking
[ ] 22:30~22:45  내일 LeetCode 문제 미리 보기 (15분 prime)
```

**주말**:
```
[ ] 토 09:00~12:00  Deep work — system design 1문제 풀이 또는 블로그 글 또는 OSS PR
[ ] 일              REST (꼭 쉬어야 다음 주 굴러감)
```

---

## 📚 자료 — Top Pick 만

**알고리즘**:
- ⭐ **NeetCode 150** (neetcode.io) — 무료, 패턴별, 영상 설명
- LeetCode Premium ($35/월) — 면접 직전 회사별 1~2개월 구독
- **Blind 75** — NeetCode 150 의 핵심 요약, 시간 없을 때 우선순위

**CS 기초**:
- ⭐ **OSTEP** (Operating Systems: Three Easy Pieces) — 무료 PDF, OS 바이블
- ⭐ **High Performance Browser Networking** — 무료 온라인, 실용적
- **Database Internals** (Alex Petrov) — DB 깊이
- **DDIA** (Designing Data-Intensive Applications) — Senior 필수, 1년에 1회 정독

**Android**:
- ⭐ **Android Developer 공식 docs** — release notes 매주
- **Now in Android** 유튜브 — 매주 follow
- **droidcon 영상** — 시니어 토픽
- Square / Cash App / Slack / Airbnb engineering blog

**System Design**:
- ⭐ **System Design Primer** (GitHub, 무료)
- **ByteByteGo Volume 1, 2** (Alex Xu)
- **Grokking the System Design Interview** ($79)
- **MIT 6.824** 유튜브 (선택, 깊게 가고 싶으면)

**Behavioral**:
- **Cracking the Coding Interview** behavioral 챕터
- Amazon LP 16개 — Amazon careers 페이지
- **The Tech Resume Inside Out** (Gergely Orosz)

**영어**:
- ⭐ **Cambly** 또는 **iTalki** — native 1:1
- **ChatGPT/Claude voice mode** — mock interview 무료
- 팟캐스트: Lex Fridman, Software Engineering Daily, The Changelog

---

## 📊 월말 자가평가 (5분, 매월 마지막 일요일)

```
□ 이번 달 LeetCode 누적: ___ 문제
□ 이번 달 막힌 패턴 1개: ___________
□ Android 새로 배운 것 1개: ___________
□ CS 새로 배운 것 1개: ___________
□ 영어 자신감 변화 (1~10): ___ → ___
□ 다음 달 조정할 것: ___________
□ 번아웃 신호 있나? (수면, 운동, 사회생활) ___
```

**Red flag** (감지하면 즉시 페이스 조정):
- 일주일에 LeetCode 3일 이상 빼먹음
- 잠 부족 (6시간 미만 3일 연속)
- 주말 deep work 2주 연속 못 함
- "이거 다 못할 것 같다" 자포자기

→ 1~2일 완전 휴식 후 plan 자체 재조정. **자기 페이스 유지가 1년 완주의 핵심**.

---

## 🎬 오늘 당장 시작할 것 3개

1. **LeetCode 계정 만들고 오늘 medium 1문제 풀기** — 영어로 풀이 녹음
2. **NeetCode 150 북마크** + 첫 5개 문제 미리 봄 (내일~일주일 분량 prime)
3. **Cambly 또는 iTalki 등록** — 첫 30분 세션 예약

이 3개만 오늘 끝내면 1일차 완료. 나머지는 내일부터 위 템플릿대로.

---

## 💪 마지막 — 마인드셋

> "하루는 짧고 1년은 길다. 매일 1%만 나아지면 1년 후 37배 (1.01^365 ≈ 37). 1% 못 했어도 0.5%면 6배 (1.005^365 ≈ 6). **0% 가 아닌 게 핵심.**"

- 7년차 Google 은 멀어 보이지만, **WHOOP 다음 라운드는 내일 일** 일 수 있음. 단기 목표가 장기 모티베이션.
- 비자/거주 압박은 부담이지만 **포커스 강화** 로 활용. 한국 개발자 평균보다 2~3배 절박하면 그 자체가 advantage.
- 매주 일요일 "이번 주 잘했어" 한 번 자축. 번아웃 방지 루틴.

화이팅. 매월 진도 공유해주시면 같이 조정해 갈 수 있습니다.
