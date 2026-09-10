# MiniLang — Statically Typed Functional Language & Interpreter

**MiniLang** is a compact research-oriented programming-language implementation built with **Scala 3.3.4**. It provides an executable model of a small statically typed functional language, connecting **formal typing rules** with a working lexer, recursive-descent parser, AST, type checker, and environment-based evaluator.

The language explores how the following features interact:

* Static typing
* Lexical scoping
* First-class and higher-order functions
* Recursion
* Pair types
* Explicit parametric polymorphism
* Call-by-value evaluation
* Static error detection

The implementation is accompanied by formal-semantics notes and a deterministic **185-program evaluation suite** covering both valid and intentionally ill-typed programs.

---

## Why MiniLang?

Programming-language features are easy to study individually but become more interesting when they interact.

For example, adding recursion requires the type checker and evaluator to agree about recursive bindings. Higher-order functions require lexical environments and closures. Explicit polymorphism introduces type variables, universal types, and type substitution.

MiniLang provides a deliberately small environment for studying these interactions while keeping the implementation understandable and directly connected to a formal specification.

### Research Question

> **How can a compact statically typed functional language keep its formal typing rules, executable type checker, and runtime evaluator aligned as higher-order functions, recursion, lexical scoping, pairs, and explicit parametric polymorphism are introduced?**

---

# Architecture

MiniLang follows a complete source-to-execution pipeline:

```text
                    Source Program
                          │
                          ▼
                    ┌──────────┐
                    │  Lexer   │
                    └────┬─────┘
                         │ Tokens
                         ▼
                    ┌──────────┐
                    │  Parser  │
                    └────┬─────┘
                         │
                         ▼
                    ┌──────────┐
                    │   AST    │
                    └────┬─────┘
                         │
                 ┌───────┴────────┐
                 │                │
                 ▼                ▼
          ┌─────────────┐  ┌────────────┐
          │Type Checker │  │ Evaluator  │
          └──────┬──────┘  └──────┬─────┘
                 │                │
                 ▼                ▼
              Static           Runtime
               Type             Value
```

The implementation therefore separates:

**Syntax → Representation → Static Validation → Runtime Evaluation**

This separation makes individual components easier to inspect, test, and reason about.

---

# Key Features

| Feature                | Implementation                                                              |
| ---------------------- | --------------------------------------------------------------------------- |
| Lexer                  | Tokenizes identifiers, literals, keywords, operators, comments, and strings |
| Parser                 | Recursive-descent parser with operator precedence                           |
| AST                    | Explicit sealed expression and type hierarchies                             |
| Static typing          | Environment-based type checker                                              |
| Functions              | Typed first-class functions                                                 |
| Higher-order functions | Functions can be passed and returned as values                              |
| Recursion              | `let rec` with recursive closures                                           |
| Scoping                | Lexical scoping through captured environments                               |
| Pairs                  | Pair construction with `fst` / `snd`                                        |
| Polymorphism           | Explicit type abstraction and type application                              |
| Errors                 | Structured static type-error taxonomy                                       |
| Evaluation             | Environment-based call-by-value interpreter                                 |
| Testing                | Deterministic 185-program evaluation suite                                  |
| Formalization          | Typing judgments and operational-semantics notes                            |

---

# Language Design

MiniLang intentionally keeps its syntax small while supporting several core functional-programming concepts.

## Primitive Types

```text
Int
Bool
String
Unit
```

Examples:

```text
42
true
"hello"
unit
```

---

## Variables and Let Bindings

Local bindings use:

```text
let x = expression in expression
```

Example:

```text
let x = 10 in
x + 2
```

Nested bindings are supported:

```text
let x = 10 in
let y = x + 2 in
y * 2
```

The type checker maintains a static environment while the evaluator creates corresponding runtime environments.

---

## Conditional Expressions

```text
if true then
  1
else
  0
```

The condition must have type `Bool`, and both branches must produce the same type.

Therefore:

```text
if true then 1 else false
```

is rejected statically.

---

# Functions

## Typed Functions

Function parameters and return types are explicitly declared:

```text
fun(x: Int): Int => x + 1
```

Functions can be bound to variables and applied:

```text
let inc = fun(x: Int): Int => x + 1 in
inc(41)
```

The corresponding function type is:

```text
Int -> Int
```

---

## Higher-Order Functions

Functions are first-class values.

They can be passed as arguments:

```text
let apply = fun(f: Int -> Int): Int => f(10) in
apply(fun(x: Int): Int => x + 5)
```

They can also return other functions:

```text
let applyTwice = fun(f: Int -> Int): Int -> Int =>
  fun(x: Int): Int => f(f(x))
in
let inc = fun(x: Int): Int => x + 1 in
applyTwice(inc)(10)
```

This requires the evaluator to preserve lexical environments through closures.

---

# Lexical Scoping & Closures

Function values capture the environment in which they are created.

Conceptually:

```text
Closure =
    parameter
    +
    function body
    +
    captured environment
```

This allows functions to reference variables from their defining lexical scope.

For example:

```text
let x = 10 in
let addX = fun(y: Int): Int => x + y in
addX(5)
```

The function retains access to `x` through its captured environment.

---

# Recursive Functions

MiniLang supports recursive bindings using `let rec`.

Example:

```text
let rec fact(n: Int): Int =
  if n == 0 then
    1
  else
    n * fact(n - 1)
in
fact(6)
```

The implementation represents recursive functions using a dedicated `RecClosure`.

During recursive application, the function name is reintroduced into the evaluation environment so that the function can reference itself.

---

# Pair Types

Pairs can contain values of different types:

```text
(1, true)
```

with type:

```text
(Int, Bool)
```

Components can be projected using:

```text
fst((1, true))
snd((1, true))
```

The type system represents pairs explicitly as:

```text
TPair(left, right)
```

---

# Explicit Parametric Polymorphism

MiniLang supports **explicit parametric polymorphism** rather than implicit Hindley–Milner-style type inference.

A polymorphic identity function can be written as:

```text
fun[T](x: T): T => x
```

Its type is represented as:

```text
forall T. T -> T
```

A concrete type can then be supplied explicitly:

```text
let id = fun[T](x: T): T => x in
id[Int](42)
```

The same polymorphic function can be instantiated with another type:

```text
let id = fun[T](x: T): T => x in
id[String]("hello")
```

The type checker represents universal types using `TForAll` and performs explicit type substitution when processing type applications.

---

# Operators

## Arithmetic

```text
+
-
*
/
```

Integer arithmetic is supported.

String concatenation is also supported through `+`:

```text
"Mini" + "Lang"
```

---

## Comparison

```text
<
<=
>
>=
```

Comparison operators operate on integers and produce `Bool`.

---

## Equality

```text
==
!=
```

Equality is supported for compatible non-function values, including primitive values and pairs.

---

## Boolean Logic

```text
&&
||
!
```

The evaluator implements **short-circuit evaluation** for `&&` and `||`.

For example:

```text
false && ...
```

does not evaluate the right-hand expression.

---

## Unary Operators

```text
-x
!x
```

`-` operates on `Int`, while `!` operates on `Bool`.

---

# Lexer

The lexer converts source text into typed tokens.

It recognizes:

* Identifiers
* Integer literals
* String literals
* Keywords
* Symbols
* Multi-character operators
* Line comments
* End-of-file

Supported multi-character operators include:

```text
=>  ->  ==  !=  <=  >=  &&  ||
```

String literals support common escape sequences including:

```text
\n
\t
\r
\"
\\
```

Lexical errors report the source offset where the problem was detected.

---

# Parser

MiniLang uses a **recursive-descent parser**.

The parser converts tokens into the explicit AST while handling operator precedence.

The precedence hierarchy is:

```text
||
&&
== !=
< <= > >=
+ -
* /
unary operators
function/type application
primary expressions
```

The parser supports:

* Let bindings
* Recursive bindings
* Conditionals
* Typed lambdas
* Explicit type parameters
* Function application
* Type application
* Pairs
* Pair projections
* Unary operators
* Binary operators

---

# Abstract Syntax Tree

Expressions are represented through a sealed `Expr` hierarchy.

The implementation includes **14 expression forms**:

```text
IntLit
BoolLit
StringLit
UnitLit
Var
Let
LetRec
IfThenElse
Lambda
Apply
TypeApply
PairExpr
Fst
Snd
Unary
Binary
```

The language type system is represented separately using:

```text
TInt
TBool
TString
TUnit
TPair
TFun
TVar
TForAll
```

Separating expressions from types keeps syntax representation independent from static checking and runtime evaluation.

---

# Static Type Checker

The type checker validates programs before evaluation.

The central judgment is:

```text
Γ ⊢ e : τ
```

which means:

> Under typing environment `Γ`, expression `e` has type `τ`.

The checker handles:

* Primitive types
* Variables
* Let bindings
* Recursive functions
* Conditionals
* Function types
* Function application
* Pair types
* Pair projections
* Unary operators
* Binary operators
* Type variables
* Universal types
* Explicit type application

---

## Example Typing Rule — Function Application

```text
Γ ⊢ e₁ : τ₁ → τ₂    Γ ⊢ e₂ : τ₁
---------------------------------
          Γ ⊢ e₁(e₂) : τ₂
```

---

## Example Typing Rule — Conditional

```text
Γ ⊢ c : Bool    Γ ⊢ t : τ    Γ ⊢ f : τ
---------------------------------------
        Γ ⊢ if c then t else f : τ
```

---

## Example Typing Rule — Pair

```text
Γ ⊢ e₁ : τ₁    Γ ⊢ e₂ : τ₂
---------------------------
        Γ ⊢ (e₁,e₂) : (τ₁,τ₂)
```

---

# Structured Type Errors

The implementation uses a structured `MiniTypeError` hierarchy instead of relying only on generic error strings.

It includes categories such as:

```text
UndefinedVariable
UnknownTypeVariable
TypeMismatch
ExpectedFunction
ExpectedPolymorphicFunction
ExpectedBooleanCondition
BranchTypeMismatch
InvalidOperandTypes
ExpectedPair
RecursiveReturnMismatch
InvalidTypeAnnotation
```

This makes static failures more precise and allows the test suite to verify that invalid programs are rejected.

---

# Evaluation Model

The interpreter uses an **environment-based, call-by-value evaluation strategy**.

Runtime values include:

```text
IntVal
BoolVal
StringVal
UnitVal
PairVal
Closure
RecClosure
```

The evaluator maintains:

```text
Environment : Variable → Value
```

Function values capture their lexical environment:

```text
Closure(
    parameter,
    body,
    capturedEnvironment
)
```

Recursive functions use:

```text
RecClosure(
    functionName,
    parameter,
    body,
    capturedEnvironment
)
```

This provides lexical scoping without implementing runtime evaluation through direct syntactic substitution.

---

# Complete Example

The following program combines recursion, polymorphism, function application, pairs, and primitive values:

```text
let id = fun[T](x: T): T => x in
let rec fact(n: Int): Int =
  if n == 0 then
    1
  else
    n * fact(n - 1)
in
(id[Int](fact(5)), id[String]("typed"))
```

The interpreter produces:

```text
Type : (Int, String)
Value: (120, "typed")
```

This example demonstrates several parts of the implementation working together:

```text
Polymorphic function
        +
Recursive function
        +
Explicit type application
        +
Function application
        +
Pair construction
        =
Typed runtime value
```

---

# Example Programs

The repository includes ready-to-run examples:

### Factorial

```text
examples/factorial.mini
```

Demonstrates recursive functions.

### Higher-Order Functions

```text
examples/higher_order.mini
```

Demonstrates functions receiving and returning functions.

### Polymorphic Identity

```text
examples/polymorphic_identity.mini
```

Demonstrates explicit parametric polymorphism.

### Type Error

```text
examples/type_error.mini
```

Demonstrates static rejection of an ill-typed program.

---

# Research Evaluation

MiniLang includes a deterministic evaluation program:

```text
src/main/scala/minilang/TestRunner.scala
```

The suite contains **185 programs**.

| Test Category                    | Programs |
| -------------------------------- | -------: |
| Arithmetic                       |       60 |
| Comparison / Boolean logic       |       30 |
| Let / lexical scoping            |       25 |
| Functions                        |       20 |
| Pairs / projections              |       10 |
| Recursive functions              |       10 |
| Explicit polymorphism            |       10 |
| Intentionally ill-typed programs |       20 |
| **Total**                        |  **185** |

The suite therefore evaluates both successful execution and static rejection.

---

# Evaluation Criteria

For successful programs, the test runner checks:

```text
Expected Type
      vs.
Observed Type

Expected Value
      vs.
Observed Value
```

For intentionally invalid programs, the test runner checks that:

```text
Expected Failure
      vs.
Actual Failure
```

This makes the evaluation more informative than simply counting whether programs execute.

---

# Evaluation Result

A successful run reports:

```text
MiniLang test suite: 185 / 185 programs passed
100% agreement between expected typing/evaluation outcomes and implementation behavior.
```

The reported **100% agreement applies specifically to the defined deterministic 185-program evaluation suite**.

It should not be interpreted as a proof that the implementation is universally correct for every possible MiniLang program.

The suite is an executable validation mechanism that complements the formal reasoning documented in the repository.

---

# Formal Semantics

The repository contains research-oriented semantics documentation:

```text
docs/semantics.md
```

It describes the relationship between the implementation and formal programming-language concepts.

Topics include:

* Core syntax
* Typing environments
* Typing judgments
* Function typing
* Function application
* Conditional typing
* Pair typing
* Explicit polymorphism
* Small-step evaluation intuition
* Progress
* Preservation
* Evaluation safety

For example:

```text
Γ ⊢ e : τ
```

represents the static typing judgment.

The documentation also relates the environment-based closure implementation to substitution-based operational semantics.

---

# Progress & Preservation

For the core simply typed subset, the project documents the standard safety properties:

### Progress

A closed, well-typed expression is either already a value or can take an evaluation step.

### Preservation

If:

```text
Γ ⊢ e : τ
```

and:

```text
e → e'
```

then:

```text
Γ ⊢ e' : τ
```

The automated evaluation suite complements this reasoning by testing representative valid and invalid programs.

It does **not** replace a formal proof.

---

# Research Methodology

The project follows a specification-to-implementation workflow:

```text
Formal Specification
        │
        ▼
Language Design
        │
        ▼
Lexer
        │
        ▼
Parser
        │
        ▼
AST
        │
        ▼
Type Checker
        │
        ▼
Evaluator
        │
        ▼
Evaluation Suite
        │
        ▼
Expected vs. Observed Behavior
        │
        ▼
Implementation Refinement
```

This structure allows the language specification and executable implementation to be studied together.

---

# Project Structure

```text
MiniLang/
│
├── build.sbt
├── project/
│   └── build.properties
│
├── src/
│   └── main/
│       └── scala/
│           └── minilang/
│               │
│               ├── Main.scala
│               ├── Runner.scala
│               ├── TestRunner.scala
│               │
│               ├── ast/
│               │   ├── Expr.scala
│               │   └── Types.scala
│               │
│               ├── lexer/
│               │   ├── Lexer.scala
│               │   └── Token.scala
│               │
│               ├── parser/
│               │   └── Parser.scala
│               │
│               ├── typing/
│               │   ├── TypeChecker.scala
│               │   └── TypeError.scala
│               │
│               └── eval/
│                   ├── Evaluator.scala
│                   └── Value.scala
│
├── examples/
│   ├── factorial.mini
│   ├── higher_order.mini
│   ├── polymorphic_identity.mini
│   └── type_error.mini
│
└── docs/
    ├── semantics.md
    └── research-notes.md
```

---

# Technologies

| Technology                    | Role                         |
| ----------------------------- | ---------------------------- |
| **Scala 3.3.4**               | Language implementation      |
| **sbt 1.10.2**                | Build and project management |
| **Recursive Descent Parsing** | Syntax analysis              |
| **AST**                       | Program representation       |
| **Static Type System**        | Compile-time validation      |
| **Closures & Environments**   | Runtime lexical scoping      |
| **Parametric Polymorphism**   | Generic typed functions      |
| **Formal Semantics**          | Language-design reasoning    |

---

# Requirements

* **JDK 17+**
* **JDK 21 recommended**
* **sbt 1.10.2+**

No database, cloud service, external dataset, or API is required.

---

# Installation

## 1. Clone the Repository

```bash
git clone https://github.com/Krushna018/MiniLang-Typed-Functional-Language-Interpreter.git
cd MiniLang-Typed-Functional-Language-Interpreter
```

## 2. Verify Java

```bash
java -version
```

Make sure JDK 17 or newer is installed.

## 3. Verify sbt

```bash
sbt --version
```

## 4. Compile

```bash
sbt compile
```

---

# Running MiniLang

## Run the Default Demo

```bash
sbt run
```

The default demo evaluates a program combining recursion, explicit polymorphism, and pairs.

Expected result:

```text
MiniLang source:
let id = fun[T](x: T): T => x in
let rec fact(n: Int): Int = if n == 0 then 1 else n * fact(n - 1) in
(id[Int](fact(5)), id[String]("typed"))

Result:
Type : (Int, String)
Value: (120, "typed")
```

---

## Run a One-Line Expression

```bash
sbt 'run let x = 10 in x * 4'
```

---

## Run a Source File

```bash
sbt 'run --file examples/factorial.mini'
```

You can similarly run:

```bash
sbt 'run --file examples/higher_order.mini'
```

or:

```bash
sbt 'run --file examples/polymorphic_identity.mini'
```

---

# Run the Evaluation Suite

Run:

```bash
sbt "runMain minilang.TestRunner"
```

Expected output:

```text
MiniLang test suite: 185 / 185 programs passed
100% agreement between expected typing/evaluation outcomes and implementation behavior.
```

This is the primary reproducibility command for the project's evaluation.

---

# Research Documentation

The repository includes two research-oriented documents.

### `docs/semantics.md`

Contains:

* Core syntax
* Typing judgments
* Type rules
* Operational-semantics discussion
* Progress reasoning
* Preservation reasoning
* Research limitations

### `docs/research-notes.md`

Contains:

* Research question
* Methodology
* Evaluation design
* 185-program test composition
* Error taxonomy
* Reproducibility information

---

# Design Principles

## 1. Specification and Implementation Alignment

The language is designed so that formal typing concepts have corresponding executable implementations.

```text
Formal Rule
     ↓
Type Checker
     ↓
Observed Behavior
```

---

## 2. Separation of Stages

Each stage has a focused responsibility:

```text
Lexer
  ↓
Parser
  ↓
AST
  ↓
Type Checker
  ↓
Evaluator
```

---

## 3. Static Validation Before Evaluation

The normal execution flow is:

```text
Source
  ↓
Parse
  ↓
Type Check
  ↓
Evaluate
```

Ill-typed programs are rejected before runtime evaluation.

---

## 4. Reproducible Evaluation

The project uses a deterministic in-code evaluation suite rather than depending on external datasets or services.

This makes the reported test-suite result straightforward to reproduce locally.

---

# Limitations

MiniLang is an **educational and research prototype**, not a production compiler.

The current implementation intentionally does not provide:

* Type inference
* Algebraic data types
* Pattern matching
* Subtyping
* Mutable state
* Effect systems
* Ownership / borrowing
* Higher-kinded types
* Asynchronous semantics
* Bytecode compilation
* Fully mechanized proofs

These limitations keep the language compact while providing clear directions for further research.

---

# Future Research Directions

Potential extensions include:

### Algebraic Data Types

Introduce user-defined structured data types.

### Pattern Matching

Add structural decomposition over algebraic data.

### Type Inference

Investigate reducing explicit type annotations while preserving static safety.

### Effect Systems

Extend the type system to track computational effects.

### Subtyping

Explore richer relationships between types.

### Ownership & Borrowing

Study static resource and memory-safety mechanisms.

### Higher-Kinded Types

Extend the polymorphic type system beyond first-order type variables.

### Proof Assistant Mechanization

Formalize the type system and safety properties in a proof assistant.

### Bytecode Compilation

Extend the interpreter into a compilation pipeline targeting bytecode.

---

# Project Status

**Completed**

The repository contains a complete working implementation of the designed MiniLang pipeline:

```text
Lexer
  ↓
Parser
  ↓
AST
  ↓
Static Type Checker
  ↓
Environment-Based Evaluator
```

The implementation includes:

* Typed functional expressions
* Lexical scoping
* Higher-order functions
* Recursive functions
* Pair types
* Explicit parametric polymorphism
* Structured static errors
* Formal semantics documentation
* Example programs
* Deterministic 185-program evaluation suite

---

# Author

**Krushna Tekane**

B.Tech Computer Science & Engineering

GitHub:
https://github.com/Krushna018/MiniLang-Typed-Functional-Language-Interpreter

---

## License

This project is intended for educational and research purposes.
