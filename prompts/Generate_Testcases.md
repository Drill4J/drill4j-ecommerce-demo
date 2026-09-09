# Generate test cases from requirements

Use this prompt when you have a specification, user story, acceptance
criteria, or feature description and need structured, review-ready test
cases.

## Prompt

```text
You are an experienced QA engineer. Given the requirements below, derive
test cases that fully cover the described behavior.

Requirements:
<paste requirements, acceptance criteria, or spec excerpts here>

Instructions:
1. Extract explicit rules, actors, data fields, validations, states, and
   integrations from the text. Note anything ambiguous and propose reasonable
   assumptions in a short "Assumptions" list before the test cases.
2. For each distinct behavior or rule, create at least one test. Include:
   - Positive paths (happy path, valid inputs, expected success outcomes).
   - Negative paths (invalid input, forbidden actions, error messages or
     failure modes if implied).
   - Boundary or equivalence cases when inputs have limits, ranges, or
     optional fields.
   - Dependencies: prerequisites, data setup, or ordering if the
     requirements imply a workflow.
3. Assign each test a clear title, preconditions (if any), steps, and
   expected result. Keep steps atomic and verifiable.
4. Add traceability: for each test, reference the requirement sentence,
   bullet, or section it validates (quote or paraphrase briefly).
5. Suggest a priority (e.g. Critical / Major / Minor) and type (functional,
   UI, API, security, performance) when inferable from context.

Output format:
- Assumptions (bullets, or "None").
- A markdown table with columns: ID, Title, Type, Priority, Requirement
  reference, Preconditions, Steps, Expected result.
- A short "Coverage gaps" section: behaviors not clearly specified that would
  need clarification before automating or signing off.
```

## Tips

- Replace the placeholder line with the actual requirements block.
- If the source is long, paste the relevant section only, or ask the model to
  focus on a named feature or user story first.
