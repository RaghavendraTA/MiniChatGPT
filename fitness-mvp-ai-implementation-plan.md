# MiniChatGPT Fitness MVP - AI Implementation Plan

This document is written as an execution brief for an AI coding agent. It is intentionally phase-based, deterministic, and scoped to reduce hallucination. The app already uses Spring Boot, Java 21, LangChain4j, and MongoDB; the plan below assumes those are the foundations.

---

## 1. Product objective

Build a Goal-driven Fitness Planning Platform where:

- the domain logic is deterministic and non-LLM-driven
- LangChain4j acts as a conversational orchestration layer only
- users can create a fitness profile, select goals, and receive a personalized plan
- the app can later evolve into tracking, progress review, and adaptation

The most important architectural rule:

- User -> Context -> Goals -> Strategy -> Safety -> Calculations -> Plan -> Progress -> Adaptation
- LangChain4j wraps the domain; it does not become the domain

---

## 2. Core guardrails for the implementation

Before writing code, the AI agent must follow these rules:

1. Do not implement LangChain4j before the core fitness engine exists.
2. Do not let the LLM calculate BMI, BMR, TDEE, calories, or macros.
3. Do not diagnose medical conditions or override safety constraints.
4. Do not create 30 goals or 30 tools at once; keep the MVP small and explicit.
5. Prefer deterministic Java services and records over LLM-generated logic.
6. Use MongoDB repositories with Spring Data, since the project already depends on MongoDB.
7. Keep one source of truth: the generated `FitnessPlan` object.
8. Build one phase at a time and stop after each phase to validate the result.

---

## 3. Recommended package structure

Use a package layout matching the current project style, under:

```text
com.ragta.miniChatGPT
├── api
│   ├── UserController
│   ├── GoalController
│   ├── FitnessPlanController
│   └── AdvisorController
├── domain
│   ├── user
│   │   ├── User
│   │   ├── PhysicalMeasurement
│   │   ├── HealthCondition
│   │   ├── ActivityProfile
│   │   └── UserRepository
│   ├── goal
│   │   ├── GoalProfile
│   │   ├── PrimaryObjective
│   │   ├── SecondaryObjective
│   │   ├── HealthObjective
│   │   ├── GoalStrategy
│   │   └── GoalStrategyResolver
│   ├── fitness
│   │   ├── FitnessMetrics
│   │   ├── FitnessCalculationService
│   │   ├── BmiCalculator
│   │   ├── BmrCalculator
│   │   ├── TdeeCalculator
│   │   └── MacroCalculator
│   ├── nutrition
│   │   ├── NutritionTarget
│   │   ├── NutritionCalculator
│   │   └── NutritionPlan
│   ├── workout
│   │   ├── TrainingPlan
│   │   ├── WorkoutPlan
│   │   └── WorkoutService
│   ├── safety
│   │   ├── SafetyAssessment
│   │   ├── SafetyService
│   │   └── SafetyRule
│   ├── plan
│   │   ├── FitnessPlan
│   │   ├── FitnessPlanEngine
│   │   └── PlanBuilder
│   └── progress
│       ├── ProgressMetrics
│       ├── ProgressMetricStrategy
│       └── WeeklyProgress
├── strategy
│   ├── WeightManagementStrategy
│   ├── HypertrophyStrategy
│   ├── StrengthStrategy
│   ├── EnduranceStrategy
│   ├── AthleticPerformanceStrategy
│   ├── GeneralFitnessStrategy
│   ├── MetabolicHealthStrategy
│   └── CardiovascularHealthStrategy
├── agent
│   ├── FitnessAdvisor
│   ├── AdvisorTools
│   ├── AdvisorSystemPrompt
│   └── AgentConfig
├── rag
│   ├── NutritionRetriever
│   ├── WorkoutRetriever
│   ├── DocumentMetadata
│   └── VectorStoreConfig
├── services
│   ├── ProfileCompletenessService
│   ├── UserContextService
│   └── GoalContextService
├── dto
│   ├── UserProfileRequest
│   ├── GoalRequest
│   ├── FitnessPlanRequest
│   └── AdvisorChatRequest
└── config
    └── LangChain4jConfig
```

---

## 4. Master phased roadmap

### Phase 1 — Profile and user context

Goal:
- create the base profile model and persistence
- enable later plan generation from real user input

Required classes:
- `User`
- `PhysicalMeasurement`
- `HealthCondition`
- `ActivityProfile`
- `UserContext`
- repositories and service layer

Minimum user data:
- date of birth
- sex / gender
- height
- weight
- activity level
- fitness level

Acceptance criteria:
- user can be created via API
- measurements can be recorded historically
- health conditions can be stored and retrieved
- app exposes a way to fetch current profile + latest measurement context

AI prompt to execute this phase:

```text
Implement Phase 1 of the fitness MVP in this Spring Boot Java project.

Requirements:
- Create the profile domain model with User, PhysicalMeasurement, HealthCondition, ActivityProfile, and UserContext.
- Use MongoDB via Spring Data repositories.
- Make PhysicalMeasurement historical, not overwritten.
- Add REST endpoints for creating a user and saving measurements and health data.
- Keep the data model simple enough for the MVP but structured for future goal and plan generation.
- Use Lombok for boilerplate, Java 21, and Spring Boot conventions.
- Include validation for required fields such as DOB, height, weight, activityLevel, and fitnessLevel.

Deliverables:
- model classes
- repositories
- services
- DTOs
- controllers
- tests for the main profile flows

Do not yet implement the LangChain4j advisor or the RAG layer.
```

### Phase 2 — Deterministic fitness calculation engine

Goal:
- compute metrics without LLM involvement

Required classes:
- `FitnessMetrics`
- `FitnessCalculationService`
- `BmiCalculator`
- `BmrCalculator`
- `TdeeCalculator`
- `MacroCalculator`
- `CalorieCalculator`

Key formulas:
- BMI = weight / height²
- BMR: choose one documented formula and keep it behind `BmrCalculator`
- TDEE = BMR × activity multiplier
- calculate calories and macros from goal + metrics

Acceptance criteria:
- `calculate()` returns bmi, bmr, tdee
- macros can be derived for the selected goal
- calculations remain deterministic and unit-testable

AI prompt:

```text
Implement Phase 2: the deterministic fitness calculation engine.

Requirements:
- Add a `FitnessMetrics` record with bmi, bmr, and tdee.
- Create `FitnessCalculationService` as the main entry point.
- Implement `BmiCalculator`, `BmrCalculator`, and `TdeeCalculator` behind interfaces.
- Use a documented formula such as Mifflin-St Jeor for BMR.
- Add `NutritionTarget` with calories, protein grams, carbohydrate grams, fat grams, and fiber grams.
- Create a nutrition calculator that can calculate targets based on user context and goal.
- Keep all logic in Java; no LLM reasoning in this layer.
- Add tests covering BMI, BMR, TDEE, and macro outputs.

Do not add any agent or conversational logic here.
```

### Phase 3 — Goal framework and strategy resolution

Goal:
- support multiple fitness objectives and strategy selection

Required classes:
- `GoalProfile`
- `PrimaryObjective`
- `SecondaryObjective`
- `HealthObjective`
- `TrainingFocus`
- `GoalTarget`
- `GoalStrategy`
- `GoalStrategyResolver`
- implementations: `WeightManagementStrategy`, `HypertrophyStrategy`, `StrengthStrategy`, `EnduranceStrategy`, `AthleticPerformanceStrategy`, `GeneralFitnessStrategy`, `MetabolicHealthStrategy`, `CardiovascularHealthStrategy`

Acceptance criteria:
- a user can create a goal profile with primary + secondary objectives
- resolver selects the correct strategy based on goal
- strategy can produce both nutrition and training output

AI prompt:

```text
Implement Phase 3: goal framework and strategy resolution.

Requirements:
- Create `GoalProfile` with primaryObjective, secondaryObjectives, trainingFocus, healthObjective, and goalTarget.
- Use a small but extensible enum set, not a large sprawling enum catalog.
- Add `GoalStrategy` with `nutrition()`, `training()`, and `progressMetrics()` methods.
- Implement the strategy resolver and map major goals to the proper strategy class.
- Keep the logic deterministic and domain-driven.
- Add tests confirming resolver mapping for weight management, hypertrophy, endurance, and general fitness.

Do not use the LLM to decide goals or strategy.
```

### Phase 4 — Safety engine and plan engine

Goal:
- create a safe recommendation system that respects user medical and behavioral constraints

Required classes:
- `SafetyAssessment`
- `SafetyService`
- `FitnessPlan`
- `TrainingPlan`
- `NutritionPlan`
- `FitnessPlanEngine`

Behavior:
- evaluate medical constraints before plan generation
- if a user has a joint issue or high impact restriction, the engine should adapt training rather than changing the goal
- keep `SafetyAssessment` as a constraint layer, not an objective layer

Acceptance criteria:
- plan engine produces a complete plan with goal, metrics, nutrition, training, safety, and progress metrics
- safety constraints are reflected in training plan modifications
- no LLM is used to decide if a plan is medically safe

AI prompt:

```text
Implement Phase 4: safety and fitness plan generation.

Requirements:
- Create `SafetyService` and `SafetyAssessment` with status, restrictions, and flags.
- Build a `FitnessPlan` as the canonical recommendation object.
- Build `FitnessPlanEngine` that combines user context, goal profile, calculations, strategy, and safety constraints.
- Ensure the plan engine returns a single coherent plan object that can be used by the API and later by the advisor.
- Add logic where safety constraints modify training modality without redefining the user's goal.
- Add tests for safe plan generation, including a joint limitation case.

This phase should be deterministic Java only.
```

### Phase 5 — LangChain4j advisor and tool layer

Goal:
- create a conversational interface around the deterministic fitness engine

Required classes:
- `FitnessAdvisor` as an `@AiService`
- `AdvisorTools`
- `AdvisorSystemPrompt`
- `AgentConfig`
- controller endpoint for chat

Required tools:
- `getUserFitnessContext(String userId)`
- `generateFitnessPlan(String userId, GoalProfile goal)`
- `getProgress(String userId)`
- `searchNutrition(String query)`
- `searchExercises(String query)`

Critical rules:
- the agent cannot calculate BMI/BMR/TDEE or decide calories/macros itself
- it must use the tool-backed domain engine
- it should explain the results in natural language, not invent numbers

Acceptance criteria:
- chat endpoint accepts a message and returns a response
- the LLM uses the domain tools to retrieve user context and generate the plan
- advisor explains the plan, not the formulas

AI prompt:

```text
Implement Phase 5: the LangChain4j fitness advisor.

Requirements:
- Build a Spring Boot `@AiService` interface named `FitnessAdvisor`.
- Add a system prompt instructing the model to use tools, gather user context, and explain the generated plan.
- The agent must not independently calculate BMI, BMR, TDEE, calories, or macros.
- Create the minimal tool set: getUserFitnessContext, generateFitnessPlan, getProgress, searchNutrition, and searchExercises.
- Expose a controller endpoint like POST /api/v1/users/{id}/advisor/chat.
- Keep the tool names and business logic small and purposeful.
- Ensure the agent is only a wrapper around the deterministic engine.

Do not add multi-agent architecture or heavy orchestration yet.
```

### Phase 6 — Missing information flow and profile completeness

Goal:
- ask the user for the data required before generating a personalized plan

Required classes:
- `ProfileCompletenessService`
- `MissingInformation`
- validation rules for each goal type

Example:
- endurance plan requires DOB, sex, height, weight, activity level, fitness level

Acceptance criteria:
- when required data is missing, the advisor asks focused clarifying questions
- generation does not proceed with incomplete profile data
- a deterministic evaluation is returned with required fields list

AI prompt:

```text
Implement Phase 6: profile completeness checks.

Requirements:
- Create `ProfileCompletenessService` that evaluates whether required profile data exists for a selected goal.
- Return a structured `MissingInformation` result listing missing fields and required context.
- Attach this service to the conversational flow so the AI asks missing questions before generating a plan.
- Support at least endurance, fat loss, muscle gain, and general fitness scenarios.
- Keep behavior deterministic and explicit; do not rely on freeform LLM guessing.
```

### Phase 7 — RAG knowledge layer for nutrition and workout guidance

Goal:
- provide knowledge retrieval for questions that require factual support but not numeric calculation

Required classes:
- `NutritionRetriever`
- `WorkoutRetriever`
- document metadata model
- vector store configuration

Document categories:
- nutrition_documents
- workout_documents

Metadata examples:
- domain, category, goal, difficulty, equipment, impact, diet

Acceptance criteria:
- RAG answers general questions like vegetarian breakfast ideas and exercise variations
- RAG does not answer numerical calorie targets or medical advice
- retrieval is filtered using goal, diet, equipment, impact, and health constraints

AI prompt:

```text
Implement Phase 7: nutrition and workout RAG layer.

Requirements:
- Configure retrieval for nutrition and workout knowledge documents.
- Add metadata filters for goal, equipment, impact, diet, category, and difficulty.
- Build a retrieval service that uses user context plus goal and safety constraints when searching the vector store.
- Keep retrieval focused on knowledge questions, not calculation questions.
- Add a few sample nutrition and workout docs to prove the retrieval pipeline works.

Do not let the LLM decide calorie or macro numbers from retrieved docs.
```

### Phase 8 — Progress tracking and weekly adaptation

Goal:
- enable habit and performance tracking that can adjust plans over time

Required classes:
- `DailyNutrition`
- `DailyActivity`
- `WorkoutSession`
- `WeightMeasurement`
- `HealthMetric`
- `ProgressMetricStrategy`
- `ProgressAnalyzer`
- `WeeklyProgress`

Important principle:
- use meaningful metrics depending on goal
- for endurance, use pace, duration, distance, heart rate, VO2max-related indicators
- for bodybuilding, use volume, sets, reps, strength
- for general fitness, use steps, active minutes, training frequency

Acceptance criteria:
- data model supports weekly comparisons
- app can calculate a trend and recommend plan adjustments
- the LLM explains changes, while the engine determines them

AI prompt:

```text
Implement Phase 8: tracking and weekly adaptation.

Requirements:
- Add a tracking model for daily nutrition, workouts, weights, and health metrics.
- Add a `ProgressMetricStrategy` that returns appropriate metrics based on the selected goal.
- Build a progress analyzer that compares weekly data and recommends adjustments.
- Use deterministic logic to evaluate trends.
- Keep the LLM in the explanation layer, not the analytical decision layer.

This is the final phase of the MVP foundation.
```

---

## 5. Implementation sequence to avoid drift

Use this exact sequencing:

1. Phase 1: Profile and context
2. Phase 2: Fitness calculations
3. Phase 3: Goals and strategies
4. Phase 4: Safety + plan engine
5. Phase 6: Profile completeness
6. Phase 5: LangChain4j advisor
7. Phase 7: RAG
8. Phase 8: Progress and planning adaptation

Rationale:
- Phase 5 depends on Phase 4 being stable
- Phase 6 belongs before or alongside the agent flow
- Phase 7 should only come after the engine works
- Phase 8 should be introduced only after the base plan pipeline is functional

---

## 6. Minimal MVP scope

This MVP should not include:

- multi-agent orchestration
- external medical diagnosis features
- advanced personalization engine beyond deterministic strategies
- complex admin dashboards
- broad tool ecosystem
- large healthcare compliance controls

The MVP only needs:

- user profile
- measurable goals
- deterministic plan generation
- basic advisor chat
- minimal retrieval for nutritional or workout knowledge
- simple tracking and weekly comparison

---

## 7. AI execution checklist

Before each phase starts, the AI must confirm:

- what layer is being built: domain, service, API, or AI wrapper
- what classes are being created
- what tests are required
- what is explicitly out of scope
- what previous phase output is assumed to exist

After each phase, the AI must validate:

- build compiles
- relevant tests pass
- APIs respond as expected
- no LLM logic replaced domain logic

---

## 8. Prompt template for future AI phases

Use this template when asking the AI to implement the next phase:

```text
Implement Phase X of the MiniChatGPT fitness MVP.

Project context:
- Spring Boot 3/Java 21 app
- LangChain4j already included
- MongoDB already configured
- Domain-first design required
- LLM is orchestration, not domain logic

Goal:
[describe target behavior]

Required classes:
- [class list]

Constraints:
- No LLM calculation logic in this phase
- Keep code deterministic and testable
- Follow existing Spring Boot project conventions
- Use package names matching the project structure
- Add unit tests for critical behavior
- Do not implement future phases until this phase is stable

Deliverables:
- production code
- controller/API layer if needed
- repository or service layer if needed
- tests
- short summary of what was implemented and what remains for the next phase
```

---

## 9. Recommended deliverable split for human + AI execution

Recommended approach:

- Plan A: implement the core domain and deterministic services first
- Plan B: add the advisor and tool layer second
- Plan C: add RAG and tracking later

This avoids one giant prompt that causes confusion and hallucination.

---

## 10. Final instruction to the AI

The implementation should be incrementally built from the deterministic domain outward. Do not start by creating a fancy agent and do not hide business logic in the language model. Build the Java fitness engine first, prove it works with tests, then wrap it with LangChain4j and add RAG only when the core recommendation pipeline exists.
