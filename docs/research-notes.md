# Research notes and evaluation plan

## Research question

How can a compact statically typed functional language keep its formal typing rules, executable type checker, and runtime evaluator aligned as higher-order functions, recursion, lexical scoping, pairs, and explicit parametric polymorphism are introduced?

## Method

1. Define syntax, type syntax, and typing judgments before implementation.
2. Implement independent lexer, parser, AST, type checker, and evaluator stages.
3. Construct valid and deliberately invalid programs for each feature family.
4. Compare expected static outcomes with observed type-checker behavior.
5. For accepted programs, compare expected values with evaluator outputs.
6. Record disagreements as specification/implementation defects and revise either the rules or the implementation.

## Quantitative evaluation

`minilang.TestRunner` programmatically executes **185 programs**:

- 60 arithmetic programs
- 30 comparison/Boolean programs
- 25 lexical-scoping programs
- 20 ordinary function programs
- 10 pair/projection programs
- 10 recursive factorial programs
- 10 explicitly polymorphic identity programs
- 20 intentionally ill-typed programs

A successful final run reports **185 / 185 passed**, corresponding to 100% agreement on the defined evaluation suite.

## Error taxonomy

The implementation distinguishes more than eight classes of static failure, including undefined variables, unknown type variables, type mismatches, non-function application, invalid polymorphic application, non-Boolean conditions, branch-type disagreement, invalid operator operands, non-pair projection, and recursive return-type mismatch.

## Reproducibility

Run:

```bash
sbt "runMain minilang.TestRunner"
```

The test runner is deterministic and requires no external datasets or services.
