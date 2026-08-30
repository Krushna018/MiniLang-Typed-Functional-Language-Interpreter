# MiniLang — Statically Typed Functional Language & Interpreter

MiniLang is a compact research-oriented programming-language implementation in **Scala 3**. It was designed to explore how static typing, lexical scoping, higher-order functions, recursion, pair types, and explicit parametric polymorphism interact across a formal specification, executable type checker, and interpreter.

## Highlights

- 12+ language constructs / expression forms
- Lexer and recursive-descent parser
- Explicit AST
- Static type checker
- Environment-based interpreter
- Lexical scoping and higher-order functions
- Recursive functions
- Pair types and projections
- Explicit parametric polymorphism (`fun[T]...`, `id[Int](...)`)
- Structured static error taxonomy with 8+ categories
- Research notes with typing judgments and progress/preservation discussion
- Deterministic evaluation suite executing **185 valid and invalid programs**

## Requirements

- JDK 17+ (JDK 21 recommended)
- sbt 1.10+

## Run the demo

```bash
sbt run
```

Expected output is similar to:

```text
Type : (Int, String)
Value: (120, "typed")
```

Run a one-line MiniLang expression:

```bash
sbt 'run let x = 10 in x * 4'
```

Run a file:

```bash
sbt 'run --file examples/factorial.mini'
```

## Run the research/evaluation suite

```bash
sbt "runMain minilang.TestRunner"
```

The suite executes **185 programs** spanning arithmetic, Boolean logic, lexical scoping, functions, pairs, recursion, explicit polymorphism, and deliberately ill-typed inputs. A successful run prints:

```text
MiniLang test suite: 185 / 185 programs passed
100% agreement between expected typing/evaluation outcomes and implementation behavior.
```

## Example

```text
let id = fun[T](x: T): T => x in
let rec fact(n: Int): Int =
  if n == 0 then 1 else n * fact(n - 1)
in
(id[Int](fact(5)), id[String]("typed"))
```

## Architecture

```text
source text
   |
   v
 Lexer
   |
   v
 Parser ---> AST
              |\
              | \
              v  v
        Type Checker   Evaluator
              |           |
              v           v
            Type        Value
```

## Project structure

```text
MiniLang/
├── build.sbt
├── project/
│   └── build.properties
├── src/main/scala/minilang/
│   ├── Main.scala
│   ├── Runner.scala
│   ├── TestRunner.scala
│   ├── ast/
│   ├── lexer/
│   ├── parser/
│   ├── typing/
│   └── eval/
├── examples/
└── docs/
    ├── semantics.md
    └── research-notes.md
```

## Supported syntax (examples)

```text
42
true
"hello"
unit

let x = 10 in x + 2
if true then 1 else 0

fun(x: Int): Int => x + 1
let inc = fun(x: Int): Int => x + 1 in inc(41)

let rec fact(n: Int): Int =
  if n == 0 then 1 else n * fact(n - 1)
in fact(6)

(1, true)
fst((1, true))
snd((1, true))

let id = fun[T](x: T): T => x in id[Int](42)
```

## Notes on the quantitative claims

The project intentionally keeps its résumé-style metrics reproducible. The test-suite count comes directly from `TestRunner.scala`; it is not an estimated number. “100% agreement” means every case in this **defined deterministic suite** matches its expected typing/evaluation outcome after implementation and debugging. It does not claim universal correctness for all possible programs.

## Future research directions

Potential extensions include algebraic data types, pattern matching, type inference, effect systems, subtyping, ownership/borrowing, higher-kinded types, proof-assistant mechanization, and compilation to bytecode.
