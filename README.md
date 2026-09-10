# MiniLang — Statically Typed Functional Language & Interpreter

> A compact Scala 3 implementation of a statically typed functional programming language, built to explore lexical scoping, higher-order functions, recursion, pairs, and explicit parametric polymorphism.

MiniLang is a small but complete programming language implementation that connects **formal language semantics** with a working compiler-style pipeline:

**Source Code → Lexer → Parser → AST → Type Checker → Evaluator → Result**

The project focuses on understanding how programming-language features are represented, type-checked, and evaluated rather than simply building a syntax parser.

---

## ✨ Key Features

* 🔤 **Custom Lexer** — Tokenizes keywords, identifiers, literals, operators, comments, and symbols.
* 🌳 **Recursive-Descent Parser** — Converts source code into a structured Abstract Syntax Tree.
* 🧩 **Explicit AST** — Represents literals, variables, bindings, functions, applications, conditionals, pairs, projections, and operators.
* 🛡️ **Static Type Checking** — Detects type errors before program execution.
* 🧠 **Lexical Scoping** — Functions capture their surrounding environment through closures.
* 🔁 **Recursive Functions** — Supports recursive bindings using `let rec`.
* 🎯 **Higher-Order Functions** — Functions can be passed as arguments and returned as values.
* 📦 **Pair Types** — Supports pair construction and `fst` / `snd` projections.
* 🧬 **Explicit Parametric Polymorphism** — Supports universal types using `forall` and explicit type application.
* ⚡ **Call-by-Value Evaluation** — Programs are evaluated using environments and closures.
* ❌ **Structured Type Errors** — Provides specific errors for invalid applications, branches, operands, variables, pairs, and recursive definitions.
* 📝 **Formal Semantics** — Includes typing rules, evaluation intuition, and progress/preservation discussion.

---

## 🔤 MiniLang at a Glance

MiniLang supports a compact functional programming model including:

### Basic Types

```text
Int
Bool
String
Unit
```

### Functions

```text
Int -> Int
Bool -> Bool
(Int, Int) -> Int
```

### Expressions

```text
let bindings
let rec bindings
if / then / else
lambda functions
function application
pairs
fst / snd
unary operators
binary operators
```

### Polymorphism

MiniLang supports explicit polymorphic functions using universal types:

```text
forall T. T -> T
```

with explicit type application such as:

```text
id[Int](42)
id[String]("hello")
```

---

## 🏗️ Architecture

```text
              MiniLang Source
                    │
                    ▼
              ┌───────────┐
              │   Lexer   │
              └─────┬─────┘
                    │ Tokens
                    ▼
              ┌───────────┐
              │   Parser  │
              └─────┬─────┘
                    │ AST
                    ▼
              ┌──────────────┐
              │ Type Checker │
              └──────┬───────┘
                     │ Typed Program
                     ▼
              ┌────────────┐
              │ Evaluator  │
              └─────┬──────┘
                    │
                    ▼
              Value / Error
```

The implementation is separated into independent stages, making the language easier to understand, test, and extend.

---

## 🧬 Example Program

### Recursive Factorial

```text
let rec fact : Int -> Int =
  fun (n : Int) ->
    if n == 0 then
      1
    else
      n * fact (n - 1)
in
fact 5
```

The interpreter performs:

1. **Lexical analysis** of the source code.
2. **Parsing** into an AST.
3. **Static type checking** of the recursive function.
4. **Environment-based evaluation** using a recursive closure.
5. Produces the final integer value.

---

## 🔬 Higher-Order Functions

MiniLang treats functions as first-class values.

Example:

```text
let applyTwice : (Int -> Int) -> Int -> Int =
  fun (f : Int -> Int) ->
    fun (x : Int) ->
      f (f x)
in
applyTwice (fun (x : Int) -> x + 1) 10
```

This demonstrates:

* Functions as values
* Nested functions
* Function parameters
* Function application
* Lexical scoping

---

## 🧬 Explicit Polymorphism

MiniLang includes explicit parametric polymorphism through universal types.

Example:

```text
let id =
  fun [T] (x : T) -> x
in
id[Int](42)
```

The type system represents universal types using:

```text
TForAll
```

and performs type substitution during explicit type application.

This provides a small experimental foundation for studying concepts related to **System F-style polymorphism**.

---

## 🛡️ Static Type Checking

Programs are type-checked before evaluation.

Examples of errors handled include:

```text
Undefined variable
Type mismatch
Expected function
Expected polymorphic function
Expected boolean condition
Branch type mismatch
Invalid operand types
Expected pair
Recursive return mismatch
Invalid type annotation
```

For example, this program is rejected:

```text
let f : Int -> Int =
  fun (x : Int) -> x + 1
in
f true
```

The evaluator is therefore protected from many invalid operations before execution begins.

---

## ⚡ Evaluation Model

The evaluator uses **call-by-value, environment-based evaluation**.

It supports:

* Integer, Boolean, String, and Unit values
* Pair values
* Closures
* Recursive closures
* Lexical environments
* Higher-order function calls
* Arithmetic and comparison operations
* String concatenation
* Short-circuit `&&` and `||`
* Runtime errors such as division by zero

Functions capture their defining environment, providing proper lexical scoping.

---

## 🧪 Testing & Validation

MiniLang includes a deterministic test suite containing **185 programs** covering:

| Category                         |   Tests |
| -------------------------------- | ------: |
| Arithmetic                       |      60 |
| Comparison & Boolean             |      30 |
| Let & Scoping                    |      25 |
| Functions                        |      20 |
| Pairs                            |      10 |
| Recursion                        |      10 |
| Explicit Polymorphism            |      10 |
| Intentionally Ill-Typed Programs |      20 |
| **Total**                        | **185** |

The test runner validates both:

* Expected type and evaluation results for valid programs
* Correct rejection of intentionally ill-typed programs

The repository reports:

```text
MiniLang test suite: 185 / 185 programs passed
100% agreement between expected typing/evaluation outcomes
and implementation behavior.
```

> **Note:** The 100% figure refers to this defined 185-program validation suite; it is not a claim of universal language correctness.

---

## 🔬 Research & Formal Semantics

The project includes research-oriented documentation in `docs/`.

### `semantics.md`

Documents:

* Typing judgments
* Function and application rules
* Conditional expressions
* Pair operations
* Explicit polymorphism
* Evaluation intuition
* Progress and preservation reasoning
* Relationship between environment-based and substitution-style semantics

### `research-notes.md`

Documents:

* Research motivation
* Evaluation methodology
* Test-suite composition
* Error taxonomy
* Reproducibility considerations
* Current limitations

This makes MiniLang more than a basic interpreter by connecting the implementation with **programming-language theory and formal semantics**.

---

## 📁 Project Structure

```text
MiniLang/
├── src/main/scala/minilang/
│   ├── ast/
│   │   ├── Expr.scala
│   │   └── Types.scala
│   ├── lexer/
│   │   ├── Lexer.scala
│   │   └── Token.scala
│   ├── parser/
│   │   └── Parser.scala
│   ├── typing/
│   │   ├── TypeChecker.scala
│   │   └── TypeError.scala
│   ├── eval/
│   │   ├── Evaluator.scala
│   │   └── Value.scala
│   ├── Main.scala
│   ├── Runner.scala
│   └── TestRunner.scala
│
├── examples/
│   ├── factorial.mini
│   ├── higher_order.mini
│   ├── polymorphic_identity.mini
│   └── type_error.mini
│
├── docs/
│   ├── semantics.md
│   └── research-notes.md
│
├── build.sbt
└── README.md
```

---

## 🛠️ Tech Stack

* **Scala 3.3.4**
* **sbt 1.10.2**
* Recursive-descent parsing
* Abstract Syntax Trees
* Static type checking
* Environment-based interpretation
* Closures & recursive closures
* Parametric polymorphism
* Formal semantics

---

## 🚀 Getting Started

### Prerequisites

Install:

* JDK
* Scala 3
* sbt

### Clone the Repository

```bash
git clone <repository-url>
cd MiniLang
```

### Compile

```bash
sbt compile
```

### Run the Default Demo

```bash
sbt run
```

### Run a MiniLang File

```bash
sbt "run --file examples/factorial.mini"
```

### Run the Test Suite

```bash
sbt "runMain minilang.TestRunner"
```

---

## 📚 Example Programs

The repository includes ready-to-run examples demonstrating:

| Example                     | Concept                 |
| --------------------------- | ----------------------- |
| `factorial.mini`            | Recursion               |
| `higher_order.mini`         | Higher-order functions  |
| `polymorphic_identity.mini` | Parametric polymorphism |
| `type_error.mini`           | Static type errors      |

These examples provide a quick way to explore the language without writing programs from scratch.

---

## 🔮 Future Directions

Potential extensions include:

* Pattern matching
* Algebraic data types
* Type inference
* Hindley–Milner-style polymorphism
* Subtyping
* Effect systems
* Algebraic effects
* More advanced type-system features
* Bytecode or native-code compilation
* Formal mechanization using a proof assistant

---

## 📌 Project Status

**Status:** Functional research/learning interpreter

MiniLang currently provides a complete pipeline from source code to typed evaluation, with recursion, higher-order functions, pairs, explicit polymorphism, structured type errors, testing, and formal-semantic documentation.

---

## 👨‍💻 Author

**Krushna Tekane**

BTech Computer Science Engineering
Interested in Software Development, Programming Languages, AI/ML, and Systems

**GitHub:** `Krushna018/MiniLang-Typed-Functional-Language-Interpreter`
